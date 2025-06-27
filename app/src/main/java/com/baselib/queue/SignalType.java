package com.baselib.queue;

// 信令类型定义
public enum SignalType {
    GIFT("送礼", 3),     // 最重要：影响收入
    FOLLOW("关注", 2),   // 重要：用户增长
    ENTER("进场", 1),    // 一般：实时互动
    COMMENT("评论", 1),  // 一般：内容互动
    LIKE("点赞", 0);     // 不重要：可丢弃

    private final String desc;
    private final int priority;

    SignalType(String desc, int priority) {
        this.desc = desc;
        this.priority = priority;
    }

    public String getDesc() {
        return desc;
    }

    public int getPriority() {
        return priority;
    }
}