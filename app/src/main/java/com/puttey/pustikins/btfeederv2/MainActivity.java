package com.puttey.pustikins.btfeederv2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends Activity {
    //Intent Constants for OnActivityResult callback
    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_BT_DEVICE = 2;
    //Bluetooth adapter object
    BluetoothSPP mBluetoothSPP;
    //Menus
    Menu menu;
    //Buttons
    Button enableBluetoothButton;
    Button viewDevicesButton;
    Button connectDeviceButton;
    Button disconnectDeviceButton;
    Button ledOnButton;
    Button ledOffButton;
    Button ledFlashButton;
    //TextViews
    TextView statusTextView;
    //Local members
    String selectedDevice;
    String selectedMacAddress;
    Boolean ledOn = false;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("Check", "onCreate");

        mBluetoothSPP = new BluetoothSPP(this);

        //if bluetooth isn't available, notify user
        if(!mBluetoothSPP.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }



        mBluetoothSPP.setOnDataReceivedListener(new OnDataReceivedListener(){
            public void onDataReceived(byte[] data, String message){
                Log.i("DATA RECEIVED", message);
                switch (message){
                    case "A":
                        statusTextView.setText(message);
                        break;
                    case "B":
                        statusTextView.setText(message);
                        break;
                    case "F":
                        statusTextView.setText(message);
                        break;
                    default:
                        statusTextView.setText(message);
                        break;
                }
            }
        });

        mBluetoothSPP.setBluetoothConnectionListener(new BluetoothConnectionListener(){
            public void onDeviceDisconnected(){
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_connection, menu);
                statusTextView.setText("Select A Device To Begin");
                disconnectDeviceButton.setVisibility(View.INVISIBLE);
                ledOnButton.setVisibility(View.INVISIBLE);
                ledOffButton.setVisibility(View.INVISIBLE);
                ledFlashButton.setVisibility(View.INVISIBLE);
                viewDevicesButton.setVisibility(View.VISIBLE);
            }

            public void onDeviceConnectionFailed(){
                statusTextView.setText(getString(R.string.connection_time_out_text));
            }

            public void onDeviceConnected(String name, String address){
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_disconnection, menu);
                statusTextView.setText("Connected To\n" + selectedDevice);
                connectDeviceButton.setVisibility(View.INVISIBLE);
                disconnectDeviceButton.setVisibility(View.VISIBLE);
                ledOnButton.setVisibility(View.VISIBLE);
                ledOffButton.setVisibility(View.VISIBLE);
                ledFlashButton.setVisibility(View.VISIBLE);
                viewDevicesButton.setVisibility(View.INVISIBLE);
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_connection, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_android_connect) {
            mBluetoothSPP.setDeviceTarget(BluetoothState.DEVICE_ANDROID);
			/*
			if(mBluetoothSPP.getServiceState() == BluetoothState.STATE_CONNECTED)
    			mBluetoothSPP.disconnect();*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if(id == R.id.menu_device_connect) {
            mBluetoothSPP.setDeviceTarget(BluetoothState.DEVICE_OTHER);
			/*
			if(mBluetoothSPP.getServiceState() == BluetoothState.STATE_CONNECTED)
    			mBluetoothSPP.disconnect();*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if(id == R.id.menu_disconnect) {
            if(mBluetoothSPP.getServiceState() == BluetoothState.STATE_CONNECTED)
                mBluetoothSPP.disconnect();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onDestroy() {
        super.onDestroy();
        mBluetoothSPP.stopService();
    }

    public void onStart() {
        super.onStart();
        if (!mBluetoothSPP.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!mBluetoothSPP.isServiceAvailable()) {
                mBluetoothSPP.setupService();
                mBluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
                setup();
                //if bluetooth is enabled, update buttons
                if(mBluetoothSPP.isBluetoothEnabled()){
                    enableBluetoothButton.setVisibility(View.INVISIBLE);
                    viewDevicesButton.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void setup() {
        enableBluetoothButton = (Button) findViewById(R.id.enableBluetoothButton);
        enableBluetoothButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (!mBluetoothSPP.isBluetoothAvailable()){  //if bluetooth not supported, alert user
                    Toast.makeText(getApplicationContext(), getString(R.string.bluetooth_not_supported_text),
                            Toast.LENGTH_SHORT).show();
                }
                else if(!mBluetoothSPP.isBluetoothEnabled()){  //if bluetooth is not enabled, direct user to enable bluetooth
                        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
                }
            }
        });
        viewDevicesButton = (Button) findViewById(R.id.viewDevicesButton);
        viewDevicesButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent viewDevicesIntent = new Intent(MainActivity.this, ViewDevicesActivity.class);
                startActivityForResult(viewDevicesIntent, REQUEST_BT_DEVICE);
            }
        });

        connectDeviceButton = (Button) findViewById(R.id.connectBluetoothDevice);
        connectDeviceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(selectedMacAddress != null){
                    Log.i("In Connect Button", selectedMacAddress);
                    mBluetoothSPP.connect(selectedMacAddress);
                }
            }
        });

        disconnectDeviceButton = (Button) findViewById(R.id.disconnectBluetoothDevice);
        disconnectDeviceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.i("In Disconnect Button", selectedMacAddress);
                mBluetoothSPP.disconnect();
            }
        });

        ledOnButton = (Button) findViewById(R.id.ledOnButton);
        ledOnButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!ledOn){
                    Log.i("LED ON BUTTON", selectedMacAddress);
                    mBluetoothSPP.send(new byte[] {0x41}, true);
                    ledOn = true;
                }
            }
        });

        ledOffButton = (Button) findViewById(R.id.ledOffButton);
        ledOffButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(ledOn){
                    Log.i("LED OFF BUTTON", selectedMacAddress);
                    mBluetoothSPP.send(new byte[]{0x42}, true);
                    ledOn = false;
                }
            }
        });

        ledFlashButton = (Button) findViewById(R.id.ledFlashButton);
        ledFlashButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.i("LED FLASH BUTTON", selectedMacAddress);
                mBluetoothSPP.send(new byte[]{0x46}, true);
                ledOn = true;
            }
        });

        statusTextView = (TextView) findViewById(R.id.statusText);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                mBluetoothSPP.connect(data);
        }
        else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                mBluetoothSPP.setupService();
                mBluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        else if (requestCode == REQUEST_ENABLE_BT){
            if (resultCode == RESULT_CANCELED){
                Toast.makeText(getApplicationContext(), getString(R.string.connection_time_out_text),
                        Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(getApplicationContext(), getString(R.string.bluetooth_enabled_text),
                        Toast.LENGTH_SHORT).show();
                enableBluetoothButton.setVisibility(View.INVISIBLE);
                viewDevicesButton.setVisibility(View.VISIBLE);
            }
        }
        else if (requestCode == REQUEST_BT_DEVICE){
            if (resultCode == RESULT_OK){
                selectedDevice = data.getStringExtra("bluetoothDevice");
                selectedMacAddress = data.getStringExtra("macAddress");
                statusTextView.setText("Device Selected\n" + selectedDevice);
                connectDeviceButton.setVisibility(View.VISIBLE);
                Log.i("DEVICE RETURNED", selectedDevice);
                Log.i("MAC Address", selectedMacAddress);
            } else{
                Toast.makeText(getApplicationContext(), getString(R.string.no_device_selected_text),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}