package com.ble.new_mattress

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import java.io.File

class change_new_server : AppCompatActivity() {
    lateinit var et_serverip : EditText
    lateinit var et_mqttuser : EditText
    lateinit var et_mqttpwd : EditText
    var mqttfile = File(storagePath,mqttserverfile)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_new_server)
        et_serverip = findViewById(R.id.et_serverip)
        et_mqttuser = findViewById(R.id.et_mqttuser)
        et_mqttpwd = findViewById(R.id.et_mqttpwd)

        et_serverip.setText(mqttjson.getString("server"))
        et_mqttuser.setText(mqttjson.getString("mqttuser"))
        et_mqttpwd.setText(mqttjson.getString("mqttpwd"))

    }

    fun clickcancel(view: View) {
        setResult(RESULT_CANCELED)
        finish()
    }
    fun clickapply(view: View) {
        mqttfile.delete()
        mqttfile.createNewFile()
        val new_server = et_serverip.getText().toString()
        val new_user = et_mqttuser.getText().toString()
        val new_pwd = et_mqttpwd.getText().toString()

        mqttjson.put("server",new_server)
        mqttjson.put("mqttuser",new_user)
        mqttjson.put("mqttpwd",new_pwd)

        mqttfile.appendText(mqttjson.toString()+ "\n" )

        setResult(RESULT_OK)
        finish()
    }

}