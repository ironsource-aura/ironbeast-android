package io.ironbeast.sdk;

import android.content.Context;

import java.io.IOException;

public class TestsUtils {
    static class MockReport implements Report {
        @Override
        public void send() {}
        @Override
        public MockReport setData(String value) {
            return this;
        }

        @Override
        public MockReport setTable(String table) {
            return this;
        }

        @Override
        public MockReport setToken(String token) {
            return this;
        }

        public int mType;
    }

    static class MockPoster implements RemoteService {
        @Override
        public boolean isOnline(Context context) { return false; }

        @Override
        public Response post(String data, String url) throws IOException { return null; }
    }

    static class MockQueue implements StorageService {

        @Override
        public int count() { return 0; }

        @Override
        public int push(String ... records) { return 0; }

        @Override
        public String[] drain() { return new String[0]; }

        @Override
        public String[] peek() { return new String[0]; }

        @Override
        public void clear() { }
    }
}
