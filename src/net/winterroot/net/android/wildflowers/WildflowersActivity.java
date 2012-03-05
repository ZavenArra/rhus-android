package net.winterroot.net.android.wildflowers;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import net.winterroot.android.rhus.RhusActivity;
import net.winterroot.android.rhus.provider.RhusDocument;


public class WildflowersActivity extends RhusActivity {

	public static String TAG = "Wildflowers";

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	Cursor documentsCursor = managedQuery(RhusDocument.CONTENT_URI, null,
                null, null, null);
    	
    	Log.v("RHUS", "HELLE");
    	System.out.println("HELLO WORLD");
   //     startCouchbase();
   //     setContentView(R.layout.main);
    }
    

}