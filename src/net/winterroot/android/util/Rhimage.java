package net.winterroot.android.util;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class Rhimage {

	//Utility Functions
		//TODO: Move to Utils
		public static File convertImageUriToFile (Uri imageUri, Activity activity)  {
			Cursor cursor = null;
			try {
				String [] proj={MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION};
				cursor = activity.managedQuery( imageUri,
						proj, // Which columns to return
						null,       // WHERE clause; which rows to return (all rows)
						null,       // WHERE clause selection arguments (none)
						null); // Order-by clause (ascending by name)
				int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				int orientation_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
				if (cursor.moveToFirst()) {
					String orientation =  cursor.getString(orientation_ColumnIndex);
					if(orientation == null){
						Log.v("RHIMAGE Orientation", "Null Orientation");
					} else {
						Log.v("RHIMAGE Orientation", orientation);
					}
					return new File(cursor.getString(file_ColumnIndex));
				}
				return null;
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		
		public static Bitmap resizeBitMapImage1(String filePath, int targetWidth,
	            int targetHeight, int orientation) {
			Log.v("RHIMAGE Orientation", String.valueOf(orientation) );

	        Bitmap bitMapImage = null;
	        // First, get the dimensions of the image
	        Options options = new Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeFile(filePath, options);
	        double sampleSize = 0;
	        // Only scale if we need to
	        // (16384 buffer for img processing)
	        
	        //Switched inequality to scale by opposite dimension
	        Boolean scaleByHeight = Math.abs(options.outHeight - targetHeight) <= Math
	                .abs(options.outWidth - targetWidth);

	        
	        // Load, scaling to smallest power of 2 that'll get it <= desired
            // dimensions
	        if (options.outHeight * options.outWidth * 2 >= 1638) {
	            sampleSize = scaleByHeight ? options.outHeight / targetHeight
	                    : options.outWidth / targetWidth;
	            sampleSize = (int) Math.pow(2d,
	                    Math.floor(Math.log(sampleSize) / Math.log(2d)));
	        }

	        // Do the actual decoding
	        options.inJustDecodeBounds = false;
	        options.inTempStorage = new byte[128];
	        while (true) {
	            try {
	                options.inSampleSize = (int) sampleSize;
	                bitMapImage = BitmapFactory.decodeFile(filePath, options);
	                if(orientation > 0){
	                	 Matrix matrix = new Matrix();
	                     matrix.postRotate(orientation);

	                     bitMapImage = Bitmap.createBitmap(bitMapImage, 0, 0, bitMapImage.getWidth(),
	                    		 bitMapImage.getHeight(), matrix, true);	                   
	                }

	                break;
	            } catch (Exception ex) {
	                try {
	                    sampleSize = sampleSize * 2;
	                } catch (Exception ex1) {

	                }
	            }
	        }

	        //and crop
            int originx = 0;
            int originy = 0;
            int width = bitMapImage.getWidth() ;
            int height = bitMapImage.getHeight() ; 
            int ratio = targetWidth / targetHeight;
            int reverseRatio = targetHeight / targetWidth;
            //use smaller dimension
            if(width > height){
            	targetHeight = height;
            	targetWidth = targetHeight * ratio;
            } else {
              	targetWidth = width;
            	targetHeight = targetWidth * reverseRatio;
            }
            
           	originx = (bitMapImage.getWidth() - targetWidth) / 2;
           	originy = (bitMapImage.getHeight() - targetHeight) / 2;
            bitMapImage = Bitmap.createBitmap(bitMapImage, originx, originy, targetWidth, targetHeight);

	        
	        return bitMapImage;
	    }

		public static int getOrientation(Context context, Uri photoUri) {
		    /* it's on the external media. */
		    Cursor cursor = context.getContentResolver().query(photoUri,
		            new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

		    if (cursor.getCount() != 1) {
		        return -1;
		    }

		    cursor.moveToFirst();
		    return cursor.getInt(0);
		}

	
}
