package com.ble.new_mattress

import MAVLink.MAVLinkPacket
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
import com.ble.mqttexample.MqttClass
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

import MAVLink.mavlink_main.*
import MAVLink.bootloader.*
import MAVLink.smartmattress.*
import MAVLink.logger.*
import MAVLink.bluetooth.*
import android.annotation.SuppressLint

import android.graphics.drawable.Drawable
import android.os.Environment
import android.view.WindowManager
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.lang.Exception
import java.lang.Thread.sleep

@ExperimentalUnsignedTypes
class bed_adjust : AppCompatActivity() {
    //////mavlink id
    val CMD_BL_COMMAND = msg_bl_command.MAVLINK_MSG_ID_BL_COMMAND                 ///2
    val CMD_BL_OTA = msg_bl_ota.MAVLINK_MSG_ID_BL_OTA                             ///3
    val CMD_BLUETOOTH_CONNECT = msg_connect.MAVLINK_MSG_ID_CONNECT                ///34
    val CMD_ADJUST_HARDNESS = msg_adjust_hardness.MAVLINK_MSG_ID_ADJUST_HARDNESS  ///52
    val CMD_RELIEVE_STRESS = msg_relieve_stress.MAVLINK_MSG_ID_RELIEVE_STRESS     ///53
    val CMD_MEDITATION =  msg_meditation.MAVLINK_MSG_ID_MEDITATION                ///54
    val CMD_SMARTMATTRESS_CONNECT = msg_connection.MAVLINK_MSG_ID_CONNECTION      ///55
    val CMD_REQUEST_DATA = msg_request_data.MAVLINK_MSG_ID_REQUEST_DATA           ///59
    val CMD_CONTROL_PUMP = msg_control_pump.MAVLINK_MSG_ID_CONTROL_PUMP           ///60
    val CMD_CONTROL_STEP = msg_control_step.MAVLINK_MSG_ID_CONTROL_STEP           ///61
    //////mavlink id

/////position val
    val pos_1 = 0x01.toByte()
    val pos_2 = 0x02.toByte()
    val pos_3 = 0x03.toByte()
    val pos_4 = 0x04.toByte()
    val pos_5 = 0x05.toByte()
    val pos_6 = 0x06.toByte()

//////for mqtt protocol

    val TAG = "bed_adjust"
    var mqttclass : MqttClass? = MqttClass()

    val topic1 = "smttrss"

    val topic2 = arrayOf("control","sensor","ack","err","config","model","ota","else")

    //var wifi_mac = ""
    //var wifi_mac = "7c:df:a1:c2:96:ac"

//////for mqtt protocol

/////FLAG

    var FLAG_CLICK_BED = false /////click the bed button
    var FLAG_AUTO_TUNE = false //////for body tune function ?

    var FLAG_RELI_ONOFF = false ///////true when mode 2 on
    var FLAG_RELI_UPDOWN = false///////true when up, false when down

    var FLAG_MEDI_ON = false  ///// true when mode 3 on


/////FLAG

    lateinit var extFile: File
    var soundexist: Boolean = false

    lateinit var mediaPlayer: MediaPlayer

    var mode = "cohe"
    private val DATA_DIRECTORY = "LOG_DATA"

////number textview

    lateinit var res_head : TextView
    lateinit var res_neck : TextView
    lateinit var res_shoulder : TextView
    lateinit var res_back : TextView
    lateinit var res_weist : TextView
    lateinit var res_butt : TextView

    lateinit var tv_pressure : TextView
    lateinit var tv_time :TextView

////number textview

/////////////command

/////////left or right

    var bed_lrb :Int = 2
    //////////// 0 both 1 left 2 right

    lateinit var bed_btn :ImageView
    lateinit var bed_icn :ImageView
    lateinit var iv_bleicn :ImageView

/////////left or right


    ///////////mode button
///////////mode button

/////cohe timer

    var pullstart = arrayListOf<Boolean>(false , false , false , false , false , false)
    var pulltimer = arrayListOf<Int>(0,0,0,0,0,0)
    var pulllevel = arrayListOf<Int>(16,16,16,16,16,16)

/////cohe timer


//////reli on off

    var reli_part = 1
    var reli_level = 10

    val RELI_MAX = 10
    val RELI_MIN = 5

    var reli_timer = 10////// how many seconds to change up and down
    var reli_count = 0  ////// change up and down when counter reach to 10

