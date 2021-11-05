package com.ble.new_mattress

import MAVLink.MAVLinkPacket
import MAVLink.Parser


import MAVLink.bluetooth.msg_connect


import android.bluetooth.*
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.drawerlayout.widget.DrawerLayout
import com.ble.mqttexample.MqttClass
import com.google.android.material.navigation.NavigationView
import java.io.File
import java.lang.Exception
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess


@ExperimentalUnsignedTypes
class bed_adjust : AppCompatActivity() {
    //////mavlink id
    val CMD_BLUETOOTH_CONNECT = 34
    val CMD_ADJUST_HARDNESS = 52
    val CMD_RELIEVE_STRESS = 53
    val CMD_MEDITATION = 54
    val CMD_SMARTMATTRESS_CONNECT = 55

    //////mavlink id
    //////mavlink packet


    //////mavlink packet

    //////for mqtt protocol
    var mqttclass : MqttClass? = MqttClass()

    val serverURL = "tcp://114.34.221.116:6673"
    val mqttuser  = "smartmattress"
    val mqttpwd =   "aRkZQwD4"

    val BA_wifissid = byteArrayOf()
    val BA_wifipwd = byteArrayOf()

    val BA_mqttIP = serverURL.toByteArray(Charsets.US_ASCII)
    val BA_mqttuser = mqttuser.toByteArray(Charsets.US_ASCII)
    val BA_mqttpassword = mqttpwd.toByteArray(Charsets.US_ASCII)

    val topic1 = "smttrss"

    val topic2 = arrayOf("control","sensor","ack","err","config","model","ota","else")

    var wifi_mac = "7C:DF:A1:C2:13:CA"

//////for mqtt protocol
/////FLAG
    var FLAG_WAIT_ACK = false
    var FLAG_DESCRIPTOR = false
/////FLAG

    lateinit var extFile: File
    var soundexist: Boolean = false
    lateinit var mediaPlayer: MediaPlayer
    lateinit var drawer: NavigationView
    lateinit var dl_th : DrawerLayout
    var mode = "cohe"
    private val DATA_DIRECTORY = "LOG_DATA"


////number textview
    lateinit var res_head : TextView
    lateinit var res_neck : TextView
    lateinit var res_shoulder : TextView
    lateinit var res_back : TextView
    lateinit var res_weist : TextView
    lateinit var res_butt : TextView
////number textview

/////////////command

/////////left or right

    var bed_lrb :Int = 0
    ////////////0:both, 1 left 2 right

    lateinit var bed_btn :ImageView
    lateinit var bed_icn :ImageView

/////////left or right


    ///////////mode button
    lateinit var bt_cohe : ImageButton
    lateinit var bt_cade : ImageButton
    lateinit var bt_medi : ImageButton
///////////mode button

/////cohe timer
    var pullstart = arrayListOf<Boolean>(false , false , false , false , false , false)
    var pulltimer = arrayListOf<Int>(0,0,0,0,0,0)
    var pulllevel = arrayListOf<Int>(16,16,16,16,16,16)
/////cohe timer



////airbag connect
    //var airbag12:Boolean ? = false
    //var airbag347:Boolean ? = false
    //var airbag37:Boolean ? = true
    //var airbag34567:Boolean ? = false

////airbag connect

    //////cade on off

    var cade_1 = 0
    var cade_2 = 0
    var cade_3 = 0
    var cade_4 = 0
    var cade_5 = 0
    var cade_6 = 0

//////cade on off

    ///////////seek bar for draw
    var sb_head: SeekBar? = null
    var sb_neck: SeekBar? = null
    var sb_shoulder: SeekBar? = null
    var sb_back: SeekBar? = null
    var sb_weist: SeekBar? = null
    var sb_butt: SeekBar? = null
    var sb_leg: SeekBar? = null
//////////////seek bar for draw
    lateinit var num_head : TextView
    lateinit var num_neck : TextView
    lateinit var num_shoulder : TextView
    lateinit var num_back : TextView
    lateinit var num_weist : TextView
    lateinit var num_butt : TextView
    lateinit var num_leg : TextView

    var current_head = 16
    var current_neck = 16
    var current_shoulder = 16
    var current_back = 16
    var current_weist = 16
    var current_butt = 16
    var current_leg = 16

    var set_head = 16
    var set_neck = 16
    var set_shoulder = 16
    var set_back = 16
    var set_weist = 16
    var set_butt = 16
    var set_leg = 16

