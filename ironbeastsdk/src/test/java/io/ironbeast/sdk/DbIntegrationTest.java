package io.ironbeast.sdk;

import io.ironbeast.sdk.StorageService.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.*;

/**
 * DbAdapter integration with SQLite test cases.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 18, manifest = Config.NONE)
public class DbIntegrationTest {

    @Before public void setUp() throws Exception {
        mAdapter = new DbAdapter(RuntimeEnvironment.application);
    }

    @Test public void addEvent() {
        assertEquals(mAdapter.getTables().size(), 0);
        assertEquals(mAdapter.addEvent(mTable, DATA), 1);
        List<Table> tables = mAdapter.getTables();
        assertEquals(tables.size(), 1);
        assertEquals(tables.get(0).name, mTable.name);
        assertEquals(tables.get(0).token, mTable.token);
    }

    @Test public void getEvents() {
        String[] events = new String[]{"foo", "bar", "baz"};
        for (String event: events) mAdapter.addEvent(mTable, event);
        Batch batch = mAdapter.getEvents(mTable, Integer.MAX_VALUE);
        assertEquals(batch.lastId, String.valueOf(events.length));
        for (int i = 0; i < events.length; i++) {
            assertEquals(events[i], batch.events.get(i));
        }
    }

    @Test public void deleteEvents() {
        Batch batch;
        int n = 10;
        for (int i = 1; i <= n; i++) mAdapter.addEvent(mTable, DATA);
        batch = mAdapter.getEvents(mTable, Integer.MAX_VALUE);
        assertEquals(batch.lastId, String.valueOf(n));
        assertEquals(mAdapter.deleteEvents(mTable, String.valueOf(n/2)), n/2);
        batch = mAdapter.getEvents(mTable, Integer.MAX_VALUE);
        assertEquals(batch.lastId, String.valueOf(n));
        assertEquals(batch.events.size(), n/2);
    }

    @Test public void deleteTable() {
        Table table1 = new Table("ibsdk", "token");
        mAdapter.addEvent(mTable, DATA);
        assertEquals(mAdapter.getTables().size(), 1);
        mAdapter.addEvent(table1, DATA);
        assertEquals(mAdapter.getTables().size(), 2);
        // Delete the first one
        mAdapter.deleteTable(mTable);
        List<Table> tables = mAdapter.getTables();
        assertEquals(tables.size(), 1);
        assertEquals(tables.get(0).name, table1.name);
        assertEquals(tables.get(0).token, table1.token);
        // Delete the second table
        mAdapter.deleteTable(table1);
        // Should be empty
        assertEquals(mAdapter.getTables().size(), 0);
    }

    @Test public void count() {
        Table table1 = new Table("ibsdk", "token");
        int n = 100;
        for (int i = 0; i < n; i++) {
            if (i < n/2) mAdapter.addEvent(table1, DATA);
            mAdapter.addEvent(mTable, DATA);
        }
        assertEquals(mAdapter.count(mTable), n);
        assertEquals(mAdapter.count(table1), n / 2);
        assertEquals(mAdapter.count(null), n + n/2);
    }

    @Test public void vacuum() {
        int n = 80;
        for (int i = 0; i < n; i++) mAdapter.addEvent(mTable, DATA);
        assertEquals(mAdapter.count(mTable), n);
        mAdapter.vacuum();
        assertEquals(mAdapter.count(mTable), 64);
    }

    DbAdapter mAdapter;
    final Table mTable = new Table("a8m", "token");
    final String DATA = "foobarbaz";
}