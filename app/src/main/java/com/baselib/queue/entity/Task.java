package com.baselib.queue.entity;

public class Task {
    final int id;
    final String name;
    final long createTime;

    public Task(int id, String name) {
        this.id = id;
        this.name = name;
        this.createTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format("Task{id=%d, name=%s}", id, name);
    }
}