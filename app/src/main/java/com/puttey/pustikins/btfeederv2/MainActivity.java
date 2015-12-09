/**
 * Justin Johnson
 * Fall 2015
 * Embedded Systems Design Course
 * Bluetooth Feeder Application designed to communicate with MSP430 via Bluetooth to program
 * and control an automated pet feeder
 */
package com.puttey.pustikins.btfeederv2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

//Bluetoothspp Library - https://github.com/akexorcist/Android-BluetoothSPPLibrary

public class MainActivity extends Activity {

    //Intent Constants for OnActivityResult callback
    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_BT_DEVICE = 2;
    //Bowl reading constants to display food supply
    private final String EMPTY_BOWL = "B0";
    private final String PERCENT_25_BOWL = "B25";
    private final String PERCENT_50_BOWL = "B50";
    private final String PERCENT_75_BOWL = "B75";
    private final String FULL_BOWL = "B100";
    //Hopper reading constants to display hopper supply
    private final String EMPTY_HOPPER = "H0";

    //Bluetooth adapter object
    BluetoothSPP mBluetoothSPP;


    //Buttons
    Button enableBluetoothButton;
    Button viewDevicesButton;
    Button connectDeviceButton;
    Button disconnectDeviceButton;
    Button updateFoodSupplyButton;
    Button addFoodButton;
    //TextViews
    TextView statusTextView;
    //ImageView
    ImageView bowlImage;
    //Local members
    String selectedDevice;
    String selectedMacAddress;
    Boolean ledOn = false;


    /**
     * Initialize Bluetooth object during onCreate()
     * @param savedInstanceState Used for data persistence. No data being saved at this time
     */
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


