package nostalgia.framework.ui.preferences;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import nostalgia.framework.GfxProfile;
import nostalgia.framework.R;
import nostalgia.framework.SlotInfo;
import nostalgia.framework.base.EmulatorUtils;
import nostalgia.framework.base.GameMenu;
import nostalgia.framework.base.GameMenu.GameMenuItem;
import nostalgia.framework.base.GameMenu.OnGameMenuListener;
import nostalgia.framework.base.SlotUtils;
import nostalgia.framework.ui.gamegallery.GameDescription;
import nostalgia.framework.ui.multitouchbutton.MultitouchLayer;
import nostalgia.framework.ui.multitouchbutton.MultitouchLayer.EDIT_MODE;
import nostalgia.framework.utils.DatabaseHelper;

/**
 * 触摸控制器设置 Activity。
 * <p>
 * 在编辑模式下调整虚拟按钮的位置和大小，
 * 支持重置和保存触摸控制器布局配置。
 * </p>
 */
public class TouchControllerSettingsActivity extends AppCompatActivity implements
        OnGameMenuListener {

    MultitouchLayer mtLayer;
    String gameHash = "";
    DatabaseHelper dbHelper;
    Bitmap lastGameScreenshot;
    private GameMenu gameMenu;
    private FrameLayout regionOverlay;
    private QuickSaveRegionEditor regionEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controler_layout);
        gameMenu = new GameMenu(this, this);
        mtLayer = findViewById(R.id.touch_layer);
        dbHelper = new DatabaseHelper(this);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (regionOverlay != null) closeRegionEditor();
                else finish();
            }
        });
        if (savedInstanceState != null && savedInstanceState.getBoolean("editingQuickRegions")) {
            showRegionEditor();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mtLayer.setEditMode(EDIT_MODE.TOUCH);
        GameDescription games = dbHelper.selectObjFromDb(GameDescription.class,
                "where lastGameTime!=0 ORDER BY lastGameTime DESC LIMIT 1");
        GfxProfile gfxProfile;
        lastGameScreenshot = null;

        if (games != null) {
            SlotInfo info = SlotUtils.getSlot(EmulatorUtils.getBaseDir(this),
                    games.checksum, 0);
            lastGameScreenshot = info.screenShot;
        }

        gfxProfile = PreferenceUtil.getLastGfxProfile(this);
        mtLayer.setLastGameScreenshot(lastGameScreenshot,
                gfxProfile == null ? null : gfxProfile.name);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mtLayer.saveEditElements();
        if (regionEditor != null) regionEditor.save();
        mtLayer.stopEditMode();

        if (lastGameScreenshot != null) {
            lastGameScreenshot.recycle();
            lastGameScreenshot = null;
        }
    }


    @Override
    public void onGameMenuCreate(GameMenu menu) {
        menu.add(R.string.quick_regions_title);
        menu.add(R.string.act_tcs_reset, R.drawable.ic_restart);
    }

    @Override
    public void onGameMenuPrepare(GameMenu menu) {
    }

    @Override
    public void onGameMenuOpened(GameMenu menu) {
    }

    @Override
    public void onGameMenuClosed(GameMenu menu) {
    }

    @Override
    public void onGameMenuItemSelected(GameMenu menu, GameMenuItem item) {
        if (item.getId() == R.string.quick_regions_title) showRegionEditor();
        else if (item.getId() == R.string.act_tcs_reset) mtLayer.resetEditElement(gameHash);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("editingQuickRegions", regionOverlay != null);
        super.onSaveInstanceState(outState);
    }

    private void showRegionEditor() {
        if (regionOverlay != null) return;
        regionOverlay = new FrameLayout(this);
        regionEditor = new QuickSaveRegionEditor(this);
        regionOverlay.addView(regionEditor, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.VERTICAL);
        toolbar.setBackgroundColor(0xee182028);
        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);
        Button done = new Button(this);
        done.setText(R.string.quick_regions_done);
        done.setOnClickListener(v -> closeRegionEditor());
        actions.addView(done);
        Button reset = new Button(this);
        reset.setText(R.string.quick_regions_reset);
        reset.setOnClickListener(v -> regionEditor.reset());
        actions.addView(reset);
        toolbar.addView(actions);
        TextView hint = new TextView(this);
        hint.setText(R.string.quick_regions_hint);
        hint.setTextColor(0xffffffff);
        hint.setTextSize(12);
        hint.setGravity(Gravity.CENTER);
        toolbar.addView(hint);
        regionOverlay.addView(toolbar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.CENTER_HORIZONTAL));
        addContentView(regionOverlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void closeRegionEditor() {
        regionEditor.save();
        ((ViewGroup) regionOverlay.getParent()).removeView(regionOverlay);
        regionEditor = null;
        regionOverlay = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            openGameMenu();
            return true;

        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public void openGameMenu() {
        if (regionOverlay == null) gameMenu.open();
    }

    @Override
    public void openOptionsMenu() {
        openGameMenu();
    }

}
