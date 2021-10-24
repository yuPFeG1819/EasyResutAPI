package com.yupfeg.resulttest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.yupfeg.resulttest.base.bindingActivity
import com.yupfeg.resulttest.databinding.ActivityTestResultApiBinding

class TestResultApiActivity : AppCompatActivity(){

    private val mBinding : ActivityTestResultApiBinding by bindingActivity(R.layout.activity_test_result_api)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.config = BindingConfig()
    }

    inner class BindingConfig{

        fun normalBack(){
            finish()
        }

        fun resultDataBack(){
            val intent = Intent().apply {
                putExtras(bundleOf("key" to "test1"))
            }
            setResult(RESULT_OK,intent)
            finish()
        }
    }
}