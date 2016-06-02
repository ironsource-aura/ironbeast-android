package io.ironsourceatom.sdk;

import io.ironsourceatom.sdk.StorageService.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * DbAdapter integration with SQLite test cases.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 18, manifest = Config.NONE)
public class DbIntegrationTest {


    DbAdapter adapter;
    final Table table = new Table("a8m", "token");
    final String DATA = "foobarbaz";

    @Before public void setUp() throws Exception {
        adapter = new DbAdapter(RuntimeEnvironment.application);
    }

    @Test public void addEvent() {
        assertEquals(adapter.getTables().size(), 0);
        assertEquals(adapter.addEvent(table, DATA), 1);
        List<Table> tables = adapter.getTables();
        assertEquals(tables.size(), 1);
        assertEquals(tables.get(0).name, table.name);
        assertEquals(tables.get(0).token, table.token);
    }

    @Test public void getEvents() {
        String[] events = new String[]{"foo", "bar", "baz"};
        for (String event: events) adapter.addEvent(table, event);
        Batch batch = adapter.getEvents(table, Integer.MAX_VALUE);
        assertEquals(batch.lastId, String.valueOf(events.length));
        for (int i = 0; i < events.length; i++) {
            assertEquals(events[i], batch.events.get(i));
        }
    }

    @Test public void deleteEvents() {
        Batch batch;
        int n = 10;
        for (int i = 1; i <= n; i++) adapter.addEvent(table, DATA);
        batch = adapter.getEvents(table, Integer.MAX_VALUE);
        assertEquals(batch.lastId, String.valueOf(n));
        assertEquals(adapter.deleteEvents(table, String.valueOf(n/2)), n/2);
        batch = adapter.getEvents(table, Integer.MAX_VALUE);
        assertEquals(batch.lastId, String.valueOf(n));
        assertEquals(batch.events.size(), n/2);
    }

    @Test public void deleteTable() {
        Table table1 = new Table("ibsdk", "token");
        adapter.addEvent(table, DATA);
        assertEquals(adapter.getTables().size(), 1);
        adapter.addEvent(table1, DATA);
        assertEquals(adapter.getTables().size(), 2);
        // Delete the first one
        adapter.deleteTable(table);
        List<Table> tables = adapter.getTables();
        assertEquals(tables.size(), 1);
        assertEquals(tables.get(0).name, table1.name);
        assertEquals(tables.get(0).token, table1.token);
        // Delete the second table
        adapter.deleteTable(table1);
        // Should be empty
        assertEquals(adapter.getTables().size(), 0);
    }

    @Test public void count() {
        Table table1 = new Table("ibsdk", "token");
        int n = 100;
        for (int i = 0; i < n; i++) {
            if (i < n/2) adapter.addEvent(table1, DATA);
            adapter.addEvent(table, DATA);
        }
        assertEquals(adapter.count(table), n);
        assertEquals(adapter.count(table1), n / 2);
        assertEquals(adapter.count(null), n + n/2);
    }

    @Test public void vacuum() {
        int n = 80;
        for (int i = 0; i < n; i++) adapter.addEvent(table, DATA);
        assertEquals(adapter.count(table), n);
        adapter.vacuum();
        assertEquals(adapter.count(table), 64);
    }

}