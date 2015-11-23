package com.ironsource.mobilcore;

import android.text.TextUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by mikhaili on 11/14/15.
 */
public class IronBeastReport extends HashMap<String, String> {
    public static final String TABLE_NAME = "table";
    public static final String AUTH = "auth";
    public static final String BULK = "bulk";

    IronBeastReport(HashMap map) {
        this.putAll(map);
    }

    public static class Builder {
        HashMap<String, Object> mValues = new HashMap<>();

        public Builder setTableName(String table) {
            mValues.put(TABLE_NAME, table);
            mValues.put(BULK, String.valueOf(true));
            return this;
        }

        public Builder setAuth(String auth) {
            mValues.put(AUTH, auth);
            return this;
        }

        public Builder bulk(boolean bulk) {
            mValues.put(BULK, String.valueOf(bulk));
            return this;
        }

        public Builder setData(String key, String value) {
            mValues.put(key, value);
            return this;
        }

        public Builder setData(Map<String, String> data) {
            mValues.putAll(data);
            return this;
        }

        public Builder setData(JSONObject data) {
            Iterator<String> it = data.keys();
            while (it.hasNext()) {
                String key = it.next();
                mValues.put(key, data.opt(key));
            }
            return this;
        }

        public IronBeastReport build() {
            IronBeastReport rep = new IronBeastReport(mValues);
            if (TextUtils.isEmpty(rep.get(TABLE_NAME))) {
                throw new IllegalStateException(Strings.WARNING_REPORT_TABLE_NOT_SET);
            }
            return rep;
        }
    }
}
