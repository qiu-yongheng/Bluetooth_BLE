package com.kc.blelib.scan;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.kc.blelib.BleManager;


/**
 * 蓝牙设备扫描回调的基类
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public abstract class BaseScanCallback implements BluetoothAdapter.LeScanCallback {

    protected Handler handler = new Handler(Looper.getMainLooper());
    /**
     * 扫描时间
     */
    protected long timeoutMillis = 10000;

    /**
     * 初始化时, 设置扫描时间
     * @param timeoutMillis
     */
    public BaseScanCallback(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * 扫描结束后的操作, 给子类实现
     */
    public abstract void onScanTimeout();

    /**
     * 在扫描时间后, 停止扫描
     */
    public void stopScanOnTimeout(final BleManager bleManager) {
        if (timeoutMillis > 0) {
            removeHandlerMsg();
            //使用handler进行定时操作
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bleManager.stopScan(BaseScanCallback.this);
                    onScanTimeout();
                }
            }, timeoutMillis);
        }
    }

    /**
     * 如果参数为null的话，会将所有的Callbacks和Messages全部清除掉
     * 在Activity退出时, 防止内存泄露
     */
    public void removeHandlerMsg() {
        handler.removeCallbacksAndMessages(null);
    }
}
