package com.ironsource.mobilcore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

public class DbStorage {

    public int insert(String table, String data) {
        // Test if we're below threshold
        // Get table name?(if we'll wrap table with enum Table)
        return 1;
    }

//    private final DatabaseHandler mDb;
    public static final String KEY_DATA = "data";
    public static final String KEY_NAME = "name";
    public static final String KEY_TOKEN = "token";
    public static final String TABLES_TABLE = "tables";
    public static final String REPORTS_TABLE = "reports";
    public static final String KEY_CREATED_AT = "created_at";

    private static final String DATABASE_NAME = "ironbeast";
    private static final int DATABASE_VERSION = 4;

    private static final String CREATE_REPORTS_TABLE =
            "CREATE TABLE " + REPORTS_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_DATA + " STRING NOT NULL, " +
                    KEY_CREATED_AT + " INTEGER NOT NULL);";
    private static final String CREATE_TABLES_TABLE =
            "CREATE TABLE " + TABLES_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_NAME + " STRING NOT NULL, " +
                    KEY_TOKEN + " STRING NOT NULL, " +
                    KEY_CREATED_AT + " INTEGER NOT NULL);";
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

        private final File mDatabaseFile;
        private final IBConfig mConfig;
    }
}
