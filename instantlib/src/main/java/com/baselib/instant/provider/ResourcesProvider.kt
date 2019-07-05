package com.baselib.instant.provider

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils

import com.baselib.instant.util.LogUtils


/**
 * 资源提供者
 *
 * 采用双重检查的方式实现带参数的单例模式
 *
 * @author wsb
 */
class ResourcesProvider private constructor(context: Context) {
    private val mPkgName: String = context.packageName
    private val mResources: Resources = context.resources
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    companion object {
        @Volatile
        private var instance: ResourcesProvider? = null

        fun getProvider(context: Context) {
            instance ?: synchronized(this) {
                instance ?: ResourcesProvider(context).also { instance = it }
            }
        }
    }

    fun getId(res: String): Int {
        val id = mResources.getIdentifier(res, "id", mPkgName)
        if (id == 0) {
            LogUtils.e("ResourcesProvider", "id:$res is not found")
        }
        return id
    }

    fun getLayoutId(res: String): Int {
        val id = mResources.getIdentifier(res, "layout", mPkgName)
        if (id == 0) {
            LogUtils.e("ResourcesProvider", "layout:$res is not found")
        }
        return id
    }

    fun getDrawableId(res: String): Int {
        val id = mResources.getIdentifier(res, "drawable", mPkgName)
        if (id == 0) {
            LogUtils.e("ResourcesProvider", "drawable:$res is not found")
        }
        return id
    }

    fun getColor(res: String): Int {
        val id = mResources.getIdentifier(res, "color", mPkgName)
        if (id == 0) {
            LogUtils.e("ResourcesProvider", "color:$res is not found")
        }
        return mResources.getColor(id)
    }

    fun getInteger(res: String): Int {
        val id = mResources.getIdentifier(res, "integer", mPkgName)
        if (id == 0) {
            LogUtils.e("ResourcesProvider", "integer:$res is not found")
        }
        return mResources.getInteger(id)
    }

    fun getDimension(res: String): Float {
        val id = mResources.getIdentifier(res, "dimen", mPkgName)
        if (id == 0) {
            LogUtils.e("ResourcesProvider", "dimen:$res is not found")
        }
        return mResources.getDimension(id)
    }

    fun getDimensionPixelSize(res: String): Int {
        val id = mResources.getIdentifier(res, "dimen", mPkgName)
        if (id == 0) {
            LogUtils.e("ResourcesProvider", "dimen:$res is not found")
        }
        return mResources.getDimensionPixelOffset(id)
    }

    fun getText(res: String): CharSequence {
        val id = mResources.getIdentifier(res, "string", mPkgName)
        if (id == 0) {
            LogUtils.e("ResourcesProvider", "string:$res is not found")
        }
        return mResources.getText(id)
    }

    fun getString(res: String): String {
        val id = mResources.getIdentifier(res, "string", mPkgName)
        if (id == 0) {
            LogUtils.e("ResourcesProvider", "string:$res is not found")
        }
        return mResources.getString(id)
    }

    fun getDrawable(res: String): Drawable {
        val id = mResources.getIdentifier(res, "drawable", mPkgName)
        if (id == 0) {
            LogUtils.e("ResourcesProvider", "drawable:$res is not found")
        }
        return mResources.getDrawable(id)
    }

    fun getView(res: String, root: ViewGroup): View {
        val id = mResources.getIdentifier(res, "layout", mPkgName)
        if (id == 0) {
            LogUtils.e("ResourcesProvider", "layout:$res is not found")
        }
        return mInflater.inflate(id, root)
    }

    fun getView(res: String, root: ViewGroup, attachToRoot: Boolean): View {
        val id = mResources.getIdentifier(res, "layout", mPkgName)
        if (id == 0) {
            LogUtils.e("ResourcesProvider", "layout:$res is not found")
        }
        return mInflater.inflate(id, root, attachToRoot)
    }

    fun getAnimation(context: Application, res: String): Animation {
        val id = mResources.getIdentifier(res, "anim", mPkgName)
        if (id == 0) {
            LogUtils.e("ResourcesProvider", "anim:$res is not found")
        }
        return AnimationUtils.loadAnimation(context, id)
    }

    fun getXml(res: String): XmlResourceParser {
        val id = mResources.getIdentifier(res, "xml", mPkgName)
        if (id == 0) {
            LogUtils.e("ResourcesProvider", "xml:$res is not found")
        }
        return mResources.getXml(id)
    }

    fun getBoolean(res: String): Boolean {
        val id = mResources.getIdentifier(res, "bool", mPkgName)
        if (id == 0) {
            LogUtils.e("ResourcesProvider", "bool:$res is not found")
        }
        return mResources.getBoolean(id)
    }

}
