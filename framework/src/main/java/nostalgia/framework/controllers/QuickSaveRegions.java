package nostalgia.framework.controllers;

/** Normalized hit areas shared by the editor and the double-tap controller. */
public final class QuickSaveRegions {
    public static final int NONE = 0;
    public static final int LOAD = 1;
    public static final int SAVE = 2;

    public static final class Region {
        public final float left, top, right, bottom;

        public Region(float left, float top, float right, float bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public boolean isValid() {
            return left >= 0 && top >= 0 && right <= 1 && bottom <= 1
                    && left < right && top < bottom;
        }

        public boolean contains(float x, float y) {
            return x >= left && x < right && y >= top && y < bottom;
        }

        public boolean intersects(Region other) {
            return left < other.right && right > other.left
                    && top < other.bottom && bottom > other.top;
        }

        public Region move(float dx, float dy) {
            dx = Math.max(-left, Math.min(dx, 1 - right));
            dy = Math.max(-top, Math.min(dy, 1 - bottom));
            return new Region(left + dx, top + dy, right + dx, bottom + dy);
        }

        public Region resize(float dx, float dy, float minWidth, float minHeight) {
            return new Region(left, top,
                    Math.min(1, Math.max(left + minWidth, right + dx)),
                    Math.min(1, Math.max(top + minHeight, bottom + dy)));
        }
    }

    private Region load;
    private Region save;

    public QuickSaveRegions() {
        this(new Region(0, 0, .5f, 1), new Region(.5f, 0, 1, 1));
    }

    public QuickSaveRegions(Region load, Region save) {
        if (!load.isValid() || !save.isValid() || load.intersects(save)) {
            load = new Region(0, 0, .5f, 1);
            save = new Region(.5f, 0, 1, 1);
        }
        this.load = load;
        this.save = save;
    }

    public Region get(int action) {
        if (action == LOAD) return load;
        if (action == SAVE) return save;
        throw new IllegalArgumentException("Unknown quick-save action: " + action);
    }

    public boolean update(int action, Region region) {
        Region other = get(action == LOAD ? SAVE : LOAD);
        if (!region.isValid() || region.intersects(other)) return false;
        if (action == LOAD) load = region;
        else if (action == SAVE) save = region;
        else return false;
        return true;
    }

    public int hit(float x, float y) {
        if (load.contains(x, y)) return LOAD;
        if (save.contains(x, y)) return SAVE;
        return NONE;
    }

    public int doubleTap(float firstX, float firstY, float secondX, float secondY) {
        int action = hit(firstX, firstY);
        return action == hit(secondX, secondY) ? action : NONE;
    }
}
