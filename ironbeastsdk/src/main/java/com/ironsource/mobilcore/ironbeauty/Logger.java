package com.ironsource.mobilcore.ironbeauty;

/**
 * Created by mikhaili on 11/12/15.
 */
abstract class Logger {
    static Logger getInstance(String name) {
        return new Logger() {
            @Override
            void verbose(String var1) {

            }

            @Override
            void info(String var1) {

            }

            @Override
            void warn(String var1) {

            }

            @Override
            void error(String var1) {

            }

            @Override
            void error(Exception var1) {

            }

            @Override
            int getLogLevel() {
                return 0;
            }

            @Override
            void setLogLevel(int var1) {

            }
        };
    }

    abstract void verbose(String var1);

    abstract void info(String var1);

    abstract void warn(String var1);

    abstract void error(String var1);

    abstract void error(Exception var1);

    abstract int getLogLevel();

    abstract void setLogLevel(int var1);

    public static class LogLevel {
        public static final int VERBOSE = 0;
        public static final int INFO = 1;
        public static final int WARNING = 2;
        public static final int ERROR = 3;

        public LogLevel() {
        }
    }
}