package net.winterroot.android.rhus;

import java.util.List;

import net.winterroot.android.rhus.provider.RhusDocument;

import android.app.Activity;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.os.Bundle;

import android.content.res.Resources;
import android.database.Cursor;
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

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);

        setContentView(R.layout.map);
        MapView mapView = (MapView) findViewById(R.id.mapmain);
        mapView.setBuiltInZoomControls(false);
        MapController mapController = mapView.getController();
        mapController.setCenter(center);
        mapController.zoomToSpan(fullLatitudeDelta, fullLongitudeDelta);
		
    	Cursor documentsCursor = managedQuery(RhusDocument.CONTENT_URI, null,
                null, null, null);
    	
        
        List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.ic_launcher);
        RhusMapItemizedOverlay itemizedoverlay = new RhusMapItemizedOverlay(drawable, this);
        
        GeoPoint point = new GeoPoint(center.getLatitudeE6(), center.getLongitudeE6());
        OverlayItem overlayitem = new OverlayItem(point, "Sup?", "I'm a plant or another piece of ecological data!"+Integer.toString(documentsCursor.getCount()));
        itemizedoverlay.addOverlay(overlayitem);
        
        documentsCursor.moveToFirst();
        while(documentsCursor.moveToNext()){
        	  GeoPoint point2 = new GeoPoint(center.getLatitudeE6(), center.getLongitudeE6()+100);
              OverlayItem overlayitem2 = new OverlayItem(point2, "NSup?", "I'm a plant or another piece of ecological data!");
              itemizedoverlay.addOverlay(overlayitem2);
              
        }
        
        mapOverlays.add(itemizedoverlay);
        
		Log.v(TAG, "onCreate");
		
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

