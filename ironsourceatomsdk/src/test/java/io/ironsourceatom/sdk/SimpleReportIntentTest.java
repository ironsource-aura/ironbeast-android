package io.ironsourceatom.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.test.mock.MockContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by kirill.bokhanov on 6/6/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleReportIntentTest {
    Context context = mock(MockContext.class);
    //Intent intent = new Intent();
    private SimpleReportIntent report;

//    @Before
//    public void initVars(){
//        report = new SimpleReportIntent(context);
//    }


    @Test
    public void testSetEnpoint() throws NoSuchFieldException, IllegalAccessException {
        Intent intent = new Intent();
        report = new SimpleReportIntent(context);
        Field privateIntent = SimpleReportIntent.class.getDeclaredField("intent");
        privateIntent.setAccessible(true);
        privateIntent.set(report, intent);
        report.setEnpoint("bla");
        report.setData("bla");
        report.setTable("bla");
        report.setToken("bla");
        report.setBulk(true);



        System.out.println(report.getIntent());
        assertEquals(report.getIntent().getStringExtra(SimpleReportIntent.ENDPOINT), null);
    }

}
