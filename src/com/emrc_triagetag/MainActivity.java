package com.emrc_triagetag;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;

import android.*;
import android.annotation.*;
import android.app.*;
import android.content.*;
import android.content.IntentFilter.*;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.net.*;
import android.nfc.*;
import android.nfc.tech.*;
import android.os.*;
import android.speech.RecognizerIntent;
import android.support.v4.view.*;
import android.support.v4.view.ViewPager.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.AdapterView.*;
import android.widget.*;

import com.emrc_transport.Adler32;
import com.emrc_transport.CheckedDataInput;
import com.emrc_triagetag.HomeListen.*;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;

@SuppressWarnings("deprecation")
@SuppressLint("HandlerLeak")
public class MainActivity extends Activity implements OnClickListener, OnItemClickListener, OnItemLongClickListener,
		OnItemSelectedListener, OnPageChangeListener,OnLongClickListener {
	// 192.168.1.150 // 120.119.155.80
	private static String ip = "120.119.155.80";
	//EMRC:2000 桃機:1995 台中:2001
	private static int port = 2000;
	private int id_Y = 3019, id_M = 10, id_D = 20;
	// private String Web = "http://120.119.155.80/app/";
	private boolean write = false, nfc = false,RLink = false;
	private boolean home = false, sktRun = false, dblist = false;
	private static final String TAG = "nfcproject";
	private static Spinner spinner,incident_spinner;
	private static TextView medi_unit, medi_qunt, print_No, menu_No;
	private static ViewPager pager;
	private AlertDialog alertDialog;
	private Button main_log;
	private HomeListen mHomeListen = null;
	private IntentFilter[] gNdefExchangeFilters, gWriteTagFilters;
	private ImageView main_left, main_right, medi_clear, medi_swap, menu_ok, menu_re, bg_emrc;
	private NfcAdapter nfcAdapter;
	private PendingIntent gNfcPendingIntent;
	private ProgressDialog mProgressDialog;
	private TextView print_item_0, print_item_1, print_item_2, print_item_3;
	private TextView print_item_4, print_item_5, print_item_6,inc_txt,menu_offline,menu_offline1;
	private static final String KEY_STORE_CLIENT_PATH = "kclient.bks"; // 客户端要给服务器端认证的证书
	private static final String KEY_STORE_TRUST_PATH = "tclient.bks"; // 客户端验证服务器端的证书库
	private static final String KEY_STORE_PASSWORD = "123456"; // 客户端证书密码
	public static int gender = 2, leve_count = 5, phot_count = 0, dev_count = 20;
	public static int emrc_count = 0, status_count = 6, textSize = 0,inc_code = 0;
	public static boolean net = false, link = false, user = true, f = true, EN = false, dev = false;
	public static String mmsg, handmsg, number = "", car_brand = "", identity = "", emrgn = "", hosp_count = "",info_age="-1";
	public static Socket skt;
	public static Bitmap info_photo = null, tmp_bmp = null;
	private static ListView menu_list, medi_list;
	private Handler LinkHandler = new Handler();
	
	
	private String h="";
	private int MH=0;

	public static DisplayMetrics metrics = new DisplayMetrics();
	public static ArrayList<String> menu_item = new ArrayList<String>();

	public static ArrayList<String> medi_item = new ArrayList<String>();
	public static ArrayList<String> lead_item = new ArrayList<String>();
	private static ArrayList<View> main_view = new ArrayList<View>();
	private static ArrayList<View> menu_view = new ArrayList<View>();

	public static ArrayList<Double> inju_front = new ArrayList<Double>();
	public static ArrayList<Double> inju_back = new ArrayList<Double>();
	public static ArrayList<Double> inju_target = new ArrayList<Double>();
	public static ArrayList<String> info_data = new ArrayList<String>();
	public static ArrayList<String> vita_data = new ArrayList<String>();
	public static ArrayList<String> emrgn_data = new ArrayList<String>();
	public static ArrayList<Bitmap> phot_bitmap = new ArrayList<Bitmap>();
	public static ArrayList<String> incident_unit = new ArrayList<String>();
	public static ArrayList<String> incident_code = new ArrayList<String>();

	public static ArrayList<hospital> Treatment_unit = new ArrayList<hospital>();

	private static TextView EMT_txt;

	public static String QRcode = "", qr_msg = "";
//	public static String EMT ="";
	private ImageView speak;
	private ArrayList<String> speak_str = new ArrayList<String>();
	private ArrayList<Integer> speak_num = new ArrayList<Integer>();
	String speak_s = "";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		metrics = getResources().getDisplayMetrics();
		findViewById();
		load_DEV_DATA();
		checkToday(0, 0, 0, false);
		addTab();
		view_0();
		view_1();
		view_2();
		// view_3();
		setTARGET();
		setNFC();
		setNET();
		setHOME();
		// load_USER_DATA();
		// ==================================
		// 直接開啟檢傷模式
		pager_upload();
		user_upload();
		// ==================================
		setPermission();

	}

	protected void onStart() {
		super.onStart();
	}

	protected void onStop() {
		super.onStop();
	}

	protected void onPause() {
		super.onPause();
		if (home) {
			mHomeListen.stop();
		}
		if (nfc) {
			// 由於NfcAdapter啟動前景模式將相對花費更多的電力，要記得關閉。
			nfcAdapter.disableForegroundNdefPush(this);
		}
	}

	protected void onResume() {
		super.onResume();
		if (home) {
			mHomeListen.start();
		}
		if (nfc) {
			// TODO 處理由Android系統送出應用程式處理的intent filter內容
			if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
				NdefMessage[] messages = getNdefMessages(getIntent());
				String data = new String(messages[0].getRecords()[0].getPayload());
				// 往下送出該intent給其他的處理對象
				setIntent(new Intent());
				String msg = "";
				if (checkData(data)) {
					msg = Decrypt(data);
					// toast("De", this);
				} else {
					msg = data;
					// toast(msg, this);
					toast(getString(R.string.s_ts_57), this);
				}
				if (msg.length() > 0) {
					final String finalMsg = msg;
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							getNFCString(finalMsg);
						}
					}, 1000);
					
				} else {
					toast(getString(R.string.s_ts_0), this);
				}
				// toast("onResume", this);
			}
			// 啟動前景模式支持Nfc intent處理
			enableNdefExchangeMode();
		}
	}

	protected void onDestroy() {
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			save();
			new AlertDialog.Builder(MainActivity.this).setTitle(getString(R.string.s_ts_61))
					.setPositiveButton(getString(R.string.s_ts_12), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {

						}
					}).setNegativeButton(getString(R.string.s_ts_13), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							android.os.Process.killProcess(android.os.Process.myPid());
						}
					}).show();
			return true;
		default:
			break;
		}
		return false;
	}

	private void setHOME() {
		home = true;
		mHomeListen = new HomeListen(this);
		mHomeListen.setOnHomeBtnPressListener(new OnHomeBtnPressLitener() {
			public void onHomeBtnPress() {
				// toast(MainActivity.this, "按下Home按键！");
				save();
				android.os.Process.killProcess(android.os.Process.myPid());

			}

			public void onHomeBtnLongPress() {
				// toast(MainActivity.this, "长按Home按键！");
				save();
				android.os.Process.killProcess(android.os.Process.myPid());

			}
		});
	}

	@TargetApi(23)
	private void setPermission() {
		if (Build.VERSION.SDK_INT >= 23) {
			int STORAGE = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			int CAMERA = checkSelfPermission(Manifest.permission.CAMERA);

			if (permission(STORAGE) || permission(CAMERA)) {
				showMessageOKCancel("親愛的用戶您好:\n由於Android 6.0 以上的版本在權限上有些更動，我們需要您授權以下的權限，感謝。",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								showPermission();
							}
						});
			} else {
				//檢傷人員輸入
				final AlertDialog.Builder editDialog = new AlertDialog.Builder(MainActivity.this);
				editDialog.setTitle("檢傷人員");
				final EditText editText = new EditText(MainActivity.this);
				editDialog.setView(editText);
				editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						// 掃QRcode
						//startActivityForResult(new Intent(MainActivity.this, CaptureActivity.class), 1);

						if(editText.getText().length()>0){
							EMT_txt.setText(editText.getText().toString());
//							EMT = editText.getText().toString();
						}
					}
				});
				editDialog.setCancelable(false);
				editDialog.show();
			}
		}
	}

	private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
		new AlertDialog.Builder(this).setMessage(message).setPositiveButton("OK", okListener).setCancelable(false)
				.create().show();
	}

	@TargetApi(23)
	@SuppressLint("NewApi")
	private void showPermission() {
		// We don't have permission so prompt the user
		List<String> permissions = new ArrayList<String>();
		permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		permissions.add(Manifest.permission.CAMERA);
		requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
	}

	@TargetApi(23)
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			// 許可授權
			//檢傷人員輸入
			final AlertDialog.Builder editDialog = new AlertDialog.Builder(MainActivity.this);
			editDialog.setTitle("檢傷人員");
			final EditText editText = new EditText(MainActivity.this);
			editDialog.setView(editText);
			editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					// 掃QRcode
					//startActivityForResult(new Intent(MainActivity.this, CaptureActivity.class), 1);

					if(editText.getText().length()>0){
						EMT_txt.setText(editText.getText().toString());
//						EMT = editText.getText().toString();
					}
				}
			});
			editDialog.setCancelable(false);
			editDialog.show();
		} else {
			// 沒有權限
			toast(getString(R.string.s_ts_58), this);
			showPermission();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch(requestCode){
			case 0:
				showPermission();
				break;
			case 1:
				QRcode = data.getStringExtra("result");
				if(link){
					out("QRC/"+QRcode+"|");
				}				
				menu_No.setText(number + "("+QRcode+")");
				print_No.setText(number + "("+QRcode+")");
				toast(QRcode,this);
				break;
			case 200:
				if(data != null){
					ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					if(result.get(0).length()>0){
//							speak2(result.get(0)+"|");
						Tools.speak(result.get(0));
						toast(result.get(0),this);
					}
				}
				break;
			}
		}
	}

	private boolean permission(int mp) {
		return mp != PackageManager.PERMISSION_GRANTED;
	}

	protected void onNewIntent(Intent intent) {
		// TODO 覆寫該Intent用於補捉如果有新的Intent進入時，可以觸發的事件任務。
		if (!write && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			NdefMessage[] messages = getNdefMessages(intent);
			String data = new String(messages[0].getRecords()[0].getPayload());
			String msg = "";
			if (checkData(data)) {
				msg = Decrypt(data);
			} else {
				msg = data;
				toast(getString(R.string.s_ts_57), this);
			}
			if (msg.length() > 0) {
				getNFCString(msg);
				toast("age = "+info_age+""+"  inc = "+inc_code+"",MainActivity.this);
			} else {
				toast(getString(R.string.s_ts_0), this);
			}
			// toast("onNewIntent", this);
		}

		// 監測到有指定ACTION進入，代表要寫入資料至Tag中。
		if (write && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			writeTag(getNoteAsNdef(), detectedTag);
		}
	}

	private NdefMessage[] getNdefMessages(Intent intent) {
		// Parse the intent
		NdefMessage[] msgs = null;
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
			} else {
				// Unknown tag type
				byte[] empty = new byte[] {};
				NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
				NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
				msgs = new NdefMessage[] { msg };
			}
		} else {
			Log.d(TAG, "Unknown intent.");
			finish();
		}
		return msgs;
	}

	private void save() {
		String File = "", Date = "";
		if (user) {
			File = "DATA.txt";
			if (medi_item.size() > 0) {
				Date = medi_item.size() + "=";
				for (int i = 0; i < medi_item.size(); i++) {
					Date += medi_item.get(i);
				}
				getFile(File, Date);
			}
		}

		if (Treatment_unit.size() > 0) {
			File = "HOSP.txt";
			Date = Treatment_unit.size() + "=";
			for (int i = 0; i < Treatment_unit.size(); i++) {
				Date += Treatment_unit.get(i).unit + "," + Treatment_unit.get(i).level + ","
						+ Treatment_unit.get(i).location + "," + Treatment_unit.get(i).area + "|";
			}
			getFile(File, Date);
		}

		if (dev) {
			File = "DEV.txt";
			Date = "1";
			getFile(File, Date);
		}
	}

	private void getFile(String File, String Date) {
		try {
			FileOutputStream outStream = this.openFileOutput(File, MODE_PRIVATE);
			outStream.write(Date.getBytes());
			outStream.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	private void load_HOSP_DATA() {
		String File = "HOSP.txt";
		String Date = "", DateTmp;
		int tmp = 0, run = 0;

		try {
			FileInputStream fin = this.openFileInput(File);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int size = fin.available();
			byte[] data = new byte[size];
			fin.read(data);
			bos.write(data);
			Date = bos.toString();
			bos.close();
			fin.close();
			if (Date.length() > 0) {
				run = Integer.parseInt(Date.substring(tmp).substring(0, Date.substring(tmp).indexOf('=')));
				tmp += ((run + "").length() + 1);
				for (int i = 0; i < run; i++) {
					DateTmp = Date.substring(tmp + i).substring(0, Date.substring(tmp + i).indexOf('|'));
					tmp += DateTmp.length();
					if (i == 0) {
						Treatment_unit.add(new hospital(getString(R.string.s_H), 0, "", ""));
					} else {
						Object[] strs = DateTmp.split(",");
						Treatment_unit.add(new hospital((String) strs[0], Integer.parseInt((String) strs[1]),
								(String) strs[2] + "," + (String) strs[3], (String) strs[4]));
					}
				}
			}
			// toast(Date, this);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	private void load_DEV_DATA() {
		String File = "DEV.txt";
		String Date = "";
		try {
			FileInputStream fin = this.openFileInput(File);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int size = fin.available();
			byte[] data = new byte[size];
			fin.read(data);
			bos.write(data);
			Date = bos.toString();
			bos.close();
			fin.close();
			if (Date.length() > 0) {
				if (Integer.parseInt(Date) == 1)
					dev = true;
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	private void load_MEDI_DATA() {
		String File;
		if (user) {
			File = "DATA.txt";
		} else {
			File = "MEDI.txt";
		}
		String Date = "", DateTmp;
		int tmp = 0, run = 0;

		try {
			FileInputStream fin = this.openFileInput(File);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int size = fin.available();
			byte[] data = new byte[size];
			fin.read(data);
			bos.write(data);
			Date = bos.toString();
			bos.close();
			fin.close();
			if (Date.length() > 0) {
				run = Integer.parseInt(Date.substring(tmp).substring(0, Date.substring(tmp).indexOf('=')));
				tmp += ((run + "").length() + 1);
				for (int i = 0; i < run; i++) {
					DateTmp = Date.substring(tmp + i).substring(0, Date.substring(tmp + i).indexOf('|'));
					medi_item.add(DateTmp + '|');
					tmp += DateTmp.length();
				}
				user_upload();
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	private void findViewById() {
		main_left = (ImageView) findViewById(R.id.main_left);
		main_right = (ImageView) findViewById(R.id.main_right);
		pager = (ViewPager) findViewById(R.id.main_pager);
		main_log = (Button) findViewById(R.id.main_log);
		main_left.getBackground().setAlpha(50);
		main_right.getBackground().setAlpha(50);
		main_log.setOnClickListener(this);
	}

	private void checkToday(int y, int m, int d, boolean check) {
		int cy, cm, cd;
		Calendar calendar = Calendar.getInstance();
		if (check) {
			if (calendar.get(Calendar.YEAR) != y || calendar.get(Calendar.MONTH) != m - 1
					|| calendar.get(Calendar.DATE) != d) {
				main_log.setVisibility(View.VISIBLE);
				main_log.setText(getString(R.string.s_ts_1));
				toast(getString(R.string.s_ts_1), this);
			}
		} else {
			boolean pass = false;
			y = id_Y;
			m = id_M;
			d = id_D;
			cy = calendar.get(Calendar.YEAR);
			cm = calendar.get(Calendar.MONTH);
			cd = calendar.get(Calendar.DATE);
			if (y > cy) {
				pass = true;
			} else {
				if (y == cy) {
					cm++;
					if (m > cm) {
						pass = true;
					} else {
						if (m == cm) {
							if (d > cd) {
								pass = true;
							} else {
								if (d == cd) {
									pass = true;
								}
							}
						}
					}
				}
			}
			if (!dev) {
				if (pass) {
					main_log.setVisibility(View.GONE);
//					toast(getString(R.string.s_ts_2) + m + getString(R.string.s_ts_4) + d + getString(R.string.s_ts_5),
//							this);
				} else {
					main_log.setVisibility(View.VISIBLE);
//					toast(getString(R.string.s_ts_3) + m + getString(R.string.s_ts_4) + d + getString(R.string.s_ts_5),
//							this);
					try {
						main_log.setText(
								"軟體版本:" + this.getPackageManager().getPackageInfo(getPackageName(), 0).versionCode
										+ "\n" + getString(R.string.s_ts_3) + m + getString(R.string.s_ts_4) + d
										+ getString(R.string.s_ts_5));
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
				}
			} else {
				main_log.setVisibility(View.GONE);
			}
		}
	}

	@SuppressWarnings("static-access")
	@SuppressLint("InflateParams")
	private void addTab() {
		main_view.clear();
		LayoutInflater mInflater = getLayoutInflater().from(this);
		View v0 = mInflater.inflate(R.layout.main_0_view, null); // START
		View v1 = mInflater.inflate(R.layout.main_1_view, null); // 傷患資訊
		View v2 = mInflater.inflate(R.layout.main_2_view, null); // 歷史紀錄
		View v3 = mInflater.inflate(R.layout.main_3_view, null); // 駕駛模式

		// View v5 = mInflater.inflate(R.layout.main_5_view, null);
		// View v6 = mInflater.inflate(R.layout.main_6_view, null);

		// 初始畫面
		main_view.add(v0);
		main_view.add(v1);
		main_view.add(v2);
		main_view.add(v3);

		// main_view.add(v5);
		// main_view.add(v6);

		// 設置標題
		// main_title_list.add("START檢傷");
		// main_title_list.add("檢傷卡");
		// main_title_list.add("即時車輛");
		// main_title_list.add("救護車設定");

		// 建立配適器
		pager.setOnPageChangeListener(this);
		getDeviceWidth();
	}

	private void getDeviceWidth() {
		// 9吋 3904 * 3072
		// 5吋 2368 * 1440
		metrics = getResources().getDisplayMetrics();
		int mWidth = (int) (metrics.widthPixels * metrics.density); // 螢幕寬
		textSize = mWidth / 48;
	}

	private void pager_upload() {
		pager.setAdapter(new MyPagerAdapter_MAIN(main_view));
	}

	// 快速檢傷
	private void view_0() {
		bg_emrc = (ImageView) main_view.get(0).findViewById(R.id.print_background);
		print_No = (TextView) main_view.get(0).findViewById(R.id.main_no_print);
		print_item_0 = (TextView) main_view.get(0).findViewById(R.id.print_tv_0_green);
		print_item_1 = (TextView) main_view.get(0).findViewById(R.id.print_tv_1_black);
		print_item_2 = (TextView) main_view.get(0).findViewById(R.id.print_tv_2_red);
		print_item_3 = (TextView) main_view.get(0).findViewById(R.id.print_tv_3_red);
		print_item_4 = (TextView) main_view.get(0).findViewById(R.id.print_tv_4_red);
		print_item_5 = (TextView) main_view.get(0).findViewById(R.id.print_tv_5_red);
		print_item_6 = (TextView) main_view.get(0).findViewById(R.id.print_tv_6_gold);
		incident_spinner = (Spinner) main_view.get(0).findViewById(R.id.incident_spinner);
		menu_offline1 = (TextView) main_view.get(0).findViewById(R.id.menu_offline1);
		EMT_txt = (TextView) main_view.get(0).findViewById(R.id.name);
		
		EMT_txt.setOnLongClickListener(this);
		print_item_0.setOnClickListener(this);
		print_item_1.setOnClickListener(this);
		print_item_2.setOnClickListener(this);
		print_item_3.setOnClickListener(this);
		print_item_4.setOnClickListener(this);
		print_item_5.setOnClickListener(this);
		print_item_6.setOnClickListener(this);
		incident_spinner.setOnItemSelectedListener(this);

		if (!this.getResources().getConfiguration().locale.getCountry().equals("TW")) {
			bg_emrc.setImageResource(R.drawable.bg_emrc);
			// Web = "http://120.119.155.80/en/";
			EN = true;
		}

		if (textSize > 60) {
			textSize /= 1.6;
			print_item_0.setBackgroundColor(this.getResources().getColor(R.color.black_gone));
			print_item_1.setBackgroundColor(this.getResources().getColor(R.color.black_gone));
			print_item_2.setBackgroundColor(this.getResources().getColor(R.color.black_gone));
			print_item_3.setBackgroundColor(this.getResources().getColor(R.color.black_gone));
			print_item_4.setBackgroundColor(this.getResources().getColor(R.color.black_gone));
			print_item_5.setBackgroundColor(this.getResources().getColor(R.color.black_gone));
			print_item_6.setBackgroundColor(this.getResources().getColor(R.color.black_gone));
		}
	}

	// 檢傷卡
	@SuppressLint("InflateParams")
	private void view_1() {
		menu_No = (TextView) main_view.get(1).findViewById(R.id.main_no_menu);
		menu_list = (ListView) main_view.get(1).findViewById(R.id.menulist);
		menu_ok = (ImageView) main_view.get(1).findViewById(R.id.menu_ok);
		menu_re = (ImageView) main_view.get(1).findViewById(R.id.menu_re);
		menu_offline = (TextView) main_view.get(1).findViewById(R.id.menu_offline);
		spinner = (Spinner) main_view.get(1).findViewById(R.id.menu_spinner);
		menu_item.add(getString(R.string.s_Menu_1)); // information
		menu_item.add(getString(R.string.s_Menu_2)); // emergency
		menu_item.add(getString(R.string.s_Menu_3)); // injured
		menu_item.add(getString(R.string.s_Menu_4)); // vital signs
		menu_item.add(getString(R.string.s_Menu_5)); // level
		menu_item.add(getString(R.string.s_Menu_6)); // photo evidence

		speak = (ImageView) main_view.get(1).findViewById(R.id.speak);

		speak.setOnClickListener(this);

		load_HOSP_DATA();

		menu_upload(this);
		menu_list.setOnItemClickListener(this);
		menu_ok.setOnClickListener(this);
		menu_re.setOnClickListener(this);
		if (Treatment_unit.size() == 0) {
			Treatment_unit.add(new hospital(getString(R.string.s_H), 0, "", "")); // 收治單位
			// toast(Treatment_unit.size() + "a", this);
		} else {
			// toast(Treatment_unit.size() + "b", this);
		}
		spinner.setOnItemSelectedListener(this);
		spinner.setAdapter(new MySpinnerAdapter_MAIN(this, Treatment_unit));
	}

	@SuppressLint("InflateParams")
	public static void menu_upload(Context con) {
		menu_view.clear();
		for (int i = 0; i < menu_item.size(); i++) {
			setMyMENUAdapter(i, con);
		}
		menu_list.setAdapter(new MyListAdapter(menu_view));
	}

	@SuppressLint("InflateParams")
	private static void setMyMENUAdapter(int f, Context con) {
		View vi = LayoutInflater.from(con).inflate(R.layout.style_textview, null);
		TextView textview = (TextView) vi.findViewById(R.id.textview);
		String msg = menu_item.get(f);
		textview.setText(msg);
		setViewColor(textview, leve_count);
		menu_view.add(vi);
	}

	// 傷患紀錄與清單
	private void view_2() {
		medi_unit = (TextView) main_view.get(2).findViewById(R.id.medi_unit);
		medi_qunt = (TextView) main_view.get(2).findViewById(R.id.medi_qunt);
		medi_list = (ListView) main_view.get(2).findViewById(R.id.medi_list);
		medi_swap = (ImageView) main_view.get(2).findViewById(R.id.medi_swap);
		medi_clear = (ImageView) main_view.get(2).findViewById(R.id.medi_clear);
		inc_txt = (TextView) main_view.get(2).findViewById(R.id.inc_txt);
		medi_unit.setText(getString(R.string.s_List_0));



		load_MEDI_DATA(); // load medi_item
		medi_clear.setOnClickListener(this);
		medi_list.setOnItemClickListener(this);
		medi_list.setOnItemLongClickListener(this);
		medi_swap.setOnClickListener(this);
		user_upload();
	}

	private void lead_upload() {
		try {
			medi_list.setAdapter(new MyListAdapter(this, true, lead_item));
			medi_qunt.setText(lead_item.size() - 1 + getString(R.string.s_List_5));
		} catch (Exception e) {
			Log.e("SSL.ERROR", e.toString());
		}
	}

	//排序 R-Y-G-B
	private void aaaaa(){
		ArrayList<String> r_list = new ArrayList<String>();
		ArrayList<String> y_list = new ArrayList<String>();
		ArrayList<String> g_list = new ArrayList<String>();
		ArrayList<String> b_list = new ArrayList<String>();
		for (int i=0;i<medi_item.size();i++){
			String mmsg = medi_item.get(i);
			int r = 0;
			String number = MainActivity.getLine(mmsg, r, '/');
			r += number.length() + 3;
			String name = MainActivity.getLine(mmsg, r, '/');
			r += name.length() + 1;
			String age = MainActivity.getLine(mmsg, r, '/');
			r += age.length() + 1;
			String other = MainActivity.getLine(mmsg,r, '║');
			r += other.length() + 2;
			String inju_front = MainActivity.getLine(mmsg,r, '║');
			r += inju_front.length() + 1;
			String inju_back = MainActivity.getLine(mmsg,r, '║');
			r += inju_back.length() + 1;
			String vita_date = MainActivity.getLine(mmsg,r, '║');
			r += vita_date.length() + 1;
			int leve_count = Integer.parseInt(MainActivity.getLine(mmsg,r, '/'));
			switch (leve_count+""){
				case "0":
					b_list.add(mmsg);
					break;
				case "1":
					r_list.add(mmsg);
					break;
				case "2":
					y_list.add(mmsg);
					break;
				case "3":
					g_list.add(mmsg);
					break;
			}
		}
		medi_item.clear();
		for (int i=0;i<b_list.size();i++){
			medi_item.add(b_list.get(i));
		}
		for (int i=0;i<g_list.size();i++){
			medi_item.add(g_list.get(i));
		}
		for (int i=0;i<y_list.size();i++){
			medi_item.add(y_list.get(i));
		}
		for (int i=0;i<r_list.size();i++){
			medi_item.add(r_list.get(i));
		}

	}

	private void user_upload() {
		dblist = false;
		try {
			aaaaa();
			medi_list.setAdapter(new MyListAdapter(this, false, medi_item));
			medi_qunt.setText(medi_item.size() + getString(R.string.s_List_5));
		} catch (Exception e) {
			Log.e("SSL.ERROR", e.toString());
		}
	}

	public void onPageScrollStateChanged(int arg0) {

	}

	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	public void onPageSelected(int arg0) {
		if (nfc) {
			if (user) {
				switch (arg0) {
				case 0:
					main_left.setVisibility(View.GONE);
					main_right.setVisibility(View.VISIBLE);
					break;
				case 1:
					main_left.setVisibility(View.VISIBLE);
					main_right.setVisibility(View.VISIBLE);
					break;
				case 2:
					main_left.setVisibility(View.VISIBLE);
					main_right.setVisibility(View.GONE);
					break;
				default:
					break;
				}
			} else {
				switch (arg0) {
				case 0:
					main_left.setVisibility(View.GONE);
					main_right.setVisibility(View.VISIBLE);
					break;
				case 1:
					main_left.setVisibility(View.VISIBLE);
					main_right.setVisibility(View.VISIBLE);
					break;
				case 2:
					main_left.setVisibility(View.GONE);
					main_right.setVisibility(View.GONE);
					break;
				default:
					break;
				}
			}
		} else {
			main_left.setVisibility(View.GONE);
			main_right.setVisibility(View.GONE);
		}
	}

	private void setTARGET() {
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		inju_target.add(0.49);// 頭.0
		inju_target.add(0.2);
		inju_target.add(0.49);// 胸部.2
		inju_target.add(0.33);
		inju_target.add(0.49);// 腹部.4
		inju_target.add(0.4245);
		inju_target.add(0.575);// 右大腿.8
		inju_target.add(0.55);
		inju_target.add(0.4);// 左大腿.6
		inju_target.add(0.55);
		inju_target.add(0.6);// 右小腿.12
		inju_target.add(0.7);
		inju_target.add(0.375);// 左小腿.10
		inju_target.add(0.7);
		inju_target.add(0.68);// 右上臂.16
		inju_target.add(0.33);
		inju_target.add(0.31);// 左上臂.14
		inju_target.add(0.33);
		inju_target.add(0.76);// 右前臂.20
		inju_target.add(0.38);
		inju_target.add(0.22);// 左前臂.18
		inju_target.add(0.38);
		inju_target.add(0.89);// 右手.24
		inju_target.add(0.44);
		inju_target.add(0.1);// 左手.22
		inju_target.add(0.43);
	}

	private void setNFC() {
		// 取得該設備預設的無線感應裝置
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter == null) {
			toast(getString(R.string.s_ts_9), this);
			Loding(true);
		} else {
			nfc = true;
			// 註冊讓該Activity負責處理所有接收到的NFC Intents。
			gNfcPendingIntent = PendingIntent.getActivity(this, 0,
					// 指定該Activity為應用程式中的最上層Activity
					new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			// 建立要處理的Intent Filter負責處理來自Tag或p2p交換的資料。
			IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
			try {
				ndefDetected.addDataType("text/plain");
			} catch (MalformedMimeTypeException e) {
			}
			gNdefExchangeFilters = new IntentFilter[] { ndefDetected };
		}
	}

	private void addNFC() {
		upup();
		if (link) {			
			out("NFC/" + setNFCString());
			toast("已上傳",this);						
		}
		if (nfc) {
			// 先停止接收任何的Intent，準備寫入資料至tag；
			disableNdefExchangeMode();
			// 啟動寫入Tag模式，監測是否有Tag進入
			enableTagWriteMode();
			// Create LinearLayout Dynamically
			LinearLayout layout = new LinearLayout(this);
			// Setup Layout Attributes
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(params);
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.addView(new mTextview(this, getString(R.string.s_ts_40)));
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(layout).setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					// 在取消模式下，先關閉監偵有Tag準備寫入的模式，再啟動等待資料交換的模式。
					// 停止寫入Tag模式，代表已有Tag進入
					disableTagWriteMode();
					// 啟動資料交換
					enableNdefExchangeMode();
				}
			});
			// 顯示對話框，告知將Tag或手機靠近本機的NFC感應區
			alertDialog = builder.create();
			alertDialog.show();
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if(v == EMT_txt){
			//檢傷人員輸入
			final AlertDialog.Builder editDialog = new AlertDialog.Builder(MainActivity.this);
			editDialog.setTitle("檢傷人員");
			final EditText editText = new EditText(MainActivity.this);
			editDialog.setView(editText);
			editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					if(editText.getText().length()>0){
						EMT_txt.setText(editText.getText().toString());
//						EMT = editText.getText().toString();
					}
				}
			});
			editDialog.setCancelable(false);
			editDialog.show();
		}
		return false;
	}

	private class mTextview extends TextView {
		public mTextview(Context context, final String str) {
			super(context);
			this.setText(str);
			this.setTextSize(35);
			this.setTextColor(Color.BLACK);
			this.setGravity(Gravity.CENTER);
			this.setPadding(20, 20, 20, 20);
		}
	}

	private void checkItem(String msg) {
		boolean run = false;
		if (msg.indexOf('|') > 0) {
			String nb = getLine(msg, 0, '/');
			if (nb.length() != 0) {
				// 有流水號比對方法
				for (int i = 0; i < medi_item.size(); i++) {
					String mmsg = medi_item.get(i);
					// 流水號
					if (nb.equals(getLine(mmsg, 0, '/'))) {
						// 流水號相同取代資料
						medi_item.remove(i);
						medi_item.add(msg);
						run = true;
						break;
					}
				}
				if (!run) {
					medi_item.add(msg);
				}
			} else {
				// 沒有流水號比對方法
				medi_item.add(msg);
			}
		}
	}

	public void onClick(View v) {

		if(v == speak){

			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請說～");
			try{
				startActivityForResult(intent,200);
			}catch (ActivityNotFoundException a){
//				Toast.makeText(getApplicationContext(),"Intent problem", Toast.LENGTH_SHORT).show();
			}
//			speak("姓名陳一二年 齡33性別女身 分證A2 23456789血壓12555呼吸22心跳100");
		}

		if (v == main_log) {
			dev_count--;
			if (dev_count == 10)
				toast("您只需完成剩餘的三個步驟，即可成為開發人員。", this);
			if (dev_count == 5)
				toast("您只需完成剩餘的二個步驟，即可成為開發人員。", this);
			if (dev_count == 1)
				toast("您只需完成剩餘的一個步驟，即可成為開發人員。", this);
			if (dev_count == 0) {
				toast("您現在已成為開發人員。", this);
				main_log.setVisibility(View.GONE);
				dev = true;
			}
		}
		if (v == medi_clear) {
			new AlertDialog.Builder(this).setTitle(getString(R.string.s_ts_11))
					.setPositiveButton(getString(R.string.s_ts_12), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
						}
					}).setNegativeButton(getString(R.string.s_ts_13), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							medi_item.clear();
							getFile("DATA.txt", "");
							medi_clear.setVisibility(View.INVISIBLE);
							user_upload();
						}
					}).show();
		}
		if (v == medi_swap) {
			if (link) {
				if (!dblist) {
					// Patients List
					
//					medi_swap.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.fs_database));
//					medi_unit.setText(getString(R.string.s_List_2));
//					medi_clear.setVisibility(View.INVISIBLE);
//					out("cc2/"+inc_code);
////					out("c2");
//					inc_txt.setVisibility(View.VISIBLE);
//					if (lead_item.size() > 0) {
//						lead_upload();
//					} else {
//						medi_qunt.setText("");
//					}
//					dblist = true;
					
					if(inc_code!=0){
						medi_swap.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.fs_database));
						medi_unit.setText(getString(R.string.s_List_2));
						medi_clear.setVisibility(View.INVISIBLE);
						out("cc2/"+inc_code);
//						inc_txt.setVisibility(View.VISIBLE);
						if (lead_item.size() > 0) {
							lead_upload();
						} else {
							medi_qunt.setText("");
						}
						dblist = true;
					}
					else{
						toast("請選擇事件",this);
					}
					
				} else {
					// Patients Record
					medi_swap.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.fs_folder));
					medi_unit.setText(getString(R.string.s_List_0));
//					inc_txt.setVisibility(View.GONE);
					if (medi_item.size() > 0) {
						medi_clear.setVisibility(View.VISIBLE);
					} else {
						medi_clear.setVisibility(View.INVISIBLE);
					}
					user_upload();
				}
			} else {
				toast(getString(R.string.s_ts_50), this);
			}
		}

		if (v == menu_ok) {
			// 保存NFC至檢傷紀錄
			if(inc_code==0){
				toast("請選擇事件",this);
			}else{
				if (leve_count != 5) {
					USHandler.obtainMessage().sendToTarget();
					addNFC();
					
				} else {
					toast("No injury level set!", this);
				}
			}
			
		}
		if (v == menu_re) {
			user_upload();
			getReset(true);
			toast(getResources().getString(R.string.s_ts_60), this);
			pager.setCurrentItem(0);
			// 掃QRcode
			//startActivityForResult(new Intent(MainActivity.this, CaptureActivity.class), 1);
		}
		if (v == print_item_0) {
			if(inc_code==0){
//				toast("Please select an event",this);
				toast("請選擇事件",this);
			}else{
				setEMRC(3, 0);
			}			
		}
		if (v == print_item_1) {
			if(inc_code==0){
//				toast("Please select an event",this);
				toast("請選擇事件",this);
			}else{
				setEMRC(0, 0);
			}			
		}
		if (v == print_item_2) {
			if(inc_code==0){
//				toast("Please select an event",this);
				toast("請選擇事件",this);
			}else{
				setEMRC(1, 1);
			}			
		}
		if (v == print_item_3) {
			if(inc_code==0){
//				toast("Please select an event",this);
				toast("請選擇事件",this);
			}else{
				setEMRC(1, 2);
			}			
		}
		if (v == print_item_4) {
			if(inc_code==0){
//				toast("Please select an event",this);
				toast("請選擇事件",this);
			}else{
				setEMRC(1, 512);
			}			
		}
		if (v == print_item_5) {
			if(inc_code==0){
//				toast("Please select an event",this);
				toast("請選擇事件",this);
			}else{
				setEMRC(1, 8);
			}			
		}
		if (v == print_item_6) {
			if(inc_code==0){
//				toast("Please select an event",this);
				toast("請選擇事件",this);
			}else{
				setEMRC(2, 0);
			}			
		}
	}

	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
		// parent = 事件發生的母體 spinner_items
		// position = 被選擇的項目index = parent.getSelectedItemPosition()
		if (parent == spinner){
			if (position != 0) {
				String Treatment = parent.getSelectedItem().toString();
				if (!user) {
					Toast.makeText(this, getString(R.string.s_ts_18) + Treatment, Toast.LENGTH_SHORT).show();
				}
			}
			hosp_count = Treatment_unit.get(position).area;
		}
		if (parent == incident_spinner && position !=0){
			inc_code = Integer.parseInt(incident_code.get(position));
			inc_txt.setText("事件名稱："+incident_unit.get(position));
			out("cc2/"+inc_code);
			// Patients Record
			medi_swap.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.fs_folder));
			medi_unit.setText(getString(R.string.s_List_0));
