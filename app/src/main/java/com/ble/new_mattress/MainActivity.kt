package com.ble.new_mattress

import android.Manifest
import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.*
import kotlin.system.exitProcess


//////global variable
var ble_cnt = false
var bleaddress = ""

var blefile : File? = null
var storagePath : File? = null
val blefilename = "blemacaddress.txt"

var mgatt: BluetoothGatt? = null
val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
lateinit var bluetoothManager : BluetoothManager
lateinit var bluetoothAdapter : BluetoothAdapter
lateinit var bluetoothDevice: BluetoothDevice

var Service_UART: BluetoothGattService? = null

var CHARACTERISTIC_DATA: BluetoothGattCharacteristic? = null
var CHARACTERISTIC_VER_MAC: BluetoothGattCharacteristic? = null
var CHARACTERISTIC_INFO: BluetoothGattCharacteristic? = null
var CHARACTERISTIC_COMMAND: BluetoothGattCharacteristic? = null

////////ble UUID
val SMARTMATTRESS_UUID = "670bef00-5278-1000-8034-12805f9b34fb"
val DATA_UUID =          "670bef01-5278-1000-8034-12805f9b34fb"
val VER_MAC_UUID =       "670bef02-5278-1000-8034-12805f9b34fb"
val INFO_UUID =          "670bef03-5278-1000-8034-12805f9b34fb"
val COMMAND_UUID =       "670bef04-5278-1000-8034-12805f9b34fb"
///////ble UUID

///////global flag

var FLAG_WIFI_CONNECT = false
var FLAG_MATTRESS_ACK = false

///////global flag

//////////bed status
var bed_pressure = longArrayOf(
    0,0,0,0,0,
    0,0,0,0,0,
    0,0,0,0,0,
    0,0,0,0,0
)
//////////bed status
//////global variable

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
val bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner

/////global variable
class MainActivity : AppCompatActivity() {
    lateinit var ib_ble :ImageButton
    var SavedBleAddr :String = ""
    var FLAG_FOUNDDEVICE = false
    var bthHandler2: Handler? = Handler()

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
        }
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if(newState == 2) {
                //gatt.discoverServices()
                if (gatt == null) {
                    Log.e("TAG", "mBluetoothGatt not created!");
                    return;
                }

                //val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(bleaddress)

                //String address = device.getAddress();
                Log.e("TAG", "onConnectionStateChange ($bleaddress) $newState status: $status");
            }
            else if(newState == 0 || newState == 3){
                ble_cnt = false
                var bleaddress = ""
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Disconnect!!", Toast.LENGTH_SHORT)
                    ib_ble.setImageResource(R.drawable.bt_off)
                }

            }
        }
        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)

        }
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.e("GATT", "onServicesDiscovered")
        }
    }


    private val leScanCallback4main = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onScanResult(callbackType: Int, result: ScanResult?) {

            if(result!!.device.address == SavedBleAddr && !FLAG_FOUNDDEVICE && !ble_cnt) {
                FLAG_FOUNDDEVICE = true
                mgatt = result!!.device.connectGatt(
                    applicationContext,
                    false,
                    gattCallback
                )
                SystemClock.sleep(500)
                if (bluetoothManager?.getConnectionState(mgatt?.device, BluetoothProfile.GATT) != STATE_DISCONNECTED &&
                    bluetoothManager?.getConnectionState(mgatt?.device, BluetoothProfile.GATT) != BluetoothProfile.STATE_DISCONNECTING) {

                    ble_cnt = true
                    bleaddress = SavedBleAddr
                    bluetoothDevice = result!!.device
                    ib_ble.setImageResource(R.drawable.bt_on)
                    Toast.makeText(
                        this@MainActivity,
                        "Connect to " + bleaddress + "!!",
                        Toast.LENGTH_SHORT
                    )

                    FLAG_FOUNDDEVICE = false

                }

            }///try to connect

        }
        override fun onScanFailed(errorCode: Int) {
            Log.e("Scan Failed", "Error Code: $errorCode")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findview()

        storagePath = this.getExternalFilesDir(null)
        blefile = File(storagePath, blefilename)

        try{
            bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = bluetoothManager?.adapter
            if (!bluetoothAdapter?.isEnabled) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, 1)
            }
        }
        catch (e: java.lang.Exception){
            Log.e("bleAdapter", e.message!!)
        }


        try{
            if(blefile!!.exists()){
                SavedBleAddr = blefile!!.readText()
                bluetoothLeScanner!!.startScan(leScanCallback4main)

                bthHandler2?.postDelayed(Runnable {
                    bluetoothLeScanner.stopScan(leScanCallback4main)
                    FLAG_FOUNDDEVICE = false
                }, 4000)
            }//////check the address could accessable or not
            else{

            }
        }
        catch (e: Exception){
            Log.d("readble", e.message!!)
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("Permission", "Request External Storage")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                9
            )
        }
        else{
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    18
                )
            }
            else{
                    opentermsActivity()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        broadcastUpdate(ACTION_GATT_DISCONNECTED)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            1 -> {
                if (resultCode == RESULT_OK) {
                    ib_ble.setImageResource(R.drawable.bt_on)
                }
            }/////ble device
            2 -> {


            }/////bed adjust
            8 -> {

            }/////terms

        }

    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED ){

            when(requestCode){
                9 -> {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                            18
                        )
                    }////ask fine location
                    else {
                        //opentermsActivity()
                    }
                }//////write external storage
                18 -> {
                    opentermsActivity()
                }//////access fine location
            }/////if granted
        }
        else{
            Toast.makeText(this, "Can't get permission!!", Toast.LENGTH_SHORT)
            moveTaskToBack(true);
            exitProcess(-1)
        }


    }

    fun opentermsActivity(){
        val intent2 = Intent(this, Terms::class.java)
        //intent.putExtra("mgatt", mgatt)
        startActivityForResult(intent2, 8)
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }


    fun findview(){

        ib_ble =findViewById(R.id.bt_ble)
    }



    fun clickbluetooth(view: View) {
        val intent = Intent(this, ble_device::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        //intent.putExtra("misc1", misc1)
        startActivityForResult(intent, 1)
    }
    fun clickmenu(view: View) {

        ////menu
        val popupMenu = PopupMenu(this@MainActivity, view)
        popupMenu.getMenuInflater().inflate(R.menu.menu_main, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {

                R.id.credit -> {
                    val intent = Intent(this, version::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                    startActivity(intent)
                }

                R.id.exit -> {
                    moveTaskToBack(true);
                    exitProcess(-1)
                }
            }
            false
        }
        popupMenu.setOnDismissListener {
            Toast.makeText(
                this@MainActivity,
                "menu close.",
                Toast.LENGTH_SHORT
            ).show()
        }
        popupMenu.show()

    }
    fun clickstart(view: View) {
        val intent = Intent(this, bed_adjust::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(intent, 2)


    }
}