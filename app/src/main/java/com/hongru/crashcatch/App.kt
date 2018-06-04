package com.hongru.crashcatch

import android.app.Application
import com.hongru.analysis.ApplicationAgent
import com.hongru.crash.CrashActionModule
import com.hongru.crash.CrashObserver

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
 * @date 2018/6/5
 * 邮箱:peng_hongru@163.com
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        CrashObserver.applyActionModule(object : CrashActionModule {
            override fun finishApplication(thread: Thread?, exception: Throwable?) {
                TODO("完全退出App")
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, exception)
            }

            override fun uploadCrashLogFile(fileName: String) {
                TODO("上传错误日志文件")
            }
        }).applyApplication(object : ApplicationAgent {
            override fun applyApplication(): Application = this@App
        })
    }
}