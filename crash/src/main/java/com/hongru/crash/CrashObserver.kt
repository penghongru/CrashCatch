package com.hongru.crash

import com.hongru.analysis.ApplicationAgent
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission

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
object CrashObserver {

    lateinit var actionModule: CrashActionModule

    fun applyActionModule(actionModule: CrashActionModule): CrashObserver {
        this.actionModule = actionModule
        return this
    }

    fun applyApplication(agent: ApplicationAgent): CrashObserver {
        AndPermission.with(agent.applyApplication())
                .runtime()
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted {
                    CrashObservable.init(agent)
                }
                .onDenied {

                }
                .start()
        return this
    }


}