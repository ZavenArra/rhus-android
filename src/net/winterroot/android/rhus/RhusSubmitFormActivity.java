package net.winterroot.android.rhus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import net.winterroot.android.rhus.provider.RhusDocument;
import net.winterroot.android.wildflowers.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class RhusSubmitFormActivity extends Activity {

	private final String TAG = "RhusSubmitFormActivity";
	
    
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		Log.v(TAG, "onCreate");	
		
		setContentView(R.layout.submitform);
     
		//set up onClick events
		//Read data from the file
		//See if it can be read into a standard object
		ObjectMapper mapper = new ObjectMapper(); 
		RhusSettings settings = null;
		try {
			 settings = mapper.readValue(new File("settings/submitform.json"), RhusSettings.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	/*	
		for( ArrayList<String> options : settings.exclusiveOptions ){
			int resID = getResources().getIdentifier(options[0], "id", net.winterroot.android.rhus);
			((ImageButton) findViewById(resID)).setOnClickListener(
					new OnClickListener(){
						public void onClick(View arg0) {
							
						}
					}
					);
		}
	*/
		
	}

	@Override
	public void onStart(){
		
	}
	
	
	
}
