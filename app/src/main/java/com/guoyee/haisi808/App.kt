package com.guoyee.haisi808

import android.app.Application
import com.blankj.utilcode.util.ServiceUtils
import com.blankj.utilcode.util.Utils

/**
 *创建时间:2018/12/20 16:48
 */
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
        ServiceUtils.startService(VideoService::class.java)
    }
}