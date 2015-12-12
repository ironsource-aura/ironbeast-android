package io.ironbeast.sdk;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class FsQueue implements StorageService {

    /**
     * Do not call directly.
     * You should use FsQueue.getInstance()
     */
    public FsQueue(String filename, Context context) {
        mFilename = filename;
        mContext = context;
        // TODO: use sharedPreferences -> get() onCreate, save() onDestroy
        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(getFile()));
            lnr.skip(Long.MAX_VALUE);
            mRecords = lnr.getLineNumber();
        } catch (IOException e) {
            mRecords = 0;
        }
    }

    /**
     * Use this to get a singleton instance of FsQueue instead of creating one directly
     * for yourself.
     */
    public static FsQueue getInstance(String filename, final Context context) {
        synchronized (sInstances) {
            FsQueue ret;
            if (sInstances.containsKey(filename)) {
                ret = sInstances.get(filename);
            } else {
                ret = new FsQueue(filename, context);
                sInstances.put(filename, ret);
            }
            return ret;
        }
    }

    private File getFile() {
        return new File(mContext.getFilesDir(), mFilename);
    }

    @Override
    public int count() { return mRecords; }

    @Override
    public int push(String ... records) {
        File file = getFile();
        FileWriter out = null;
        String data = "";
        for (String record: records) data += record + "\n";
        try {
            out = new FileWriter(file, true);
            out.write(data);
            out.close();
            mRecords += records.length;
        } catch (IOException e) {
            Logger.log("Failed to write record to 'fs'", Logger.SDK_DEBUG);
        }
        return mRecords;
    }

    @Override
    public String[] drain() {
        String[] records = peek();
        clear();
        return records;
    }

    @Override
    public String[] peek() {
        String[] lines = null;
        File fi = null;
        InputStream in;
        try {
            fi = getFile();
            in = new FileInputStream(fi);
            byte[] data = Utils.slurp(in);
            if(data != null && data.length > 0) {
                String rawLines = new String(data, Charset.forName("UTF-8"));
                lines = rawLines.split("\n");
            }

            in.close();
        } catch(IOException e) {
            Logger.log("Failed to read records from 'fs'", Logger.SDK_DEBUG);
        }
        return lines;
    }

    @Override
    public void clear() {
        // If everything worked well, delete the file and reset `nRecords`
        File fi = getFile();
        mRecords = 0;
        fi.delete();
    }

    private static final Map<String, FsQueue> sInstances = new HashMap<String, FsQueue>();
    private Context mContext;
    private String mFilename;
    private int mRecords;
}
