package com.xipu.h5.sdk.config;

public class H5Config {
    // 服务端版本
    public final static String SDK_VERSION = "v1";
    // 客户端版本
    public final static String SDK_CODE = "8";
    public final static String H5SDK_URL = "";
    // 第一次使用标示位
    public final static String SP_ISFIRST = "h5_isfirst";
    public final static String SP_TUIAID = "h5_tuiaid";
    public final static String SP_ISACTIVATE = "h5_activate";
    public final static String SP_PAYSUCCESS_DATA = "h5_paysuccess_data";

   // public final static String API_URL = "http://testapi.kuaigames.com/"+SDK_VERSION+"/";
    public final static String API_URL = "http://api.kuaigames.com/" + SDK_VERSION + "/";

    //  public static String GAME_BASE_URL = "http://testh5.xipu.com";
    public static String GAME_BASE_URL = "https://h5.zhangyueyx.com";

    public static String GAME_URL = GAME_BASE_URL + "play.php";
    // 获取配置信息
    public final static String APPCONFIG_URL = API_URL + "app/config";
    // 三方登录
    public final static String THIRD_LOGIN_URL = API_URL + "third/login";
    // 查询渠道
    public final static String QUERY_CHANNEL = API_URL + "device/query_channel";
    // 生成随机账号
    public static final String CREATE_RANDOM_ACCOUNT = API_URL + "user/anonymous_account";
    // 注册账号
    public static final String REGISTERACCOUNT_URL = API_URL + "user/register";
    // 匿名账号绑定三方账户
    public static final String GUESTBINDTHIRDACCOUNT_URL = API_URL + "third/bind";
    // 登录
    public static final String LOGIN_URL = API_URL + "user/login";
    // 三方激活
    public final static String THIRD_ACTIVATE_URL = API_URL + "third/activate";
    // 激活
    public final static String ACTIVATE_URL = API_URL + "device/activate";
    // 查询订单
    public final static String QUERY_ORDER_URL = API_URL + "h5pay/query_order";
    // 三方支付
    public final static String THIRD_PAY_URL = API_URL + "third/pay";

    // 协议头
    public final static String AGREEMENT_SCHEME = "kwsdk";
    // 协议支付成功
    public final static String AGREEMENT_CLOSE_WINDOW = "close_window";
    // 协议刷新
    public final static String AGREEMENT_REFRESH_WINDOW = "refresh_window";
    // 协议支付成功
    public final static String AGREEMENT_PAY_SUCCESS = "close_window_pay_success";
    // 协议支付取消
    public final static String AGREEMENT_PAY_CANCEL = "close_window_pay_cancel";
    // 协议支付失败
    public final static String AGREEMENT_PAY_FAILED = "close_window_pay_failed";
    // 协议支付结果确认中
    public final static String AGREEMENT_PAY_CONFIRM = "close_window_pay_confirm";
    // 协议支付方式-支付宝
    public final static String AGREEMENT_PAY_WAY_ALIPAY = "alipay";
    // 协议支付方式-支付宝
    public final static String AGREEMENT_PAY_WAY_WECHAT = "wxpay";
    // 支付相关
    // 00 正式 01 测试
    public static final String YLMODE = "00";
    // 支付宝
    public static final int SDK_PAY_FLAG = 1;
    // 返回标志-微信支付
    public final static int WX_PAY = 90094001;
    // 自定义SDK卡文件夹保存路径
    public final static String SDK_FOLDER_LOCATION = "sysgem";
    // 设备kid保存文件名
    public final static String SDK_ID_FILENAME = "android_id";

    public final static String THIRD_API_TUIA_URL = "https://activity.tuia.cn/log/effect/v2";
    // 上报接口地址
    public final static String SERVER_REPORT_URL = "http://rd.kuaigames.com/";
    // 今日头条注册上报
    public final static String REPORT_JRTT_REGISTER_URL = SERVER_REPORT_URL + "jrtt/jrtt_register_log.php";
    // 今日头条付费上报
    public final static String REPORT_JRTT_PAY_URL = SERVER_REPORT_URL + "jrtt/jrtt_trade_log.php";
    // 广点通注册上报
    public final static String REPORT_GDT_REGISTER_URL = SERVER_REPORT_URL + "gdt/gdt_register_log.php";
    public static String SDK_SP_NAME = "h5_info";
    public static String SP_CHANNEL = "h5_channel";
    public static String SP_DEVICE_ID = "hs_device_id";

    public static final int TYPE_H5SDK = 3000;
    public static final int TYPE_OSDK = 3302;
    private static final String SDK = "com.xipu.h5";
    public static final String H5SDK = SDK + ".h5sdk.H5SDK";
    public static final String OSDK = SDK + ".osdk.OSDK";
}