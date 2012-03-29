package net.winterroot.android.rhus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import net.winterroot.android.rhus.provider.RhusDocument;

import android.app.Activity;

import com.dogantekin.baloon.BaloonLayout;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import net.winterroot.android.wildflowers.R;
import net.winterroot.android.rhus.provider.RhusDocument;




public class RhusMapActivity extends MapActivity implements LocationListener {

	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 101;
	
	// Make strings for logging
	private final String TAG = this.getClass().getSimpleName();
	private final String RESTORE = ", can restore state";

	// Map View Defaults
	private final GeoPoint center = new GeoPoint( (int) (42.35*1000000), (int) (-83.07*1000000) );
	private final int fullLatitudeDelta = (int) (.05 * 1000000);
	private final int fullLongitudeDelta = (int) (.05 * 1000000);
	
	
	//TODO:  imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
	private Uri imageUri;
	
	private LocationManager locationManager;
	private String bestProvider;
	private Location lastLocation;
	private Drawable marker;
	
	//Maps
	RhusMapItemizedOverlay itemizedOverlay;
	List<Overlay> mapOverlays;
	Cursor documentsCursor;
	MapView mapView;
	MapController mapController;
	boolean startedUpdates = false;
	List<String> loadedMapPoints;
	BaloonLayout noteBaloon;

	private class OverlayDelegate extends RhusMapItemizedOverlayDelegate{

		@Override
		public void onTap(GeoPoint geoPoint, OverlayItem noteOverlay) {

			mapView.removeView(noteBaloon);
			noteBaloon.setVisibility(View.VISIBLE);
			((TextView)noteBaloon.findViewById(R.id.note_text)).setText("Sup Dog");
			mapController.animateTo(geoPoint);
			mapView.addView(noteBaloon, new MapView.LayoutParams(200,200,geoPoint,MapView.LayoutParams.BOTTOM_CENTER));
			mapView.setEnabled(false);       

		}
				
	}
	
	private class QueryMapPointsTask extends AsyncTask<RhusMapActivity, Void, Void> {


		@Override
		protected Void doInBackground(RhusMapActivity... mapActivities) {
			Log.v(TAG, "Setting document cursor asynchronously");
			RhusMapActivity mapActivity = mapActivities[0];
			documentsCursor = managedQuery(RhusDocument.CONTENT_URI, null,
					null, null, null);

			documentsCursor.setNotificationUri(mapActivity.getBaseContext().getContentResolver(), RhusDocument.CONTENT_URI);
			documentsCursor.registerDataSetObserver(new MapDataObserver());
			return null;
		}
		
