/**   
 * @(#)ArgsVO.java	2017年1月11日	上午10:30:57	   
 *     
 * Copyrights (C) 2017艺龙旅行网保留所有权利
 */
package com.elong.hotel.service.daemon.alarm.check.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 参数实体类
 *
 * <p>
 * 修改历史:											<br>  
 * 修改日期    		修改人员   	版本	 		修改内容<br>  
 * -------------------------------------------------<br>  
 * 2017年1月11日 上午10:30:57   dong.tian     1.0    	初始化创建<br>
 * </p> 
 *
 * @author		dong.tian
 * @version		1.0  
 * @since		JDK1.7
 */
public class ArgsVO {
	
	private String checkurl;
	
	private String timeout;
	
	/**
	 * 持续报警次数
	 */
	private String count;
	
	/**
	 * 若持续报警count次,则20分钟内不报,unit为s
	 */
	private String splitTime;
	
	private List<User> users = new ArrayList<User>();
	
	/**
	 * 失败重试次数
	 */
	private String retryTimes;
	
	/**   
	 * 得到count的值   
	 *   
	 * @return count的值
	 */
	public String getCount() {
		return count;
	}

	/**
	 * 设置count的值
	 *   
	 * @param count 被设置的值
	 */
	public void setCount(String count) {
		this.count = count;
	}

	/**   
	 * 得到splitTime的值   
	 *   
	 * @return splitTime的值
	 */
	public String getSplitTime() {
		return splitTime;
	}

	/**
	 * 设置splitTime的值
	 *   
	 * @param splitTime 被设置的值
	 */
	public void setSplitTime(String splitTime) {
		this.splitTime = splitTime;
	}

	/**   
	 * 得到checkurl的值   
	 *   
	 * @return checkurl的值
	 */
	public String getCheckurl() {
		return checkurl;
	}

	/**
	 * 设置checkurl的值
	 *   
	 * @param checkurl 被设置的值
	 */
	public void setCheckurl(String checkurl) {
		this.checkurl = checkurl;
	}

	/**   
	 * 得到timeout的值   
	 *   
	 * @return timeout的值
	 */
	public String getTimeout() {
		return timeout;
	}

	/**
	 * 设置timeout的值
	 *   
	 * @param timeout 被设置的值
	 */
	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	/**   
	 * 得到users的值   
	 *   
	 * @return users的值
	 */
	public List<User> getUsers() {
		return users;
	}

	/**
	 * 设置users的值
	 *   
	 * @param users 被设置的值
	 */
	public void setUsers(List<User> users) {
		this.users = users;
	}
	

	/**   
	 * 得到retryTimes的值   
	 *   
	 * @return retryTimes的值
	 */
	public String getRetryTimes() {
		return retryTimes;
	}

	/**
	 * 设置retryTimes的值
	 *   
	 * @param retryTimes 被设置的值
	 */
	public void setRetryTimes(String retryTimes) {
		this.retryTimes = retryTimes;
	}

	/** 
	 * (方法说明描述) 
	 *
	 * @return 
	 *
	 * @see java.lang.Object#toString()    
	 */
	@Override
	public String toString() {
		return "checkurl:" + checkurl + " timeout:" + timeout + " users:" + users.toString();
	}
}
