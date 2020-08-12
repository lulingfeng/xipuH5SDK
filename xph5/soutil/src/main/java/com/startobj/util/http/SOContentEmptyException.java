package com.startobj.util.http;

/**
 * @Explain
 * @Version 1.0
 * @CreateDate 2016/01/20 2:38 PM
 * @Author Eagle Email:lizhengpei@gmail.com
 */
public class SOContentEmptyException extends Exception {
	public SOContentEmptyException(String detailMessage) {
		super(detailMessage + ",内容为空！");
	}
}
