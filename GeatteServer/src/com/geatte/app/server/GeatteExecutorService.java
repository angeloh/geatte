package com.geatte.app.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Google appending is not allowed threads, only task queue is allowed.
 *
 */
public class GeatteExecutorService {

    private static final GeatteExecutorService mGeatteExecutorService = new GeatteExecutorService();
    private static final int CORE_POOL_SIZE = 5;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
	private final AtomicInteger mCount = new AtomicInteger(1);

	public Thread newThread(Runnable r) {
	    return new Thread(r, "Geatte thread #" + mCount.getAndIncrement());
	}
    };

    private ExecutorService mExecutorService;

    private GeatteExecutorService() {
    }

    public static GeatteExecutorService getService(){
	return mGeatteExecutorService;
    }

    public ExecutorService getExecutor() {
	if (mExecutorService == null) {
	    mExecutorService = Executors.newFixedThreadPool(CORE_POOL_SIZE, sThreadFactory);
	}
	return mExecutorService;
    }

}
