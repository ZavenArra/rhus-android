package net.winterroot.android.couchbasemobile.provider;


import android.database.MatrixCursor;
import android.util.Log;
import java.util.List;


import org.codehaus.jackson.JsonNode;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.ViewResult.Row; 
import org.ektorp.CouchDbConnector;
import org.ektorp.android.util.*;
import org.ektorp.changes.ChangesCommand;
import org.ektorp.changes.DocumentChange;
import org.ektorp.DbAccessException;



public class CouchCursor extends MatrixCursor {
	//TODO: remove MatrixCursor
	
	public static final String TAG = "Rhus - CouchCursor";
	
	private CouchDbConnector dbConnector;
	protected ViewQuery viewQuery;
	protected boolean followChanges;
	protected List<Row> listRows;
	
	protected EktorpAsyncTask updateListItemsTask;
	protected CouchbaseListChangesAsyncTask couchChangesAsyncTask = null;
	
	protected long lastUpdateChangesFeed = -1L;
	protected long lastUpdateView = -1L;
	
	public CouchCursor(CouchDbConnector setDbConnector, ViewQuery setViewQuery) {
		super(new String[] {"id","document"});
		this.dbConnector = setDbConnector;
		this.viewQuery = setViewQuery;
		followChanges = true;
		updateListItems();
	}
	
	protected void updateListItems() {
		Log.v(TAG, "Update List Items Task!");

		
		//if we're not already in the process of updating the list, start a task to do so
		if(updateListItemsTask == null) {
			Log.v(TAG, "Creating the Update List Items Task!");


			updateListItemsTask = new EktorpAsyncTask() {

				protected ViewResult viewResult;

				@Override
				protected void doInBackground() {
					Log.v(TAG, "Running EKTorp Background");
					viewResult = dbConnector.queryView(viewQuery);
				}

				protected void onSuccess() {
					Log.v(TAG, "Successfull Query");
					if(viewResult != null) {
						lastUpdateView = viewResult.getUpdateSeq();
						//listRows = viewResult.getRows();
						Log.v(TAG, "Query Response!");

						//This seems like it will get ALL the rows again, not just since the change
						for(ViewResult.Row aRow : viewResult.getRows()){
						//	Log.v(TAG, "Found an object"+aRow.getId());

							Object[] rowValues = new Object[] { aRow.getId(), aRow.getValue() };
							addRow(rowValues);
						//	Log.v(TAG, "now with"+getCount());

						}
						Log.v(TAG, "Callinng onChange() now with"+getCount());
						onChange(true);
						//Implementation in Anrdoid of onChange() and requery() is pretty vague.
						//Checking source of android indicates that requery() simply causes the DataSetObserver to be notified, whereas onChange does not
						requery();

					} else {
						Log.v(TAG, "Null Query Response!");

					}
					updateListItemsTask = null;

					
					Log.v(TAG, "Settingup Changes Task "+Long.toString(lastUpdateView)+" : "+Long.toString(lastUpdateChangesFeed));

					if(lastUpdateView > 0) {
						//we want to start our changes feed AFTER
						//getting our first copy of the view
						if(couchChangesAsyncTask == null && followChanges) {

							//create an ansyc task to get updates
							Log.v(TAG, "Actually making the task "+Long.toString(lastUpdateView)+" : "+Long.toString(lastUpdateChangesFeed));
							//Should change to Changes Feed
							//Ref: http://ektorp.org/javadoc/ektorp/1.2.2/org/ektorp/changes/ChangesFeed.html
							//Ref: http://ektorp.org/reference_documentation.html#d100e1039
							ChangesCommand changesCmd = new ChangesCommand.Builder().since(lastUpdateView)
									.includeDocs(false)
									.continuous(true)
									.heartbeat(1000)
									.build();

							couchChangesAsyncTask = new CouchbaseListChangesAsyncTask(dbConnector, changesCmd);
							couchChangesAsyncTask.execute();
						}
					}

						if(lastUpdateChangesFeed > lastUpdateView) {
							Log.d("CouchCursor", "Finished, but still behind " + lastUpdateChangesFeed + " > " + lastUpdateView);
							updateListItems();
						}
					

				}

				
				@Override
				protected void onDbAccessException(
						DbAccessException dbAccessException) {
					handleViewAsyncTaskDbAccessException(dbAccessException);
				}

			};

			updateListItemsTask.execute();
		}
	}

	private class CouchbaseListChangesAsyncTask extends ChangesFeedAsyncTask {

		public CouchbaseListChangesAsyncTask(CouchDbConnector couchDbConnector,
				ChangesCommand changesCommand) {
			super(couchDbConnector, changesCommand);
			Log.v(TAG, "Changes task setup");

		}

		@Override
		protected void handleDocumentChange(DocumentChange change) {
			lastUpdateChangesFeed = change.getSequence();
			Log.v(TAG, "Handle Document Change "+String.valueOf(lastUpdateChangesFeed));
			
			updateListItems();   
		}

		@Override
		protected void onDbAccessException(DbAccessException dbAccessException) {
			handleChangesAsyncTaskDbAccessException(dbAccessException);
		}

	}
	

	protected void handleViewAsyncTaskDbAccessException(DbAccessException dbAccessException) {
		Log.e(TAG, "DbAccessException accessing view for list", dbAccessException);
	}	
	
	protected void handleChangesAsyncTaskDbAccessException(DbAccessException dbAccessException) {
		Log.e(TAG, "DbAccessException following changes feed for list", dbAccessException);
	}


}
