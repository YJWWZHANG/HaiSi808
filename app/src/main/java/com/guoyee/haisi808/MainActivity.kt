package com.guoyee.haisi808

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import kotlin.experimental.xor

class MainActivity : AppCompatActivity() {
    private val mCmd = ConvertUtils.hexString2Bytes(("7E 8100 0007 013900139001 001D" +
            "00ff 00 A8888888" + "FE7E").replace(" ", ""))
    private val mTest = ConvertUtils.hexString2Bytes("81000007013900139001001D00ff00A8888888")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_send.setOnClickListener {
            EventBus.getDefault().post(SendUdpEvent())
            val bytes = ByteArray(1)
            bytes[0] = ConvertUtilsPlus.getXOR(mTest)
            LogUtils.e(ConvertUtils.bytes2HexString(bytes))
        }
    }

}
