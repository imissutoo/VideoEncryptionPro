package cn.edu.hit.software.videoencryptionpro;

import android.app.Activity;
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

public class MainActivity extends Activity implements OnClickListener,
		TextWatcher {

	DBTools db = null;
	boolean firstTime = false;
	EditText editTextFirst = null;
	EditText editTextFirst2 = null;
	EditText mainEditText = null;
	Button loginButton = null;
	Button exitButton = null;
	Button startButton = null;
	TextView warning1 = null;
	TextView warning2 = null;
	TextView warning3 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		db = new DBTools(this);
		String a = db.getPassword();
		db.close();
		if (a == null) {
			firstTime = true;
			setContentView(R.layout.activity_first);
			this.InitMainFirst();
		} else {
			setContentView(R.layout.activity_login);
			this.InitMain();
		}
	}

	private void ClickStartButton() {
		db = new DBTools(this);
		String password = editTextFirst.getText().toString();
		db.setPassword(SHA1.Encrypt(SHA1.Encrypt(password)));
		db.close();
		this.showNextActivity(password);
	}

	private boolean ClickLoginButton() {
		db = new DBTools(this);
		String temppassword = ((EditText) this.findViewById(R.id.main_editText))
				.getText().toString();
		String newp = SHA1.Encrypt(SHA1.Encrypt(temppassword));
		String old = db.getPassword();
		db.close();
		if (old.equals(newp)) {
			this.showNextActivity(temppassword);
			return true;
		} else {
			warning3.setVisibility(View.VISIBLE);
			return false;
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.exit_Button: {
			this.finish();
			break;
		}
		case R.id.login_Button: {
			ClickLoginButton();
			break;
		}
		case R.id.start_Button: {
			ClickStartButton();
			break;
		}
		default:
			break;
		}
	}

	private void InitMain() {
		this.setTitle(R.string.login);
		exitButton = (Button) findViewById(R.id.exit_Button);
		loginButton = (Button) findViewById(R.id.login_Button);
		mainEditText = (EditText) this.findViewById(R.id.main_editText);
		warning3 = (TextView) this.findViewById(R.id.textView_warning3);
		mainEditText.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					ClickLoginButton();
					return true;
				}
				return false;
			}

		});
		mainEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				warning3.setVisibility(View.GONE);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}

		});
		exitButton.setOnClickListener(this);
		loginButton.setOnClickListener(this);
	}

	private void InitMainFirst() {
		this.setTitle(R.string.setPassword);
		startButton = (Button) findViewById(R.id.start_Button);
		warning1 = (TextView) findViewById(R.id.textView_warning);
		warning2 = (TextView) findViewById(R.id.textView_warning2);
		editTextFirst = (EditText) this.findViewById(R.id.editTextFirst);
		editTextFirst.addTextChangedListener(this);
		editTextFirst2 = (EditText) this.findViewById(R.id.editTextFirst2);
		editTextFirst2.addTextChangedListener(this);
		editTextFirst2.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				if (arg1 == KeyEvent.KEYCODE_ENTER
						&& arg2.getAction() == KeyEvent.ACTION_DOWN) {
					if (startButton.isEnabled()) {
						ClickStartButton();
						return true;
					}
				}
				return false;
			}

		});
		startButton.setEnabled(false);
		startButton.setOnClickListener(this);
	}

	private void showNextActivity(String password) {
		Intent intent = new Intent(this, FileListActivity.class);
		intent.putExtra("password", password);
		this.startActivity(intent);
		this.finish();
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		boolean flag1 = false, flag2 = false;
		String t1 = editTextFirst.getText().toString();
		String t2 = editTextFirst2.getText().toString();
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
			startButton.setEnabled(true);
		} else {
			startButton.setEnabled(false);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_main_exit: {
			this.finish();
		}
		default:
			break;
		}
		return true;
	}

}
