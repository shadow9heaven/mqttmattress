package com.ble.new_mattress

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    var device_find = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent2 = Intent(this, Terms::class.java)
        //intent.putExtra("mgatt", mgatt)
        startActivityForResult(intent2,8)

        setContentView(R.layout.activity_main)

    }

    fun clickbluetooth(view: View) {


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
        if(device_find){
            val intent = Intent(this, bed_adjust::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("device_find", device_find)
            startActivityForResult(intent, 2)
        }
        else {
            Toast.makeText(this,"請先連接藍芽裝置",Toast.LENGTH_SHORT)
        }
    }
}