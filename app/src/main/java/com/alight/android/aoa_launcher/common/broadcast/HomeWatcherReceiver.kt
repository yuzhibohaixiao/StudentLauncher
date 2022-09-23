package com.alight.android.aoa_launcher.common.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.alight.android.aoa_launcher.activity.NewLauncherActivity
import com.alight.android.aoa_launcher.utils.BtnClickUtil
import com.alight.android.aoa_launcher.utils.CommonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HomeWatcherReceiver : BroadcastReceiver() {
    private val LOG_TAG = "HomeReceiver"
    private val SYSTEM_DIALOG_REASON_KEY = "reason"

    //action内的某些reason
    private val SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps" //home键旁边的最近程序列表键
    private val SYSTEM_DIALOG_REASON_HOME_KEY = "homekey" //按下home键
    private val SYSTEM_DIALOG_REASON_LOCK = "lock" //锁屏键
    private val SYSTEM_DIALOG_REASON_ASSIST = "assist" //某些三星手机的程序列表键
    private var isForeground = false
    private var isClear = false

    @Synchronized
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            if (BtnClickUtil.isFastShow()) return@launch
            val action = intent.action
            isForeground =
                CommonUtil.isForeground(context, NewLauncherActivity::class.java.name)
            Log.i("HomeWatcherReceiver", "Launcher是否在前台: $isForeground")
            //        App app = (App) context.getApplicationContext();
//        Log.i(LOG_TAG, "onReceive: action: $action")
            if (action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) { //Action
                // android.intent.action.CLOSE_SYSTEM_DIALOGS
                val reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY)
                if (SYSTEM_DIALOG_REASON_HOME_KEY == reason) { // 短按Home键
                    if (!isForeground) {
                        backLauncher(context)
                    }
                    //可以在这里实现关闭程序操作。。。
                } else if (SYSTEM_DIALOG_REASON_RECENT_APPS == reason) { //Home键旁边的显示最近的程序的按钮
                    // 长按Home键 或者 activity切换键
                } else if (SYSTEM_DIALOG_REASON_LOCK == reason) {  // 锁屏，似乎是没有反应，监听Intent.ACTION_SCREEN_OFF这个Action才有用
                } else if (SYSTEM_DIALOG_REASON_ASSIST == reason) {   // samsung 长按Home键
                    if (!isForeground) {
                        backLauncher(context)
                    }
                }
            }
        }
    }

    private fun backLauncher(context: Context) {
        var intent = Intent(context, NewLauncherActivity::class.java)
//        if (isClear) {
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
//        } else {
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK) //清除任务栈中的所有activity
//            isClear = true
//        }
        context.startActivity(intent)

        //不关闭Activity退回主界面
      /*  val launcherIntent: Intent =
            Intent(Intent.ACTION_MAIN)
        launcherIntent.addCategory(Intent.CATEGORY_HOME)
        context.startActivity(intent)*/

    }
}