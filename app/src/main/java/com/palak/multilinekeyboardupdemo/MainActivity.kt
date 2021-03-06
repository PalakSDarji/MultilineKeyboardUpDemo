package com.palak.multilinekeyboardupdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){

    private lateinit var multiLineKeyboardHeightAdjuster: MultiLineKeyboardHeightAdjuster

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        multiLineKeyboardHeightAdjuster = MultiLineKeyboardHeightAdjuster(this)
        root.post { multiLineKeyboardHeightAdjuster.start(nestedScroll, root, 0, btnSubmit.height) }
    }

    override fun onDestroy() {
        super.onDestroy()
        multiLineKeyboardHeightAdjuster.close()
    }
}
