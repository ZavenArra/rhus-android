package net.winterroot.android.couchbasemobile.provider;


import android.database.MatrixCursor;
import android.util.Log;
import java.util.List;


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
	
	public static final String TAG = "CouchCursor";
	
	private CouchDbConnector dbConnector;
	protected ViewQuery viewQuery;
	protected boolean followChanges;
	protected List<Row> listRows;
	
	protected EktorpAsyncTask updateListItemsTask;
	protected CouchbaseListChangesAsyncTask couchChangesAsyncTask;
	
	protected long lastUpdateChangesFeed = -1L;
	protected long lastUpdateView = -1L;
	
	public CouchCursor(CouchDbConnector setDbConnector, ViewQuery setViewQuery) {
		super(new String[] {"_id","document"});
		this.dbConnector = setDbConnector;
		this.viewQuery = setViewQuery;
		updateListItems();
	}
	
	protected void updateListItems() {
		Log.v(TAG, "Update List Items Task!");

		
		//if we're not already in the process of updating the list, start a task to do so
		if(updateListItemsTask == null) {

			updateListItemsTask = new EktorpAsyncTask() {

				protected ViewResult viewResult;

				@Override
				protected void doInBackground() {
					Log.v(TAG, "Running Background");
					viewResult = dbConnector.queryView(viewQuery);
					
				}

				protected void onSuccess() {
					Log.v(TAG, "Successfull Query");
					if(viewResult != null) {
						lastUpdateView = viewResult.getUpdateSeq();
						//listRows = viewResult.getRows();
						for(ViewResult.Row aRow : viewResult.getRows()){
							
							Object[] rowValues = new Object[] { aRow.getId(), aRow.getValueAsNode() };
							addRow(rowValues);
						}
						onChange(true);
					}
					updateListItemsTask = null;

					//we want to start our changes feed AFTER
					//getting our first copy of the view
					if(couchChangesAsyncTask == null && followChanges) {
						//create an ansyc task to get updates
						ChangesCommand changesCmd = new ChangesCommand.Builder().since(lastUpdateView)
								.includeDocs(false)
								.continuous(true)
								.heartbeat(5000)
								.build();

						couchChangesAsyncTask = new CouchbaseListChangesAsyncTask(dbConnector, changesCmd);
						couchChangesAsyncTask.execute();
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
		}

		@Override
		protected void handleDocumentChange(DocumentChange change) {
			lastUpdateChangesFeed = change.getSequence();
			updateListItems();   //This updates the list items
								 //With the matrix cursor, we'll still have to notify the view rather that just handle it here.
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
