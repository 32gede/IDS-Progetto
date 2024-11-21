package com.example.progetto.data.model;

import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {

    private static final int SWIPE_THRESHOLD = 50;
    private static final int SWIPE_VELOCITY_THRESHOLD = 50;
    private final GestureDetector gestureDetector;
    private final Context context;
    private final Class<?> rightSwipeActivity;
    private final Class<?> leftSwipeActivity;

    public SwipeGestureListener(Context context, Class<?> rightSwipeActivity, Class<?> leftSwipeActivity) {
        this.context = context;
        this.rightSwipeActivity = rightSwipeActivity;
        this.leftSwipeActivity = leftSwipeActivity;
        gestureDetector = new GestureDetector(context, this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();
        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    // Swipe right
                    onSwipeRight();
                } else {
                    // Swipe left
                    onSwipeLeft();
                }
                return true;
            }
        }
        return false;
    }

    private void onSwipeRight() {
        Intent intent = new Intent(context, rightSwipeActivity);
        context.startActivity(intent);
    }

    private void onSwipeLeft() {
        Intent intent = new Intent(context, leftSwipeActivity);
        context.startActivity(intent);
    }
}