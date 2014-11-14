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
		s1.append("        ��һ��ʹ��ʱ�������루��Ϊp���������������SHA1ɢ��ֵ�洢��Ϊ�������Ϊp2��\n\n");
		s1.append("        ���ر����ʱ�����м��ܡ���ȡ��Ƶ�ļ�ǰ1184���ֽڣ���AES�����㷨��ʹ�������һ��SHA1ɢ��ֵ����Ϊp1����Ϊ�ܳ׽��м��ܣ��õ�1200���ֽڵ����ģ�������д���ļ���β���ټ���20���ֽڵġ��Ѽ��ܡ���ʶ�����ļ�ͷ����p2��������־�û��������룩�����ļ�ͷ��ʣ��1164�ֽ���0Ϳ�ġ�����ļ�·���������ݿ⡣\n\n");
		s1.append("        ���ٴδ����ʱ��ʹ��p2�ȶԣ���ȷ���ǲ��Ǹ��û��������ļ������ѵ�����ļ����ܣ�ͬʱ���ļ�β���Ѽ��ܱ�ʶ�޸�Ϊ��δ���ܡ���ʶ��\n");
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
