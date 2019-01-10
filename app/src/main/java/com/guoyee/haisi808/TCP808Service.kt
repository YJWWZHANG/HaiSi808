package com.guoyee.haisi808

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.common.primitives.Bytes
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.io.OutputStream
import java.net.*
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import kotlin.experimental.and

/**
 *创建时间:2019/1/9 16:10
 */
class TCP808Service: Service() {

    private lateinit var mSingleThreadExecutor: ExecutorService
    private val mTcpPort: Int = 8888
    private val mUdpPort: Int = 9999

    private var mCmdQueue: LinkedBlockingQueue<ByteArray> = LinkedBlockingQueue()


    private var mIp = "192.168.1.99"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        TcpReceiveThread().start()
        UdpSendThread().start()
        mSingleThreadExecutor = Executors.newSingleThreadExecutor()
        LogUtils.e("服务开启")

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    inner class UdpSendThread: Thread() {
        private lateinit var mUdpSocket: DatagramSocket
        private lateinit var mIpAddress: InetAddress
        private var mData = ""

        override fun run() {
            super.run()
            mUdpSocket = DatagramSocket(mUdpPort)
            mUdpSocket.broadcast = true
            mIpAddress = InetAddress.getByName("255.255.255.255")
            mData = NetworkUtils.getIPAddress(true) + ":" + mTcpPort
            LogUtils.e(mData)
            ToastUtils.showLong(mData)
            mUdpSocket.send(DatagramPacket(mData.toByteArray(), mData.length, mIpAddress, mUdpPort))
            mUdpSocket.close()
        }
    }

