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
    private final ExecutorService backgroundExecutor;
    // 用定时线程
    private final ScheduledExecutorService scheduledTaskExecutor;

    private AppExecutors() {
        backgroundExecutor = Executors.newFixedThreadPool(IO_BOUND_THREAD_COUNT);
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
     * 获取用于后台和非调度任务的 ExecutorService
     * @return ExecutorService
     */
    public ExecutorService getBackgroundExecutor() {
        return backgroundExecutor;
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
        System.out.println("Shutting down AppExecutors...");
        // 关闭backgroundExecutor
        if (!backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
            try {
                // 等待所有任务完成，最长等待5秒
                if (!backgroundExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    // 如果超时，则强制关闭
                    backgroundExecutor.shutdownNow();
                    System.err.println("Background IO Executor did not terminate in time, forced shutdown.");
                }
            } catch (InterruptedException e) {
                backgroundExecutor.shutdownNow(); // 线程中断，强制关闭
                Thread.currentThread().interrupt(); // 恢复中断状态
                System.err.println("Background IO Executor shutdown interrupted.");
            }
        }
        //关闭scheduledTaskExecutor
        if (!scheduledTaskExecutor.isShutdown()) {
            scheduledTaskExecutor.shutdownNow();
            try {
                if (!scheduledTaskExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Scheduled Task Executor did not terminate in time, forced shutdown.");
                }
            } catch (InterruptedException e) {
                scheduledTaskExecutor.shutdownNow();
                Thread.currentThread().interrupt();
                System.err.println("Scheduled Task Executor shutdown interrupted.");
            }
        }
        System.out.println("AppExecutors shut down complete.");
    }
}