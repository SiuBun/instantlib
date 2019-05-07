package com.baselib.instant.floatwindow;

import android.app.Activity;

import com.baselib.mvpuse.manager.IManager;

/**
 * 悬浮窗管理对象
 *
 * @author wsb
 */
public class FloatButtonController implements IManager {

    public static final int USER_CENTER_VIEW = 0;
    public static final int COMMUNITY_VIEW = 1;
    public static final int CUSTOMER_SERVICE_VIEW = 2;
    public static final int HIDE_FLOAT_WINDOW_VIEW = 3;
    public static final int MSG_CENTER_VIEW = 4;
    public static final int ANNOUNCEMENT_VIEW = 5;
    public static final int FEEDBACK_VIEW = 6;

    /**
     * 悬浮窗展示类型
     * */
    private AbstractShowMode mShowType;


    public FloatButtonController setShowType(AbstractShowMode showType) {
        mShowType = showType;
        return this;
    }

    /**
     * 悬浮窗按钮展示
     * <p>
     * 根据当前配置和用户的身份决定是否展示
     *
     * @param activity 展示所用上下文
     */
    public void showFloatButton(final Activity activity) {
        FloatEventListener listener = new FloatEventListener() {
            @Override
            public void eventCode(int code) {
                getEventDispatcher(code).dispatchEvent(activity);
            }
        };
        mShowType.showFloatButton(activity, listener);
    }

    public void closeFloatButton() {
        mShowType.closeFloatButton();
    }

    /**
     * 根据用户类型显示对应状态内容的悬浮窗按钮
     *
     * @param isTourists 是否为游客身份
     * @return 展示的悬浮窗类型
     */
    public static AbstractShowMode getShowType(boolean isTourists) {
        AbstractShowMode type;
        if (isTourists) {
            type = new TouristsFloatMode();
        } else {
            type = new NormalFloatMode();
        }
        return type;
    }

    /**
     * 获取弹窗按钮对应的处理方式
     *
     * @param code 按钮事件
     * @return 处理方式对象
     */
    private AbstractEventDispatcher getEventDispatcher(int code) {
        AbstractEventDispatcher eventDispatcher;
        switch (code) {
            case USER_CENTER_VIEW:
                eventDispatcher = new UserCenterDispatcher();
                break;
            case COMMUNITY_VIEW:
                eventDispatcher = new CommunityDispatcher();
                break;
            case CUSTOMER_SERVICE_VIEW:
                eventDispatcher = new CustomerServiceDispatcher();
                break;
            case HIDE_FLOAT_WINDOW_VIEW:
                eventDispatcher = new HideFloatDispatcher();
                break;
            case MSG_CENTER_VIEW:
                eventDispatcher = new MsgCenterDispatcher();
                break;
            case ANNOUNCEMENT_VIEW:
                eventDispatcher = new AnnouncementDispatcher();
                break;
            case FEEDBACK_VIEW:
                eventDispatcher = new FeedbackDispatcher();
                break;
            default:
                eventDispatcher = new DefaultDispater();
                break;
        }
        return eventDispatcher;
    }

    @Override
    public void detach() {
        mShowType = null;
    }
}
