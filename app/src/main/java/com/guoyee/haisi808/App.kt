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
//        ServiceUtils.startService(VideoService::class.java)
        ServiceUtils.startService(TCP808Service::class.java)
        ServiceUtils.startService(TCP1078Service1::class.java)
        ServiceUtils.startService(TCP1078Service2::class.java)
        ServiceUtils.startService(TCP1078Service3::class.java)
        ServiceUtils.startService(TCP1078Service4::class.java)
    }
}