    var set_lhead = 16
    var set_lneck = 16
    var set_lshoulder = 16
    var set_lback = 16
    var set_lweist = 16
    var set_lbutt = 16
    var set_lleg = 16

    var set_rhead = 16
    var set_rneck = 16
    var set_rshoulder = 16
    var set_rback = 16
    var set_rweist = 16
    var set_rbutt = 16
    var set_rleg = 16


////////runnable
///////////handler function
var uihandle = Handler()
    private val uiRunnable: Runnable = object : Runnable {
        override fun run(){
            runOnUiThread {

            }
            uihandle.postDelayed(this, 1000)
        }
    }////uihandle?.postDelayed(uiRunnable, 0)///////run
    //// uihandle.removeCallbacks(uiRunnable)///stop
///////////handler function



////////runnable



    fun change_time(sec:Int) : String{
        if(sec % 60 >9)return (sec/60).toString() +":"+ (sec % 60).toString()
        else return (sec/60).toString()+":0"+ (sec%60).toString()
    }


    fun get_command(mod :String,body :Byte , bed : Int , p1 :Byte, p2 :Byte,
                    p3 :Byte, p4 :Byte, p5 :Byte, p6 :Byte, p7 :Byte):ByteArray{
        //var tmp = header_len/////for output package
        var tmp = byteArrayOf()
        var cks = 0//////check sum

        //Log.e(cks.toString() +"check sum:", c1.toString() +c2.toString() +c3.toString() +c4.toString() )
        ///////checksum
        return tmp
    }


    fun send_commandbyBle(com :ByteArray, msgid:Int) :Boolean{
            //val mavparser = Parser().mavlink_parse_char(34)
            var mavpac = MAVLinkPacket(com.size ,false)
            //mavpac.payload.
            when(msgid){
                CMD_BLUETOOTH_CONNECT->{
                    val cmdconstructor =  msg_connect(com[0].toShort(),0,0,false)
                    mavpac = cmdconstructor.pack()
                }
            }
            val output = mavpac.encodePacket()

            Log.d("sendcommand mavlink",mavpac.toString())
            if (CHARACTERISTIC_COMMAND!= null) {
                var ch_cmd = false
                while(!ch_cmd) {
                    CHARACTERISTIC_COMMAND?.setValue(output)
                    ch_cmd = mgatt!!.writeCharacteristic(CHARACTERISTIC_COMMAND)
                    if (ch_cmd) {
                        Log.e("sendcommand", "start_send")
                        //PlotThread.start()
                    }
                    else {
                        Log.e("sendcommand", "start_sendfailed!!")
                        Thread.sleep(200)
                    }
                }
                val dff = SimpleDateFormat("HH-mm-ss")
                dff.setTimeZone(TimeZone.getTimeZone("GMT+8:00"))

                var oritext = dff.format(Date()) + ": "
                for(r in com) oritext += r.toUByte().toString() + "_"
                oritext += "\n"
                extFile.appendText(oritext)
                return true
            }
            else{
                Log.e("sendcommand", "cmd is null")
                return false
            }

    }


    fun set_347(progresa : Int){
        var progress =  progresa /5
        if(current_shoulder != progress){
            current_shoulder = progress
            sb_shoulder?.setProgress(progresa)
            num_shoulder.text = "$current_shoulder"
        }
        if(current_back != progress){
            current_back = progress
            sb_back?.setProgress(progresa)
            num_back.text = "$current_back"
        }

        if(current_leg != progress){
            current_leg = progress
            sb_leg?.setProgress(progresa)
            num_leg.text = "$current_leg"
        }
        if(mode == "cohe"&&bed_lrb == 0){
            set_lshoulder = current_shoulder
            set_rshoulder = current_shoulder
            set_lback = current_back
            set_rback = current_back
            set_lleg = current_leg
            set_rleg = current_leg
        }

    }

    fun set_34567(progresa : Int){
        var progress = progresa /5
        if(current_shoulder != progress){
            current_shoulder = progress
            sb_shoulder?.setProgress(progresa)
            num_shoulder.text = "$current_shoulder"
        }
        if(current_back != progress){
            current_back = progress
            sb_back?.setProgress(progresa)
            num_back.text = "$current_back"
        }
        if(current_weist != progress){
            current_weist = progress
            sb_weist?.setProgress(progresa)
            num_weist.text = "$current_weist"
        }
        if(current_butt != progress){
            current_butt = progress
            sb_butt?.setProgress(progresa)
            num_butt.text = "$current_butt"
        }
        if(current_leg != progress){
            current_leg = progress
            sb_leg?.setProgress(progresa)
            num_leg.text = "$current_leg"
        }
        if(mode == "cohe"&&bed_lrb == 0){
            set_lshoulder = current_shoulder
            set_rshoulder = current_shoulder
            set_lback = current_back
            set_rback = current_back
            set_lweist = current_weist
            set_rweist = current_weist
            set_lbutt = current_butt
            set_rbutt = current_butt
            set_lleg = current_leg
            set_rleg = current_leg
        }
    }



