package net.winterroot.android.rhus;

import java.util.ArrayList;

import net.winterroot.android.rhus.provider.RhusDocument;
import net.winterroot.android.rhus.provider.RhusProject;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ProjectsListActivity extends Activity {
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
        setContentView(R.layout.projects_list);
        
		ContentValues contentValues = new ContentValues();
		contentValues.put("project", "testing!!!");
        getContentResolver().insert(RhusProject.PROJECTS_URI , contentValues);
        
    	( (Button) findViewById(R.id.projectsListBackButton)).setOnClickListener(
				new OnClickListener(){
					public void onClick(View arg0) {
						finish();
					}
				}
		);
 	
     	( (Button) findViewById(R.id.projectsListAddProjectButton)).setOnClickListener(
				new OnClickListener(){
					public void onClick(View arg0) {
						
					}
				}
		);
        
        ListView listView = (ListView) findViewById(R.id.projectsListView);
		
        Uri projectsUri = RhusProject.PROJECTS_URI.buildUpon().appendQueryParameter("project", RhusState.project).build();
		Cursor projectsCursor  = managedQuery(projectsUri, null, null, null, null);
        
		ArrayList<String> values = new ArrayList<String>();
		
		if(projectsCursor.getCount() > 0){
			projectsCursor.moveToFirst();

			do {
				String project = projectsCursor.getString(0);
				values.add(project); //add the keys to the values array
			} while (projectsCursor.moveToNext());

		}
		/*
        String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
        	"Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
        	"Linux", "OS/2" };
        */
        
		String[] valuesArray = values.toArray(new String[values.size()]); 
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        		android.R.layout.simple_list_item_1, android.R.id.text1, valuesArray);

        // Assign adapter to ListView
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position,
					long id) {
				//TODO: Include a CompountButton to show selection state
				//view.setSelected(true);
				RhusState.project = (String) adapterView.getAdapter().getItem(position);
				finish();
			}
        });
	}
}
