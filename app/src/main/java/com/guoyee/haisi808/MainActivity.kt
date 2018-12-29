package com.guoyee.haisi808

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import com.blankj.utilcode.util.*
import com.google.common.primitives.Bytes
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileWriter
import kotlin.experimental.xor

class MainActivity : AppCompatActivity() {
    private val mCmd = ConvertUtils.hexString2Bytes(("7E 8100 0007 013900139001 001D" +
            "00ff 00 A8888888" + "FE7E").replace(" ", ""))
    private val mTest = ConvertUtils.hexString2Bytes("81000007013900139001001D00ff00A8888888")

    private var mList: ArrayList<Byte> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_send.setOnClickListener {
            EventBus.getDefault().post(SendUdpEvent())

            LogUtils.e(NetworkUtils.getIPAddress(true) + " " + Integer.toHexString(NetworkUtils.getIPAddress(true).length))

            val bytes = ByteArray(1)
            bytes[0] = ConvertUtilsPlus.getXOR(mTest)
            LogUtils.e(ConvertUtils.bytes2HexString(bytes))

            mList.add(0x7e)
            mList.addAll(ConvertUtils.hexString2Bytes("91010005013900139001001D").toTypedArray())
            mList.add(NetworkUtils.getIPAddress(true).length.toByte())
            mList.addAll(NetworkUtils.getIPAddress(true).toByteArray().toTypedArray())

            LogUtils.e(ConvertUtils.bytes2HexString(Bytes.toArray(mList)))
            LogUtils.e(String(Bytes.toArray(mList)))

//            FileUtils.getFileByPath(Environment.getExternalStorageDirectory().absoluteFile.path + "/test2333.txt")
            val file = File(Environment.getExternalStorageDirectory().absoluteFile.path + "/test2333.txt")
            FileWriter(file, true).write("hello")
        }
    }

}
