package io.ironsourceatom.sdk;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ReportIntentTest {


    @Mock
    Context context;


    @Test
    public void testSetters(){
        ReportIntent report= new ReportIntent(context);
        report.setEnpoint("bla");
        report.setTable("bla");
        report.setToken("bla");
        report.setData("bla");
        report.setBulk(true);
    }
    @Test
    public void testSend(){
        ReportIntent report= new ReportIntent(context);
        report.send();
    }

    @Test
    public void testConstructor(){
        ReportIntent report= new ReportIntent(context);
        assertNotNull(report);
    }

}

