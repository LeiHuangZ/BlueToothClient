package com.zhuoxin.bluetoothclient;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoxin.bluetoothclient.commons.ActivityUtils;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private BluetoothManager mBluetoothManager;//本地蓝牙管理服务
    private BluetoothAdapter mBluetoothAdapter;//本地蓝牙适配器
    private String TAG = "MainActivity： ";
    private ActivityUtils mActivityUtils;
    private TextView tvDevice;
    private BluetoothDevice mRemoteDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivityUtils = new ActivityUtils(this);
        tvDevice = (TextView) findViewById(R.id.tv);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(MainActivity.this,"您的设备不支持BLE，请更换设备安装",Toast.LENGTH_SHORT).show();
            finish();
        }

        //1.获取本地蓝牙管理服务和适配器
        mBluetoothManager = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null){
            mActivityUtils.showToast("您的设备不支持蓝牙功能");
            finish();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);
    }


    public void open_bluetooth(View view) {
        if (!mBluetoothAdapter.isEnabled()){
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
        }else {
            mActivityUtils.showToast("蓝牙设备已开启");
        }
    }

    public void check_connected(View view) {
        Set<BluetoothDevice> mBondedDevices = mBluetoothAdapter.getBondedDevices();
        if (mBondedDevices.size() == 0){
            mActivityUtils.showToast("没有已配对的设备");
            return;
        }
        for (BluetoothDevice bluetoothDevice:
             mBondedDevices) {
            tvDevice.append(bluetoothDevice.getName()+"："+bluetoothDevice.getAddress()+"\n");
        }
    }

    public void scan_bluetooth(View view) {
        mBluetoothAdapter.startDiscovery();
        tvDevice.setText("发现设备"+"\n");
        tvDevice.append("开始扫描"+"\n");
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        private ProgressDialog mShow;

        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            if (mAction.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //74:51:BA:D0:F2:4D
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    tvDevice.append(device.getName() + "：" + device.getAddress() + "\n");
                    Log.e(TAG, "onReceive: " + device.getName() + "：" + device.getAddress());
                    if (device.getAddress().equals("74:51:BA:D0:F2:4D")){
                        mRemoteDevice = mBluetoothAdapter.getRemoteDevice("74:51:BA:D0:F2:4D");
                    }
                }

            }else if (mAction.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                tvDevice.append("搜索完成\n");
            }else if (mAction.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (mDevice.getBondState()){
                    case BluetoothDevice.BOND_BONDING://正在配对
                        mShow = ProgressDialog.show(MainActivity.this, "", "正在配对");
                        break;
                    case BluetoothDevice.BOND_BONDED://配对结束
                        Log.d("yxs", "完成配对");
                        mShow.dismiss();
                        break;
                    case BluetoothDevice.BOND_NONE://取消配对/未配对
                        mActivityUtils.showToast("配对取消");
                        Log.d("yxs", "取消配对");
                    default:
                        break;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    public void bind(View view) {
        mRemoteDevice.createBond();
    }
}
