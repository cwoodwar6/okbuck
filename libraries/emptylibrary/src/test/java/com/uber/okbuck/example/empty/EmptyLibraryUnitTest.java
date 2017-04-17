package com.uber.okbuck.example.empty;

import android.content.Context;
import android.content.ComponentName;

import com.uber.okbuck.BuckRobolectricTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(BuckRobolectricTestRunner.class)
@Config(sdk = 21, packageName = "com.uber.okbuck.example.empty")
public class EmptyLibraryUnitTest {
	private static final String PACKAGE_NAME = "com.test";
    private static final ComponentName CALLING_ACTIVITY =
       new ComponentName(PACKAGE_NAME, "com.test.TestActivity");

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void resource_loading() {
        Context context = (Context) RuntimeEnvironment.application;
        assertEquals(context.getResources().getString(R.string.empty_release_string), "empty_release_string");
    }
}
