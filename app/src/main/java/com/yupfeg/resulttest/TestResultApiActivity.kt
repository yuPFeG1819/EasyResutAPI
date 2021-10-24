package com.yupfeg.resulttest

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf

class TestResultApiActivity : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_result_api)
        findViewById<View>(R.id.btn_test_result_api_normal_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_test_result_api_result_data).setOnClickListener {
            resultDataBack()
        }
    }

    private fun resultDataBack(){
        val intent = Intent().apply {
            putExtras(bundleOf("key" to "test1"))
        }
        setResult(RESULT_OK,intent)
        finish()
    }

}