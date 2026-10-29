package nostalgia.framework.controllers;

import org.junit.Test;

import nostalgia.framework.controllers.QuickSaveRegions.Region;

import static org.junit.Assert.*;
import static nostalgia.framework.controllers.QuickSaveRegions.*;

public class QuickSaveRegionsTest {
    @Test
    public void defaultsKeepLeftLoadAndRightSave() {
        QuickSaveRegions areas = new QuickSaveRegions();
        assertEquals(LOAD, areas.doubleTap(.2f, .2f, .21f, .21f));
        assertEquals(SAVE, areas.doubleTap(.8f, .8f, .81f, .81f));
        assertEquals(NONE, areas.doubleTap(.49f, .5f, .51f, .5f));
    }

    @Test
    public void bothTapsMustBeInsideTheSameCustomArea() {
        QuickSaveRegions areas = new QuickSaveRegions(
                new Region(.1f, .2f, .3f, .4f), new Region(.6f, .7f, .9f, .9f));
        assertEquals(LOAD, areas.doubleTap(.2f, .3f, .21f, .31f));
        assertEquals(SAVE, areas.doubleTap(.7f, .8f, .71f, .81f));
        assertEquals(NONE, areas.doubleTap(.2f, .5f, .21f, .51f));
        assertEquals(NONE, areas.doubleTap(.2f, .3f, .2f, .41f));
        assertEquals(NONE, areas.doubleTap(.2f, .19f, .2f, .21f));
        assertEquals(NONE, areas.hit(-.1f, .5f));
        assertEquals(NONE, areas.hit(1f, .5f));
    }

    @Test
    public void editingRejectsOverlapsAndAllowsSharedEdges() {
        QuickSaveRegions areas = new QuickSaveRegions();
        assertFalse(areas.update(LOAD, new Region(.2f, .2f, .7f, .5f)));
        assertEquals(.5f, areas.get(LOAD).right, 0);
        assertTrue(areas.update(LOAD, new Region(.2f, .2f, .5f, .5f)));
        assertEquals(NONE, areas.hit(.1f, .1f));
    }

    @Test
    public void moveClampsToScreenWithoutChangingSize() {
        Region moved = new Region(.1f, .2f, .3f, .5f).move(2, -2);
        assertEquals(.8f, moved.left, .0001f);
        assertEquals(1f, moved.right, .0001f);
        assertEquals(0f, moved.top, .0001f);
        assertEquals(.3f, moved.bottom, .0001f);
    }

    @Test
    public void resizeHasIndependentAxesAndMinimumSize() {
        Region start = new Region(.1f, .2f, .4f, .6f);
        Region resized = start.resize(.2f, -.1f, .1f, .1f);
        assertEquals(.6f, resized.right, .0001f);
        assertEquals(.5f, resized.bottom, .0001f);
        Region minimum = start.resize(-2, -2, .1f, .1f);
        assertEquals(.2f, minimum.right, .0001f);
        assertEquals(.3f, minimum.bottom, .0001f);
        assertEquals(1f, start.resize(2, 2, .1f, .1f).bottom, 0);
    }

    @Test
    public void invalidStoredRegionsFallBackToDefaults() {
        Region valid = new Region(.5f, 0, 1, 1);
        Region[] invalid = {new Region(Float.NaN, 0, .4f, 1),
                new Region(-.1f, 0, .4f, 1), new Region(.4f, 0, .4f, 1),
                new Region(0, 0, .7f, 1)};
        for (Region region : invalid) {
            QuickSaveRegions areas = new QuickSaveRegions(region, valid);
            assertEquals(LOAD, areas.hit(.1f, .5f));
            assertEquals(.5f, areas.get(LOAD).right, 0);
        }
    }
}
