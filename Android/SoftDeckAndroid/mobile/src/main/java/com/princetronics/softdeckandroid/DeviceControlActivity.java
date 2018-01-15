package com.princetronics.softdeckandroid;

/**
 * Created by princ on 14-Jan-18.
 */

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    private TextView mConnectionState;
    private TextView mDataField;
    private Button button;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;

    private boolean mConnected = false;
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;


    public final static UUID HM_RX_TX =
            UUID.fromString(SampleGattAttributes.HM_RX_TX);

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private static final int MY_PERMISSION_RESPONSE = 42;

    private String deviceName = "SoftDeck";
    private String deviceAddress = "7C:66:9D:9A:B0:26";

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(mBluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void clearUI() {
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        // Sets up UI references.
        mConnectionState = (TextView) findViewById(R.id.connection_state);

        mDataField = (TextView) findViewById(R.id.data_value);

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        addListenerOnButton();
        // Start the
        Log.d("Custom", "SoftDeckConnection created...");
        //getActionBar().setTitle(R.string.title_devices);
        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Prompt for permissions
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("BleActivity", "Location access not granted!");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_RESPONSE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(deviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }


        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                //mBluetoothAdapter.cancelDiscovery();
            }
        }, SCAN_PERIOD);
        mScanning = true;
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(deviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {

        if (data != null) {
            mDataField.setText(data);
        }
    }


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();


        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));

            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            // get characteristic when UUID matches RX/TX UUID
            characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
            characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
        }

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void addListenerOnButton() {

        button = (Button) findViewById(R.id.button_lock);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.d("ManualPrinting","Lock Button Pressed");
                String messageToBeSentTest = getString(R.string.softdeck_press_and_hold)+getString(R.string.key_left_gui); // Press-hold Win key
                final byte[] tx = messageToBeSentTest.getBytes();
                if(mConnected) {
                    Log.d("ManualPrinting","Press-hold WIN");
                    characteristicTX.setValue(tx);
                    mBluetoothLeService.writeCharacteristic(characteristicTX); // Send to SoftDeck Adapter
                    mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);
                }
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String messageToBeSentTest = "l"; // Press-release l key
                        final byte[] tx = messageToBeSentTest.getBytes();
                        if(mConnected) {
                            Log.d("ManualPrinting","Press-release l");
                            characteristicTX.setValue(tx);
                            mBluetoothLeService.writeCharacteristic(characteristicTX); // Send to SoftDeck Adapter
                            mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);
                        }
                    }
                }, 500);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String messageToBeSentTest = getString(R.string.softdeck_release_all_keys); // Press-release l key
                        final byte[] tx = messageToBeSentTest.getBytes();
                        if(mConnected) {
                            Log.d("ManualPrinting","Release-all-keys");
                            characteristicTX.setValue(tx);
                            mBluetoothLeService.writeCharacteristic(characteristicTX); // Send to SoftDeck Adapter
                            mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);
                        }
                    }
                }, 1000);
            }
        });
        button = (Button) findViewById(R.id.button_password);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.d("ManualPrinting","Password Button Pressed");
                String messageToBeSentTest = "mysecretpassword"; // Press-release Win key
                final byte[] tx = messageToBeSentTest.getBytes();
                if(mConnected) {
                    characteristicTX.setValue(tx);
                    mBluetoothLeService.writeCharacteristic(characteristicTX); // Send to SoftDeck Adapter
                    mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);
                }
            }
        });
        button = (Button) findViewById(R.id.button_notepadplusplus);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.d("ManualPrinting","NotePad++ Button Pressed");
                String messageToBeSentTest = getString(R.string.softdeck_press_and_release)+getString(R.string.key_left_gui); // Press-release Win key
                final byte[] tx = messageToBeSentTest.getBytes();
                if(mConnected) {
                    characteristicTX.setValue(tx);
                    mBluetoothLeService.writeCharacteristic(characteristicTX); // Send to SoftDeck Adapter
                    mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);
                }
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String messageToBeSentTest = "notepad"; // Type notepad++
                        final byte[] tx = messageToBeSentTest.getBytes();
                        if(mConnected) {
                            Log.d("ManualPrinting","Type notepad");
                            characteristicTX.setValue(tx);
                            mBluetoothLeService.writeCharacteristic(characteristicTX); // Send to SoftDeck Adapter
                            mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);
                        }
                    }
                }, 500);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String messageToBeSentTest = getString(R.string.softdeck_press_and_release)+getString(R.string.key_return); // Press-release Enter key
                        final byte[] tx = messageToBeSentTest.getBytes();
                        if(mConnected) {
                            Log.d("ManualPrinting","Press-release Enter key");
                            characteristicTX.setValue(tx);
                            mBluetoothLeService.writeCharacteristic(characteristicTX); // Send to SoftDeck Adapter
                            mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);
                        }
                    }
                }, 2000);
            }
        });
    }

    //    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.d("Custom", "FOUND DEVICE");
                            if(!(device.getName() == null)){
                                //Log.d("Custom", device.getName());
                                if(device.getAddress().toString().equals(deviceAddress)){
                                    Log.d("Custom", "Found SoftDeck bluetooth adapter");
                                    //Toast.makeText(getApplicationContext(), "FOUND SOFTDECK", Toast.LENGTH_SHORT).show();

                                    if (mScanning) {
                                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                                        mScanning = false;
                                    }
                                }
                            }
                        }
                    });
                }
            };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}