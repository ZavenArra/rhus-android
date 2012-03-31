package net.winterroot.android.rhus.provider;

//import com.oreilly.demo.pa.finchvideo.provider.FileHandlerFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.HashMap;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.StdDateFormat;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.ektorp.ComplexKey;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.UpdateConflictException;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.ViewResult.Row;
import org.ektorp.support.DesignDocument;




import net.winterroot.android.couchbasemobile.provider.CouchbaseMobileContentProvider;
import net.winterroot.android.couchbasemobile.provider.CouchbaseMobileEktorpAsyncTask;
import net.winterroot.android.couchbasemobile.provider.CouchCursor;

import net.winterroot.android.rhus.*;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;

import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.database.MatrixCursor;

public class RhusDocumentContentProvider extends CouchbaseMobileContentProvider {
	
	private static final String TAG = "RhusDocumentContentProvider";
	
	//TODO: This should be abstract??
    private static final String FILE_CACHE_DIR =
    		"/data/data/net.winterroot.net.android.rhus/file_cache";
    
    private static String REPLICATION_URL = "http://data.winterroot.net:5984/squirrels_of_the_earth";
    private static String BUCKET_NAME = "documents";
	
    private static final int DOCUMENTS = 1;			  //get all documents (possibly filtered)
    private static final int DOCUMENT_ID = 2;		  //get document by id
    private static final int DOCUMENT_THUMB_ID = 3;   //get document thumb by id
    private static final int DOCUMENT_IMAGE_ID = 4;   //get document image by id
    private static final int USER_DOCUMENTS = 5;
    private static final int USER_IDENTIFIER = 6;
    
