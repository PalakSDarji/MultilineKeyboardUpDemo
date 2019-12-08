package com.palak.multilinekeyboardupdemo

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.NestedScrollView

object Utils {

    /**
     * Used to scroll to the given view.
     *
     * @param scrollViewParent Parent ScrollView
     * @param view View to which we need to scroll.
     */
    fun scrollToView(context: Context, scrollViewParent: NestedScrollView, view: View) {
        // Get deepChild Offset
        var childOffset = Point()
        getDeepChildOffset(scrollViewParent, view.parent, view, childOffset)
        // Scroll to child.
        val padding = context.resources.getDimension(R.dimen.margin_20).toInt();
        val totalYToGo = childOffset.y + padding
        scrollViewParent.post {
            scrollViewParent.smoothScrollTo(0, totalYToGo)
        }
    }


    /**
     * Used to get deep child offset.
     * <p/>
     * 1. We need to scroll to child in scrollview, but the child may not the direct child to scrollview.
     * 2. So to get correct child position to scroll, we need to iterate through all of its parent views till the main parent.
     *
     * @param mainParent        Main Top parent.
     * @param parent            Parent.
     * @param child             Child.
     * @param accumulatedOffset Accumulated Offset.
     */
    private fun getDeepChildOffset(
        mainParent: ViewGroup,
        parent: ViewParent,
        child: View,
        accumulatedOffset: Point
    ) {
        val parentGroup: ViewGroup = parent as ViewGroup
        accumulatedOffset.x += child.left
        accumulatedOffset.y += child.top
        if (parentGroup.equals(mainParent)) {
            return;
        }
        getDeepChildOffset(mainParent, parentGroup.parent, parentGroup, accumulatedOffset);
    }

    /**
     * This method set touch listener on all the views which are not edittext, which hides the keyboard.
     */
    fun getCurrentFocusedEdittext(activity: Activity, view: View) : EditText?{

        // Set up touch listener for non-text box views to hide keyboard.
        if (view is EditText && view.hasFocus()) {
            return view
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            val count = view.childCount
            for (i in 0 until count) {
                val innerView : View = view.getChildAt(i)
                val et =  getCurrentFocusedEdittext(activity, innerView)
                if(et != null) return et
            }
        }

        return null
    }

}