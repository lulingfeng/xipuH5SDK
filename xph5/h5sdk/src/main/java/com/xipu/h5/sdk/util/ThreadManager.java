package com.xipu.h5.sdk.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {

    private static ThreadManager mInstance;
    private static ExecutorService mFixedPoolThread;
    private static ExecutorService mSingleThreadPool;
    private static ExecutorService mCacheThreadPool;

    public static ThreadManager getInstance() {
        if (mInstance == null) {
            synchronized (ThreadManager.class) {
                if (mInstance == null) {
                    mInstance = new ThreadManager();
                }
            }
        }
        return mInstance;
    }

    // 创建核心线程池
    public ExecutorService getFixedPoolThread(){
        if(mFixedPoolThread == null) {
            mFixedPoolThread = Executors.newFixedThreadPool(3);
        }
        return mFixedPoolThread;
    }

    // 创建单线程线程池
    public ExecutorService getSinglePoolThread(){
        if(mSingleThreadPool == null) {
            mSingleThreadPool = Executors.newSingleThreadExecutor();
        }
        return mSingleThreadPool;
    }

    // 创建非核心线程池
    public ExecutorService getCachePoolThread(){
        if(mCacheThreadPool == null) {
            mCacheThreadPool = Executors.newCachedThreadPool();
        }
        return mCacheThreadPool;
    }
}
