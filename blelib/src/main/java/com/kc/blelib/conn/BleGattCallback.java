package com.kc.blelib.conn;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.kc.blelib.BleManager;
import com.kc.blelib.constant.Constant;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

/**
 * @author qiuyongheng
 * @time 2017/2/17  11:42
 * @desc 连接蓝牙, 读取数据, 写入数据的回调
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public abstract class BleGattCallback extends BluetoothGattCallback {

    public final Handler handler;
    private final String ACTION_NAME_RSSI = "AMOMCU_RSSI"; // 其他文件广播的定义必须一致
    private final String ACTION_CONNECT = "AMOMCU_CONNECT";    // 其他文件广播的定义必须一致
    private final Context context;

    public BleGattCallback(Context context) {
        this.context = context;
        this.handler = BleManager.handler;
    }

    /**
     * 连接状态改变时回调
     *
     * @param gatt
     * @param status
     * @param newState
     */
    @Override
    public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.i(TAG, "连接成功 onConnectionSuccess");
            BleManager.connectionState = Constant.STATE_CONNECTED;
            /** 第一步: 开始搜索service */
            gatt.discoverServices();

            // 增加读rssi 的定时器
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (gatt != null) {
                        gatt.readRemoteRssi();
                    }
                }
            };
            Timer rssiTimer = new Timer();
            rssiTimer.schedule(task, 160, 160);

            // 发送广播
            Intent mIntent = new Intent(ACTION_CONNECT);
            mIntent.putExtra("CONNECT_STATUC", 1);
            context.sendBroadcast(mIntent);
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i(TAG, "连接失败 onConnectionFault");
            BleManager.connectionState = Constant.STATE_DISCONNECTED;
            onConnectFailure(gatt, status);

            // 发送广播
            Intent mIntent = new Intent(ACTION_CONNECT);
            mIntent.putExtra("CONNECT_STATUC", 0);
            context.sendBroadcast(mIntent);
        }
    }


    /**
     * 相当于一个监听器, 当蓝牙设备有数据返回时执行
     *
     * @param gatt
     * @param characteristic
     */
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.e(TAG, "onCharacteristicChanged 返回数据: " + Arrays.toString(characteristic.getValue()));
        Message message = new Message();
        message.what = Constant.RESULT_DATA;
        message.obj = characteristic.getValue();
        handler.sendMessage(message);
        //回调
        onCharacteristicRefresh(gatt, characteristic);
    }

    /**
     * 服务被发现
     * 可以给特征符写入数据, 发送给BLE终端
     * @param gatt
     * @param status
     */
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            BleManager.connectionState = Constant.STATE_SERVICES_DISCOVERED;
            onServiceConnectSuccess(gatt, status);
        } else {
            Log.w(TAG, "onServicesDiscovered received: " + status);
        }
    }

    /**
     * BLE终端数据被读的回调
     *
     * @param gatt
     * @param characteristic
     * @param status
     */
    @Override
    public abstract void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

    /**
     * BLE终端数据返回时的回调
     *
     * @param gatt
     * @param characteristic
     */
    public abstract void onCharacteristicRefresh(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

    /**
     * 获取RSSI值, 计算距离
     *
     * @param gatt
     * @param rssi
     * @param status
     */
    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        // 发送广播
        Intent mIntent = new Intent(ACTION_NAME_RSSI);
        mIntent.putExtra("RSSI", rssi);
        context.sendBroadcast(mIntent);
    }


    /**
     * 连接成功, 获取数据
     *
     * @param gatt
     * @param status
     */
    public abstract void onServiceConnectSuccess(BluetoothGatt gatt, int status);

    /**
     * 连接失败
     *
     * @param gatt
     * @param status
     */
    public abstract void onConnectFailure(BluetoothGatt gatt, int status);

}
