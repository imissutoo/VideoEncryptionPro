package cn.edu.hit.software.videoencryptionpro;

//1184加密字节数
//1200加密后字节数
//20字节为密码sha值
//解密后字节数为1184

//是否已加密160字节
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;

public class CodeHelper {

	// 长度为160个字节
	private static final byte[] hasEncoded = AESEncode(new byte[159],
			"hasEncoded".getBytes());
	private static final byte[] hasNotEncoded = AESEncode(new byte[159],
			"hasNotEncoded".getBytes());

	public static final int OK = 0;
	public static final int CANCLE = -1;
	public static final int CODE1 = 1;
	public static final int CODE2 = 2;
	public static final int CODE3 = 3;
	public static final int CODE4 = 4;
	public static final int CODE5 = 5;
	public static final int CODE6 = 6;

	/**
	 * 使用AES加密算法对字节数组加密
	 * 
	 * @param byteContent
	 * @param password
	 * @return
	 */
	public static byte[] AESEncode(byte[] byteContent, byte[] password) {
		KeyGenerator kgen;
		try {
			kgen = KeyGenerator.getInstance("AES");
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
			sr.setSeed(password);
			kgen.init(128, sr);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding"); // 创建密码器
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(byteContent);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 尝试对文件加密
	 * 
	 * @param file
	 * @param password
	 * @return
	 */
	public static EncodeResult Encode(File file, String key) {
		EncodeResult r = new EncodeResult();
		// 记录文件是否已有空余空间用于存放加密后的字节
		boolean fileState = false;
		// 判断路径是否正确
		if (file == null || !file.exists() || !file.canWrite()) {
			r.result = false;
			r.tip = "文件不存在，文件不可写";
			return r;
		}
		long fileLength = file.length();
		if (fileLength < 1024 * 5) {
			r.result = false;
			r.tip = "文件不能小于5KB";
			return r;
		}
		if (key.length() < 3) {
			r.result = false;
			r.tip = "密匙长度太短";
			return r;
		}
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "rw");
			// 通过读取文件最后160个字节判断是否已加密
			raf.seek(fileLength - 160);
			byte[] state = new byte[160];
			raf.read(state);
			if (byteEqual(state, hasEncoded)) {
				raf.close();
				r.result = true;
				r.tip = "文件已加密";
				return r;
			}
			// 已修改
			// if (byteEqual(state, hasNotEncoded)) {
			// fileState = true;// 文件曾加密过
			// }
			// 读取文件前1184字节
			// 存储原文
			byte[] data = new byte[1184];
			raf.seek(0);
			raf.read(data);
			// 计算加密密匙
			byte[] key1 = SHA1.Encrypt(key.getBytes());
			// 进行AES加密
			byte[] encode = AESEncode(data, key1);
			// 计算加密密匙的SHA值，用于标识文件
			byte[] md = SHA1.Encrypt(key1);// 长度40
			if (encode == null) {
				throw new Exception("文件加密算法出错，文件未修改");
			}
			// 写入密码的两次SHA值，用于标识创建者
			raf.seek(0);
			raf.write(md);
			raf.write(new byte[1184 - 20]);// 用0补齐空余，擦除信息
			// 写入密文 密文长度为1200

			// 已修改
			// if (fileState) {
			// raf.seek(fileLength - 160 - 1200);
			// } else {
			raf.seek(fileLength);
			// }
			raf.write(encode);
			raf.write(hasEncoded);
			r.result = true;
			r.tip = "加密完成";
			return r;
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof FileNotFoundException) {
				r.tip = "文件拒绝访问";
			} else if (e instanceof IOException) {
				r.tip = "文件操作失败";
			} else {
				r.tip = "文件加密算法出错，文件未修改";
			}
			r.result = false;
			return r;
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 尝试将文件解密
	 * 
	 * @param file
	 * @param key
	 * @return
	 */
	public static EncodeResult Decode(File file, String key) {
		EncodeResult r = new EncodeResult();
		// 判断路径是否正确
		if (file == null || key == null || !file.exists() || !file.canWrite()
				|| key.length() < 3) {
			r.result = false;
			r.tip = "文件不可写，或密码太短";
			return r;
		}
		long fileLength = file.length();
		if (fileLength < 1024 * 5) {
			r.result = false;
			r.tip = "文件太小";
			return r;
		}
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "rw");
			// 通过读取文件最后160个字节判断是否已加密
			byte[] state = new byte[160];
			raf.seek(fileLength - 160);
			raf.read(state);
			if (!byteEqual(state, hasEncoded)) {
				raf.close();
				r.result = true;
				r.tip = "文件无需解密 (已解密，或从未被加密)";
				return r;
			}
			// 计算解密密匙
			byte[] key1 = SHA1.Encrypt(key.getBytes());
			// 计算解密密匙的SHA
			byte[] md = SHA1.Encrypt(key1);
			// 读取文件头，判断用户是否正确
			raf.seek(0);
			byte[] tempmd = new byte[20];
			raf.read(tempmd);
			// 判断用户是否正确
			if (!byteEqual(tempmd, md)) {
				raf.close();
				r.result = false;
				r.tip = "用户不正确，也可以说密码不正确";
				return r;
			}
			// 读取密文
			byte[] data = new byte[1200];
			raf.seek(fileLength - 160 - 1200);
			raf.read(data);
			// 进行AES解密
			byte[] decode = AESDecode(data, key1);
			if (decode == null) {
				throw new Exception("解密算法出现错误，未修改文件");
			}
			// 写入标识，已解密 ---- 已修改

			// raf.write(hasNotEncoded);
			// 将原文写回
			raf.seek(0);
			raf.write(decode);
			FileChannel fc = raf.getChannel();
			fc.truncate(fileLength - 160 - 1200);
			r.result = true;
			r.tip = "解密完成";
			return r;
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof FileNotFoundException) {
				r.tip = "文件访问拒绝";
			} else if (e instanceof IOException) {
				r.tip = "文件读写出错";
			} else {
				r.tip = "解密算法出现错误，未修改文件";
			}
			r.result = false;
			return r;
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void Clear(Node n, Handler handler) {
		Message me = handler.obtainMessage();
		me.arg1 = 1;
		Bundle b = new Bundle();
		if (!n.file.exists() || !n.file.canWrite()) {
			b.putString("value", "指定的文件不存在或不能读写！");
			me.setData(b);
			me.sendToTarget();
			return;
		}
		long fileLength = n.file.length();
		if (fileLength < 5 * 1024) {
			b.putString("value", "文件小于5KB，无法操作！");
			me.setData(b);
			me.sendToTarget();
			return;
		}
		try {
			RandomAccessFile raf = new RandomAccessFile(n.file, "rw");
			// 通过读取文件最后160个字节判断是否已加密
			byte[] state = new byte[160];
			raf.seek(fileLength - 160);
			raf.read(state);
			if (byteEqual(state, hasEncoded)) {
				raf.close();
				b.putString("value", "文件未被解密！\n请尝试将该视频导入来解密，然后移除，"
						+ "即可进行去除多余信息的操作。");
				me.setData(b);
				me.sendToTarget();
				return;
			} else if (!byteEqual(state, hasNotEncoded)) {
				raf.close();
				b.putString("value", "文件从未被加密，无需去除多余信息。");
				me.setData(b);
				me.sendToTarget();
				return;
			}
			String sdcard = Environment.getExternalStorageDirectory().getPath();
			StatFs statFs = new StatFs(sdcard);
			long blockSize = statFs.getBlockSize();
			long blocks = statFs.getAvailableBlocks();
			long availableSpare = blocks * blockSize;
			if (availableSpare < fileLength) {
				raf.close();
				b.putString("value", "没有足够的空间用于操作");
				me.setData(b);
				me.sendToTarget();
				return;
			}
			raf.seek(0);
			File f2 = new File(n.file.getAbsolutePath()
					+ System.currentTimeMillis());
			RandomAccessFile raf2 = new RandomAccessFile(f2, "rw");
			long up = fileLength - 1200 - 160;
			long times = up / (1024 * 1024);
			byte[] temp = new byte[1024 * 1024];
			me.arg1 = 0;
			b.putLong("times", times + 1);
			b.putLong("now", 0);
			me.setData(b);
			me.sendToTarget();
			for (int i = 0; i < times; i++) {
				if (!n.isCheckable) {
					raf.close();
					raf2.close();
					if (f2.delete()) {
						Message mee = handler.obtainMessage();
						mee.arg1 = 1;
						Bundle bb = new Bundle();
						bb.putString("value", "p");
						mee.setData(bb);
						mee.sendToTarget();
					} else {
						Message mee = handler.obtainMessage();
						mee.arg1 = 1;
						Bundle bb = new Bundle();
						bb.putString("value", "临时文件删除或移动失败，请手动检查文件系统！");
						mee.setData(bb);
						mee.sendToTarget();
					}
					return;
				}
				raf.read(temp);
				raf2.write(temp);
				Message m = handler.obtainMessage();
				m.arg1 = 0;
				Bundle bl = new Bundle();
				bl.putLong("times", times + 1);
				bl.putLong("now", i + 1);
				m.setData(bl);
				m.sendToTarget();
			}
			raf.read(temp, 0, (int) (up - times * 1024 * 1024));
			raf2.write(temp, 0, (int) (up - times * 1024 * 1024));
			Message m = handler.obtainMessage();
			m.arg1 = 0;
			Bundle bl = new Bundle();
			bl.putLong("times", times + 1);
			bl.putLong("now", times + 1);
			m.setData(bl);
			m.sendToTarget();
			raf.close();
			raf2.close();
			if (n.file.delete() && f2.renameTo(n.file)) {
				Message mee = handler.obtainMessage();
				mee.arg1 = 1;
				Bundle bb = new Bundle();
				bb.putString("value", "o");
				mee.setData(bb);
				mee.sendToTarget();
			} else {
				Message mee = handler.obtainMessage();
				mee.arg1 = 1;
				Bundle bb = new Bundle();
				bb.putString("value", "临时文件删除或移动失败，请手动检查文件系统！");
				mee.setData(bb);
				mee.sendToTarget();
			}
		} catch (FileNotFoundException e) {
			Message mee = handler.obtainMessage();
			mee.arg1 = 1;
			Bundle bb = new Bundle();
			bb.putString("value", "指定的文件不存在或不能读写！");
			mee.setData(bb);
			mee.sendToTarget();
			return;
		} catch (IOException e) {
			Message mee = handler.obtainMessage();
			mee.arg1 = 1;
			Bundle bb = new Bundle();
			bb.putString("value", "文件读写出错！");
			mee.setData(bb);
			mee.sendToTarget();
			return;
		}
	}

	/**
	 * 判断两个字节数组长度是否相等
	 * 
	 * @param b1
	 * @param b2
	 * @return
	 */
	public static boolean byteEqual(byte[] b1, byte[] b2) {
		if (b1 == null || b2 == null || b1.length != b2.length) {
			return false;
		}
		for (int i = 0; i < b1.length; i++) {
			if (b1[i] != b2[i]) {
				return false;
			}
		}
		return true;
	}

	public static byte[] AESDecode(byte[] byteContent, byte[] password) {
		KeyGenerator kgen;
		try {
			kgen = KeyGenerator.getInstance("AES");
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
			sr.setSeed(password);
			kgen.init(128, sr);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding"); // 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(byteContent);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

}

class EncodeResult {
	public boolean result;
	public String tip;
}
