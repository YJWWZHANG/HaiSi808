package com.guoyee.haisi808

import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.common.primitives.Bytes
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.util.ArrayList
import java.util.concurrent.LinkedBlockingQueue
import kotlin.experimental.and

/**
 *创建时间:2019/1/9 16:24
 */
class TCP1078Service3: Service() {

    private val mTcpVideoPort: Int = 5002

    private var mVideoQueue: LinkedBlockingQueue<ByteArray> = LinkedBlockingQueue()


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        TcpVideoThread().start()
        LogUtils.e("TCP1078_3服务开启")
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    inner class TcpVideoThread: Thread() {

        private lateinit var mTcpVideoSocket: ServerSocket

        override fun run() {
            super.run()
            mTcpVideoSocket = ServerSocket(mTcpVideoPort)
            while (!isInterrupted) {
                try {
                    val socket = mTcpVideoSocket.accept()
                    LogUtils.e("TCP1078_3视频已经成功连接")
                    ToastUtils.showLong("TCP1078_3视频已经成功连接")
                    VideoParseThread().start()
                    Thread(Runnable {
                        try {
                            while (!socket.isClosed) {
                                val buf = ByteArray(1430)
                                val len = socket.getInputStream().read(buf)
                                if (len > 0) {
                                    val byt = ByteArray(len)
                                    System.arraycopy(buf, 0, byt, 0, len)
                                    mVideoQueue.offer(byt)
                                }
//                                LogUtils.e("接收")
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }).start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    }

    inner class VideoParseThread: Thread() {

        private var mVideoBufferList: ArrayList<Byte> = ArrayList()
        private var mDataBufferList: ArrayList<Byte> = ArrayList()
        private var mFileOutputStream: FileOutputStream

        init {
            val file = File(Environment.getExternalStorageDirectory().absoluteFile.path + "/vvvv6666.mp4")
            mFileOutputStream = FileOutputStream(file, true)
        }

        override fun run() {
            super.run()
            LogUtils.e("TCP1078_3视频解析线程已开启")
            while (true) {
                if (mVideoQueue.size > 0) {
//                    LogUtils.e("TCP1078_1开始")
                    val poll = mVideoQueue.poll()
                    if (poll == null) {
                        LogUtils.e("空")
                        continue
                    }
//                    LogUtils.e(poll.size.toString() + "----" + ConvertUtils.bytes2HexString(poll))


                    mVideoBufferList.addAll(poll.toTypedArray())
//                    LogUtils.e(mVideoBufferList.size.toString() + "----" + ConvertUtils.bytes2HexString(Bytes.toArray(mVideoBufferList)))
//                    LogUtils.e(mVideoQueue.size.toString() + "--------------")
                    if (mVideoBufferList.size in 0..29) {
                        continue
                    }
                    if (Bytes.toArray(mVideoBufferList)[0] == 0x30.toByte() && Bytes.toArray(mVideoBufferList)[1] == 0x31.toByte() &&
                        Bytes.toArray(mVideoBufferList)[2] == 0x63.toByte() && Bytes.toArray(mVideoBufferList)[3] == 0x64.toByte()) {

                        val lenBytes = byteArrayOf(Bytes.toArray(mVideoBufferList)[28], Bytes.toArray(mVideoBufferList)[29])
                        val len = java.lang.Long.parseLong(ConvertUtils.bytes2HexString(lenBytes), 16)

                        val pkgLen = len + 30
                        if (pkgLen > mVideoBufferList.size) {
//                            LogUtils.e("长度不够:$pkgLen " + mVideoBufferList.size + " " + len)
                            continue
                        }

                        val pkg = ByteArray(pkgLen.toInt())
                        System.arraycopy(Bytes.toArray(mVideoBufferList), 0, pkg, 0, pkg.size)
//                        LogUtils.e(ConvertUtils.bytes2HexString(pkg))

                        val data = ByteArray(len.toInt())
                        System.arraycopy(pkg, 30, data, 0, data.size)

                        val binaryString = Integer.toBinaryString(Bytes.toArray(mVideoBufferList)[15].toInt())
                        val tag = Bytes.toArray(mVideoBufferList)[15] and 0x0f
//                        LogUtils.e("$binaryString $tag")
                        when(tag) {
                            0.toByte() -> {     //原子包
                                mDataBufferList.addAll(data.toTypedArray())
                                EventBus.getDefault().post(ShowVideo3Event(Bytes.toArray(mDataBufferList)))
                                mDataBufferList.clear()
//                                LogUtils.e("原子包-显示视频")
                            }
                            1.toByte() -> {     //第一个包
                                mDataBufferList.addAll(data.toTypedArray())
                            }
                            2.toByte() -> {     //最后一个包
                                mDataBufferList.addAll(data.toTypedArray())
                                EventBus.getDefault().post(ShowVideo3Event(Bytes.toArray(mDataBufferList)))
                                mDataBufferList.clear()
//                                LogUtils.e("显示视频")
                            }
                            3.toByte() -> {     //中间包
                                mDataBufferList.addAll(data.toTypedArray())
                            }
                        }

//                        mFileOutputStream.write(data)

//                        LogUtils.e("写入")

                        val array = ByteArray(mVideoBufferList.size - pkgLen.toInt())
                        System.arraycopy(Bytes.toArray(mVideoBufferList), pkgLen.toInt(), array, 0, array.size)

//                        LogUtils.e("" + mVideoBufferList.size + " " + pkgLen + " " + len + " " + array.size)
                        mVideoBufferList.clear()
                        mVideoBufferList.addAll(array.toTypedArray())
//                        mVideoBufferList.removeAll(pkg.toTypedArray())
//                        LogUtils.e("" + mVideoBufferList.size + " " + pkgLen + " " + len)
                    } else {
                        val bytes2HexString = ConvertUtils.bytes2HexString(Bytes.toArray(mVideoBufferList))
                        val list = bytes2HexString.split("30316364")
//                        LogUtils.e(mVideoBufferList.size.toString() + "----" + bytes2HexString + "----" + list[0] + "----" + list[1])
                        LogUtils.e("清空:" + bytes2HexString.length + " " + list[0].length)
                        mVideoBufferList.clear()
                        mVideoBufferList.addAll(ConvertUtils.hexString2Bytes("30316364" + list[1]).toTypedArray())
                    }

                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageSendEmptyEvent(s: String) {

    }

}