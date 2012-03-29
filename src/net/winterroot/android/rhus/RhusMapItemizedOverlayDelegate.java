package net.winterroot.android.rhus;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

abstract public class RhusMapItemizedOverlayDelegate {

	abstract public void onTap(GeoPoint geoPoint, OverlayItem item);
	
}
