/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppExecutors {
    // 单例模式
    private static volatile AppExecutors instance;
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    private static final int IO_BOUND_THREAD_COUNT = CPU_CORES * 2;
    // 后台线程
    private final ExecutorService backgroundCachedExecutor;
    private final ExecutorService backgroundFixedExecutor;
    // 定时线程
    private final ScheduledExecutorService scheduledTaskExecutor;

    private AppExecutors() {
        backgroundCachedExecutor =Executors.newCachedThreadPool();
        backgroundFixedExecutor = Executors.newFixedThreadPool(IO_BOUND_THREAD_COUNT);
        scheduledTaskExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * 获取 AppExecutors 的单例实例
     * @return AppExecutors 实例
     */
    public static AppExecutors getInstance() {
        if (instance == null) {
            synchronized (AppExecutors.class) {
                if (instance == null) {
                    instance = new AppExecutors();
                }
            }
        }
        return instance;
    }

    /**
     * 获取用于后台任务的 ExecutorService
     * @return ExecutorService
     */
    public ExecutorService getBackgroundCachedExecutor() {
        return backgroundCachedExecutor;
    }

    /**
     * 获取用于后台任务的 ExecutorService
     * @return ExecutorService
     */
    public ExecutorService getBackgroundFixedExecutor() {
        return backgroundFixedExecutor;
    }

    /**
     * 获取用于定时任务的 ScheduledExecutorService
     * @return ScheduledExecutorService
     */
    public ScheduledExecutorService getScheduledTaskExecutor() {
        return scheduledTaskExecutor;
    }

    /**
     * 关闭所有管理的线程池。
     */
    public void shutdown() {
        shutdownExecutor(backgroundFixedExecutor);
        shutdownExecutor(backgroundCachedExecutor);
        shutdownExecutor(scheduledTaskExecutor);
    }

    /**
     * 关闭指定线程池。
     * @param executor 线程池
     */
    private void shutdownExecutor(ExecutorService executor) {
        if (!executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    // 强制关闭
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}