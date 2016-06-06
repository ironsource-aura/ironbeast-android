package io.ironsourceatom.sdk;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by kirill.bokhanov on 6/6/16.
 */

@RunWith(MockitoJUnitRunner.class)
public class IronSourceAtomTest {
    private final Context context = mock(Context.class);
    private final String auth = "hdhdhhd";
    private final IronSourceAtom atom=mock(IronSourceAtom.class);


    @Test
    public void testConstructor() {
        IronSourceAtom myAtom = new IronSourceAtom(context, auth);
        assert(myAtom!=null);

    }
    @Test
    public void testPutEvent(){
        atom.putEvent("hfhhf", "djdjdj");

}

    @Test
    public void testPutEvents(){
        atom.putEvents("hfhhf", "djdjdj");

    }

    }

