package net.winterroot.android.touchdb.provider;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

public class BlobCursor implements Cursor {

	byte[] blob;
	
	public void setBlob(byte[] aBlob){
		blob = aBlob;
	}
	
	
/*
	@Override
	public byte[] getBlob(int column) {
		Object value = super.getBlob(column);
		return (byte[]) value;
	}

	public void fillWindow(int position, CursorWindow window) {
		if (position < 0 || position >= getCount()) {
			return;
		}
		window.acquireReference();
		try {
			int oldpos = mPos;
			mPos = position - 1;
			window.clear();
			window.setStartPosition(position);
			int columnNum = getColumnCount();
			window.setNumColumns(columnNum);
			while (moveToNext() && window.allocRow()) {            
				for (int i = 0; i < columnNum; i++) {
					byte [] field = getBlob(i);
					if (field != null) {
						if (!window.putBlob(field, mPos, i)) {
							window.freeLastRow();
							break;
						}
					} else {
						if (!window.putNull(mPos, i)) {
							window.freeLastRow();
							break;
						}
					}
				}
			}

			mPos = oldpos;
		} catch (IllegalStateException e){
			// simply ignore it
		} finally {
			window.releaseReference();
		}
	}
*/
	
	public void close() {
		// TODO Auto-generated method stub
		
	}

	public void copyStringToBuffer(int arg0, CharArrayBuffer arg1) {
		// TODO Auto-generated method stub
		
	}

	public void deactivate() {
		// TODO Auto-generated method stub
		
	}

	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getColumnIndex(String columnName) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getColumnIndexOrThrow(String columnName)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getColumnName(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getColumnNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getCount() {
		return 1;
	}

	public double getDouble(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Bundle getExtras() {
		// TODO Auto-generated method stub
		return null;
	}

	public float getFloat(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getInt(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getLong(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	public short getShort(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getString(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getType(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean getWantsAllOnMoveCalls() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isAfterLast() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isBeforeFirst() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isFirst() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isLast() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isNull(int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean move(int offset) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean moveToFirst() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean moveToLast() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean moveToNext() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean moveToPosition(int position) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean moveToPrevious() {
		// TODO Auto-generated method stub
		return false;
	}

	public void registerContentObserver(ContentObserver observer) {
		// TODO Auto-generated method stub
		
	}

	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	public boolean requery() {
		// TODO Auto-generated method stub
		return false;
	}

	public Bundle respond(Bundle extras) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setNotificationUri(ContentResolver cr, Uri uri) {
		// TODO Auto-generated method stub
		
	}

	public void unregisterContentObserver(ContentObserver observer) {
		// TODO Auto-generated method stub
		
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	public byte[] getBlob(int columnIndex) {
		return blob;
	}
}
