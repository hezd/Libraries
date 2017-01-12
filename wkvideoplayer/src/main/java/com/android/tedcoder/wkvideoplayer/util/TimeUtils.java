package com.android.tedcoder.wkvideoplayer.util;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 格式化时间
 * Created by wmm on 2016/11/28.
 */
public class TimeUtils {

    /**
     * 格式化时间
     *
     * @param time
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    private static String formatPlayTime(long time) {
        DateFormat formatter = new SimpleDateFormat("mm:ss");
        return formatter.format(new Date(time));
    }

    /**
     * 格式化播放时间
     *
     * @param playSecond
     * @param allSecond
     * @return
     */
    public static String getPlayTime(int playSecond, int allSecond) {
        String playSecondStr = "00:00";
        String allSecondStr = "00:00";
        if (playSecond > 0) {
            playSecondStr = formatPlayTime(playSecond);
        }
        if (allSecond > 0) {
            allSecondStr = formatPlayTime(allSecond);
        }
        return playSecondStr + "/" + allSecondStr;
    }

}
