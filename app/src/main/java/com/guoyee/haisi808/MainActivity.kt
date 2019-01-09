package com.guoyee.haisi808

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {

    private lateinit var mMediaCodecUtil1: MediaCodecUtil
    private lateinit var mMediaCodecUtil2: MediaCodecUtil
    private lateinit var mMediaCodecUtil3: MediaCodecUtil
    private lateinit var mMediaCodecUtil4: MediaCodecUtil


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        EventBus.getDefault().register(this)
        btn_send.setOnClickListener {
            EventBus.getDefault().post(SendUdpEvent())
        }

        surface_view_1.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                mMediaCodecUtil1 =  MediaCodecUtil(holder)
                mMediaCodecUtil1.startCodec()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                mMediaCodecUtil1.stopCodec()
            }

        })

        surface_view_2.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                mMediaCodecUtil2 =  MediaCodecUtil(holder)
                mMediaCodecUtil2.startCodec()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                mMediaCodecUtil2.stopCodec()
            }

        })

        surface_view_3.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                mMediaCodecUtil3 =  MediaCodecUtil(holder)
                mMediaCodecUtil3.startCodec()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                mMediaCodecUtil3.stopCodec()
            }

        })

        surface_view_4.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                mMediaCodecUtil4 =  MediaCodecUtil(holder)
                mMediaCodecUtil4.startCodec()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                mMediaCodecUtil4.stopCodec()
            }

        })

    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageshowVideo1Event(showVideo1Event: ShowVideo1Event) {
        mMediaCodecUtil1.onFrame(showVideo1Event.data, 0, showVideo1Event.data.size)
//        mMediaCodecUtil2.onFrame(showVideo1Event.data, 0, showVideo1Event.data.size)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageshowVideo2Event(showVideo2Event: ShowVideo2Event) {
        mMediaCodecUtil2.onFrame(showVideo2Event.data, 0, showVideo2Event.data.size)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageshowVideo3Event(showVideo3Event: ShowVideo3Event) {
        mMediaCodecUtil3.onFrame(showVideo3Event.data, 0, showVideo3Event.data.size)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageshowVideo4Event(showVideo4Event: ShowVideo4Event) {
        mMediaCodecUtil4.onFrame(showVideo4Event.data, 0, showVideo4Event.data.size)
    }


}
