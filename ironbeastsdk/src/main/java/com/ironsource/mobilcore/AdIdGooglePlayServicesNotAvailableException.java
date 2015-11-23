package com.ironsource.mobilcore;

final class AdIdGooglePlayServicesNotAvailableException extends Exception {

    private static final long serialVersionUID = 1L;

    public final int errorCode;

    public AdIdGooglePlayServicesNotAvailableException(int errorCode)
    {
	this.errorCode = errorCode;
    }
}