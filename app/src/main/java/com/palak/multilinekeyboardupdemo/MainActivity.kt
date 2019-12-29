package com.palak.multilinekeyboardupdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){

    private lateinit var keyboardHeightProvider: KeyboardHeightProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        keyboardHeightProvider = KeyboardHeightProvider(this)
        root.post { keyboardHeightProvider.start(nestedScroll, root, viewSpace, 0, btnSubmit.height) }
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardHeightProvider.close()
    }
}
