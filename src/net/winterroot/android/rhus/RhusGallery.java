package net.winterroot.android.rhus;

import net.winterroot.android.rhus.R;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class RhusGallery extends Activity {

	public RhusGallery(Context context) {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.detail );

	    Gallery gallery = (Gallery) findViewById(R.id.gallery);
	    gallery.setAdapter(new RhusImageAdapter(this));

	    /*
	    gallery.setOnClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView parent, View v, int position, long id) {
	            Toast.makeText(HelloGallery.this, "" + position, Toast.LENGTH_SHORT).show();
	        }
	    });
	    */
	}
	
	public class RhusImageAdapter extends BaseAdapter {
	    int mGalleryItemBackground;
	    private Context mContext;

	    private Integer[] mImageIds = {
	            R.drawable.ic_launcher,
	            R.drawable.camera
	    };

	    public RhusImageAdapter(Context c) {
	        mContext = c;
	        /*
	        TypedArray attr = mContext.obtainStyledAttributes(R.styleable.HelloGallery);
	        mGalleryItemBackground = attr.getResourceId(
	                R.styleable.HelloGallery_android_galleryItemBackground, 0);
	                */
	      // attr.recycle();
	    }

	    public int getCount() {
	        return mImageIds.length;
	    }

	    public Object getItem(int position) {
	        return position;
	    }

	    public long getItemId(int position) {
	        return position;
	    }

	    public View getView(int arg0, View arg1, ViewGroup arg2) {
	        ImageView imageView = new ImageView(mContext);

	        imageView.setImageResource(mImageIds[arg0]);
	        imageView.setLayoutParams(new Gallery.LayoutParams(150, 100));
	        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
	        imageView.setBackgroundResource(mGalleryItemBackground);

	        return imageView;
	    }

	}
}
