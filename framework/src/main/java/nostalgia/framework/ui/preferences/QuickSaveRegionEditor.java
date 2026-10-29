package nostalgia.framework.ui.preferences;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import nostalgia.framework.R;
import nostalgia.framework.controllers.QuickSaveRegions;
import nostalgia.framework.controllers.QuickSaveRegions.Region;

/** Full-size overlay: drag a region to move it, drag its corner to resize freely. */
public final class QuickSaveRegionEditor extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;
    private QuickSaveRegions regions;
    private boolean landscape;
    private int selected = QuickSaveRegions.NONE;
    private boolean resizing;
    private Region dragStart;
    private float startX, startY;

    public QuickSaveRegionEditor(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        setContentDescription(getResources().getString(R.string.quick_regions_hint));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        save();
        landscape = w > h;
        regions = QuickSaveRegionSettings.load(getContext(), landscape);
        selected = QuickSaveRegions.NONE;
    }

    public void save() {
        if (regions != null) QuickSaveRegionSettings.save(getContext(), landscape, regions);
    }

    public void reset() {
        regions = new QuickSaveRegions();
        selected = QuickSaveRegions.NONE;
        save();
        invalidate();
    }

    private RectF bounds(Region region) {
        return new RectF(region.left * getWidth(), region.top * getHeight(),
                region.right * getWidth(), region.bottom * getHeight());
    }

    private RectF handle(Region region) {
        RectF rect = bounds(region);
        // Keep the handle away from Android's back/home gesture edges.
        float inset = 8 * density;
        float size = Math.min(32 * density, Math.min(rect.width(), rect.height()) - 2 * inset);
        return new RectF(rect.right - inset - size, rect.bottom - inset - size,
                rect.right - inset, rect.bottom - inset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0xc0182028);
        if (regions == null) return;
        drawRegion(canvas, QuickSaveRegions.LOAD, 0xff42a5f5, R.string.quick_region_load);
        drawRegion(canvas, QuickSaveRegions.SAVE, 0xffffb74d, R.string.quick_region_save);
    }

    private void drawRegion(Canvas canvas, int action, int color, int label) {
        Region region = regions.get(action);
        RectF rect = bounds(region);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor((color & 0xffffff) | 0x33000000);
        canvas.drawRect(rect, paint);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((selected == action ? 3 : 1.5f) * density);
        canvas.drawRect(rect.left + density, rect.top + density,
                rect.right - density, rect.bottom - density, paint);
        paint.setStyle(Paint.Style.FILL);
        RectF handle = handle(region);
        canvas.drawRect(handle, paint);
        paint.setColor(0xff182028);
        paint.setStrokeWidth(2 * density);
        canvas.drawLine(handle.left + 8 * density, handle.bottom - 6 * density,
                handle.right - 6 * density, handle.top + 8 * density, paint);
        paint.setColor(color);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(16 * density);
        String text = getResources().getString(label);
        float available = rect.width() - 8 * density;
        if (paint.measureText(text) > available) {
            paint.setTextSize(paint.getTextSize() * available / paint.measureText(text));
        }
        canvas.drawText(text, rect.centerX(), rect.centerY(), paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (regions == null || getWidth() == 0 || getHeight() == 0) return true;
        if (event.getPointerCount() > 1 || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            if (selected != QuickSaveRegions.NONE) regions.update(selected, dragStart);
            selected = QuickSaveRegions.NONE;
            invalidate();
            return true;
        }
        float x = event.getX() / getWidth();
        float y = event.getY() / getHeight();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                selected = regions.hit(x, y);
                if (selected != QuickSaveRegions.NONE) {
                    dragStart = regions.get(selected);
                    resizing = handle(dragStart).contains(event.getX(), event.getY());
                    startX = x;
                    startY = y;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (selected != QuickSaveRegions.NONE) {
                    Region candidate = resizing
                            ? dragStart.resize(x - startX, y - startY,
                                    48 * density / getWidth(), 48 * density / getHeight())
                            : dragStart.move(x - startX, y - startY);
                    regions.update(selected, candidate);
                }
                break;
            case MotionEvent.ACTION_UP:
                save();
                selected = QuickSaveRegions.NONE;
                performClick();
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
