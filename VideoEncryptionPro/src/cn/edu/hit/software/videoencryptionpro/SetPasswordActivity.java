package cn.edu.hit.software.videoencryptionpro;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SetPasswordActivity extends Activity implements TextWatcher,
		OnClickListener {

	private TextView warning1, warning2;
	private EditText et1, et2, et3;
	private Button button, cancelButton;
	private String password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		password = this.getIntent().getStringExtra("password");
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_set_password);
		warning1 = (TextView) this
				.findViewById(R.id.set_password_textView_warning);
		warning2 = (TextView) this
				.findViewById(R.id.set_password_textView_warning2);
		et1 = (EditText) this.findViewById(R.id.set_password_editText1);
		et2 = (EditText) this.findViewById(R.id.set_password_editText2);
		et3 = (EditText) this.findViewById(R.id.set_password_editText3);
		button = (Button) this.findViewById(R.id.set_password_Button);
		cancelButton = (Button) this.findViewById(R.id.set_password_cancel);

		et1.addTextChangedListener(this);
		et2.addTextChangedListener(this);

		et2.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				if (arg1 == KeyEvent.KEYCODE_ENTER
						&& arg2.getAction() == KeyEvent.ACTION_DOWN) {
					if (button.isEnabled()) {
						doIt();
						return true;
					}
				}
				return false;
			}

		});

		button.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
	}

	private void doIt() {
		if (et3.getText().toString().equals(password)) {
			DBTools db = new DBTools(this);
			String pa = et1.getText().toString();
			db.setPassword(SHA1.Encrypt(SHA1.Encrypt(pa)));
			db.close();
			Intent t = new Intent();
			t.putExtra("password", pa);
			this.setResult(CodeHelper.OK, t);
			this.finish();
		} else {
			Builder b = new Builder(this);
			b.setTitle("失败");
			b.setMessage("原密码错误 ！");
			b.setPositiveButton(R.string.OK, null);
			b.show();
		}
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		boolean flag1 = false, flag2 = false;
		String t1 = et1.getText().toString();
		String t2 = et2.getText().toString();
		// 第一个文本框
		if (t1.length() < 3 && t1.length() > 0) {
			warning1.setVisibility(View.VISIBLE);
		} else {
			warning1.setVisibility(View.GONE);
		}
		if (t1.length() >= 3) {
			flag1 = true;
		}
		// 第二个文本框
		if (t2.length() == 0) {
			warning2.setVisibility(View.GONE);
		} else {
			if (t2.equals(t1)) {
				warning2.setVisibility(View.GONE);
				flag2 = true;
			} else {
				warning2.setVisibility(View.VISIBLE);
			}
		}

		if (flag1 && flag2) {
			button.setEnabled(true);
		} else {
			button.setEnabled(false);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.set_password_Button: {
			doIt();
			break;
		}
		case R.id.set_password_cancel: {
			this.setResult(CodeHelper.CANCLE);
			this.finish();
			break;
		}
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			this.setResult(CodeHelper.CANCLE);
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.set_password, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_set_password_cancel: {
			this.setResult(CodeHelper.CANCLE);
			this.finish();
			break;
		}
		default:
			break;
		}
		return true;
	}

}
