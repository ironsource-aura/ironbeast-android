package com.ironsource.mobilcore;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * ReportHandler unit test. should cover:
 * 1. Getting single post-event, should "Post" it to IronBeast
 *    to "IronBeastEndPoint"
 *    - if it's success, we're cool.
 *    - if it's fail, should "write/push" it to StorageService
 * 2. Getting track-event, should write it to StorageService,
 *    - if the StorageService.count() is greater or equal to "BulkSize" should
 *      flush(see below) the queue.
 * 3. Flush should drain the queue and map the entries based on the destination/table field.
 *    then, send each "bulk" separately to "IronBestBulkEndPoint".
 *    if the bulk is "too big(bytes/or bulkSize)", we split it into chunks.
 *    if some of the "bulk-request" failed(500>=), we put all its entries back to the queue.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReportHandlerTest {

    @Before
    public void clearMocks() {
        reset(mQueue, mHandler, mConfig);
    }

    @Test
    public void trackOnly() {

    }

//    private ReportIntent newRecord() {
//        return new ReportIntent(m)
//    }

    final IBConfig mConfig = spy(IBConfig.getsInstance());
    final StorageService mQueue = spy(new TestsUtils.MockQueue());
    final RemoteService mPoster = spy(new TestsUtils.MockPoster());
    final ReportHandler mHandler = new ReportHandler() {
        @Override
        protected RemoteService getPoster() { return mPoster; }
        @Override
        protected StorageService getQueue(String filename, Context context) { return mQueue; }
    };
}
