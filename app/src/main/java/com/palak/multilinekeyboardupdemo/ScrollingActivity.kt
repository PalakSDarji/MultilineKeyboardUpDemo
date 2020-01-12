package com.palak.multilinekeyboardupdemo

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.android.synthetic.main.content_scrolling.*

class ScrollingActivity : AppCompatActivity() {

    private lateinit var multiLineKeyboardHeightAdjuster: MultiLineKeyboardHeightAdjuster

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        multiLineKeyboardHeightAdjuster = MultiLineKeyboardHeightAdjuster(this)
        root.post { multiLineKeyboardHeightAdjuster.start(nestedScroll, root, app_bar.height, 0) }
    }

    override fun onDestroy() {
        super.onDestroy()
        multiLineKeyboardHeightAdjuster.close()
    }
}
