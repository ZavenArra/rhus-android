package net.winterroot.android.rhus.provider;

import android.net.Uri;

public class RhusDocument {

	public static final String AUTHORITY =
	            "net.winterroot.android.rhus.provider.RhusDocument";
	
	public static final String DOCUMENTS = "documents";
	public static final String THUMB = "thumb";
	public static final String IMAGE = "image";
	public static final String USER_DOCUMENTS = "user_documents";
	
    // uri references all videos
    public static final Uri DOCUMENTS_URI = Uri.parse("content://" +
            AUTHORITY + "/" + RhusDocument.DOCUMENTS);
    public static final Uri USER_DOCUMENTS_URI = Uri.parse("content://"+
            AUTHORITY + "/" + RhusDocument.USER_DOCUMENTS);

    /**
     * The content:// style URI for this table
     */
    public static final Uri CONTENT_URI = DOCUMENTS_URI;

    
    
    public String id;
    public String longitude;
    public String latitude;
    public String created_at;
    public String deviceuser_identifier;
    public byte[] thumb;
    public byte[] medium;
    public String author;
    public String comment;
}
