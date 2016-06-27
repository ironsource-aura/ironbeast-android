package io.ironbeast.sdk;

import io.ironbeast.sdk.DbAdapter.*;
import io.ironbeast.sdk.StorageService.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
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

    final Table table = new Table("table", "token") {
        @Override
        public boolean equals(Object obj) {
            Table table = (Table) obj;
            return this.name.equals(table.name) && this.token.equals(table.token);
        }
    };
    final DatabaseHandler handler = mock(DatabaseHandler.class);
    final Context context = mock(MockContext.class);
    final DbAdapter adapter = new DbAdapter(context) {
        @Override
        protected DatabaseHandler getSQLHandler(Context context) {
            return handler;
        }

        @Override
        protected boolean belowDatabaseLimit() {
            return true;
        }
    };

    @Before
    public void clearMocks() {
        reset(handler);
    }

    // If everything goes well, it should return the number if rows
    // and make sure it close the db connection and the cursor too.
    @Test
    public void countSuccess() {
        SQLiteStatement stmt = mock(SQLiteStatement.class);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(db.compileStatement(anyString())).thenReturn(stmt);
        when(stmt.simpleQueryForLong()).thenReturn(10L);
        when(handler.getReadableDatabase()).thenReturn(db);
        assertEquals(adapter.count(table), 10);
        verify(db, times(1)).close();
    }

    // When Adapter encounter SQLiteException it'll return 0 and discard the database
    @Test
    public void countFailed() {
        SQLiteStatement stmt = mock(SQLiteStatement.class);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(db.compileStatement(anyString())).thenThrow(new SQLiteException());
        when(handler.getReadableDatabase()).thenReturn(db);
        assertEquals(adapter.count(table), 0);
        verify(db, times(1)).close();
        verify(handler, times(1)).delete();
    }

    @Test
    public void getTables() {
        Cursor cursor = mock(Cursor.class);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(db.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(handler.getReadableDatabase()).thenReturn(db);
        // 3 iterations
        when(cursor.moveToNext()).thenReturn(true, true, true, false);
        when(cursor.getString(anyInt())).thenReturn("table1", "token1", "table2", "token2", "table3", "token3");
        List<Table> tables = adapter.getTables();
        assertEquals(tables.size(), 3);
        int i = 1;
        for (Table table : tables) {
            assertEquals(table.name, "table" + i);
            assertEquals(table.token, "token" + i++);
        }
        verify(cursor, times(1)).close();
        verify(handler, times(1)).close();
    }

    @Test
    public void deleteTable() {
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(handler.getWritableDatabase()).thenReturn(db);
        adapter.deleteTable(table);
        verify(db, times(1)).delete(eq(DbAdapter.TABLES_TABLE), anyString(), any(String[].class));
    }

    @Test
    public void getEvents() {
        Cursor cursor = mock(Cursor.class);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(db.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(handler.getReadableDatabase()).thenReturn(db);
        // 2 iterations
        when(cursor.moveToNext()).thenReturn(true, true, false);
        when(cursor.isLast()).thenReturn(false, true);
        // DATA, ID, DATA
        when(cursor.getString(anyInt())).thenReturn("foo", "2", "bar");
        Batch batch = adapter.getEvents(table, 1);
        assertEquals(batch.events.toString(), "[foo, bar]");
        assertEquals(batch.lastId, "2");
    }

    @Test
    public void deleteEvents() {
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(handler.getWritableDatabase()).thenReturn(db);
        adapter.deleteEvents(table, "100");
        verify(db, times(1)).delete(eq(DbAdapter.REPORTS_TABLE), anyString(), any(String[].class));
    }

    // Should addEvent to REPORTS_TABLE and return the number of rows
    // related to the given `Table`
    @Test
    public void addEvent1() {
        SQLiteStatement stmt = mock(SQLiteStatement.class);
        when(stmt.simpleQueryForLong()).thenReturn(29L);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(handler.getWritableDatabase()).thenReturn(db);
        when(db.compileStatement(anyString())).thenReturn(stmt);
        assertEquals(adapter.addEvent(table, "foo bar"), 29);
        verify(db, times(1)).insert(eq(DbAdapter.REPORTS_TABLE), isNull(String.class),
                any(ContentValues.class));
    }

    // When `addEvent()` trigger table creation
    @Test
    public void addEvent2() {
        SQLiteStatement stmt = mock(SQLiteStatement.class);
        when(stmt.simpleQueryForLong()).thenReturn(1L);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(handler.getWritableDatabase()).thenReturn(db);
        when(db.compileStatement(anyString())).thenReturn(stmt);
        adapter.addEvent(table, "foo bar");
        verify(db, times(1)).insert(eq(DbAdapter.REPORTS_TABLE), isNull(String.class),
                any(ContentValues.class));
        verify(db, times(1)).insertWithOnConflict(eq(DbAdapter.TABLES_TABLE),
                isNull(String.class), any(ContentValues.class), eq(SQLiteDatabase.CONFLICT_IGNORE));
    }

    @Test
    public void getInstanceTest() {
        DbAdapter adapter1 = DbAdapter.getInstance(context);
        DbAdapter adapter2 = DbAdapter.getInstance(context);
        assertTrue(adapter1 == adapter2);
    }
}
