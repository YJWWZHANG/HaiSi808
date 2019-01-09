package com.guoyee.haisi808

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import com.blankj.utilcode.util.*
import com.google.common.primitives.Bytes
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileWriter
import kotlin.experimental.xor

class MainActivity : AppCompatActivity() {

    private lateinit var mMediaCodecUtil: MediaCodecUtil


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        EventBus.getDefault().register(this)
        btn_send.setOnClickListener {
            EventBus.getDefault().post(SendUdpEvent())
        }

        surface_view.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                mMediaCodecUtil =  MediaCodecUtil(holder)
                mMediaCodecUtil.startCodec()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                mMediaCodecUtil.stopCodec()
            }

        })

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageSendVideoEvent(sendVideoEvent: SendVideoEvent) {
        mMediaCodecUtil.onFrame(sendVideoEvent.data, 0, sendVideoEvent.data.size)
    }


}
