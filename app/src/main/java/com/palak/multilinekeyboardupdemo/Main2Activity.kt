package com.palak.multilinekeyboardupdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main2.*

class Main2Activity : AppCompatActivity() {


    private lateinit var multiLineKeyboardHeightAdjuster: MultiLineKeyboardHeightAdjuster

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        multiLineKeyboardHeightAdjuster = MultiLineKeyboardHeightAdjuster(this)
        rootView.post { multiLineKeyboardHeightAdjuster.start(scroll, rootView, 0, 0) }
    }

    override fun onDestroy() {
        super.onDestroy()
        multiLineKeyboardHeightAdjuster.close()
    }
}
