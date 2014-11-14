package cn.edu.hit.software.videoencryptionpro;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBTools {

	private Context c;

	public SQLiteDatabase sQLiteDatabase = null;

	private static int count = 0;

	public DBTools(Context c) {
		this.c = c;
		if (count == 0) {
			init();
		}
		count++;
	}

	public String getPassword() {
		String result = null;
		SQLiteDatabase db = this.getConnection();
		Cursor cu = db.rawQuery("SELECT * FROM password", null);
		if (cu.moveToNext()) {
			result = cu.getString(0);
		}
		cu.close();
		db.close();
		return result;
	}

	public boolean setPassword(String p) {
		boolean result = false;
		SQLiteDatabase db = this.getConnection();
		db.execSQL("DELETE FROM password");
		ContentValues cv = new ContentValues();
		cv.put("value", p);
		long a = db.insert("password", null, cv);
		if (a != -1) {
			result = true;
		}
		return result;
	}

	public List<Node> getFileList() {
		List<Node> result = new ArrayList<Node>();
		SQLiteDatabase db = this.getConnection();
		Cursor cu = db.rawQuery("SELECT * FROM fileList", null);
		while (cu.moveToNext()) {
			Node f = new Node();
			f.file = new File(cu.getString(cu.getColumnIndex("path")));
			f.sha = cu.getString(cu.getColumnIndex("image"));
			result.add(f);
		}
		cu.close();
		return result;
	}

	public boolean setFileList(List<Node> d) {
		try {
			SQLiteDatabase db = this.getConnection();
			db.execSQL("DELETE FROM fileList");
			for (int i = 0; i < d.size(); i++) {
				db.execSQL("INSERT INTO fileList values (?,?)", new Object[] {
						d.get(i).longName, d.get(i).sha });
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isTableExist(String tableName) {
		boolean result = false;
		if (tableName == null) {
			return false;
		}
		SQLiteDatabase db = this.getConnection();
		Cursor cu = null;
		String sql = "select count(*) from sqlite_master where type='table' and name='"
				+ tableName.trim() + "'";
		cu = db.rawQuery(sql, null);
		if (cu.moveToNext()) {
			if (cu.getInt(0) > 0) {
				return true;
			}
		}
		return result;
	}

	public SQLiteDatabase getConnection() {
		if (sQLiteDatabase == null || !sQLiteDatabase.isOpen()
				|| sQLiteDatabase.isReadOnly()) {
			sQLiteDatabase = c.openOrCreateDatabase("VideoEncryption",
					Context.MODE_PRIVATE, null);
		}
		return sQLiteDatabase;
	}

	private void init() {
		SQLiteDatabase db = this.getConnection();
		if (!isTableExist("password")) {
			db.execSQL("CREATE TABLE password (value NTEXT)");
		}
		if (!isTableExist("fileList")) {
			db.execSQL("CREATE TABLE fileList (path NTEXT PRIMARY KEY, image NTEXT)");
		}
	}

	public void close() {
		if (sQLiteDatabase != null && sQLiteDatabase.isOpen()) {
			sQLiteDatabase.close();
		}
	}
}
