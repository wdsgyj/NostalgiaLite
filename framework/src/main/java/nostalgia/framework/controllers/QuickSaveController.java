package nostalgia.framework.controllers;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.os.SystemClock;

import androidx.core.view.GestureDetectorCompat;

import nostalgia.framework.Emulator;
import nostalgia.framework.EmulatorController;
import nostalgia.framework.base.EmulatorActivity;
import nostalgia.framework.ui.gamegallery.GameDescription;
import nostalgia.framework.ui.preferences.PreferenceUtil;
import nostalgia.framework.ui.preferences.QuickSaveRegionSettings;

/**
 * 快速存档控制器，通过双击手势实现快速存档/读档。
 * <p>
 * 默认左半屏读档、右半屏存档，作用区域可在控制器布局中调整。
 * </p>
 */
public class QuickSaveController implements EmulatorController {

    TouchController touchController;
    EmulatorActivity activity;
    private GestureDetectorCompat gestureDetector;
    private View view;
    private QuickSaveRegions regions = new QuickSaveRegions();
    private final int[] location = new int[2];
    private float firstX, firstY;
    private boolean isEnabled;

    public QuickSaveController(EmulatorActivity activity,
                               TouchController touchController) {
        this.activity = activity;
        this.touchController = touchController;
        this.gestureDetector = new GestureDetectorCompat(activity, new GestureListener());
    }

    @Override
    public void onResume() {
        isEnabled = PreferenceUtil.isQuickSaveEnabled(activity);
        reloadRegions();
    }

    @Override
    public void onPause() {
        cancelGesture();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    }

    @Override
    public void onGameStarted(GameDescription game) {
    }

    @Override
    public void onGamePaused(GameDescription game) {
    }

    @Override
    public void connectToEmulator(int port, Emulator emulator) {
    }

    @Override
    public View getView() {
        view = new View(activity) {
            @Override
            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                super.onSizeChanged(w, h, oldw, oldh);
                reloadRegions();
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (!isEnabled) {
                    return true;
                }
                int pointerId = event.getPointerId(event.getActionIndex());
                if (event.getPointerCount() > 1 || touchController.isPointerHandled(pointerId)) {
                    cancelGesture();
                    return true;
                }
                return gestureDetector.onTouchEvent(event);

            }
        };
        return view;
    }

    private void reloadRegions() {
        if (view != null && view.getWidth() > 0) {
            View content = activity.findViewById(android.R.id.content);
            regions = QuickSaveRegionSettings.load(activity, content.getWidth() > content.getHeight());
        }
    }

    private void cancelGesture() {
        long now = SystemClock.uptimeMillis();
        MotionEvent cancel = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        gestureDetector.onTouchEvent(cancel);
        cancel.recycle();
    }

    @Override
    public void onDestroy() {
        cancelGesture();
        view = null;
        activity = null;
        touchController = null;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }


        @Override
        public boolean onDoubleTap(MotionEvent e) {
            View content = activity.findViewById(android.R.id.content);
            content.getLocationOnScreen(location);
            firstX = (e.getRawX() - location[0]) / content.getWidth();
            firstY = (e.getRawY() - location[1]) / content.getHeight();
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                View content = activity.findViewById(android.R.id.content);
                content.getLocationOnScreen(location);
                int action = regions.doubleTap(firstX, firstY,
                        (e.getRawX() - location[0]) / content.getWidth(),
                        (e.getRawY() - location[1]) / content.getHeight());
                if (action == QuickSaveRegions.LOAD) activity.quickLoad();
                else if (action == QuickSaveRegions.SAVE) activity.quickSave();
            }
            return true;
        }
    }

}
