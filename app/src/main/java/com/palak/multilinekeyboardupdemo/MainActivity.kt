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
        root.post { keyboardHeightProvider.start(nestedScroll, root, viewSpace) }

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

    private fun printEditTextScrollY() {
        et1.setText(""+getLocationOfView(et1,nestedScroll).top)
        et2.setText(""+getLocationOfView(et2,nestedScroll).top)
        et3.setText(""+getLocationOfView(et3,nestedScroll).top)
        et4.setText(""+getLocationOfView(et4,nestedScroll).top)
        et5.setText(""+getLocationOfView(et5,nestedScroll).top)
        et6.setText(""+getLocationOfView(et6,nestedScroll).top)
        etMultiline1.setText(""+getLocationOfView(etMultiline1,nestedScroll).top)
        etMultiline2.setText(""+getLocationOfView(etMultiline2,nestedScroll).top)

    }

    private fun getLocationOfView(childView : View, parentViewGroup: ViewGroup) : Rect{
        val offsetViewBounds = Rect()
        //returns the visible bounds
        childView.getDrawingRect(offsetViewBounds)
        // calculates the relative coordinates to the parent
        parentViewGroup.offsetDescendantRectToMyCoords(childView, offsetViewBounds)

        return offsetViewBounds
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardHeightProvider.close()
    }

}
