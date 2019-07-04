package com.baselib.mvpuse.model;

import android.content.Context;

import com.baselib.instant.manager.GlobalManager;
import com.baselib.instant.mvp.BaseModel;
/**
 * 示例代码
 * <p>
 * 在该层的方法中完成数据的解析
 *
 * @author wsb
 */
public class MainModel extends BaseModel {

    @Override
    public void detach(Context context) {
        super.detach(context);
        GlobalManager.INSTANCE.detach();
    }
}
