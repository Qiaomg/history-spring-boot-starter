package net.shopin.context;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @title: HistoryClientContext
 * @description: TODO(用一句话描述该文件做什么)
 * @author: qmg
 * @date: 2020/6/18 14:27
 * @version: V1.0
 */
public class HistoryClientContext {

    /**
     * 工作线程池
     */
    public static ExecutorService executorService = new ThreadPoolExecutor(2, 5, 5, TimeUnit.MINUTES, new SynchronousQueue<>(),new ThreadFactoryBuilder().setNameFormat("worker-%d").build(),new ThreadPoolExecutor.AbortPolicy());
}
