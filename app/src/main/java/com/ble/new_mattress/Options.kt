package com.ble.new_mattress

import MAVLink.bluetooth.msg_connect
import MAVLink.bluetooth.msg_mqtt_set_ip_password
import MAVLink.bluetooth.msg_wifi_set_ssid_password
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class Options : AppCompatActivity() {
    lateinit var tv_connected_device :TextView
    lateinit var tv_serverip :TextView
    lateinit var tv_mqttuser :TextView
    lateinit var tv_mqttpwd :TextView

    lateinit var et_ssid :EditText
    lateinit var et_wifipwd : EditText
    var ssidjson = JSONObject()
    val CMD_SET_SSID   = msg_wifi_set_ssid_password.MAVLINK_MSG_ID_WIFI_SET_SSID_PASSWORD              ///32
    val CMD_SET_IP_PWD = msg_mqtt_set_ip_password.MAVLINK_MSG_ID_MQTT_SET_IP_PASSWORD              ///33
    val CMD_SET_WIFI_CONNECT = msg_connect.MAVLINK_MSG_ID_CONNECT    ///34
    var ssidfiletmp = File(storagePath, ssidfile)
    /*
    val BA_mqttIP = serverURL.toByteArray(Charsets.US_ASCII)
    val BA_mqttuser = mqttuser.toByteArray(Charsets.US_ASCII)
    val BA_mqttpassword = mqttpwd.toByteArray(Charsets.US_ASCII)
    */



    fun send_commandbyBle(msgid:Int) :Boolean{

        var mavpac = byteArrayOf()

        when(msgid){
            CMD_SET_SSID->{


                val ssidstring = et_ssid.text.toString().toByteArray(Charsets.US_ASCII)
                val pwdstring = et_wifipwd.text.toString().toByteArray(Charsets.US_ASCII)

                var ssid = shortArrayOf()
                var password = shortArrayOf()
///////////////set ssid and password here///////////////////
                for(i in 0..19){
                    if(i< ssidstring.size) ssid = ssid.plus(ssidstring[i].toShort())
                    else ssid = ssid.plus(0)

                    if(i< pwdstring.size) password = password.plus(pwdstring[i].toShort())
                    else password = password.plus(0)
                }
///////////////////////////////////////////////////////////
                if(ssid.size == 20 && password.size == 20){
                    val cmdconstructor =  msg_wifi_set_ssid_password(ssid,password)
                    mavpac = cmdconstructor.pack().encodePacket()
                }
                else return false
                ssidfiletmp.deleteRecursively()
                ssidfiletmp.createNewFile()
                ssidjson.put("ssid",et_ssid.text.toString())
                ssidjson.put("password",et_wifipwd.text.toString())
                ssidfiletmp.appendText(ssidjson.toString() + "\n")

                Toast.makeText(
                        this@Options,
                        "ssid set successful",
                        Toast.LENGTH_SHORT
                ).show()

            }/////32 set wifi ssid
            CMD_SET_IP_PWD->{
                val ipstring = ("mqtt://" + mqttjson.getString("server")).toByteArray(Charsets.US_ASCII)
                val userstring = mqttjson.getString("mqttuser").toByteArray(Charsets.US_ASCII)
                val pwdstring = mqttjson.getString("mqttpwd").toByteArray(Charsets.US_ASCII)

                var ip = shortArrayOf()
                var user = shortArrayOf()
                var password = shortArrayOf()

////////////////////set ip user password here//////////////
                for(i in 0..19){
                    if(i< ipstring.size) ip = ip.plus(ipstring[i].toShort())
                    else ip = ip.plus(0)

                    if(i< userstring.size) user = user.plus(userstring[i].toShort())
                    else user = user.plus(0)

                    if(i<pwdstring.size) password = password.plus(pwdstring[i].toShort())
                    else password = password.plus(0)
                }

                for(i in 20..29){
                    if(i< ipstring.size) ip = ip.plus(ipstring[i].toShort())
                    else ip =  ip.plus(0)
                }
///////////////////////////////////////////////////////////
                if(ip.size == 30 && user.size == 20  && password.size == 20){
                    val cmdconstructor =  msg_mqtt_set_ip_password(ip,user,password)
                    mavpac = cmdconstructor.pack().encodePacket()
                }
                else return false
                Toast.makeText(
                        this@Options,
                        "mqtt set successful",
                        Toast.LENGTH_SHORT
                ).show()

            }/////33 set mqtt ip password
            CMD_SET_WIFI_CONNECT->{
                val cmdconstructor =  msg_connect(3.toShort())
                mavpac = cmdconstructor.pack().encodePacket()
            }//////34 reconnect wifi router and mqttbroker
        }
        Log.d("sendcommand mavlink",mavpac.toString())

        if (CHARACTERISTIC_COMMAND!= null) {
            var ch_cmd = false
            while(!ch_cmd) {
                CHARACTERISTIC_COMMAND?.setValue(mavpac)
                ch_cmd = mgatt!!.writeCharacteristic(CHARACTERISTIC_COMMAND)
                if (ch_cmd) {
                    Log.e("sendcommand", "start_send")
                }
                else {
                    Log.e("sendcommand", "start_sendfailed!!")
                    Thread.sleep(200)
                }
            }
            return true
        }
        else{
            Log.e("sendcommand", "cmd is null")
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)
        tv_connected_device = findViewById(R.id.tv_connected_device)
        tv_serverip = findViewById(R.id.tv_serverip)
        tv_mqttuser = findViewById(R.id.tv_mqttuser)
        tv_mqttpwd = findViewById(R.id.tv_mqttpwd)
        et_ssid = findViewById(R.id.et_ssid)
        et_wifipwd = findViewById(R.id.et_wifipwd)



        try {
            val ssidtmp = ssidfiletmp.readText()
            ssidjson = JSONObject(ssidtmp)
        }
        catch(e : Exception){
            Log.d("main",e.message!!)

            //mqttfile?.createNewFile()
            ////write default mqtt server file
            val reader = BufferedReader(InputStreamReader(getAssets().open(defaultssidfile), "UTF-8"))
            var mLine = reader.readLine()
            var wifilist = arrayListOf<String>()
            while (mLine != null) {
                if(mLine != "null" ) wifilist.add(mLine)
                //process line
                try{ mLine = reader.readLine()}
                catch(e: IOException){
                }
            }

            ssidjson.put("ssid", wifilist[0])
            ssidjson.put("password", wifilist[1])
            ssidfiletmp.appendText(ssidjson.toString() + "\n")
        }



        et_ssid.setText(ssidjson.getString("ssid"))
        et_wifipwd.setText(ssidjson.getString("password"))

        tv_connected_device.text = SavedBleAddr
        tv_serverip.text = mqttjson.getString("server")
        tv_mqttuser.text = mqttjson.getString("mqttuser")
        tv_mqttpwd.text  = mqttjson.getString("mqttpwd")
    }

    fun clickblereset(view: View) {
        try{
            blefile!!.delete()
            Toast.makeText(this@Options, "ble device profile cleared, next time will not auto connect again!", Toast.LENGTH_SHORT).show()

        }
        catch(e :Exception){
            Log.e("options","didn't bond any device yet!")
        }

    }

    fun clickserversetting(view: View) {
        val intent = Intent(this, change_new_server::class.java)
        startActivityForResult(intent, 99)
    }////call change server

    fun clickreconnect(view: View) {
        if(ble_cnt){
            send_commandbyBle(CMD_SET_WIFI_CONNECT)
        }
        else{
            Toast.makeText(
                this@Options,
                "Not connect to ble device yet",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun clicksetserver(view: View) {
        if(ble_cnt){
            send_commandbyBle(CMD_SET_IP_PWD)

            ////////////refresh new mqtt setting
            var mqttfile = File(storagePath,mqttserverfile)
            mqttfile.createNewFile()
            ////write default mqtt server file
            mqttfile.appendText(mqttjson.toString() + "\n")

            serverURL = mqttjson.getString("server")
            mqttuser  = mqttjson.getString("mqttuser")
            mqttpwd =   mqttjson.getString("mqttpwd")
        }
        else{
            Toast.makeText(
                this@Options,
                "Not connect to ble device yet",
                Toast.LENGTH_SHORT
            ).show()
        }
    }////click ble set to new server
    fun clicksetssid(view: View) {
        if(ble_cnt){
            send_commandbyBle(CMD_SET_SSID)

        }
        else{
            Toast.makeText(
                this@Options,
                "Not connect to ble device yet",
                Toast.LENGTH_SHORT
            ).show()
        }
    }////click ble set to new wifi
    fun clickback(view: View) {

        setResult(RESULT_OK)
        finish()
    }
    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            when(requestCode){
                99->{
                    tv_serverip.text = mqttjson.getString("server")
                    tv_mqttuser.text = mqttjson.getString("mqttuser")
                    tv_mqttpwd.text  = mqttjson.getString("mqttpwd")
                }///change server
            }

        }
    }



}