//			inc_txt.setVisibility(View.GONE);
			if (medi_item.size() > 0) {
				medi_clear.setVisibility(View.VISIBLE);
			} else {
				medi_clear.setVisibility(View.INVISIBLE);
			}
//			user_upload();
		}
		if (parent == incident_spinner && position ==0){
			inc_code = 0;
		}
		
		
		
		
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		Toast.makeText(this, getString(R.string.s_ts_19), Toast.LENGTH_LONG).show();
	}

	private void setEMRC(int c, int e) {
		leve_count = c;
		emrc_count = e;
		// 傷患等級大於輕傷
		if (c != 3) {
			startActivity(new Intent(MainActivity.this, EMRCActivity.class));
		} else {
			uploadview(this);
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (parent == menu_list) {
			start(position);
		}
		if (parent == medi_list) {
			String pms = "";
			if (dblist) {
				if (position != 0)
					pms = lead_item.get(position);
			} else {
				pms = (String) parent.getItemAtPosition(position);
			}
			if (pms.length() != 0) {
				final String mmsg = pms;
				// 是否要取得傷患資訊(DB)
				new AlertDialog.Builder(this).setTitle(getString(R.string.s_ts_20))
						.setPositiveButton(getString(R.string.s_ts_12), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
							}
						}).setNegativeButton(getString(R.string.s_ts_13), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
								getNFCString(mmsg);
								if (dblist){
									checkItem(setNFCString());
								}
								toast(info_age+"",MainActivity.this);
							}
						}).show();
			}
		}
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
		if (!dblist) {
			new AlertDialog.Builder(this).setTitle(getString(R.string.s_ts_11))
					.setPositiveButton(getString(R.string.s_ts_12), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
						}
					}).setNegativeButton(getString(R.string.s_ts_13), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							medi_item.remove(medi_item.size()-1-position);
							user_upload();
							if (user) {
								if (medi_item.size() == 0) {
									getFile("DATA.txt", "");
									medi_clear.setVisibility(View.INVISIBLE);
								}
							}
						}
					}).show();
		}
		return true;
	}

	private void disableNdefExchangeMode() {
		nfcAdapter.disableForegroundNdefPush(this);
		nfcAdapter.disableForegroundDispatch(this);
	}

	private void enableTagWriteMode() {
		write = true;
		IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		gWriteTagFilters = new IntentFilter[] { tagDetected };
		nfcAdapter.enableForegroundDispatch(this, gNfcPendingIntent, gWriteTagFilters, null);
	}

	private void disableTagWriteMode() {
		write = false;
		nfcAdapter.disableForegroundDispatch(this);
	}

	private void enableNdefExchangeMode() {
		// 讓NfcAdatper啟動前景Push資料至Tag或應用程式。
		nfcAdapter.enableForegroundNdefPush(this, getNoteAsNdef());

		// 讓NfcAdapter啟動能夠在前景模式下進行intent filter的dispatch。
		nfcAdapter.enableForegroundDispatch(this, gNfcPendingIntent, gNdefExchangeFilters, null);
	}

	private NdefMessage getNoteAsNdef() {
		// 啟動Ndef交換資料模式。
		String msg = setNFCString();
		msg = Encrypt(msg);
		byte[] textBytes = msg.getBytes();
		NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(), new byte[] {},
				textBytes);
		return new NdefMessage(new NdefRecord[] { textRecord });
	}
	private void upup(){
		if(hosp_count.equals("")){
			
			if(hosp_count.equals("") && MH == 0){
				MH = 1;// 無醫院 ，1次
			}else{
				MH = -1;// 無醫院 ，多次
			}
		}else {
			if(!hosp_count.equals(h) && MH == 0){ //有醫院，1次
				h = hosp_count; //目前有醫院
				MH = 2; 
			}else if(h.equals("") && !hosp_count.equals(h)){ // 無醫院 到 有醫院，1次
				h = hosp_count;
				if(MH == 1 || MH == -1){
					MH = 3;
				}else{
					MH = -1;
				} 
			}else{
				MH = -1; //不管變更 都是 多次
			}
		}
	}

	private void getNFCString(String tag) {
		if (tag.indexOf('|') != -1) {
			getReset(false);
			int tmp = 0;
			String msg = tag;
			String numbers = msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('/'));
			tmp += numbers.length() + 1;
			number = numbers;
			int genders = Integer.parseInt(msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('/')));
			tmp += 2;
			gender = genders;
			String info = msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('║'));
			tmp += info.length() + 1;
			Tools.getListString(info, info_data, 3);
			String inju_f = msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('║'));
			tmp += inju_f.length() + 1;
			Tools.getListString(inju_f, inju_front, 0);
			String inju_b = msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('║'));
			tmp += inju_b.length() + 1;
			Tools.getListString(inju_b, inju_back, 0);
			String vita = msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('║'));
			tmp += vita.length() + 1;
			Tools.getListString(vita, vita_data, 18);
			int leve = Integer.parseInt(msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('/')));
			tmp += 2;
			leve_count = leve;
			int emrc = Integer.parseInt(msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('/')));
			tmp += (emrc + "").length() + 1;
			emrc_count = emrc;
			String hosp = msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('/'));
			tmp += (hosp + "").length() + 1;
			hosp_count = hosp;
			if (msg.substring(tmp).indexOf('/') > 0) {
				String iden = msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('/'));
				identity = iden;
				tmp += (iden + "").length();
			}
			tmp++;
			if (msg.substring(tmp).indexOf('/') > 0) {
				String inc = msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('/'));// 事件
				tmp += (inc + "").length();
				inc_code = Integer.parseInt(inc);
			}
			tmp++;
			if (msg.substring(tmp).indexOf('/') > 0) {
				String age = msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('/'));// 年齡
				tmp += (age + "").length();
				info_age = age;
				Log.e("age = ", info_age);
			}
			tmp++;
			if (msg.substring(tmp).indexOf('/') > 0) {
				String mh = msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('/'));// MH  
				tmp += (mh + "").length();
				MH = Integer.parseInt(mh);
				Log.e("MH = ", mh+"");
			}
			tmp++;
			if (msg.substring(tmp).indexOf('/') > 0) {
				String QRC = msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('/'));// QRcode  
				tmp += (QRC + "").length();
				QRcode = QRC;
				Log.e("QRC = ", QRC+"");
			}
			tmp++;
