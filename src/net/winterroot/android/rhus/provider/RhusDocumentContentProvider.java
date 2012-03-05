package net.winterroot.android.rhus.provider;

//import com.oreilly.demo.pa.finchvideo.provider.FileHandlerFactory;

import net.winterroot.android.couchbasemobile.provider.CouchbaseMobileContentProvider;
import net.winterroot.android.couchbasemobile.provider.CouchbaseMobileEktorpAsyncTask;
import net.winterroot.android.rhus.*;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.test.mock.MockCursor;

import android.net.Uri;
import android.util.Log;
import android.database.MatrixCursor;

public class RhusDocumentContentProvider extends CouchbaseMobileContentProvider {
	
	//TODO: This should be abstract??
    private static final String FILE_CACHE_DIR =
    		"/data/data/net.winterroot.net.android.rhus/file_cache";
    
    private static String REPLICATION_URL = "http://ec2-50-112-24-87.us-west-2.compute.amazonaws.com";
    private static String BUCKET_NAME = "items";
	
    private static final int DOCUMENTS = 1;			  //get all documents (possibly filtered)
    private static final int DOCUMENT_ID = 2;		  //get document by id
    private static final int DOCUMENT_THUMB_ID = 3;   //get document thumb by id
    private static final int DOCUMENT_IMAGE_ID = 4;   //get document image by id

    private static UriMatcher sUriMatcher;
	
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(RhusDocument.AUTHORITY,
                RhusDocument.DOCUMENTS, DOCUMENTS);
        // use of the hash character indicates matching of an id
        sUriMatcher.addURI(RhusDocument.AUTHORITY,
                RhusDocument.DOCUMENTS + "/#",
                DOCUMENT_ID);
        sUriMatcher.addURI(RhusDocument.AUTHORITY,
                RhusDocument.THUMB + "/#",
                DOCUMENT_THUMB_ID);
        sUriMatcher.addURI(RhusDocument.AUTHORITY,
                RhusDocument.IMAGE + "/*",
                DOCUMENT_IMAGE_ID);
    }
   /* 
    public RhusDocumentContentProvider() {
     //	  Include fileHandler for full size image cache
     //   super(new FileHandlerFactory(FILE_CACHE_DIR));
     //   mFileHandlerFactory = new FileHandlerFactory(FILE_CACHE_DIR);

    }

    /*
     * Don't think this is necessary
     * */
    /*
    public RhusDocumentContentProvider(Context setAppContext) {
    	appContext = setAppContext;
        System.out.println("here");
        //super(new FileHandlerFactory(FILE_CACHE_DIR));
        //init();
    }
   
    /*
    
    private void init() {
        mOpenHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null);
        mDb = mOpenHelper.getWritableDatabase();
        mFileHandlerFactory = new FileHandlerFactory(FILE_CACHE_DIR);
    }
    */

    
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override 
	public String getBucketName(){
		return BUCKET_NAME;
	}
	
	@Override
	public String getReplicationUrl(){
		return REPLICATION_URL;
	}
	

	@Override
	public void initializationTask(){
		CouchbaseMobileEktorpAsyncTask initializationTask = new CouchbaseMobileEktorpAsyncTask(){
			
			@Override
			protected void doInBackground() {
				Log.v(TAG, "RHUS initialization");

				/*
				//ensure we have a design document with a view
				//update the design document if it exists, or create it if it does not exist
				//This is handled by a callback from the extender
				//TODO: change the below to be Rhus specific
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
				Log.v(TAG, "Finished RHUS initialization");
			}
		};
		initializationTask.execute();
	}



	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

	    MatrixCursor queryCursor;


	    int match = sUriMatcher.match(uri);
	    switch (match) {
	        case DOCUMENTS:
	        	//This really shouldn't ever happen, or should be limited by default
	        	//to avoid attempts to display a huge dataset
	        	Log.v(TAG, "URI Query for all documents");
	        	break;
	        	
	    }
    	System.out.println("This thing!");
    	Log.v("whatever", "wahtever");
    	queryCursor = new MatrixCursor(new String[] { "" });

		return queryCursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
