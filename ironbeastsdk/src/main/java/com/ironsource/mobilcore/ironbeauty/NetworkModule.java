//package com.ironsource.mobilcore.ironbeauty;
//
//import com.ironsource.mobilcore.IBConsts;
//import com.ironsource.mobilcore.IBHttpObserver;
//import com.ironsource.mobilcore.IBHttpTask;
//import com.ironsource.mobilcore.IronBeastReport;
//
//import org.json.JSONObject;
//
///**
// * Created by mikhaili on 11/12/15.
// */
//class NetworkModule extends Network<IronBeastReport> {
//    private String requestMethod;
//    private String mHostName;
//
//    NetworkModule() {
//        setHostName(IBConsts.URL_DEFAULT_IRON_BEAST_HOST_NAME);
//    }
//    @Override
//    public void append() {
//
//    }
//
//    @Override
//    protected void send(IronBeastReport report, boolean bulk) {
//
//        JSONObject data = new JSONObject();
//        String url = getHostName();
//        IBHttpTask task = new IBHttpTask.HttpTaskBuilder(url, new IBHttpObserver() {
//            @Override
//            public void onFinish(String responseString) {
//
//            }
//
//            @Override
//            public void onError() {
//
//            }
//        })
//                .jsonBody(data.toString())
//                .build();
//        task.executeTask();
//    }
//
//    @Override
//    public String getRequestMethod() {
//        return requestMethod;
//    }
//
//    @Override
//    public void setRequestMethod(String requestMethod) {
//        this.requestMethod = requestMethod;
//    }
//
//    @Override
//    public String getHostName() {
//        return mHostName;
//    }
//
//    @Override
//    public void setHostName(String hostName) {
//        mHostName = hostName;
//    }
//}
