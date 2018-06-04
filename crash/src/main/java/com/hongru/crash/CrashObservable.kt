package com.hongru.crash

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.hongru.analysis.ApplicationAgent
import com.hongru.analysis.ApplicationHolder
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

//<pre>
//                       _oo0oo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                      0\  =  /0
//                    ___/`---'\___
//                  .' \\|     |// '.
//                 / \\|||  :  |||// \
//                / _||||| -:- |||||- \
//               |   | \\\  -  /// |   |
//               | \_|  ''\---/''  |_/ |
//               \  .-\__  '-'  ___/-. /
//             ___'. .'  /--.--\  `. .'___
//          ."" '<  `.___\_<|>_/___.' >' "".
//         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//         \  \ `_.   \_ __\ /__ _/   .-` /  /
//     =====`-.____`.___ \_____/___.-`___.-'=====
//                       `=---='
//
//
//     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
//               佛祖保佑         永无BUG
//</pre>


/**
 *@author 彭鸿儒
 * @date 2018/6/4
 * 邮箱:peng_hongru@163.com
 */
object CrashObservable : Thread.UncaughtExceptionHandler {

    private const val TAG: String = "异常上报"

    var defaultCatchHandler: Thread.UncaughtExceptionHandler? = null

    private val infos: LinkedHashMap<String, String> = LinkedHashMap()

    private val formatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")

    fun init(agent: ApplicationAgent) {
        ApplicationHolder.setAgent(agent)
        defaultCatchHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread?, exception: Throwable?) {
        if (!handleException(exception) && defaultCatchHandler != null) {
            defaultCatchHandler?.uncaughtException(thread, exception)
        } else {
            try {
                Thread.sleep(3000)
            } catch (exception: Throwable) {
                Log.e(TAG, "异常捕获时出错", exception)
            }
            CrashObserver.actionModule.finishApplication(thread, exception)
        }
    }


    private fun handleException(exception: Throwable?): Boolean {
        exception ?: return false
        collectDeviceInfo()
        Thread(Runnable {
            Looper.prepare()
            Toast.makeText(ApplicationHolder.context(), "程序出现异常，即将退出", Toast.LENGTH_SHORT).show()
            Looper.loop()
        }).start()
        saveCatchInfo2File(exception)
        return true
    }


    private fun collectDeviceInfo() {
        try {
            val packageManager = ApplicationHolder.context().packageManager
            val packageInfo: PackageInfo? = packageManager.getPackageInfo(ApplicationHolder.context().packageName, PackageManager.GET_ACTIVITIES)
            if (packageInfo != null) {
                val versionName: String = packageInfo.versionName ?: "null"
                val versionCode: Int = packageInfo.versionCode
                infos["versionName"] = versionName
                infos["versionCode"] = versionCode.toString()
            }
        } catch (exception: Exception) {
            Log.e(TAG, "收集设备信息时出错", exception)
        }
        val fields = Build::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                infos[field.name] = field.get(null).toString()
                Log.d(TAG, "${field.getName()} : ${field.get(null)}")
            } catch (exception: Exception) {
                Log.e(TAG, "收集错误信息时出错", exception)
            }
        }
    }

    private fun saveCatchInfo2File(exception: Throwable): String {
        val buffer = StringBuffer()
        infos.forEach { entry ->
            buffer.append("${entry.key} = ${entry.value}\n")
        }
        val strWriter = StringWriter()
        val writer = PrintWriter(strWriter)
        exception.printStackTrace(writer)
        var cause: Throwable? = exception.cause
        while (cause != null) {
            cause.printStackTrace(writer)
            cause = cause.cause
        }
        writer.close()
        buffer.append(strWriter.toString())
        try {
            val fileName = "crash - time${formatter.format(Date())}.log"
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val path = "${Environment.getExternalStorageDirectory()}/crash/"
                val dir = File(path)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val fos = FileOutputStream(path + fileName)
                fos.write(buffer.toString().toByteArray())
                sendCrashLog2PM(path + fileName)
                fos.close()
            }
            return fileName
        } catch (exception: Exception) {
            Log.e(TAG, "存储错误日志时出错", exception)
        }
        return ""
    }

    private fun sendCrashLog2PM(fileName: String) {
        if (!File(fileName).exists()) {
            Log.e(TAG, "$fileName 错误日志文件不存在")
            return
        }
        // 上传错误日志
        CrashObserver.actionModule.uploadCrashLogFile(fileName)
    }

}