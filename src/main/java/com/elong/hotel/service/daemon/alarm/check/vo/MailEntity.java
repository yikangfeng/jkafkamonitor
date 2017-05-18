/**   
 * @(#)MailEntity.java	2017年1月10日	下午7:19:32	   
 *     
 * Copyrights (C) 2017艺龙旅行网保留所有权利
 */
package com.elong.hotel.service.daemon.alarm.check.vo;

/**
 * 邮件实体
 *
 * <p>
 * 修改历史:											<br>  
 * 修改日期    		修改人员   	版本	 		修改内容<br>  
 * -------------------------------------------------<br>  
 * 2017年1月10日 下午7:19:32   dong.tian     1.0    	初始化创建<br>
 * </p> 
 *
 * @author		dong.tian
 * @version		1.0  
 * @since		JDK1.7
 */
public class MailEntity {
	
	public String Host;
	public String Mail_To;
	public String Mail_From;
	public String Mail_Subject;
	public String Mail_Body;
	
	/**
	 * 
	 */
	public MailEntity(){
		Host = "";
		Mail_To = "";
		Mail_From = "";
		Mail_Subject = "";
		Mail_Body = "";
	}
}
