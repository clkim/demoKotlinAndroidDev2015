package net.gouline.dagger2demo.activity;

import android.graphics.Bitmap;

/**
 * Created by clkim on 9/22/15.
 */
public class AlbumItem {

    private Bitmap bitMap;
    private String name;

    public AlbumItem(Bitmap bitmap, String name) {
        this.bitMap = bitmap;
        this.name = name;
    }

    public Bitmap getBitMap() {
        return bitMap;
    }

    public void setBitMap(Bitmap bitMap) {
        this.bitMap = bitMap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
