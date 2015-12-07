import com.ironsource.mobilcore.Report;

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
}
