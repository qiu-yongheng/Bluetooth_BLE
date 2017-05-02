package com.kc.blelib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.kc.blelib.conn.BleDataResultCallback;
import com.kc.blelib.conn.BleGattCallback;
import com.kc.blelib.constant.Constant;
import com.kc.blelib.constant.UUIDCons;
import com.kc.blelib.scan.BleScanCallback;

import java.lang.reflect.Method;
import java.util.UUID;

import static com.kc.blelib.constant.Constant.STATE_CONNECTED;
import static com.kc.blelib.constant.Constant.STATE_CONNECTING;
import static com.kc.blelib.constant.Constant.STATE_DISCONNECTED;
import static com.kc.blelib.constant.Constant.STATE_SCANNING;
import static com.kc.blelib.constant.Constant.STATE_SERVICES_DISCOVERED;

/**
 * @author qiuyongheng
 * @time 2017/2/17  9:55
 * @desc 蓝牙BLE操作管理类
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleManager {
    private static final String TAG = "-----BleManager-----";
    public static BleManager mBle = null;
    private Context mContext;
    private static BleDataResultCallback resultCallback;
    /**
     * 连接状态
     */
    public static int connectionState = 0;

    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.RESULT_DATA:
                    if (resultCallback != null) {
                        byte[] data = (byte[]) msg.obj;
                        resultCallback.onDataResult(data);
                    }
                    break;
            }
        }
    };
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt mGatt;

    private BleManager(Context context) {
        init(context);
    }

    /**
     * 获取返回的数据
     *
     * @param resultCallback
     */
    public void getDatas(BleDataResultCallback resultCallback) {
        this.resultCallback = resultCallback;

    }

    /**
     * 获取对象
     *
     * @return
     */
    public static BleManager getInstance(Context context) {

        if (mBle == null) {
            synchronized (BleManager.class) {
                if (mBle == null) {
                    mBle = new BleManager(context);
                }
            }
        }
        return mBle;
    }

    /**
     * 1. 初始化蓝牙操作类
     * 2. 判断设备是否支持BLE
     * 3. 判断蓝牙是否打开, 没有打开就打开
     */
    public void init(Context context) {
        mContext = context.getApplicationContext();
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        isSupportBle();
        openBluetooth((Activity) context, Constant.REQUEST_CODE);
    }

    /**
     * 扫描蓝牙设备
     */
    public boolean scanDevice(BleScanCallback callback) {
        callback.stopScanOnTimeout(this); //指定时间后停止扫描
        connectionState = STATE_SCANNING;
        return bluetoothAdapter.startLeScan(callback);
    }

    /**
     * 停止扫描蓝牙设备
     *
     * @param callback
     */
    public void stopScan(BluetoothAdapter.LeScanCallback callback) {
        bluetoothAdapter.stopLeScan(callback);
        if (connectionState == STATE_SCANNING) {
            connectionState = STATE_DISCONNECTED;
        }
    }

    /**
     * 连接蓝牙设备(根据设备)
     */
    public BluetoothGatt connectDevice(BluetoothDevice device, boolean autoConnect, BleGattCallback callback) {
        mGatt = device.connectGatt(mContext, autoConnect, callback);
        return mGatt;
    }

    /**
     * 连接蓝牙设备(根据IMEI)
     */
    public BluetoothGatt connectDevice(String address, BleGattCallback callback) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        mGatt = device.connectGatt(mContext, false, callback);
        return mGatt;
    }

    /**
     * 向设备写描述码, 请求获取数据
     *
     * @param service 服务码
     * @param chara   特征码
     * @return
     */
    public boolean WriteCharX(UUID service, UUID chara, UUID desc) {
        BluetoothGattCharacteristic characteristic = mGatt.getService(service).getCharacteristic(chara);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(desc);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        return mGatt.writeDescriptor(descriptor);
    }

    /**
     * 向设备写描述码, 不能获取数据(应该在descriptor中写)
     *
     * @param GattCharacteristic
     * @param writeValue
     * @return
     */
    public boolean WriteCharX(BluetoothGattCharacteristic GattCharacteristic, byte[] writeValue) {
        if (GattCharacteristic != null) {
            GattCharacteristic.setValue(writeValue);
            return mGatt.writeCharacteristic(GattCharacteristic);
        }
        return false;
    }

    /**
     * 读取特征码写入的值
     *
     * @param GattCharacteristic
     */
    public boolean ReadCharX(BluetoothGattCharacteristic GattCharacteristic) {
        if (GattCharacteristic != null) {
            return mGatt.readCharacteristic(GattCharacteristic);
        }
        return false;
    }

    /**
     * 刷新返回的数据
     *
     * @param service
     * @param chara
     * @return
     */
    public void setCharacteristicNotification(UUID service, UUID chara, UUID desc, boolean enable) {
        BluetoothGattCharacteristic characteristic = mGatt.getService(service).getCharacteristic(chara);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(desc);
        if (enable) {
            mGatt.setCharacteristicNotification(characteristic, true);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mGatt.writeDescriptor(descriptor);
        } else {
            mGatt.setCharacteristicNotification(characteristic, false);
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            mGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * 刷新返回的数据
     *
     * @return
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUIDCons.d);
        if (enable) {
            Log.i(TAG, "Enable Notification");
            mGatt.setCharacteristicNotification(characteristic, true);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mGatt.writeDescriptor(descriptor);
        } else {
            Log.i(TAG, "Disable Notification");
            mGatt.setCharacteristicNotification(characteristic, false);
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            mGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * 刷新蓝牙设备缓存
     */
    public void refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null) {
                final boolean success = (Boolean) refresh.invoke(mGatt);
                Log.i(TAG, "Refreshing result: " + success);
            }
        } catch (Exception e) {
            Log.e(TAG, "An exception occured while refreshing device", e);
        }
    }

    /**
     * 断开设备连接
     */
    public void closeBluetoothGatt() {
        if (mGatt != null) {
            mGatt.disconnect();
            refreshDeviceCache();
            mGatt.close();
        }
        connectionState = STATE_DISCONNECTED;
    }

    /**
     * 当前设备是否支持BLE
     */
    public boolean isSupportBle() {
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mContext, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Toast.makeText(mContext, R.string.ble_supported, Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    /**
     * 开启蓝牙
     *
     * @param context
     */
    public void openBluetooth(Activity context, int requestCode) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, requestCode);
        }
    }

    /**
     * 关闭蓝牙
     */
    public void disableBluetooth() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.disable();
        }
    }

    /**
     * 本机蓝牙是否打开
     */
    public boolean isBlueEnable() {
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.isEnabled();
        }
        return false;
    }

    /**
     * 是否在扫描状态
     */
    public boolean isInScanning() {
        return connectionState == STATE_SCANNING;
    }

    /**
     * 是否在连接或已连接状态
     */
    public boolean isConnectingOrConnected() {
        return connectionState >= STATE_CONNECTING;
    }

    /**
     * 是否已连接
     */
    public boolean isConnected() {
        return connectionState >= STATE_CONNECTED;
    }

    /**
     * 服务是否已发现
     */
    public boolean isServiceDiscovered() {
        return connectionState == STATE_SERVICES_DISCOVERED;
    }
}
