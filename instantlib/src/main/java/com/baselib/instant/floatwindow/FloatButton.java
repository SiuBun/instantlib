package com.baselib.instant.floatwindow;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baselib.instant.R;
import com.baselib.instant.util.LogUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * time 2017/11/8  19:27
 * desc 悬浮按钮（没用到xml布局）
 * @author zgy
 */
public class FloatButton extends FrameLayout implements View.OnTouchListener {

    static final String TAG = "FloatButton";

    /**
     * 悬浮按钮半隐藏
     * */
    private static final int HANDLER_TYPE_HALF_HIDE = 2;
    private static final float DRAG_MIN_PX = 3;
    private static final int SCREEN_HALF = 2;

    /**
     * 保存创建的悬浮按钮实例
     * */
    private static FloatButton sInstance = null;

    // 悬浮控件
    /**
     * 布局容器
     * */
    FrameLayout layContainer;
    /**
     * 图标
     * */
    ImageView ivLogo;
    /**
     * 菜单子项的容器
     * */
    LinearLayout layMenu;
    /**
     * 菜单下的功能子项
     * */
    LinearLayout layAccount, layMsg, layCommunity, layCustomer, layAnnouncement,layHide;
    /**
     * 消息图标
     * */
    MsgView mMsgView;

    private boolean mDraging, mIsRight, mIsExpand;
    private int mScreenWidth, mScreenHeight;
    private float mTouchStartX, mTouchStartY, mLogoSize, mIconSize, mMargin, mTextSize;

    private FloatEventListener mFloatEventListener;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWmParams;
    /**
     * 定时器
     * */
//    private Timer mTimer = null;
    /**
     * 定时任务
     * */
    private TimerTask mTimerTask = null;
    /**
     * 定时间隔
     * */
    private final static int TIMER_INTERVAL = 3000;

    private static boolean sMsgVisible, sCommunityVisible, sCustomerVisible,sAnnouncementVisible;
    /**
     * 用户点击隐藏后，只有重启游戏才可以启动悬浮按钮
     * */
    private static boolean sHide = false;
    private ScheduledThreadPoolExecutor mScheduledThreadPoolExecutor;
    public static int sMsgCount = 1;


    /**
     * 启动悬浮按钮(单实例，重复调用可以设置子菜单项显示或隐藏的状态)
     *
     * @param context          必须为CP的Activity
     * @param msgVisible       是否显示消息
     * @param communityVisible 是否显示社区
     * @param customerVisible  是否显示客服
     * @param announcementVisible  是否显示公告
     * @param listener         监听
     */
    public static void show(Context context, boolean msgVisible, boolean communityVisible, boolean customerVisible, boolean announcementVisible,FloatEventListener listener) {
        if (sHide) {
            return;
        }

        if (context == null) {
            return;
        }

        sMsgVisible = msgVisible;
        sCommunityVisible = communityVisible;
        sCustomerVisible = customerVisible;
        sAnnouncementVisible = announcementVisible;

        if (sInstance == null) {
            sInstance = new FloatButton(context, listener);
        }
    }

    /**
     * 关闭悬浮按钮
     */
    public static void close() {
        if (sInstance != null) {
            sInstance.destory();
            sInstance = null;
        }
    }

