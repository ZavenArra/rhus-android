package net.winterroot.android.couchbasemobile.provider;

import org.codehaus.jackson.JsonNode;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbAccessException;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.ReplicationCommand;
import org.ektorp.UpdateConflictException;
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


import com.couchbase.android.CouchbaseMobile;
import com.couchbase.android.ICouchbaseDelegate;

abstract public class CouchbaseMobileContentProvider extends ContentProvider {

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
	
	@Override
	public boolean onCreate() {
    	Log.v(TAG, "On Create Content Provider");
    	//call ensure here and block with wait() in 2nd call to ensure from query, etc.
    	//this would be the best
    	
    	sync = new Object();
    	return true;

	}
	
	//inelegant way to all the service to start synchronously
	//since this shouldn't be on the UI thread.
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


	protected boolean ensureCouchServiceStarted(){
		Log.v(TAG, "Ensuring couch service");
		//Since this will be called asyncronously, will need an approach that pauses if the provider is currently starting up.
		if(couchDbConnector == null){
			Log.v(TAG, "Starting the couch... can i relax??");
			CouchbaseMobile couch = new CouchbaseMobile(getContext(), couchCallbackHandler);

			couchServiceConnection = couch.startCouchbase();

			//essentially turning async launch into synchronous with onCreate().
			waitMonitor(true);
   		}
		//When the application is destroyed and restarted, EKTorp needs to be restarted
		//TODO: Find a way to detect a dead EKTorp, i.e. http client which is no longer connected.
		//Reference here: http://stackoverflow.com/questions/9505358/android-httpclient-hangs-on-second-request-to-the-server-connection-timed-out
		//Restarting HTTP client with every request is a suggested fix to other related problems.
		startEktorp();
		return true;	

	}

	
    protected ICouchbaseDelegate couchCallbackHandler = new ICouchbaseDelegate() {

		public void exit(String error) {
			/*
			TODO: needs(?) to have a way to communicate errors to the UI.
			..not really necessary..
			AlertDialog.Builder builder = new AlertDialog.Builder(AndroidGrocerySyncActivity.this);
			builder.setMessage(error)
			       .setCancelable(false)
			       .setPositiveButton(R.string.error_dialog_button, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   AndroidGrocerySyncActivity.this.finish();
			           }
			       })
			       .setTitle(R.string.error_dialog_title);
			AlertDialog alert = builder.create();
			alert.show();
			*/
		}

		public void couchbaseStarted(String startupHost, int startupPort) {
			host = startupHost;
			port = startupPort;
			Log.v(TAG, "got couch started " + host + " " + port);
			//do we want to notify a creator somehow?
			//startEktorp(host, port);
			startEktorp();
			waitMonitor(false);
		}
	};
	
	protected void startEktorp() {
		Log.v(TAG, "starting ektorp "+host+port);

		if(httpClient != null) {
			httpClient.shutdown();
		}

		
		httpClient =  new AndroidHttpClient.Builder().host(host).port(port).maxConnections(100).build();
		dbInstance = new StdCouchDbInstance(httpClient);
		couchDbConnector = dbInstance.createConnector(getBucketName(), true);
		startReplications();
		initialization();

		/* Changing this to synchronous task
		CouchbaseMobileEktorpAsyncTask startupTask = new CouchbaseMobileEktorpAsyncTask() {

			@Override
			protected void doInBackground() {
		 */
		/*
			}
		couchDbConnector = dbInstance.createConnector(getBucketName(), true);

			@Override
			protected void onSuccess() {
								startReplications();
		initializationTask();
				Log.v(TAG, "Successful ekTorp startup");

		 */

		/*	}
		};

		startupTask.execute();
		 */
	}
	
	//perform any necessary initialization, view creation, etc.
	public abstract void initialization();

	
	public void startReplications() {
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

	public void stopEktorp() {
	}
	
	//Shutdown connection to the couchDB service
	public void shutdown(){
		Log.v(TAG,  "Shutting down couchServiceConnection");
		getContext().unbindService(couchServiceConnection);
		//clean up our http client connection manager
		if(httpClient != null) {
			httpClient.shutdown();
		}

	}

}
