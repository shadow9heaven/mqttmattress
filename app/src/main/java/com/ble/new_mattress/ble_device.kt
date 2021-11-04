package com.ble.new_mattress

import android.Manifest
import android.app.PendingIntent
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.File
import java.lang.Exception
import java.lang.Thread.sleep
import java.util.*
import kotlin.collections.HashMap
import kotlin.system.exitProcess
import com.ble.new_mattress.ble_device as bluetooth

class ble_device : AppCompatActivity() {

    lateinit var swipe : SwipeRefreshLayout
    private lateinit var tx_srh : TextView

    private val listlabel = arrayOf("name", "misc")
    private val listid = intArrayOf(android.R.id.text1, android.R.id.text2)

    //constant========================================
    //parameters======================================
    var FLAG_REFRESHING = false
    var device_connect = false
    private val STATE_DISCONNECTED = 0
    var devicename = "BIOLOGUE_SMARTMATTRESS"


    ////////////save list
    private var BLE_DeviceList = HashMap<String, BluetoothDevice>()
    private var BLE_nameList :List<HashMap<String, String>> = ArrayList()

    private lateinit var listView: ListView


    ////////////

    var bthHandler: Handler? = Handler()
    private var adapter: SimpleAdapter? = null


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_device)

        tx_srh = findViewById(R.id.searchtext)

        try{
            bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = bluetoothManager?.adapter
            if (!bluetoothAdapter?.isEnabled) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, 1)
            }
        }
        catch(e :java.lang.Exception){
            Log.e("bleAdapter",e.message!!)
        }

        listView = findViewById(R.id.BTHlist)
        swipe = findViewById(R.id.swiperefresh)

        setList()
        val checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    )
            ) Toast.makeText(this, "didn't get permission", Toast.LENGTH_LONG).show()

        }
        else {///////permission already get
            if(GPSisOPen(this)){
                refreshBtList()
            }
            else{
                val builder = AlertDialog.Builder(this)
                builder.setTitle("warning")
                builder.setIcon(R.mipmap.ic_launcher_round)
                builder.setMessage("didn't get permission")
                builder.setCancelable(false)
                builder.setPositiveButton("OK") { dialogInterface, i ->
                    refreshBtList()
                }///////do sth when touch OK
                builder.create().show()

            }
        }


    }

    fun GPSisOPen(context: Context): Boolean {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager

        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return if (gps || network) {
            true
        } else false
    }

    fun openGPS(context: Context?) {
        val GPSIntent = Intent()
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider")
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE")
        GPSIntent.data = Uri.parse("custom:3")
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send()
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
    }

    fun writeDevice2file(devadd :String){
        blefile = File( storagePath , blefilename)

        if(blefile!!.createNewFile()){
            blefile!!.writeText(devadd)
        }////save the connect device mac
        else{
            blefile!!.delete()
            blefile!!.createNewFile()
            blefile!!.writeText(devadd)
        }

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setList(){
        Log.e("GATT", bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).toString())
        listView.setOnItemClickListener { parent, view, position, id ->

            val clickHandler:Handler=Handler()
            val clickRunnable= Runnable {
                device_connect=false
                val devadd = BLE_nameList[position]["misc"]
                    Log.d("device choose", devadd!!)
                var conDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
                if (conDevices.size > 0) {
                    for (condev in conDevices) {
                        if (condev.address == devadd) {

                            ble_cnt = true
                            bleaddress  = devadd

                            setResult(RESULT_OK, getIntent())
                            finish()
                        }
                    }
                    if (bleaddress != "" && !ble_cnt) {
                        disconnect(bleaddress)
                    }
                    SystemClock.sleep(1000)

                }
                for (r in BLE_DeviceList) {
                    if (r.key == devadd) {
                        var loop = 0
                        while (loop < 5 && !ble_cnt) {
                            mgatt = BLE_DeviceList.get(r.key)!!.connectGatt(
                                    applicationContext,
                                    false,
                                    gattCallback
                            )

                            SystemClock.sleep(500)
                            if (bluetoothManager?.getConnectionState(mgatt?.device, BluetoothProfile.GATT) != STATE_DISCONNECTED &&
                                    bluetoothManager?.getConnectionState(mgatt?.device, BluetoothProfile.GATT) != BluetoothProfile.STATE_DISCONNECTING){

                                ble_cnt = true
                                bleaddress  = devadd

                                writeDevice2file(devadd)

                                setResult(RESULT_OK, getIntent())
                                finish()
                            }
                            loop++
                        }
                    }
                }
                if (!device_connect) {
                    refreshBtList()
                }
            }
            val clickThread=Thread{clickHandler!!.postDelayed(clickRunnable, 500)}
            clickThread.priority=10
            clickThread.start()
        }

        //讓 RecyclerView 的 Adapter 更新畫面
        val listener = object : SwipeRefreshLayout.OnRefreshListener {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onRefresh() {
                clickUpdate(listView)

                //BleInfoadapter?.notifyDataSetChanged()

                swipe.isRefreshing = false
            }
        }
        swipe.setOnRefreshListener(listener)
    }


    private val leScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if(result!!.device.name == devicename) {

                val hashMap: HashMap<String, String> = HashMap()
                hashMap.put("name", result!!.device.name)
                hashMap.put("misc", result!!.device.address)
                if (!BLE_nameList.contains(hashMap)){
                    BLE_nameList += hashMap
                }
                BLE_DeviceList[result.device.toString()] = result.device

            }

        }
        override fun onScanFailed(errorCode: Int) {
            Log.e("Scan Failed", "Error Code: $errorCode")
        }
    }

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
                gatt.discoverServices()
                if (gatt == null) {
                    Log.e("TAG", "mBluetoothGatt not created!");
                    return;
                }

                bluetoothDevice = bluetoothAdapter.getRemoteDevice(bleaddress)

                //String address = device.getAddress();
                Log.e("TAG", "onConnectionStateChange ($bleaddress) $newState status: $status");

                try {
                    when (newState) {
                        BluetoothProfile.STATE_DISCONNECTED -> {
                            broadcastUpdate(ACTION_GATT_DISCONNECTED, bleaddress, status);
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace();
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

            Service_UART = gatt!!.getService(UUID.fromString(SMARTMATTRESS_UUID))

            // Get Characteristic
            CHARACTERISTIC_DATA = Service_UART!!.getCharacteristic(UUID.fromString(DATA_UUID))
            CHARACTERISTIC_VER_MAC  = Service_UART!!.getCharacteristic(UUID.fromString(VER_MAC_UUID))
            CHARACTERISTIC_INFO = Service_UART!!.getCharacteristic(UUID.fromString(INFO_UUID))
            CHARACTERISTIC_COMMAND    = Service_UART!!.getCharacteristic(UUID.fromString(COMMAND_UUID))
            Log.i("serviceDiscovered",  CHARACTERISTIC_COMMAND.toString())

            // Enable Notify ECG

            var notify_success = gatt!!.setCharacteristicNotification(CHARACTERISTIC_DATA, true)
            if(notify_success) Log.i("Biologue", "Enable notify 1")
            else Log.e("Biologue", "Fail to enable notify 1")

            for (dp in CHARACTERISTIC_DATA!!.getDescriptors()) {
                Log.i("gattdevice-ecg", "dp:" + dp.toString())
                if (dp != null) {
                    if(CHARACTERISTIC_DATA!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0){
                        dp.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    }
                    else if (CHARACTERISTIC_DATA!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_INDICATE != 0 ) {
                        dp.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                    }
                    var tmp = gatt.writeDescriptor(dp)
                }
            }

        }
    }



    private fun broadcastUpdate(action: String, address_connected: String, status: Int) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    fun BluetoothDevice.removeBond() {
        try { javaClass.getMethod("refresh").invoke(this) }
        catch (e: Exception) { Log.e("", "Removing bond has been failed. ${e.message}") }
    }



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun refreshBtList(){
        tx_srh.text = "searching..."
        FLAG_REFRESHING = true
        BLE_DeviceList.clear()

        val conDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        bluetoothLeScanner!!.startScan(leScanCallback)

        bthHandler?.postDelayed(Runnable {
            bluetoothLeScanner.stopScan(leScanCallback)

            if (conDevices.size > 0) {
                for (CD in conDevices) {

                    //val hashMap: HashMap<String, String> = HashMap()
                    // hashMap.put("name", CD.name + "(connected)")
                    // hashMap.put("misc", CD.address)
                    val hashMap: java.util.HashMap<String, String> = java.util.HashMap()
                    hashMap.put("name", CD.name)
                    hashMap.put("misc", CD.address)
                    if (!BLE_nameList.contains(hashMap)){
                        BLE_nameList += hashMap
                    }
                    //BLE_DeviceList.add(HashMap(CD.name,))
                }/////for every device already connected
            }//////add connected device first

            adapter = SimpleAdapter(
                this,
                BLE_nameList,
                android.R.layout.simple_list_item_2,
                listlabel,
                listid
            );
            listView.setAdapter(adapter);

            tx_srh.text = ""
            FLAG_REFRESHING = false

        }, 4000)

    }


    fun disconnect(address: String?) {
        if (bluetoothAdapter == null) {
            Log.w("TAG", "disconnect: BluetoothAdapter not initialized")
            return
        }
        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
        val connectionState: Int = bluetoothManager.getConnectionState(device, BluetoothProfile.GATT)
        if (mgatt != null) {
            Log.i("TAG", "disconnect")
            if (connectionState != BluetoothProfile.STATE_DISCONNECTED) {
                mgatt?.disconnect()
            } else {
                Log.w(
                        "TAG",
                        "Attempt to disconnect in state: $connectionState"
                )
            }
        }
    }


    fun clickDisconnect(view: View) {
        try{
            disconnect(bleaddress)
        }catch (e: Exception){

        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun clickUpdate(view: View) {
        if(!FLAG_REFRESHING) {
            if (!bluetoothAdapter.isEnabled) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, 1)
            }
            refreshBtList()
        }
        else{
            Toast.makeText(this, "Searching", Toast.LENGTH_SHORT).show()
        }
    }

    fun clickback(view: View) {
        Log.e("# of the device", bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).size.toString())

        if(device_connect) {
            setResult(RESULT_OK, getIntent())
            device_connect = true
            getIntent().putExtra("device", mgatt?.device?.address)
        }
        else{
            getIntent().putExtra("device", bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).size > 0)
            setResult(RESULT_CANCELED, getIntent())
        }

        finish()
    }

    fun clickmenu(view: View) {
        ////menu
        val popupMenu = PopupMenu(this@ble_device, view)
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
                this@ble_device,
                "menu close.",
                Toast.LENGTH_SHORT
            ).show()
        }
        popupMenu.show()
    }


}