    lateinit var tv_medi_settime :TextView
    lateinit var ib_reli1 :ImageButton
    lateinit var ib_reli2 :ImageButton
    lateinit var ib_reli3 :ImageButton
    lateinit var ib_reli4 :ImageButton
    lateinit var ib_reli5 :ImageButton
    lateinit var ib_reli6 :ImageButton

//////reli on off

//////medi on off
    lateinit var ib_funmedi : ImageButton
    lateinit var iv_medistatus : ImageView

    var prev_weist = 5
    var prev_butt = 5

    val medi_level = 10.toByte()

    //////medi on off
/////
    val normal_sleep_file = "normal.txt"
    val side_sleep_file = "side.txt"


    ///////////seek bar for draw
   // lateinit var GREENBAR : Drawable
   // lateinit var PURPLEBAR : Drawable


    var sb_head: SeekBar? = null
    var sb_neck: SeekBar? = null
    var sb_shoulder: SeekBar? = null
    var sb_back: SeekBar? = null
    var sb_weist: SeekBar? = null
    var sb_butt: SeekBar? = null
//////////////seek bar for draw
    lateinit var num_head : TextView
    lateinit var num_neck : TextView
    lateinit var num_shoulder : TextView
    lateinit var num_back : TextView
    lateinit var num_weist : TextView
    lateinit var num_butt : TextView


    var current_head = 2
    var current_neck = 2
    var current_shoulder = 2
    var current_back = 2
    var current_weist = 2
    var current_butt = 2

    var set_head = 2
    var set_neck = 2
    var set_shoulder = 2
    var set_back = 2
    var set_weist = 2
    var set_butt = 2

    var set_lhead = 2
    var set_lneck = 2
    var set_lshoulder = 2
    var set_lback = 2
    var set_lweist = 2
    var set_lbutt = 2


    var set_rhead = 2
    var set_rneck = 2
    var set_rshoulder = 2
    var set_rback = 2
    var set_rweist = 2
    var set_rbutt = 2


    val progressdivide = 5

    var queue_cmd : Queue<MqttMessage> = LinkedList<MqttMessage>(listOf())
    var queue_resend = 0
    var queue_retry = 0

////////runnable
///////////handler function
    ////////command thread

    private  val cmdthread :Thread = Thread{

        while(FLAG_MQTT_CONNECT){
            if(queue_cmd.size>0){
                queue_resend++
                if(queue_resend >3 && !FLAG_MATTRESS_ACK){
                    /////need resend
                    val thistopic = topic1 + "/"  + topic2[0] + "/" + wifi_mac
                    mqttclass!!.publish(thistopic, queue_cmd.first() , 1)
                    queue_retry++
                }////need resend
                else if(FLAG_MATTRESS_ACK || queue_retry > 2){
                    queue_cmd.poll()
                    if(queue_retry >2){
                        Log.d("cmd_queue","send falied")
                    }
                    else{
                        Log.d("cmd_queue","send success")
                    }
                    queue_resend = 0
                    queue_retry = 0
                    FLAG_MATTRESS_ACK = false
                }////send success or give up
                else {
                }///wait ack fail

            }////if queue has sth
            else{

            }

            if(FLAG_RELI_ONOFF){
                ///if reli mode is on


            }///if reli mode is on

            sleep(1000)
        }
    }


////////command thread
fun makesigned2unsigned(x : Short):Short{
    if(x<0)return (x+65536).toShort()
    else return x
}
fun get_timer():String{
    var tmp = ""
    if(Date().hours<10) tmp += "0" + Date().hours.toString()+ ":"
    else tmp += Date().hours.toString() +":"

    if(Date().minutes<10) tmp += "0" + Date().minutes.toString()+ ":"
    else tmp += Date().minutes.toString() +":"

    if(Date().seconds<10) tmp += "0" + Date().seconds.toString()
    else tmp += Date().seconds.toString()

    return tmp
}


var uihandle = Handler()
private val uiRunnable: Runnable = object : Runnable {
    override fun run(){
        runOnUiThread {
            if(mode == "cohe"){
                var pressure_str = ""
                for (i in 0..bed_pressure.size-1){
                    pressure_str += bed_pressure[i].toString()+ ","
                    if(i%3  == 2)pressure_str += "\n"
                }

                pressure_str = pressure_str.dropLast(1)
                try{

                    tv_pressure.text = pressure_str
                    tv_time.text = get_timer()
                }
                catch(e:Exception){

                }
                if(FLAG_MQTT_CONNECT)iv_bleicn.setImageResource(R.drawable.bt_on)
                else iv_bleicn.setImageResource(R.drawable.bt_off)
            }
            else {

            }
        }
        uihandle.postDelayed(this, 1000)
    }
}////uihandle?.postDelayed(uiRunnable, 0)///////run
    //// uihandle.removeCallbacks(uiRunnable)///stop

///////////handler function

////////runnable

