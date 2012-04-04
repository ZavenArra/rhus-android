package net.winterroot.android.rhus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.dogantekin.baloon.BaloonLayout;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
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
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import net.winterroot.android.util.Rhimage;
import net.winterroot.android.wildflowers.R;
import net.winterroot.android.rhus.provider.RhusDocument;

public class RhusMapActivity extends MapActivity implements LocationListener {

	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 101;
	
	// Make strings for logging
	private final String TAG = this.getClass().getSimpleName();
	private final String RESTORE = ", can restore state";

	// Map View Defaults
	private final GeoPoint center = new GeoPoint( (int) (42.35*1000000), (int) (-83.07*1000000) );
//	private final int fullLatitudeDelta = (int) (.05 * 1000000);
//	private final int fullLongitudeDelta = (int) (.05 * 1000000);
	private final int fullLatitudeDelta = (int) (50 * 1000000);
	private final int fullLongitudeDelta = (int) (50 * 1000000);
	
	
	//TODO:  imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
	private Uri imageUri;
	
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
	boolean onlyUserData = false;
	boolean mapOptionsShowing = false;
	boolean updatingMapPoints = false;
	
	//Device
	TelephonyManager tm;
	String deviceId;



	private class OverlayDelegate extends RhusMapItemizedOverlayDelegate{

		@Override
		public void onTap(GeoPoint geoPoint, RhusOverlayItem rhusOverlayItem) {

			mapView.removeView(noteBaloon);
			
			if( loadedMapPoints.containsKey(rhusOverlayItem.documentId()) ){
				RhusDocument document = loadedMapPoints.get(rhusOverlayItem.documentId());
				((TextView)noteBaloon.findViewById(R.id.note_text)).setText(document.created_at);
				
				if(document.thumb != null){
					ByteArrayInputStream is = new ByteArrayInputStream(document.thumb);
					Drawable drw = Drawable.createFromStream(is, "thumbnailImage");
					((ImageView)noteBaloon.findViewById(R.id.thumbnail)).setBackgroundDrawable(drw);
				} else {
					((ImageView)noteBaloon.findViewById(R.id.thumbnail)).setBackgroundDrawable(null);
				}


				
				//Drawable	thumb =  new BitmapDrawable(BitmapFactory.decodeByteArray(b, 0, b.length));

				mapView.addView(noteBaloon, new MapView.LayoutParams(200,200,geoPoint,MapView.LayoutParams.BOTTOM_CENTER));
				noteBaloon.setVisibility(View.VISIBLE);

			}

			mapController.animateTo(geoPoint);
	

			//mapView.setEnabled(false);       

		}
				

	}
	
	private class QueryMapPointsTask extends AsyncTask<RhusMapActivity, Void, Void> {


