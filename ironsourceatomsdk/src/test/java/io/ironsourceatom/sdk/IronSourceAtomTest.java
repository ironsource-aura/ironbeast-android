package io.ironsourceatom.sdk;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by kirill.bokhanov on 6/6/16.
 */

@RunWith(MockitoJUnitRunner.class)
public class IronSourceAtomTest {
    private final Context context = mock(Context.class);
    private final String auth = "hdhdhhd";
    private final IronSourceAtom atom=mock(IronSourceAtom.class);
    private SimpleReportIntent reportIntent=mock(SimpleReportIntent.class);


    @Test
    public void testConstructor() {
        IronSourceAtom myAtom = new IronSourceAtom(context, auth);
        assertNotNull(myAtom);

    }
    @Test
    public void testPutEvent(){
        doNothing().when(atom).putEvent("hfhhf", "djdjdj");

}

    @Test
    public void testPutEvents(){
        doNothing().when(atom).putEvents("hfhhf", "djdjdj");

    }

    @Test
    public void testOpenReport(){
        when(atom.openReport(context)).thenCallRealMethod();
        when(reportIntent.setData("data")).thenCallRealMethod();
        when(reportIntent.setBulk(true)).thenCallRealMethod();
        when(reportIntent.setEnpoint("data")).thenCallRealMethod();
        when(reportIntent.setTable("data")).thenCallRealMethod();
        when(reportIntent.setToken("data")).thenCallRealMethod();



    }

    }

