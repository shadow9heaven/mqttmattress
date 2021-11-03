package com.ble.new_mattress

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*
import com.ble.new_mattress.ble_device as bluetooth

class ble_device : AppCompatActivity() {
    val SERVICE_UUID_UART = "1234E101-FFFF-1234-ffff-111122223333"
    val COMMAND_CONTROL_UUID = "1234e102-ffff-1234-ffff-111122223333"
    val RESPONSE_UUID = "1234e103-ffff-1234-ffff-111122223333"
    val HARDNESS_STATUS_UUID = "1234e104-ffff-1234-ffff-111122223333"
    private val DEVICE_NAME = "smart bed"

    var Service_UART: BluetoothGattService? = null
    var CHARACTERISTIC_COMMAND: BluetoothGattCharacteristic? = null
    var CHARACTERISTIC_RESPONSE: BluetoothGattCharacteristic? = null
    var CHARACTERISTIC_HARDNESS: BluetoothGattCharacteristic? = null

    private val REQUEST_CODE_ENABLE_BT: Int = 1

    private lateinit var bluetoothManager : BluetoothManager
    private lateinit var bluetoothAdapter : BluetoothAdapter

    private val bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner

    ////////////save list
    private var BLE_DeviceList = HashMap<String, ScanResult>()
    private var devicename: List<HashMap<String, String>> = ArrayList()
    private val listlabel = arrayOf("name", "misc")
    private val listid = intArrayOf(android.R.id.text1, android.R.id.text2)

    lateinit var tx_srh : TextView

    lateinit var mgatt: BluetoothGatt

    var bthHandler: Handler? = Handler()

    private lateinit var listView: ListView
    private var adapter: SimpleAdapter? = null

    private val STATE_DISCONNECTED = 0
    private val STATE_CONNECTING = 1
    private val STATE_CONNECTED = 2
    val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
    val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
    val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
    val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
    val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_device)

        bluetoothManager  = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter


        if (bluetoothAdapter?.isEnabled == false) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, 1)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        }


        tx_srh = findViewById(R.id.searchtext);
        listView = findViewById<ListView>(R.id.BTHlist);
        Log.e("GATT", bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).toString())


        bluetoothLeScanner!!.startScan(leScanCallback)
        tx_srh.text = "Searching......"

        bthHandler?.postDelayed(Runnable {
            bluetoothLeScanner!!.stopScan(leScanCallback)

            for (r in BLE_DeviceList) {
                if(r.value.device.name == "smart bed") {
                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap.put("name", r.value.device.name)
                    hashMap.put("misc", r.key)
                    devicename += hashMap
                }
            }

            adapter = SimpleAdapter(
                this,
                devicename,
                android.R.layout.simple_list_item_2,
                listlabel,
                listid
            );
            listView.setAdapter(adapter);

            tx_srh.text = ""
        }, 3000)

        listView.setOnItemClickListener{ parent, view, position, id ->
            clickConn(this, position)
        }

    }

    fun clickUpdate(view: View) {
        if (bluetoothAdapter?.isEnabled == false) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, 1)
        }

        for(i in devicename) devicename -= i

        bluetoothLeScanner!!.startScan(leScanCallback)

        tx_srh.text = "Searching......"
        bthHandler?.postDelayed(Runnable {
            bluetoothLeScanner!!.stopScan(leScanCallback)
            for (r in BLE_DeviceList) {
                if(r.value.device.name == "smart bed") {

                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap.put("name", r.value.device.name)
                    hashMap.put("misc", r.key)
                    if (!devicename.contains(hashMap)) devicename += hashMap
                }
            }

            adapter = SimpleAdapter(
                this,
                devicename,
                android.R.layout.simple_list_item_2,
                listlabel,
                listid
            );
            listView.setAdapter(adapter);
            tx_srh.text = ""
        }, 3000)
    }


    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if(result!!.device.name == "smart bed") {
                BLE_DeviceList.put(result!!.device.toString(), result)
            }
        }
        override fun onScanFailed(errorCode: Int) {
            Log.e("Scan Failed", "Error Code: $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if(newState != STATE_DISCONNECTED && gatt != null){
                gatt?.discoverServices()


            }
            else if(newState ==STATE_DISCONNECTED){

                broadcastUpdate(ACTION_GATT_DISCONNECTED)

            }

        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {

            Service_UART = gatt!!.getService(UUID.fromString(SERVICE_UUID_UART))

            Log.e("BLE_SERVICE", "onServicesDiscovered()"+gatt.services.toString())

            if(Service_UART == null)
            {
                Log.e("BLE_service", "onServicesDiscovered() Service_UART not found")
                //return
            }

            CHARACTERISTIC_COMMAND = Service_UART!!.getCharacteristic(UUID.fromString(COMMAND_CONTROL_UUID))
            CHARACTERISTIC_HARDNESS = Service_UART!!.getCharacteristic(UUID.fromString(HARDNESS_STATUS_UUID))
            CHARACTERISTIC_RESPONSE = Service_UART!!.getCharacteristic(UUID.fromString(RESPONSE_UUID))

            gatt.setCharacteristicNotification(CHARACTERISTIC_COMMAND, true)


            //Enable Notification for UART TX
         //   val _descriptor = CHARACTERISTIC_UART_TX?.getDescriptor(UUID.fromString(DESCRIPTOR_UUID_ID_TX_UART))
         //   if (_descriptor != null) {
          //      Log.i("BLE_SERVICE", "onServicesDiscovered() Write to Descriptor ENABLE_NOTIFICATION_VALUE")
          //      _descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                //_descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
         //       gatt.writeDescriptor(_descriptor)
          //  } else Log.i("BLE_SERVICE", "onServicesDiscovered() descriptor == null")

        }
    }

    fun clickConn(view: bluetooth, position: Int) {
        if (bluetoothAdapter?.isEnabled == false) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BT)
        }

        Log.e("BUTTON $position", BLE_DeviceList.keys.elementAt(position))

        mgatt = BLE_DeviceList.get(BLE_DeviceList.keys.elementAt(position))?.device!!.connectGatt(
            applicationContext,
            false,
            gattCallback
        )
        SystemClock.sleep(100)
        if(bluetoothManager?.getConnectionState(mgatt.device, BluetoothProfile.GATT) != STATE_DISCONNECTED && bluetoothManager?.getConnectionState(mgatt.device, BluetoothProfile.GATT) != BluetoothProfile.STATE_DISCONNECTING){
            //val builder = AlertDialog.Builder(this)
            //builder.setMessage("Connect to " + mgatt.device.toString() + " successful!")
            //builder.show()

            getIntent().putExtra("device",mgatt.device.toString())
            setResult(RESULT_OK, getIntent())
            finish()
        }
    }

    fun backtomain(view: View) {
        finish()
    }




    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    fun clickmenu(view: View) {

    }
}