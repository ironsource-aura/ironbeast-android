package com.ironsource.mobilcore;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.ironsource.mobilcore.Consts.EServiceType;
import com.ironsource.mobilcore.ReportingConsts.EReportType;

import java.util.concurrent.LinkedBlockingQueue;

public class IronBeastReportService extends Service {

    private static final int REPORTING_QUEUE_SIZE = 50;
    private static final int DOWNLOAD_FILE_QUEUE_SIZE = 10;
    private static final int DELAY_FOR_STOPPING_SELF_MILI = 5000;
    private static final String TAG = IronBeastReport.class.getSimpleName();

    private ReportingWorker mReportingWorker;
    private DownloadFileWorker mDownloadFileWorker;
    private LinkedBlockingQueue<ServiceTask> mReportingQueue;
    private LinkedBlockingQueue<ServiceTask> mDownloadFileQueue;

    private Handler mHandler;

    private int mNumCurrentWorkingJobs;
    /***************
     * runnables
     ******************/

    private Runnable mStopSelfRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.log("MobileCoreReport service , mStopSelfRunnable , run() | called. stopping self", Logger.SDK_DEBUG);
            stopSelf();
        }
    };

    public IronBeastReportService() {
        super();

    }

    /***************
     * service methods
     ******************/

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Logger.log(TAG + " service onCreate() | called", Logger.SDK_DEBUG);
        android.os.Debug.waitForDebugger();

        try {
            IronBeast.setAppContext(this);

            // init members
            mReportingQueue = new LinkedBlockingQueue<ServiceTask>(REPORTING_QUEUE_SIZE);
            mDownloadFileQueue = new LinkedBlockingQueue<ServiceTask>(DOWNLOAD_FILE_QUEUE_SIZE);

            mDownloadFileWorker = new IronBeastReportService.DownloadFileWorker();
            mReportingWorker = new IronBeastReportService.ReportingWorker();
            mHandler = new Handler();
            mNumCurrentWorkingJobs = 0;

            mDownloadFileWorker.start();
            mReportingWorker.start();
        } catch (Throwable th) {
            // Reporting, not interfering with main task queue
            final String msg = th.getMessage();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    /* Proceed task that was not appended to the queue */
                    Logger.log("MobileCoreReport service | onCreate exception", Logger.SDK_DEBUG);
                    IronBeastReportIntent reportIntent = new IronBeastReportIntent(IronBeastReportService.this, EReportType.REPORT_TYPE_ERROR);
                    reportIntent.putExtra(ReportingConsts.EXTRA_EXCEPTION, "MobileCoreReport ### onCreate " + msg);
                    IronBeastReportData report = new IronBeastReportData();
                    report.doReport(IronBeastReportService.this, reportIntent);
                }
            }).start();
        }
    }

    @Override
    public void onDestroy() {

        Logger.log(TAG + " service onDestroy() | called", Logger.SDK_DEBUG);

        super.onDestroy();

        //clear members
        mReportingQueue.clear();
        mDownloadFileQueue.clear();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.log(TAG + " service , onStartCommand() | startId:" + startId, Logger.SDK_DEBUG);
        if (intent != null && intent.getExtras() != null) {
            final ServiceTask newTask = new ServiceTask(startId, intent);

            try {
                // new task. remove current stop self callback
                addRemoveStopSelfRunnable(false);
                // add task to queue
                EServiceType type = newTask.getServiceType();

                if (type == EServiceType.SERVICE_TYPE_APK_DOWNLOAD) {
                    mDownloadFileQueue.add(newTask);
                } else {
                    //REPORTING
                    mReportingQueue.add(newTask);
                }
            } catch (IllegalStateException e) {
                // Q is full. we should not get here since we decided on a very large queue
                Logger.log("MobileCoreReport service | ServiceTask , doJob() | dropping request:" + startId, Logger.SDK_DEBUG);
				/* Clear task queue */
                try {
                    Logger.log("MobileCoreReport service | Clear task queue" + startId, Logger.SDK_DEBUG);
                    mReportingQueue.clear();
                    mDownloadFileQueue.clear();
                } catch (Exception ex) {
                }

                EReportType tmpType = null;
                EServiceType tmpServiceType = null;
                try {
                    tmpType = EReportType.parseString(intent.getIntExtra(ReportingConsts.EXTRA_REPORT_TYPE, -1));
                } catch (Exception ex) {
                }

                try {
                    tmpServiceType = EServiceType.getValue(Consts.EXTRA_SERVICE_TYPE, intent);
                } catch (Exception ex) {
                }
                final EReportType thrownReportType = tmpType;
                final EServiceType serviceType = tmpServiceType;

                final String msg = e.getMessage();
                // Reporting, not interfering with main task queue
                new Thread(new Runnable() {

                    @Override
                    public void run() {
						/* Proceed task that was not appended to the queue */
                        Logger.log("MobileCoreReport service | Send exception", Logger.SDK_DEBUG);
                        IronBeastReportIntent reportIntent = new IronBeastReportIntent(IronBeastReportService.this, EReportType.REPORT_TYPE_ERROR);
                        reportIntent.putExtra(ReportingConsts.EXTRA_EXCEPTION, "MobileCoreReport ### Task queue is full, dropping request " + serviceType + " trt: " + thrownReportType + " m: " + msg);
                        IronBeastReportData report = new IronBeastReportData();
                        report.doReport(IronBeastReportService.this, reportIntent);
                    }
                }).start();
            }
        }
        return START_STICKY;
    }

    /*************** private methods ******************/

    /**
     * Called when a task is finished.
     * If there is nothing the the QUeue and All thread are not busy - we can kill ourself.
     */
    private void stopSelfIfNeeded() {
        if (mDownloadFileQueue.isEmpty() && mReportingQueue.isEmpty() && 0 == getCurrentRefCount()) {
            addRemoveStopSelfRunnable(true);
        }
    }

    /**
     * we add a timer to stopping the service.
     * we do this because every time the service dies and restarts, CONCURRENT_JOBS threads are created.
     * To prevent a case where the service dies and restarts often (and many threads are created),
     * we stop the service after DELAY_FOR_STOPPING_SELF_MILI passed since the last task ended
     *
     * @param doAdd
     */
    private void addRemoveStopSelfRunnable(boolean doAdd) {

        Logger.log("MobileCoreReport service , addRemoveStopSelfRunnable() | called. doAdd:" + doAdd, Logger.SDK_DEBUG);

        if (doAdd) {
            mHandler.postDelayed(mStopSelfRunnable, DELAY_FOR_STOPPING_SELF_MILI);
        } else {
            mHandler.removeCallbacks(mStopSelfRunnable);
        }

    }

    /*********
     * reference count methods
     **********/

    private synchronized int getCurrentRefCount() {
        return mNumCurrentWorkingJobs;
    }

    public synchronized void decreaseRefCount() {
        mNumCurrentWorkingJobs--;
    }

    public synchronized void increaseRefCount() {
        mNumCurrentWorkingJobs++;
    }

    /**********
     * inner classes
     ************/

    public class ServiceTask {

        private int mStartId;
        private Intent mIntent;

        public ServiceTask(int startId, Intent intent) {
            mStartId = startId;
            mIntent = intent;
        }

        public Intent getIntent() {
            return mIntent;
        }

        public EServiceType getServiceType() {
            return EServiceType.getValue(Consts.EXTRA_SERVICE_TYPE, mIntent);
        }

        public void doJob() {
            if (mIntent != null) {

                Logger.log("MobileCoreReport service , ServiceTask , doJob() | mStartId:" + mStartId, Logger.SDK_DEBUG);
                final EServiceType serviceType = EServiceType.getValue(Consts.EXTRA_SERVICE_TYPE, mIntent);
                try {

                    switch (serviceType) {
                        case SERVICE_TYPE_REPORT:
                            IronBeastReportData report = new IronBeastReportData();
                            report.doReport(IronBeastReportService.this, mIntent);
                            break;
                        case SERVICE_TYPE_SEND_REPORTS:
                            IronBeastReportData.doScheduledSend();
                            break;
                        default:
                            break;
                    }

                } catch (Exception e) {
                    final String msg = e.getMessage();

                    EReportType tmpType = null;
                    try {
                        tmpType = EReportType.parseString(mIntent.getIntExtra(ReportingConsts.EXTRA_REPORT_TYPE, -1));
                    } catch (Exception ex) {
                    }

                    final EReportType thrownReportType = tmpType;
                    Logger.log("MobileCoreReport service , ServiceTask , doJob() | dropping request " + serviceType + "trt: " + thrownReportType + " m: " + msg, Logger.SDK_DEBUG);
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            IronBeastReportIntent reportIntent = new IronBeastReportIntent(IronBeastReportService.this, EReportType.REPORT_TYPE_ERROR);
                            reportIntent.putExtra(ReportingConsts.EXTRA_EXCEPTION, "MobileCoreReport ### ServiceTask ## doJob # " + serviceType + " trt: " + thrownReportType + " m: " + msg);
                            IronBeastReportData report = new IronBeastReportData();
                            report.doReport(IronBeastReportService.this, reportIntent);
                        }
                    }).start();
                }

            }
        }
    }

    public class DownloadFileWorker extends Thread {

        @Override
        public void run() {
            super.run();
            ServiceTask task;
            while (true) {
                try {
                    task = mDownloadFileQueue.take();
                    // starting job. increase ref count
                    increaseRefCount();
                    // do job
                    task.doJob();
                    // finished job. increase ref count
                    decreaseRefCount();
                    // stop self if needed
                    stopSelfIfNeeded();
                } catch (InterruptedException ie) {
                    return;
                }
            }
        }
    }

    public class ReportingWorker extends Thread {

        @Override
        public void run() {

            super.run();
            ServiceTask task;
            while (true) {
                try {
                    task = mReportingQueue.take();
                    // starting job. increase ref count
                    increaseRefCount();
                    // do job
                    task.doJob();
                    // finished job. increase ref count
                    decreaseRefCount();
                    // stop self if needed
                    stopSelfIfNeeded();
                } catch (InterruptedException ie) {
                    return;
                }

            }
        }
    }

}