//			if (msg.substring(tmp).indexOf('/') > 0) {
//				String name = msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('/'));// 檢傷人員
//				tmp += (name + "").length();
//				EMT = name;
//				Log.e("NAME = ", name+"");
//			}
//			tmp++;
			if (msg.substring(tmp).indexOf('║') > 0) {
				String ien = msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('║'));
				emrgn = ien;
				// toast(emrgn, this);
			}
			uploadview(this);
			if (link) {
				if (numbers.length() < 2) {
					// 無流水號
					out("a0");
					// 上傳資料
					UPHandler.obtainMessage().sendToTarget();
				} else {
					// 搜尋照片
					out(number);
				}
			}
			if (!user && link) {
				out("c0");
				if (hosp_count.equals("")) {
					out("c3");
				}
			}
		} else {
			toast(getString(R.string.s_ts_0), this);
		}
		// user_name = null;
	}

	public static void uploadview(Context con) {

//		EMT_txt.setText(EMT);

		if (number.length() > 2) {
			print_No.setText(number);
			menu_No.setText(number);
		} else {
			print_No.setText(con.getResources().getString(R.string.id_000004));
			menu_No.setText(con.getResources().getString(R.string.id_000004));
		}
		if (info_data.size() > 2) {
			String name = info_data.get(0);
			if (name.length() == 0) {
				name = con.getResources().getString(R.string.info_unknow);
			}
			switch (gender) {
			case 0:
				name += "," + con.getResources().getString(R.string.info_femal);
				break;
			case 1:
				name += "," + con.getResources().getString(R.string.info_male);
				break;
			default:
				break;
			}
			menu_item.set(0, con.getResources().getString(R.string.s_Menu_1) + "\n<" + name + ">");
		} else {
			menu_item.set(0, con.getResources().getString(R.string.s_Menu_1));
		}
		if (inju_front.size() > 0 || inju_back.size() > 0) {
			String title = "";
			if (inju_front.size() > 0) {
				for (int i = 0; i < inju_front.size(); i += 2) {
					Double x = inju_front.get(i), y = inju_front.get(i + 1);
					title += Tools.getInjury(con, x, y, 0);
				}
			}
			if (inju_back.size() > 0) {
				for (int i = 0; i < inju_back.size(); i += 2) {
					Double x = inju_back.get(i), y = inju_back.get(i + 1);
					title += Tools.getInjury(con, x, y, 1);
				}
			}
			if (title.length() > 1) {
				menu_item.set(2, con.getResources().getString(R.string.s_Menu_3) + "\n<" + title + ">");
			} else {
				menu_item.set(2, con.getResources().getString(R.string.s_Menu_3));
			}
		} else {
			menu_item.set(2, con.getResources().getString(R.string.s_Menu_3));
		}
		if (vita_data.size() > 0) {
			for (int i = 0; i < 3; i++) {
				String j = vita_data.get(i * 6);
				if (j.length() > 1) {
					menu_item.set(3, con.getResources().getString(R.string.s_Menu_4) + "\n<" + j + ">");
				}
			}
		} else {
			menu_item.set(3, con.getResources().getString(R.string.s_Menu_4));
		}
		if (leve_count == 5) {
			// String intArray[] = { "死亡", "極危險", "危險", "輕傷" };
			// menu_item.set(4, "檢傷分類 <" + intArray[leve_count] + ">");
			// } else {
		}
		if (phot_count > 0) {
			int cc = phot_count, set = 0;
			while (cc != 0) {
				if (cc - 32 >= 0) {
					cc -= 32;
					set++;
				} else if (cc - 16 >= 0) {
					cc -= 16;
					set++;
				} else if (cc - 8 >= 0) {
					cc -= 8;
					set++;
				} else if (cc - 4 >= 0) {
					cc -= 4;
					set++;
				} else if (cc - 2 >= 0) {
					cc -= 2;
					set++;
				} else {
					cc--;
					set++;
				}
			}
			menu_item.set(5,
					con.getResources().getString(R.string.s_Menu_6) + "\n<"
							+ con.getResources().getString(R.string.s_List_3) + set
							+ con.getResources().getString(R.string.s_List_4) + ">");
		} else {
			menu_item.set(5, con.getResources().getString(R.string.s_Menu_6));
		}
		if (emrc_count != 0) {
			if (menu_item.size() == 6) {
				menu_item.add(getEMRC(con, emrc_count, leve_count));
			} else {
				menu_item.set(6, getEMRC(con, emrc_count, leve_count));
			}
		} else {
			if (menu_item.size() == 7) {
				menu_item.remove(6);
			}
		}
		spinner.setSelection(getTrainsportSelection(hosp_count), false);


		menu_upload(con);
		if (user) {
			pager.setCurrentItem(1);
		} else {
			pager.setCurrentItem(0);
		}
		// if (c < 6) {
		// menu_list.setSelection(c);
		// }
	}

	public static int getTrainsportSelection(String area) {
		int s = 0;
		for (int i = 0; i < Treatment_unit.size(); i++) {
			if (Treatment_unit.get(i).area.equals(area)) {
				s = i;
				break;
			}
		}
		return s;
	}

	public static String getEMRC(Context c, int count, int leve) {
		String msg = "";
		ArrayList<String> list_string = new ArrayList<String>();
		switch (leve) {
		case 0:
			list_string.add(c.getResources().getString(R.string.s_EMRC_00));
			list_string.add(c.getResources().getString(R.string.s_EMRC_01));
			list_string.add(c.getResources().getString(R.string.s_EMRC_02));
			list_string.add(c.getResources().getString(R.string.s_EMRC_03));
			list_string.add(c.getResources().getString(R.string.s_EMRC_04));
			break;
		case 1:
			list_string.add(c.getResources().getString(R.string.s_EMRC_10));
			list_string.add(c.getResources().getString(R.string.s_EMRC_11));
			list_string.add(c.getResources().getString(R.string.s_EMRC_12));
			list_string.add(c.getResources().getString(R.string.s_EMRC_13));
			list_string.add(c.getResources().getString(R.string.s_EMRC_14));
			list_string.add(c.getResources().getString(R.string.s_EMRC_15));
			list_string.add(c.getResources().getString(R.string.s_EMRC_16));
			list_string.add(c.getResources().getString(R.string.s_EMRC_17));
			list_string.add(c.getResources().getString(R.string.s_EMRC_18));
			list_string.add(c.getResources().getString(R.string.s_EMRC_19));
			break;
		case 2:
			list_string.add(c.getResources().getString(R.string.s_EMRC_20));
			list_string.add(c.getResources().getString(R.string.s_EMRC_21));
			list_string.add(c.getResources().getString(R.string.s_EMRC_22));
			list_string.add(c.getResources().getString(R.string.s_EMRC_23));
			list_string.add(c.getResources().getString(R.string.s_EMRC_24));
			list_string.add(c.getResources().getString(R.string.s_EMRC_25));
			list_string.add(c.getResources().getString(R.string.s_EMRC_26));
			break;
		}
		int getcheck = count;
		for (int i = 9; i >= 0; i--) {
			if (getcheck >= (1 << i)) {
				getcheck -= (1 << i);
				if (msg.length() < 1) {
					msg += (list_string.get(i));
				} else {
					msg += ("\n" + list_string.get(i));
				}
			}
		}
		return msg;
	}

	private static void setViewColor(TextView view, int count) {
		switch (count) {
		case 0:
			view.setTextColor(Color.WHITE);
			menu_list.setBackgroundColor(Color.BLACK);
			break;
		case 1:
			view.setTextColor(Color.WHITE);
			menu_list.setBackgroundColor(Color.RED);
			break;
		case 2:
			view.setTextColor(Color.BLACK);
			menu_list.setBackgroundColor(Color.YELLOW);
			break;
		case 3:
			view.setTextColor(Color.BLACK);
			menu_list.setBackgroundColor(Color.GREEN);
			break;
		default:
			view.setTextColor(Color.BLACK);
			menu_list.setBackgroundColor(Color.WHITE);
			break;
		}
	}

	private void start(int item) {
		switch (item) {
		case 0:
			startActivity(new Intent(this, InformationActivity.class));
			break;
		case 1:
			startActivity(new Intent(this, EmrgnActivity.class));
			break;
		case 2:
			startActivity(new Intent(this, InjuredActivity.class));
			break;
		case 3:
			startActivity(new Intent(this, VitalActivity.class));
			break;
		case 4:
			startActivity(new Intent(this, LevelActivity.class));
			break;
		case 5:
			startActivity(new Intent(this, PhotoActivity.class));
			break;
		case 6:
			startActivity(new Intent(this, EMRCActivity.class));
			break;
		}
	}

	private void getReset(boolean a0) {
		number = "";
		gender = 2;
		identity = "";
		emrgn = "";
		leve_count = 5;
		phot_count = 0;
		emrc_count = 0;
		hosp_count = "";
		status_count = 6;
		info_photo = null;
		info_data.clear();
		inju_front.clear();
		inju_back.clear();
		vita_data.clear();
		phot_bitmap.clear();
		h="";
		MH=0;
		QRcode = "";
		if (a0) {
			menu_item.clear();
			menu_item.add(getString(R.string.s_Menu_1)); // information
			menu_item.add(getString(R.string.s_Menu_2)); // emergency
			menu_item.add(getString(R.string.s_Menu_3)); // injured
			menu_item.add(getString(R.string.s_Menu_4)); // vital signs
			menu_item.add(getString(R.string.s_Menu_5)); // level
			menu_item.add(getString(R.string.s_Menu_6)); // photo
			menu_upload(this);
			spinner.setSelection(getTrainsportSelection(hosp_count), false);
			if (net && link) {
				out("a0");
			} else {
				print_No.setText(getString(R.string.id_000004));
				menu_No.setText(getString(R.string.id_000004));
			}
		}
	}

	@SuppressLint("TrulyRandom")
	private String Encrypt(String sSrc) {
		String s = "";
		try {
			byte[] raw = "AIzaSyCpvaLBjUPz".getBytes();
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES");// "算法/模式/补码方式"
			IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			s = Base64.encodeToString(cipher.doFinal(sSrc.getBytes()), android.util.Base64.NO_WRAP);
		} catch (Exception e) {
		}
		return s;
	}

	private boolean checkData(String sSrc) {
		if (sSrc.indexOf('|') != -1) {
			// Normal data
			return false;
		} else {
			// Encrypted data
			return true;
		}
	}

	private String Decrypt(String sSrc) {
		String s = "";
		try {
			byte[] raw = "AIzaSyCpvaLBjUPz".getBytes();
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			s = new String(cipher.doFinal(Base64.decode(sSrc, android.util.Base64.NO_WRAP)));
		} catch (Exception e) {
		}
		return s;
	}
	
	//QRcode_解密
	private String QRcode_Decrypt(String sSrc) {
		String s = "";
		try {
			byte[] raw = "AIzaSyCpvaLBjUPz".getBytes("ASCII");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			s = new String(cipher.doFinal(Base64.decode(sSrc, android.util.Base64.NO_WRAP)));
		} catch (Exception e) {
		}
		return s;
	}

	private String setNFCString() {
		return number + '/' + gender + '/' + Tools.getList(info_data, true) + '║' + inju_front.size() + '='
				+ Tools.getList(inju_front, false) + '║' + inju_back.size() + '=' + Tools.getList(inju_back, false)
				+ '║' + Tools.getList(vita_data, false) + '║' + leve_count + '/' + emrc_count + '/' + hosp_count + '/'
				+ identity +"/"+inc_code+ "/" +info_age + "/" + MH + "/"+ QRcode + "/" + emrgn + "║|";


//		return number + '/' + gender + '/' + Tools.getList(info_data, true) + '║' + inju_front.size() + '='
//				+ Tools.getList(inju_front, false) + '║' + inju_back.size() + '=' + Tools.getList(inju_back, false)
//				+ '║' + Tools.getList(vita_data, false) + '║' + leve_count + '/' + emrc_count + '/' + hosp_count + '/'
//				+ identity +"/"+inc_code+ "/" +info_age + "/" + MH + "/"+ QRcode + "/" + EMT +"/"+ emrgn + "║|";
	}

	private boolean writeTag(NdefMessage message, Tag tag) {

		int size = message.toByteArray().length;
		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();

				if (!ndef.isWritable()) {
					// 標籤是只讀的
					toast(getString(R.string.s_ts_21), this);
					nfcClose(false);
					return false;
				}
				if (ndef.getMaxSize() < size) {
					// 標籤容量ndef.getMaxSize() bytes,訊息是size bytes
					toast(getString(R.string.s_ts_22), this);
					toast(getString(R.string.s_ts_23) + ndef.getMaxSize() + " bytes, " + getString(R.string.s_ts_24)
							+ size + " bytes.", this);
					nfcClose(false);
					return false;
				}

				ndef.writeNdefMessage(message);
				// 將消息寫入預格式化的標籤
				toast(getString(R.string.s_ts_25), this);
				nfcClose(true);
				return true;
			} else {
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
						// 格式化標籤和寫入資料
						toast(getString(R.string.s_ts_25), this);
						nfcClose(true);
						return true;
					} catch (IOException e) {
						// 無法格式化標籤
						toast(getString(R.string.s_ts_26), this);
						nfcClose(false);
						return false;
					}
				} else {
					// 標籤不支持NDEF
					toast(getString(R.string.s_ts_27), this);
					nfcClose(false);
					return false;
				}
			}
		} catch (Exception e) {
			// 無法寫入標籤
			toast(getString(R.string.s_ts_26), this);
			nfcClose(false);
			return false;
		}
	}

	private void nfcClose(boolean tag) {
		alertDialog.cancel();
		if (!tag) {
			toast(getString(R.string.s_ts_28), this);
		} else {
			if (user) {
				// 連線後,寫入成功
				new AlertDialog.Builder(this).setTitle(getString(R.string.s_ts_29))
						.setPositiveButton(getString(R.string.s_ts_12), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {

							}
						}).setNegativeButton(getString(R.string.s_ts_13), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
								user_upload();
								getReset(true);
								pager.setCurrentItem(0);
							}
						}).show();
			}
		}
	}

	public static void toast(String text, Context con) {
		Toast.makeText(con, text + "", Toast.LENGTH_SHORT).show();
	}

	private void setNET() {
		// 取得通訊服務
		ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			// 是否有連線
			Boolean IsConnected = false;
			// 依序檢查各種網路連線方式
			for (NetworkInfo network : connectivity.getAllNetworkInfo()) {
				if (network.getState() == NetworkInfo.State.CONNECTED) {
					IsConnected = true;
					// 顯示網路連線種類
					Toast.makeText(getApplicationContext(),
							getString(R.string.s_ts_30) + network.getTypeName() +" "+ getString(R.string.s_ts_31),
							Toast.LENGTH_SHORT).show();
					net = true;
					ConnectAndSend();
				}
			}
			if (!IsConnected) {
				Toast.makeText(getApplicationContext(), getString(R.string.s_ts_32), Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(getApplicationContext(), getString(R.string.s_ts_32), Toast.LENGTH_SHORT).show();
		}
	}

	private void ConnectAndSend() {
		new Thread(new Runnable() {
			public void run() {
				try {
					// TODO TCP SSLSocket
					// 使用TLS協議
					SSLContext context = SSLContext.getInstance("TLS");

					// 服务器端需要验证的客户端证书 p12
					KeyStore keyManagers = KeyStore.getInstance("BKS");
					keyManagers.load(getResources().getAssets().open(KEY_STORE_CLIENT_PATH),
							KEY_STORE_PASSWORD.toCharArray());

					// 客户端信任的服务器端证书 bks
					KeyStore trustManagers = KeyStore.getInstance("BKS");
					trustManagers.load(getResources().getAssets().open(KEY_STORE_TRUST_PATH),
							KEY_STORE_PASSWORD.toCharArray());

					// 获得X509密钥库管理实例
					KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
					keyManagerFactory.init(keyManagers, KEY_STORE_PASSWORD.toCharArray());
					TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
					trustManagerFactory.init(trustManagers);

					// 初始化SSLContext
					context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

					// 获取SSLSocket
					skt = context.getSocketFactory().createSocket(ip, port);
					// OutputStream out = skt.getOutputStream();
					// out.write("Connection established.\n".getBytes());
					readData();

				} catch (UnrecoverableKeyException | CertificateException | NoSuchAlgorithmException | KeyStoreException
						| KeyManagementException | IOException e) {
					Log.e("SSL.ERROR", e.toString());
				}
			}
		}).start();
	}

	private void Loding(Boolean bl) {
		Button p = (Button) main_view.get(0).findViewById(R.id.print_log);
		Button m = (Button) main_view.get(1).findViewById(R.id.menu_log);
		if (nfc) {
			if (bl) {
				p.setVisibility(View.VISIBLE);
				m.setVisibility(View.VISIBLE);
				main_left.setVisibility(View.GONE);
				main_right.setVisibility(View.GONE);
			} else {
				p.setVisibility(View.GONE);
				m.setVisibility(View.GONE);
			}
		} else {
			p.setVisibility(View.VISIBLE);
			m.setVisibility(View.VISIBLE);
		}
	}

	private void DateInput(Boolean i) {
		Adler32 inChecker = new Adler32();
		CheckedDataInput in = null;
		try {
			DataInputStream dis = new DataInputStream(skt.getInputStream());
			int size = dis.readInt();
			in = new CheckedDataInput(new DataInputStream(skt.getInputStream()), inChecker);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] data = new byte[size];
			in.readFully(data);
			out.write(data);
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			bitmap.compress(CompressFormat.JPEG, 100, out);
			if (i) {
				info_photo = bitmap;
			} else {
				phot_bitmap.add(bitmap);
			}
		} catch (IOException e) {
		}
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setTitle(getString(R.string.s_ts_33)); // 设置标题
			mProgressDialog.setMessage(getString(R.string.s_ts_34) + ".."); // 设置body信息
			mProgressDialog.setMax(1); // 进度条最大值是100
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // 设置进度条样式是横向的
			return mProgressDialog;
		}
		return super.onCreateDialog(id);
	}

	public static void out(String msg) {
		if (net) {
			new MainSocketOutput(msg).start();
		}
	}

	public static String getLine(String msg, int run, char key) {
		return msg.substring(run).substring(0, msg.substring(run).indexOf(key));
	}

	private void readData() {
		// TODO readData
		// new Thread(new Runnable() {
		// public void run() {
		try {
			BufferedReader buf = new BufferedReader(new InputStreamReader(skt.getInputStream(), "UTF-8"));
			if (number.length() < 2) {
				out("a0");
			} else {
				out(number);
			}
			out("CHP/" + Treatment_unit.size());
			out("INC/");
			sktRun = true;
			RLinkHandler.obtainMessage().sendToTarget();
			String Msg;
			while ((Msg = buf.readLine()) != null && sktRun) {
				link = true;
				RLink = true;
				if (Msg.length() == 2) {
					switch (Msg) {
					case "a1":
						// 客戶端交握
						break;
					case "s1":
						out("s1");
						break;
					case "b0":
						DateInput(true);
						break;
					case "b1":
						DateInput(false);
						phot_count++;
						break;
					case "b2":
						DateInput(false);
						phot_count += 2;
						break;
					case "b3":
						DateInput(false);
						phot_count += 4;
						break;
					case "b4":
						DateInput(false);
						phot_count += 8;
						break;
					case "b5":
						DateInput(false);
						phot_count += 16;
						break;
					case "b6":
						DateInput(false);
						phot_count += 32;
						break;
					case "b7":
						PhotoHandler.obtainMessage().sendToTarget();
						break;
					case "c2":
						if (dblist) {
							out("c2");
						}
						break;
					default:
						break;
					}
				} else {
					// Msg.length() >= 3
					String tag = getLine(Msg, 0, '/');
					// 取得標籤
					switch (tag) {
					case "NO":
						String msgs = Msg.substring(3, Msg.indexOf('|'));
						number = msgs;
						NOHandler.obtainMessage().sendToTarget();
						break;
					case "C2":
						// 載入傷患清單
						String C2 = Msg.substring(3);
						if (!C2.equals("END")) {
							String NUM = getLine(C2, 0, '/');
							if (NUM.length() < 5) {
								lead_item.clear();
							}
							lead_item.add(C2);
						} else {
							C2Handler.obtainMessage().sendToTarget();
						}
						break;
					case "C4":
						// 載入醫院
						String C4 = Msg.substring(3);
						if (!C4.equals("END")) {
							if (C4.equals("CL")) {
								Treatment_unit.clear();
								Treatment_unit.add(new hospital(getString(R.string.s_H), 0, "", "")); // 收治單位
							} else {
								Object[] strs = C4.split(",");
								Treatment_unit.add(new hospital((String) strs[0], Integer.parseInt((String) strs[1]),
										(String) strs[2] + "," + (String) strs[3], (String) strs[4]));
							}
						} else {
							C4Handler.obtainMessage().sendToTarget();
						}
						break;
					case "C5":
						qr_msg = Msg.substring(3);
						C5Handler.obtainMessage().sendToTarget();
						break;
					case "C6":
						String C6 = Msg.substring(3);
						if (!C6.equals("END")) {
							if (C6.equals("CL")) {
								incident_unit.clear();
								incident_unit.add("選擇事件");
								incident_code.add("0");
							} else {
								String[] strs = C6.split(",");
								incident_unit.add(strs[1]);
								incident_code.add(strs[0]);
							}
						} else {
							C6Handler.obtainMessage().sendToTarget();
						}
						break;
					default:
						break;
					}
				}
			}
		} catch (Exception e) {
		}
	}

	private Runnable checkLink = new Runnable() {
		public void run() {
			if (RLink) {
				out("NET/");
				RLink = false;
			}else {
				menu_offline.setVisibility(View.VISIBLE);
				menu_offline1.setVisibility(View.VISIBLE);
			}
			LinkHandler.postDelayed(this, 5000);// 每5秒呼叫自己執行一次
		}
	};
	private Handler RLinkHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			LinkHandler.removeCallbacks(checkLink);// 如有運作則停止
			LinkHandler.postDelayed(checkLink, 2000);// 設定2秒後啟動
		};
	};
	
	private Handler UPHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			new Timer(true).schedule(new TimerTask() {
				public void run() {
					out("NFC/" + setNFCString());
				}
			}, 2000);
		};
	};

	private Handler C2Handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (dblist) {
				// 檢視傷患資訊時刷新
				lead_upload();
//				toast("asdfasdf",MainActivity.this);
			}
		};
	};

	private Handler C4Handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			spinner.setAdapter(new MySpinnerAdapter_MAIN(MainActivity.this, Treatment_unit));
			toast("OK", MainActivity.this);
		};
	};
	private Handler C6Handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			incident_spinner.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.style_spinner, incident_unit));
		};
	};
	private Handler C5Handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			getNFCString(qr_msg);
			checkItem(setNFCString());
		};
	};

	private Handler NOHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String msgs = (number + "");
			print_No.setText(number);
			menu_No.setText(number);
			if (msgs.length() > 6) {
				String y = 20 + msgs.substring(0, 2);
				String m = msgs.substring(2, 4);
				String d = msgs.substring(4, 6);
				int Y = Integer.parseInt(y);
				int M = Integer.parseInt(m);
				int D = Integer.parseInt(d);
				checkToday(Y, M, D, true);
			}
		};
	};

	private Handler USHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			checkItem(setNFCString());
			user_upload();
		};
	};

	private Handler PhotoHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (phot_count > 0) {
				int cc = phot_count, set = 0;
				while (cc != 0) {
					if (cc - 32 >= 0) {
						cc -= 32;
						set++;
					} else if (cc - 16 >= 0) {
						cc -= 16;
						set++;
					} else if (cc - 8 >= 0) {
						cc -= 8;
						set++;
					} else if (cc - 4 >= 0) {
						cc -= 4;
						set++;
					} else if (cc - 2 >= 0) {
						cc -= 2;
						set++;
					} else {
						cc--;
						set++;
					}
				}
				menu_item.set(5,
						MainActivity.this.getResources().getString(R.string.s_Menu_6) + "<"
								+ MainActivity.this.getResources().getString(R.string.s_List_3) + set
								+ MainActivity.this.getResources().getString(R.string.s_List_4) + ">");
				menu_upload(MainActivity.this);
			} else {
				menu_item.set(5, MainActivity.this.getResources().getString(R.string.s_Menu_6));
				menu_upload(MainActivity.this);
			}
		};
	};


	/*
	public void speak (String s){
		Log.e("string",s);
		String[] split_line = s.split(" ");
		String show_split_line = "";
		for (String ss: split_line) {
			show_split_line = show_split_line + ss;
		}
		Log.e("string",show_split_line);
		int count=0;
		speak_s="|"+show_split_line+"|";
		speak_str.clear();
		speak_num.clear();


		if(speak_s.indexOf("姓名")>0){
			speak_str.add("姓名");
			speak_num.add(speak_s.indexOf("姓名"));
		}
		if(speak_s.indexOf("年齡")>0){
			speak_str.add("年齡");
			speak_num.add(speak_s.indexOf("年齡"));
		}
		if(speak_s.indexOf("性別")>0){
			speak_str.add("性別");
			speak_num.add(speak_s.indexOf("性別"));
		}
		if(speak_s.indexOf("身分證")>0){
			speak_str.add("身分證");
			speak_num.add(speak_s.indexOf("身分證"));
		}
		if(speak_s.indexOf("血壓")>0){
			speak_str.add("血壓");
			speak_num.add(speak_s.indexOf("血壓"));
		}
		if(speak_s.indexOf("呼吸")>0){
			speak_str.add("呼吸");
			speak_num.add(speak_s.indexOf("呼吸"));
		}
		if(speak_s.indexOf("心跳")>0){
			speak_str.add("心跳");
			speak_num.add(speak_s.indexOf("心跳"));
		}
		if(speak_s.indexOf("血氧")>0){
			speak_str.add("血氧");
			speak_num.add(speak_s.indexOf("血氧"));
		}
		for(int i=0;i<speak_num.size()-1;i++){
			for (int j=i+1;j<speak_num.size();j++){
				if(speak_num.get(i)>speak_num.get(j)){
					int tempNum;
					tempNum = speak_num.get(i);
					speak_num.set(i,speak_num.get(j));
					speak_num.set(j,tempNum);

					String tempString;
					tempString = speak_str.get(i);
					speak_str.set(i,speak_str.get(j));
					speak_str.set(j,tempString);
				}
			}
		}
		speak_str.add("|");
		count = speak_str.get(0).length()+1;
		for(int i=0;i<speak_num.size();i++){
			speak_input(speak_str.get(i),speak_s.substring(count).substring(0,speak_s.substring(count).indexOf(speak_str.get(i+1))));
			Log.e("speak_input", speak_str.get(i)+":"+speak_s.substring(count).substring(0,speak_s.substring(count).indexOf(speak_str.get(i+1))));
			count = count+ speak_s.substring(count).substring(0,speak_s.substring(count).indexOf(speak_str.get(i+1))).length()+speak_str.get(i+1).length();
		}

//		upload();
		uploadview(this);
	}

	public void speak_input(String title,String value){
		switch (title){
			case "姓名":
				if (info_data.size() > 0){
					info_data.set(0,value);
				}else {
					info_data.add(value);
					info_data.add("");
					info_data.add("");
				}
				break;
			case "年齡":
				if (info_data.size() > 0){
					info_data.set(1,value);
				}else {
					info_data.add("");
					info_data.add(value);
					info_data.add("");
				}
				break;
			case "性別":
				switch (value) {
					case "男":
						gender = 1;
						break;
					case "女":
						gender = 0;
						break;
					default:
						gender = 2;
						break;
				}
				break;
			case "身分證":
				identity = value;
				break;
			case "血壓":
				if(isNumeric(value) && value.length()>4){
					vital(value,0);
				}
				break;
			case "心跳":
				if(isNumeric(value) && value.length()>0){
					vital(value,1);
				}
				break;
			case "呼吸":
				if(isNumeric(value) && value.length()>0){
					vital(value,2);
				}
				break;
			case "血氧":
				if(isNumeric(value) && value.length()>0){
					vital(value,3);
				}
				break;
		}
	}

	//判斷是否為數字
	private boolean isNumeric(String value) {
		for (int i = value.length();--i>=0;){
			if (!Character.isDigit(value.charAt(i))){
				return false;
			}
		}
		return true;
	}

	public void vital(String value,int count){

		switch (count){
			case 0:
				vital_ip(1,value);
				break;
			case 1:
				vital_ip(2,value);
				break;
			case 2:
				vital_ip(3,value);
				break;
			case 3:
				vital_ip(4,value);
				break;
		}
	}

	public void vital_ip(int i,String value){
		Calendar calendar = Calendar.getInstance();
		String HOUR = "" + calendar.get(Calendar.HOUR_OF_DAY);
		String MIN = "" + calendar.get(Calendar.MINUTE);
		if (HOUR.length() < 2) {
			HOUR = "0" + HOUR;
		}
		if (MIN.length() < 2) {
			MIN = "0" + MIN;
		}
		if(vita_data.size()>0){
			if(vita_data.get(0).length()==0 && vita_data.get(i).length()==0){
				vita_data.set(0,HOUR+":"+MIN);
				vita_data.set(i,value);
			}
			else if(vita_data.get(0).length()>0 && vita_data.get(i).length()==0){
				vita_data.set(i,value);
			}
			else if(vita_data.get(6).length()==0 && vita_data.get(i+6).length()==0){
				vita_data.set(6,HOUR+":"+MIN);
				vita_data.set(i+6,value);
			}
			else if(vita_data.get(6).length()>0 && vita_data.get(i+6).length()==0){
				vita_data.set(i+6,value);
			}
			else if(vita_data.get(12).length()==0 && vita_data.get(i+12).length()==0){
				vita_data.set(12,HOUR+":"+MIN);
				vita_data.set(i+12,value);
			}
			else if(vita_data.get(12).length()>0 && vita_data.get(i+12).length()==0){
				vita_data.set(i+12,value);
			}
		}else {
			for(int j = 0; j < 18; j++){
				vita_data.add("");
			}
			vita_data.set(0,HOUR+":"+MIN);
			vita_data.set(i,value);
		}
	}


	*/
}