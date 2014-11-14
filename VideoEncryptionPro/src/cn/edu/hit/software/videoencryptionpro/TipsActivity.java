package cn.edu.hit.software.videoencryptionpro;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

public class TipsActivity extends Activity {

	TextView t1_1, t1_2, t2_1, t2_2, t3_1, t3_2, t4_1, t4_2, t5_1, t5_2, t6_1,
			t6_2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_tips);
		t1_1 = (TextView) this.findViewById(R.id.tips_textView1_1);
		t1_2 = (TextView) this.findViewById(R.id.tips_textView1_2);
		t2_1 = (TextView) this.findViewById(R.id.tips_textView2_1);
		t2_2 = (TextView) this.findViewById(R.id.tips_textView2_2);
		t3_1 = (TextView) this.findViewById(R.id.tips_textView3_1);
		t3_2 = (TextView) this.findViewById(R.id.tips_textView3_2);
		t4_1 = (TextView) this.findViewById(R.id.tips_textView4_1);
		t4_2 = (TextView) this.findViewById(R.id.tips_textView4_2);
		t5_1 = (TextView) this.findViewById(R.id.tips_textView5_1);
		t5_2 = (TextView) this.findViewById(R.id.tips_textView5_2);
		t6_1 = (TextView) this.findViewById(R.id.tips_textView6_1);
		t6_2 = (TextView) this.findViewById(R.id.tips_textView6_2);

		t1_1.setText(R.string.tips1_1);
		t1_2.setText(R.string.tips1_2);
		t2_1.setText(R.string.tips2_1);
		t2_2.setText(R.string.tips2_2);
		t3_1.setText(R.string.tips3_1);
		t3_2.setText(R.string.tips3_2);
		t4_1.setText(R.string.tips4_1);
		t4_2.setText(R.string.tips4_2);
		t5_1.setText(R.string.tips5_1);
		t5_2.setText(R.string.tips5_2);
		t6_1.setText(R.string.tips6_1);
		t6_2.setText(R.string.tips6_2);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.tips, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_tips_back: {
			this.finish();
			break;
		}
		default:
			break;
		}
		return true;
	}

}
