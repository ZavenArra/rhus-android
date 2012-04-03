package net.winterroot.android.util;

import java.io.File;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.provider.MediaStore;

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
	            int targetHeight) {
	        Bitmap bitMapImage = null;
	        // First, get the dimensions of the image
	        Options options = new Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeFile(filePath, options);
	        double sampleSize = 0;
	        // Only scale if we need to
	        // (16384 buffer for img processing)
	        Boolean scaleByHeight = Math.abs(options.outHeight - targetHeight) >= Math
	                .abs(options.outWidth - targetWidth);

	        if (options.outHeight * options.outWidth * 2 >= 1638) {
	            // Load, scaling to smallest power of 2 that'll get it <= desired
	            // dimensions
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

	                break;
	            } catch (Exception ex) {
	                try {
	                    sampleSize = sampleSize * 2;
	                } catch (Exception ex1) {

	                }
	            }
	        }

	        return bitMapImage;
	    }

	
}
