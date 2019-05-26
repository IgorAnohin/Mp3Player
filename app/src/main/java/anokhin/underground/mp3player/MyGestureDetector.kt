package anokhin.underground.mp3player

import android.support.v4.media.session.MediaControllerCompat
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class MyGestureDetector(val view: View, val mediaController: MediaControllerCompat?) : GestureDetector.SimpleOnGestureListener() {
    override fun onFling(
        e1: MotionEvent, e2: MotionEvent, velocityX: Float,
        velocityY: Float
    ): Boolean {

        // swipe right to left
        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
            && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY
        ) {
            if (mediaController != null)
                mediaController.transportControls.skipToNext()
        }

        // swipe left to right
        else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
            && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY
        ){
        if (mediaController != null)
            mediaController.transportControls.skipToPrevious()
        }

        return super.onFling(e1, e2, velocityX, velocityY)
    }

    companion object {
        private val SWIPE_MIN_DISTANCE = 120
        private val SWIPE_THRESHOLD_VELOCITY = 200
    }
}

