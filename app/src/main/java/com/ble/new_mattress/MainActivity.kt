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
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.system.exitProcess

import org.json.JSONObject
import java.lang.Thread.sleep

//////global variable

//////ble
var ble_cnt = false
var bleaddress = ""
var SavedBleAddr :String = ""

var blefile : File? = null
var storagePath : File? = null

val blefilename = "blemacaddress.txt"

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

//////ble

////////mqtt


val mqttserverfile = "mqttserver.txt"
val defaultmqttfile ="defaultmqttserver.txt"
val mqttlist = arrayListOf<String>()

var serverURL = "tcp://114.34.221.116:6673"
var mqttuser  = "smartmattress"
var mqttpwd =   "aRkZQwD4"

var mqttjson : JSONObject = JSONObject()

////////mqtt

var mgatt: BluetoothGatt? = null

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

    lateinit var reader : BufferedReader;
    lateinit var ib_ble :ImageButton

    var FLAG_FOUNDDEVICE = false
    var bthHandler2: Handler? = Handler()


    private val gattCallback = object : BluetoothGattCallback() {
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            //super.onCharacteristicRead(gatt, characteristic, status)
            Log.e("onCharacteristicRead",characteristic!!.uuid.toString())
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            //super.onCharacteristicWrite(gatt, characteristic, status)
            Log.e("onCharacteristicWrite",status.toString() +" :"+characteristic!!.uuid.toString())
        }
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ){
            val data = characteristic!!.value
            Log.e("onCharacteristicChanged",characteristic.uuid.toString())

            when(characteristic.uuid){

                UUID.fromString(VER_MAC_UUID)->{
                    var wifi_ByteArray = byteArrayOf(data[10],data[11],data[12],data[13],data[14] ,data[15])
                    Log.d("onVerMac",wifi_ByteArray.toString())
                }/////get version and mac
                UUID.fromString(INFO_UUID)->{

                }/////get info
            }
        }


        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if(newState == 1 || newState == 2) {
                val result = gatt.requestMtu(256)
                Log.e("TAG", "onConnectionStateChange ($bleaddress) $newState status: $status");
                if (gatt == null) {
                    Log.e("TAG", "mBluetoothGatt not created!");
                    return;
                }
                else{
                    gatt.discoverServices()
                }
                //bluetoothDevice = bluetoothAdapter.getRemoteDevice(bleaddress)

                //String address = device.getAddress();
            }
            else if(newState ==0 || newState == 3){
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
                ble_cnt = false
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)
            Log.e("DR", gatt.toString())
            Log.e("DR", descriptor.toString())
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.e("DW", gatt.toString())
            Log.e("DW", descriptor.toString())
            ////send connect both device
            when(descriptor){
                CHARACTERISTIC_DATA!!.getDescriptors().first()->{
                    val dp = CHARACTERISTIC_DATA!!.getDescriptors().last()
                    Log.i("CHARACTERISTIC_DATA-2", "dp:" + dp.toString())
                    if (dp != null) {
                        if(CHARACTERISTIC_DATA!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0){
                            dp.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        }
                        else if (CHARACTERISTIC_DATA!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_INDICATE != 0 ) {
                            dp.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        }
                        var tmp = mgatt!!.writeDescriptor(dp)
                        Log.e("response",tmp.toString())
                    }

                }
                CHARACTERISTIC_DATA!!.getDescriptors().last()-> {
                    mgatt!!.readCharacteristic(CHARACTERISTIC_VER_MAC)
                    //mgatt!!.readCharacteristic(CHARACTERISTIC_INFO)
                    /////get mac and version first
                    //send_commandbyBle(byteArrayOf(0x03), CMD_BLUETOOTH_CONNECT )
                }/////get mac and version first
                CHARACTERISTIC_COMMAND!!.getDescriptors().last()->{
                    val dp = CHARACTERISTIC_DATA!!.getDescriptors().first()
                    Log.i("CHARACTERISTIC_DATA-1", "dp:" + dp.toString())
                    if (dp != null) {
                        if(CHARACTERISTIC_DATA!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0){
                            dp.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        }
                        else if (CHARACTERISTIC_DATA!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_INDICATE != 0 ) {
                            dp.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        }
                        var tmp = mgatt!!.writeDescriptor(dp)
                        Log.e("response",tmp.toString())
                    }

                    /////get mac and version first
                }///////send get mac first
                CHARACTERISTIC_VER_MAC!!.getDescriptors().last()->{
                    for (dp in CHARACTERISTIC_INFO!!.getDescriptors()){
                        Log.i("CHARACTERISTIC_INFO", "dp:" + dp.toString())
                        if (dp != null) {
                            if(CHARACTERISTIC_INFO!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0){
                                dp.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            }
                            else if (CHARACTERISTIC_INFO!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_INDICATE != 0 ) {
                                dp.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                            }
                            var tmp = mgatt!!.writeDescriptor(dp)
                            Log.e("response",tmp.toString())
                        }
                    }
                }////////get info descriptor after ver mac
                CHARACTERISTIC_INFO!!.getDescriptors().last()->{
                    for (dp in CHARACTERISTIC_COMMAND!!.getDescriptors()) {
                        Log.i("CHARACTERISTIC_COMMAND", "dp:" + dp.toString())

                        if (dp != null) {

                            if(CHARACTERISTIC_COMMAND!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0){
                                dp.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            }
                            else if (CHARACTERISTIC_COMMAND!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_INDICATE != 0 ) {
                                dp.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                            }

                            var tmp = mgatt!!.writeDescriptor(dp)
                            Log.e("response",tmp.toString())
                        }
                    }////command descriptors
                }//////get commmand descriptor after info
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.e("GATT", "onServicesDiscovered")

            Service_UART = gatt!!.getService(UUID.fromString(SMARTMATTRESS_UUID))
            // Get Characteristic
            CHARACTERISTIC_DATA       = Service_UART!!.getCharacteristic(UUID.fromString(DATA_UUID))
            CHARACTERISTIC_VER_MAC    = Service_UART!!.getCharacteristic(UUID.fromString(VER_MAC_UUID))
            CHARACTERISTIC_INFO       = Service_UART!!.getCharacteristic(UUID.fromString(INFO_UUID))
            CHARACTERISTIC_COMMAND    = Service_UART!!.getCharacteristic(UUID.fromString(COMMAND_UUID))

            // Enable Notify
            try{
                var notify_success = gatt!!.setCharacteristicNotification(CHARACTERISTIC_DATA, true)
                if(notify_success) Log.i("cDATAnotify", "Enable notify 1")
                else Log.e("cDATAnotify", "Fail to enable notify 1")
            }
            catch(e : java.lang.Exception){
                e.message?.let { Log.d("on notify", it) }
            }


            for (dp in CHARACTERISTIC_VER_MAC!!.getDescriptors()){
                Log.e("CHARACTERISTIC_VER_MAC", "dp:" + dp.toString())
                if (dp != null) {
                    if(CHARACTERISTIC_VER_MAC!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0){
                        dp.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        Log.e("VER_MAC-notify", "dp:" + dp.toString())

                    }
                    else if (CHARACTERISTIC_VER_MAC!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_INDICATE != 0 ) {
                        dp.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        Log.e("VER_MAC-indicate", "dp:" + dp.toString())

                    }
                    var tmp = mgatt!!.writeDescriptor(dp)
                    Log.e("response",tmp.toString())
                }
            }
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

        storagePath = this.getExternalFilesDir(null)
        blefile = File(storagePath, blefilename)
        var mqttfile = File(storagePath,mqttserverfile)

        try{
            val mqtttmp = mqttfile.readText()
            mqttjson = JSONObject(mqtttmp)
        }
        catch(e:Exception){
            Log.d("main",e.message!!)
            //mqttfile?.createNewFile()
            ////write default mqtt server file
            reader = BufferedReader(InputStreamReader(getAssets().open(defaultmqttfile), "UTF-8"))
            var mLine = reader.readLine()
            while (mLine != null) {
                if(mLine != "null" ) mqttlist.add(mLine)

                //process line
                try{ mLine = reader.readLine()}
                catch(e: IOException){
                }
            }

            mqttjson.put("server", mqttlist[0])
            mqttjson.put("mqttuser", mqttlist[1])
            mqttjson.put("mqttpwd", mqttlist[2])
            mqttfile.appendText(mqttjson.toString() + "\n")
        }




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
            16->{

            }/////options

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
                R.id.options->{
                    val intent = Intent(this, Options::class.java)
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