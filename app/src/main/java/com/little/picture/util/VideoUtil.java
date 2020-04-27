package com.little.picture.util;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.fos.fosmvp.common.utils.StringUtils;

/**
 * Created by xxjsb on 2020/4/16.
 */

public class VideoUtil {
    /**
     * 获取视频文件截图
     *
     * @param path 视频文件的路径
     * @return Bitmap 返回获取的Bitmap
     */
    public static Bitmap getVideoThumb(String path) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(path);

        return media.getFrameAtTime();
    }

    /**
     * 获取视频文件缩略图 API>=8(2.2)
     *
     * @param path 视频文件的路径
     * @param kind 缩略图的分辨率：MINI_KIND、MICRO_KIND、FULL_SCREEN_KIND
     * @return Bitmap 返回获取的Bitmap
     */
    public static Bitmap getVideoThumb2(String path, int kind) {
        return ThumbnailUtils.createVideoThumbnail(path, kind);
    }

    public static Bitmap getVideoThumb2(String path) {
        return getVideoThumb2(path, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
    }

    public static String formatVideoTime(String duration) {
        String result = "";
        if (StringUtils.isEmpty(duration)){
            return result;
        }
        int d = Integer.parseInt(duration)/1000;
        int m = d%60;
        int f = d/60;
        if (m<10){
            result = f+":0"+m;
        }else {
            result = f+":"+m;
        }
        return result;
    }
}
