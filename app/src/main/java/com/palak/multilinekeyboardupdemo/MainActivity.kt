package com.palak.multilinekeyboardupdemo

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){

    private lateinit var keyboardHeightProvider: KeyboardHeightProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        keyboardHeightProvider = KeyboardHeightProvider(this)
        root.post { keyboardHeightProvider.start(nestedScroll, root, viewSpace, 0, btnSubmit.height) }
    }


    override fun onPause() {
        super.onPause()
        //Dont delete this
        //keyboardHeightProvider.setKeyboardHeightObserver(null)
    }

    override fun onResume() {
        super.onResume()
       // keyboardHeightProvider.setKeyboardHeightObserver(this)


    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardHeightProvider.close()
    }
}
