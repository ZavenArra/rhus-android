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
	
	//constants
	//these should be removed or abstract functions
	public static final String DATABASE_NAME = "grocery-sync";


	//couch internals
	protected static ServiceConnection couchServiceConnection;
	protected static HttpClient httpClient;

	//ektorp impl
	protected CouchDbInstance dbInstance;
	protected CouchDbConnector couchDbConnector;
	protected ReplicationCommand pushReplicationCommand;
	protected ReplicationCommand pullReplicationCommand;


	//context setup for couchbase service
    protected Context appContext = null;

	@Override
	public boolean onCreate() {
    	System.out.println("Starting couch");
		startCouch();
		return true;
	}
	
	abstract public String getBucketName();
	abstract public String getReplicationUrl();

	
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

		public void couchbaseStarted(String host, int port) {
			Log.v(TAG, "got couch started " + host + " " + port);
			//do we want to notify a creator somehow?
			startEktorp(host, port);
		}
	};
	
	
	protected void startCouch() {
    	System.out.println("Starting couch...");

    	//getContext won't work until after onCreate, so we cache it locally..
    	
		CouchbaseMobile couch = new CouchbaseMobile(getContext(), couchCallbackHandler);

		couchServiceConnection = couch.startCouchbase(); //could call with a ctx here.
	}

	
	protected void startEktorp(String host, int port) {
		Log.v(TAG, "starting ektorp");

		if(httpClient != null) {
			httpClient.shutdown();
		}

		httpClient =  new AndroidHttpClient.Builder().host(host).port(port).maxConnections(100).build();
		dbInstance = new StdCouchDbInstance(httpClient);


		CouchbaseMobileEktorpAsyncTask startupTask = new CouchbaseMobileEktorpAsyncTask() {

			@Override
			protected void doInBackground() {

				couchDbConnector = dbInstance.createConnector(getBucketName(), true);

				/*
				//ensure we have a design document with a view
				//update the design document if it exists, or create it if it does not exist
				//This is handled by a callback from the extender
				try {
					DesignDocument dDoc = couchDbConnector.get(DesignDocument.class, dDocId);
					dDoc.addView("byDate", new DesignDocument.View(byDateViewMapFunction));
					couchDbConnector.update(dDoc);
				}
				catch(DocumentNotFoundException ndfe) {
					DesignDocument dDoc = new DesignDocument(dDocId);
					dDoc.addView("byDate", new DesignDocument.View(byDateViewMapFunction));
					couchDbConnector.create(dDoc);
				}
				*/
				

			}

			@Override
			protected void onSuccess() {
				Log.v(TAG, "Successful ekTorp startup");
				initializationTask();
			}
		};
		startupTask.execute();
		
	}
	
	//perform any necessary initialization, view creation, etc.
	public abstract void initializationTask();

	
	public void startReplications() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

		pushReplicationCommand = new ReplicationCommand.Builder()
			.source(getBucketName())
			.target(prefs.getString("sync_url", getReplicationUrl()))
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
			.source(prefs.getString("sync_url", getReplicationUrl()))
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
		getContext().unbindService(couchServiceConnection);
		
		//clean up our http client connection manager
		if(httpClient != null) {
			httpClient.shutdown();
		}

	}

}
