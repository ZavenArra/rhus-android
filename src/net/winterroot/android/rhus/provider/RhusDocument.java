package net.winterroot.android.rhus.provider;

import android.net.Uri;

public class RhusDocument {

	public static final String AUTHORITY =
	            "net.winterroot.android.rhus.provider.RhusDocument";
	
	public static final String DOCUMENTS = "documents";
	public static final String THUMB = "thumb";
	public static final String IMAGE = "image";
	
    // uri references all videos
    public static final Uri DOCUMENTS_URI = Uri.parse("content://" +
            AUTHORITY + "/" + RhusDocument.DOCUMENTS);

    /**
     * The content:// style URI for this table
     */
    public static final Uri CONTENT_URI = DOCUMENTS_URI;

}
