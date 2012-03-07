package net.winterroot.android.rhus;

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
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import net.winterroot.android.wildflowers.R;
import net.winterroot.android.rhus.provider.RhusDocument;




public class RhusMapActivity extends MapActivity {

	// Make strings for logging
	private final String TAG = this.getClass().getSimpleName();
	private final String RESTORE = ", can restore state";

	// Map View Defaults
	private final GeoPoint center = new GeoPoint( (int) (42.35*1000000), (int) (-83.07*1000000) );
	private final int fullLatitudeDelta = (int) (.05 * 1000000);
	private final int fullLongitudeDelta = (int) (.05 * 1000000);
	
	// The string "fortytwo" is used as an example of state
	private final String state = "fortytwo";
	
	
	//Maps
	RhusMapItemizedOverlay itemizedoverlay;
	List<Overlay> mapOverlays;
	Cursor documentsCursor;
	MapView mapView;
	boolean startedUpdates = false;
	List<String> loadedMapPoints;

	private class MyTask extends AsyncTask<RhusMapActivity, Void, Void> {


		@Override
		protected Void doInBackground(RhusMapActivity... mapActivities) {
			Log.v(TAG, "Setting document cursor asynchronously");
			RhusMapActivity mapActivity = mapActivities[0];
			documentsCursor = managedQuery(RhusDocument.CONTENT_URI, null,
					null, null, null);
			//Handler handler = new Handler(mapActivity);
			documentsCursor.setNotificationUri(mapActivity.getBaseContext().getContentResolver(), RhusDocument.CONTENT_URI);
			documentsCursor.registerDataSetObserver(new MapDataObserver());
			Log.v(TAG, "Registered Observer");

			return null;
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
	

	
	protected void updateOverlays() throws JsonProcessingException, IOException{
		Log.v(TAG, "Updating Overlays");
		
		ObjectMapper mapper = new ObjectMapper();

		//JsonFactory factory = mapper.getJsonFactory();

		GeoPoint point = new GeoPoint(center.getLatitudeE6(), center.getLongitudeE6());
		OverlayItem overlayitem = new OverlayItem(point, "Sup?", "I'm a plant or another piece of ecological data!");
		itemizedoverlay.addOverlay(overlayitem);
	

		if(documentsCursor != null && documentsCursor.getCount()>0){
			Log.v(TAG, "Reading from the cursor");


			point = new GeoPoint(center.getLatitudeE6(), center.getLongitudeE6()+2000);
			overlayitem = new OverlayItem(point, "Sup?", "I'm a plant or another piece of ecological data!"+Integer.toString(documentsCursor.getCount()));
			itemizedoverlay.addOverlay(overlayitem);


	
			documentsCursor.moveToFirst();
	
		
			int i = 0;
			do {
				//Log.v(TAG, "Loading geopoint from cursor");
			

				String id = documentsCursor.getString(0);
				String document = documentsCursor.getString(1);
					
				JsonNode documentObject = mapper.readTree(document);

				//Log.v(TAG, document);
				//Log.v(TAG, id);

				if(!loadedMapPoints.contains(id) && documentObject.get("latitude") != null ){
					Log.v(TAG, "Adding geopoint from cursor");


					int latitude = (int) (documentObject.get("latitude").getDoubleValue()*1000000);					
					int longitude = (int) (documentObject.get("longitude").getDoubleValue()*1000000);

					GeoPoint point2 = new GeoPoint(latitude, longitude);
					OverlayItem overlayitem2 = new OverlayItem(point2, "NSup?", "I'm a plant or another piece of ecological data!"+id);
					itemizedoverlay.addOverlay(overlayitem2);
					loadedMapPoints.add(id);
					Log.v(TAG, loadedMapPoints.toString());
				}
				i++;
			} while(documentsCursor.moveToNext());

		}
		mapOverlays.add(itemizedoverlay);
		mapView.invalidate();
	}
     
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		Log.v(TAG, "onCreate");
		
        setContentView(R.layout.map);
        mapView = (MapView) findViewById(R.id.mapmain);
        mapView.setBuiltInZoomControls(false);
        MapController mapController = mapView.getController();
        mapController.setCenter(center);
        mapController.zoomToSpan(fullLatitudeDelta, fullLongitudeDelta);
        loadedMapPoints = new ArrayList<String>();
		
   
		Drawable drawable = this.getResources().getDrawable(R.drawable.ic_launcher);
		itemizedoverlay = new RhusMapItemizedOverlay(drawable, this);
    	
        
        mapOverlays = mapView.getOverlays();
        
        MyTask myTask = new MyTask();
        myTask.execute(this);
        
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
	
	
	

	@Override
	protected void onRestart() {
		super.onRestart();
		// Notification that the activity will be started
		Log.i(TAG, "onRestart");
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Notification that the activity is starting
		Log.i(TAG, "onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Notification that the activity will interact with the user
		Log.i(TAG, "onResume");
	}

	protected void onPause() {
		super.onPause();
		// Notification that the activity will stop interacting with the user
		Log.i(TAG, "onPause" + (isFinishing() ? " Finishing" : ""));
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
		outState.putString("answer", state);
		super.onSaveInstanceState(outState);
		Log.i(TAG, "onSaveInstanceState");

	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Log.i(TAG, "onRetainNonConfigurationInstance");
		// It's not what
		return new Integer(getTaskId());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		Log.i(TAG, "onRestoreInstanceState"
				+ (null == savedState ? "" : RESTORE) + " ");
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


}

