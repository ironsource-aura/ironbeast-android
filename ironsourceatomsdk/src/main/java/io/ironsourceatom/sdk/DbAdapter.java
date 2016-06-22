package io.ironsourceatom.sdk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

class DbAdapter implements StorageService {



    /**
     * Do not call directly. You should use DbAdapter.getInstance()
     */
    public DbAdapter(Context context) {
        mDb = getSQLHandler(context);
    }

    /**
     * Use this to get a singleton instance of DbAdapter instead of creating
     * one directly for yourself.
     */
    public static DbAdapter getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (null == sInstance) {
                sInstance = new DbAdapter(context);
            }
        }
        return sInstance;
    }

    /**
     * Insert event to "reports" table.
     * if it's the first member that related to the given Table, we create
     * a new destination/table(contains name and token) in the "tables" table.
     * @param table
     * @param data
     * @return number of rows in "records" related to the given table.
     */
    public int addEvent(Table table, String data) {
        if (!this.belowDatabaseLimit()) {
            Logger.log(TAG, "Database file is above the limit", Logger.SDK_DEBUG);
            vacuum();
        }
        int n = 0;
        Cursor c = null;
        try {
            SQLiteDatabase db = mDb.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(KEY_TABLE, table.name);
            cv.put(KEY_DATA, data);
            cv.put(KEY_CREATED_AT, System.currentTimeMillis());
            db.insert(REPORTS_TABLE, null, cv);
            SQLiteStatement stmt = db.compileStatement("SELECT COUNT(*) FROM ? WHERE ?=?");
            stmt.bindString(1, REPORTS_TABLE);
            stmt.bindString(2, KEY_TABLE);
            stmt.bindString(3, table.name);
            n=(int)stmt.simpleQueryForLong();

            if (n == 1) {
                cv = new ContentValues();
                cv.put(KEY_TABLE, table.name);
                cv.put(KEY_TOKEN, table.token);
                db.insertWithOnConflict(TABLES_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
            }
        } catch (final SQLiteException e) {
            Logger.log(TAG, "Failed to insert event to 'records' table", Logger.SDK_DEBUG);
            mDb.delete();
        } finally {
            if (null != c) c.close();
            mDb.close();
        }
        return n;
    }

    /**
     * Get the number of records that sit in the "reports" table and related to
     * to the given table.
     * @param table
     * @return
     */
    public int count(Table table) {
        int n = 0;
        SQLiteDatabase db = null;
        try {
            db = mDb.getReadableDatabase();

            String qs = "SELECT COUNT(*) FROM ?";
            if (table != null) {
                qs += " WHERE ? = ?";
            }
            SQLiteStatement stmt = db.compileStatement(qs);
            stmt.bindString(1, REPORTS_TABLE);

            if (table != null) {
                stmt.bindString(2, KEY_TABLE);
                stmt.bindString(3, table.name);
            }
            n = (int)stmt.simpleQueryForLong();
        } catch (final SQLiteException e) {
            Logger.log(TAG, "Failed to count records in table: " + table.name, Logger.SDK_DEBUG);
            mDb.delete();
        } finally {
            if (null != db) db.close();
        }
        return n;
    }

    /**
     * Get table object and int as a limit, and return a "batch" of events.
     * @param table
     * @param limit
     * @return Batch object contains List of events and "lastId" as a String(that
     * will be used later to clean up this batch).
     */
    public Batch getEvents(Table table, int limit) {
        Cursor c = null;
        String lastId = null;
        List<String> events = null;
        try {
            final SQLiteDatabase db = mDb.getReadableDatabase();
            c = db.rawQuery("SELECT * FROM ? WHERE ?=? ORDER BY ? ASC LIMIT ?",
                     new String[]{REPORTS_TABLE, KEY_TABLE, table.name, KEY_CREATED_AT, String.valueOf(limit)});
            events = new ArrayList<>();
            while (c.moveToNext()) {
                if (c.isLast()) {
                    lastId = c.getString(c.getColumnIndex(REPORTS_TABLE + "_id"));
                }
                events.add(c.getString(c.getColumnIndex(KEY_DATA)));
            }
        } catch (final SQLiteException e) {
            Logger.log(TAG, "Failed to get a events of table" + table.name, Logger.SDK_DEBUG);
            lastId = null;
            events = null;
        } finally {
            if (null != c) c.close();
            mDb.close();
        }
        if (lastId != null && events != null) {
            return new Batch(lastId, events);
        }
        return null;
    }

    /**
     * Get list of all "destinations/tables" that sit in "tables" table.
     * @return List of tables contains "name" and "token"
     */
    public List<Table> getTables() {
        Cursor c = null;
        List<Table> tables = new ArrayList<>();
        try {
            final SQLiteDatabase db = mDb.getReadableDatabase();
            c = db.rawQuery("SELECT * FROM ?", new String[]{TABLES_TABLE});
            while (c.moveToNext()) {
                String name = c.getString(c.getColumnIndex(KEY_TABLE));
                String token = c.getString(c.getColumnIndex(KEY_TOKEN));
                tables.add(new Table(name, token));
            }
        } catch (final SQLiteException e) {
            Logger.log(TAG, "Failed to get all tables" + e.getMessage(), Logger.SDK_DEBUG);
        } finally {
            if (null != c) c.close();
            mDb.close();
        }
        return tables;
    }

    /**
     * Remove events from records table that related to the given "table/destination"
     * and with an id that less than or equal to the "lastId"
     * @param table
     * @param lastId
     * @return the number of rows affected
     */
    public int deleteEvents(Table table, String lastId) {
        int n = 0;
        try {
            final SQLiteDatabase db = mDb.getWritableDatabase();
            n = db.delete(REPORTS_TABLE, String.format("%s=? AND %s_id <= ?", KEY_TABLE, REPORTS_TABLE),
                    new String[]{table.name, lastId});
        } catch (final SQLiteException e) {
            Logger.log(TAG, "Failed to clean up events from table: " + table.name, Logger.SDK_DEBUG);
            mDb.delete();
        } finally {
            mDb.close();
        }
        return n;
    }

    /**
     * Getting table object and delete it from the "destinations/tables" table.
     * @param table
     */
    public void deleteTable(Table table) {
        try {
            final SQLiteDatabase db = mDb.getWritableDatabase();
            db.delete(TABLES_TABLE, String.format("%s=?", KEY_TABLE), new String[]{table.name});
        } catch (final SQLiteException e) {
            Logger.log(TAG, "Failed to delete table:" + table.name, Logger.SDK_DEBUG);
            mDb.delete();
        } finally {
            mDb.close();
        }
    }

    /**
     * Delete the oldest 20 percent rows from "Reports" table
     * and run vacuum, to reduces the file fragmentation.
     */
    public void vacuum() {
        int nRows = count(null);
        int limit = (int) (((double) nRows / 100) * 20);
        try {
            final SQLiteDatabase db = mDb.getWritableDatabase();
            final String id = REPORTS_TABLE + "_id";
            SQLiteStatement stmt = db.compileStatement("DELETE FROM ?"+
                    " WHERE ? IN (SELECT ?"+
                    " FROM ?"+
                    " ORDER BY ? "+ " ASC" +
                    " LIMIT ? );");
            stmt.bindString(1, REPORTS_TABLE);
            stmt.bindString(2, id);
            stmt.bindString(3, id);
            stmt.bindString(4, REPORTS_TABLE);
            stmt.bindString(5, KEY_CREATED_AT);
            stmt.bindDouble(6, limit);
            stmt.execute();

            db.execSQL("VACUUM");
        } catch (SQLiteException e) {
            Logger.log(TAG, "Failed to shrink and vacuum db:" + e, Logger.SDK_DEBUG);
            mDb.delete();
        } finally {
            mDb.close();
        }
    }

    /**
     * For testing purpose. to allow mocking this behavior.
     */
    protected boolean belowDatabaseLimit() { return mDb.belowDatabaseLimit(); }
    protected DatabaseHandler getSQLHandler(Context context) { return new DatabaseHandler(context); }

    private static final Object sInstanceLock = new Object();
    private static DbAdapter sInstance;
    private final DatabaseHandler mDb;
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "ironbeast";
    private static final String TAG = "DbAdapter";
    public static final String KEY_DATA = "data";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_TABLE = "table_name";
    public static final String KEY_CREATED_AT = "created_at";
    public static final String TABLES_TABLE = "tables";
    public static final String REPORTS_TABLE = "reports";

    /**
     * Private subclass that take care of opening(or creating), upgrading
     * or deleting the database.
     */
    protected static class DatabaseHandler extends SQLiteOpenHelper {


        private final File databaseFile;
        private final IsaConfig mConfig;
        DatabaseHandler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            databaseFile = context.getDatabasePath(DATABASE_NAME);
            mConfig = IsaConfig.getInstance(context);
        }

        public void delete() {
            close();
            databaseFile.delete();
        }

        /**
         * Called when the database is created for the FIRST time.
         * If a database already exists, this method will NOT be called.
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            Logger.log(TAG, "Creating the IronBeastSdk database", Logger.SDK_DEBUG);

            SQLiteStatement stmt = db.compileStatement("CREATE TABLE ? (?_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "? STRING NOT NULL, ? STRING NOT NULL, ? INTEGER NOT NULL);");
            stmt.bindString(1, REPORTS_TABLE);
            stmt.bindString(2, REPORTS_TABLE);
            stmt.bindString(3, KEY_DATA);
            stmt.bindString(4, KEY_TABLE);
            stmt.bindString(5, KEY_CREATED_AT);
            stmt.execute();

            SQLiteStatement stmt1 = db.compileStatement("CREATE TABLE ? (?_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "? STRING NOT NULL UNIQUE, ? STRING NOT NULL);");
            stmt1.bindString(1, TABLES_TABLE);
            stmt1.bindString(2, TABLES_TABLE);
            stmt1.bindString(3, KEY_TABLE);
            stmt1.bindString(4,  KEY_TOKEN);
            stmt1.execute();


            SQLiteStatement stmt2 = db.compileStatement("CREATE INDEX IF NOT EXISTS time_idx ON ? (?);");
            stmt2.bindString(1, REPORTS_TABLE);
            stmt2.bindString(2, KEY_CREATED_AT);
            stmt2.execute();

        }

        /**
         * Called when the database needs to be upgraded.
         * This method will only be called if the database already exists on disk,
         * but the DATABASE_VERSION is different than the version of the database that exists on disk.
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion != newVersion) {
                Logger.log(TAG, "Upgrading the IronBeastSdk database", Logger.SDK_DEBUG);
                SQLiteStatement stmt = db.compileStatement("DROP TABLE IF EXISTS ?");
                stmt.bindString(1, TABLES_TABLE);
                stmt.execute();

                SQLiteStatement stmt1 = db.compileStatement("DROP TABLE IF EXISTS ?");
                stmt1.bindString(1, REPORTS_TABLE);
                stmt1.execute();

                onCreate(db);
            }
        }

        /**
         * Test if the persistent data amount below the "databaseLimit" only if
         * there is not enough free space in the storage capacity of the device.
         * @return
         */
        public boolean belowDatabaseLimit() {
            if (databaseFile.exists()) {
                long limit = Math.max(databaseFile.getUsableSpace(), mConfig.getMaximumDatabaseLimit());
                return limit >= databaseFile.length();
            }
            return true;
        }

    }
}
