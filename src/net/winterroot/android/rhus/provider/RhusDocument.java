package net.winterroot.android.rhus.provider;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

@JsonIgnoreProperties(ignoreUnknown = true)

public class RhusDocument implements Parcelable {

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
    public byte[] thumb = null;
    public byte[] medium = null;
    public String reporter;
    public String comment;
    
	

    public RhusDocument(){
    	
    }

    
    
    public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public byte[] getThumb() {
		return thumb;
	}
	public void setThumb(byte[] thumb) {
		this.thumb = thumb;
	}
	public byte[] getMedium() {
		return medium;
	}
	public void setMedium(byte[] medium) {
		this.medium = medium;
	}
	public String getReporter() {
		return reporter;
	}
	public void setReporter(String reporter) {
		this.reporter = reporter;
	}
    public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
    	out.writeString(id);
    	out.writeString(longitude);
    	out.writeString(latitude);
    	out.writeString(created_at);
    	out.writeString(deviceuser_identifier);
    	out.writeInt(thumb.length);
    	out.writeByteArray(thumb);
    	out.writeInt(medium.length);
    	out.writeByteArray(medium);
    	out.writeString(reporter);
    	out.writeString(comment);
    }

	public static final Parcelable.Creator<RhusDocument> CREATOR
	= new Parcelable.Creator<RhusDocument>() {
		public RhusDocument createFromParcel(Parcel in) {
			return new RhusDocument(in);
		}

		public RhusDocument[] newArray(int size) {
			return new RhusDocument[size];
		}
	};

	private RhusDocument(Parcel in) {
    	id = in.readString();
    	longitude = in.readString();
    	latitude = in.readString();
    	created_at = in.readString();
    	deviceuser_identifier = in.readString();
    	int thumbLength = in.readInt();
    	thumb = new byte[thumbLength];
    	in.readByteArray(thumb);
    	int mediumLength = in.readInt();
    	medium = new byte[mediumLength];
    	in.readByteArray(medium);
    	reporter = in.readString();
    	comment = in.readString();
	}

	
}
