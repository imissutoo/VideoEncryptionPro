package cn.edu.hit.software.videoencryptionpro;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

public class PrincipleActivity extends Activity {

	TextView textView1 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_principle);
		textView1 = (TextView) this.findViewById(R.id.principle_textView);
		StringBuffer s1 = new StringBuffer();
		s1.append("        第一次使用时输入密码（记为p）。将密码的两次SHA1散列值存储作为存根，记为p2。\n\n");
		s1.append("        当关闭软件时，进行加密。读取视频文件前1184个字节，按AES加密算法，使用密码的一次SHA1散列值（记为p1）作为密匙进行加密，得到1200个字节的密文，将密文写入文件结尾。再加入20个字节的“已加密”标识。在文件头加入p2，用来标志用户（即密码），将文件头的剩余1164字节用0涂改。最后将文件路径存入数据库。\n\n");
		s1.append("        当再次打开软件时，使用p2比对，以确定是不是该用户创建的文件。将已导入的文件解密，同时将文件尾的已加密标识修改为“未加密”标识。\n");
		textView1.setText(s1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.principle, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_principle_back: {
			this.finish();
			break;
		}
		default:
			break;
		}
		return true;
	}

}
