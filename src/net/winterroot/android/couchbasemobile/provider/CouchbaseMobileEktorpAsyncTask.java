package net.winterroot.android.couchbasemobile.provider;

import org.ektorp.DbAccessException;
import org.ektorp.android.util.EktorpAsyncTask;

import android.util.Log;

public abstract class CouchbaseMobileEktorpAsyncTask extends EktorpAsyncTask {

	@Override
	protected void onDbAccessException(DbAccessException dbAccessException) {
		Log.e("Ektorp async task", "DbAccessException in background", dbAccessException);
	}

}
