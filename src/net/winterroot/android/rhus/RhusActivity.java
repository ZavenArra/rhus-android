package net.winterroot.android.rhus;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.content.Intent;
import android.content.ComponentName;

import net.winterroot.android.rhus.provider.RhusDocument;
import net.winterroot.android.wildflowers.R;

import net.winterroot.android.wildflowers.*;


public class RhusActivity extends Activity {

    /** Called when the activity is first created. */
	

	public static String TAG = "Rhus";

	//splash screen
	//protected SplashScreenDialog splashDialog;

	//main screen
	protected EditText addItemEditText;
	protected ListView itemListView;
	//protected GrocerySyncListAdapter itemListViewAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

    	
    	Log.v(TAG, "onCreate");
    	System.out.println("HELLO WORLD");
    	once = false;
    
    }
    
    public static boolean once;
    
    @Override
    public void onStart(){
    	super.onStart();
    	Log.v(TAG, "Before");

      /*  Intent i = new Intent();
        i.setClass(RhusActivity.this, TestActivity.class);
        startActivity(i);
    	Log.v("Test", "AFter Test Act");
*/
    	
    	/*
    	if(!once){
        	Log.v(TAG, "In the Once");
    		Intent i = new Intent();
    		i.setClass(RhusActivity.this, RhusMapActivity.class);
    		startActivity(i);
    		Log.v(TAG, "AFter Map Act");
    		once = true;
    	}
    	*/
    	
   
    }
    
    
    protected void onDestroy() {
    	super.onDestroy();
		Log.v(TAG, "onDestroy");


	}

	@Override
	protected void onRestart() {
		super.onRestart();
		// Notification that the activity will be started
		Log.i(TAG, "onRestart");
	}



	@Override
	protected void onResume() {
		super.onResume();
		// Notification that the activity will interact with the user
		Log.i(TAG, "onResume");
	}

	protected void onPause() {
		super.onPause();
		// Notification that the activity will stop interacting with the user
		Log.i(TAG, "onPause" + (isFinishing() ? " Finishing" : ""));
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Notification that the activity is no longer visible
		Log.i(TAG, "onStop");
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Save instance-specific state
	
		super.onSaveInstanceState(outState);
		Log.i(TAG, "onSaveInstanceState");

	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Log.i(TAG, "onRetainNonConfigurationInstance");
		// It's not what
		return new Integer(getTaskId());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		Log.i(TAG, "onRestoreInstanceState"
				);
	}

    
}
