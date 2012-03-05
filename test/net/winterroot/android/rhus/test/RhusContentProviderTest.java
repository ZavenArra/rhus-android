package net.winterroot.android.rhus.test;

import net.winterroot.android.rhus.provider.RhusDocument;
import net.winterroot.android.rhus.provider.RhusDocumentContentProvider;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import android.database.Cursor;
import android.app.Activity;
import android.content.Context;

import net.winterroot.net.android.wildflowers.WildflowersActivity;



@RunWith(RobolectricTestRunner.class)

public class RhusContentProviderTest {
    @Test
    public void shouldBeTrue() throws Exception {
    	assertThat(true, equalTo(true));
    }
    
    @Test
    public void shouldHaveContentURI() throws Exception {
    	assertNotNull(RhusDocument.CONTENT_URI);
    	assertThat(RhusDocument.CONTENT_URI.toString(), equalTo("content://net.winterroot.android.rhus.provider.RhusDocument/documents"));
   
    }
    
    @Test
    public void shouldLaunchContentProvider() throws Exception {
    	//RhusDocumentContentProvider rdcp = new RhusDocumentContentProvider();
    	WildflowersActivity activity = new WildflowersActivity();
    	assertNotNull(activity.getApplicationContext());
    	
    	System.out.println("In content provider test 1");
    	System.out.println(RhusDocument.CONTENT_URI);
    	Cursor documentsCursor =  activity.managedQuery(RhusDocument.CONTENT_URI, null,
    	                null, null, null);
        assertNotNull(documentsCursor);
    	
    	//context baseContext = activity.getBaseContext();
    	
    	//RhusDocumentContentProvider rdcp = new RhusDocumentContentProvider(baseContext);
    	//boolean created = rdcp.onCreate();
    	//assertThat(created, equalTo(true));
    }
}

