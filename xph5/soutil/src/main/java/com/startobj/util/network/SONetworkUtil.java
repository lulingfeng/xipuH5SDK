package com.startobj.util.network;

import com.startobj.util.common.SOCommonUtil;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class SONetworkUtil {
	/**
	 * 检测网络连接是否正常
	 *
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		if (SOCommonUtil.hasContext(context)) {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (cm == null)
				return false;
			else {
				NetworkInfo[] info = cm.getAllNetworkInfo();
				if (info != null)
					for (int i = 0; i < info.length; i++)
						if (isStateConnected(info, i) || isStateConnecting(info, i))
							return true;
			}
		}
		return false;
	}

	private static boolean isStateConnected(NetworkInfo[] info, int i) {
		return info[i].getState() == NetworkInfo.State.CONNECTED;
	}

	private static boolean isStateConnecting(NetworkInfo[] info, int i) {
		return info[i].getState() == NetworkInfo.State.CONNECTING;
	}
}
