package com.kc.blelib.constant;

/**
 * @author qiuyongheng
 * @time 2017/2/17  11:57
 * @desc ${TODD}
 */

public class Constant {
    public static final int RESULT_DATA = 0x111111;
    public static final int REQUEST_CODE = 0x222222;

    public static final String TAG = "==========";


    /**
     * 没有连接
     */
    public static final int STATE_DISCONNECTED = 0;
    /**
     * 正在搜索
     */
    public static final int STATE_SCANNING = 1;
    /**
     * 正在连接
     */
    public static final int STATE_CONNECTING = 2;
    /**
     * 已连接
     */
    public static final int STATE_CONNECTED = 3;
    /**
     * 服务被发现
     */
    public static final int STATE_SERVICES_DISCOVERED = 4;
}
