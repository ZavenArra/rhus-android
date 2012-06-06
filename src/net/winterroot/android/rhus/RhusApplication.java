package net.winterroot.android.rhus;

import net.winterroot.android.util.RhusDevice;
import android.app.Application;

public class RhusApplication extends Application {

	public RhusDataSet rhusDataSet;
	public RhusDevice rhusDevice;
	
	@Override
	public void onCreate() {
		super.onCreate();
		rhusDataSet = new RhusDataSet( (RhusApplication) getApplicationContext());
		rhusDevice = new RhusDevice(getBaseContext());
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}
	
}
