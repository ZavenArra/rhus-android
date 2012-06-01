package net.winterroot.android.rhus.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.StdDateFormat;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.ektorp.AttachmentInputStream;
import org.ektorp.ComplexKey;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.UpdateConflictException;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.ViewResult.Row;
import org.ektorp.support.DesignDocument;

import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.TDViewMapBlock;
import com.couchbase.touchdb.TDViewMapEmitBlock;
import com.couchbase.touchdb.TDViewReduceBlock;

import net.winterroot.android.rhus.*;
import net.winterroot.android.rhus.configuration.RhusDevelopmentConfiguration;
import net.winterroot.android.touchdb.provider.BlobCursor;
import net.winterroot.android.touchdb.provider.CouchCursor;
import net.winterroot.android.touchdb.provider.TouchDBContentProvider;
import net.winterroot.android.touchdb.provider.CouchbaseMobileEktorpAsyncTask;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.database.MatrixCursor;

public class RhusDocumentContentProvider extends TouchDBContentProvider {
	
	private static final String TAG = "RhusDocumentContentProvider";
	
    private static final String FILE_CACHE_DIR =
    		"/data/data/net.winterroot.net.android.rhus/file_cache";
    
    //Change this manually to build different versions of the app, or set up build targets using ant, etc.
    private static String REPLICATION_URL = RhusDevelopmentConfiguration.REPLICATION_URL;
    private static String BUCKET_NAME = "documents";
	
    private static final int DOCUMENTS = 1;			  //get all documents (possibly filtered)
    private static final int DOCUMENT_ID = 2;		  //get document by id
    private static final int DOCUMENT_THUMB_ID = 3;   //get document thumb by id
    private static final int DOCUMENT_IMAGE_ID = 4;   //get document image by id
    private static final int USER_DOCUMENTS = 5;
    private static final int USER_IDENTIFIER = 6;
    private static final int PROJECTS = 7;
    
    //TODO: This design document should be keep as a JSON text file, the below is not a superb way to handle things
    private static final String dDocName = "rhus";
    private static final String dDocId = "_design/"+dDocName;
    public static final String userDocsViewName = "userDocuments";
	public static final String allDocsViewName = "allDocuments";
	public static final String projectsViewName = "projects";

