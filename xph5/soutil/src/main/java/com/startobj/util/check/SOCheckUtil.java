package com.startobj.util.check;

import android.text.TextUtils;

public class SOCheckUtil {
	/**
	 * 验证手机格式
	 */
	public static boolean isMobileNO(String mobile) {
		return TextUtils.isEmpty(mobile) ? false : mobile.matches("1\\d{10}");
	}

	/**
	 * 验证账号名 ^[a-zA-Z]\w{3,17}$ 用户名以字母开头，由4-18个字母、数字或下划线所组成
	 */
	public static boolean checkUsername(String username) {
		return TextUtils.isEmpty(username) ? false : username.matches("^[a-zA-Z]\\w{3,17}$");
	}

	/**
	 * 验证密码 ^\S{6,18}$ 密码长度为6-18个字符，不能包含空格
	 */
	public static boolean checkPassword(String password) {
		return TextUtils.isEmpty(password) ? false : password.matches("^\\S{6,18}$");
	}
}
