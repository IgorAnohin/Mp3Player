package anokhin.underground.mp3player

import android.view.MotionEvent

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

class SongsActivity : Activity(), SimpleGestureFilter.SimpleGestureListener {
    private var detector: SimpleGestureFilter? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.songs)

        // Detect touched area
        detector = SimpleGestureFilter(this, this)
    }

    override fun dispatchTouchEvent(me: MotionEvent): Boolean {
        // Call onTouchEvent of SimpleGestureFilter class
        this.detector!!.onTouchEvent(me)
        return super.dispatchTouchEvent(me)
    }

    override fun onSwipe(direction: Int) {
        var str = ""

        when (direction) {

            SimpleGestureFilter.SWIPE_RIGHT -> str = "Swipe Right"
            SimpleGestureFilter.SWIPE_LEFT -> str = "Swipe Left"
            SimpleGestureFilter.SWIPE_DOWN -> str = "Swipe Down"
            SimpleGestureFilter.SWIPE_UP -> str = "Swipe Up"
        }
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
    }

    override fun onDoubleTap() {
//        Toast.makeText(this, "Double Tap", Toast.LENGTH_SHORT).show()
    }

}