    /**
     * 跟CP的Activity生命周期有关，显示
     */
    public static void onResume() {
        if(sInstance != null && sInstance.layContainer != null) {
            LogUtils.d(TAG, "FloatButton onResume");
            sInstance.layContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 跟CP的Activity生命周期有关，隐藏
     */
    public static void onPause() {
        if(sInstance != null && sInstance.layContainer != null) {
            LogUtils.d(TAG, "FloatButton onPause");
            sInstance.layContainer.setVisibility(View.GONE);
        }
    }




    private FloatButton(Context context, FloatEventListener listener) {
        super(context);
        mFloatEventListener = listener;
        sInstance = this;

        // 初始化窗口管理器和窗口参数
        initWmParams(context);

// 图片宽高
//        mLogoSize = (int) getRawSize(TypedValue.COMPLEX_UNIT_DIP, 45);
        // 图片宽高
        mLogoSize = getResources().getDimension(R.dimen.zo_45dp);
        // ICON宽高
        mIconSize = getResources().getDimension(R.dimen.zo_20dp);
        // ICON间距
        mMargin = getResources().getDimension(R.dimen.zo_10dp);
        // 文字大小
        mTextSize = getResources().getDimensionPixelSize(R.dimen.zo_10sp);

        // 默认在左侧
        mIsRight = false;
        // 默认收起菜单项
        mIsExpand = false;
        // 创建悬浮控件
        createView(context);
        // 以Logo为起点，菜单项从左向右排序
        itemsSortL2R();
        // 收起菜单项
        shrinkMenuIetms();

        // 将本控件添加到窗体管理器
        mWindowManager.addView(layContainer, mWmParams);

        // 定时每隔5秒检查一次，是否半隐藏悬浮按钮
        mTimerTask = createTimerTask();
//        mTimer = new Timer();
        mScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5, r -> {
            Thread thread = new Thread(r);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        });

        mScheduledThreadPoolExecutor.schedule(mTimerTask, TIMER_INTERVAL, TimeUnit.MILLISECONDS);
//        mTimer.schedule(mTimerTask, TIMER_INTERVAL, TIMER_INTERVAL);
    }

    /**
     * 初始化窗口管理器和窗口参数
     * @param context 初始化窗口参数
     * */
    private void initWmParams(Context context) {
        if (context == null) {
            return;
        }

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        // 更新浮动窗的位置参数
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        mWmParams = new WindowManager.LayoutParams();
        mWmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        mWmParams.format = PixelFormat.RGBA_8888;
        mWmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        // 调整为靠左显示
        mWmParams.gravity = Gravity.LEFT | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        mWmParams.x = 0;
        mWmParams.y = mScreenHeight / 2;
        // 设置悬浮窗口长宽数据
        mWmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }


    /**
     * 处理主按钮移动事件
     * */
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        removeTimerTask();// 删除定时任务，排除半隐藏干扰
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartX = (int) event.getX();
                mTouchStartY = (int) event.getY();
                resetSize();// 手指按下时恢复大小
                mDraging = false;
                break;

            case MotionEvent.ACTION_MOVE:
                float mMoveStartX = event.getX();
                float mMoveStartY = event.getY();
                // 移动过3个像素才算拖动，OnClick事件才屏蔽
                if (Math.abs(mTouchStartX - mMoveStartX) > DRAG_MIN_PX && Math.abs(mTouchStartY - mMoveStartY) > DRAG_MIN_PX) {
                    mDraging = true;
                    shrinkMenuIetms();// 拖动时收起菜单项
                    resetSize();// 拖动时恢复大小
                    mWmParams.x = (int) (x - mTouchStartX);
                    mWmParams.y = (int) (y - mTouchStartY);
                    mWindowManager.updateViewLayout(layContainer, mWmParams);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                    // 移到左侧
                if (mWmParams.x <= mScreenWidth / SCREEN_HALF) {
                    mWmParams.x = 0;
                    mIsRight = false;
                } else {
                    // 移到右侧
                    mWmParams.x = mScreenWidth - ivLogo.getMeasuredWidth();
                    mIsRight = true;
                }
                mWindowManager.updateViewLayout(layContainer, mWmParams);
                mTouchStartX = mTouchStartY = 0;
                // 恢复定时任务
                mTimerTask = createTimerTask();
//                mTimer.schedule(mTimerTask, TIMER_INTERVAL, TIMER_INTERVAL);
                mScheduledThreadPoolExecutor.schedule(mTimerTask, TIMER_INTERVAL, TimeUnit.MILLISECONDS);
                break;
            default:
                break;
        }

        // 此处必须返回false，否则OnClickListener获取不到监听
        return mDraging;
    }

