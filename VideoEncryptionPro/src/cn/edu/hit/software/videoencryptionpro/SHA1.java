package cn.edu.hit.software.videoencryptionpro;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1 {

	public static byte[] Encrypt(byte[] password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			return md.digest(password);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	public static String Encrypt(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			try {
				return getHexString(md.digest(password.getBytes("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	/**
	 * 将字节数组转为十六进制字符串
	 * 
	 * @param b
	 * @return
	 */
	public static String getHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;

	}
}
