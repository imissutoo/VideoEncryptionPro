package cn.edu.hit.software.videoencryptionpro;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Images;
import android.util.DisplayMetrics;

public class BitmapTools {

	public int width = 0;
	public int height = 0;
	public Activity c;
	public Bitmap setSmallPic = null;
	public Bitmap noneSmallPic = null;
	File f = null;
	String base = null;

	public BitmapTools(Activity c) {
		long l = System.currentTimeMillis();
		this.c = c;
		f = c.getDir("image", Activity.MODE_PRIVATE);
		base = f.getAbsolutePath();
		File[] temp = f.listFiles();
		for (int i = 0; i < temp.length; i++) {
			if (l - temp[i].lastModified() > 1000 * 60 * 60 * 24 * 7) {
				temp[i].delete();
			}
		}
	}

	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	/**
	 * ��bitmap��Ϊpng
	 * 
	 * @param s
	 * @param target
	 * @return
	 */
	public boolean saveAsPng(Bitmap s, File target) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(target);
		} catch (FileNotFoundException e) {
			return false;
		}
		if (s != null && fos != null
				&& s.compress(CompressFormat.PNG, 100, fos)) {
			try {
				fos.close();
				return true;
			} catch (IOException e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * ���ⲿ�ļ�����Ϊbitmap
	 * 
	 * @param path
	 * @return
	 */
	public Bitmap GetBitmapFromPng(String path) {
		return BitmapFactory.decodeFile(path);
	}

	/**
	 * ��ȡ�򴴽�Ĭ��ͼ��
	 * 
	 * @param path
	 * @return
	 */
	public Bitmap GetSetBitmap() {
		// ���û��Ĭ��ͼ��
		if (setSmallPic == null) {
			// ����Ĭ��ͼ��
			setSmallPic = BitmapFactory.decodeResource(c.getResources(),
					R.drawable.image);
			if (width == 0) {
				getDimens();
			}
			setSmallPic = ThumbnailUtils.extractThumbnail(setSmallPic, width,
					height);
			setSmallPic = toRoundCorner(setSmallPic, width / 20);
			// �洢Ĭ��ͼ��
			// this.saveAsPng(setSmallPic, new File(path));

		}
		return setSmallPic;
	}

	/**
	 * ��ȡ�򴴽�Ĭ��ͼ��
	 * 
	 * @param path
	 * @return
	 */
	public Bitmap GetNoneBitmap() {
		// ���û��Ĭ��ͼ��
		if (noneSmallPic == null) {
			// ����Ĭ��ͼ��
			noneSmallPic = BitmapFactory.decodeResource(c.getResources(),
					R.drawable.image);
			if (width == 0) {
				getDimens();
			}
			noneSmallPic = ThumbnailUtils.extractThumbnail(noneSmallPic, width,
					height);
			noneSmallPic = toRoundCorner(noneSmallPic, width / 20);
			// �洢Ĭ��ͼ��
			// this.saveAsPng(setSmallPic, new File(path));

		}
		return noneSmallPic;
	}

	/**
	 * �������ͼ�ߴ�
	 * 
	 * @param c
	 */
	public void getDimens() {
		DisplayMetrics dm = new DisplayMetrics();
		c.getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels / 4;
		if (width > 512) {
			width = 512;
		}
		height = (int) ((double) (width) / 1.4);
	}

	/**
	 * ��ȡ������ָ����ͼƬ
	 * 
	 * @param id
	 * @return
	 */
	public void GetSmallPic(Node m) {
		// ���ָ��Ĭ��ͼ��
		if (m.sha.equals("0")) {
			m.b = this.GetSetBitmap();
		} else {
			File targetf = new File(base + "/" + m.sha);
			// �Ѵ�������ͼ
			if (targetf != null && targetf.exists() && targetf.canRead()) {
				m.b = this.GetBitmapFromPng(base + "/" + m.sha);
			} else {
				// ���Դ�������ͼ
				Bitmap b = ThumbnailUtils.createVideoThumbnail(m.longName,
						Images.Thumbnails.MINI_KIND);
				// ����ʧ��
				if (b == null) {
					// ����Ĭ��ͼ��
					m.sha = "0";
					m.b = this.GetSetBitmap();
				} else {
					// ���Գɹ�
					if (width == 0) {
						this.getDimens();
					}
					b = ThumbnailUtils.extractThumbnail(b, width, height);
					b = BitmapTools.toRoundCorner(b, width / 20);
					// �洢����ͼ
					this.saveAsPng(b, new File(base + "/" + m.sha));
					m.b = BitmapTools.toRoundCorner(b, width / 20);
				}
			}
		}
	}

}
