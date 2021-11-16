package com.ble.new_mattress

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.system.exitProcess

class Terms : AppCompatActivity() {

    lateinit var reader : BufferedReader;
    var mLine : String ?=null;
    var termbuffer :String ="";
    lateinit var term_text : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)
        term_text = findViewById(R.id.term_text)

        try {

            reader =BufferedReader(InputStreamReader(getAssets().open("flasknote.txt"), "UTF-8"))

            // do reading, usually loop until end of file reading
            //reader.lines()

            mLine = reader.readLine()
            while (mLine != null) {
                if(mLine != "null" )termbuffer += mLine

                //process line
                try{ mLine = reader.readLine()}
                catch(e:IOException){

                }
            }

            term_text.text = termbuffer

        } catch (e : IOException) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (e :IOException) {

                    //log the exception
                }
            }
        }

    }

    fun clickagree(view: View) {
        finish()
    }
    fun clickdisagree(view: View) {
        moveTaskToBack(true);
        exitProcess(-1)
    }
}