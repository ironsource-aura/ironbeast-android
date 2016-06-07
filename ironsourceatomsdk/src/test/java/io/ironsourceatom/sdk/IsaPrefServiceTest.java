package io.ironsourceatom.sdk;

import android.content.Context;
import android.test.mock.MockContext;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by kirill.bokhanov on 6/7/16.
 */
public class IsaPrefServiceTest {

    final Context context = mock(MockContext.class);
    private IsaPrefService service;

    @Before
    public void setUp(){
        service=IsaPrefService.getInstance(context);
    }

    @Test
    public void testLoadIntDef(){
        assertEquals(service.load("blabla", 12),12);

    }
    @Test
    public void testLoadLongDef(){
        assertEquals(service.load("blabla", 12L), 12L);

    }
}
