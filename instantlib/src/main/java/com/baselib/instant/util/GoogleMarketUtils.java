package com.baselib.instant.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

/**
 * google电子市场工具类
 *
 * @author matt
 * @date: 2015年1月30日
 *
 */
public class GoogleMarketUtils {
	public static final int NEW_MARKET_VERSION_CODE = 8006027;
	public static final String MARKET_PACKAGE = "com.android.vending";
	// 用包名搜索market上的软件
	public static final String SEARCH_BY_PKGNAME = "market://search?q=pname:";
	// 直接使用关键字搜索market上的软件
	public static final String SEARCH_BY_KEYWORD = "market://search?q=";
	// 进入软件详细页面
	public static final String MARKET_APP_DETAIL = "market://details?id=";
	//浏览器版本的电子市场详情地址
	public static final String BROWSER_APP_DETAIL_HTTP = "http://play.google.com/store/apps/details";
	public static final String BROWSER_APP_DETAIL_HTTPS = "https://play.google.com/store/apps/details";
	
	/**
	 * 手机上是否有电子市场
	 * @param context
	 * @return
	 */
	public static boolean isMarketExist(Context context) {
		return SystemUtil.isAppExist(context, MARKET_PACKAGE);
	}
	
	/**
	 * 是否为GoogleMarket地址
	 * @param url
	 * @return
	 */
	public static boolean isMarketUrl(String url) {
		if (!TextUtils.isEmpty(url)) {
			if (url.contains("play.google.com") || url.contains("market://details")) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获取指定应用的谷歌市场链接
	 * @param context
	 * @param pkgName
	 * @return
	 */
	public static String getAppDetailUrl(Context context, String pkgName) {
		if (context == null || TextUtils.isEmpty(pkgName)) {
			return null;
		}
		String uriString = null;
		//有Gp市场，则直接组装地址跳gp
		if (isMarketExist(context)) {
			uriString = MARKET_APP_DETAIL + pkgName;
		} else {
			//否则跳浏览器gp
			uriString = BROWSER_APP_DETAIL_HTTPS + "?id=" + pkgName;
		}
		return uriString;
	}
	
	/**
	 * 谷歌市场打开结果
	 *
	 * @author chenchongji
	 * 2016年3月14日
	 */
	public static enum MarketOpenResult {
		/**
		 * 成功打开google play app
		 */
		success_googleplay,
		/**
		 * 成功打开浏览器
		 */
		success_browser,
		/**
		 * 失败
		 */
		fail
	}

	@SuppressLint("NewApi")
	public static MarketOpenResult gotoMarket(Context context, String uriString, boolean isOpenBrowser) {
		if (context == null || TextUtils.isEmpty(uriString)) {
			return MarketOpenResult.fail;
		}
		//判断Google市场是否安装
		if (isMarketExist(context)) {
			try {
				//判断是否为Market开头的Uri,如果不是,则进行转换.
				if (!uriString.startsWith(MARKET_APP_DETAIL)) {
					if (uriString.startsWith(BROWSER_APP_DETAIL_HTTP)) {
						int start = uriString.indexOf("id=");
						uriString = uriString.substring(start + "id=".length());
					} else if (uriString.startsWith(BROWSER_APP_DETAIL_HTTPS)) {
						int start = uriString.indexOf("id=");
						uriString = uriString.substring(start + "id=".length());
					} else if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
					    return gotoBrowser(context, uriString) ? MarketOpenResult.success_browser : MarketOpenResult.fail;
					}
					uriString = MARKET_APP_DETAIL + uriString;
				}
				Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
				
                marketIntent.setPackage(MARKET_PACKAGE);
                if (context instanceof Activity) {
                    marketIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                } else {
                    marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    marketIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                context.startActivity(marketIntent);
				return MarketOpenResult.success_googleplay;
			} catch (Exception e) {
				gotoBrowser(context, uriString);
			}
		} else if (isOpenBrowser) {
			//使用浏览器打开
			return gotoBrowser(context, uriString) ? MarketOpenResult.success_browser : MarketOpenResult.fail;
		}
		return MarketOpenResult.fail;
	}
	
	/**
	 * 通过浏览器打开Google Market
	 * @param context
	 * @param uriString
	 * @return
	 */
	private static boolean gotoBrowser(Context context, String uriString) {
		if (context == null || TextUtils.isEmpty(uriString)) {
			return false;
		}
		//修改Ur
		if (!uriString.startsWith(BROWSER_APP_DETAIL_HTTP) && !uriString.startsWith(BROWSER_APP_DETAIL_HTTPS)) {
			//判断是否为Market或非http://等开始的Url,如果是,则去除.
			if (uriString.startsWith(MARKET_APP_DETAIL) || (!uriString.startsWith("http://") && !uriString.startsWith("https://"))) {
				if (uriString.startsWith(MARKET_APP_DETAIL)) {
					uriString = uriString.replace(MARKET_APP_DETAIL, "?id=");
				}
				//将Url修改成https://play.google.com/store/apps/details?id=
	            uriString = BROWSER_APP_DETAIL_HTTPS + uriString;
			}
		}
		try {
			Uri uri = Uri.parse(uriString);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			//获取已安装的浏览器列表
			PackageManager pm = context.getPackageManager();
			List<ResolveInfo> resolveList = pm.queryIntentActivities(intent, 0);
			//获取第一个浏览器启动
			if (resolveList != null && resolveList.size() > 0) {
				ActivityInfo activityInfo = resolveList.get(0) != null ? resolveList.get(0).activityInfo : null;
				String packageName = activityInfo != null ? activityInfo.packageName : null;
				String activityName = activityInfo != null ? activityInfo.name : null;
				if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(activityName)) {
					intent.setClassName(packageName, activityName);
				}
			}
			if (context instanceof Activity) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			} else {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); //Intent.FLAG_ACTIVITY_CLEAR_TASK  need API11
			}
			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 跳到电子市场的我的应用界面
	 * @param context
	 * @return
	 */
	public static boolean gotoMarketMyApp(Context context) {
		boolean result = false;
		if (context == null) {
			return result;
		}
		String marketPkgName = "com.android.vending";
		int versionCode = SystemUtil.getAppVersionCode(context, marketPkgName);
		Intent emarketIntent = null;
		if (versionCode >= NEW_MARKET_VERSION_CODE) {
			// 直接跳到电子市场我的应用界面
			emarketIntent = new Intent("com.google.android.finsky.VIEW_MY_DOWNLOADS");
			emarketIntent.setClassName(marketPkgName,
					"com.google.android.finsky.activities.MainActivity");
		} else {
			//跳转至电子市场首界面
			PackageManager packageMgr = context.getPackageManager();
			emarketIntent = packageMgr.getLaunchIntentForPackage(marketPkgName);
		}
		try {
			emarketIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(emarketIntent);
			result = true;
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * market上查看程序信息
	 * 
	 * @param packageName
	 */
	public static void gotoMarketAppDetail(Context context, String packageName) {
		String keyword = MARKET_APP_DETAIL + packageName;
		Uri uri = Uri.parse(keyword);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		keyword = null;
	}
}