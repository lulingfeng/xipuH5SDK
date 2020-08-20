package com.startobj.util.string;

import java.security.MessageDigest;
import java.util.regex.Pattern;

import android.text.TextUtils;

public class SOStringUtil {
	/**
	 * 过滤特殊字符 [\\/:*?<> |\"\n\t]
	 *
	 * @param str
	 * @return
	 */
	public static String stringFilter(String str) {
		return TextUtils.isEmpty(str) ? "" : Pattern.compile(" |\"\n\t]").matcher(str).replaceAll("");
	}

	/**
	 * MD5加密
	 *
	 * @param plainText
	 * @return
	 */
	public static String Md5(String plainText) {
		if (TextUtils.isEmpty(plainText))
			return "";
		StringBuffer buf = new StringBuffer("");
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();
			int i;
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			return buf.toString();// 32位的加密
			// buf.toString().substring(8, 24);//16位的加密
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 生成随机账号 {账号,密码} 账号 : beginStr + 1位26随机字母 + 密码的3、4两位 + 时间戳的后8八位 密码 : 随机6位
	 */
	public static String[] creatRandomAccount(String beginStr) {
		// 密码
		StringBuilder passwrodBuilder = new StringBuilder(String.valueOf(Math.random()));
		String passwrod = passwrodBuilder.reverse().toString().substring(0, 6);
		// 账号
		StringBuilder accountBuilder = new StringBuilder(beginStr);
		String chars = "abcdefghijklmnopqrstuvwxyz";
		accountBuilder.append(chars.charAt((int) (Math.random() * 26)));
		String ranNum = passwrod.substring(2, 4) + String.valueOf(System.currentTimeMillis()).substring(4);
		accountBuilder.append(ranNum);
		passwrod = generate();
		return new String[] { accountBuilder.toString(), passwrod};
	}

	public static String generate() {

		String[] letters = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
				"s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		StringBuffer buffers = new StringBuffer();
		for (int i = 0; i < 6; i++) {
			buffers.append(letters[(int) (Math.random() * (letters.length))]);
		}
		return buffers.toString();
	}

	// 根据Unicode编码完美的判断中文汉字和符号
	private static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}

	// 完整的判断中文汉字和符号
	public static boolean isChinese(String strName) {
		char[] ch = strName.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				return true;
			}
		}
		return false;
	}

	// 完整的判断中文汉字和符号
	public static int getChineseCount(String strName) {
		int chineseCount = 0;
		char[] ch = strName.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				chineseCount++;
			}
		}
		return chineseCount;
	}

	// 只能判断部分CJK字符（CJK统一汉字）
	public static boolean isChineseByREG(String str) {
		if (str == null) {
			return false;
		}
		Pattern pattern = Pattern.compile("[\\u4E00-\\u9FBF]+");
		return pattern.matcher(str.trim()).find();
	}

	// 只能判断部分CJK字符（CJK统一汉字）
	public static boolean isChineseByName(String str) {
		if (str == null) {
			return false;
		}
		// 大小写不同：\\p 表示包含，\\P 表示不包含
		// \\p{Cn} 的意思为 Unicode 中未被定义字符的编码，\\P{Cn} 就表示 Unicode中已经被定义字符的编码
		String reg = "\\p{InCJK Unified Ideographs}&&\\P{Cn}";
		Pattern pattern = Pattern.compile(reg);
		return pattern.matcher(str.trim()).find();
	}
}
