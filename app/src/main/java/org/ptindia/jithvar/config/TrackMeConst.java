package org.ptindia.jithvar.config;

/**
 * Created by root on 27/6/17.
 */


public class TrackMeConst {

    private static boolean isTrackEnable = false;
    private static String userId;


    public static boolean isTrackEnable() {
        return isTrackEnable;
    }

    public static void setIsTrackEnable(boolean isTrackEnable) {
        TrackMeConst.isTrackEnable = isTrackEnable;
    }


    public static String getUserId1() {
        return userId;
    }

    public static void setUserId(String userId) {
        TrackMeConst.userId = userId;
    }
}
