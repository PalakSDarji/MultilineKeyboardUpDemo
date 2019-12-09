package com.palak.multilinekeyboardupdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), KeyboardHeightObserver {

    private lateinit var keyboardHeightProvider: KeyboardHeightProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        keyboardHeightProvider = KeyboardHeightProvider(this)
        root.post { keyboardHeightProvider.start() }
    }


    override fun onPause() {
        super.onPause()
        keyboardHeightProvider.setKeyboardHeightObserver(null)
    }

    override fun onResume() {
        super.onResume()
        keyboardHeightProvider.setKeyboardHeightObserver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardHeightProvider.close()
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {

        val viewTempHeight = resources.getDimension(R.dimen.margin_20).toInt()
        val params = viewSpace.layoutParams as ConstraintLayout.LayoutParams

        if(height == 0){
            params.height = viewTempHeight
            viewSpace.layoutParams = params
        }
        else {
            val et: EditText = Utils.getCurrentFocusedEdittext(this,findViewById(android.R.id.content)) ?: return
            val keyboardHeightFromTop = height - nestedScroll.top
            if(et.y <= keyboardHeightFromTop){
                return
            }
            params.height = (keyboardHeightProvider.getMaximumHeightOfKeyboard() + viewTempHeight) - btnSubmit.height
            viewSpace.layoutParams = params
            Utils.scrollToView(this, nestedScroll, et)
        }
    }
}
