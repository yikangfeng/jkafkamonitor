package com.elong.hotel.service.daemon.commons;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elong.hotel.service.daemon.alarm.check.util.HttpClientProxy;

/**
 * @author YiKangFeng.
 */
public class GlobalUtils {
	static private final Logger LOGGER = LoggerFactory.getLogger(GlobalUtils.class);

	static public void sendMsg(final String alertName, final String errorMsg) {
		if (alertName == null || alertName.isEmpty() || errorMsg == null || errorMsg.isEmpty()) {
			return;
		}

		AlertConfig alertConfig = SpringUtils.getBean(AlertConfig.class);

		StringBuilder alertTitle = new StringBuilder().append(errorMsg);
		StringBuilder url = new StringBuilder(200);
		url.append("http://");
		url.append(alertConfig.getRootUrl()).append(":").append(alertConfig.getPort());
		url.append(alertConfig.getServiceUri()).append("?");
		BasicNameValuePair alertTitlePair = new BasicNameValuePair("alertTitle", alertTitle.toString());
		BasicNameValuePair alertContentPair = new BasicNameValuePair("alertContent", errorMsg);

		CloseableHttpResponse response = null;
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(alertTitlePair);
		pairs.add(alertContentPair);
		pairs.add(new BasicNameValuePair("alertName", alertName));
		try {
			HttpGet httpGet = new HttpGet(
					url.toString() + EntityUtils.toString(new UrlEncodedFormEntity(pairs, "UTF-8")));
			response = HttpClientProxy.getInstance().execute(httpGet);
			LOGGER.warn("service-daemon send sms success " + errorMsg);
		} catch (final Throwable t) {
			LOGGER.error("service-daemon send sms failed for ", t);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (Exception e) {

				}
			}
		}

	}
}
