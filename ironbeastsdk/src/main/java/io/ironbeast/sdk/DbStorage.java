package io.ironbeast.sdk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import static java.lang.Math.*;

import java.io.File;

// if (addReport("table", "hello world") == 1) {
//  addTable("table", "token");
// }
// JSONObject[] tables = getTables("table"); ==> {table, token}
// for each table, get `JSONArray arr = getReports("table", limit/30)`
// merged them -> {table, token, arr}
// send them ->
//
// 1. While insert a "row" to db
// if the current count is equal to 1, create row in "tables/destination" table(tName, token)
public class DbStorage {

    public DbStorage(Context context) {
        mDb = new DatabaseHandler(context);
    }

    // Assuming event have `table`, `token` and `data` fields
    public int addEvent(String table, String token, String data) {
        if (this.belowMemThreshold()) {
            // Log something
            // and do what ???(delete some of the records, delete all db, return outOfMemory-code)
        }
        int n = 0;
        try {
            SQLiteDatabase db = mDb.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(KEY_TABLE, table);
            cv.put(KEY_DATA, data);
            cv.put(KEY_CREATED_AT, System.currentTimeMillis());
            db.insert(REPORTS_TABLE, null, cv);
            // Create "table-row" in "tables" table
            if ((n = count(table)) == 1) {
                cv = new ContentValues();
                cv.put(KEY_TABLE, table);
                cv.put(KEY_TOKEN, token);
                db.insert(TABLES_TABLE, null, cv);
            }
        } catch (final SQLiteException e) {
            // TODO: logging
            mDb.delete();
        } finally {
            mDb.close();
        }
        return n;
    }

    public int count(final String table) {
        int n = 0;
        Cursor c = null;
        SQLiteDatabase db = null;
        try {
            db = mDb.getReadableDatabase();
            c = db.rawQuery(String.format("SELECT COUNT(*) FROM %s WHERE %s=?",
                            REPORTS_TABLE, KEY_TABLE), new String[]{table});
            c.moveToFirst();
            n = c.getInt(0);
        } catch (final SQLiteException e) {
            // TODO: logging
            mDb.delete();
        } finally {
            if (null != c) c.close();
            if (null != db) db.close();
        }
        return n;
    }

    public String[] getEvents(String table, int limit) {
        Cursor c = null;
        String data = null;
        String lastId = null;
        try {
            final SQLiteDatabase db = mDb.getReadableDatabase();
            // WORKS:
            // c = db.rawQuery("SELECT * FROM " + REPORTS_TABLE + " ORDER BY ? ASC LIMIT ?",
            // new String[]{KEY_CREATED_AT, String.valueOf(limit)});
            c = db.rawQuery("SELECT * FROM " + REPORTS_TABLE + " WHERE ? = ?",
                    new String[]{KEY_TABLE, table});
//            c = db.rawQuery("SELECT * FROM " + REPORTS_TABLE + " WHERE ? = ?",
//                    new String[]{KEY_TABLE, table, KEY_CREATED_AT, String.valueOf(limit)});
            final JSONArray arr = new JSONArray();
            while (c.moveToNext()) {
                if (c.isLast()) {
                    lastId = c.getString(c.getColumnIndex(REPORTS_TABLE + "_id"));
                }
                arr.put(c.getString(c.getColumnIndex(KEY_DATA)));
            }
            if (arr.length() > 0) {
                data = arr.toString();
            }
        } catch (final SQLiteException e) {
            lastId = data = null;
        } finally {
            if (null != c) c.close();
            mDb.close();
        }
        if (lastId != null && data != null) {
            final String[] ret = {lastId, data};
            return ret;
        }
        return null;
    }

//    public String[] getTables() {
//        Cursor c = null;
//        try {
//
//        } catch (final SQLiteException e) {
//
//        } finally {
//            if (null != c) c.close();
//            mDb.close();
//        }
//    }

    // Override in testing mode
    protected boolean belowMemThreshold() {
        return mDb.belowMemThreshold();
    }

    private final DatabaseHandler mDb;
    public static final String KEY_DATA = "data";
    public static final String KEY_TABLE = "table_name";
    public static final String KEY_TOKEN = "token";
    public static final String TABLES_TABLE = "tables";
    public static final String REPORTS_TABLE = "reports";
    public static final String KEY_CREATED_AT = "created_at";

    private static final String DATABASE_NAME = "ironbeast";
    private static final int DATABASE_VERSION = 4;

    private static final String CREATE_REPORTS_TABLE =
            "CREATE TABLE " + REPORTS_TABLE + " (" + REPORTS_TABLE + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_DATA + " STRING NOT NULL, " +
                    KEY_TABLE + " STRING NOT NULL, " +
                    KEY_CREATED_AT + " INTEGER NOT NULL)";
    private static final String CREATE_TABLES_TABLE =
            "CREATE TABLE " + TABLES_TABLE + " (" + TABLES_TABLE + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_TABLE + " STRING NOT NULL UNIQUE, " +
                    KEY_TOKEN + " STRING NOT NULL);";
    private static final String REPORTS_INDEXING =
            "CREATE INDEX IF NOT EXISTS time_idx ON " + REPORTS_TABLE +
                    " (" + KEY_CREATED_AT + ");";
    private static final String TABLES_INDEXING =
            "CREATE INDEX IF NOT EXISTS time_idx ON " + TABLES_TABLE +
                    " (" + KEY_CREATED_AT + ");";


    private static class DatabaseHandler extends SQLiteOpenHelper {
        DatabaseHandler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mDatabaseFile = context.getDatabasePath(DATABASE_NAME);
            mConfig = IBConfig.getInstance(context);
        }

        public void delete() {
            close();
            mDatabaseFile.delete();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Log
            db.execSQL(CREATE_TABLES_TABLE);
            db.execSQL(CREATE_REPORTS_TABLE);
            db.execSQL(REPORTS_INDEXING);
            db.execSQL(TABLES_INDEXING);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Log
            // Drop and create
            db.execSQL("DROP TABLE IF EXISTS " + TABLES_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + REPORTS_TABLE);
            onCreate(db);
        }

        public boolean belowMemThreshold() {
            // TODO: move to IBConfig
            // An integer number of bytes. IronBeast attempts to limit the size of its persistent data
            // queue based on the storage capacity of the device, but will always allow queing below this limit.
            // Higher values will take up more storage even when user storage is very full.
            int minimumDatabaseLimit = 20 * 1024 * 1024; // 20 Mb
            if (mDatabaseFile.exists()) {
                return max(mDatabaseFile.getUsableSpace(), minimumDatabaseLimit) >= mDatabaseFile.length();
            }
            return true;
        }

        private final File mDatabaseFile;
        private final IBConfig mConfig;
    }
}
