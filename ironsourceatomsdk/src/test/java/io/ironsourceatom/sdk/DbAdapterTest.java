package io.ironsourceatom.sdk;

import io.ironsourceatom.sdk.DbAdapter.*;
import io.ironsourceatom.sdk.StorageService.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.test.mock.MockContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static junit.framework.Assert.*;

import org.mockito.runners.MockitoJUnitRunner;


import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DbAdapterTest {

    @Before public void clearMocks() {
        reset(mHandler);
    }

    // If everything goes well, it should return the number if rows
    // and make sure it close the db connection and the cursor too.
    @Test public void countSuccess() {
        Cursor cursor = mock(Cursor.class);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(db.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(cursor.getInt(0)).thenReturn(10);
        when(mHandler.getReadableDatabase()).thenReturn(db);
        assertEquals(mAdapter.count(mTable), 10);
        verify(cursor, times(1)).close();
        verify(db, times(1)).close();
    }

    // When Adapter encounter SQLiteException it'll return 0 and discard the database
    @Test public void countFailed() {
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(db.rawQuery(anyString(), any(String[].class))).thenThrow(new SQLiteException());
        when(mHandler.getReadableDatabase()).thenReturn(db);
        assertEquals(mAdapter.count(mTable), 0);
        verify(db, times(1)).close();
        verify(mHandler, times(1)).delete();
    }

    @Test public void getTables() {
        Cursor cursor = mock(Cursor.class);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(db.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(mHandler.getReadableDatabase()).thenReturn(db);
        // 3 iterations
        when(cursor.moveToNext()).thenReturn(true, true, true, false);
        when(cursor.getString(anyInt())).thenReturn("table1", "token1", "table2", "token2", "table3", "token3");
        List<Table> tables = mAdapter.getTables();
        assertEquals(tables.size(), 3);
        int i = 1;
        for (Table table: tables) {
            assertEquals(table.name, "table" + i);
            assertEquals(table.token, "token" + i++);
        }
        verify(cursor, times(1)).close();
        verify(mHandler, times(1)).close();
    }

    @Test public void deleteTable() {
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(mHandler.getWritableDatabase()).thenReturn(db);
        mAdapter.deleteTable(mTable);
        verify(db, times(1)).delete(eq(DbAdapter.TABLES_TABLE), anyString(), any(String[].class));
    }

    @Test public void getEvents() {
        Cursor cursor = mock(Cursor.class);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(db.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(mHandler.getReadableDatabase()).thenReturn(db);
        // 2 iterations
        when(cursor.moveToNext()).thenReturn(true, true, false);
        when(cursor.isLast()).thenReturn(false, true);
        // DATA, ID, DATA
        when(cursor.getString(anyInt())).thenReturn("foo", "2", "bar");
        Batch batch = mAdapter.getEvents(mTable, 1);
        assertEquals(batch.events.toString(), "[foo, bar]");
        assertEquals(batch.lastId, "2");
    }

    @Test public void deleteEvents() {
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(mHandler.getWritableDatabase()).thenReturn(db);
        mAdapter.deleteEvents(mTable, "100");
        verify(db, times(1)).delete(eq(DbAdapter.REPORTS_TABLE), anyString(), any(String[].class));
    }

    // Should addEvent to REPORTS_TABLE and return the number of rows
    // related to the given `Table`
    @Test public void addEvent1() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.getInt(0)).thenReturn(29);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(mHandler.getWritableDatabase()).thenReturn(db);
        when(db.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        assertEquals(mAdapter.addEvent(mTable, "foo bar"), 29);
        verify(db, times(1)).insert(eq(DbAdapter.REPORTS_TABLE), isNull(String.class),
                any(ContentValues.class));
    }

    // When `addEvent()` trigger table creation
    @Test public void addEvent2() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.getInt(0)).thenReturn(1);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(mHandler.getWritableDatabase()).thenReturn(db);
        when(db.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        mAdapter.addEvent(mTable, "foo bar");
        verify(db, times(1)).insert(eq(DbAdapter.REPORTS_TABLE), isNull(String.class),
                any(ContentValues.class));
        verify(db, times(1)).insertWithOnConflict(eq(DbAdapter.TABLES_TABLE),
                isNull(String.class), any(ContentValues.class), eq(SQLiteDatabase.CONFLICT_IGNORE));
    }

    final Table mTable = new Table("table", "token") {
        @Override
        public boolean equals(Object obj) {
            Table table = (Table) obj;
            return this.name.equals(table.name) && this.token.equals(table.token);
        }
    };
    final DatabaseHandler mHandler = mock(DatabaseHandler.class);
    final Context mContext = mock(MockContext.class);
    final DbAdapter mAdapter = new DbAdapter(mContext) {
        @Override
        protected DatabaseHandler getSQLHandler(Context context) { return mHandler; }
        @Override
        protected boolean belowDatabaseLimit() { return true; }
    };
}
