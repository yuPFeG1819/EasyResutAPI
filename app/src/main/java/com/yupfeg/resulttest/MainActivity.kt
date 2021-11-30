package com.yupfeg.resulttest

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView(){
        findViewById<View>(R.id.btn_main_normal_use).setOnClickListener {
            startActivity(Intent(this,NormalUseActivity::class.java))
        }

        findViewById<View>(R.id.btn_main_coroutine_use).setOnClickListener {
            startActivity(Intent(this,CoroutineUseActivity::class.java))
        }
    }

}