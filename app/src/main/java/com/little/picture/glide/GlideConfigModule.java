package com.little.picture.glide;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.fos.fosmvp.common.utils.LogUtils;
import com.little.picture.PictureStartManager;

import java.io.File;

@GlideModule
public class GlideConfigModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context,@NonNull GlideBuilder builder) {
        String folder = PictureStartManager.getImageFolder()+"glide_cache";
        File f = new File(folder);
        if (!f.exists()){
            f.mkdirs();
        }
        //磁盘缓存
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, folder, ConfigConstants.MAX_CACHE_DISK_SIZE));
        //指定内存缓存大小
        builder.setMemoryCache(new LruResourceCache(ConfigConstants.MAX_CACHE_MEMORY_SIZE));
        //全部的内存缓存用来作为图片缓存
        builder.setBitmapPool(new LruBitmapPool(ConfigConstants.MAX_CACHE_MEMORY_SIZE));

        LogUtils.e("------GlideConfigModule set-------");
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }



}
