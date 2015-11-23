package com.ironsource.mobilcore;


/**
 * Created by avilevinshtein on 8/16/15.
 */
public class AdUnitEventManager {

    private static AdUnitEventManager sInstance;

    protected static AdUnitEventManager getInstance() {
        if (sInstance == null) {
            sInstance = new AdUnitEventManager();
        }
        return sInstance;
    }

}
