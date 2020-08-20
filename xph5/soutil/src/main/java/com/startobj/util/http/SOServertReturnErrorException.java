package com.startobj.util.http;

/**
 * 服务器返回数据异常
 *
 * @Explain
 * @Version 1.0
 * @CreateDate 2016/01/20 2:45 PM
 * @Author Eagle Email:lizhengpei@gmail.com
 */
public class SOServertReturnErrorException extends Exception {
	public SOServertReturnErrorException(String detailMessage) {
		super(detailMessage);
	}
}
