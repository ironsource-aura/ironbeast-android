package com.ironsource.mobilcore;

interface IBHttpObserver {
    public void onFinish(String responseString);
    public void onError();
}
