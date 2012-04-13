package net.winterroot.android.rhus;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.winterroot.android.rhus.provider.RhusDocument;
import net.winterroot.android.wildflowers.R;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.RelativeLayout;



public class RhusDocumentDetailActivity extends Activity {
	
	static final String DOCUMENT_EXTRA = "net.winterroot.android.rhus.RhusDocumentDetailActivity_Document_Extra";

	private RhusDocument document = null; 

	private String TAG = "RhusDocumentDetailActivity";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		document = extras.getParcelable(RhusDocumentDetailActivity.DOCUMENT_EXTRA);
					
        setContentView(R.layout.documentdetail);
        
        ((ImageButton) findViewById(R.id.closeDocumentDetail)).setOnClickListener(	new OnClickListener(){
			public void onClick(View arg0) {
				finish();
			}
        }
		);
        
        ((ImageButton) findViewById(R.id.infoButton)).setOnClickListener(	new OnClickListener(){
   			public void onClick(View arg0) {
   				((TextView) findViewById(R.id.name)).setText(document.reporter);
   				((TextView) findViewById(R.id.date)).setText(document.created_at);
   				((TextView) findViewById(R.id.comments)).setText(document.comment);
   				((TextView) findViewById(R.id.location)).setText(document.latitude+" "+document.longitude);
   				((RelativeLayout) findViewById(R.id.infoView)).setVisibility(View.VISIBLE);
   			}
           }
   		);
        
        ((ImageButton) findViewById(R.id.closeinfo)).setOnClickListener(	new OnClickListener(){
   			public void onClick(View arg0) {
   				((RelativeLayout) findViewById(R.id.infoView)).setVisibility(View.INVISIBLE);
   			}
           }
   		);
        
        
        ImageView detailImage = (ImageView) findViewById(R.id.detailImage);
        if(document != null && document.medium != null){
        	ByteArrayInputStream is = new ByteArrayInputStream(document.medium);
        	Drawable drw = Drawable.createFromStream(is, "mediumImage");
        	detailImage.setImageDrawable(drw);
        }
		
	}




}