    private static final String dDocId = "_design/rhus";
    public static final String userDocsViewName = "userDocuments";
	public static final String allDocsViewName = "allDocuments";
    private static final String userDocsMapFunction = "function(doc) { emit([doc.deviceuser_identifier, doc.created_at],{'id':doc._id, 'thumb':doc.androidThumb1, 'medium':doc.androidMedium1, 'latitude':doc.latitude, 'longitude':doc.longitude, 'reporter':doc.reporter, 'comment':doc.comment, 'created_at':doc.created_at, 'deviceuser_identifier':doc.deviceuser_identifier } );}";
    private static final String allDocsMapFunction = "function(doc) { emit(doc.created_at,{'id':doc._id, 'thumb':doc.androidThumb1, 'medium':doc.androidMedium1, 'latitude':doc.latitude, 'longitude':doc.longitude, 'reporter':doc.reporter, 'comment':doc.comment, 'created_at':doc.created_at, 'deviceuser_identifier':doc.deviceuser_identifier } );}";
    
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
        sUriMatcher.addURI(RhusDocument.AUTHORITY,
        		RhusDocument.USER_DOCUMENTS,
        		USER_DOCUMENTS);
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
		return "vnd.android.cursor.dir/vnd.rhus.document";
	}

	/*
	 * TODO:  This is a kludgey temporary workaround.  ContentProvider takes arguments of ContentValues, but it 
	 * makes more sense to call with Json, or to provide a class for converting between Json and ContentValues objects
	 * For the time being we just bypass the insert method.
	 * Jackson ObjectMapper would be wonderful to use here, if the 'ContentValues' can be circumvented while
	 * still allowing this class to subclass contentprovider
	 */
	
	public Uri insert(Uri uri, ContentValues values) {
		
		/*
		final String saveItemString = values.getAsString("jsonNode");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode saveItem = null;
		try {
			saveItem = mapper.readTree(saveItemString);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		
		ObjectNode documentNode = JsonNodeFactory.instance.objectNode();
		documentNode.put("latitude", values.getAsDouble("latitude").toString() );
		documentNode.put("longitude", values.getAsDouble("longitude").toString() );
		documentNode.put("created_at", new StdDateFormat().format(new Date()) );
		documentNode.put("deviceuser_identifier", values.getAsString("deviceuser_identifier"));
		documentNode.put("thumbAndroid1", JsonNodeFactory.instance.binaryNode(values.getAsByteArray("thumb")));
		documentNode.put("mediumAndroid1",JsonNodeFactory.instance.binaryNode(values.getAsByteArray("medium")));

		//Skip matching the URI for the moment

		//Doing this synchronously - caller should call this content provider aynchronously.
		Log.v(TAG, "Adding Document "+ documentNode.toString());
		try {
			couchDbConnector.create(documentNode);
		} catch (UpdateConflictException e) {
			Log.d(TAG, "Got an update conflict for: " + documentNode.toString());
			return null;
		}
		
		
		//And then add the attachments
		// ??
		
		Log.d(TAG, "Document created successfully");

		long id = 0;  //Not the id!!
		Log.d(TAG, "FIX: NOT THE ID");
		Uri documentUri = ContentUris.withAppendedId(
				RhusDocument.CONTENT_URI, id);
		getContext().getContentResolver().notifyChange(documentUri, null);
		
		
		//TODO: This should return the Content Provider Uri for the newly created item 
		return null;
	}
	
	@Override 
	public String getBucketName(){
		return BUCKET_NAME;
	}
	
	@Override
	public String getReplicationUrl(){
		Log.v(TAG, REPLICATION_URL);
		return REPLICATION_URL;
	}
	

	@Override
	public void initialization(){

		Log.v(TAG, "RHUS initialization");


		//ensure we have a design document with a view
		//update the design document if it exists, or create it if it does not exist
		//TODO: change the below to be Rhus specific
		try {
			DesignDocument dDoc = couchDbConnector.get(DesignDocument.class, dDocId);
			dDoc.addView(allDocsViewName, new DesignDocument.View(allDocsMapFunction));
			couchDbConnector.update(dDoc);
		}
		catch(DocumentNotFoundException ndfe) {
			DesignDocument dDoc = new DesignDocument(dDocId);
			dDoc.addView(allDocsViewName, new DesignDocument.View(allDocsMapFunction));
			couchDbConnector.create(dDoc);
		}

		
		try {
			DesignDocument dDoc = couchDbConnector.get(DesignDocument.class, dDocId);
			dDoc.addView(userDocsViewName, new DesignDocument.View(userDocsMapFunction));
			couchDbConnector.update(dDoc);
		}
		catch(DocumentNotFoundException ndfe) {
			DesignDocument dDoc = new DesignDocument(dDocId);
			dDoc.addView(userDocsViewName, new DesignDocument.View(userDocsMapFunction));
			couchDbConnector.create(dDoc);
		}

		Log.v(TAG, "Finished RHUS initialization");
	}



	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		ensureCouchServiceStarted();
		
    	Log.v(TAG, "Content provider query");

	    CouchCursor queryCursor = null;
	    ViewQuery viewQuery;

	    int match = sUriMatcher.match(uri);
	    switch (match) {
	        case DOCUMENTS:
	        	//This really shouldn't ever happen, or should be limited by default
	        	//to avoid attempts to display a huge dataset
	        	Log.v(TAG, "URI Query for all documents");
	        	viewQuery = new ViewQuery().designDocId(dDocId).viewName(allDocsViewName).updateSeq(true);	
	        	queryCursor = new CouchCursor(couchDbConnector, viewQuery);
	        	queryCursor.setNotificationUri(getContext().getContentResolver(), uri);
	        	
	        	break;
	        	
	        case USER_DOCUMENTS:
	        	Log.v(TAG, "URI Query for user documents");
	        	viewQuery = new ViewQuery().designDocId(dDocId).viewName(userDocsViewName).updateSeq(true);
	        	String deviceuser_identifier = uri.getQueryParameter("deviceuser_identifier");
	        	if(deviceuser_identifier==null){
	        		Log.e(TAG, "USER_DOCUMENTS Uri called without user_identifier");
	        		//TODO: Improve exception handling
	        		throw new RuntimeException();
	        	}
	        	
	        	ComplexKey startKey = ComplexKey.of(deviceuser_identifier, new HashMap());
	        	ComplexKey endKey = ComplexKey.of(deviceuser_identifier);
	        	viewQuery.startKey(startKey);
	        	viewQuery.endKey(endKey);
	        	viewQuery.descending(true);
	        	queryCursor = new CouchCursor(couchDbConnector, viewQuery);
	        	queryCursor.setNotificationUri(getContext().getContentResolver(), uri);
	        	break;
	        	
	    }
   
	    return queryCursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}



}
