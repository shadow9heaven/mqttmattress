package com.ble.new_mattress

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.Settings
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap


object CrashLogCatch {
    const val THREAD_NAME_MAIN = "com.ble.drive_status_cntl"
    const val THREAD_NAME_REMOTE = "com.ble.drive_status_cntl:remote_service"
    //private var storagePath: File? = null

    fun initCrashLog(context: Context) {
        val oriHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            try {
                val buffer = StringBuilder()
                buffer.append("""
    ${getCurProcessName(context)}
    """.trimIndent())

                buffer.append("uncaught exception at ")
                        .append(Date(System.currentTimeMillis()))
                        .append("\n")
                buffer.append(" "+e.message +" " + e.localizedMessage +"\n")
                for(i in e.cause!!.stackTrace){
                    buffer.append(i.toString() +"\n")
                }

                //val log: String = HttpLogController.getInstance().makeCrashLog(buffer.toString())

                getExceptionLog(buffer.toString())

                //SdLog.dFileAlways("crash" + System.currentTimeMillis() + ".log", log)

                if (BuildConfig.DEBUG) {
                    oriHandler.uncaughtException(thread, e)
                } else {
                    val threadName = thread.name
                    if (threadName == THREAD_NAME_REMOTE) {
                        Process.killProcess(Process.myPid())
                    } else if (threadName == THREAD_NAME_MAIN) {
                        oriHandler.uncaughtException(thread, e)
                    }
                }
            } catch (ex: Exception) {
            }
        }
    }


    private fun getCurProcessName(context: Context): String {
        try {
            val pid = Process.myPid()
            val mActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (appProcess in mActivityManager.runningAppProcesses) {
                if (appProcess.pid == pid) {
                    return appProcess.processName
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }


    private fun getExceptionLog(log: String) {
        try {
            Log.e("unhandledexception",log)
            var filePath: String = Environment.getExternalStorageDirectory().absolutePath +
                                   "/Android/data/com.ble.new_mattress"
            if(BuildConfig.BUILD_TYPE == "debug")filePath += ".debug/"
            ///this.getExternalFilesDir(null)

            var file = File(filePath,"crashlogcat.txt")

            file.createNewFile()
            file.appendText(log, Charset.defaultCharset())
            filePath += "crash" + System.currentTimeMillis() + ".log"
            Log.e("logpath",filePath)


            //file.createNewFile();
            var proc = Runtime.getRuntime().exec(arrayOf<String>("logcat", "-f", filePath))


            //var bufferedReader = BufferedReader(InputStreamReader(proc.getInputStream()));
            //var logg :StringBuilder  = StringBuilder();
            //var line = bufferedReader.readLine()

            //Log.e("log start...", line);
            //while (line != null) {
            //    Log.e("log cat task...", line);
            //    logg.append(line);
                    //publishProgress(log.toString());

                    //to write logcat in text file
            //    val fOut : FileOutputStream =  FileOutputStream(file);
            //    val osw : OutputStreamWriter=  OutputStreamWriter(fOut);

                    // Write the string to the file
             //   osw.write(logg.toString());
             //   osw.flush();
             //   osw.close();

             //   Thread.sleep(10)
             //   line = bufferedReader.readLine()
            //}


        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e("error",e.message!!)
        }
    }
}