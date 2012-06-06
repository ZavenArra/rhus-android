package net.winterroot.android.touchdb.provider;

import java.io.IOException;
import java.util.Map;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ReplicationCommand;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import android.content.ContentProvider;
import android.content.ServiceConnection;
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
	protected static final ServiceConnection couchServiceConnection = null;
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
        	
    	startCouchService();    	
    	
    	return true;

	}
		
	protected boolean startCouchService(){
		String filesDir = getContext().getFilesDir().getAbsolutePath();
		try {
			server = new TDServer(filesDir);
		} catch (IOException e) {
			Log.e(TAG, "Error starting TDServer", e);
		}

		db = server.getDatabaseNamed(getBucketName());

		final TDView allDocsView = db.getViewNamed(String.format("%s/%s", "test", "view"));
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
