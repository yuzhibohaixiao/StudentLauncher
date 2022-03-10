package com.alight.android.aoa_launcher.common.event;

/**
 * @author wangzhe
 */
public class ParentControlEvent {

    public final String childId;
    public final String parentId;
    public final String title;

    public static ParentControlEvent getInstance(String childId, String parentId, String title) {
        return new ParentControlEvent(childId, parentId, title);
    }

    private ParentControlEvent(String childId, String parentId, String title) {
        this.childId = childId;
        this.parentId = parentId;
        this.title = title;
    }
}