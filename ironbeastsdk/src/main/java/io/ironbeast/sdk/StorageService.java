package io.ironbeast.sdk;

import java.util.List;

public interface StorageService {
    List<Table> getTables();
    void deleteTable(Table table);
    int count(Table table);
    Batch getEvents(Table table, int limit);
    int deleteEvents(Table table, String lastId);
    int addEvent(Table table, String data);


    /**
     * Batch is just a syntactic-sugar way to store bulk of events
     * with its lastId to acknowledge them later
     */
    class Batch {
        public String lastId;
        public List<String> events;

        Batch(String lastId, List<String> events) {
            this.lastId = lastId;
            this.events = events;
        }
    }

    /**
     * Table contains `name`, and `token` that represent
     * an IronBeastSDK destination/table
     */
    class Table {
        public String name;
        public String token;

        Table(String name, String token) {
            this.name = name;
            this.token = token;
        }
    }
}