    inner class TcpReceiveThread: Thread() {

        private lateinit var mTcpServerSocket: ServerSocket

        override fun run() {
            super.run()
            mTcpServerSocket = ServerSocket(mTcpPort)
            while (!isInterrupted) {
                try {
                    val socket = mTcpServerSocket.accept()
                    LogUtils.e("客户端已经成功连接")
                    ToastUtils.showLong("客户端已经成功连接")
                    Thread(Runnable {
                        try {
                            CmdParseThread(socket).start()
                            while (!socket.isClosed) {
                                val buf = ByteArray(1024)
                                val len = socket.getInputStream().read(buf)
//                                LogUtils.e(ConvertUtils.bytes2HexString(buf))
//                                ToastUtils.showLong(ConvertUtils.bytes2HexString(buf))
                                if (len > 0) {
                                    val byt = ByteArray(len)
                                    System.arraycopy(buf, 0, byt, 0, len)
                                    mCmdQueue.offer(byt)
                                }
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

    inner class CmdParseThread(private var socket: Socket) : Thread() {

        private var mCmdBufferList: ArrayList<Byte> = ArrayList()

        override fun run() {
            super.run()
            LogUtils.e("TCP808命令解析线程已开启")
            while (true) {
                if (mCmdQueue.size > 0) {
                    val poll = mCmdQueue.poll()
                    if (poll == null) {
                        LogUtils.e("空")
                        continue
                    }

                    mCmdBufferList.addAll(poll.toTypedArray())

                    if (mCmdBufferList.size in 0..17) {
                        continue
                    }
                    if (Bytes.toArray(mCmdBufferList)[0] == 0x7e.toByte()) {
                        var pkgLen: Int
                        var headLen: Int
                        var msgLen = ConvertUtilsPlus.short2Unsigned(Bytes.toArray(mCmdBufferList)[4].toShort())
                        msgLen += ConvertUtilsPlus.short2Unsigned((Bytes.toArray(mCmdBufferList)[3] and 0x03.toByte()).toShort()) shl 8
                        var pkgType = ConvertUtilsPlus.short2Unsigned(Bytes.toArray(mCmdBufferList)[3].toShort()) shr 4
                        pkgType = pkgType and 0x01

                        headLen = if (pkgType == 0) {
                            13
                        } else {
                            17
                        }
                        pkgLen = headLen + msgLen + 2
                        if (pkgLen > mCmdBufferList.size) {
                            continue
                        }
                        val pkg = ByteArray(pkgLen)
                        System.arraycopy(Bytes.toArray(mCmdBufferList), 0, pkg, 0, pkg.size)
                        LogUtils.e(ConvertUtils.bytes2HexString(pkg))
                        ToastUtils.showShort(ConvertUtils.bytes2HexString(pkg))

                        val msgId = byteArrayOf(pkg[1], pkg[2])
                        if (ConvertUtils.bytes2HexString(msgId) == "0100") {
                            mSingleThreadExecutor.execute(TcpSend8100Thread(socket.getOutputStream()))
                        }
                        if (ConvertUtils.bytes2HexString(msgId) == "0102") {
                            mSingleThreadExecutor.execute(TcpSend8001Thread(socket.getOutputStream()))

                            mSingleThreadExecutor.execute(TcpSend9101Video1(socket.getOutputStream()))
                            mSingleThreadExecutor.execute(TcpSend9101Video2(socket.getOutputStream()))
                            mSingleThreadExecutor.execute(TcpSend9101Video3(socket.getOutputStream()))
                            mSingleThreadExecutor.execute(TcpSend9101Video4(socket.getOutputStream()))
                        }

                        var encryptType = ConvertUtilsPlus.short2Unsigned(pkg[3].toShort()) shr 1
                        encryptType = encryptType and 0x07


                        val phone = byteArrayOf(pkg[5], pkg[6], pkg[7], pkg[8], pkg[9], pkg[10])

                        val msgNum = byteArrayOf(pkg[11], pkg[12])


                        val msgBody = ByteArray(msgLen)
                        System.arraycopy(pkg, headLen, msgBody, 0, msgLen)

                        val checkCode = pkg[headLen + msgLen]



//                        LogUtils.e("" + ConvertUtils.bytes2HexString(msgId) + " " + ConvertUtils.bytes2HexString(phone) +
//                                " " + msgLen + " " + encryptType + " " + pkgType + " " + ConvertUtils.bytes2HexString(msgNum) +
//                                " " + ConvertUtils.bytes2HexString(msgBody) + " " + Integer.toHexString(checkCode.toInt()))


                        val array = ByteArray(mCmdBufferList.size - pkgLen)
                        System.arraycopy(Bytes.toArray(mCmdBufferList), pkgLen, array, 0, array.size)

                        mCmdBufferList.clear()
                        mCmdBufferList.addAll(array.toTypedArray())
                    } else {
                        val bytes2HexString = ConvertUtils.bytes2HexString(Bytes.toArray(mCmdBufferList))
                        val list = bytes2HexString.split("7e")
//                        LogUtils.e(mVideoBufferList.size.toString() + "----" + bytes2HexString + "----" + list[0] + "----" + list[1])
                        LogUtils.e("清空:" + bytes2HexString.length + " " + list[0].length)
                        mCmdBufferList.clear()
                        mCmdBufferList.addAll(ConvertUtils.hexString2Bytes("7e" + list[1]).toTypedArray())
                    }
                }
            }
        }
    }


    inner class TcpSend9101Video1(var outputStream: OutputStream) : Thread() {
        private var mList: ArrayList<Byte> = ArrayList()
        init {
            mList.add(0x7e)
            mList.addAll(ConvertUtils.hexString2Bytes("91010005013900139001001D").toTypedArray())
            mList.add(NetworkUtils.getIPAddress(true).length.toByte())
//            mList.add(mIp.length.toByte())
            mList.addAll(NetworkUtils.getIPAddress(true).toByteArray().toTypedArray())
//            mList.addAll(mIp.toByteArray().toTypedArray())
            mList.addAll(ConvertUtils.hexString2Bytes("1388").toTypedArray())
            mList.addAll(ConvertUtils.hexString2Bytes("1388").toTypedArray())
            mList.add(0x01)/*逻辑通道号*/
            mList.add(0x01)/*数据类型*/
            mList.add(0x00)/*码流类型*/
            mList.add(0x00)/**/
            mList.add(0x7e)
            mList[4] = (mList.size - 15).toByte()
            mList[mList.size - 2] = ConvertUtilsPlus.getXOR(mList)/**/
        }
        override fun run() {
            super.run()
            LogUtils.e("已发送数据：" + ConvertUtils.bytes2HexString(Bytes.toArray(mList)))
            LogUtils.e("已发送数据：" + String(Bytes.toArray(mList)))
            outputStream.write(Bytes.toArray(mList))
        }
    }

    inner class TcpSend9101Video2(var outputStream: OutputStream) : Thread() {
        private var mList: ArrayList<Byte> = ArrayList()
        init {
            mList.add(0x7e)
            mList.addAll(ConvertUtils.hexString2Bytes("91010005013900139001001D").toTypedArray())
            mList.add(NetworkUtils.getIPAddress(true).length.toByte())
//            mList.add(mIp.length.toByte())
            mList.addAll(NetworkUtils.getIPAddress(true).toByteArray().toTypedArray())
//            mList.addAll(mIp.toByteArray().toTypedArray())
            mList.addAll(ConvertUtils.hexString2Bytes("1389").toTypedArray())
            mList.addAll(ConvertUtils.hexString2Bytes("1389").toTypedArray())
            mList.add(0x02)/*逻辑通道号*/
            mList.add(0x01)/*数据类型*/
            mList.add(0x00)/*码流类型*/
            mList.add(0x00)/**/
            mList.add(0x7e)
            mList[4] = (mList.size - 15).toByte()
            mList[mList.size - 2] = ConvertUtilsPlus.getXOR(mList)/**/
        }

        override fun run() {
            super.run()
            LogUtils.e("已发送数据：" + ConvertUtils.bytes2HexString(Bytes.toArray(mList)))
            LogUtils.e("已发送数据：" + String(Bytes.toArray(mList)))
            outputStream.write(Bytes.toArray(mList))
        }
    }

    inner class TcpSend9101Video3(var outputStream: OutputStream) : Thread() {
        private var mList: ArrayList<Byte> = ArrayList()
        init {
            mList.add(0x7e)
            mList.addAll(ConvertUtils.hexString2Bytes("91010005013900139001001D").toTypedArray())
            mList.add(NetworkUtils.getIPAddress(true).length.toByte())
//            mList.add(mIp.length.toByte())
            mList.addAll(NetworkUtils.getIPAddress(true).toByteArray().toTypedArray())
//            mList.addAll(mIp.toByteArray().toTypedArray())
            mList.addAll(ConvertUtils.hexString2Bytes("138A").toTypedArray())
            mList.addAll(ConvertUtils.hexString2Bytes("138A").toTypedArray())
            mList.add(0x03)/*逻辑通道号*/
            mList.add(0x01)/*数据类型*/
            mList.add(0x00)/*码流类型*/
            mList.add(0x00)/**/
            mList.add(0x7e)
            mList[4] = (mList.size - 15).toByte()
            mList[mList.size - 2] = ConvertUtilsPlus.getXOR(mList)/**/
        }

        override fun run() {
            super.run()
            LogUtils.e("已发送数据：" + ConvertUtils.bytes2HexString(Bytes.toArray(mList)))
            LogUtils.e("已发送数据：" + String(Bytes.toArray(mList)))
            outputStream.write(Bytes.toArray(mList))
        }
    }

    inner class TcpSend9101Video4(var outputStream: OutputStream) : Thread() {
        private var mList: ArrayList<Byte> = ArrayList()
        init {
            mList.add(0x7e)
            mList.addAll(ConvertUtils.hexString2Bytes("91010005013900139001001D").toTypedArray())
            mList.add(NetworkUtils.getIPAddress(true).length.toByte())
//            mList.add(mIp.length.toByte())
            mList.addAll(NetworkUtils.getIPAddress(true).toByteArray().toTypedArray())
//            mList.addAll(mIp.toByteArray().toTypedArray())
            mList.addAll(ConvertUtils.hexString2Bytes("138B").toTypedArray())
            mList.addAll(ConvertUtils.hexString2Bytes("138B").toTypedArray())
            mList.add(0x04)/*逻辑通道号*/
            mList.add(0x01)/*数据类型*/
            mList.add(0x00)/*码流类型*/
            mList.add(0x00)/**/
            mList.add(0x7e)
            mList[4] = (mList.size - 15).toByte()
            mList[mList.size - 2] = ConvertUtilsPlus.getXOR(mList)/**/
        }

        override fun run() {
            super.run()
            LogUtils.e("已发送数据：" + ConvertUtils.bytes2HexString(Bytes.toArray(mList)))
            LogUtils.e("已发送数据：" + String(Bytes.toArray(mList)))
            outputStream.write(Bytes.toArray(mList))
        }
    }

    inner class TcpSend8001Thread(var outputStream: OutputStream) : Thread() {

        private val mCmd = ConvertUtils.hexString2Bytes(("7E 8001 0005 013900139001 001D" +
                "ffff 0102 00" + "207E").replace(" ", ""))

        override fun run() {
            super.run()
//            LogUtils.e("已发送数据：" + ConvertUtils.bytes2HexString(ConvertUtils.hexString2Bytes("7E0100002E7E")))
            LogUtils.e("已发送数据：" + ConvertUtils.bytes2HexString(mCmd))
            outputStream.write(mCmd)
        }
    }

    inner class TcpSend8100Thread(var outputStream: OutputStream) : Thread() {

        private val mCmd = ConvertUtils.hexString2Bytes(("7E 8100 0007 013900139001 001D" +
                "00ff 00 A8888888" + "FE7E").replace(" ", ""))

        override fun run() {
            super.run()
//            LogUtils.e("已发送数据：" + ConvertUtils.bytes2HexString(ConvertUtils.hexString2Bytes("7E0100002E7E")))
            LogUtils.e("已发送数据：" + ConvertUtils.bytes2HexString(mCmd))
            outputStream.write(mCmd)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageSendUpdEvent(sendUdpEvent: SendUdpEvent) {
        mSingleThreadExecutor.execute(UdpSendThread())
    }

}