    /////mqtt function

    fun mqttconnect() {
        Thread{
            mqttclass!!.connect(this, serverURL, mqttuser, mqttpwd)
            Thread.sleep(5000)
            runOnUiThread{findviewID1()}
            mqttsub()
            FLAG_MQTT_CONNECT = true
            cmdthread.start()
        }.start()
    }

    fun mqttdisconnect(){
        mqttclass!!.disconnect()
        //unregisterReceiver()
    }

    fun mqttpublish(com :ByteArray, msgid:Int) {

        var publishmsg : MqttMessage?= MqttMessage()

        var thistopic = ""
        publishmsg!!.setId(msgid)
        when(msgid){
            CMD_BL_COMMAND->{
                thistopic = topic1 + "/"  + topic2[0] + "/" + wifi_mac
                val mavpac = msg_bl_command()
                publishmsg!!.setPayload(mavpac.pack().encodePacket())
            }//////2
            CMD_BL_OTA->{
                thistopic = topic1 + "/"  + topic2[0] + "/" + wifi_mac
                val mavpac = msg_bl_ota()
                publishmsg!!.setPayload(mavpac.pack().encodePacket())
            }/////3
            CMD_BLUETOOTH_CONNECT->{
                thistopic = topic1 + "/"  + topic2[0] + "/" + wifi_mac
                val mavpac = msg_connect(3)
                publishmsg!!.setPayload(mavpac.pack().encodePacket())

            }/////34

            ////for smart ress
            CMD_ADJUST_HARDNESS->{
                thistopic = topic1 + "/"  + topic2[0] + "/" + wifi_mac
                val pos  = com[1].toShort()
                val level = com[2].toShort()

                Log.d(TAG,"adjust hardness"+ pos.toString()+"=="+level.toString())

                val mavpac = msg_adjust_hardness(0x55,bed_lrb.toShort() , pos ,level)

                publishmsg!!.setPayload(mavpac.pack().encodePacket())
            }/////52
            CMD_RELIEVE_STRESS->{

                thistopic = topic1 + "/"  + topic2[0] + "/" + wifi_mac
                val pos = com[1].toShort()
                val level = com[2].toShort()
                val mavpac = msg_relieve_stress(0x55,bed_lrb.toShort(),pos,level)
                publishmsg!!.setPayload(mavpac.pack().encodePacket())

            }/////53

            CMD_MEDITATION->{
                thistopic = topic1 + "/"  + topic2[0] + "/" + wifi_mac
                val pos = com[1].toShort()
                val level = makesigned2unsigned(com[2].toShort())


                try{
                    val mavpac = msg_meditation(0x55,bed_lrb.toShort(),pos,level)
                    publishmsg!!.setPayload(mavpac.pack().encodePacket())
                }
                catch(e: Exception){
                    Log.e("medi",e.message!!)
                }/////Value is outside of the range of an unsigned byte: -1

            }/////54

            CMD_SMARTMATTRESS_CONNECT->{
                thistopic = topic1 + "/"  + topic2[0] + "/" + wifi_mac
                val mavpac = msg_connection()
                publishmsg!!.setPayload(mavpac.pack().encodePacket())
            }/////55

            CMD_REQUEST_DATA->{
                thistopic = topic1 + "/"  + topic2[0] + "/" + wifi_mac
                val mavpac = msg_request_data(2)
                publishmsg!!.setPayload(mavpac.pack().encodePacket())
            }/////59

            CMD_CONTROL_PUMP->{
                thistopic = topic1 + "/"  + topic2[0] + "/" + wifi_mac
                val mavpac = msg_control_pump(1)
                publishmsg!!.setPayload(mavpac.pack().encodePacket())
            }/////60

            CMD_CONTROL_STEP->{
                thistopic = topic1 + "/"  + topic2[0] + "/" + wifi_mac
                val steps = com[0].toInt()
                val id = com[1].toShort()
                val mavpac = msg_control_step(steps,id)

                publishmsg!!.setPayload(mavpac.pack().encodePacket())
            }////61

        }


        if (publishmsg != null) {
            queue_cmd.add(publishmsg)////put into queue
            //mqttclass!!.publish(thistopic, publishmsg , 1)///send command
        }
    }

    fun mqttsub(){
        mqttclass!!.subscribe(topic1+"/" + topic2[0] + "/" + wifi_mac , 1)////control
        mqttclass!!.subscribe(topic1+"/" + topic2[1] + "/" + wifi_mac , 1)///sensor
        mqttclass!!.subscribe(topic1+"/" + topic2[2] + "/" + wifi_mac , 1)////ack
    }

