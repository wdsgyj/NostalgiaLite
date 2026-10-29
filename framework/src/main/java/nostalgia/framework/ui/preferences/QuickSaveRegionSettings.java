package nostalgia.framework.ui.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import nostalgia.framework.controllers.QuickSaveRegions;
import nostalgia.framework.controllers.QuickSaveRegions.Region;

/** Kept separate from the legacy button layout's numeric preference keys. */
public final class QuickSaveRegionSettings {
    private QuickSaveRegionSettings() {
    }

    private static SharedPreferences preferences(Context context, boolean landscape) {
        return context.getSharedPreferences("quick-save-regions-"
                + (landscape ? "landscape" : "portrait"), Context.MODE_PRIVATE);
    }

    public static QuickSaveRegions load(Context context, boolean landscape) {
        SharedPreferences prefs = preferences(context, landscape);
        QuickSaveRegions defaults = new QuickSaveRegions();
        return new QuickSaveRegions(read(prefs, "load", defaults.get(QuickSaveRegions.LOAD)),
                read(prefs, "save", defaults.get(QuickSaveRegions.SAVE)));
    }

    private static Region read(SharedPreferences prefs, String key, Region fallback) {
        return new Region(prefs.getFloat(key + "_left", fallback.left),
                prefs.getFloat(key + "_top", fallback.top),
                prefs.getFloat(key + "_right", fallback.right),
                prefs.getFloat(key + "_bottom", fallback.bottom));
    }

    public static void save(Context context, boolean landscape, QuickSaveRegions regions) {
        SharedPreferences.Editor editor = preferences(context, landscape).edit();
        write(editor, "load", regions.get(QuickSaveRegions.LOAD));
        write(editor, "save", regions.get(QuickSaveRegions.SAVE));
        editor.apply();
    }

    private static void write(SharedPreferences.Editor editor, String key, Region region) {
        editor.putFloat(key + "_left", region.left).putFloat(key + "_top", region.top)
                .putFloat(key + "_right", region.right).putFloat(key + "_bottom", region.bottom);
    }
}