        /**
         * Handle incoming Bluetooth messages from MSP403
         * Changes value of Status TextView to the incoming message
         * This assumes incoming data is formatted appropriately
         */
        mBluetoothSPP.setOnDataReceivedListener(new OnDataReceivedListener(){
            public void onDataReceived(byte[] data, String message){
                Log.i("DATA RECEIVED", message);
                if(message.contains(EMPTY_HOPPER)){
                    Toast.makeText(getApplicationContext(),"Hopper Low\nRefill Soon", Toast.LENGTH_SHORT).show();
                }
                if(message.contains(EMPTY_BOWL)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        bowlImage.setImageDrawable(getResources().getDrawable(R.drawable.empty_bowl, getApplicationContext().getTheme()));
                    } else {
                        bowlImage.setImageDrawable(getResources().getDrawable(R.drawable.empty_bowl));
                    }
                }else if(message.contains(PERCENT_25_BOWL)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        bowlImage.setImageDrawable(getResources().getDrawable(R.drawable.twentyfivepercentbowl, getApplicationContext().getTheme()));
                    } else {
                        bowlImage.setImageDrawable(getResources().getDrawable(R.drawable.twentyfivepercentbowl));
                    }
                }else if(message.contains(PERCENT_50_BOWL)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        bowlImage.setImageDrawable(getResources().getDrawable(R.drawable.fiftypercentbowl, getApplicationContext().getTheme()));
                    } else {
                        bowlImage.setImageDrawable(getResources().getDrawable(R.drawable.fiftypercentbowl));
                    }
                }else if(message.contains(PERCENT_75_BOWL)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        bowlImage.setImageDrawable(getResources().getDrawable(R.drawable.seventyfivepercentbowl, getApplicationContext().getTheme()));
                    } else {
                        bowlImage.setImageDrawable(getResources().getDrawable(R.drawable.seventyfivepercentbowl));
                    }
                }else if(message.contains(FULL_BOWL)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        bowlImage.setImageDrawable(getResources().getDrawable(R.drawable.full_bowl, getApplicationContext().getTheme()));
                    } else {
                        bowlImage.setImageDrawable(getResources().getDrawable(R.drawable.full_bowl));
                    }
                }else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        bowlImage.setImageDrawable(getResources().getDrawable(R.drawable.not_connected_bowl, getApplicationContext().getTheme()));
                    } else {
                        bowlImage.setImageDrawable(getResources().getDrawable(R.drawable.not_connected_bowl));
                    }
                }
            }
        });

        /**
         * Acts on Blutooth connection and disconnection events accordingly
         */
        mBluetoothSPP.setBluetoothConnectionListener(new BluetoothConnectionListener(){
            public void onDeviceDisconnected(){
                statusTextView.setText("Connect A Device To Begin");
                disconnectDeviceButton.setVisibility(View.INVISIBLE);
                viewDevicesButton.setVisibility(View.VISIBLE);
                updateFoodSupplyButton.setVisibility(View.INVISIBLE);
                addFoodButton.setVisibility(View.INVISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    bowlImage.setImageDrawable(getResources().getDrawable(R.drawable.not_connected_bowl, getApplicationContext().getTheme()));
                } else {
                    bowlImage.setImageDrawable(getResources().getDrawable(R.drawable.not_connected_bowl));
                }
            }

            public void onDeviceConnectionFailed(){
                statusTextView.setText(getString(R.string.connection_time_out_text));
            }

            public void onDeviceConnected(String name, String address){
                statusTextView.setText("Bluetooth Connected");
                connectDeviceButton.setVisibility(View.INVISIBLE);
                disconnectDeviceButton.setVisibility(View.VISIBLE);
                viewDevicesButton.setVisibility(View.INVISIBLE);
                updateFoodSupplyButton.setVisibility(View.VISIBLE);
                addFoodButton.setVisibility(View.VISIBLE);
                //request food supply to dispay appropriate bowl to user
                mBluetoothSPP.send(new byte[]{0x57}, true);
            }
        });
    }

    /**
     * Inflate appropriate menu for display
     * @param menu
     * @return
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_connection, menu);
        return true;
    }

    /**
     * Handle Menu Item Selections
     * @param item  The menu item selected
     * @return
     */
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

    /**
     * An application destroy - end Bluetooth connection service
     */
    public void onDestroy() {
        super.onDestroy();
        mBluetoothSPP.stopService();
    }

    /**
     * On application start
     * Check if Bluetooth is enabled
     * Launch Bluetooth Enable activity if Necessary
     * Start Bluetooth Service for non-mobile device communication (MSP430)
     */
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

    /**
     * Initialize Buttons and OnClick Listeners
     */
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
             //   Log.i("In Disconnect Button", selectedMacAddress);
                mBluetoothSPP.disconnect();
            }
        });

        //Update Food Supply button sends 0x57 = 'W' to check weight sensor value
        updateFoodSupplyButton = (Button) findViewById(R.id.viewFoodSupply);
        updateFoodSupplyButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!ledOn){
                //    Log.i("VIEW FOOD SUPPLY", selectedMacAddress);
                    mBluetoothSPP.send(new byte[] {0x57}, true);
                }
            }
        });
        //Add Food Button sends 0x46 = 'F' to signal MSP430 to dispense food
        addFoodButton = (Button) findViewById(R.id.addFood);
        addFoodButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.i("ADD FOOD", selectedDevice);
                mBluetoothSPP.send(new byte[] {0x46}, true);
            }
        });
        //TextView to display current status
        statusTextView = (TextView) findViewById(R.id.statusText);
        //ImageView to display bowl food supply
        bowlImage = (ImageView) findViewById(R.id.bowlImageView);
    }

    /**
     * Handle Activity callbacks
     * @param requestCode Corresponds to the Activity called to distinguish which callback to handle
     * @param resultCode RESULT_OK or RESULT_CANCEL represents success/failed activity
     * @param data Additional information from Activity
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Connect Device callback
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                mBluetoothSPP.connect(data);
        }
        //Enable Bluetooth callback
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
        //Enable Bluetooth callback
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
        //Select Bluetooth Device callback
        else if (requestCode == REQUEST_BT_DEVICE){
            if (resultCode == RESULT_OK){
                selectedDevice = data.getStringExtra("bluetoothDevice");
                selectedMacAddress = data.getStringExtra("macAddress");
                statusTextView.setText("Device Selected " + selectedDevice);
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