    private static final String replicationFilter = "  function(doc, req) {"+
    		  "return \"_design/\" !== doc._id.substr(0, 8)" +
    		  "}";

    
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
                RhusDocument.THUMB + "/*",
                DOCUMENT_THUMB_ID);
        
        sUriMatcher.addURI(RhusDocument.AUTHORITY,
                RhusDocument.MEDIUM + "/*",
                DOCUMENT_IMAGE_ID);
        
        sUriMatcher.addURI(RhusDocument.AUTHORITY,
        		RhusDocument.USER_DOCUMENTS,
        		USER_DOCUMENTS);
        
        sUriMatcher.addURI(RhusProject.AUTHORITY,
        		RhusProject.PROJECTS,
        		PROJECTS);
    }
    
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
		
		ensureCouchServiceStarted();
		
	    int match = sUriMatcher.match(uri);
	    switch (match) {
	    case DOCUMENTS:

	    	ObjectNode documentNode = JsonNodeFactory.instance.objectNode();
	    	documentNode.put("latitude", values.getAsDouble("latitude").toString() );
	    	documentNode.put("longitude", values.getAsDouble("longitude").toString() );
	    	documentNode.put("created_at", new StdDateFormat().format(new Date()) );
	    	documentNode.put("deviceuser_identifier", values.getAsString("deviceuser_identifier"));
	    	//documentNode.put("thumb", JsonNodeFactory.instance.binaryNode(values.getAsByteArray("thumb")));
	    	//documentNode.put("medium",JsonNodeFactory.instance.binaryNode(values.getAsByteArray("medium")));

	    	//Skip matching the URI for the moment

	    	//Doing this synchronously - caller should call this content provider asynchronously.
	    	Log.v(TAG, "Adding Document "+ documentNode.toString());
	    	try {
	    		couchDbConnector.create(documentNode);
	    	} catch (UpdateConflictException e) {
	    		Log.d(TAG, "Got an update conflict for: " + documentNode.toString());
	    		return null;
	    	}
	    	Log.d(TAG, "Added "+documentNode.toString());
	    	
	    	String id = documentNode.get("_id").getTextValue();
	    	
	    	byte[] thumb = values.getAsByteArray("thumb");
	    	//byte[] thumb = new byte[] {'a','b'};
	    	ByteArrayInputStream thumbStream =  new ByteArrayInputStream(thumb);
	    	AttachmentInputStream a = new AttachmentInputStream("thumb.jpg",
	    			thumbStream,
	    			"image/jpeg");

	    	String _rev =couchDbConnector.createAttachment(id, documentNode.get("_rev").getTextValue(), a);
	    	
	    	byte[] medium = values.getAsByteArray("medium");
	    	int ln = medium.length;
	    	a = new AttachmentInputStream("medium.jpg",
	    			new ByteArrayInputStream(values.getAsByteArray("medium")),
	    			"image/jpeg");
	    	
	    	couchDbConnector.createAttachment(id, _rev, a);

	    	
	    	
	    	//long id = 0;  //Not the id!!
	    	Log.d(TAG, "FIX: NOT THE ID");
	    	Uri documentUri = RhusDocument.CONTENT_URI.buildUpon().appendPath(id).build();
	    	getContext().getContentResolver().notifyChange(documentUri, null);
	    	//TODO: This should return the Content Provider Uri for the newly created item 
	    	return documentUri;
	    case PROJECTS:
	    	ObjectNode projectNode = JsonNodeFactory.instance.objectNode();
	    	projectNode.put("project", values.getAsString("project"));
	    	try {
	    		couchDbConnector.create(projectNode);
	    	} catch (UpdateConflictException e) {
	    		Log.d(TAG, "Got an update conflict for: " + projectNode.toString());
	    		return null;
	    	}
	    	break;
	    }
	    
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
	public void initialization(){

		Log.v(TAG, "RHUS initialization");

		//Java Views
		
		 final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
		 final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");


		
		TDView allDocsView = db.getViewNamed(String.format("%s/%s", dDocName, allDocsViewName));
		allDocsView.setMapReduceBlocks(new TDViewMapBlock() {
			 
			public void map(Map<String, Object> document,
					TDViewMapEmitBlock emitter) {

				String created_at = (String) document.get("created_at");
				Date date = null;
				if(created_at != null) {
					try {
						date = dateFormatter.parse( created_at.substring(0, 10) );
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				String niceDate = "";
				if(date != null){
					formatter.format(date);
				}
				
				//TODO: break out the below into a function for reuse
				Map<String, String> value = new HashMap<String, String>();
				value.put("id", (String) document.get("_id"));
				value.put("latitude", (String) document.get("latitude"));
				value.put("longitude", (String) document.get("longitude"));
				value.put("reporter", (String) document.get("reporter"));
				value.put("comment", (String) document.get("comment"));
				value.put("created_at", niceDate);
				value.put("deviceuser_identifier", (String) document.get("deviceuser_identifier"));
				
				emitter.emit(niceDate, value);
				
				//function(doc) {  date = new Date(doc.created_at.substr(0,19)); niceDate = (date.getMonth()+1)+\"/\"+date.getDate()+\"/\"+date.getFullYear();
				//emit(doc.created_at,{'id':doc._id, 'thumb':doc.thumb, 'medium':doc.medium, 'latitude':doc.latitude, 'longitude':doc.longitude, 'reporter':doc.reporter, 'comment':doc.comment, 'created_at':niceDate, 'deviceuser_identifier':doc.deviceuser_identifier } );}";
			}
		  }, null, "1.0");

		
		
		 TDView userDocsMapView = db.getViewNamed(String.format("%s/%s", dDocName, userDocsViewName));
		 userDocsMapView.setMapReduceBlocks(new TDViewMapBlock() {
				 
				public void map(Map<String, Object> document,
						TDViewMapEmitBlock emitter) {

					String created_at = (String) document.get("created_at");
				
					Date date = null;
					try {
						date = dateFormatter.parse( created_at.substring(0, 10) );
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String niceDate = formatter.format(date);
					
					Map<String, String> value = new HashMap<String, String>();
					value.put("id", (String) document.get("_id"));
					value.put("latitude", (String) document.get("latitude"));
					value.put("longitude", (String) document.get("longitude"));
					value.put("reporter", (String) document.get("reporter"));
					value.put("comment", (String) document.get("comment"));
					value.put("created_at", niceDate);
					value.put("deviceuser_identifier", (String) document.get("deviceuser_identifier"));
					
					ArrayList<String> key = new ArrayList<String>();
					key.add((String) document.get("deviceuser_identifier"));
					key.add((String) document.get("created_at"));
					emitter.emit(key, value);
				
					//    private static final String userDocsMapFunction = "function(doc) {  date = new Date(doc.created_at.substr(0,19)); niceDate = (date.getMonth()+1)+\"/\"+date.getDate()+\"/\"+date.getFullYear();
//emit([doc.deviceuser_identifier, doc.created_at],{'id':doc._id, 'thumb':doc.thumb, 'medium':doc.medium, 'latitude':doc.latitude, 'longitude':doc.longitude, 'reporter':doc.reporter, 'comment':doc.comment, 'created_at':niceDate, 'deviceuser_identifier':doc.deviceuser_identifier } );}";

				}
			  }, null, "1.0");


		 TDView projectsMapView = db.getViewNamed(String.format("%s/%s", dDocName, projectsViewName));
		 projectsMapView.setMapReduceBlocks(new TDViewMapBlock() {
				 
				public void map(Map<String, Object> document,
						TDViewMapEmitBlock emitter) {

					emitter.emit(document.get("project"), null);
				}

			  }, 
			  new TDViewReduceBlock(){

				public Object reduce(List<Object> keys, List<Object> values,
						boolean rereduce) {
					return true;
				}
			  }, "1.0");
		
	
		Log.v(TAG, "Finished RHUS initialization");
	}

	
	//Stuff to write about
	//Following changes seems really unnecessary because of android activity life cycle, unless you are doing this WITHIN a content provider
	//ASYNC tasks vs. Thread, the whole thing with lossing connections
	//Couchbase Mobile also had this problem, loosing connections to EKTorp
	//Work toward a CouchCursor..  interesting but has lots of issues
	//Async tasks get leaked..
	//Android Cursor implementation is too restrictive, way too geared towards SQLite


	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		ensureCouchServiceStarted();
		
    	Log.v(TAG, "Content provider query");

    	Cursor queryCursor = null;
	    ViewQuery viewQuery;
	    String documentId;
	    
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
	        	
	        case PROJECTS:
	        	viewQuery = new ViewQuery().designDocId(dDocId).viewName(projectsViewName);
	          	String project = uri.getQueryParameter("project");
	          	if(project==null){
	        		Log.e(TAG, "PROJECTS Uri called without project");
	        		//TODO: Improve exception handling
	        		throw new RuntimeException();
	        	}
	          	viewQuery.startKey(project);
	          	viewQuery.endKey(project);
	        	queryCursor = new CouchCursor(couchDbConnector, viewQuery);	
	        	queryCursor.setNotificationUri(getContext().getContentResolver(), uri);
	        	break;
	        	
	        case DOCUMENT_THUMB_ID:
	        	documentId = uri.getLastPathSegment();
	        	queryCursor = this.getBlobCursorForAttachment(documentId, "medium.jpg");
	        	//break; TODO: understand how to return different types of cursors
	        	break;
	        	
	        case DOCUMENT_IMAGE_ID:
	        	documentId = uri.getLastPathSegment();
	        	queryCursor = this.getBlobCursorForAttachment(documentId, "medium.jpg");
	        	
	        	//break; TODO: understand how to return different types of cursors
	        	break;
	    }
   
	    if(queryCursor == null){
	    	throw new RuntimeException();
	    }
	    
	    return queryCursor;
	    
	}
	
	private Cursor getBlobCursorForAttachment(String documentId, String attachmentName){
		AttachmentInputStream data;
    	try {
    		data = couchDbConnector.getAttachment(documentId,
                attachmentName);
    		Log.v(TAG, data.toString());

    	} catch (DocumentNotFoundException e) {
    		return null;
    	}
    	
    	byte[] dataBytes = null;
    	//String  dataBytes = null;
    	try {
    		dataBytes = IOUtils.toByteArray(data);
    	} catch (IOException e1) {
    		// TODO Auto-generated catch block
    		e1.printStackTrace();
    	}	        	

    	if(dataBytes != null){
    		//TODO: this is a terrible dirty hack, because I can't understand how to just
    		//pass back a blob from the content provider without jumping through tons of hoops
    		//AbstractCursor doesn't implement getBlob, so MatrixCursor won't grant access to an assigned blob
    		BlobCursor blobCursor = new BlobCursor();
    		blobCursor.setBlob(dataBytes);
    		return blobCursor;
    	}
    	return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}


	

}
