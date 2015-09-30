package net.gouline.dagger2demo.activity;

/**
 * Created by clkim on 9/22/15
 */
public class AlbumItem {

    private String name;
    private String url;

    public AlbumItem(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
