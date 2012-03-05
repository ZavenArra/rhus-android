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

import net.winterroot.android.rhus.provider.RhusDocument;
import net.winterroot.android.wildflowers.R;

import org.codehaus.jackson.JsonNode;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.ReplicationCommand;
import org.ektorp.UpdateConflictException;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult.Row;
import org.ektorp.android.http.AndroidHttpClient;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.support.DesignDocument;

import com.couchbase.android.CouchbaseMobile;
import com.couchbase.android.ICouchbaseDelegate;


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
        
    	Cursor documentsCursor = managedQuery(RhusDocument.CONTENT_URI, null,
                null, null, null);
    	
    	Log.v("RHUS", "HELLE");
    	System.out.println("HELLO WORLD");
   //     startCouchbase();
   //     setContentView(R.layout.main);
    }
    /*
    private final ICouchbaseDelegate couchCallbackHandler = new ICouchbaseDelegate() {
        public void couchbaseStarted(String host, int port) {
			startEktorp(host, port);
        }    
        public void exit(String error) {
        	
        }
    };
    
    public void startCouchbase() {
        CouchbaseMobile couch = new CouchbaseMobile(getBaseContext(), couchCallbackHandler);
        couchServiceConnection = couch.startCouchbase();
     } 
    
    protected void startEktorp(String host, int port) {
		Log.v(TAG, "starting ektorp");

		if(httpClient != null) {
			httpClient.shutdown();
		}

		httpClient =  new AndroidHttpClient.Builder().host(host).port(port).maxConnections(100).build();
		dbInstance = new StdCouchDbInstance(httpClient);

	/*	GrocerySyncEktorpAsyncTask startupTask = new GrocerySyncEktorpAsyncTask() {

			@Override
			protected void doInBackground() {

				couchDbConnector = dbInstance.createConnector(DATABASE_NAME, true);

				//ensure we have a design document with a view
				//update the design document if it exists, or create it if it does not exist
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

			}

			@Override
			protected void onSuccess() {
				//attach list adapter to the list and handle clicks
				ViewQuery viewQuery = new ViewQuery().designDocId(dDocId).viewName(byDateViewName).descending(true);
				itemListViewAdapter = new GrocerySyncListAdapter(AndroidGrocerySyncActivity.this, couchDbConnector, viewQuery);
				itemListView.setAdapter(itemListViewAdapter);
				itemListView.setOnItemClickListener(AndroidGrocerySyncActivity.this);
				itemListView.setOnItemLongClickListener(AndroidGrocerySyncActivity.this);

				startReplications();
			}
		};
		startupTask.execute();
		*/
	//}
/*
	public void startReplications() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		pushReplicationCommand = new ReplicationCommand.Builder()
			.source(DATABASE_NAME)
			.target(prefs.getString("sync_url", "http://couchbase.iriscouch.com/grocery-sync"))
			.continuous(true)
			.build();

		GrocerySyncEktorpAsyncTask pushReplication = new GrocerySyncEktorpAsyncTask() {

			@Override
			protected void doInBackground() {
				dbInstance.replicate(pushReplicationCommand);
			}
		};

		pushReplication.execute();

		pullReplicationCommand = new ReplicationCommand.Builder()
			.source(prefs.getString("sync_url", "http://couchbase.iriscouch.com/grocery-sync"))
			.target(DATABASE_NAME)
			.continuous(true)
			.build();

		GrocerySyncEktorpAsyncTask pullReplication = new GrocerySyncEktorpAsyncTask() {

			@Override
			protected void doInBackground() {
				dbInstance.replicate(pullReplicationCommand);
			}
		};

		pullReplication.execute();
	}
*/
/*
	public void stopEktorp() {
	}

*/
    
    
    protected void onDestroy() {
		Log.v(TAG, "onDestroy");


	}

    
}
