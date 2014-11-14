package cn.edu.hit.software.videoencryptionpro;

//1184�����ֽ���
//1200���ܺ��ֽ���
//20�ֽ�Ϊ����shaֵ
//���ܺ��ֽ���Ϊ1184

//�Ƿ��Ѽ���160�ֽ�
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

	// ����Ϊ160���ֽ�
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
	 * ʹ��AES�����㷨���ֽ��������
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
			Cipher cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding"); // ����������
			cipher.init(Cipher.ENCRYPT_MODE, key);// ��ʼ��
			byte[] result = cipher.doFinal(byteContent);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ���Զ��ļ�����
	 * 
	 * @param file
	 * @param password
	 * @return
	 */
	public static EncodeResult Encode(File file, String key) {
		EncodeResult r = new EncodeResult();
		// ��¼�ļ��Ƿ����п���ռ����ڴ�ż��ܺ���ֽ�
		boolean fileState = false;
		// �ж�·���Ƿ���ȷ
		if (file == null || !file.exists() || !file.canWrite()) {
			r.result = false;
			r.tip = "�ļ������ڣ��ļ�����д";
			return r;
		}
		long fileLength = file.length();
		if (fileLength < 1024 * 5) {
			r.result = false;
			r.tip = "�ļ�����С��5KB";
			return r;
		}
		if (key.length() < 3) {
			r.result = false;
			r.tip = "�ܳ׳���̫��";
			return r;
		}
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "rw");
			// ͨ����ȡ�ļ����160���ֽ��ж��Ƿ��Ѽ���
			raf.seek(fileLength - 160);
			byte[] state = new byte[160];
			raf.read(state);
			if (byteEqual(state, hasEncoded)) {
				raf.close();
				r.result = true;
				r.tip = "�ļ��Ѽ���";
				return r;
			}
			// ���޸�
			// if (byteEqual(state, hasNotEncoded)) {
			// fileState = true;// �ļ������ܹ�
			// }
			// ��ȡ�ļ�ǰ1184�ֽ�
			// �洢ԭ��
			byte[] data = new byte[1184];
			raf.seek(0);
			raf.read(data);
			// ��������ܳ�
			byte[] key1 = SHA1.Encrypt(key.getBytes());
			// ����AES����
			byte[] encode = AESEncode(data, key1);
			// ��������ܳ׵�SHAֵ�����ڱ�ʶ�ļ�
			byte[] md = SHA1.Encrypt(key1);// ����40
			if (encode == null) {
				throw new Exception("�ļ������㷨�����ļ�δ�޸�");
			}
			// д�����������SHAֵ�����ڱ�ʶ������
			raf.seek(0);
			raf.write(md);
			raf.write(new byte[1184 - 20]);// ��0������࣬������Ϣ
			// д������ ���ĳ���Ϊ1200

			// ���޸�
			// if (fileState) {
			// raf.seek(fileLength - 160 - 1200);
			// } else {
			raf.seek(fileLength);
			// }
			raf.write(encode);
			raf.write(hasEncoded);
			r.result = true;
			r.tip = "�������";
			return r;
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof FileNotFoundException) {
				r.tip = "�ļ��ܾ�����";
			} else if (e instanceof IOException) {
				r.tip = "�ļ�����ʧ��";
			} else {
				r.tip = "�ļ������㷨�����ļ�δ�޸�";
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
	 * ���Խ��ļ�����
	 * 
	 * @param file
	 * @param key
	 * @return
	 */
	public static EncodeResult Decode(File file, String key) {
		EncodeResult r = new EncodeResult();
		// �ж�·���Ƿ���ȷ
		if (file == null || key == null || !file.exists() || !file.canWrite()
				|| key.length() < 3) {
			r.result = false;
			r.tip = "�ļ�����д��������̫��";
			return r;
		}
		long fileLength = file.length();
		if (fileLength < 1024 * 5) {
			r.result = false;
			r.tip = "�ļ�̫С";
			return r;
		}
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "rw");
			// ͨ����ȡ�ļ����160���ֽ��ж��Ƿ��Ѽ���
			byte[] state = new byte[160];
			raf.seek(fileLength - 160);
			raf.read(state);
			if (!byteEqual(state, hasEncoded)) {
				raf.close();
				r.result = true;
				r.tip = "�ļ�������� (�ѽ��ܣ����δ������)";
				return r;
			}
			// ��������ܳ�
			byte[] key1 = SHA1.Encrypt(key.getBytes());
			// ��������ܳ׵�SHA
			byte[] md = SHA1.Encrypt(key1);
			// ��ȡ�ļ�ͷ���ж��û��Ƿ���ȷ
			raf.seek(0);
			byte[] tempmd = new byte[20];
			raf.read(tempmd);
			// �ж��û��Ƿ���ȷ
			if (!byteEqual(tempmd, md)) {
				raf.close();
				r.result = false;
				r.tip = "�û�����ȷ��Ҳ����˵���벻��ȷ";
				return r;
			}
			// ��ȡ����
			byte[] data = new byte[1200];
			raf.seek(fileLength - 160 - 1200);
			raf.read(data);
			// ����AES����
			byte[] decode = AESDecode(data, key1);
			if (decode == null) {
				throw new Exception("�����㷨���ִ���δ�޸��ļ�");
			}
			// д���ʶ���ѽ��� ---- ���޸�

			// raf.write(hasNotEncoded);
			// ��ԭ��д��
			raf.seek(0);
			raf.write(decode);
			FileChannel fc = raf.getChannel();
			fc.truncate(fileLength - 160 - 1200);
			r.result = true;
			r.tip = "�������";
			return r;
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof FileNotFoundException) {
				r.tip = "�ļ����ʾܾ�";
			} else if (e instanceof IOException) {
				r.tip = "�ļ���д����";
			} else {
				r.tip = "�����㷨���ִ���δ�޸��ļ�";
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
			b.putString("value", "ָ�����ļ������ڻ��ܶ�д��");
			me.setData(b);
			me.sendToTarget();
			return;
		}
		long fileLength = n.file.length();
		if (fileLength < 5 * 1024) {
			b.putString("value", "�ļ�С��5KB���޷�������");
			me.setData(b);
			me.sendToTarget();
			return;
		}
		try {
			RandomAccessFile raf = new RandomAccessFile(n.file, "rw");
			// ͨ����ȡ�ļ����160���ֽ��ж��Ƿ��Ѽ���
			byte[] state = new byte[160];
			raf.seek(fileLength - 160);
			raf.read(state);
			if (byteEqual(state, hasEncoded)) {
				raf.close();
				b.putString("value", "�ļ�δ�����ܣ�\n�볢�Խ�����Ƶ���������ܣ�Ȼ���Ƴ���"
						+ "���ɽ���ȥ��������Ϣ�Ĳ�����");
				me.setData(b);
				me.sendToTarget();
				return;
			} else if (!byteEqual(state, hasNotEncoded)) {
				raf.close();
				b.putString("value", "�ļ���δ�����ܣ�����ȥ��������Ϣ��");
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
				b.putString("value", "û���㹻�Ŀռ����ڲ���");
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
						bb.putString("value", "��ʱ�ļ�ɾ�����ƶ�ʧ�ܣ����ֶ�����ļ�ϵͳ��");
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
				bb.putString("value", "��ʱ�ļ�ɾ�����ƶ�ʧ�ܣ����ֶ�����ļ�ϵͳ��");
				mee.setData(bb);
				mee.sendToTarget();
			}
		} catch (FileNotFoundException e) {
			Message mee = handler.obtainMessage();
			mee.arg1 = 1;
			Bundle bb = new Bundle();
			bb.putString("value", "ָ�����ļ������ڻ��ܶ�д��");
			mee.setData(bb);
			mee.sendToTarget();
			return;
		} catch (IOException e) {
			Message mee = handler.obtainMessage();
			mee.arg1 = 1;
			Bundle bb = new Bundle();
			bb.putString("value", "�ļ���д����");
			mee.setData(bb);
			mee.sendToTarget();
			return;
		}
	}

	/**
	 * �ж������ֽ����鳤���Ƿ����
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
			Cipher cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding"); // ����������
			cipher.init(Cipher.DECRYPT_MODE, key);// ��ʼ��
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
