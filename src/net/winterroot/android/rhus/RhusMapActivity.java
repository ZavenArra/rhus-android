package net.winterroot.android.rhus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.dogantekin.baloon.BaloonLayout;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import net.winterroot.android.util.*;
import net.winterroot.android.rhus.R;
import net.winterroot.android.rhus.configuration.RhusDevelopmentConfiguration;
import net.winterroot.android.rhus.provider.RhusDocumentContentProvider;
import net.winterroot.android.rhus.provider.RhusDocument;


public class RhusMapActivity extends MapActivity implements LocationListener {

	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 101;
	
	// Make strings for logging
	private final String TAG = this.getClass().getSimpleName();
	private final String RESTORE = ", can restore state";


	//private final int fullLatitudeDelta = (int) (50 * 1000000);
	//private final int fullLongitudeDelta = (int) (50 * 1000000);
	

	//Taking Pictures
	private Uri imageUri;
	private long captureTime = 0;
	
	private LocationManager locationManager;
	private String bestProvider;
	private Location lastLocation;
	
	//Maps
	RhusMapItemizedOverlay itemizedOverlay;
	List<Overlay> mapOverlays;
	Cursor documentsCursor;
	MapView mapView;
	MapController mapController;
	boolean startedUpdates = false;
	HashMap<String, RhusDocument> loadedMapPoints;
	BaloonLayout noteBaloon;
	RelativeLayout mapOptionsBaloon;
	boolean mapOptionsShowing = false;
	boolean updatingMapPoints = false;
	boolean lockInterface = false;

	private class OverlayDelegate extends RhusMapItemizedOverlayDelegate{

		@Override
		public void onTap(GeoPoint geoPoint, RhusOverlayItem rhusOverlayItem) {

			mapView.removeView(noteBaloon);
			
			if( loadedMapPoints.containsKey(rhusOverlayItem.documentId()) ){
				final RhusDocument document = loadedMapPoints.get(rhusOverlayItem.documentId());
				((TextView)noteBaloon.findViewById(R.id.note_text)).setText(document.created_at);
				
				ImageView thumbnail = ((ImageView)noteBaloon.findViewById(R.id.thumbnail));
				if(document.thumb != null){
					ByteArrayInputStream is = new ByteArrayInputStream(document.thumb);
					Drawable drw = Drawable.createFromStream(is, "thumbnailImage");
					thumbnail.setImageDrawable(drw);
				} else {
					thumbnail.setImageDrawable(null);
				}
				
				thumbnail.setOnClickListener(	new OnClickListener(){
					public void onClick(View arg0) {
		
						Intent intent = new Intent("net.winterroot.android.rhus.action.DOCUMENTDETAIL");
						intent.putExtra(RhusDocumentDetailActivity.DOCUMENT_EXTRA, document );
						startActivity(intent);

					}
				});

				mapView.addView(noteBaloon, new MapView.LayoutParams(150,230,geoPoint,MapView.LayoutParams.BOTTOM_CENTER));
				noteBaloon.setVisibility(View.VISIBLE);

			}

			mapController.animateTo(geoPoint);
	
		}
	}
	
	private class QueryMapPointsTask extends AsyncTask<RhusMapActivity, Void, Void> {


		@Override
		protected Void doInBackground(RhusMapActivity... mapActivities) {
			Log.v(TAG, "Setting document cursor asynchronously");
			RhusMapActivity mapActivity = mapActivities[0];
			
			Uri workingSetUri = ((RhusApplication) getApplicationContext()).rhusDataSet.getQueryUri();
			documentsCursor  = managedQuery(workingSetUri, null, null, null, null);
	
			documentsCursor.setNotificationUri(mapActivity.getBaseContext().getContentResolver(), workingSetUri);
			documentsCursor.registerDataSetObserver(new MapDataObserver());
			return null;
		}
		
		protected void onPostExecute(Void result) {
			
			clearMapPoints();
			
			try {
				updateOverlays();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.v(TAG, "updatingMapPoints FALSE");
			updatingMapPoints = false;
		}

	}
	
