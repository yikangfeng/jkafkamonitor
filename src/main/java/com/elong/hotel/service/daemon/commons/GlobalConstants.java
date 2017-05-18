package com.elong.hotel.service.daemon.commons;

/**
 * @author kangfeng.yi.
 */
public interface GlobalConstants {
	static public final String ALARM_SERVICE_CONFIG_FILE_PATH = String.format("%s%s",
			GlobalConstants.ENV_RESOURCE_PATH, GlobalConstants.ALARM_SERVICE_CHECK_CONFIG_FILENAME);
	static public final String ENV_RESOURCE_PATH = "conf/custom/env/";
	static public final String NOT_ENV_RESOURCE_PATH = "conf/custom/notenv/";
	static public final String ALARM_SERVICE_CHECK_CONFIG_FILENAME = "checkalert";
	static public final String ALARM_SERVICE_CHECK_DEPEND_FILENAME = "isAlertFileName";
	static public final String USER_HOME = System.getProperty("user.home");
}
