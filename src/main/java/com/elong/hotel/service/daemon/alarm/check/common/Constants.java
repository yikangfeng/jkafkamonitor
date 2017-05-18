/**   
 * @(#)Constants.java	2017年1月11日	上午10:23:24	   
 *     
 * Copyrights (C) 2017艺龙旅行网保留所有权利
 */
package com.elong.hotel.service.daemon.alarm.check.common;

import java.io.File;

import com.elong.hotel.service.daemon.commons.GlobalConstants;

/**
 * 常量类
 *
 * <p>
 * 修改历史: <br>
 * 修改日期 修改人员 版本 修改内容<br>
 * -------------------------------------------------<br>
 * 2017年1月11日 上午10:23:24 dong.tian 1.0 初始化创建<br>
 * </p>
 *
 * @author dong.tian
 * @version 1.0
 * @since JDK1.7
 */
public class Constants {

	/**
	 * 检测的url
	 */
	public static final String CHECK_URL = "checkurl";

	/**
	 * 检测的url超时时间
	 */
	public static final String TIMEOUT = "timeout";

	/**
	 * 报警的人员: dong.tian:13909294845,xxx:13983477234
	 */
	public static final String USERS = "users";

	/**
	 * 持续报警次数
	 */
	public static final String COUNT = "count";

	/**
	 * 若持续报警count次,则splitTime内不报,unit为s
	 */
	public static final String SPLIT_TIME = "splitTime";

	public final static String ALARM_SERVICE_DEPEND_FILE_PATH = String.format("%s%s%s%s", GlobalConstants.USER_HOME,
			File.separator, "alarmservicechecker", File.separator);
	/**
	 * 动态保存报警情况的文件名
	 */
	public static final String isAlertFileName = String.format("%s%s", ALARM_SERVICE_DEPEND_FILE_PATH,
			"isAlertFileName");
	
	/**
	 * 上次报警的时间
	 */
	public static final String LAST_ALERTIME = "lastAlertTime";

	/**
	 * 已经持续报过警的次数
	 */
	public static final String ALERT_COUNT = "alertCount";

	/**
	 * 调用报警服务失败时的重试次数
	 */
	public static final String RETRY_TIMES = "retryTimes";
}