	private class MapDataObserver extends DataSetObserver {

		@Override
		public void onChanged(){
			super.onChanged();
			Log.v(TAG, "DataSetObserver onChanged()");

			try {
				updateOverlays();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	}
	
     
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		Log.v(TAG, "onCreate");
		
		/* for Testing * /
		Intent intent = new Intent("net.winterroot.android.rhus.action.PROJECTS_LIST");
		startActivity(intent);
		
		/* */
		
		
		startLocationUpdates();
		
        setContentView(R.layout.map);
        mapView = (MapView) findViewById(R.id.mapmain);
        mapView.setBuiltInZoomControls(false);
        mapController = mapView.getController();
        
        //TODO: getLastNon.. is deprecated.  Convert this to user the normal 'bundle' paradigm
        RhusMapState mapState = (RhusMapState) getLastNonConfigurationInstance();
        if(mapState != null){
            mapController.zoomToSpan(mapState.latitudeSpan, mapState.longitudeSpan);
        } else {
        	mapController.setCenter(RhusDevelopmentConfiguration.center);
        	mapController.zoomToSpan(RhusDevelopmentConfiguration.fullLatitudeDelta, RhusDevelopmentConfiguration.fullLongitudeDelta);
        }
        
        
        
        loadedMapPoints = new HashMap<String, RhusDocument>();
		
		Drawable drawable = this.getResources().getDrawable(R.drawable.ic_launcher);
		itemizedOverlay = new RhusMapItemizedOverlay(drawable, this);
		itemizedOverlay.setDelegate(new OverlayDelegate());
    	
		inflateLayouts();
		
		//Wire up camera activity button
		((ImageButton) findViewById(R.id.cameraButton)).setOnClickListener(
				new OnClickListener(){
					public void onClick(View arg0) {
						if(lockInterface){
							return;
						}
						
						//Check that we have a geo-fix
						if(lastLocation == null){
							Toast.makeText( getBaseContext(), "You do not currently have a geo-fix.  Please make sure your phone can 'see' the GPS satellites (make sure you are outside) and retry.", Toast.LENGTH_SHORT ).show();
							return;

						}
						
						//define the file-name to save photo taken by Camera activity
						String fileName = "new-photo-name.jpg";
						//create parameters for Intent with filename
						ContentValues values = new ContentValues();
						values.put(MediaStore.Images.Media.TITLE, fileName);
						values.put(MediaStore.Images.Media.DESCRIPTION,"Image capture by camera");
						//imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
						imageUri = getContentResolver().insert(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
						//create new Intent
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
						intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
						captureTime = new Date().getTime();
						lockInterface = true;
						startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

					}
				}		
				);

		final RhusMapActivity mapActivity = this;
		//TODO: Clicking back and forth quickly could create a race condition.
		( (ImageButton) findViewById(R.id.everyonesDataButton )).setOnClickListener(
				new OnClickListener(){
					public void onClick(View arg0) {
						((ImageButton) findViewById(R.id.everyonesDataButton )).setImageDrawable(mapActivity.getResources().getDrawable(R.drawable.everyoneselected));
						((ImageButton) findViewById(R.id.myDataButton )).setImageDrawable(mapActivity.getResources().getDrawable(R.drawable.mydeselected));
						((RhusApplication)getApplicationContext()).rhusDataSet.setUserDataOnly(false);
						updateMapPoints();

					}
				}
				);
		( (ImageButton) findViewById(R.id.myDataButton)).setOnClickListener(
				new OnClickListener(){
					public void onClick(View arg0) {
						((ImageButton) findViewById(R.id.everyonesDataButton )).setImageDrawable(mapActivity.getResources().getDrawable(R.drawable.everyonedeselected));
						((ImageButton) findViewById(R.id.myDataButton )).setImageDrawable(mapActivity.getResources().getDrawable(R.drawable.myselected));
						((RhusApplication)getApplicationContext()).rhusDataSet.setUserDataOnly(true);
						updateMapPoints();

					}
				}
				);
		
		final ImageButton mapOptionsButton = (ImageButton) findViewById(R.id.mapOptionsButton);
		mapOptionsButton.setOnClickListener(
				new OnClickListener(){
					public void onClick(View arg0){
								
						RelativeLayout mapOptionsBaloon = (RelativeLayout) findViewById(R.id.mapOptionsBaloon);
						if(mapOptionsShowing){
							mapOptionsBaloon.setVisibility(View.INVISIBLE);
							mapOptionsShowing = false;
						} else {
							mapOptionsBaloon.setVisibility(View.VISIBLE);	
							mapOptionsShowing = true;
						}
					
					}
				}		
				);
		
		/*
		( (ImageButton) findViewById(R.id.backer_button)).setOnClickListener(
				new OnClickListener(){
					public void onClick(View arg0) {
						Intent intent = new Intent("net.winterroot.android.wildflowers.BACKER_LIST");
			        	startActivity(intent);

					}
				}
				);
 		*/
 
		
		( (ImageButton) findViewById(R.id.projectsButton)).setOnClickListener(
				new OnClickListener(){
					public void onClick(View arg0) {
						Intent intent = new Intent("net.winterroot.android.rhus.action.PROJECTS_LIST");
			        	startActivity(intent);

					}
				}
				);
 	
		
        ( (ImageButton) noteBaloon.findViewById(R.id.close_button)).setOnClickListener(
        		new OnClickListener(){
        			public void onClick(View arg0) {
        				noteBaloon.setVisibility(View.GONE);                    
        				mapView.setEnabled(true);    
        			}
        		}
        		);


	}
	
	private void inflateLayouts(){
	    LayoutInflater              layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        noteBaloon = (BaloonLayout) layoutInflater.inflate(R.layout.baloon, null);
        RelativeLayout.LayoutParams layoutParams   = new RelativeLayout.LayoutParams(10,20);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        noteBaloon.setLayoutParams(layoutParams);  
	}
	
	private void updateMapPoints(){
		if(!updatingMapPoints){
			QueryMapPointsTask queryMapPointsTask = new QueryMapPointsTask();
			queryMapPointsTask.execute(this);
			updatingMapPoints = true;
			Log.v(TAG, "updatingMapPoints TRUE");

		}
	}
	
	private void clearMapPoints(){
		itemizedOverlay.removeAllOverlays();
		loadedMapPoints.clear();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		Log.i(TAG, "onStart");
        
        mapOverlays = mapView.getOverlays();
        QueryMapPointsTask queryMapPointsTask = new QueryMapPointsTask();
        queryMapPointsTask.execute(this);
   
 	}
	
	protected void updateOverlays() throws JsonProcessingException, IOException{
		Log.v(TAG, "Updating Overlays");
		
		ObjectMapper mapper = new ObjectMapper();


		if(documentsCursor == null){
			Log.v(TAG, "cursor is null");
		}

		if(documentsCursor != null && documentsCursor.getCount()>0){
			Log.v(TAG, "Reading from the cursor"+ documentsCursor.getCount());
			documentsCursor.moveToFirst();
			
			do {
				String id = documentsCursor.getString(0);				
				Log.v(TAG, "Loading geopoint from cursor: "+id);

				String documentJson = documentsCursor.getString(1);	
			//	JsonNode documentObject = mapper.readTree(document);
				RhusDocument document = mapper.readValue(documentJson, RhusDocument.class);	
				
				
				if(!loadedMapPoints.containsKey(id) && document.latitude != null ){
					Log.v(TAG, "Adding geopoint from cursor");
					int latitude = (int) (new Double(document.latitude) *1000000);					
					int longitude = (int) ( new Double(document.longitude) *1000000);
					if(latitude == 0 && longitude == 0){
						Log.v(TAG, "Ignoring datapoint - 0:0 coordinate");
						continue;
					}

					GeoPoint point = new GeoPoint(latitude, longitude);
					
					OverlayItem overlayItem = new RhusOverlayItem(point, id);

					Drawable pointMarker;
					String identifier = document.deviceuser_identifier;
					
					if( identifier!= null && (identifier.equals( ((RhusApplication) getApplicationContext()).rhusDevice.getDeviceId()))){
						pointMarker = this.getResources().getDrawable(R.drawable.map_device_user_point);
					} else {
						pointMarker = this.getResources().getDrawable(R.drawable.mappoint);
					}
					
					pointMarker.setBounds(0, 0, pointMarker.getIntrinsicWidth(),pointMarker.getIntrinsicHeight()); 
					overlayItem.setMarker(pointMarker);
					itemizedOverlay.addOverlay(overlayItem);
					loadedMapPoints.put(id, document);
		
					//Log.v(TAG, loadedMapPoints.toString());
				}
			} while(documentsCursor.moveToNext());

		}
		mapOverlays.add(itemizedOverlay);
		mapView.invalidate();
	}


	@Override
	protected void onRestart() {
		super.onRestart();

		// Notification that the activity will be started
		Log.i(TAG, "onRestart");
	}
	
	private void startLocationUpdates(){
		Log.i("LOCATION", "Getting Location Service");
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Log.i("LOCATION", "Got Location Service");


		// List all providers:
		if( !locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER ) ) {
			//.setGravity(Gravity.TOP, 0, 0)
			Toast.makeText( this, "Please turn on GPS", Toast.LENGTH_SHORT ).show();
        	Intent myIntent = new Intent( Settings.ACTION_SECURITY_SETTINGS );
        	startActivity(myIntent);
        	return;
		}
		
		/*
		List<String> providers = locationManager.getAllProviders();
		for (String provider : providers) {
			LocationProvider info = locationManager.getProvider(provider);
			Log.i("LOCATION", "huh"+info.toString() + "\n\n");
		}
		*/

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		bestProvider = locationManager.getBestProvider(criteria, false);
		Log.i("LOCATION", "Best Provider with criteria: "+bestProvider.toString() + "\n\n");

		
		Log.i("LOCATION", "Requesting location updates");
		locationManager.requestLocationUpdates(bestProvider, 1000, 1, (LocationListener) this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		// Notification that the activity will interact with the user
		Log.i(TAG, "onResume");
		
		Log.i("LOCATION", "Resuming location updates");
		startLocationUpdates();

	}

	protected void onPause() {
		super.onPause();
		// Notification that the activity will stop interacting with the user
		Log.i(TAG, "onPause" + (isFinishing() ? " Finishing" : ""));
		
		Log.i("LOCATION", "Removing location updates");
		//locationManager.removeUpdates( (LocationListener) this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Notification that the activity is no longer visible
		Log.i(TAG, "onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Notification the activity will be destroyed
		Log.i(TAG,
				"onDestroy "
				// Log which, if any, configuration changed
				+ Integer.toString(getChangingConfigurations(), 16));
	}

	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Save instance-specific state
		super.onSaveInstanceState(outState);

		//TODO: Save current zoom bounds
		
		//Save myData or everyonesData view status
		//Save datapoints currently being viewed for quick use during launch
		
		Log.i(TAG, "onSaveInstanceState");

	}

	
	
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		Log.i(TAG, "onRetainNonConfigurationInstance");

		RhusMapState mapState = new RhusMapState(
				mapView.getMapCenter(),
				mapView.getLatitudeSpan(),
				mapView.getLongitudeSpan()
				);
		return mapState;
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		
		//TODO: Load last saved zoom bounds
		
	}

