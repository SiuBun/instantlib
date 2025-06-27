package com.baselib.queue.simple;

import com.baselib.queue.entity.Signal;
import com.baselib.queue.entity.SignalType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CommonOperation {
    /**
     * 生成测试信令 - 模拟真实直播间信令分布
     */
    public static List<Signal> generateSignals(int count) {
        List<Signal> signals = new ArrayList<>();
        Random random = new Random();

        // 真实直播间信令比例
        // 送礼：1%（最重要但数量少）
        // 关注：4%（重要，影响粉丝增长）
        // 进场：15%（人员流动）
        // 评论：20%（用户互动）
        // 点赞：60%（最多但不重要）

        int giftCount = (int) (count * 0.01);
        int followCount = (int) (count * 0.04);
        int enterCount = (int) (count * 0.15);
        int commentCount = (int) (count * 0.20);
        int likeCount = count - giftCount - followCount - enterCount - commentCount;

        // 生成送礼信令
        for (int i = 0; i < giftCount; i++) {
            signals.add(new Signal(SignalType.GIFT, "user_" + random.nextInt(5000),
                    "火箭x" + (1 + random.nextInt(10))));
        }

        // 生成关注信令
        for (int i = 0; i < followCount; i++) {
            signals.add(new Signal(SignalType.FOLLOW, "user_" + random.nextInt(5000), "关注"));
        }

        // 生成进场信令
        for (int i = 0; i < enterCount; i++) {
            signals.add(new Signal(SignalType.ENTER, "user_" + random.nextInt(5000), "进入直播间"));
        }

        // 生成评论信令
        String[] comments = {"666", "主播好美", "什么时候下播", "刷个火箭", "第一次来"};
        for (int i = 0; i < commentCount; i++) {
            signals.add(new Signal(SignalType.COMMENT, "user_" + random.nextInt(5000),
                    comments[random.nextInt(comments.length)]));
        }

        // 生成点赞信令
        for (int i = 0; i < likeCount; i++) {
            signals.add(new Signal(SignalType.LIKE, "user_" + random.nextInt(5000), "点赞"));
        }

        // 随机打乱，模拟真实接收顺序
        Collections.shuffle(signals);

        return signals;
    }
    public static void handleSignal(Signal signal) throws InterruptedException {
            // 模拟不同信令的处理时间
            switch (signal.getType()) {
                case GIFT:
                    Thread.sleep(30);
                    break;
                case FOLLOW:
                    Thread.sleep(20);
                    break;
                case ENTER:
                    Thread.sleep(10);
                    break;
                case COMMENT:
                    Thread.sleep(15);
                    break;
                case LIKE:
                    Thread.sleep(5);
                    break;
            }

            // System.out.printf("✅ 处理完成：%s - %s%n",
            //     signal.getType().getDesc(), signal.getUserId());

    }
}
