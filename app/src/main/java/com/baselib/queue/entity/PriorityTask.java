package com.baselib.queue.entity;

/**
 * 优先级任务类
 */
public class PriorityTask implements Comparable<PriorityTask> {
    final int priority;
    final String name;

    public PriorityTask(int priority, String name) {
        this.priority = priority;
        this.name = name;
    }

    @Override
    public int compareTo(PriorityTask other) {
        return Integer.compare(other.priority, this.priority); // 高优先级在前
    }

    @Override
    public String toString() {
        return String.format("PriorityTask{priority=%d, name=%s}", priority, name);
    }
}