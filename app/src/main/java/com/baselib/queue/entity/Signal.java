package com.baselib.queue.entity;

// 信令数据结构
public class Signal {
    private final SignalType type;
    private final String userId;
    private final String content;
    private final long timestamp;
    private final long createTime;

    public Signal(SignalType type, String userId, String content) {
        this.type = type;
        this.userId = userId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.createTime = System.currentTimeMillis();
    }

    public SignalType getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getCreateTime() {
        return createTime;
    }

    @Override
    public String toString() {
        return String.format("Signal{type=%s, userId=%s}", type.getDesc(), userId);
    }
}