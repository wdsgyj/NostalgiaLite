package nostalgia.framework.base;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;

import nostalgia.framework.ui.preferences.PreferenceUtil;

/** Applies the same screen direction to games, galleries and layout editors. */
public final class ScreenOrientationController implements Application.ActivityLifecycleCallbacks,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private Activity resumedActivity;

    public ScreenOrientationController(Application application) {
        application.registerActivityLifecycleCallbacks(this);
        PreferenceManager.getDefaultSharedPreferences(application)
                .registerOnSharedPreferenceChangeListener(this);
    }

    private void apply(Activity activity) {
        // This helper runs in a separate process and only restarts the emulator.
        if (activity instanceof RestarterActivity) return;
        int orientation;
        switch (PreferenceUtil.getDisplayRotation(activity)) {
            case LAND:
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            case PORT:
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            default:
                orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
                break;
        }
        if (activity.getRequestedOrientation() != orientation) {
            activity.setRequestedOrientation(orientation);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if ("general_pref_rotation".equals(key) && resumedActivity != null) {
            apply(resumedActivity);
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        apply(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        resumedActivity = activity;
        apply(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (resumedActivity == activity) resumedActivity = null;
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (resumedActivity == activity) resumedActivity = null;
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }
}
