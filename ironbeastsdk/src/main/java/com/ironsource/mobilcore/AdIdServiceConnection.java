package com.ironsource.mobilcore;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

class AdIdServiceConnection implements ServiceConnection {

    boolean iN = false;
    private final BlockingQueue<IBinder> iO = new LinkedBlockingQueue();

    public void onServiceConnected(ComponentName name, IBinder service)
    {
	try
	{
	    this.iO.put(service);
	}
	catch (InterruptedException localInterruptedException) {}
    }

    public void onServiceDisconnected(ComponentName name) {}

    public IBinder aG()
    throws InterruptedException
    {
	if (this.iN) {
	    throw new IllegalStateException();
	}
	this.iN = true;
	return this.iO.take();
    }
}