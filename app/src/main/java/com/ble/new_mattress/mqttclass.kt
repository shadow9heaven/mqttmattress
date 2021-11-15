package com.ble.mqttexample

import MAVLink.MAVLinkPacket
import MAVLink.Messages.MAVLinkMessage
import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import android.net.Uri;
import MAVLink.Parser;
import MAVLink.bluetooth.msg_ble_ack
import MAVLink.bluetooth.msg_connect
import MAVLink.bootloader.msg_bl_command
import MAVLink.bootloader.msg_bl_ota
import MAVLink.smartmattress.*
import com.ble.new_mattress.FLAG_MATTRESS_ACK
import com.ble.new_mattress.FLAG_WIFI_CONNECT
import com.ble.new_mattress.bed_pressure

@ExperimentalUnsignedTypes
class MqttClass {
    var mqttClient :MqttAndroidClient? = null
    val TAG = "MqttClass"


    fun connect(context: Context, URL : String, clientID : String, password : String) {
        //val serverURI = "tcp://broker.emqx.io:1883"
        //val uri = Uri.parse(URL)

        mqttClient = MqttAndroidClient(context, URL, clientID)
        //mvlnk_par.mavlink_parse_char(30)
        mqttClient!!.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                val message2UByte = message!!.payload

                //Log.d(TAG, "Receive message2ubyte: ${message2UByte} from topic: $topic")
                //val parsedByteArray =
                var mvlnk_partest = Parser()
                var parsertmp :MAVLinkPacket ?= null
                for(i in message2UByte){
                    val res = mvlnk_partest.mavlink_parse_char(i.toInt())
                    if(res != null)parsertmp = res
                }
                //Log.d(TAG, "Receive message2ubyte: ${parsertmp!!.payload.payload.array()} from topic: $topic")

                /*unpack*/
                if(parsertmp != null){
                    when (parsertmp.msgid){
                        msg_bl_command.MAVLINK_MSG_ID_BL_COMMAND->{

                        }//////2

                        msg_bl_ota.MAVLINK_MSG_ID_BL_OTA->{
                            Log.d("parse result","54-MAVLINK_MSG_ID_MEDITATION")
                            val respac : msg_adjust_hardness  = msg_adjust_hardness()
                            respac.unpack(parsertmp.payload)
                        }/////3

                        msg_ble_ack.MAVLINK_MSG_ID_BLE_ACK->{
                            val respac : msg_ble_ack  = msg_ble_ack()
                            respac.unpack(parsertmp.payload)
                            val ack = respac.ack_code

                            Log.d("parse result","30-MAVLINK_MSG_ID_BLE_ACK : " +ack.toString())
                        }//////30
                        msg_connect.MAVLINK_MSG_ID_CONNECT->{
                            Log.d("parse result","54-MAVLINK_MSG_ID_MEDITATION")
                            val respac : msg_adjust_hardness  = msg_adjust_hardness()
                            respac.unpack(parsertmp.payload)
                        }/////34

                        ////for smart ress
                        msg_mattress_ack.MAVLINK_MSG_ID_MATTRESS_ACK->{

                            FLAG_MATTRESS_ACK = true
                            val respac : msg_mattress_ack  = msg_mattress_ack()
                            respac.unpack(parsertmp.payload)
                            val ack = respac.ack_code

                            Log.d("parse result","51-MAVLINK_MSG_ID_MATTRESS_ACK : " +ack.toString())

                        }/////51
                        msg_adjust_hardness.MAVLINK_MSG_ID_ADJUST_HARDNESS->{
                            val respac : msg_adjust_hardness  = msg_adjust_hardness()
                            respac.unpack(parsertmp.payload)
                            val pktype = respac.pk_type
                            val level  = respac.level
                            val pos = respac.pos
                            val sub_bed = respac.sub_bed

                            Log.d("parse result","52-MAVLINK_MSG_ID_ADJUST_HARDNESS "+ sub_bed.toString() +"-"+pos.toString() +" : " +level.toString() )

                        }/////52

                        msg_relieve_stress.MAVLINK_MSG_ID_RELIEVE_STRESS->{
                            Log.d("parse result","53-MAVLINK_MSG_ID_RELIEVE_STRESS")
                            val respac : msg_relieve_stress  = msg_relieve_stress()
                            respac.unpack(parsertmp.payload)

                        }/////53

                        msg_meditation.MAVLINK_MSG_ID_MEDITATION->{
                            Log.d("parse result","54-MAVLINK_MSG_ID_MEDITATION")
                            val respac : msg_meditation  = msg_meditation()
                            respac.unpack(parsertmp.payload)

                        }/////54

                        msg_connection.MAVLINK_MSG_ID_CONNECTION->{
                            Log.d("parse result","55-MAVLINK_MSG_ID_CONNECTION")
                            val respac : msg_connection  = msg_connection()
                            respac.unpack(parsertmp.payload)

                        }/////55

                        ///////56 for pressure
                        msg_pressure.MAVLINK_MSG_ID_PRESSURE->{
                            val respac : msg_pressure  =  msg_pressure()
                            respac.unpack(parsertmp.payload)
                            val pressure = respac.pressure
                            val timestamp = respac.timestamp

                            pressure.copyInto(bed_pressure,0,2,22)

                            //Log.d("parse result","56-MAVLINK_MSG_ID_PRESSURE")
                            //Log.d("pressure",pressure.asList().toString())
                            //Log.d("timestamp",timestamp.toString())
                        }///pressure 56

                        msg_pump_status.MAVLINK_MSG_ID_PUMP_STATUS->{

                            Log.d("parse result","57-MAVLINK_MSG_ID_PUMP_STATUS")
                            val respac : msg_pump_status  =  msg_pump_status()
                            respac.unpack(parsertmp.payload)
                            val status = respac.status
                            val timestamp = respac.timestamp

                            Log.d("status", status.asList().toString())
                            //Log.d("timestamp",timestamp.toString())
                        }///pump status 57

                        msg_step_status.MAVLINK_MSG_ID_STEP_STATUS->{

                            Log.d("parse result","58-MAVLINK_MSG_ID_step_STATUS")
                            val respac : msg_step_status  =  msg_step_status()
                            respac.unpack(parsertmp.payload)
                            val status = respac.status
                            val position = respac.position
                            val timestamp = respac.timestamp

                            Log.d("status", status.asList().toString())
                            Log.d("position", position.asList().toString())
                            //Log.d("timestamp",timestamp.toString())
                        }///step status 58

                        msg_request_data.MAVLINK_MSG_ID_REQUEST_DATA->{
                            Log.d("parse result","59-request_data")
                        }/////59

                        msg_control_pump.MAVLINK_MSG_ID_CONTROL_PUMP->{

                            Log.d("parse result","60-control_pump")
                            val respac : msg_control_pump =  msg_control_pump()
                            respac!!.unpack(parsertmp.payload)


                        }/////60

                        msg_control_step.MAVLINK_MSG_ID_CONTROL_STEP->{

                            Log.d("parse result","61-control_step")
                            val respac : msg_control_step =  msg_control_step()
                            respac!!.unpack(parsertmp.payload)

                        }////61
                    }/////when msgid

                }////if parse is not null
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })

        val options = MqttConnectOptions()
        options.connectionTimeout = 30
        options.keepAliveInterval = 120
        options.isAutomaticReconnect = true
        options.isCleanSession = true
        options.userName = clientID
        options.password = password.toCharArray()


        try {
            mqttClient!!.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")
                    FLAG_WIFI_CONNECT = true
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG,asyncActionToken.toString())
                    Log.d(TAG, exception.toString())
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }


    fun subscribe(topic: String, qos: Int = 1) {
        try {
            mqttClient!!.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Subscribed to $topic")

                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to subscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unsubscribe(topic: String) {
        try {
            mqttClient!!.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Unsubscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to unsubscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic: String, msg: MqttMessage, qos: Int = 1, retained: Boolean = false) {
        try {
            mqttClient!!.publish(topic, msg, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, msg.toString() + " published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish $msg to $topic")
                }
            } )
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            mqttClient!!.unregisterResources()
            mqttClient!!.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Disconnected")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to disconnect")
                }
            })

        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}