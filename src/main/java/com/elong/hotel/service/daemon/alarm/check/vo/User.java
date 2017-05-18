/**   
 * @(#)User.java	2017年1月10日	下午6:31:40	   
 *     
 * Copyrights (C) 2017艺龙旅行网保留所有权利
 */
package com.elong.hotel.service.daemon.alarm.check.vo;

/**
 * (类型功能说明描述)
 *
 * <p>
 * 修改历史:											<br>  
 * 修改日期    		修改人员   	版本	 		修改内容<br>  
 * -------------------------------------------------<br>  
 * 2017年1月10日 下午6:31:40   dong.tian     1.0    	初始化创建<br>
 * </p> 
 *
 * @author		dong.tian
 * @version		1.0  
 * @since		JDK1.7
 */
public class User {
	
	private String userName;
	private String mobile;
	
	
	public User(){}
	
	public User(String userName,String mobile){
		this.userName = userName;
		this.mobile = mobile;
	}
	/**   
	 * 得到userName的值   
	 *   
	 * @return userName的值
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * 设置userName的值
	 *   
	 * @param userName 被设置的值
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**   
	 * 得到mobile的值   
	 *   
	 * @return mobile的值
	 */
	public String getMobile() {
		return mobile;
	}
	/**
	 * 设置mobile的值
	 *   
	 * @param mobile 被设置的值
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
}
