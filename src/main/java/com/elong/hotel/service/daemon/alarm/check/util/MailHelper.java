/**   
 * @(#)MailHelper.java	2017年1月10日	下午7:21:16	   
 *     
 * Copyrights (C) 2017艺龙旅行网保留所有权利
 */
package com.elong.hotel.service.daemon.alarm.check.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.elong.hotel.service.daemon.alarm.check.vo.MailEntity;


/**
 * (类型功能说明描述)
 *
 * <p>
 * 修改历史:											<br>  
 * 修改日期    		修改人员   	版本	 		修改内容<br>  
 * -------------------------------------------------<br>  
 * 2017年1月10日 下午7:21:16   dong.tian     1.0    	初始化创建<br>
 * </p> 
 *
 * @author		dong.tian
 * @version		1.0  
 * @since		JDK1.7
 */
public class MailHelper {
	
	private static final String _CONTENT_TYPE = "text/html; charset=UTF-8";
	
	public static void SendMail(MailEntity mailEntity) {
		try {
			// LogHelper.Log("start to sendmail");
			List<Address> mailToList = new ArrayList<Address>();
			String[] toArray = mailEntity.Mail_To.split(";");
			Address[] mailToArray = new Address[toArray.length];
			for (String to : toArray) {
				Address address;
				try {
					address = new InternetAddress(to);
					mailToList.add(address);
				} catch (AddressException e) {
				}
			}
			mailToList.toArray(mailToArray);
			// Get system properties
			Properties properties = System.getProperties();
			// Setup mail server
			properties.setProperty("mail.smtp.host", mailEntity.Host);
			// Get the default Session object.
			Session session = Session.getDefaultInstance(properties);
			try {
				// Create a default MimeMessage object.
				MimeMessage message = new MimeMessage(session);
				// Set From: header field of the header.
				message.setFrom(new InternetAddress(mailEntity.Mail_From));
				// Set To: header field of the header.
				message.addRecipients(Message.RecipientType.TO, mailToArray);
				// Set Subject: header field
				message.setSubject(mailEntity.Mail_Subject);
				// Send the actual HTML message, as big as you like
				message.setContent(mailEntity.Mail_Body, _CONTENT_TYPE);
				// Send message
				Transport.send(message);
				// LogHelper.Log(mailEntity.Mail_Body);
			} catch (Exception e) {
				// LogHelper.Log(e.getMessage());
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
