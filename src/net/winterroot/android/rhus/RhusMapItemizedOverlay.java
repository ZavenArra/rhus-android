package net.winterroot.android.rhus;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;


public class RhusMapItemizedOverlay extends ItemizedOverlay {
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	private RhusMapItemizedOverlayDelegate delegate = null;
	
	public RhusMapItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		populate();
	}
	
	public RhusMapItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));		
		mContext = context;
		populate();
	}
	
	public void setDelegate(RhusMapItemizedOverlayDelegate setDelegate){
		delegate = setDelegate;
	}

	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    setLastFocusedIndex(-1);
	    populate();
	}
	
	protected void removeOverlay(OverlayItem o){
		mOverlays.remove(o);
	    setLastFocusedIndex(-1);
	    populate();
	}
	
	public void addOverlays(OverlayItem overlay) {
	 //   mOverlays.add(overlay);
	  //  populate();
	}
	
	public void removeAllOverlays(){
		mOverlays.clear();
		populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	protected boolean onTap(int index) {
	  RhusOverlayItem item = (RhusOverlayItem) mOverlays.get(index);
	  GeoPoint geoPoint = item.getPoint();
	  
	  if(delegate != null){
		  delegate.onTap(geoPoint, item);
	  } else {
		  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		  dialog.setTitle(item.getTitle());
		  dialog.setMessage(item.getSnippet());
		  dialog.show();
	  }
	  return true;
	}
}
