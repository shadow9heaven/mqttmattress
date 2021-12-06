package com.ble.new_mattress

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity

class version : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_version)
        //setSupportActionBar(findViewById(R.id.toolbar))
        val tv_version = findViewById<TextView>(R.id.tv_version)
        val strtmp = this.getString(R.string.version) + " "+ BuildConfig.VERSION_NAME
        tv_version.text = strtmp
    }

    fun backtoprev(view: View) {
        finish()
    }

}