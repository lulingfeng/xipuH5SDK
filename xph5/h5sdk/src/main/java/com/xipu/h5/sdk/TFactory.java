package com.xipu.h5.sdk;

public class TFactory {

	public static Object getClass(String clazz) {
		Object obj = null;
		try {
			obj = Class.forName(clazz).newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return obj;
	}
}
