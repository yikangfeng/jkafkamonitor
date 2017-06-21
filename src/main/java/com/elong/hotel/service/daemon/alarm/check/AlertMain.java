/**   
 * @(#)Main.java	2017年1月10日	下午5:51:40	   
 *     
 * Copyrights (C) 2017艺龙旅行网保留所有权利
 */
package com.elong.hotel.service.daemon.alarm.check;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.elong.hotel.service.daemon.alarm.check.common.Constants;
import com.elong.hotel.service.daemon.alarm.check.util.CommonUtils;
import com.elong.hotel.service.daemon.alarm.check.util.HttpClientProxy;
import com.elong.hotel.service.daemon.alarm.check.util.MailHelper;
import com.elong.hotel.service.daemon.alarm.check.vo.ArgsVO;
import com.elong.hotel.service.daemon.alarm.check.vo.MailEntity;
import com.elong.hotel.service.daemon.alarm.check.vo.User;
import com.elong.hotel.service.daemon.commons.NetKit;

/**
 * 报警服务检查类
 *
 * <p>
 * 修改历史: <br>
 * 修改日期 修改人员 版本 修改内容<br>
 * -------------------------------------------------<br>
 * 2017年1月10日 下午5:51:40 dong.tian 1.0 初始化创建<br>
 * </p>
 *
 * @author dong.tian
 * @version 1.0
 * @since JDK1.7
 */
public class AlertMain {
	static private final Logger LOGGER = LoggerFactory.getLogger(AlertMain.class);
	// 企业微信
	private static final String WORKWEICHARTADRESS = "http://ework.elong.com/qywechat/apply/text";

	// 短信
	// private static final String SERVER_ADDRESS =
	// "http://notice.mis.elong.com/sms/SendSMSForAlert";
	private static final String SERVER_ADDRESS = "http://smssender.vip.elong.com:8080/message/send";

	private static final String DEPARTMENT_ID = "alarm-006";

	private static final String BUSINESS_TYPE_ID = "6080";

	private static final String Content = "报警服务不可用，请及时处理！";

	public static void main(String[] args) throws IOException {
		if (args == null || args.length == 0) {
			throw new IllegalArgumentException();
		}
		checkAndCreateDependFile();
		ArgsVO vo = CommonUtils.convertVO(args[0]);
		int retCode = checkAlertUrl(vo.getCheckurl(), vo.getTimeout(), vo.getRetryTimes());
		LOGGER.info("hotel second monitor service check alarm service return code={},record time={}", retCode,
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		if (retCode != 200) {
			if (CommonUtils.isAlert(vo)) {
				sendAlertMessage(vo.getUsers());
			}
		}
	}

	static private void checkAndCreateDependFile() throws IOException {
		final String isAlertFileName = String.format("%s.properties", Constants.isAlertFileName);
		LOGGER.info("the alarm service check and create depend file path={}", isAlertFileName);
		if (isAlertFileName == null || isAlertFileName.isEmpty())
			return;
		final File file = new File(isAlertFileName);
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		if (!file.exists()) {
			file.createNewFile();
		}
	}

	private static void sendAlertMessage(List<User> users) {
		mailAlert(users);
		messageAlert(users);
		workAlert(users);
	}

	/**
	 * 
	 * 邮件报警
	 *
	 * @param users
	 */
	private static void mailAlert(List<User> users) {
		List<String> mailList = new ArrayList<String>();
		try {
			if (users != null && users.size() > 0) {
				for (User user : users) {
					mailList.add(user.getUserName() + "@corp.elong.com");
				}
			}
			if (mailList != null && mailList.size() > 0) {
				MailEntity mailEntity = new MailEntity();
				mailEntity.Host = "mta1.corp.ebj.elong.com";
				mailEntity.Mail_From = "Hotel_Alert@elong.com";
				for (String mail : mailList) {
					mailEntity.Mail_To += mail + ";";
				}
				mailEntity.Mail_Subject = "checkAlert";
				mailEntity.Mail_Body = buildMsgBody();
				MailHelper.SendMail(mailEntity);
			}
		} catch (Exception e) {
			// ignore
		}
	}

	static private String buildMsgBody() {
		return String.format("%s,%s(%s),%s", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
				NetKit.getLocalHostName(), NetKit.getLocalIp(), Content);
	}

	/**
	 * 
	 * 短信报警
	 *
	 * @param users
	 */
	private static void messageAlert(List<User> users) {
		if (users != null && users.size() > 0) {
			for (User user : users) {
				CloseableHttpResponse response = null;
				try {
					Map<String, String> map = new HashMap<String, String>();
					map.put("msg_content", buildMsgBody());
					map.put("mobile", user.getMobile());
					map.put("department_id", DEPARTMENT_ID);
					map.put("business_type_id", BUSINESS_TYPE_ID);
					HttpPost postMethod = new HttpPost(SERVER_ADDRESS);
					postMethod.setHeader("Content-Type", "application/json");
					postMethod.setEntity(new StringEntity(JSON.toJSONString(map), "utf-8"));
					response = HttpClientProxy.getInstance().execute(postMethod);
					if (response != null) {
						int retCode = response.getStatusLine().getStatusCode();
						if (retCode != 200) {
							postMethod.abort();
						}
					}
				} catch (Exception e) {
					// ignore
				} finally {
					if (response != null) {
						try {
							response.close();
						} catch (Exception e) {
							// ignore
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 * 企业微信报警
	 *
	 * @param users
	 */
	private static void workAlert(List<User> users) {
		CloseableHttpResponse response = null;
		try {
			if (users != null && users.size() > 0) {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("content", buildMsgBody()));
				for (User user : users) {
					params.add(new BasicNameValuePair("users[]", user.getUserName()));
				}
				HttpPost postMethod = new HttpPost(new URI(WORKWEICHARTADRESS));
				postMethod.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
				response = HttpClientProxy.getInstance().execute(postMethod);
				if (response != null) {
					int retCode = response.getStatusLine().getStatusCode();
					if (retCode != 200) {
						postMethod.abort();
					}
				}
			}
		} catch (Exception e) {
			// ignore
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
	}

	/**
	 * 
	 * check alert
	 *
	 * @return
	 */
	private static int checkAlertUrl(String checkUrl, String timeout, String retryTimes) {
		int retCode = 0;
		if (!CommonUtils.isEmpty(retryTimes)) {
			int retryTimes_ = Integer.parseInt(retryTimes);
			for (int i = 0; i < retryTimes_; i++) {
				retCode = doExecute(checkUrl, timeout);
				if (retCode == 200) {
					break;
				}
			}
		} else {
			retCode = doExecute(checkUrl, timeout);
		}
		return retCode;
	}

	public static int doExecute(String checkUrl, String timeout) {
		CloseableHttpResponse response = null;
		int retCode = 200;
		try {
			Builder custom = RequestConfig.copy(HttpClientProxy.getConfig());
			if (timeout != null) {
				int timeout_ = Integer.parseInt(timeout);
				custom.setSocketTimeout(timeout_);
				custom.setConnectTimeout(timeout_);
			}
			HttpGet httpGet = new HttpGet(checkUrl);
			httpGet.setConfig(custom.build());
			response = HttpClientProxy.getInstance().execute(httpGet);
			if (response != null) {
				retCode = response.getStatusLine().getStatusCode();
				if (retCode != 200) {
					httpGet.abort();
				}
			}
		} catch (Throwable t) {
			LOGGER.error("hotel second monitor service check alert service error,return code={},reasons:", retCode, t);
			// 异常
			retCode = -100;
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return retCode;
	}

}
