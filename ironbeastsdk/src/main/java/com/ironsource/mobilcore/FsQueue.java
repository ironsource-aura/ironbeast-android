package com.ironsource.mobilcore;

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
            nRecords = lnr.getLineNumber();
        } catch (IOException e) {
            nRecords = 0;
        }
    }

    /**
     * Use this to get a singleton instance of FsQueue instead of creating one directly
     * for yourself.
     */
    public synchronized static FsQueue getInstance(String filename, final Context context) {
        FsQueue ret;
        if (sInstances.containsKey(filename)) {
            ret = sInstances.get(filename);
        } else {
            ret = new FsQueue(filename, context);
            sInstances.put(filename, ret);
        }
        return ret;
    }

    private File getFile() {
        return new File(mContext.getFilesDir(), mFilename);
    }

    @Override
    public int count() { return nRecords; }

    @Override
    public int push(String record) {
        File file = getFile();
        FileWriter out = null;
        try {
            out = new FileWriter(file, true);
            out.write(record + "\n");
            out.close();
            nRecords++;
        } catch (IOException e) {
            Logger.log("Failed to write record to 'fs'", Logger.NORMAL);
        }
        return nRecords;
    }

    @Override
    public String[] drain() {
        String[] lines = null;
        File fi = null;
        InputStream in;
        try {
            fi = getFile();
            in = new FileInputStream(fi);
            String rawLines = new String(MCUtils.slurp(in), Charset.forName("UTF-8"));
            lines = rawLines.split("\n");
            in.close();
        } catch(IOException e) {
            Logger.log("Failed to read records from 'fs'", Logger.NORMAL);
        } finally {
            // If everything worked well, delete the file and reset `nRecords`
            if (fi != null && lines != null) {
                nRecords = 0;
                fi.delete();
            }
        }
        return lines;
    }

    private static final Map<String, FsQueue> sInstances = new HashMap<String, FsQueue>();
    private Context mContext;
    private String mFilename;
    private int nRecords;
}