    fun mqttunsub(){
        mqttclass!!.unsubscribe("#")
    }
    /////mqtt function



    fun send_commandbyBle(com :ByteArray, msgid:Int) :Boolean{
            //val mavparser = Parser().mavlink_parse_char(34)
            var mavpac = byteArrayOf()
            //mavpac.payload.

            when(msgid){
                CMD_BLUETOOTH_CONNECT->{
                    val cmdconstructor =  msg_connect(com[0].toShort(),0,0,false)
                    mavpac = cmdconstructor.pack().encodePacket()
                }

            }
            Log.d("sendcommand mavlink",mavpac.toString())

            if (CHARACTERISTIC_COMMAND!= null) {
                var ch_cmd = false
                while(!ch_cmd) {
                    CHARACTERISTIC_COMMAND?.setValue(mavpac)
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


    val l = object : View.OnTouchListener  {
        override fun onTouch(v:View, event: MotionEvent):Boolean{
            return true
        }
    }/////seekbar undraggable


    val tune_head  = object : SeekBar.OnSeekBarChangeListener{
        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if(FLAG_MQTT_CONNECT && FLAG_WIFI_CONNECT && mode == "cohe"){
                Log.d(TAG,"tunehead"+ current_head.toString())
                var com = byteArrayOf(bed_lrb.toByte(),pos_1,current_head.toByte())
                mqttpublish(com, CMD_ADJUST_HARDNESS)
            }

        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            current_head = (progress / progressdivide)
            if(current_head>19) current_head -= 1
            num_head.text = "$current_head"
        }
    }////////head draggable

    val tune_neck     = object : SeekBar.OnSeekBarChangeListener{
        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if(FLAG_MQTT_CONNECT && FLAG_WIFI_CONNECT && mode == "cohe"){
                Log.d(TAG,"tuneneck"+ current_neck.toString())
                var com = byteArrayOf(bed_lrb.toByte(),pos_2,current_neck.toByte())
                mqttpublish(com, CMD_ADJUST_HARDNESS)
            }
        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            //if(mode == "cohe") {
            current_neck = progress /progressdivide
            if(current_neck>19) current_neck -= 1

            num_neck.text = "$current_neck"

            //}
        }
    }///////neck draggable

    val tune_shoulder = object : SeekBar.OnSeekBarChangeListener{
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if(FLAG_MQTT_CONNECT && FLAG_WIFI_CONNECT && mode == "cohe"){
                Log.d(TAG,"tuneshoulder"+ current_shoulder.toString())
                var com = byteArrayOf(bed_lrb.toByte(),pos_3,current_shoulder.toByte())
                mqttpublish(com, CMD_ADJUST_HARDNESS)
            }

        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                current_shoulder = progress / progressdivide
                if(current_shoulder>19) current_shoulder -= 1
                num_shoulder.text = "$current_shoulder"

        }
    }//////shoulder draggable

