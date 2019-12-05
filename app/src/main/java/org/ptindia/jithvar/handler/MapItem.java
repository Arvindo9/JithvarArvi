package org.ptindia.jithvar.handler;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Arvindo Mondal on 14/8/17.
 * Company name Jithvar
 * Email arvindo@jithvar.com
 */
public class MapItem implements ClusterItem {
    private final LatLng mPosition;
    public int iconImage;
    private String mTitle;
    private String mSnippet;

    public MapItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    public MapItem(double lat, double lng, String title, String snippet, int iconImage) {
        this.iconImage = iconImage;
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

}
