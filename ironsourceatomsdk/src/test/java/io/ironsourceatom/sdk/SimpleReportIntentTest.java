package io.ironsourceatom.sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.test.IsolatedContext;
import android.test.mock.MockContext;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;

/**
 * Created by kirill.bokhanov on 6/6/16.
 */

@RunWith(MockitoJUnitRunner.class)
public class SimpleReportIntentTest {


    @Mock
    Context context;


    private SimpleReportIntent report;


    @Test
    public void testSetEnpoint(){

        report = new SimpleReportIntent(context);
        report.setEnpoint("bla");
        report.setData("bla");
        report.setTable("bla");
        report.setToken("bla");
        report.setBulk(true);
        assertNull(report.getIntent().getExtras());

    }

}