    val tune_back  = object : SeekBar.OnSeekBarChangeListener{
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if(FLAG_MQTT_CONNECT && FLAG_WIFI_CONNECT && mode == "cohe"){
                Log.d(TAG,"tuneback"+ current_back.toString())
                var com = byteArrayOf(bed_lrb.toByte(),pos_4,current_back.toByte())
                mqttpublish(com, CMD_ADJUST_HARDNESS)
            }

        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                current_back = progress / progressdivide
                if(current_back>19) current_back -= 1
                num_back.text = "$current_back"

        }
    }//////back draggable

    val tune_weist    = object : SeekBar.OnSeekBarChangeListener{
        override fun onStartTrackingTouch(seekBar: SeekBar?){
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?){
            if(FLAG_MQTT_CONNECT && FLAG_WIFI_CONNECT && mode == "cohe"){
                Log.d(TAG,"tuneweist"+ current_weist.toString())
                var com = byteArrayOf(bed_lrb.toByte(),pos_5,current_weist.toByte())
                mqttpublish(com, CMD_ADJUST_HARDNESS)
            }
        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                current_weist = progress / progressdivide
                if(current_weist>19) current_weist -= 1
                num_weist.text = "$current_weist"

        }
    }///////weist draggable

    val tune_butt     = object : SeekBar.OnSeekBarChangeListener{
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if(FLAG_MQTT_CONNECT && FLAG_WIFI_CONNECT && mode == "cohe"){
                Log.d(TAG,"tunebutt"+ current_butt.toString())
                var com = byteArrayOf(bed_lrb.toByte(),pos_6,current_butt.toByte())
                mqttpublish(com, CMD_ADJUST_HARDNESS)
            }

        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean){

                current_butt = progress / progressdivide
                if(current_butt>19) current_butt -= 1
                num_butt.text = "$current_butt"

        }

    }///////butt draggable

    fun findloadview(){
        setContentView(R.layout.loadingscreen)
    }

    fun findviewID1(){

        //////////////////////findviewbyid
        setContentView(R.layout.activity_bed_adjust)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        bed_btn = findViewById(R.id.bed_btn)
        bed_icn = findViewById(R.id.bed_icn)
        iv_bleicn = findViewById(R.id.iv_bleicn)

        num_head = findViewById(R.id.num_head)
        num_neck = findViewById(R.id.num_neck)
        num_shoulder = findViewById(R.id.num_shoulder)
        num_back = findViewById(R.id.num_back)
        num_weist = findViewById(R.id.num_weist)
        num_butt =findViewById(R.id.num_butt)

        /////disable watchbar first
        sb_head = findViewById(R.id.wb_head);
        sb_neck = findViewById(R.id.wb_neck);
        sb_shoulder = findViewById(R.id.wb_shoulder);
        sb_back = findViewById(R.id.wb_back);
        sb_weist = findViewById(R.id.wb_weist);
        sb_butt = findViewById(R.id.wb_butt);
/////////
        sb_head?.setEnabled(false)
        sb_neck?.setEnabled(false)
        sb_shoulder?.setEnabled(false)
        sb_back?.setEnabled(false)
        sb_weist?.setEnabled(false)
        sb_butt?.setEnabled(false)
//////////

        tv_pressure = findViewById(R.id.tv_pressure)
        tv_time = findViewById(R.id.tv_time)

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
//////////////////////findviewbyid
        sb_head?.setOnSeekBarChangeListener(tune_head)
        sb_neck?.setOnSeekBarChangeListener(tune_neck)
        sb_shoulder?.setOnSeekBarChangeListener(tune_shoulder)
        sb_back?.setOnSeekBarChangeListener(tune_back)
        sb_weist?.setOnSeekBarChangeListener(tune_weist)
        sb_butt?.setOnSeekBarChangeListener(tune_butt)


        setbedlrb()
    }/////for bed adjust
    @SuppressLint("ClickableViewAccessibility")
    fun findviewID2(){
        setContentView(R.layout.activity_bed_cadence)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        tv_medi_settime = findViewById(R.id.tv_medi_settime)
        bed_btn = findViewById(R.id.bed_btn)
        bed_icn = findViewById(R.id.bed_icn)
        iv_bleicn = findViewById(R.id.iv_bleicn)

        /////disable watchbar first

        sb_head = findViewById(R.id.sb_head);
        sb_neck = findViewById(R.id.sb_neck);
        sb_shoulder = findViewById(R.id.sb_shoulder);
        sb_back = findViewById(R.id.sb_back);
        sb_weist = findViewById(R.id.sb_weist);
        sb_butt = findViewById(R.id.sb_butt);

        sb_head?.setOnTouchListener(l)
        sb_neck?.setOnTouchListener(l)
        sb_shoulder?.setOnTouchListener(l)
        sb_back?.setOnTouchListener(l)
        sb_weist?.setOnTouchListener(l)
        sb_butt?.setOnTouchListener(l)

        ib_reli1 = findViewById(R.id.ib_reli1)
        ib_reli2 = findViewById(R.id.ib_reli2)
        ib_reli3 = findViewById(R.id.ib_reli3)
        ib_reli4 = findViewById(R.id.ib_reli4)
        ib_reli5 = findViewById(R.id.ib_reli5)
        ib_reli6 = findViewById(R.id.ib_reli6)
        setbedlrb()

    }/////for bed cadence
    fun setbedlrb(){
        when(bed_lrb){
            0->{
                sb_head?.setProgress(current_head*progressdivide)
                sb_neck?.setProgress(current_neck*progressdivide)
                sb_shoulder?.setProgress(current_shoulder*progressdivide)
                sb_back?.setProgress(current_back*progressdivide)
                sb_weist?.setProgress(current_weist*progressdivide)
                sb_butt?.setProgress(current_butt*progressdivide)
                bed_btn.setImageResource(R.drawable.bed_all)
                bed_icn.setImageResource(R.drawable.small_lr)
            }
            1->{

                sb_head?.setProgress(current_head*progressdivide)
                sb_neck?.setProgress(current_neck*progressdivide)
                sb_shoulder?.setProgress(current_shoulder*progressdivide)
                sb_back?.setProgress(current_back*progressdivide)
                sb_weist?.setProgress(current_weist*progressdivide)
                sb_butt?.setProgress(current_butt*progressdivide)

                bed_btn.setImageResource(R.drawable.bed_left)
                bed_icn.setImageResource(R.drawable.small_l)
            }
            2->{
                sb_head?.setProgress(current_head*progressdivide)
                sb_neck?.setProgress(current_neck*progressdivide)
                sb_shoulder?.setProgress(current_shoulder*progressdivide)
                sb_back?.setProgress(current_back*progressdivide)
                sb_weist?.setProgress(current_weist*progressdivide)
                sb_butt?.setProgress(current_butt*progressdivide)
                bed_btn.setImageResource(R.drawable.bed_right)
                bed_icn.setImageResource(R.drawable.small_r)
            }
        }
    }
    fun findviewID3(){
        setContentView(R.layout.activity_bed_meditation)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        iv_medistatus = findViewById(R.id.iv_medistatus)
        ib_funmedi = findViewById(R.id.ib_funmedi)

    }/////for bed meditation
    fun findviewID4(){
        setContentView(R.layout.activity_body_move)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findloadview()
        extFile = File(storagePath, "command.txt")
        uihandle?.postDelayed(uiRunnable, 0)
        //GREENBAR  = this@bed_adjust.getResources().getDrawable(R.drawable.draw_seekbar_reli)
        //PURPLEBAR  = this@bed_adjust.getResources().getDrawable(R.drawable.draw_seekbar)
        if(ble_cnt){
            mgatt!!.disconnect()
            mgatt!!.close()
            sleep(500)
            mgatt = bluetoothDevice.connectGatt(
                applicationContext,
                false,
                gattCallback
            )
            findviewID1()
        }
        else{
            findviewID1()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        try{
            mqttunsub()
            mqttdisconnect()
        }
        catch(e :Exception){
            Log.e("onDestroy","mqtt null")
        }
        var filePath: String = Environment.getExternalStorageDirectory().absolutePath +
                "/Android/data/com.ble.new_mattress/logcat.txt"
        //if(BuildConfig.BUILD_TYPE == "debug")filePath += ".debug/"
        Runtime.getRuntime().exec(arrayOf("logcat", "-f", filePath, "*:D"))
        uihandle.removeCallbacks(uiRunnable)

        FLAG_MQTT_CONNECT = false


    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Log.e("onCharacteristicRead",characteristic!!.uuid.toString())


            val data = characteristic!!.value
            when(characteristic.uuid.toString()){
                VER_MAC_UUID->{
                    val ble_mac = byteArrayOf(data[4],data[5],data[6],data[7],data[8] ,data[9])
                    val wifi_bytearray = byteArrayOf(data[10],data[11],data[12],data[13],data[14] ,data[15])

                    var strtmp = ""
                    for(i in wifi_bytearray){
                        var j = i.toInt()
                        if(j<0){
                            j = j+256
                        }
                        strtmp += byte2str(j)
                        strtmp += ":"
                    }
                    wifi_mac = strtmp.dropLast(1)

                    mqttconnect()

                    Log.d("onVerMac",wifi_mac)
                }/////get version and mac
                INFO_UUID->{

                    Log.d("onINFO",data[0].toString())
                }/////get info

            }

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
                    Log.d("onVERMAC",wifi_ByteArray.toString())

                }/////get version and mac
                UUID.fromString(INFO_UUID)->{

                }/////get info
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if(newState == 2) {
                mgatt!!.requestMtu(300)
                sleep(2000)
                if (gatt == null) {
                    Log.e("TAG", "mBluetoothGatt not created!");
                    return;
                }
                else{
                    gatt.discoverServices()
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
                   val dp  = CHARACTERISTIC_DATA!!.getDescriptors().first()
                        Log.i("CHARACTERISTIC_INFO", "dp:" + dp.toString())
                        if (dp != null) {
                            if(CHARACTERISTIC_DATA!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0){
                                dp.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            }
                            else if (CHARACTERISTIC_DATA!!.getProperties() != 0 && BluetoothGattCharacteristic.PROPERTY_INDICATE != 0 ) {
                                dp.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                            }
                            var tmp = mgatt!!.writeDescriptor(dp)
                            Log.e("response",tmp.toString())
                        }/////get mac and version first

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
                    mgatt!!.readCharacteristic(CHARACTERISTIC_VER_MAC)

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
            try{
                var notify_success = gatt!!.setCharacteristicNotification(CHARACTERISTIC_DATA, true)
                if(notify_success) Log.i("cDATAnotify", "Enable notify 1")
                else Log.e("cDATAnotify", "Fail to enable notify 1")
            }
            catch(e :Exception){
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

/////////relieve mode

    fun turn_relion(){


        FLAG_RELI_ONOFF = true
        var com = byteArrayOf(0x55.toByte(),reli_part.toByte(),reli_level.toByte())
        if(FLAG_MQTT_CONNECT && FLAG_WIFI_CONNECT)mqttpublish(com, CMD_RELIEVE_STRESS)

        sb_head!!.alpha = 0.5f
        sb_neck!!.alpha = 0.5f
        sb_shoulder!!.alpha = 0.5f
        sb_weist!!.alpha = 0.5f
        sb_back!!.alpha = 0.5f
        sb_butt!!.alpha = 0.5f



        when(reli_part){
            1->{
                sb_head!!.alpha = 1f
                //sb_head!!.scrollBarStyle
                //sb_head!!.setProgressDrawableTiled(GREENBAR)
            }
            2->{
                sb_neck!!.alpha = 1f
                //sb_neck!!.setProgressDrawableTiled(GREENBAR)
            }
            3->{
                sb_shoulder!!.alpha = 1f
                //sb_shoulder!!.setProgressDrawableTiled(GREENBAR)
            }
            4->{
                sb_back!!.alpha = 1f
                //sb_back!!.setProgressDrawableTiled(GREENBAR)
            }
            5->{
                sb_weist!!.alpha = 1f
                //sb_weist!!.setProgressDrawableTiled(GREENBAR)
            }
            6->{
                sb_butt!!.alpha = 1f
                //sb_butt!!.setProgressDrawableTiled(GREENBAR)
            }

        }


    }

    fun turn_relionoff(part : Int){

        /*
        sb_head!!.setProgressDrawableTiled(PURPLEBAR)
        sb_neck!!.setProgressDrawableTiled(PURPLEBAR)
        sb_shoulder!!.setProgressDrawableTiled(PURPLEBAR)
        sb_weist!!.setProgressDrawableTiled(PURPLEBAR)
        sb_back!!.setProgressDrawableTiled(PURPLEBAR)
        sb_butt!!.setProgressDrawableTiled(PURPLEBAR)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            sb_head!!.setMaxHeight(3)
            sb_head!!.setMinHeight(3)
            sb_neck!!.setMaxHeight(3)
            sb_neck!!.setMinHeight(3)
            sb_shoulder!!.setMaxHeight(3)
            sb_shoulder!!.setMinHeight(3)
            sb_back!!.setMaxHeight(3)
            sb_back!!.setMinHeight(3)
            sb_weist!!.setMaxHeight(3)
            sb_weist!!.setMinHeight(3)
            sb_butt!!.setMaxHeight(3)
            sb_butt!!.setMinHeight(3)
        }
        */

        if(FLAG_RELI_ONOFF){
            ///if RELI already on, turn if off
            var com = byteArrayOf(0x55.toByte(),reli_part.toByte(),0x00.toByte())
            if(FLAG_MQTT_CONNECT && FLAG_WIFI_CONNECT)mqttpublish(com, CMD_RELIEVE_STRESS)

            /////turn on if not the same part pressed
            if(part != reli_part){
                /////set new reli part and turn on
                reli_part = part
                turn_relion()
            }
            else{

                ///////turn off
                FLAG_RELI_ONOFF = false
                sb_head!!.alpha = 0.5f
                sb_neck!!.alpha = 0.5f
                sb_shoulder!!.alpha = 0.5f
                sb_weist!!.alpha = 0.5f
                sb_back!!.alpha = 0.5f
                sb_butt!!.alpha = 0.5f

            }////just turn off if part is the same

        }/////RELI already on
    //////turn on
        else{
            /////set reli part and turn on
            reli_part = part
            turn_relion()
        }/////else turn on
    }
    fun clickreli1(view: View) {

        turn_relionoff(1)

    }
    fun clickreli2(view: View) {

        turn_relionoff(2)

    }
    fun clickreli3(view: View) {

        turn_relionoff(3)

    }
    fun clickreli4(view: View) {

        turn_relionoff(4)

    }
    fun clickreli5(view: View) {

        turn_relionoff(5)

    }
    fun clickreli6(view: View) {

        turn_relionoff(6)

    }
    /*
    * triggered when relieve minus button is pressed
    */
    fun clickreliminus(view: View) {
        if(reli_level>RELI_MIN){

            reli_level--
            tv_medi_settime.text = reli_level.toString()
        }////
        else{
            ////already min
        }
    }
    /*
    * triggered when relieve plus button is pressed
    */

    fun clickreliplus(view: View) {
        if(reli_level<RELI_MAX){
            reli_level++
            tv_medi_settime.text = reli_level.toString()
        }////
        else{
            ////already max
        }
    }
/////////relieve mode

    /*
    * triggered when menu button is pressed
    */
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
                /*
                R.id.exit -> {
                    if(soundexist){
                        mediaPlayer.release()
                    }
                    moveTaskToBack(true);
                    exitProcess(-1)
                }
                */
            }
            false
        }

        /*
        popupMenu.setOnDismissListener {
            Toast.makeText(
                this,
                "menu close.",
                Toast.LENGTH_SHORT
            ).show()
        }
        popupMenu.show()
        */

    }


    /*
    *  triggered when bed button is pressed
    */
    fun clickbed(view: View) {

        FLAG_CLICK_BED = true

        if(bed_lrb ==0){
            set_head = current_head
            set_neck = current_neck
            set_shoulder = current_shoulder
            set_back = current_back
            set_weist = current_weist
            set_butt = current_butt


            current_head = set_lhead
            current_neck = set_lneck
            current_shoulder = set_lshoulder
            current_back = set_lback
            current_weist = set_lweist
            current_butt = set_lbutt



            bed_lrb = 1

            ///UI set
            setbedlrb()
            ///UI set

        }//////change to left
        else if(bed_lrb ==1){

            set_lhead = current_head
            set_lneck = current_neck
            set_lshoulder = current_shoulder
            set_lback = current_back
            set_lweist = current_weist
            set_lbutt = current_butt

            current_head = set_rhead
            current_neck = set_rneck
            current_shoulder = set_rshoulder
            current_back = set_rback
            current_weist = set_rweist
            current_butt = set_rbutt

            bed_lrb = 2

            setbedlrb()

        }//////change to right
        else if(bed_lrb == 2){

            set_rhead = current_head
            set_rneck = current_neck
            set_rshoulder = current_shoulder
            set_rback = current_back
            set_rweist = current_weist
            set_rbutt = current_butt

            current_head = set_head
            current_neck = set_neck
            current_shoulder = set_shoulder
            current_back = set_back
            current_weist = set_weist
            current_butt = set_butt

            bed_lrb = 0

            setbedlrb()
        }///////change to both

        FLAG_CLICK_BED = false

    }////////click bed

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)

    }
