package com.ironsource.mobilcore.ironbeauty;

interface IBHttpObserver {
    public void onFinish(String responseString);
    public void onError();
}
