package net.winterroot.android.wildflowers;

import android.app.Activity;
import android.os.Bundle;
import android.content.ServiceConnection;

import com.couchbase.android.CouchbaseMobile;
import com.couchbase.android.ICouchbaseDelegate;



public class WildflowersActivity extends Activity {
    /** Called when the activity is first created. */
	
    private ServiceConnection couchServiceConnection;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startCouchbase();
        setContentView(R.layout.main);
    }
    
    private final ICouchbaseDelegate couchCallbackHandler = new ICouchbaseDelegate() {
        public void couchbaseStarted(String host, int port) {
        	
        }    
        public void exit(String error) {
        	
        }
    };
    
    public void startCouchbase() {
        CouchbaseMobile couch = new CouchbaseMobile(getBaseContext(), couchCallbackHandler);
        couchServiceConnection = couch.startCouchbase();
     } 

}