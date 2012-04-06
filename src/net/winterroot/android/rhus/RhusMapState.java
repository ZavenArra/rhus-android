package net.winterroot.android.rhus;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;

public class RhusMapState{
   // public Overlay image;
    public GeoPoint center;
    public int latitudeSpan;
    public int longitudeSpan;

    public RhusMapState( GeoPoint center, int latitudeSpan, int longitudeSpan){
        this.center = center;
        this.latitudeSpan = latitudeSpan;
        this.longitudeSpan = longitudeSpan;
    }
}