		protected void onPostExecute(Void result) {
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
        loadedMapPoints = new ArrayList<String>();
		
   
		Drawable drawable = this.getResources().getDrawable(R.drawable.ic_launcher);
		//	marker = this.getResources().getDrawable(R.drawable.mappoint);
		itemizedOverlay = new RhusMapItemizedOverlay(drawable, this);
		itemizedOverlay.setDelegate(new OverlayDelegate());
    	
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

	    LayoutInflater              layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        noteBaloon = (BaloonLayout) layoutInflater.inflate(R.layout.baloon, null);
        RelativeLayout.LayoutParams layoutParams   = new RelativeLayout.LayoutParams(200,100);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        noteBaloon.setLayoutParams(layoutParams);   
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		Log.i(TAG, "onStart");
        
        mapOverlays = mapView.getOverlays();

        QueryMapPointsTask queryMapPointsTask = new QueryMapPointsTask();
        queryMapPointsTask.execute(this);
                
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
	
	protected void updateOverlays() throws JsonProcessingException, IOException{
		Log.v(TAG, "Updating Overlays");
		
		ObjectMapper mapper = new ObjectMapper();


		if(documentsCursor == null){
			Log.v(TAG, "cursor is null");
		}

		if(documentsCursor != null && documentsCursor.getCount()>0){
			Log.v(TAG, "Reading from the cursor");
			documentsCursor.moveToFirst();
			
			int i = 0;
			do {
				Log.v(TAG, "Loading geopoint from cursor");
				String id = documentsCursor.getString(0);
				String document = documentsCursor.getString(1);	
				JsonNode documentObject = mapper.readTree(document);

				if(!loadedMapPoints.contains(id) && documentObject.get("latitude") != null ){
					Log.v(TAG, "Adding geopoint from cursor");
					int latitude = (int) (documentObject.get("latitude").getValueAsDouble()*1000000);					
					int longitude = (int) (documentObject.get("longitude").getValueAsDouble()*1000000);
					if(latitude == 0 && longitude == 0){
						continue;
					}

					GeoPoint point = new GeoPoint(latitude, longitude);
					
					OverlayItem overlayItem = new OverlayItem(point, "Geo-tagged at "+String.valueOf(latitude)+':'+String.valueOf(longitude), "");

					Drawable pointMarker = this.getResources().getDrawable(R.drawable.mappoint);
					pointMarker.setBounds(0, 0, pointMarker.getIntrinsicWidth(),pointMarker.getIntrinsicHeight()); 
					Log.v(TAG, pointMarker.toString());
					overlayItem.setMarker(pointMarker);
					itemizedOverlay.addOverlay(overlayItem);
					loadedMapPoints.add(id);
					//Log.v(TAG, loadedMapPoints.toString());
				}
				i++;
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
		Log.d("LOCATION", "Getting Location Service");
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Log.d("LOCATION", "Got Location Service");


		// List all providers:
		List<String> providers = locationManager.getAllProviders();
		for (String provider : providers) {
			LocationProvider info = locationManager.getProvider(provider);
			Log.d("LOCATION", "huh"+info.toString() + "\n\n");
		}

		
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		bestProvider = locationManager.getBestProvider(criteria, false);
		Log.d("LOCATION", "Best Provider with criteria: "+bestProvider.toString() + "\n\n");

		
		Log.d("LOCATION", "Requesting location updates");
		locationManager.requestLocationUpdates(bestProvider, 1000, 1, (LocationListener) this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		// Notification that the activity will interact with the user
		Log.i(TAG, "onResume");
		
		Log.d("LOCATION", "Resuming location updates");
		//locationManager.requestLocationUpdates(bestProvider, 1000, 1, (LocationListener) this);

	}

	protected void onPause() {
		super.onPause();
		// Notification that the activity will stop interacting with the user
		Log.i(TAG, "onPause" + (isFinishing() ? " Finishing" : ""));
		
		Log.d("LOCATION", "Removing location updates");
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
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK && imageUri != null) {
				
				Log.i(TAG, imageUri.toString());
				File imageFile = RhusMapActivity.convertImageUriToFile(imageUri, this);
				Log.i(TAG, imageFile.toString());
				
				
					
			
				ContentValues values = new ContentValues();
				
				//TODO: lastKnownLocation is probably not accurate enough, we should consider waiting for the next update
				//or allow a user to review the geofix later if necessary.
				//Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				Location loc = lastLocation;
				
				if(loc == null){
					Log.d(TAG, "Lst known location returned NULL, not saving this datapoint");
					//TODO: Handle this exception somehow, probably by kicking them to a map where they can enter their location manually
					//or allowing them to try to get a geofix again. 
					return;
				}
				double latitude = loc.getLatitude();
				double longitude = loc.getLongitude();
				
				values.put("latitude", latitude);
				values.put("longitude", longitude);
				
				
				Bitmap thumb = resizeBitMapImage1(imageFile.getAbsolutePath(), 50, 50);
				Log.d("BINARY", thumb.toString());
				Bitmap medium = resizeBitMapImage1(imageFile.getAbsolutePath(), 320, 480);
				Log.d(TAG, medium.toString());


				ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
				thumb.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			    byte[] thumbData = stream.toByteArray();

				ByteArrayOutputStream stream2 = new ByteArrayOutputStream() ;
				thumb.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
			    byte[] mediumData = stream2.toByteArray();
				
				values.put("thumb", thumbData); ///??? why do we get a crash in insert() ???
				values.put("medium", mediumData);
				
				//values.put()
				
				//values.put("jsonNode", "{ \"latitude\":0, \"longitude\":-83 }");
				
				getContentResolver().insert(RhusDocument.CONTENT_URI, values);


			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
			} else {
				Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
			}
		}
	}
	
	public static File convertImageUriToFile (Uri imageUri, Activity activity)  {
		Cursor cursor = null;
		try {
			String [] proj={MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION};
			cursor = activity.managedQuery( imageUri,
					proj, // Which columns to return
					null,       // WHERE clause; which rows to return (all rows)
					null,       // WHERE clause selection arguments (none)
					null); // Order-by clause (ascending by name)
			int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			int orientation_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
			if (cursor.moveToFirst()) {
				String orientation =  cursor.getString(orientation_ColumnIndex);
				return new File(cursor.getString(file_ColumnIndex));
			}
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	
	public static Bitmap resizeBitMapImage1(String filePath, int targetWidth,
            int targetHeight) {
        Bitmap bitMapImage = null;
        // First, get the dimensions of the image
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        double sampleSize = 0;
        // Only scale if we need to
        // (16384 buffer for img processing)
        Boolean scaleByHeight = Math.abs(options.outHeight - targetHeight) >= Math
                .abs(options.outWidth - targetWidth);

        if (options.outHeight * options.outWidth * 2 >= 1638) {
            // Load, scaling to smallest power of 2 that'll get it <= desired
            // dimensions
            sampleSize = scaleByHeight ? options.outHeight / targetHeight
                    : options.outWidth / targetWidth;
            sampleSize = (int) Math.pow(2d,
                    Math.floor(Math.log(sampleSize) / Math.log(2d)));
        }

        // Do the actual decoding
        options.inJustDecodeBounds = false;
        options.inTempStorage = new byte[128];
        while (true) {
            try {
                options.inSampleSize = (int) sampleSize;
                bitMapImage = BitmapFactory.decodeFile(filePath, options);

                break;
            } catch (Exception ex) {
                try {
                    sampleSize = sampleSize * 2;
                } catch (Exception ex1) {

                }
            }
        }

        return bitMapImage;
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

