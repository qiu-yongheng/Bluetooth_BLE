# Bluetooth_BLE
--------------------
# 一个蓝牙BLE的操作库

### 1. import Module -> blelib包到项目中
![](https://github.com/qiu-yongheng/Bluetooth_BLE/blob/master/Image.png)

### 2. 在build.gradle中添加
```Java
dependencies {
    compile project(':blelib')
}
```

## 使用
-----------------
### 初始化BLE管理类
```Java
bleManager = BleManager.getInstance(this);
```

### 扫描设备
```Java
bleManager.scanDevice(new BleScanCallback(设置扫描时间) {
    @Override
    public void onDeviceFound(List<BluetoothDevice> deviceList) {
          //返回搜索到的设备
    }
});
```

### 连接设备
```Java
bleManager.connectDevice(mBlueDevices.get(0), false, new BleGattCallback(handler) {
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        //向蓝牙设备写入描述符, 请求获取数据
        bleManager.writeDevice(gatt, UUIDCons.s1, UUIDCons.c1, UUIDCons.d1, null);
        //读取设备返回的数据, 数据通过handler返回
        bleManager.readDevice(gatt, UUIDCons.s1, UUIDCons.c1);
    }

    @Override
    public void onConnectSuccess(BluetoothGatt gatt, int status) {
        //搜索service, 成功后回调onServicesDiscovered
        gatt.discoverServices();
    }

    @Override
    public void onConnectFailure(BluetoothGatt gatt, int status) {

    }
});
```

### 获取返回的数据
```Java
private Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 0x2:
                byte[] datas = (byte[]) msg.obj;
                mTv_hearbeat.setText("当前心率: " + datas[1]);
                break;
        }
    }
};
```
