package net.winterroot.android.rhus;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class TestActivity extends Activity {

	private static final String TAG = "Test";
	
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		Log.i(TAG, "onCreate"
				+ (null == savedState ? "" : " "));
	}
}
