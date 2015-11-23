package com.ironsource.mobilcore;

import android.content.Intent;

class AdIdGooglePlayServicesRepairableException extends AdIdUserRecoverableException {

    private static final long serialVersionUID = 1L;

    private final int iL;

    AdIdGooglePlayServicesRepairableException(int connectionStatusCode, String msg, Intent intent)
    {
	super(msg, intent);
	this.iL = connectionStatusCode;
    }

    public int getConnectionStatusCode()
    {
	return this.iL;
    }
}