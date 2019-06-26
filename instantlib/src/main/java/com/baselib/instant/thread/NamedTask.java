package com.baselib.instant.thread;

/**
 * 拥有名称的Runnable
 * @author wsb
 */
public class NamedTask implements Runnable {

    public Runnable mTask;
    public int mPriority = Thread.NORM_PRIORITY;
    public String mThreadName;

    public NamedTask(Runnable task) {
        mTask = task;
    }

    @Override
    public void run() {
        mTask.run();
    }
}