/**   
 * @(#)CommonUtils.java	2017年1月11日	上午10:33:44	   
 *     
 * Copyrights (C) 2017艺龙旅行网保留所有权利
 */
package com.elong.hotel.service.daemon.alarm.check.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import com.elong.hotel.service.daemon.alarm.check.common.Constants;
import com.elong.hotel.service.daemon.alarm.check.vo.ArgsVO;
import com.elong.hotel.service.daemon.alarm.check.vo.User;

/**
 * (类型功能说明描述)
 *
 * <p>
 * 修改历史:											<br>  
 * 修改日期    		修改人员   	版本	 		修改内容<br>  
 * -------------------------------------------------<br>  
 * 2017年1月11日 上午10:33:44   dong.tian     1.0    	初始化创建<br>
 * </p> 
 *
 * @author		dong.tian
 * @version		1.0  
 * @since		JDK1.7
 */
public class CommonUtils {
	
	private static Properties properties = new Properties();
	
	/**
	 * 
	 * 参数转实体
	 *
	 * @param fileName
	 * @return
	 */
	public static ArgsVO convertVO(String fileName){
		ArgsVO vo = new ArgsVO();
		try {
			ResourceBundle resource = ResourceBundle.getBundle(fileName);
			vo.setCheckurl(resource.getString(Constants.CHECK_URL));
			vo.setTimeout(resource.getString(Constants.TIMEOUT));
			vo.setCount(resource.getString(Constants.COUNT));
			vo.setSplitTime(resource.getString(Constants.SPLIT_TIME));
			vo.setRetryTimes(resource.getString(Constants.RETRY_TIMES));
			String userStr = resource.getString(Constants.USERS);
			if(userStr != null){
				List<User> usersList = new ArrayList<User>();
				String[] users = userStr.split(",");
				for(String users_ : users){
					String[] name_phone = users_.split(":");
					usersList.add(new User(name_phone[0],name_phone[1]));
				}
				vo.setUsers(usersList);
			}
		} catch (Exception e) {
			// ignore
		}
		return vo;
	}
	
	/**
	 * 
	 * 根据报警策略判断是否需要进行报警
	 *
	 * @return
	 */
	public static boolean isAlert(ArgsVO vo){
		String count = vo.getCount();
		String splitTime = vo.getSplitTime();
		if(count == null || splitTime == null){
			return true;
		}
		File file = null;
		FileOutputStream fos = null;
		FileInputStream fis = null;
		try {
			int count_ = Integer.parseInt(count);
			int splitTime_ = Integer.parseInt(splitTime);
			//上次报警的时间
			String lastAletTime = null;
			//已经持续报警的次数
			String alertCount = null;
			try {
			    file = new File(Constants.isAlertFileName + ".properties");
				if(!file.exists()){
					file.createNewFile();
				}
			    fis = new FileInputStream(file);
				properties.load(fis);
				lastAletTime = properties.getProperty(Constants.LAST_ALERTIME);
				alertCount = properties.getProperty(Constants.ALERT_COUNT);
			} catch (Exception e) {
			}
			try {
				 fos = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				// ignore
			}
			if(lastAletTime == null || alertCount == null){
				properties.setProperty(Constants.LAST_ALERTIME, String.valueOf(System.currentTimeMillis()));
				properties.setProperty(Constants.ALERT_COUNT, String.valueOf(1));
			}else{
				long lastAletTime_ = Long.parseLong(lastAletTime);
				int alertCount_ = Integer.parseInt(alertCount);
				//如果已经报警的次数达到阈值
				if(alertCount_ >= count_){
					long currentTime = System.currentTimeMillis();
					if(currentTime - lastAletTime_ >= splitTime_ * 1000l){
						properties.setProperty(Constants.LAST_ALERTIME, String.valueOf(System.currentTimeMillis()));
						properties.setProperty(Constants.ALERT_COUNT, String.valueOf(1));
					}else{
						return false;
					}
				}else{
					long currentTime = System.currentTimeMillis();
					// 如果当前报警时间距离上次报警时间在2分钟内，则认为是持续报警状态
					if(currentTime - lastAletTime_ < 2*60*1000){
						properties.setProperty(Constants.ALERT_COUNT, String.valueOf(alertCount_ + 1));
					}else{
						properties.setProperty(Constants.ALERT_COUNT, String.valueOf(1));
					}
					properties.setProperty(Constants.LAST_ALERTIME, String.valueOf(System.currentTimeMillis()));
				}
			}
		} finally{
			try {
				properties.store(fos, "update alert status");
			} catch (IOException e) {
				// ignore
			}
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	
	
	public static boolean isEmpty(String str){
		return str == null || str.length() == 0;
	}
}
