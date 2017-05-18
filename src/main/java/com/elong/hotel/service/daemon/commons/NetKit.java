package com.elong.hotel.service.daemon.commons;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kangfeng.yi
 */
public class NetKit {

	static public String getLocalHostName() {
		String hostName = "";
		try {
			InetAddress ia = InetAddress.getLocalHost();
			hostName = ia.getHostName();
		} catch (final Throwable ignored) {
		}
		return hostName;
	}

	public static boolean isInRange(String ip, String cidr) {
		String[] ips = ip.split("\\.");
		int ipAddr = (Integer.parseInt(ips[0]) << 24) | (Integer.parseInt(ips[1]) << 16)
				| (Integer.parseInt(ips[2]) << 8) | Integer.parseInt(ips[3]);
		int type = Integer.parseInt(cidr.replaceAll(".*/", ""));
		int mask = 0xFFFFFFFF << (32 - type);
		String cidrIp = cidr.replaceAll("/.*", "");
		String[] cidrIps = cidrIp.split("\\.");
		int cidrIpAddr = (Integer.parseInt(cidrIps[0]) << 24) | (Integer.parseInt(cidrIps[1]) << 16)
				| (Integer.parseInt(cidrIps[2]) << 8) | Integer.parseInt(cidrIps[3]);

		return (ipAddr & mask) == (cidrIpAddr & mask);
	}

	private static int matchedIndex(String ip, String[] prefix) {
		for (int i = 0; i < prefix.length; i++) {
			String p = prefix[i];
			if ("*".equals(p)) { // *假定为匹配外网IP
				if (ip.startsWith("127.") || ip.startsWith("10.") || ip.startsWith("172.") || ip.startsWith("192.")) {
					continue;
				}
				return i;
			} else {
				if (ip.startsWith(p)) {
					return i;
				}
			}
		}

		return -1;
	}

	public static String getLocalIp() {
		return getLocalIp("*>10>172>192>127");
	}

	public static String getLocalIp(String ipPreference) {
		if (ipPreference == null) {
			ipPreference = "*>10>172>192>127";
		}
		String[] prefix = ipPreference.split("[> ]+");
		try {
			Pattern pattern = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+");
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			String matchedIp = null;
			int matchedIdx = -1;
			while (interfaces.hasMoreElements()) {
				NetworkInterface ni = interfaces.nextElement();
				Enumeration<InetAddress> en = ni.getInetAddresses();
				while (en.hasMoreElements()) {
					InetAddress addr = en.nextElement();
					String ip = addr.getHostAddress();
					Matcher matcher = pattern.matcher(ip);
					if (matcher.matches()) {
						int idx = matchedIndex(ip, prefix);
						if (idx == -1)
							continue;
						if (matchedIdx == -1) {
							matchedIdx = idx;
							matchedIp = ip;
						} else {
							if (matchedIdx > idx) {
								matchedIdx = idx;
								matchedIp = ip;
							}
						}
					}
				}
			}
			if (matchedIp != null)
				return matchedIp;
			return "127.0.0.1";
		} catch (Throwable e) {
			return "127.0.0.1";
		}
	}
}
