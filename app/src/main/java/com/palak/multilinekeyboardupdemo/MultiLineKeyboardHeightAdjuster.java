package com.palak.multilinekeyboardupdemo;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.NestedScrollView;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The keyboard height provider, this class uses a PopupWindow
 * to calculate the window height when the floating keyboard is opened and closed.
 */
public class MultiLineKeyboardHeightAdjuster extends PopupWindow {

    /**
     * The keyboard height observer
     */
    private KeyboardHeightObserver observer;

    /**
     * The cached landscape height of the keyboard
     */
    private int keyboardLandscapeHeight;

    /**
     * The cached portrait height of the keyboard
     */
    private int keyboardPortraitHeight;

    /**
     * The view that is used to calculate the keyboard height
     */
    private View popupView;

    /**
     * The parent view
     */
    private View parentView;

    /**
     * The root activity that uses this MultiLineKeyboardHeightAdjuster
     */
    private Activity activity;

    private Set<Integer> diffHeightList = new HashSet<>();

    private int oldKeyboardHeight;

    private NestedScrollView nestedScroll;

    private View viewSpace;

    private Rect scrollViewRect = null;

    private int paddingTop;
    private float scrollDY;
    private int heightToCutFromScrollTop;
    private int heightToCutFromBottom;
    private int closingHeight;


    /**
     * Construct a new MultiLineKeyboardHeightAdjuster
     *
     * @param activity The parent activity
     */
    public MultiLineKeyboardHeightAdjuster(Activity activity) {
        super(activity);
        this.activity = activity;

        LayoutInflater inflator = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        this.popupView = inflator.inflate(R.layout.popupwindow, null, false);
        setContentView(popupView);

        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);

        parentView = activity.findViewById(android.R.id.content);

