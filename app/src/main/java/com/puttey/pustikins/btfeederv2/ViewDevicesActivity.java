package com.puttey.pustikins.btfeederv2;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class ViewDevicesActivity extends ListActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_devices);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //query for paired devices
        if(queryDevices()){ //if found - display
            setListAdapter(mConnectionsArrayAdapter);
            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                    String selectedDevice = ((TextView) view).getText().toString();
                    Log.i("DEVICE STORED", selectedDevice);
                    Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
                    Intent i = getIntent();
                    i.putExtra("bluetoothDevice", selectedDevice);
                    i.putExtra("macAddress", mMacArrayList.get(position));
                    setResult(RESULT_OK, i);
                    finish();
                }
            });
        }

    }

    /**
     * Scan for paired bluetooth connections and add append them to ArrayAdapter
     * @return False if 0 paired devices exist
     */
    private boolean queryDevices(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        mConnectionsArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mMacArrayList = new ArrayList<String>();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mConnectionsArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mMacArrayList.add(device.getAddress());
            }

            ListView listView = (ListView) findViewById(android.R.id.list);
            listView.setAdapter(mConnectionsArrayAdapter);
            return true;
        }
        return false;
    }


    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mConnectionsArrayAdapter;
    private ArrayList<String> mMacArrayList;
}
