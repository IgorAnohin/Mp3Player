package com.bignerdranch.android.barexample

import android.app.AlertDialog
import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.Nullable
import android.widget.Button
import android.view.*

import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.MotionEvent

import android.view.GestureDetector

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        val builder = AlertDialog.Builder(this@MainActivity, R.style.FullScreenDialogStyle)

        builder.setView(R.layout.second)

        val dialog: AlertDialog = builder.create()
        val window = dialog.window
        window.requestFeature(Window.FEATURE_NO_TITLE)
        val wlp = window.attributes
        window.attributes = wlp

        val gestureDetector = GestureDetector(this, MyGestureDetector(dialog))
        val inflater = this.getLayoutInflater()
        val view = inflater.inflate(R.layout.second, null)
        view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val eventConsumed = gestureDetector.onTouchEvent(event)
                return if (eventConsumed) {
                    true
                } else {
                    false
                }
            }
        })
        val windowCallback = object : Window.Callback {
            override fun onWindowStartingActionMode(callback: ActionMode.Callback?, type: Int): ActionMode? {
                return onWindowStartingActionMode(callback);
            }


            override fun onSearchRequested(searchEvent: SearchEvent?): Boolean {
                return false;
            }

            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.keyCode === KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss()
                }
                return false
            }

            override fun dispatchKeyShortcutEvent(event: KeyEvent): Boolean {
                return false
            }

            override fun dispatchTouchEvent(event: MotionEvent): Boolean {
                return if (gestureDetector != null) {
                    gestureDetector.onTouchEvent(event)
                } else false
            }

            override fun dispatchTrackballEvent(event: MotionEvent): Boolean {
                return false
            }

            override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
                return true
            }

            override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent): Boolean {
                return false
            }

            @Nullable
            override fun onCreatePanelView(featureId: Int): View? {
                return null
            }

            override fun onCreatePanelMenu(featureId: Int, menu: Menu): Boolean {
                return false
            }

            override fun onPreparePanel(featureId: Int, view: View, menu: Menu): Boolean {
                return false
            }

            override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
                return false
            }

            override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
                return false
            }

            override fun onWindowAttributesChanged(attrs: WindowManager.LayoutParams) {

            }

            override  fun onContentChanged() {

            }

            override fun onWindowFocusChanged(hasFocus: Boolean) {

            }

            override fun onAttachedToWindow() {

            }

            override fun onDetachedFromWindow() {

            }

            override fun onPanelClosed(featureId: Int, menu: Menu) {

            }

            override  fun onSearchRequested(): Boolean {
                return false
            }

            @Nullable
            override fun onWindowStartingActionMode(callback: ActionMode.Callback): ActionMode? {
                return null
            }

            override fun onActionModeStarted(mode: ActionMode) {

            }

            override fun onActionModeFinished(mode: ActionMode) {

            }
        }

        dialog.window.callback = windowCallback
        button.setOnClickListener({dialog.show()})


    }
}


 class MyGestureDetector(val dialog: Dialog) : GestureDetector.SimpleOnGestureListener() {
    override fun onFling(
        e1: MotionEvent, e2: MotionEvent, velocityX: Float,
        velocityY: Float
    ): Boolean {

        if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
            && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
            dialog.dismiss()
        }

        return super.onFling(e1, e2, velocityX, velocityY)
    }
    companion object {
        private val SWIPE_MIN_DISTANCE = 120
        private val SWIPE_THRESHOLD_VELOCITY = 200
    }
}
