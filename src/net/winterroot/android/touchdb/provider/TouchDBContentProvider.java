package net.winterroot.android.touchdb.provider;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.conn.ClientConnectionManager;
import org.codehaus.jackson.JsonNode;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbAccessException;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.ReplicationCommand;
import org.ektorp.UpdateConflictException;
import org.ektorp.ViewQuery;
import org.ektorp.android.http.AndroidHttpClient;
import org.ektorp.android.util.EktorpAsyncTask;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.support.DesignDocument;

import android.app.AlertDialog;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.Context;

import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;


import com.couchbase.touchdb.TDDatabase;
import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.TDViewMapBlock;
import com.couchbase.touchdb.TDViewMapEmitBlock;
import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;

abstract public class TouchDBContentProvider extends ContentProvider {

	public static String TAG = "CouchbaseMobileContentProvider";
	
	//couch internals
	protected static ServiceConnection couchServiceConnection = null;
	protected static HttpClient httpClient;
	protected static String host;
	protected static int port;

	//ektorp impl
	protected CouchDbInstance dbInstance = null;
	protected CouchDbConnector couchDbConnector = null;
	
	protected ReplicationCommand pushReplicationCommand;
	protected ReplicationCommand pullReplicationCommand;
  
	abstract public String getBucketName();
	abstract public String getReplicationUrl();
		
	public Object sync;

	public TDServer server = null;
	public TDDatabase db = null;
	
	boolean waitingForEktorp = false;
	boolean ektorpStarted = false;

	
	{
		TDURLStreamHandlerFactory.registerSelfIgnoreError();
	}
	
	
	@Override
	public boolean onCreate() {
    	Log.v(TAG, "On Create Content Provider");
    	
    	//sync = new Object();
    	
    	startCouchService();    	
    	
    	return true;

	}
	
	//Inelegant way to all the service to start synchronously
	//This content provider shouldn't be on the UI thread anyway,
	//and should always be CALLED asynchronously, 
	//so that's why internally we've made it synchronous
	/*
	private void waitMonitor(boolean wait){
		 
		synchronized(sync) {
			if(wait){
				try {
					Log.v(TAG, "Waiting");
					sync.wait();
					Log.v(TAG, "Done Waiting");
				} catch(InterruptedException e) {
					System.out.println("InterruptedException caught");
				} 
			} else {
				Log.v(TAG, "Got Notified");
				sync.notify();
			}
		}
	}
	
	*/
	
	protected boolean startCouchService(){
		String filesDir = getContext().getFilesDir().getAbsolutePath();
		try {
			server = new TDServer(filesDir);
		} catch (IOException e) {
			Log.e(TAG, "Error starting TDServer", e);
		}

		db = server.getDatabaseNamed(getBucketName());

		TDView allDocsView = db.getViewNamed(String.format("%s/%s", "test", "view"));
		allDocsView.setMapReduceBlocks(new TDViewMapBlock() {
            
            public void map(Map<String, Object> document, TDViewMapEmitBlock emitter) {
                Object createdAt = document.get("created_at");
                if(createdAt != null) {
                    emitter.emit(createdAt.toString(), document);
                }

            }
        }, null, "2.0");
		
		
		startEktorp();
		
		return true;
	}

	protected boolean ensureCouchServiceStarted(){
		Log.v(TAG, "Ensuring couch service");
		//This doesn't matter now, because we are just going to start synchronously in onCreate()
		
		//Since this will be called asyncronously, will need an approach that pauses if the provider is currently starting up.

		//TODO: this is not correct.
		/*
		if(!ektorpStarted){
			try {
				waitingForEktorp = true;
				synchronized(sync) {
					sync.wait();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			if(couchDbConnector == null){
        		throw new RuntimeException(); 
			}
		
   		}
   		*/
		
		return true;	

	}
	
	protected void startEktorp() {
		Log.v(TAG, "Starting EKTorp");
		httpClient =  new TouchDBHttpClient(server);
		dbInstance = new StdCouchDbInstance(httpClient);
		couchDbConnector = dbInstance.createConnector(getBucketName(), true);
			

		startReplications();
		initialization();		
		
	}
	
	//perform any necessary initialization, view creation, etc.
	public abstract void initialization();

	
	public void startReplications() {
		if(getReplicationUrl() == null){
			Log.v(TAG, "No replication URL, skipping replications");
			return;
		}
		Log.v(TAG, "starting replications"+getReplicationUrl()+getBucketName());

		
		pushReplicationCommand = new ReplicationCommand.Builder()
			.source(getBucketName())
			.target(getReplicationUrl())
			.continuous(true)
			.build();

		CouchbaseMobileEktorpAsyncTask pushReplication = new CouchbaseMobileEktorpAsyncTask() {

			@Override
			protected void doInBackground() {
				dbInstance.replicate(pushReplicationCommand);
			}
		};

		pushReplication.execute();

		pullReplicationCommand = new ReplicationCommand.Builder()
			.source(getReplicationUrl())
			.target(getBucketName())
			.continuous(true)
			.build();

		CouchbaseMobileEktorpAsyncTask pullReplication = new CouchbaseMobileEktorpAsyncTask() {

			@Override
			protected void doInBackground() {
				dbInstance.replicate(pullReplicationCommand);
			}
		};

		pullReplication.execute();
	}

	//Shutdown connection to the couchDB service
	//This is only called for debugging, as content provider will not be destroyed as long as
	//process is running.
	public void shutdown(){
		Log.v(TAG,  "Shutting down couchServiceConnection");
		getContext().unbindService(couchServiceConnection);
		//clean up our http client connection manager
		if(httpClient != null) {
			httpClient.shutdown();
		}

	}

}