        setWidth(0);
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);

        popupView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (popupView != null) {
                    handleOnGlobalLayout();
                }
            }
        });

        paddingTop = activity.getResources().getDimensionPixelSize(R.dimen.margin_10);
    }

    /**
     * Start the MultiLineKeyboardHeightAdjuster, this must be called after the onResume of the Activity.
     * PopupWindows are not allowed to be registered before the onResume has finished
     * of the Activity.
     */
    public void start(NestedScrollView nestedScrollView, final ViewGroup parentViewGroup,
                      int heightToCutFromScrollTop, int heightToCutFromBottom) throws Exception {

        if (!isShowing() && parentView.getWindowToken() != null) {
            setBackgroundDrawable(new ColorDrawable(0));
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0);
        }

        this.nestedScroll = nestedScrollView;
        this.viewSpace = insertView();
        this.heightToCutFromScrollTop = heightToCutFromScrollTop;
        this.heightToCutFromBottom = heightToCutFromBottom;

        nestedScroll.post(new Runnable() {
            @Override
            public void run() {
                scrollViewRect = getLocationOfView(nestedScroll, parentViewGroup);
                Log.v("KEYBH", "scrollViewRect top : " + scrollViewRect.top + ", bottom " + scrollViewRect.bottom + ", height: " + scrollViewRect.height());
            }
        });

        setListenerOnFocusedEdittext(activity.findViewById(android.R.id.content), new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) onKeyboardHeightChanged(getMaximumHeightOfKeyboard(), 0);
            }
        });
    }

    private Rect getLocationOfView(View childView, ViewGroup parentViewGroup) {
        Rect offsetViewBounds = new Rect();
        //returns the visible bounds
        childView.getDrawingRect(offsetViewBounds);
        // calculates the relative coordinates to the parent
        parentViewGroup.offsetDescendantRectToMyCoords(childView, offsetViewBounds);
        return offsetViewBounds;
    }

    /**
     * Close the keyboard height provider,
     * this provider will not be used anymore.
     */
    public void close() {
        this.observer = null;
        dismiss();
    }

    /**
     * Set the keyboard height observer to this provider. The
     * observer will be notified when the keyboard height has changed.
     * For example when the keyboard is opened or closed.
     *
     * @param observer The observer to be added to this provider.
     */
    public void setKeyboardHeightObserver(KeyboardHeightObserver observer) {
        this.observer = observer;
    }

    /**
     * Popup window itself is as big as the window of the Activity.
     * The keyboard can then be calculated by extracting the popup view bottom
     * from the activity window height.
     */
    private void handleOnGlobalLayout() {

        Point screenSize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(screenSize);

        Rect rect = new Rect();
        popupView.getWindowVisibleDisplayFrame(rect);

        // REMIND, you may like to change this using the fullscreen size of the phone
        // and also using the status bar and navigation bar heights of the phone to calculate
        // the keyboard height. But this worked fine on a Nexus.
        int orientation = getScreenOrientation();
        int keyboardHeight = screenSize.y - rect.bottom;

        if (oldKeyboardHeight != keyboardHeight) {
            oldKeyboardHeight = keyboardHeight;
        } else {
            return;
        }

        if (keyboardHeight == 0) {
            notifyKeyboardHeightChanged(0, orientation);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.keyboardPortraitHeight = keyboardHeight;
            notifyKeyboardHeightChanged(keyboardPortraitHeight, orientation);
        } else {
            this.keyboardLandscapeHeight = keyboardHeight;
            notifyKeyboardHeightChanged(keyboardLandscapeHeight, orientation);
        }

    }

    public int getMaximumHeightOfKeyboard() {
        if (diffHeightList.isEmpty()) return 0;
        return Collections.max(diffHeightList);
    }

    private int getScreenOrientation() {
        return activity.getResources().getConfiguration().orientation;
    }

    private void notifyKeyboardHeightChanged(int height, int orientation) {
        //if (observer != null) {
        if (height == 0) {
            diffHeightList.clear();
        }
        diffHeightList.add(height);
        onKeyboardHeightChanged(height, orientation);
        //}
    }


    /**
     * This method set touch listener on all the views which are not edittext, which hides the keyboard.
     */
    private void setListenerOnFocusedEdittext(View view, View.OnFocusChangeListener
            onFocusChangeListener) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (view instanceof EditText) {
            view.setOnFocusChangeListener(onFocusChangeListener);
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            int count = ((ViewGroup) view).getChildCount();
            for (int i = 0; i < count; i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setListenerOnFocusedEdittext(innerView, onFocusChangeListener);
            }
        }
    }


    public void onKeyboardHeightChanged(int height, int orientation) {

        if (scrollViewRect == null) return;

        Log.v("KEYBH", "keyboardHeight " + height + ",  scrollViewRectHeight " + scrollViewRect.height());

        int viewPortHeight = scrollViewRect.height() - (height - heightToCutFromBottom - closingHeight) - paddingTop;
        Log.v("KEYBH", "viewPortHeight " + viewPortHeight);

        int viewTempHeight = (int) activity.getResources().getDimension(R.dimen.margin_20);
        ViewGroup.LayoutParams params = viewSpace.getLayoutParams();

        if (height <= 0) {
            //This height is closing height. save it.
            closingHeight = height;
            params.height = viewTempHeight;
            viewSpace.setLayoutParams(params);
        } else {
            EditText et = getCurrentFocusedEdittext(activity.findViewById(android.R.id.content));

            if (et == null) return;

            Rect rectOfEt = getLocationOfView(et, nestedScroll);
            //rectOfEt.top = rectOfEt.top + heightToCutFromScrollTop;

            Log.v("KEYBH", "nestedScroll.getScrollY: " + nestedScroll.getScrollY() + ", heightToCutFromScrollTop: " + heightToCutFromScrollTop);

            int currNestedScrollY = nestedScroll.getScrollY() + heightToCutFromScrollTop;

            Log.v("KEYBH", "currNestedScrollY: " + currNestedScrollY);

            if (nestedScroll.getScrollY() + viewPortHeight < rectOfEt.bottom) {

                //that means, view is below the keyboard, scroll it up. align its bottom to keyboard's top.
                Log.v("KEYBH", "scroll Down : nestedScroll.getScrollY(): " + nestedScroll.getScrollY() + ", viewPortHeight: " + viewPortHeight + ", rectOfEt.bottom: " + rectOfEt.bottom);

                float currentYPostionInViewPort = rectOfEt.bottom - nestedScroll.getScrollY();

                Log.v("KEYBH", "rectOfEt.bottom: " + rectOfEt.bottom + ", nestedScroll.getScrollY(): " + nestedScroll.getScrollY() + ", currentYPostionInViewPort : " + currentYPostionInViewPort);

                scrollDY = currentYPostionInViewPort - viewPortHeight;

                Log.v("KEYBH", "currentYPostionInViewPort: " + currentYPostionInViewPort + ", viewPortHeight: " + viewPortHeight);

            } else if (rectOfEt.top < currNestedScrollY) {
                //that means, view is slightly visible and is at top of scrollview. scroll it bottom a bit.
                //align its top to scrollview's top.

                Log.v("KEYBH", "scroll Up : scrollViewRect.top: " + scrollViewRect.top + ", rectOfEt.top: " + rectOfEt.top);

                scrollDY = rectOfEt.top - currNestedScrollY;

                Log.v("KEYBH", "nestedScroll.scrollY : " + currNestedScrollY + ", rectOfEt.top: " + rectOfEt.top);

            } else {
                Log.v("KEYBH", "No need to scroll");
                return;
            }

            params.height = getMaximumHeightOfKeyboard() + viewTempHeight;
            viewSpace.setLayoutParams(params);

            if (scrollDY != 0) {
                nestedScroll.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.v("KEYBH", "scrollDY: " + scrollDY);
                        nestedScroll.smoothScrollBy(0, (int) scrollDY);
                    }
                }, 200);
            }
        }
    }


    /**
     * This method set touch listener on all the views which are not edittext, which hides the keyboard.
     */
    private EditText getCurrentFocusedEdittext(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (view instanceof EditText && view.hasFocus()) {
            return (EditText) view;
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            int count = ((ViewGroup) view).getChildCount();
            for (int i = 0; i < count; i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                EditText et = getCurrentFocusedEdittext(innerView);
                if (et != null) return et;
            }
        }

        return null;
    }

    //TODO in progress.
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private View insertView() throws Exception {

        View viewToInsert = new View(activity);
        viewToInsert.setId(View.generateViewId());
        int heightDefault = activity.getResources().getDimensionPixelSize(R.dimen.margin_20);
        viewToInsert.setBackgroundResource(android.R.color.background_dark);

        if(nestedScroll.getChildCount() == 0) return viewToInsert;
        View firstChild = nestedScroll.getChildAt(0);

        if(firstChild instanceof ConstraintLayout){

            View lastChild = ((ConstraintLayout) firstChild).getChildAt(((ConstraintLayout) firstChild).getChildCount()-1);
            if(lastChild.getId() <= 0) lastChild.setId(View.generateViewId());

            ConstraintSet set = new ConstraintSet();
            ((ConstraintLayout) firstChild).addView(viewToInsert,0);
            set.clone((ConstraintLayout) firstChild);
            set.connect(viewToInsert.getId(), ConstraintSet.TOP, lastChild.getId(), ConstraintSet.BOTTOM,0);
            set.applyTo((ConstraintLayout) firstChild);
        }
        else if(firstChild instanceof LinearLayout){
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, heightDefault);
            ((LinearLayout) firstChild).addView(viewToInsert,layoutParams);
        }
        else{
            throw new Exception("NesetedScrollView must have viewgroup as first child!");
        }

        return viewToInsert;
    }
}


interface KeyboardHeightObserver {

    /**
     * Called when the keyboard height has changed, 0 means keyboard is closed,
     * >= 1 means keyboard is opened.
     *
     * @param height        The height of the keyboard in pixels
     * @param orientation   The orientation either: Configuration.ORIENTATION_PORTRAIT or
     *                      Configuration.ORIENTATION_LANDSCAPE
     */
    void onKeyboardHeightChanged(int height, int orientation);
}
