package net.winterroot.android.util;

import java.util.UUID;

import android.content.Context;
import android.telephony.TelephonyManager;

public class RhusDevice {

	private String deviceId = null;
	private TelephonyManager tm;

	public RhusDevice(Context context) {
		tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String tmDevice, tmSerial, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

		UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
		deviceId = "ANDROID"+deviceUuid.toString();
		tm = null;
		
	}

	public String getDeviceId(){
		return deviceId;
	}
}