    /**
     * 屏幕旋转时，调整悬浮按钮的位置
     * */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 主要调整悬浮按钮在右侧的情况
        if (mWindowManager != null &&
                layContainer != null && mIsRight) {
            DisplayMetrics dm = new DisplayMetrics();
            mWindowManager.getDefaultDisplay().getMetrics(dm);
            mScreenWidth = dm.widthPixels;
            mScreenHeight = dm.heightPixels;
            mWmParams.x = mScreenWidth;
            mWindowManager.updateViewLayout(layContainer, mWmParams);
        }
    }

    /**
     * 创建定时任务
     * */
    private TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = HANDLER_TYPE_HALF_HIDE;
                mTimerHandler.sendMessage(message);
            }
        };
    }

    /**
     * 处理定时器任务
     * */
    Handler mTimerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
                // 悬浮按钮半隐藏
            if (msg.what == HANDLER_TYPE_HALF_HIDE) {
                shrinkMenuIetms();// 收起菜单项
                hideHalfSize();// 半隐藏主按钮
            }
            super.handleMessage(msg);
        }
    };

    /**
     * Logo半隐藏（隐藏2/3）
     * */
    private void hideHalfSize() {
        if (mWmParams == null || mWindowManager == null || ivLogo == null) {
            return;
        }

        FrameLayout.LayoutParams vLayoutParams = (FrameLayout.LayoutParams) ivLogo.getLayoutParams();

        float m = mLogoSize / 3 * 2;

            // 右侧
        if (mIsRight) {
            mWmParams.x = (int) (mScreenWidth - mLogoSize + m);
            vLayoutParams.setMargins(0, 0, (int) -m, 0);
            ivLogo.setLayoutParams(vLayoutParams);

        } else {// 左侧
            if (vLayoutParams.leftMargin >= 0) {
                vLayoutParams.setMargins((int) -m, 0, 0, 0);
                ivLogo.setLayoutParams(vLayoutParams);
            }
        }

        try {
            mWmParams.alpha = 0.7f;
            mWindowManager.updateViewLayout(layContainer, mWmParams);
        } catch (Exception ex) {
            LogUtils.e(TAG, "WindowManager对已脱离的View操作产生异常，已捕获");
            ex.printStackTrace();
        }
    }

    /**
     * Logo恢复大小
     * */
    private void resetSize() {
        if (mWmParams == null || mWindowManager == null || ivLogo == null) {
            return;
        }

        FrameLayout.LayoutParams vLayoutParams = (FrameLayout.LayoutParams) ivLogo.getLayoutParams();
            // 右侧
        if (mIsRight) {
            mWmParams.x = (int) (mScreenWidth - mLogoSize);
        } else {// 左侧
            mWmParams.x = 0;
        }

        vLayoutParams.width = (int) mLogoSize;
        vLayoutParams.setMargins(0, 0, 0, 0);
        ivLogo.setLayoutParams(vLayoutParams);
        mWmParams.alpha = 1f;
        mWindowManager.updateViewLayout(layContainer, mWmParams);
    }

    /**
     * 移除定时器任务
     * */
    private void removeTimerTask() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    /**
     * 移除定时器
     */
    private void removeTimer() {
//        if (mTimer != null) {
//            mTimer.cancel();
//            mTimer = null;
//        }
        if (mScheduledThreadPoolExecutor!=null){
            mScheduledThreadPoolExecutor.shutdown();
            mScheduledThreadPoolExecutor = null;
        }
    }

    /**
     * 销毁时候移除任务队列,移除按钮布局
     * */
    private void destory() {
        removeTimerTask();
        removeTimer();
        if (mWindowManager != null) {
            try {
                mWindowManager.removeView(layContainer);
                mWindowManager = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 展开菜单项
     * */
    private void expandMenuItems() {
        if (layMenu == null || layContainer == null) {
            return;
        }
        layContainer.getBackground().setAlpha(250);
        layMenu.setVisibility(View.VISIBLE);
        mIsExpand = true;
        if (mIsRight) {
            itemsSortR2L();
        } else {
            itemsSortL2R();
        }
        // 设置消息数量
        setMsgNum(sMsgCount);
    }

    /**
     * 收起菜单项
     * */
    private void shrinkMenuIetms() {
        if (layMenu == null || layContainer == null) {
            return;
        }
        layContainer.getBackground().setAlpha(0);
        layMenu.setVisibility(View.GONE);
        mIsExpand = false;
    }


    /**
     * 创建悬浮按钮控件，并设置监听
     * @param context 上下文
     * */
    private void createView(Context context) {
        if (context == null) {
            return;
        }

        // 创建根容器
        layContainer = new FrameLayout(context);
        // 设置背景图片
        layContainer.setBackgroundResource(R.drawable.zo_menu_bg);
        layContainer.setPadding(0, 0, 0, 0);
        layContainer.setClipChildren(false);
        layContainer.setClipToPadding(false);

        // 创建Logo图标
        createLogo(context);

        // 菜单容器
        layMenu = new LinearLayout(context);
        layMenu.setOrientation(LinearLayout.HORIZONTAL);

        // 用户信息
        layAccount = setMenuItem(context,layAccount,"account",R.drawable.zo_float_person, FloatButtonController.USER_CENTER_VIEW);
        // 消息
        if (sMsgVisible) {
            layMsg = setMenuItem(context,layMsg,"feedback",R.drawable.zo_float_msg,FloatButtonController.FEEDBACK_VIEW);
        }

        // 社区
        if (sCommunityVisible) {
            layCommunity = setMenuItem(context,layCommunity,"community",R.drawable.zo_float_shequ,FloatButtonController.COMMUNITY_VIEW);
        }

        // 客服
        if (sCustomerVisible) {
            layCustomer = setMenuItem(context,layCustomer,"customerservice",R.drawable.zo_float_kefu,FloatButtonController.CUSTOMER_SERVICE_VIEW);
        }
        // 公告
        if (sAnnouncementVisible) {
            layAnnouncement = setMenuItem(context,layAnnouncement,"announcement",R.drawable.zo_float_announcement,FloatButtonController.ANNOUNCEMENT_VIEW);
        }

        // 隐藏
        layHide = createMenuItem(context, "hide", R.drawable.zo_float_hide, false);
        layHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shrinkMenuIetms();// 收起菜单项
                sHide = true;
                if (mFloatEventListener != null) {
                    mFloatEventListener.eventCode(FloatButtonController.HIDE_FLOAT_WINDOW_VIEW);
                }
            }
        });

        // 先添加菜单容器，再添加Logo，这样Logo在帧布局顶层
        layContainer.addView(layMenu);
        layContainer.addView(ivLogo);
    }

    private LinearLayout setMenuItem(Context context, LinearLayout group, String resourceText, @DrawableRes int drawable, final int exentCode){
        // 用户信息
        group = createMenuItem(context, resourceText, drawable, false);
        group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shrinkMenuIetms();// 收起菜单项
                if (mFloatEventListener != null) {
                    mFloatEventListener.eventCode(exentCode);
                }
            }
        });
        return group;
    }

    /**
     * 创建悬浮按钮Logo，并设置拖放、点击展开或收起菜单的监听器
     * @param context 上下文
     * */
    private void createLogo(Context context) {
        ivLogo = new ImageView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) mLogoSize, (int) mLogoSize);
        ivLogo.setLayoutParams(params);
        ivLogo.setImageResource(R.drawable.zo_new_float3_1_5);
        // 设置拖放监听
        ivLogo.setOnTouchListener(this);
        ivLogo.setOnClickListener(v -> {
                // 已展开，则收起
            if (mIsExpand) {
                shrinkMenuIetms();
            } else {
                // 已收起，则展开
                expandMenuItems();
            }
        });
    }

    /**
     * 创建悬浮按钮菜单子项
     *
     * @param context 上下文
     * @param text 文案
     * @param drawableResID 子项图片资源
     * @param useMsgView 是否使用带消息提示的控件
     * */
    private LinearLayout createMenuItem(Context context, String text, int drawableResID, boolean useMsgView) {

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins((int) mMargin, 0, (int) mMargin, 0);
        LinearLayout lay = new LinearLayout(context);
        lay.setLayoutParams(params);
        lay.setOrientation(LinearLayout.VERTICAL);

        params = new LinearLayout.LayoutParams((int) mIconSize, (int) mIconSize);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        // 图片
            // 使用带消息提示的控件
        if (useMsgView) {
            mMsgView = new MsgView(context);
            mMsgView.setLayoutParams(params);
            mMsgView.setImageResource(drawableResID);
            lay.addView(mMsgView);

        } else {// 使用图片控件
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(params);
            imageView.setImageResource(drawableResID);
            lay.addView(imageView);
        }

        // 文字
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        TextView textView = new TextView(context);
        textView.setLayoutParams(params);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        textView.setTextColor(Color.WHITE);
        textView.setText(text);

        lay.addView(textView);

        return lay;
    }

    /**
     * 以Logo为起点，菜单项从左向右排序
     * */
    private void itemsSortL2R() {
        if (layMenu == null || ivLogo == null) {
            return;
        }

        // 删除子控件，再重新按顺序添加
        layMenu.removeAllViews();

        // Logo在左侧
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) mLogoSize, (int) mLogoSize);
        params.gravity = Gravity.LEFT;
        ivLogo.setLayoutParams(params);

        // 菜单在右侧
        params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, (int) mLogoSize);
        params.gravity = Gravity.RIGHT;
        params.setMargins((int) (mLogoSize + mMargin), 0, 0, 0);
        layMenu.setLayoutParams(params);

        if (layAccount != null) {
            layMenu.addView(layAccount);
        }
        if (sMsgVisible && layMsg != null) {
            layMenu.addView(layMsg);
        }
        if (sCommunityVisible && layCommunity != null) {
            layMenu.addView(layCommunity);
        }
        if (sCustomerVisible && layCustomer != null) {
            layMenu.addView(layCustomer);
        }
        if (sAnnouncementVisible && layAnnouncement != null) {
            layMenu.addView(layAnnouncement);
        }
        if (layHide != null) {
            layMenu.addView(layHide);
        }

        // 将右边距与“隐藏标签”调宽
        layMenu.setPadding(0, 0, (int) mMargin, 0);
    }

    /**
     * // 以Logo为起点，菜单项从右向左排序
     * */
    private void itemsSortR2L() {
        if (layMenu == null || ivLogo == null) {
            return;
        }

        // 删除子控件，再重新按顺序添加
        layMenu.removeAllViews();

        // Logo在右侧
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) mLogoSize, (int) mLogoSize);
        params.gravity = Gravity.RIGHT;
        ivLogo.setLayoutParams(params);

        // 菜单在左侧
        params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, (int) mLogoSize);
        params.gravity = Gravity.LEFT;
        params.setMargins(0, 0, (int) (mLogoSize + mMargin), 0);
        layMenu.setLayoutParams(params);

        if (layHide != null) {
            layMenu.addView(layHide);
        }
        if (sAnnouncementVisible && layAnnouncement != null) {
            layMenu.addView(layAnnouncement);
        }
        if (sCustomerVisible && layCustomer != null) {
            layMenu.addView(layCustomer);
        }
        if (sCommunityVisible && layCommunity != null) {
            layMenu.addView(layCommunity);
        }
        if (sMsgVisible && layMsg != null) {
            layMenu.addView(layMsg);
        }
        if (layAccount != null) {
            layMenu.addView(layAccount);
        }

        // 将左边距与“隐藏标签”调宽
        layMenu.setPadding((int) mMargin, 0, 0, 0);
    }

    /**
     * 设置消息数量
     * @param num 数量
     * */
    private void setMsgNum(int num) {
        if (sInstance != null && sInstance.mMsgView != null) {
            sInstance.mMsgView.setNum(num);
        }
    }


    /**
     * 刷新悬浮球消息数量
     */
    public static void refreshMsg(Context context) {
        if (context != null) {
            return;
        }
        sMsgCount = 2;
    }

}
