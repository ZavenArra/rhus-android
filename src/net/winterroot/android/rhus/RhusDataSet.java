package net.winterroot.android.rhus;

import net.winterroot.android.rhus.provider.RhusDocument;
import net.winterroot.android.util.RhusDevice;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class RhusDataSet {
	
	private boolean userDataOnly;
	//private int[] boundingBox[]; use GeoJson  http://www.mapfish.org/svn/mapfish/contribs/java-geojson/trunk/src/main/java/org/mapfish/geo/
	private boolean threePetal;
	private boolean fourPetal;
	private boolean fivePetal;
	private boolean sixPetal;
	private boolean manyPetal;
	private boolean tree;
	private boolean fruit;
	private boolean composite;
	private boolean irregular;
	private RhusApplication applicationContext;
	
	public boolean isUserDataOnly() {
		return userDataOnly;
	}
	public void setUserDataOnly(boolean userDataOnly) {
		this.userDataOnly = userDataOnly;
	}
	public boolean isThreePetal() {
		return threePetal;
	}
	public void setThreePetal(boolean threePetal) {
		this.threePetal = threePetal;
	}
	public boolean isFourPetal() {
		return fourPetal;
	}
	public void setFourPetal(boolean fourPetal) {
		this.fourPetal = fourPetal;
	}
	public boolean isFivePetal() {
		return fivePetal;
	}
	public void setFivePetal(boolean fivePetal) {
		this.fivePetal = fivePetal;
	}
	public boolean isSixPetal() {
		return sixPetal;
	}
	public void setSixPetal(boolean sixPetal) {
		this.sixPetal = sixPetal;
	}
	public boolean isManyPetal() {
		return manyPetal;
	}
	public void setManyPetal(boolean manyPetal) {
		this.manyPetal = manyPetal;
	}
	public boolean isTree() {
		return tree;
	}
	public void setTree(boolean tree) {
		this.tree = tree;
	}
	public boolean isFruit() {
		return fruit;
	}
	public void setFruit(boolean fruit) {
		this.fruit = fruit;
	}
	public boolean isComposite() {
		return composite;
	}
	public void setComposite(boolean composite) {
		this.composite = composite;
	}
	public boolean isIrregular() {
		return irregular;
	}
	public void setIrregular(boolean irregular) {
		this.irregular = irregular;
	}
	
	public RhusDataSet(RhusApplication context){
		applicationContext = context;
	}
	
	public Uri getQueryUri(){
		Uri queryUri;
		if(userDataOnly){
			queryUri = RhusDocument.USER_DOCUMENTS_URI.buildUpon().appendQueryParameter("deviceuser_identifier", 
					applicationContext.rhusDevice.getDeviceId()).build();
		} else {
			queryUri = RhusDocument.CONTENT_URI;	
		}
		return queryUri;
	
	
	}
	
	public void purgeCursor() {
		// TODO Auto-generated method stub
		
	}

}
