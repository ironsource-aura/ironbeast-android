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
        public boolean isOnline(Context context) { return true; }

        @Override
        public Response post(String data, String url) throws IOException { return null; }
    }
}