		@Override
		protected Void doInBackground(RhusMapActivity... mapActivities) {
			Log.v(TAG, "Setting document cursor asynchronously");
			RhusMapActivity mapActivity = mapActivities[0];
			if(onlyUserData){
				documentsCursor = managedQuery(RhusDocument.USER_DOCUMENTS_URI.buildUpon().appendQueryParameter("deviceuser_identifier", deviceId).build(),
						null, null, null, null);
			} else {
				documentsCursor = managedQuery(RhusDocument.CONTENT_URI, null,
					null, null, null);
			}

			documentsCursor.setNotificationUri(mapActivity.getBaseContext().getContentResolver(), RhusDocument.CONTENT_URI);
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
		
		startLocationUpdates();
		
        setContentView(R.layout.map);
        mapView = (MapView) findViewById(R.id.mapmain);
        mapView.setBuiltInZoomControls(false);
        mapController = mapView.getController();
        mapController.setCenter(center);
        mapController.zoomToSpan(fullLatitudeDelta, fullLongitudeDelta);
        loadedMapPoints = new HashMap<String, RhusDocument>();
		
		Drawable drawable = this.getResources().getDrawable(R.drawable.ic_launcher);
		itemizedOverlay = new RhusMapItemizedOverlay(drawable, this);
		itemizedOverlay.setDelegate(new OverlayDelegate());
    	
		inflateLayouts();
		
		//Wire up camera activity button
		((ImageButton) findViewById(R.id.cameraButton)).setOnClickListener(
				new OnClickListener(){
					public void onClick(View arg0) {
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
						updateMapPoints(false);
					}
				}
				);
		( (ImageButton) findViewById(R.id.myDataButton)).setOnClickListener(
				new OnClickListener(){
					public void onClick(View arg0) {
						((ImageButton) findViewById(R.id.everyonesDataButton )).setImageDrawable(mapActivity.getResources().getDrawable(R.drawable.everyonedeselected));
						((ImageButton) findViewById(R.id.myDataButton )).setImageDrawable(mapActivity.getResources().getDrawable(R.drawable.myselected));
						updateMapPoints(true);
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
		
		

 
        
        ( (ImageButton) noteBaloon.findViewById(R.id.close_button)).setOnClickListener(
        		new OnClickListener(){
        			public void onClick(View arg0) {
        				noteBaloon.setVisibility(View.GONE);                    
        				mapView.setEnabled(true);    
        			}
        		}
        		);
       
        tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
    	String tmDevice, tmSerial, tmPhone, androidId;
    	tmDevice = "" + tm.getDeviceId();
    	tmSerial = "" + tm.getSimSerialNumber();
    	androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

    	UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
    	deviceId = "ANDROID"+deviceUuid.toString();
	}
	
	private void inflateLayouts(){
	    LayoutInflater              layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        noteBaloon = (BaloonLayout) layoutInflater.inflate(R.layout.baloon, null);
        RelativeLayout.LayoutParams layoutParams   = new RelativeLayout.LayoutParams(200,100);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        noteBaloon.setLayoutParams(layoutParams);  
	}
	
	private void updateMapPoints(boolean setOnlyUserData){
		if(!updatingMapPoints){
			onlyUserData = setOnlyUserData;
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
					
					if( identifier!= null && (identifier.equals(deviceId))){
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
		List<String> providers = locationManager.getAllProviders();
		for (String provider : providers) {
			LocationProvider info = locationManager.getProvider(provider);
			Log.i("LOCATION", "huh"+info.toString() + "\n\n");
		}

		
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
		//locationManager.requestLocationUpdates(bestProvider, 1000, 1, (LocationListener) this);

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
		return new Integer(getTaskId());
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
				Log.i(TAG, imageFile.toString());
				
				ContentValues values = new ContentValues();
				
				//TODO: lastKnownLocation is probably not accurate enough, we should consider waiting for the next update
				//or allow a user to review the geofix later if necessary.
				//Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				Location loc = lastLocation;
				
				if(loc == null){
					Log.i(TAG, "Lst known location returned NULL, not saving this datapoint");
					//TODO: Handle this exception somehow, probably by kicking them to a map where they can enter their location manually
					//or allowing them to try to get a geofix again. 
					return;
				}
				double latitude = loc.getLatitude();
				double longitude = loc.getLongitude();
				
				values.put("latitude", latitude);
				values.put("longitude", longitude);
				
				
				Bitmap thumb = Rhimage.resizeBitMapImage1(imageFile.getAbsolutePath(), 50, 50);
				Log.i("BINARY", thumb.toString());
				Bitmap medium = Rhimage.resizeBitMapImage1(imageFile.getAbsolutePath(), 320, 480);
				Log.i(TAG, medium.toString());


				ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
				thumb.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			    byte[] thumbData = stream.toByteArray();

				ByteArrayOutputStream stream2 = new ByteArrayOutputStream() ;
				medium.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
			    byte[] mediumData = stream2.toByteArray();
				
				values.put("thumb", thumbData); ///??? why do we get a crash in insert() ???
				values.put("medium", mediumData);
				
				values.put("deviceuser_identifier", deviceId);
				
				getContentResolver().insert(RhusDocument.CONTENT_URI, values);


			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
			} else {
				Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
			}
		}
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
		// TODO Auto-generated method stub
		Log.i(TAG, "Provider Enabled" );

		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}



}

