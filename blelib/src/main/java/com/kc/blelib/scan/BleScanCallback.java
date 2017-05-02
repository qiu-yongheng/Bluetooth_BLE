package com.kc.blelib.scan;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qiuyongheng
 * @time 2017/2/17  11:18
 * @desc 扫描设备的回调, 在指定时间后, 回调搜索到的设备给调用者
 */

public abstract class BleScanCallback extends BaseScanCallback {
    /**
     * 所有被发现的设备集合
     */
    private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    /**
     * 初始化时, 设置扫描时间
     *
     * @param timeoutMillis
     */
    public BleScanCallback(long timeoutMillis) {
        super(timeoutMillis);
    }

    /**
     * 扫描到设备的回调
     * @param bluetoothDevice
     * @param rssi
     * @param bytes
     */
    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
        if (bluetoothDevice == null) {
            return;
        }

        if (!deviceList.contains(bluetoothDevice)) {
            deviceList.add(bluetoothDevice);
            onDeviceFound(deviceList);
            onDeviceFound(bluetoothDevice, rssi, bytes);
        }
    }

    /**
     * 扫描结束后的操作
     */
    @Override
    public abstract void onScanTimeout();

    /**
     * 回调找到的蓝牙设备
     * @param deviceList
     */
    public abstract void onDeviceFound(List<BluetoothDevice> deviceList);

    /**
     * 回调找到的蓝牙设备
     * @param bluetoothDevice
     */
    public abstract void onDeviceFound(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes);

}
