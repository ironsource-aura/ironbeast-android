package com.ironsource.mobilcore;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

interface AdIdBinderHolder extends IInterface {

    String getId()
    throws RemoteException;

    boolean a(boolean paramBoolean)
    throws RemoteException;

    String e(String paramString)
    throws RemoteException;

    void b(String paramString, boolean paramBoolean)
    throws RemoteException;

    abstract class a extends Binder implements AdIdBinderHolder {

	public static AdIdBinderHolder b(IBinder paramIBinder)
	{
	    if (paramIBinder == null) {
		return null;
	    }
	    IInterface localIInterface = paramIBinder.queryLocalInterface("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
	    if ((localIInterface != null) && ((localIInterface instanceof AdIdBinderHolder))) {
		return (AdIdBinderHolder)localIInterface;
	    }
	    return new AdIDBinder(paramIBinder);
	}

	public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
	throws RemoteException
	{
	    String str2;
	    switch (code)
	    {
	    case 1598968902: 
		reply.writeString("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
		return true;
	    case 1: 
		data.enforceInterface("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
		String str1 = getId();
		reply.writeNoException();
		reply.writeString(str1);
		return true;
	    case 2: 
		data.enforceInterface("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
		boolean bool1 = 0 != data.readInt();
		boolean bool2 = a(bool1);
		reply.writeNoException();
		reply.writeInt(bool2 ? 1 : 0);
		return true;
	    case 3: 
		data.enforceInterface("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
		str2 = data.readString();
		String str3 = e(str2);
		reply.writeNoException();
		reply.writeString(str3);
		return true;
	    case 4: 
		data.enforceInterface("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
		str2 = data.readString();
		boolean bool3 = 0 != data.readInt();
		b(str2, bool3);
		reply.writeNoException();
		return true;
	    }
	    return super.onTransact(code, data, reply, flags);
	}

	private static class AdIDBinder implements AdIdBinderHolder{
	    private IBinder dG;

	    AdIDBinder(IBinder paramIBinder)
	    {
		this.dG = paramIBinder;
	    }

	    public IBinder asBinder()
	    {
		return this.dG;
	    }

	    public String getId()
	    throws RemoteException
	    {
		Parcel localParcel1 = Parcel.obtain();
		Parcel localParcel2 = Parcel.obtain();
		String str;
		try
		{
		    localParcel1.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
		    this.dG.transact(1, localParcel1, localParcel2, 0);
		    localParcel2.readException();
		    str = localParcel2.readString();
		}
		finally
		{
		    localParcel2.recycle();
		    localParcel1.recycle();
		}
		return str;
	    }

	    public boolean a(boolean paramBoolean)
	    throws RemoteException
	    {
		Parcel localParcel1 = Parcel.obtain();
		Parcel localParcel2 = Parcel.obtain();
		boolean bool;
		try
		{
		    localParcel1.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
		    localParcel1.writeInt(paramBoolean ? 1 : 0);
		    this.dG.transact(2, localParcel1, localParcel2, 0);
		    localParcel2.readException();
		    bool = 0 != localParcel2.readInt();
		}
		finally
		{
		    localParcel2.recycle();
		    localParcel1.recycle();
		}
		return bool;
	    }

	    public String e(String paramString)
	    throws RemoteException
	    {
		Parcel localParcel1 = Parcel.obtain();
		Parcel localParcel2 = Parcel.obtain();
		String str;
		try
		{
		    localParcel1.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
		    localParcel1.writeString(paramString);
		    this.dG.transact(3, localParcel1, localParcel2, 0);
		    localParcel2.readException();
		    str = localParcel2.readString();
		}
		finally
		{
		    localParcel2.recycle();
		    localParcel1.recycle();
		}
		return str;
	    }

	    public void b(String paramString, boolean paramBoolean)
	    throws RemoteException
	    {
		Parcel localParcel1 = Parcel.obtain();
		Parcel localParcel2 = Parcel.obtain();
		try
		{
		    localParcel1.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
		    localParcel1.writeString(paramString);
		    localParcel1.writeInt(paramBoolean ? 1 : 0);
		    this.dG.transact(4, localParcel1, localParcel2, 0);
		    localParcel2.readException();
		}
		finally
		{
		    localParcel2.recycle();
		    localParcel1.recycle();
		}
	    }
	}
    }
}