////////click fragment change
    fun clicktrans1(view: View) {
        mode = "cohe"
        findviewID1()

    }
    fun clickcadence1(view: View) {
        mode = "cade"
        findviewID2()
    }
    fun clickmedi1(view: View) {
        mode = "medi"
        findviewID3()
    }

    fun clickbody1(view: View) {
        mode = "body"
        findviewID4()
    }

    fun clickfunmedi(view: View) {
        if(FLAG_MEDI_ON){
         ////turn off
            ib_funmedi.setImageResource(R.drawable.medi_start)
            iv_medistatus.alpha = 0.5f
            FLAG_MEDI_ON = false

            current_weist = prev_weist
            current_butt = prev_butt


            if(FLAG_MQTT_CONNECT && FLAG_WIFI_CONNECT) {
                var com = byteArrayOf(bed_lrb.toByte(),pos_5,medi_level)
                mqttpublish(com, CMD_ADJUST_HARDNESS)
                var com2 = byteArrayOf(bed_lrb.toByte(),pos_6,medi_level)
                mqttpublish(com2, CMD_ADJUST_HARDNESS)
            }

        }// turn off
        else{
         ////turn on
            ib_funmedi.setImageResource(R.drawable.medi_stop)
            iv_medistatus.alpha = 1f
            FLAG_MEDI_ON = true
            prev_weist = current_weist
            prev_butt = current_butt
            current_weist = 10
            current_butt = 10

            if(FLAG_MQTT_CONNECT && FLAG_WIFI_CONNECT){

                var com = byteArrayOf(bed_lrb.toByte(),pos_5,medi_level)
                mqttpublish(com, CMD_ADJUST_HARDNESS)
                var com2 = byteArrayOf(bed_lrb.toByte(),pos_6,medi_level)
                mqttpublish(com2, CMD_ADJUST_HARDNESS)

            /*
                val com = byteArrayOf(bed_lrb.toByte(),0x04.toByte(),0xFF.toByte())
                mqttpublish(com,CMD_MEDITATION)

            */

            }


        }//turn on
    }

    fun clicktraining(view: View) {

    }


////////click fragment change




}