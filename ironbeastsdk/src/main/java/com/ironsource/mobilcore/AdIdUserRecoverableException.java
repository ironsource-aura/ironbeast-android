package com.ironsource.mobilcore;

import android.content.Intent;

class AdIdUserRecoverableException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private final Intent mIntent;

	public AdIdUserRecoverableException(String msg, Intent intent)
	{
		super(msg);
		this.mIntent = intent;
	}

	public Intent getIntent()
	{
		return new Intent(this.mIntent);
	}
}