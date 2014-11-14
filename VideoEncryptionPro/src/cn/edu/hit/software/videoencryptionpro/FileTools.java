package cn.edu.hit.software.videoencryptionpro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.os.Environment;

public class FileTools {

	private static MyFilenameFilter myFilenameFilter = new MyFilenameFilter();
	private static MyFileFilter myFileFilter = new MyFileFilter();

	/**
	 * 获取文件系统中除已导入的文件的剩余视频文件
	 * 
	 * @param hasImported
	 * @return
	 */
	public static List<MyVideoFolder> getVideosFromFileSystem(
			List<Node> hasImported, Activity c) {
		int maxFloor = getDepthOfTheSearchForFile();
		List<MyVideoFolder> result = new ArrayList<MyVideoFolder>();
		HashSet<String> h = new HashSet<String>();
		Iterator<Node> ii = hasImported.iterator();
		while (ii.hasNext()) {
			h.add(ii.next().longName);
		}
		CheckFolder(result, h, Environment.getExternalStorageDirectory(), 0,
				maxFloor);
		return result;
	}

	public static boolean setDepthOfTheSearchForFile(int a) {
		if (a < 2 || a > 4) {
			return false;
		}
		File con = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/config");
		try {
			FileWriter fw = new FileWriter(con);
			fw.write(String.valueOf(a));
			fw.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static int getDepthOfTheSearchForFile() {
		File con = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/config");
		if (con.exists()) {
			try {
				FileReader fr = new FileReader(con);
				BufferedReader br = new BufferedReader(fr);
				String temp = br.readLine();
				int a = Integer.valueOf(temp);
				fr.close();
				br.close();
				if (a < 2 || a > 4) {
					return 2;
				}
				return a;
			} catch (FileNotFoundException e) {
				return 2;
			} catch (IOException e) {
				return 2;
			} catch (Exception e) {
				return 2;
			}
		} else {
			try {
				con.createNewFile();
				FileWriter fw = new FileWriter(con);
				fw.write("2".toString());
				fw.close();
				return 2;
			} catch (IOException e) {
				return 2;
			}
		}
	};

	private static void CheckFolder(List<MyVideoFolder> r,
			HashSet<String> hasImported, File file, int floor, int max) {
		if (file != null && !file.isHidden()) {
			// 列出所有视频文件
			File[] f = file.listFiles(myFilenameFilter);
			if (f != null && f.length > 0) {
				MyVideoFolder v = new MyVideoFolder();
				v.path = file.getAbsolutePath();
				for (int i = 0; i < f.length; i++) {
					// 视频未被导入
					if (!f[i].isHidden()
							&& f[i].length() >= 5 * 1024
							&& !Node.containPath(hasImported,
									f[i].getAbsolutePath())) {
						v.videoList.add(f[i].getAbsolutePath());
					}
				}
				if (v.videoList.size() > 0) {
					r.add(v);
				}
			}
			if (floor >= max) {
				return;
			}
			// 列出所有目录
			File[] g = file.listFiles(myFileFilter);
			if (g != null)
				for (int i = 0; i < g.length; i++) {
					if (!g[i].isHidden())
						CheckFolder(r, hasImported, g[i], floor + 1, max);
				}

		}
	}
}

class MyFilenameFilter implements FileFilter {

	private static HashSet<String> fileType = new HashSet<String>();
	static {
		fileType.add("mov");
		fileType.add("rmvb");
		fileType.add("mkv");
		fileType.add("rm");
		fileType.add("flv");
		fileType.add("mp4");
		fileType.add("avi");
		fileType.add("wmv");
		fileType.add("wmp");
		fileType.add("wm");
		fileType.add("asf");
		fileType.add("mpg");
		fileType.add("mpeg");
		fileType.add("mpe");
		fileType.add("m1v");
		fileType.add("m2v");
		fileType.add("mpv2");
		fileType.add("mp2v");
		fileType.add("ts");
		fileType.add("tp");
		fileType.add("tpr");
		fileType.add("trp");
		fileType.add("vob");
		fileType.add("ifo");
		fileType.add("ogm");
		fileType.add("ogv");
		fileType.add("m4v");
		fileType.add("m4p");
		fileType.add("m4b");
		fileType.add("3gp");
		fileType.add("3gpp");
		fileType.add("3g2");
		fileType.add("3gp2");
		fileType.add("ram");
		fileType.add("rpm");
		fileType.add("swf");
		fileType.add("qt");
		fileType.add("nsv");
		fileType.add("dpg");
		fileType.add("m2ts");
		fileType.add("m2t");
		fileType.add("mts");
		fileType.add("dvr-ms");
		fileType.add("k3g");
		fileType.add("skm");
		fileType.add("evo");
		fileType.add("nsr");
		fileType.add("amv");
		fileType.add("divx");
		fileType.add("webm");
	}

	@Override
	public boolean accept(File pathname) {
		if (pathname == null || !pathname.exists() || pathname.isHidden()
				|| pathname.isDirectory() || pathname.length() < 5 * 1024) {
			return false;
		}
		String b = pathname.getName();
		int a = b.lastIndexOf('.');
		if (a != -1) {
			b = b.substring(a + 1).toLowerCase();
			if (fileType.contains(b)) {
				return true;
			}
		}
		return false;
	}
}

class MyFileFilter implements FileFilter {

	@Override
	public boolean accept(File arg0) {
		if (arg0.isDirectory()) {
			return true;
		}
		return false;
	}

}

class MyVideoFolder {
	String path = null;
	List<String> videoList = new ArrayList<String>();
}