    val l = object : View.OnTouchListener  {
        override fun onTouch(v:View, event: MotionEvent):Boolean{
            return true
        }
    }/////seekbar undraggable

    val tune_head     = object : SeekBar.OnSeekBarChangeListener{
        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if(mode == "cohe"&& ble_cnt) {
                var set = 0x01.toByte()


                //if(airbag12 == true)set = 0x03.toByte()

                var cmd = get_command("tune", set , bed_lrb , 0x01.toByte(), (current_head).toByte(),
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())

                Log.e("command send",(current_head).toByte().toString())

                //var res = send_command(cmd)
                //Log.e("result:", res.toString())
                //sleep(100)
/*
    var pullstart = arrayListOf<Boolean>(false , false , false , false , false , false)
    var pulltimer = arrayListOf<Int>(0,0,0,0,0,0)
    var pulllevel = arrayListOf<Int>(16,16,16,16,16,16)
 */
//////////////////////pool count
                if(!pullstart[0]){
                    pullstart[0] = true
                    pulltimer[0] = 0
                    pulllevel[0] = current_head
                }
                else{
                    pulltimer[0] = 0
                    pulllevel[0] = current_head
                }
//////////////////////pool count


            }///////send command
            else if(mode == "cade"&& ble_cnt){
                if(cade_1==3 || cade_1 ==bed_lrb) {

                    //Log.e("command send",(current_head).toByte().toString())
                    //var res = send_command(cmd)
                }
            }///////cade send command
            if(mode == "cohe"&&bed_lrb == 0){
                //set_head = current_head
                set_lhead = current_head
                set_rhead = current_head


            }

        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            current_head = progress / 5
            num_head.text = "$current_head"
        }
    }////////head draggable

    val tune_neck     = object : SeekBar.OnSeekBarChangeListener{
        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if(mode == "cohe"&& ble_cnt) {

                var set = 0x02.toByte()



                var cmd = get_command("tune",set , bed_lrb , 0x02.toByte(), (current_neck).toByte(),
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())

                Log.e("command send",cmd.toString())
                //var res = send_command(cmd)
               // Log.e("result:", res.toString())

                //sleep(100)

//////////////////////pool count
                if(!pullstart[1]){
                    pullstart[1] = true
                    pulltimer[1] = 0
                    pulllevel[1] = current_neck
                }
                else{
                    pulltimer[1] = 0
                    pulllevel[1] = current_neck
                }
//////////////////////pool count




            }
            else if(mode == "cade"&& ble_cnt){
                if(cade_2==3 || cade_2 ==bed_lrb) {

                    //Log.e("command send",(current_head).toByte().toString())
                    //var res = send_command(cmd)
                }
            }///////cade send command

            if(mode == "cohe"&&bed_lrb == 0){
                //set_neck = current_neck
                set_lneck = current_neck
                set_rneck = current_neck


                    //set_head = current_head
                    set_lhead = current_head
                    set_rhead = current_head

            }
        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            //if(mode == "cohe") {
            current_neck = progress /5
            //set_neck = current_neck
            num_neck.text = "$current_neck"

            //}
        }
    }///////neck draggable

    val tune_shoulder = object : SeekBar.OnSeekBarChangeListener{
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if(mode == "cohe"&& ble_cnt) {


                //Log.e("command send",cmd.toString())
                //var res = send_command(cmd)
                //Log.e("result:", res.toString())
                //sleep(100)

//////////////////////pool count
                if(!pullstart[2]){
                    pullstart[2] = true
                    pulltimer[2] = 0
                    pulllevel[2] = current_shoulder
                }
                else{
                    pulltimer[2] = 0
                    pulllevel[2] = current_shoulder
                }
//////////////////////pool count

            }//////send command
            else if(mode == "cade"&& ble_cnt){
                if(cade_3==3 || cade_3 ==bed_lrb) {

                    //Log.e("command send",(current_head).toByte().toString())
                    //var res = send_command(cmd)
                }
            }///////cade send command

            if(mode == "cohe"&&bed_lrb == 0){
                // set_shoulder = current_shoulder
                set_lshoulder = current_shoulder
                set_rshoulder = current_shoulder
                set_lleg = current_leg
                set_rleg = current_leg

            }

        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                current_shoulder = progress /5
                current_leg = current_shoulder
                sb_shoulder?.setProgress(progress)
                sb_leg?.setProgress(progress)
                num_shoulder.text = "$current_shoulder"
                num_leg.text = "$current_leg"

        }
    }//////shoulder draggable

    val tune_back     = object : SeekBar.OnSeekBarChangeListener{
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if(mode == "cohe" && ble_cnt) {


                //Log.e("command send",cmd.toString())
                //var res = send_command(cmd)
                //Log.e("result:", res.toString())

//////////////////////pool count
                if(!pullstart[3]){
                    pullstart[3] = true
                    pulltimer[3] = 0
                    pulllevel[3] = current_back
                }
                else{
                    pulltimer[3] = 0
                    pulllevel[3] = current_back
                }
//////////////////////pool count

                //sleep(100)
            }
            else if(mode == "cade"&& ble_cnt){
                if(cade_4==3 || cade_4 ==bed_lrb) {

                    //Log.e("command send",(current_head).toByte().toString())
                    //var res = send_command(cmd)
                }
            }///////cade send command

            if(mode == "cohe"&&bed_lrb == 0){
                set_lback = current_back
                set_rback = current_back
            }
        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                current_back = progress /5
                num_back.text = "$current_back"


        }
    }//////back draggable

    val tune_weist    = object : SeekBar.OnSeekBarChangeListener{
        override fun onStartTrackingTouch(seekBar: SeekBar?){
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?){
            if(mode == "cohe" && ble_cnt){



//////////////////////pool count
                if(!pullstart[4]){
                    pullstart[4] = true
                    pulltimer[4] = 0
                    pulllevel[4] = current_weist
                }
                else{
                    pulltimer[4] = 0
                    pulllevel[4] = current_weist
                }
//////////////////////pool count

                //sleep(100)
            }
            else if(mode == "cade"&& ble_cnt){
                if(cade_5==3 || cade_5 ==bed_lrb) {

                    //Log.e("command send",(current_head).toByte().toString())
                    //var res = send_command(cmd)
                }
            }///////cade send command

            if(mode == "cohe"&&bed_lrb == 0){
                set_lweist = current_weist
                set_rweist = current_weist
            }

        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                current_weist = progress /5
                num_weist.text = "$current_weist"

        }
    }///////weist draggable

    val tune_butt     = object : SeekBar.OnSeekBarChangeListener{
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if(mode == "cohe" && ble_cnt) {

                //Log.e("command send",cmd.toString())
                //var res = send_command(cmd)
                //Log.e("result:", res.toString())


//////////////////////pool count
                if(!pullstart[5]){
                    pullstart[5] = true
                    pulltimer[5] = 0
                    pulllevel[5] = current_butt
                }
                else{
                    pulltimer[5] = 0
                    pulllevel[5] = current_butt
                }
//////////////////////pool count

                //sleep(100)
            }


            else if(mode == "cade"&& ble_cnt ){
                if(cade_6==3 || cade_6 ==bed_lrb){
                    //Log.e("command send",(current_head).toByte().toString())
                    //var res = send_command(cmd)
                }
            }///////cade send command


            if(mode == "cohe"&&bed_lrb == 0){
                set_lbutt = current_butt
                set_rbutt = current_butt
            }
        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean){

                current_butt = progress /5
                num_butt.text = "$current_butt"

        }

    }///////butt draggable

    fun findviewID1(){
        //////////////////////findviewbyid
        setContentView(R.layout.activity_bed_adjust)
        num_head = findViewById(R.id.num_head)
        num_neck = findViewById(R.id.num_neck)
        num_shoulder = findViewById(R.id.num_shoulder)
        num_back = findViewById(R.id.num_back)
        num_weist = findViewById(R.id.num_weist)
        num_butt =findViewById(R.id.num_butt)
        num_leg = findViewById(R.id.num_leg)


        /////disable watchbar first
        sb_head = findViewById(R.id.wb_head);
        sb_neck = findViewById(R.id.wb_neck);
        sb_shoulder = findViewById(R.id.wb_shoulder);
        sb_back = findViewById(R.id.wb_back);
        sb_weist = findViewById(R.id.wb_weist);
        sb_butt = findViewById(R.id.wb_butt);
        sb_leg = findViewById(R.id.wb_leg);
/////////
        sb_head?.setEnabled(false)
        sb_neck?.setEnabled(false)
        sb_shoulder?.setEnabled(false)
        sb_back?.setEnabled(false)
        sb_weist?.setEnabled(false)
        sb_butt?.setEnabled(false)
        sb_leg?.setEnabled(false)
//////////


        res_head = findViewById(R.id.texthead)
        res_neck = findViewById(R.id.textneck)
        res_shoulder = findViewById(R.id.textshoulder)
        res_back = findViewById(R.id.textback)
        res_weist = findViewById(R.id.textweist)
        res_butt = findViewById(R.id.textbutt)

        sb_head = findViewById(R.id.sb_head);
        sb_neck = findViewById(R.id.sb_neck);
        sb_shoulder = findViewById(R.id.sb_shoulder);
        sb_back = findViewById(R.id.sb_back);
        sb_weist = findViewById(R.id.sb_weist);
        sb_butt = findViewById(R.id.sb_butt);
        sb_leg = findViewById(R.id.sb_leg);
//////////////////////findviewbyid
        sb_head?.setOnSeekBarChangeListener(tune_head)
        sb_neck?.setOnSeekBarChangeListener(tune_neck)
        sb_shoulder?.setOnSeekBarChangeListener(tune_shoulder)
        sb_back?.setOnSeekBarChangeListener(tune_back)
        sb_weist?.setOnSeekBarChangeListener(tune_weist)
        sb_butt?.setOnSeekBarChangeListener(tune_butt)
        sb_leg?.setOnSeekBarChangeListener(tune_shoulder)

        bed_btn = findViewById(R.id.bed_btn)
        bed_icn = findViewById(R.id.bed_icn)

    }/////for bed adjust
    fun findviewID2(){
        setContentView(R.layout.activity_bed_cadence)

        bed_btn = findViewById(R.id.bed_btn)
        bed_icn = findViewById(R.id.bed_icn)

    }/////for bed cadence
    fun findviewID3(){
        setContentView(R.layout.activity_bed_meditation)

    }/////for bed meditation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        extFile = File(storagePath, "command.txt")
        findviewID1()
        if(ble_cnt){
            mgatt = bluetoothDevice.connectGatt(
                applicationContext,
                false,
                gattCallback
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        uihandle.removeCallbacks(uiRunnable)

    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Log.e("onCharacteristicRead",characteristic!!.uuid.toString())

        }
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
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
                    //wifi_mac =

                }/////get version and mac
                UUID.fromString(INFO_UUID)->{

                }/////get info

            }

        }


        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if(newState == 2) {
                gatt.discoverServices()
                if (gatt == null) {
                    Log.e("TAG", "mBluetoothGatt not created!");
                    return;
                }
                //bluetoothDevice = bluetoothAdapter.getRemoteDevice(bleaddress)

                //String address = device.getAddress();
                Log.e("TAG", "onConnectionStateChange ($bleaddress) $newState status: $status");
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
                CHARACTERISTIC_COMMAND!!.getDescriptors().last()->{
                    send_commandbyBle(byteArrayOf(0x03), CMD_BLUETOOTH_CONNECT )
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
            CHARACTERISTIC_DATA = Service_UART!!.getCharacteristic(UUID.fromString(DATA_UUID))
            CHARACTERISTIC_VER_MAC  = Service_UART!!.getCharacteristic(UUID.fromString(VER_MAC_UUID))
            CHARACTERISTIC_INFO = Service_UART!!.getCharacteristic(UUID.fromString(INFO_UUID))
            CHARACTERISTIC_COMMAND    = Service_UART!!.getCharacteristic(UUID.fromString(COMMAND_UUID))

            // Enable Notify

            var notify_success = gatt!!.setCharacteristicNotification(CHARACTERISTIC_DATA, true)
            if(notify_success) Log.i("cDATAnotify", "Enable notify 1")
            else Log.e("cDATAnotify", "Fail to enable notify 1")


            for (dp in CHARACTERISTIC_VER_MAC!!.getDescriptors()){
                Log.i("CHARACTERISTIC_VER_MAC", "dp:" + dp.toString())
                if (dp != null) {
                    if(CHARACTERISTIC_VER_MAC!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0){
                        dp.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    }
                    else if (CHARACTERISTIC_VER_MAC!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_INDICATE != 0 ) {
                        dp.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                    }
                    var tmp = mgatt!!.writeDescriptor(dp)
                    Log.e("response",tmp.toString())
                }
            }


        }
    }


    fun clickmenu(view: View) {
        ////menu
        val popupMenu = PopupMenu(this, view)
        popupMenu.getMenuInflater().inflate(R.menu.menu_bed, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.credit -> {
                    val intent = Intent(this, version::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                    startActivity(intent)
                }
                R.id.back ->{
                    if(soundexist){
                        mediaPlayer.release()
                    }
                    finish()

                }
                R.id.exit -> {
                    if(soundexist){
                        mediaPlayer.release()
                    }
                    moveTaskToBack(true);
                    exitProcess(-1)
                }
            }
            false
        }
        popupMenu.setOnDismissListener {
            Toast.makeText(
                this,
                "menu close.",
                Toast.LENGTH_SHORT
            ).show()
        }
        popupMenu.show()

    }

    fun clickbed(view: View) {


        if(bed_lrb ==0){
            set_head = current_head
            set_neck = current_neck
            set_shoulder = current_shoulder
            set_back = current_back
            set_weist = current_weist
            set_butt = current_butt
            set_leg = current_leg

            current_head = set_lhead
            current_neck = set_lneck
            current_shoulder = set_lshoulder
            current_back = set_lback
            current_weist = set_lweist
            current_butt = set_lbutt
            current_leg = set_lleg

            sb_head?.setProgress(current_head*5)
            sb_neck?.setProgress(current_neck*5)
            sb_shoulder?.setProgress(current_shoulder*5)
            sb_back?.setProgress(current_back*5)
            sb_weist?.setProgress(current_weist*5)
            sb_butt?.setProgress(current_butt*5)
            sb_leg?.setProgress(current_leg*5)

            bed_btn.setImageResource(R.drawable.bed_left)
            bed_icn.setImageResource(R.drawable.small_l)

            bed_lrb = 1

            ///UI set
            set_bedUI()
            ///UI set


        }//////change to left
        else if(bed_lrb ==1){

            set_lhead = current_head
            set_lneck = current_neck
            set_lshoulder = current_shoulder
            set_lback = current_back
            set_lweist = current_weist
            set_lbutt = current_butt
            set_lleg = current_leg

            current_head = set_rhead
            current_neck = set_rneck
            current_shoulder = set_rshoulder
            current_back = set_rback
            current_weist = set_rweist
            current_butt = set_rbutt
            current_leg = set_rleg

            sb_head?.setProgress(current_head*5)
            sb_neck?.setProgress(current_neck*5)
            sb_shoulder?.setProgress(current_shoulder*5)
            sb_back?.setProgress(current_back*5)
            sb_weist?.setProgress(current_weist*5)
            sb_butt?.setProgress(current_butt*5)
            sb_leg?.setProgress(current_leg*5)



            bed_btn.setImageResource(R.drawable.bed_right)
            bed_icn.setImageResource(R.drawable.small_r)
            bed_lrb = 2

            ///UI set
            set_bedUI()
            ///UI set

        }//////change to right
        else if(bed_lrb == 2){

            set_rhead = current_head
            set_rneck = current_neck
            set_rshoulder = current_shoulder
            set_rback = current_back
            set_rweist = current_weist
            set_rbutt = current_butt
            set_rleg = current_leg

            current_head = set_head
            current_neck = set_neck
            current_shoulder = set_shoulder
            current_back = set_back
            current_weist = set_weist
            current_butt = set_butt
            current_leg = set_leg

            sb_head?.setProgress(current_head*5)
            sb_neck?.setProgress(current_neck*5)
            sb_shoulder?.setProgress(current_shoulder*5)
            sb_back?.setProgress(current_back*5)
            sb_weist?.setProgress(current_weist*5)
            sb_butt?.setProgress(current_butt*5)
            sb_leg?.setProgress(current_leg*5)


            bed_btn.setImageResource(R.drawable.bed_all)
            bed_icn.setImageResource(R.drawable.small_lr)
            bed_lrb = 0

            ///cadence UI set
            //set_UI(bed_lrb)
            ///cadence UI set

        }///////change to both
    }////////click bed

    private fun set_bedUI() {

    }
    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    fun clicktrans1(view: View) {

    }
    fun clickcadence1(view: View) {
        findviewID2()
    }
    fun clickmedi1(view: View) {
        findviewID3()
    }

}