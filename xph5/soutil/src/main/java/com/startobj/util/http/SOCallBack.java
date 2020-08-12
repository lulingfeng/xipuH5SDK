package com.startobj.util.http;

/**
 * Http回调接口
 * 
 * @Explain
 * @Version 1.0
 * @CreateDate 2016-08-16 22:09:54
 * @Author Eagle Email:lizhengpei@gmail.com
 */
public interface SOCallBack {
	public interface SOCommonCallBack<ResultType> extends SOCallBack {

		void onSuccess(ResultType result);

		void onHttpError(Throwable ex, boolean isOnCallback);

		void onCodeError(CodeErrorException cex);

		void onFinished();
	}

	public static class CodeErrorException extends RuntimeException {
		public CodeErrorException(String detailMessage) {
			super(detailMessage);
		}
	}
}
