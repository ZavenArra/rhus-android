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
        

    	
    	Log.v("RHUS", "HELLE");
    	System.out.println("HELLO WORLD");

    
    }
    
    public static boolean once = false;
    
    @Override
    public void onStart(){
    	super.onStart();
    	Log.v("Test", "Before");

      /*  Intent i = new Intent();
        i.setClass(RhusActivity.this, TestActivity.class);
        startActivity(i);
    	Log.v("Test", "AFter Test Act");
*/
    	if(!once){
        Intent i = new Intent();
     //   i.setComponent( new ComponentName("net.winterroot.android.rhus", "net.winterroot.android.rhus.RhusMapActivity") );
        i.setClass(RhusActivity.this, RhusMapActivity.class);
        startActivity(i);
    	Log.v("Test", "AFter Map Act");
    	once = true;
    	}
        
   
    }
    
    
    protected void onDestroy() {
    	super.onDestroy();
		Log.v(TAG, "onDestroy");


	}

    
}
