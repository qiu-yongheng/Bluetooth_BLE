package com.kc.bluetooth_ble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kc.blelib.BleManager;
import com.kc.blelib.conn.BleGattCallback;
import com.kc.blelib.scan.BleScanCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.kc.bluetooth_ble.R.id.tv_break;
import static com.kc.bluetooth_ble.R.id.tv_heartbeat;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "==========";
    private Button mBtn_scan;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 5000;
    private List<BluetoothDevice> mBlueDevices = new ArrayList<>();
    private TextView mTv;
    private Button mBtn_conn;
    private List<byte[]> datas = new ArrayList<>();
    private Button mBtn_excel;
    private File file;
    private Button mBtn_clean;
    private TextView mTv_hearbeat;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x2:
                    byte[] data = (byte[]) msg.obj;
                    datas.add(data);
                    int i = data[1];
                    if (i == -1) {
                        mTv_hearbeat.setText("当前心率: " + "请稍等");
                    } else {
                        mTv_hearbeat.setText("当前心率: " + data[1]);
                    }
                    break;
            }
        }
    };
    private BleManager bleManager;
    private Pulse_Sensor pulse_sensor;
    private TextView mTv_break;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**判断手机系统的版本*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                String[] permissions = new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions, 0x2);
            }
        }
        initView();
        initListener();
        init();
    }

    private void initListener() {
        mBtn_scan.setOnClickListener(this);
        mBtn_conn.setOnClickListener(this);
        mBtn_excel.setOnClickListener(this);
        mBtn_clean.setOnClickListener(this);
    }

    private void initView() {
        mBtn_scan = (Button) findViewById(R.id.btn_scan);
        mBtn_conn = (Button) findViewById(R.id.btn_conn);
        mTv = (TextView) findViewById(R.id.tv);
        mBtn_excel = (Button) findViewById(R.id.btn_excel);
        mBtn_clean = (Button) findViewById(R.id.btn_clean);
        mTv_hearbeat = (TextView) findViewById(tv_heartbeat);
        mTv_break = (TextView) findViewById(tv_break);
    }

    private void init() {
        bleManager = BleManager.getInstance(this);
        pulse_sensor = new Pulse_Sensor();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                Toast.makeText(this, "开始扫描, 5秒后返回结果", Toast.LENGTH_SHORT).show();
                bleManager.scanDevice(new BleScanCallback(SCAN_PERIOD) {
                    @Override
                    public void onScanTimeout() {

                    }

                    @Override
                    public void onDeviceFound(List<BluetoothDevice> deviceList) {
                        Log.d("===", "找到了");
                        if (deviceList != null && deviceList.size() > 0) {
                            mBlueDevices = deviceList;
                            mTv.setText(deviceList.get(0).getName() + ", " + deviceList.get(0).getAddress());
                        } else {
                            mTv.setText("未找到蓝牙心率设备");
                        }
                    }

                    @Override
                    public void onDeviceFound(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
                        Log.d("===", "找到了1");
                    }
                });
                break;
            case R.id.btn_conn:
                if (mBlueDevices.isEmpty()) {
                    Toast.makeText(this, "未连接蓝牙设备", Toast.LENGTH_SHORT).show();
                    return;
                }

                bleManager.connectDevice(mBlueDevices.get(0), false, new BleGattCallback(this) {
                    /**
                     * 蓝牙终端被读时回调
                     * @param gatt
                     * @param characteristic
                     * @param status
                     */
                    @Override
                    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        byte[] value = characteristic.getValue();
                        Log.e("!!!", Arrays.toString(value));
                    }

                    /**
                     * 刷新特征码时, 返回数据
                     * @param gatt
                     * @param characteristic
                     */
                    @Override
                    public void onCharacteristicRefresh(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                        byte[] value = characteristic.getValue();
                        Log.e("===", Arrays.toString(value));
                        datas.add(value);
                        final int bpm = pulse_sensor.pulseSenosrCal(value[1]);
                        Log.e("---------------------", "呼吸频率: " + bpm);
                        if (bpm !=0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTv_hearbeat.setText("当前心率: " + bpm);

                                    mTv_break.setText("呼吸频率: " + bpm/5);
                                }
                            });
                        }
                    }

                    /**
                     * 服务连接成功, 开始对特征码进行获取数据操作
                     * @param gatt
                     * @param status
                     */
                    @Override
                    public void onServiceConnectSuccess(BluetoothGatt gatt, int status) {
                        List<BluetoothGattService> services = gatt.getServices();
                        for (BluetoothGattService service : services) {
                            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                            for (BluetoothGattCharacteristic characteristic : characteristics) {
                                Log.d("==", "uuid = " + characteristic.getUuid());
                                if (characteristic.getUuid().toString().equals("00002a37-0000-1000-8000-00805f9b34fb")) {
                                    //请求获取数据
                                    bleManager.setCharacteristicNotification(characteristic, true);
                                }
//                                if (characteristic.getUuid().toString().equals("0000fff2-0000-1000-8000-00805f9b34fb")) {
//                                    bleManager.setCharacteristicNotification(characteristic, true);
//                                }
//                                if (characteristic.getUuid().toString().equals("0000fff3-0000-1000-8000-00805f9b34fb")) {
//                                    bleManager.setCharacteristicNotification(characteristic, true);
//                                }
                            }
                        }
                    }

                    @Override
                    public void onConnectFailure(BluetoothGatt gatt, int status) {

                    }
                });
                break;
            case R.id.btn_excel:
                initData();
                break;
            case R.id.btn_clean:
                datas.clear();
                Log.d(TAG, "清除数据");
                break;
        }
    }

    //忽略特定的警告
    @SuppressLint("SimpleDateFormat")
    public void initData() {
        file = new File(getSDPath() + "/Family");
        Log.d(TAG, "表格文件路径" + file.toString());
        makeDir(file);
        ExcelUtils.initExcel(file.toString() + "/ECG.xls", new String[]{"一", "二", "三", "四", "五", "六", "七"});
        ExcelUtils.writeObjListToExcel(datas, getSDPath() + "/Family/ECG.xls", this);
    }

    /**
     * 创建文件夹(递归)
     *
     * @param dir
     */
    public static void makeDir(File dir) {
        if (!dir.getParentFile().exists()) {
            makeDir(dir.getParentFile());
        }
        dir.mkdir();
    }

    /**
     * 获取SD卡路径
     *
     * @return
     */
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        String dir = sdDir.toString();
        return dir;
    }
}
