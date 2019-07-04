package com.baselib.instant.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import java.lang.Exception

class GoogleMarketUtils {
    companion object {
        private const val NEW_MARKET_VERSION_CODE = 8006027
        private const val MARKET_PACKAGE = "com.android.vending"
        // 用包名搜索market上的软件
        const val SEARCH_BY_PKGNAME = "market://search?q=pname:"
        // 直接使用关键字搜索market上的软件
        const val SEARCH_BY_KEYWORD = "market://search?q="
        // 进入软件详细页面
        private const val MARKET_APP_DETAIL = "market://details?id="
        //浏览器版本的电子市场详情地址
        private const val BROWSER_APP_DETAIL_HTTP = "http://play.google.com/store/apps/details"

        private const val BROWSER_APP_DETAIL_HTTPS = "https://play.google.com/store/apps/details"

        /**
         * 手机上是否有电子市场
         * @param context
         * @return
         */
        fun isMarketExist(context: Context): Boolean {
            return SystemUtil.isAppExist(context, MARKET_PACKAGE)
        }

        /**
         * 是否为GoogleMarket地址
         * @param url
         * @return
         */
        fun isMarketUrl(url: String): Boolean {
            return !TextUtils.isEmpty(url) && (url.contains("play.google.com") || url.contains("market://details"))
        }

        /**
         * 获取指定应用的谷歌市场链接
         * @param context
         * @param pkgName
         * @return 对应在谷歌市场的链接
         */
        fun getAppDetailUrl(context: Context, pkgName: String): String? {
            if (TextUtils.isEmpty(pkgName)) {
                return null
            }
            return if (isMarketExist(context)) {
                MARKET_APP_DETAIL + pkgName
            } else {
                "$BROWSER_APP_DETAIL_HTTPS?id=$pkgName"
            }
        }

        fun jumpToMarket(context: Context, uriString: String, openByBrowser: Boolean, jumpListenerImpl: JumpListener) {
            if (TextUtils.isEmpty(uriString)) {
                jumpListenerImpl.fail()
            } else {
                if (isMarketExist(context)) {
                    try {
                        if (uriString.startsWith(BROWSER_APP_DETAIL_HTTP) || uriString.startsWith(BROWSER_APP_DETAIL_HTTPS)) {
                            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getSubUri(uriString)))
                            marketIntent.setPackage(MARKET_PACKAGE)

                            if (context is Activity) {
                                marketIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                            } else {
                                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                marketIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            context.startActivity(marketIntent)
                            jumpListenerImpl.success()
                        } else {
                            jumpUrlByBrowser(context, uriString, jumpListenerImpl)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        jumpUrlByBrowser(context, uriString, jumpListenerImpl)
                    }
                } else {
                    if (openByBrowser) {
                        jumpUrlByBrowser(context, uriString, jumpListenerImpl)
                    } else {
                        jumpListenerImpl.fail()
                    }
                }
            }
        }

        private fun getSubUri(uriString: String): String {
            var subUri = ""
            //判断是否为Market开头的Uri,如果不是,则进行转换.
            if (uriString.startsWith(BROWSER_APP_DETAIL_HTTP) || uriString.startsWith(BROWSER_APP_DETAIL_HTTPS)) {
                val start = uriString.indexOf("id=")
                subUri = uriString.substring(start + "id=".length)
            }

            subUri = MARKET_APP_DETAIL + subUri
            return subUri
        }


        /**
         * 通过浏览器打开Google Market
         * @param context 上下文
         * @param uriString 链接
         * @param jumpListenerImpl 跳转监听
         */
        fun jumpUrlByBrowser(context: Context, uriString: String, jumpListenerImpl: JumpListener?) {
            var targetUri = uriString
            if (TextUtils.isEmpty(targetUri)) {
                jumpListenerImpl?.fail()
            }else{
                //修改Ur
                if (!targetUri.startsWith(BROWSER_APP_DETAIL_HTTP) && !targetUri.startsWith(BROWSER_APP_DETAIL_HTTPS)) {
                    //判断是否为Market或非http://等开始的Url,如果是,则去除.
                    if (targetUri.startsWith(MARKET_APP_DETAIL) || !targetUri.startsWith("http://") && !targetUri.startsWith("https://")) {
                        if (targetUri.startsWith(MARKET_APP_DETAIL)) {
                            targetUri = targetUri.replace(MARKET_APP_DETAIL, "?id=")
                        }
                        //将Url修改成https://play.google.com/store/apps/details?id=
                        targetUri = BROWSER_APP_DETAIL_HTTPS + targetUri
                    }
                }
                try {
                    val uri = Uri.parse(targetUri)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    //获取已安装的浏览器列表
                    val pm = context.packageManager
                    val resolveList = pm.queryIntentActivities(intent, 0)
                    //获取第一个浏览器启动
                    if (resolveList != null && resolveList.size > 0) {
                        val activityInfo = if (resolveList[0] != null) resolveList[0].activityInfo else null
                        val packageName = activityInfo?.packageName
                        val activityName = activityInfo?.name
                        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(activityName)) {
                            intent.setClassName(packageName!!, activityName!!)
                        }
                    }
                    if (context is Activity) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    } else {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) //Intent.FLAG_ACTIVITY_CLEAR_TASK  need API11
                    }
                    context.startActivity(intent)
                    jumpListenerImpl?.success()
                } catch (e: Exception) {
                    e.printStackTrace()
                    jumpListenerImpl?.fail()
                }
            }

        }

        /**
         * 跳到电子市场的我的应用界面
         * @param context
         * @return true代表跳转成功
         */
        fun jumpToMyAppInMarket(context: Context): Boolean {
            var result = false
            val marketPkgName = "com.android.vending"
            val versionCode = SystemUtil.getAppVersionCode(context, marketPkgName)
            val emarketIntent: Intent?
            if (versionCode >= NEW_MARKET_VERSION_CODE) {
                // 直接跳到电子市场我的应用界面
                emarketIntent = Intent("com.google.android.finsky.VIEW_MY_DOWNLOADS")
                emarketIntent.setClassName(marketPkgName,
                        "com.google.android.finsky.activities.MainActivity")
            } else {
                //跳转至电子市场首界面
                val packageMgr = context.packageManager
                emarketIntent = packageMgr.getLaunchIntentForPackage(marketPkgName)
            }
            try {
                emarketIntent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(emarketIntent)
                result = true
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return result
        }

        /**
         * market上查看程序信息
         *
         * @param packageName
         */
        fun jumpToAppDetailInMarket(context: Context, packageName: String) {
            var keyword: String? = MARKET_APP_DETAIL + packageName
            val uri = Uri.parse(keyword)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    interface JumpListener {
        fun fail()
        fun success()
    }
}


