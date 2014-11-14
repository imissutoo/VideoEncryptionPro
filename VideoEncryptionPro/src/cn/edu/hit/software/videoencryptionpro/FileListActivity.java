package cn.edu.hit.software.videoencryptionpro;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FileListActivity extends TabActivity implements OnClickListener {

	private int longClick = 0;
	// 选择搜索层数时选中的层数
	private int check = 0;

	private boolean isScaning = false;
	// 按下返回的时间
	private long exitTime = 0;
	private TabHost myTabHost = null;
	// 存储导入的视频列表
	private List<Node> data1 = new ArrayList<Node>();
	private int data1Count;
	// 同步界面上的checkbox
	private HashMap<Integer, CheckBox> dataCheck1 = new HashMap<Integer, CheckBox>();
	// 存储未导入的视频列表
	private List<MyNode> data2 = new ArrayList<MyNode>();
	// 同步界面上的checkbox
	private HashMap<Integer, CheckBox> dataCheck2 = new HashMap<Integer, CheckBox>();
	// 存储用户密码
	private String password = null;
	private BitmapTools bTools = null;
	// 在选择文件夹，否则在选择文件
	private boolean isChoosingFile = true;
	// 选中了那个文件夹
	private int folderNum = 0;
	// 是否在选择
	private boolean isSettingState = false;
	// 显示“再按一次退出程序”的toast
	private Toast toast = null;
	// 显示“正在刷新”的toast
	private Toast toastRefresh = null;
	// 两个页面的蒙版
	private ImageView imageViewFore1, imageViewFore2;
	// 表示是否在刷新
	private boolean isRefresh = false;
	// 线程队列分别负责列表1中缩略图的加载和列表2中缩略图的加载
	private MySmallPicHandler mySmallPicHandler = new MySmallPicHandler();
	// 表示是否正在退出
	private boolean isExiting = false;
	private ListView listView1, listView2;
	private Button buttonImport, buttonCancel, buttonSelectAll,
			buttonSelectAll1, buttonDelete, buttonRemove;
	private TableLayout relativeLayout, relativeLayout1;
	private TextView textView = null;
	private MyAdapter1 myAdapter1 = null;
	private MyAdapter2 myAdapter2 = null;

	private GestureDetector gestureDetector = null;// 用户滑动

	private int flaggingWidth;// 互动翻页所需滚动的长度是当前屏幕宽度的1/3
	// 获取分辨率
	DisplayMetrics dm = new DisplayMetrics();

	// 复杂对话框
	private AlertDialog clearDialog;
	private TextView clearDialogText;
	private TextView clearDialogt1, clearDialogt2;
	private ProgressBar clearDialogPro;

	private MyClearHandler myClearHandler = new MyClearHandler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		password = this.getIntent().getStringExtra("password");
		super.onCreate(savedInstanceState);
		// 初始化界面
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		myTabHost = this.getTabHost();
		
		LayoutInflater.from(this).inflate(R.layout.activity_file_list,
				myTabHost.getTabContentView(), true);
		String temp = this.getResources().getString(R.string.alreadyImport);
		myTabHost.addTab(myTabHost.newTabSpec(temp)
				.setContent(R.id.file_list_frameLayout1).setIndicator(temp));
		temp = this.getResources().getString(R.string.notImport);
		myTabHost.addTab(myTabHost.newTabSpec(temp)
				.setContent(R.id.file_list_frameLayout2).setIndicator(temp));
		// 初始化数据
		bTools = new BitmapTools(this);
		listView1 = (ListView) this.findViewById(R.id.file_list_list1_ListView);
		listView2 = (ListView) this.findViewById(R.id.file_list_list2_listView);
		relativeLayout = (TableLayout) this
				.findViewById(R.id.file_list_frame2_low);
		relativeLayout1 = (TableLayout) this
				.findViewById(R.id.file_list_relative);
		buttonImport = (Button) this.findViewById(R.id.file_list_frame2_import);
		buttonCancel = (Button) this.findViewById(R.id.file_list_frame2_cancel);
		buttonSelectAll = (Button) this
				.findViewById(R.id.file_list_frame2_selectAll);
		buttonSelectAll1 = (Button) this
				.findViewById(R.id.file_list_button_selectAll);
		buttonDelete = (Button) this.findViewById(R.id.file_list_button_delete);
		buttonRemove = (Button) this.findViewById(R.id.file_list_button_remove);
		textView = (TextView) this.findViewById(R.id.file_list_frame2_textView);
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		flaggingWidth = dm.widthPixels / 3;
		gestureDetector = new GestureDetector(new TabHostTouch());
		imageViewFore1 = (ImageView) this
				.findViewById(R.id.file_list_frame1_fore);
		imageViewFore2 = (ImageView) this
				.findViewById(R.id.file_list_frame2_fore);
		// 获取数据库中的文件列表 填充数据
		DBTools db = new DBTools(this);
		List<Node> re = db.getFileList();
		this.addToData1(re.iterator());
		db.close();
		// 扫描所有视频文件 填充数据
		List<MyVideoFolder> m = FileTools.getVideosFromFileSystem(data1, this);

		this.addToData2(m.iterator());
		// 绘制界面
		myAdapter1 = new MyAdapter1(this);
		myAdapter2 = new MyAdapter2(this);
		listView1.setAdapter(myAdapter1);
		listView2.setAdapter(myAdapter2);
		myAdapter1.notifyDataSetChanged();
		myAdapter2.notifyDataSetChanged();

		listView1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (!isSettingState) {
					Intent intent = new Intent("android.intent.action.VIEW");
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("oneshot", 0);
					intent.putExtra("configchange", 0);
					Uri uri = Uri.fromFile(data1.get(arg2).file);
					intent.setDataAndType(uri, "video/*");
					startActivity(intent);
				} else {
					CheckBox temp = dataCheck1.get(arg2);
					temp.setChecked(!temp.isChecked());
				}
			}
		});

		listView1.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				switchSettingState(arg2);
				return true;
			}
		});

		listView2.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if (isChoosingFile) {
					goTo2(arg2);
				} else {
					CheckBox temp = dataCheck2.get(arg2);
					temp.setChecked(!temp.isChecked());
				}
			}
		});

		listView2.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				longClick = arg2;
				return false;
			}
		});

		myTabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String arg0) {
				if (myTabHost.getCurrentTab() == 0) {
					if (isSettingState) {
						switchSettingState(0);
					}
				}
			}
		});

		buttonImport.setOnClickListener(this);
		buttonCancel.setOnClickListener(this);
		buttonSelectAll.setOnClickListener(this);
		buttonSelectAll1.setOnClickListener(this);
		buttonDelete.setOnClickListener(this);
		buttonRemove.setOnClickListener(this);
		// 实现在刷新时界面不可点
		imageViewFore1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				return true;
			}
		});
		imageViewFore2.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				return true;
			}
		});

		this.registerForContextMenu(listView2);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		// Log.e("sfs", v.toString() + "  " + longClick);
		if (!isChoosingFile) {
			Node n = data2.get(folderNum).n.get(longClick);
			menu.setHeaderTitle(n.name);
			menu.add(0, 0, 0, R.string.play);
			menu.add(0, 1, 1, R.string.delete);
			menu.add(0, 2, 2, R.string.property);
		}

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/**
	 * 切换是否为设置模式
	 * 
	 * @param t
	 */
	private void switchSettingState(int t) {
		if (isSettingState) {
			isSettingState = false;
			relativeLayout1.setVisibility(View.GONE);
			Iterator<Node> ii = data1.iterator();
			while (ii.hasNext()) {
				Node temp = ii.next();
				temp.isCheckable = false;
				temp.isChecked = false;
			}
		} else {
			isSettingState = true;
			relativeLayout1.setVisibility(View.VISIBLE);
			Iterator<Node> ii = data1.iterator();
			while (ii.hasNext()) {
				Node temp = ii.next();
				temp.isCheckable = true;
				temp.isChecked = false;
			}
			data1.get(t).isChecked = true;

		}
		if (!isScaning)
			myAdapter1.notifyDataSetChanged();
	}

	/**
	 * 向已导入文件列表中添加文件，会尝试解密
	 * 
	 * @param it
	 */
	public void addToData1(Iterator<Node> it) {
		if (it != null) {
			StringBuffer re = new StringBuffer();
			while (it.hasNext()) {
				Node ts = it.next();
				ts.longName = ts.file.getAbsolutePath();
				if (!Node.containPath(data1, ts.longName)) {
					if (ts.file.canRead()) {
						EncodeResult r = CodeHelper.Decode(ts.file, password);
						if (r.result) {
							ts.name = ts.file.getName();
							int a = ts.name.lastIndexOf('.');
							if (a != -1) {
								ts.name = ts.name.substring(0, a);
							}
							ts.b = bTools.GetNoneBitmap();
							data1.add(ts);
						} else {
							re.append(r.tip + "\n");
						}
					}

				}
			}
		}
	}

	/**
	 * 向未导入文件列表中添加文件
	 * 
	 * @param it
	 */
	private void addToData2(Iterator<MyVideoFolder> it) {
		if (it != null) {
			MyVideoFolder v = null;
			while (it.hasNext()) {
				v = it.next();
				int im = ContainFolder(v.path);
				MyNode m;
				if (im == -1) {
					m = new MyNode();
					m.file = new File(v.path);
				} else {
					m = data2.get(im);
				}
				Iterator<String> ii = v.videoList.iterator();
				while (ii.hasNext()) {
					String temp = ii.next();
					Node p = new Node();
					p.file = new File(temp);
					p.longName = p.file.getAbsolutePath();
					p.name = p.file.getName();
					int a = p.name.lastIndexOf('.');
					if (a != -1) {
						p.name = p.name.substring(0, a);
					}
					p.sha = SHA1.Encrypt(p.longName);
					p.b = bTools.GetNoneBitmap();
					p.isCheckable = true;
					m.n.add(p);
				}
				if (im == -1) {
					data2.add(m);
				}
			}
		}
	}

	/**
	 * 判断是否有r文件夹
	 * 
	 * @param r
	 * @return
	 */
	private int ContainFolder(String r) {
		for (int i = 0; i < data2.size(); i++) {
			if (data2.get(i).file.getAbsolutePath().equals(r)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (!isExiting) {
			switch (item.getItemId()) {
			// 播放
			case 0: {
				Intent intent = new Intent("android.intent.action.VIEW");
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("oneshot", 0);
				intent.putExtra("configchange", 0);
				Uri uri = Uri
						.fromFile(data2.get(folderNum).n.get(longClick).file);
				intent.setDataAndType(uri, "video/*");
				startActivity(intent);
				break;
			}
			// 删除
			case 1: {
				Node n = data2.get(folderNum).n.get(longClick);
				Builder b = new Builder(this);
				b.setTitle(R.string.delete);
				b.setMessage("确认删除文件\n\"" + n.name + "\"\n吗？");
				b.setPositiveButton(R.string.OK,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (data2.get(folderNum).n.get(longClick).file
										.delete()) {
									data2.get(folderNum).n.remove(longClick);
									if (data2.get(folderNum).n.size() == 0) {
										data2.remove(folderNum);
										goBack1();
									} else {
										myAdapter2.notifyDataSetChanged();
									}
								}
							}
						});
				b.setNegativeButton(R.string.cancel, null);
				b.show();
				break;
			}
			// 属性
			case 2: {
				Node n = data2.get(folderNum).n.get(longClick);
				long l = n.file.length();
				String ts;
				if (l / (1024 * 1024 * 1024) != 0) {
					ts = String.valueOf(((double) l) / (1024 * 1024 * 1024));
					if (ts.length() > 4) {
						ts = ts.substring(0, 4);
					}
					ts += "GB";
				} else if (l / (1024 * 1024) != 0) {
					ts = String.valueOf(((double) l) / (1024 * 1024));
					if (ts.length() > 4) {
						ts = ts.substring(0, 4);
					}
					ts += "MB";
				} else if (l / 1024 != 0) {
					ts = String.valueOf(((double) l) / 1024);
					if (ts.length() > 4) {
						ts = ts.substring(0, 4);
					}
					ts += "KB";
				} else {
					ts = String.valueOf(l) + "B";
				}
				Builder b = new Builder(this);
				b.setTitle(n.name);
				LayoutInflater inflater = LayoutInflater.from(this);
				TableLayout t = (TableLayout) inflater.inflate(
						R.layout.dialog_video_property, null);
				((TextView) t.findViewById(R.id.dialog_video_property_fileName))
						.setText(n.file.getName());
				((TextView) t.findViewById(R.id.dialog_video_property_path))
						.setText(data2.get(folderNum).file.getAbsolutePath());
				((TextView) t.findViewById(R.id.dialog_video_property_size))
						.setText(ts + "(" + l + ")");
				Date date = new Date(n.file.lastModified());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM");
				String dd = sdf.format(date);
				((TextView) t.findViewById(R.id.dialog_video_property_date))
						.setText(dd);
				b.setView(t);
				b.setPositiveButton(R.string.OK, null);
				b.show();
				break;
			}
			// 清理
			// !!!!!!!!!!case 3 将不可能发生，这个选项已经去除
			case 3: {
				Node n = data2.get(folderNum).n.get(longClick);
				final Node tn = n.clone();
				// 标识是否取消
				tn.isCheckable = true;
				LayoutInflater inflater = LayoutInflater.from(this);
				LinearLayout v = (LinearLayout) inflater.inflate(
						R.layout.dialog_clear,
						(ViewGroup) findViewById(R.id.dialog_clear));
				clearDialogText = (TextView) v
						.findViewById(R.id.dialog_clear_progressing);
				clearDialogPro = (ProgressBar) v
						.findViewById(R.id.dialog_clear_progressBar);
				clearDialogt1 = (TextView) v
						.findViewById(R.id.dialog_clear_text1);
				clearDialogt2 = (TextView) v
						.findViewById(R.id.dialog_clear_text2);
				((Button) v.findViewById(R.id.dialog_clear_button))
						.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								tn.isCheckable = false;
							}
						});
				clearDialog = new AlertDialog.Builder(this)
						.setCancelable(false).setTitle(R.string.clear)
						.setView(v).show();

				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						CodeHelper.Clear(tn, myClearHandler);
					}
				});
				t.start();
				break;
			}
			// 刷新
			case R.id.menu_file_list_refresh: {
				if (!isRefresh) {
					isRefresh = true;
					if (!isChoosingFile) {
						goBack1();
					}
					if (isSettingState) {
						switchSettingState(0);
					}
					imageViewFore1.setVisibility(View.VISIBLE);
					imageViewFore2.setVisibility(View.VISIBLE);
					if (toast != null) {
						exitTime = 0;
						toast.cancel();
					}
					toastRefresh = Toast.makeText(FileListActivity.this,
							"正在刷新", Toast.LENGTH_LONG);
					toastRefresh.setGravity(Gravity.CENTER, 0, 0);
					LinearLayout t = (LinearLayout) toastRefresh.getView();
					ImageView imageView = new ImageView(getApplicationContext());
					imageView.setImageResource(R.drawable.refresh);
					Animation animation = AnimationUtils.loadAnimation(
							FileListActivity.this, R.anim.rotate);
					t.addView(imageView, 0);
					toastRefresh.show();
					imageView.startAnimation(animation);
					Runnable rr = new Runnable() {
						@Override
						public void run() {
							isScaning = true;
							// 保存视频
							Save();
							// 执行过快，导致listView的刷新，刷新时，获取到count的时候刚好data被清空，会出错
							// 必须等待已经启动listView刷新的主线程
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							// 清空缓存
							data1.clear();
							dataCheck1.clear();
							data2.clear();
							dataCheck2.clear();
							// 获取数据库中的文件列表 填充数据
							DBTools db = new DBTools(FileListActivity.this);
							List<Node> re = db.getFileList();
							addToData1(re.iterator());
							db.close();
							// 扫描所有视频文件 填充数据
							List<MyVideoFolder> m = FileTools
									.getVideosFromFileSystem(data1,
											FileListActivity.this);
							addToData2(m.iterator());
							isScaning = false;
							mySmallPicHandler.post(new Runnable() {
								@Override
								public void run() {
									myAdapter1.notifyDataSetChanged();
									myAdapter2.notifyDataSetChanged();
									imageViewFore1.setVisibility(View.GONE);
									imageViewFore2.setVisibility(View.GONE);
									isRefresh = false;
									if (isExiting) {
										finish();
									}
								}
							});
						}
					};
					final Thread tt = new Thread(rr);
					tt.start();
				}
				break;
			}
			// 退出
			case R.id.menu_file_list_exit: {
				if (!isRefresh) {
					finish();
				} else {
					isExiting = true;
				}
				break;
			}
			// 关于
			case R.id.menu_file_list_about: {
				StringBuffer sb = new StringBuffer();
				sb.append("版本：视频加密V1.5\n");
				sb.append("作者：黑暗游侠\n");
				sb.append("邮箱：\n");
				sb.append("hanyizhao10@gmail.com\n\n");
				sb.append("");
				Builder b = new Builder(this);
				b.setTitle(R.string.about);
				b.setNegativeButton(R.string.OK, null);
				b.setMessage(sb);
				b.show();
				break;
			}
			// 原理
			// case R.id.menu_file_list_principle: {
			// Intent i = new Intent(this, PrincipleActivity.class);
			// this.startActivity(i);
			// break;
			// }
			// 注意
			case R.id.menu_file_list_tips: {
				Intent i = new Intent(this, TipsActivity.class);
				this.startActivity(i);
				break;
			}
			// 设置密码
			case R.id.menu_file_list_correctPassword: {
				Intent i = new Intent(this, SetPasswordActivity.class);
				i.putExtra("password", password);
				this.startActivityForResult(i, CodeHelper.CODE1);
				break;
			}
			// 设置深度
			case R.id.menu_file_list_searchDeep: {
				int d = FileTools.getDepthOfTheSearchForFile();
				Builder b = new Builder(this);
				b.setTitle(R.string.setDepthOfTheSearchForFile);
				CharSequence[] items = { "2层", "3层", "4层" };
				check = d - 2;
				b.setSingleChoiceItems(items, d - 2,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								check = which;
							}
						});
				b.setPositiveButton(R.string.OK,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								FileTools.setDepthOfTheSearchForFile(check + 2);
							}
						});
				b.setNegativeButton(R.string.cancel, null);
				b.show();
				break;
			}
			default:
				break;
			}
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CodeHelper.CODE1) {
			if (resultCode == CodeHelper.OK) {
				password = data.getStringExtra("password");
				// 100毫秒后执行，可以使输入法框落下
				mySmallPicHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						Builder b = new Builder(FileListActivity.this);
						b.setTitle("成功");
						b.setMessage("修改密码成功！");
						b.setPositiveButton(R.string.OK, null);
						b.show();
					}
				}, 100);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_list, menu);
		return true;
	}

	class MyAdapter1 extends BaseAdapter {

		private LayoutInflater la = null;

		public MyAdapter1(Context c) {
			la = LayoutInflater.from(c);
		}

		@Override
		public int getCount() {
			return data1.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			View v = la.inflate(R.layout.activity_file_list_list1, null);
			ImageView image = (ImageView) v
					.findViewById(R.id.file_list_list1_image);
			TextView name = (TextView) v
					.findViewById(R.id.file_list_list1_textView);
			CheckBox checkBox = (CheckBox) v
					.findViewById(R.id.file_list_list1_checkBox);
			dataCheck1.put(arg0, checkBox);
			name.setText(data1.get(arg0).name);
			image.setImageBitmap(data1.get(arg0).b);
			if (data1.get(arg0).b == bTools.GetNoneBitmap()) {
				Message msg = mySmallPicHandler.obtainMessage();
				msg.arg1 = 1;
				msg.arg2 = arg0;
				msg.sendToTarget();
			}
			if (data1.get(arg0).isCheckable) {
				checkBox.setVisibility(View.VISIBLE);
			} else {
				checkBox.setVisibility(View.INVISIBLE);
			}
			if (data1.get(arg0).isChecked) {
				checkBox.setChecked(true);
			} else {
				checkBox.setChecked(false);
			}
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					int po = indexOfCheckBox1(arg0, data1.size());
					data1.get(po).isChecked = arg1;
					if (arg1) {
						boolean flag = true;
						Iterator<Node> ii = data1.iterator();
						while (ii.hasNext()) {
							if (!ii.next().isChecked) {
								flag = false;
								break;
							}
						}
						if (flag) {
							buttonSelectAll1.setText(R.string.disSelectALl);
						}
					} else {
						buttonSelectAll1.setText(R.string.selectAll);
					}
				}

			});
			return v;
		}

	}

	class MyAdapter2 extends BaseAdapter {

		private LayoutInflater la = null;

		public MyAdapter2(Context c) {
			la = LayoutInflater.from(c);
		}

		@Override
		public int getCount() {
			if (isChoosingFile) {
				return data2.size();
			} else {
				return data2.get(folderNum).n.size();
			}
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View arg1, ViewGroup arg2) {
			View v = null;
			if (isChoosingFile) {
				v = la.inflate(R.layout.activity_file_list_list2, null);

				TextView name = (TextView) v
						.findViewById(R.id.file_list_list2_textView1);
				TextView num = (TextView) v
						.findViewById(R.id.file_list_list2_textView2);

				name.setText(data2.get(position).file.getName());
				num.setText(data2.get(position).n.size() + "部视频");
			} else {
				Node ttt = data2.get(folderNum).n.get(position);
				v = la.inflate(R.layout.activity_file_list_list1, null);
				ImageView image = (ImageView) v
						.findViewById(R.id.file_list_list1_image);
				TextView name = (TextView) v
						.findViewById(R.id.file_list_list1_textView);
				CheckBox checkBox = (CheckBox) v
						.findViewById(R.id.file_list_list1_checkBox);
				dataCheck2.put(position, checkBox);
				name.setText(ttt.name);
				image.setImageBitmap(ttt.b);
				if (!ttt.isSearchSmallPic && ttt.b == bTools.GetNoneBitmap()) {
					ttt.isSearchSmallPic = true;
					Message msg = mySmallPicHandler.obtainMessage();
					msg.arg1 = 2;
					msg.arg2 = folderNum;
					Bundle b = new Bundle();
					b.putInt("position2", position);
					msg.setData(b);
					msg.sendToTarget();
				}

				if (ttt.isCheckable) {
					checkBox.setVisibility(View.VISIBLE);
				} else {
					checkBox.setVisibility(View.INVISIBLE);
				}
				if (ttt.isChecked) {
					checkBox.setChecked(true);
				} else {
					checkBox.setChecked(false);
				}
				checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						int po = indexOfCheckBox2(arg0,
								data2.get(folderNum).n.size());
						data2.get(folderNum).n.get(po).isChecked = arg1;
						if (arg1) {
							boolean flag = true;
							Iterator<Node> ii = data2.get(folderNum).n
									.iterator();
							while (ii.hasNext()) {
								if (!ii.next().isChecked) {
									flag = false;
									break;
								}
							}
							if (flag) {
								buttonSelectAll.setText(R.string.disSelectALl);
							}
						} else {
							buttonSelectAll.setText(R.string.selectAll);
						}
					}

				});
			}
			return v;
		}
	}

	/**
	 * 打开文件夹
	 * 
	 * @param fold
	 */
	private void goTo2(int fold) {
		isChoosingFile = false;
		textView.setText(data2.get(fold).file.getName());
		folderNum = fold;
		dataCheck2.clear();
		Iterator<Node> ii = data2.get(fold).n.iterator();
		while (ii.hasNext()) {
			ii.next().isChecked = false;
		}
		buttonSelectAll.setText(R.string.selectAll);
		relativeLayout.setVisibility(View.VISIBLE);
		if (!isScaning)
			myAdapter2.notifyDataSetInvalidated();
	}

	/**
	 * 返回至文件夹列表
	 */
	private void goBack1() {
		isChoosingFile = true;
		textView.setText(R.string.folder);
		relativeLayout.setVisibility(View.GONE);
		if (!isScaning)
			myAdapter2.notifyDataSetChanged();
	}

	private int indexOfCheckBox1(CompoundButton t, int size) {
		for (int i = 0; i < size; i++) {
			CheckBox temp = dataCheck1.get(i);
			if (temp != null && temp == t) {
				return i;
			}
		}
		return 0;
	}

	private int indexOfCheckBox2(CompoundButton t, int size) {
		for (int i = 0; i < size; i++) {
			CheckBox temp = dataCheck2.get(i);
			if (temp != null && temp == t) {
				return i;
			}
		}
		return 0;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			// 是否退出标识
			boolean flag = false;
			if (myTabHost.getCurrentTab() == 0) {
				if (isSettingState) {
					switchSettingState(0);
					return true;
				} else {
					flag = true;
				}
			} else {
				if (isChoosingFile) {
					flag = true;
				} else {
					this.goBack1();
					return true;
				}
			}
			if (flag) {
				if (!isExiting) {
					if (toastRefresh != null) {
						toastRefresh.cancel();
					}
					if (toast == null) {
						toast = Toast.makeText(getApplicationContext(),
								R.string.PressAgainToEncodeAndToExitTheProgram,
								Toast.LENGTH_SHORT);
					}
					long now = System.currentTimeMillis();
					if (now - exitTime <= 2000) {
						toast.cancel();
						if (isRefresh) {
							isExiting = true;
						} else {
							finish();
						}
					} else {
						exitTime = now;
						toast.show();
					}
				}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View arg0) {
		if (arg0 == buttonImport) {
			MyNode m = data2.get(folderNum);
			int count = 0;
			for (int i = 0; i < m.n.size(); i++) {
				Node temp = m.n.get(i);
				if (temp.isChecked) {
					EncodeResult r = CodeHelper.Decode(temp.file, password);
					if (r.result) {
						m.n.remove(i);
						temp.isCheckable = false;
						temp.isChecked = false;
						data1.add(temp);
						count++;
						i--;
					} else {
						Builder b = new Builder(this);
						b.setTitle("错误");
						b.setMessage("文件\"" + temp.name + "\"\n" + r.tip);
						/*
						 * b.setPositiveButton("确认", new
						 * DialogInterface.OnClickListener() {
						 * 
						 * @Override public void onClick(DialogInterface dialog,
						 * int which) { finish(); }
						 * 
						 * });
						 */
						b.show();
					}
				}
			}
			if (m.n.size() == 0) {
				data2.remove(folderNum);
				this.goBack1();
			}
			if (!isScaning) {
				myAdapter2.notifyDataSetChanged();
				myAdapter1.notifyDataSetChanged();
			}
			if (count != 0) {
				myTabHost.setCurrentTab(0);
				// 弹窗
				// Builder b = new Builder(this);
				// b.setTitle(R.string.success);
				// b.setMessage("已成功导入" + count + "个文件！");
				// b.setPositiveButton(R.string.OK, null);
				// b.show();
			}
		} else if (arg0 == buttonCancel) {
			this.goBack1();
			myTabHost.setCurrentTab(0);
		} else if (arg0 == buttonSelectAll) {
			if (((String) buttonSelectAll.getText()).equals(getResources()
					.getString(R.string.selectAll))) {
				buttonSelectAll.setText(R.string.disSelectALl);
				Iterator<Node> ii = data2.get(folderNum).n.iterator();
				while (ii.hasNext()) {
					ii.next().isChecked = true;
				}
				if (!isScaning)
					myAdapter2.notifyDataSetChanged();
			} else {
				buttonSelectAll.setText(R.string.selectAll);
				Iterator<Node> ii = data2.get(folderNum).n.iterator();
				while (ii.hasNext()) {
					ii.next().isChecked = false;
				}
				if (!isScaning)
					myAdapter2.notifyDataSetChanged();
			}
		} else if (arg0 == buttonSelectAll1) {
			if (((String) buttonSelectAll1.getText()).equals(getResources()
					.getString(R.string.selectAll))) {
				buttonSelectAll1.setText(R.string.disSelectALl);
				Iterator<Node> ii = data1.iterator();
				while (ii.hasNext()) {
					ii.next().isChecked = true;
				}

			} else {
				buttonSelectAll1.setText(R.string.selectAll);
				Iterator<Node> ii = data1.iterator();
				while (ii.hasNext()) {
					ii.next().isChecked = false;
				}
			}
			if (!isScaning)
				myAdapter1.notifyDataSetChanged();
		} else if (arg0 == buttonRemove) {
			int count = 0;
			for (int i = 0; i < data1.size(); i++) {
				Node temp = data1.get(i);
				if (temp.isChecked) {
					List<MyVideoFolder> ml = new ArrayList<MyVideoFolder>();
					MyVideoFolder m = new MyVideoFolder();
					m.path = temp.longName.substring(0,
							temp.longName.lastIndexOf('/'));
					m.videoList.add(temp.longName);
					ml.add(m);
					addToData2(ml.iterator());
					data1.remove(i);
					count++;
					i--;
				}
			}
			if (!isScaning) {
				myAdapter1.notifyDataSetChanged();
				myAdapter2.notifyDataSetChanged();
			}
			// 弹窗
			// if (count != 0) {
			// Builder b = new Builder(this);
			// b.setTitle(R.string.success);
			// b.setMessage("已成功移除" + count + "个文件！");
			// b.setPositiveButton(R.string.OK, null);
			// b.show();
			// }
		} else if (arg0 == buttonDelete) {
			int count = 0;
			Node n = null;
			for (int i = 0; i < data1.size(); i++) {
				if (data1.get(i).isChecked) {
					count++;
					n = data1.get(i);
				}
			}
			if (count != 0) {
				Builder b = new Builder(this);
				b.setTitle("删除");
				b.setMessage("确认删除\n" + n.name + "...\n等" + count + "个文件吗？");
				b.setPositiveButton("确认",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								for (int i = 0; i < data1.size(); i++) {
									if (data1.get(i).isChecked) {
										if (data1.get(i).file.delete()) {
											data1.remove(i);
											i--;
										}
									}
								}
								if (!isScaning)
									myAdapter1.notifyDataSetChanged();
							}
						});
				b.setNegativeButton("取消", null);
				b.show();
			}
		}
	}

	@Override
	protected void onDestroy() {
		Save();
		super.onDestroy();
	}

	private void Save() {
		List<Node> f = new ArrayList<Node>();
		DBTools db = new DBTools(this);
		for (int i = 0; i < data1.size(); i++) {
			EncodeResult result = CodeHelper
					.Encode(data1.get(i).file, password);
			if (result.result) {
				f.add(data1.get(i));
			}
		}
		db.setFileList(f);
		db.close();
	}

	private class TabHostTouch extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (Math.abs(e1.getX() - e2.getX()) > Math.abs(e1.getY()
					- e2.getY())
					&& (e1.getX() - e2.getX() <= (-flaggingWidth) || e1.getX()
							- e2.getX() >= flaggingWidth)) {
				if (e1.getX() - e2.getX() <= (-flaggingWidth)) {
					int currentTabID = myTabHost.getCurrentTab() - 1;
					if (currentTabID < 0) {
						currentTabID = 1;
					}
					myTabHost.setCurrentTab(currentTabID);
					return true;
				} else if (e1.getX() - e2.getX() >= flaggingWidth) {
					int currentTabID = myTabHost.getCurrentTab() + 1;
					if (currentTabID > 1) {
						currentTabID = 0;
					}
					myTabHost.setCurrentTab(currentTabID);
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event)) {
			event.setAction(MotionEvent.ACTION_CANCEL);
		}
		return super.dispatchTouchEvent(event);
	}

	class Runn extends Thread {
		Node te;

		public Runn(Node te) {
			this.te = te;
		}

		@Override
		public void run() {
			bTools.GetSmallPic(te);
		}

	}

	/**
	 * 处理smallpic的动态加载
	 * 
	 * @author Administrator
	 * 
	 */
	class MySmallPicHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			int a1 = msg.arg1;
			int a2 = msg.arg2;
			if (a1 == 1) {
				try {
					Node te = data1.get(a2);
					Runn r = new Runn(te);
					r.start();
					mySmallPicHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (!isScaning)
								myAdapter1.notifyDataSetChanged();
						}
					}, 500);
					mySmallPicHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (!isScaning)
								myAdapter1.notifyDataSetChanged();
						}
					}, 3000);
				} catch (IndexOutOfBoundsException e) {
				}
			} else {
				try {
					Bundle b = msg.getData();
					int tempcc = b.getInt("position2");
					Node te = data2.get(a2).n.get(tempcc);
					Runn r = new Runn(te);
					r.start();
					mySmallPicHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (!isScaning)
								myAdapter2.notifyDataSetChanged();
						}
					}, 100);
					mySmallPicHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (!isScaning)
								myAdapter2.notifyDataSetChanged();
						}
					}, 3000);
				} catch (IndexOutOfBoundsException e) {
				}
			}
		}
	}

	class MyClearHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			if (msg.arg1 == 0) {
				Bundle b = msg.getData();
				long now = b.getLong("now");
				long times = b.getLong("times");
				if (now != times) {
					clearDialogText.setText(R.string.progressing);
				} else {
					clearDialogText.setText(R.string.finish);
				}
				clearDialogt1.setText(now * 100 / times + "%");
				clearDialogt2.setText(now + "/" + times + "MB");
				clearDialogPro.setProgress((int) (now * 100 / times));
			} else {
				Bundle b = msg.getData();
				String v = b.getString("value");
				if (v.equals("o")) {
					clearDialog.cancel();
				} else if (v.equals("p")) {
					Builder bf = new Builder(FileListActivity.this);
					bf.setTitle("成功");
					bf.setMessage(R.string.successToCancel);
					bf.setPositiveButton(R.string.OK, null);
					bf.show();
					clearDialog.cancel();
				} else {
					Builder bf = new Builder(FileListActivity.this);
					bf.setTitle("错误");
					bf.setMessage(v);
					bf.setPositiveButton(R.string.OK, null);
					bf.show();
					clearDialog.cancel();
				}
			}
		}

	}

}

/**
 * 存储一个视频文件下的所有视频信息
 * 
 * @author Administrator
 * 
 */
class MyNode {
	File file;
	List<Node> n = new ArrayList<Node>();
}

/**
 * 存储视频列表中的一项
 * 
 * @author Administrator
 * 
 */
class Node {
	File file = null;
	String longName = null;
	String sha = null;
	Bitmap b = null;
	String name = null;
	boolean isCheckable = false;
	boolean isChecked = false;
	boolean isSearchSmallPic = false;

	public static boolean containPath(List<Node> hasImported, String a) {
		Iterator<Node> ii = hasImported.iterator();
		while (ii.hasNext()) {
			if (ii.next().longName.equals(a)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected Node clone() {
		Node temp = new Node();
		temp.b = this.b;
		temp.file = this.file;
		temp.isCheckable = this.isCheckable;
		temp.isChecked = this.isChecked;
		temp.isSearchSmallPic = this.isSearchSmallPic;
		temp.longName = this.longName;
		temp.name = this.name;
		temp.sha = this.sha;
		return temp;
	}

	public static boolean containPath(HashSet<String> hasImported, String a) {
		if (hasImported.contains(a))
			return true;
		return false;
	}

}
