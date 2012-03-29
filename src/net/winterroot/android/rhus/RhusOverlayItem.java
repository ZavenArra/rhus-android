package net.winterroot.android.rhus;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class RhusOverlayItem extends OverlayItem {

	private String documentId;
	
	public RhusOverlayItem(GeoPoint point, String id) {
		super(point, "", "");
		documentId = id;
	}

}