	// ////////////////////////////////////////////////////////////////////////////
	// These are the minor lifecycle methods, you probably won't need these
	// ////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onPostCreate(Bundle savedState) {
		super.onPostCreate(savedState);

		Log.i(TAG, "onPostCreate"
				+ (null == savedState ? "" : (RESTORE + " " )));

	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		Log.i(TAG, "onPostResume");
		

	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		Log.i(TAG, "onUserLeaveHint");
	}

    /**
     * Required method to indicate whether we display routes
     */
    @Override
    protected boolean isRouteDisplayed() { return false; }

    
    
    /**
     * Camera Activity Calls
     */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "onActivityResult");
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK && imageUri != null) {
				
			
				startLocationUpdates();
				
				Log.i(TAG, imageUri.toString());
				File imageFile = Rhimage.convertImageUriToFile(imageUri, this);
				
				ContentValues values = new ContentValues();
				
				//TODO: lastKnownLocation is probably not accurate enough, we should consider waiting for the next update
				//or allow a user to review the geofix later if necessary.
				//Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				Location loc = lastLocation;
				
				if(loc == null){
					Log.i(TAG, "Last known location returned NULL, not saving this datapoint");
					//TODO: Handle this exception somehow, probably by kicking them to a map where they can enter their location manually
					//or allowing them to try to get a geofix again. 
					//return;
					loc = new Location("gps");
					loc.setLatitude(42.35*1000000);
					loc.setLongitude(83.35*1000000);
					
				}
				double latitude = loc.getLatitude();
				double longitude = loc.getLongitude();
				
				values.put("latitude", latitude);
				values.put("longitude", longitude);
				
				//Get correct rotation information
				int rotation =-1;
				long fileSize = imageFile.length();

				Cursor mediaCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] {MediaStore.Images.ImageColumns.ORIENTATION, MediaStore.MediaColumns.SIZE }, MediaStore.MediaColumns.DATE_ADDED + ">=?", new String[]{String.valueOf(captureTime/1000 - 1)}, MediaStore.MediaColumns.DATE_ADDED + " desc");

				//Code from http://stackoverflow.com/questions/8450539/images-taken-with-action-image-capture-always-returns-1-for-exifinterface-tag-or/8864367#8864367
				if (mediaCursor != null && captureTime != 0 && mediaCursor.getCount() !=0 ) {
					while(mediaCursor.moveToNext()){
						long size = mediaCursor.getLong(1);
						//Extra check to make sure that we are getting the orientation from the proper file
						if(size == fileSize){
							rotation = mediaCursor.getInt(0);
							break;
						}
					}
				} else if(rotation == -1){
					ExifInterface exif = null;
					try {
						exif = new ExifInterface(imageFile.getPath());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
				}
				captureTime = 0;
				
				Bitmap thumb = Rhimage.resizeBitMapImage1(imageFile.getAbsolutePath(), 100, 100, rotation);
				Log.i("BINARY", thumb.toString());
				Bitmap medium = Rhimage.resizeBitMapImage1(imageFile.getAbsolutePath(), 320, 480, rotation);
				Log.i(TAG, medium.toString());


				ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
				thumb.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			    byte[] thumbData = stream.toByteArray();

				ByteArrayOutputStream stream2 = new ByteArrayOutputStream() ;
				medium.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
			    byte[] mediumData = stream2.toByteArray();
				
				values.put("thumb", thumbData);
				values.put("medium", mediumData);
				
				values.put("deviceuser_identifier", ((RhusApplication) getApplicationContext()).rhusDevice.getDeviceId());
				
				getContentResolver().insert(RhusDocument.CONTENT_URI, values);
				
				
				/*TODO: Skipping for now
				Intent intent = new Intent("net.winterroot.android.rhus.action.SUBMITFORM");
				startActivity(intent);
				*/


			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
			} else {
				Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
			}
		}
		lockInterface = false;

	}
	
	
	
	//Location Listener Events
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Log.i("LOCATION", "LocationChanged"+location.toString() );
		Log.i("LOCATION", locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).toString() );
		lastLocation = location;
	
		
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		Log.i(TAG, "Provider Enabled" );
		startLocationUpdates();
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}



}

