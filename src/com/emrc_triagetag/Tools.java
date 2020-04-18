package com.emrc_triagetag;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.Log;

@SuppressLint("TrulyRandom")
public class Tools {
	public static String target(Context c, int x) {
		String msg = "";
		switch (x) {
		case 0:
			msg = c.getResources().getString(R.string.s_List_23) + " "; // 50,22
			break;
		case 2:
			msg = c.getResources().getString(R.string.s_List_24) + " "; // 50,34
			break;
		case 4:
			msg = c.getResources().getString(R.string.s_List_25) + " "; // 50,42.45
			break;
		case 6:
			msg = c.getResources().getString(R.string.s_List_26) + " "; // 42.5,55
			break;
		case 8:
			msg = c.getResources().getString(R.string.s_List_27) + " "; // 57.5,55
			break;
		case 10:
			msg = c.getResources().getString(R.string.s_List_28) + " "; // 42.5,68
			break;
		case 12:
			msg = c.getResources().getString(R.string.s_List_29) + " "; // 57.5,68
			break;
		case 14:
			msg = c.getResources().getString(R.string.s_List_30) + " "; // 32,34.5
			break;
		case 16:
			msg = c.getResources().getString(R.string.s_List_31) + " "; // 68,34.5
			break;
		case 18:
			msg = c.getResources().getString(R.string.s_List_32) + " "; // 24,40
			break;
		case 20:
			msg = c.getResources().getString(R.string.s_List_33) + " "; // 76,40
			break;
		case 22:
			msg = c.getResources().getString(R.string.s_List_34) + " "; // 11,45
			break;
		case 24:
			msg = c.getResources().getString(R.string.s_List_35) + " "; // 89,45
			break;
		case 25:
			msg = c.getResources().getString(R.string.s_List_36) + " "; // 50,34
			break;
		case 26:
			msg = c.getResources().getString(R.string.s_List_37) + " "; // 50,42.45
			break;
		}
		return msg;
	}

	public static void setInjury(ArrayList<Integer> List) {
		int mWidth = MainActivity.metrics.widthPixels; // 螢幕寬
		int mHeight = MainActivity.metrics.heightPixels; // 螢幕長
		for (int j = 0; j < MainActivity.inju_target.size(); j += 2) {
			double dx = MainActivity.inju_target.get(j) * mWidth;
			double dy = MainActivity.inju_target.get(j + 1) * mHeight;
			List.add((int) dx);
			List.add((int) dy);
		}
	}

	public static String getInjury(Context c, double x, double y, int ba) {
		String z = "";
		int mWidth = MainActivity.metrics.widthPixels; // 螢幕寬
		int mHeight = MainActivity.metrics.heightPixels; // 螢幕長
		for (int j = 0; j < MainActivity.inju_target.size(); j += 2) {
			double dx = MainActivity.inju_target.get(j) * mWidth;
			double dy = MainActivity.inju_target.get(j + 1) * mHeight;
			double mx = x * mWidth;
			double my = y * mHeight;
			if (Math.abs((int) dx - mx) < 100 && Math.abs((int) dy - my) < 100) {
				if (ba == 0) {
					z += target(c, j);
					break;
				} else {
					if (j == 2) {
						z += target(c, 25);
						break;
					}
					if (j == 4) {
						z += target(c, 26);
						break;
					}
					if (j == 6) {
						z += target(c, 8);
						break;
					}
					if (j == 8) {
						z += target(c, 6);
						break;
					}
					if (j == 10) {
						z += target(c, 12);
						break;
					}
					if (j == 12) {
						z += target(c, 10);
						break;
					}
					if (j == 14) {
						z += target(c, 16);
						break;
					}
					if (j == 16) {
						z += target(c, 14);
						break;
					}
					if (j == 18) {
						z += target(c, 20);
						break;
					}
					if (j == 20) {
						z += target(c, 18);
						break;
					}
					if (j == 22) {
						z += target(c, 24);
						break;
					}
					if (j == 24) {
						z += target(c, 22);
						break;
					}
				}
			}
		}
		return z;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void getListString(String msg, ArrayList Data, int size) {
		if (msg.length() > 2) {
			int tmp = 0;
			Data.clear();
			if (size != 0) {
				for (int i = 0; i < size; i++) {
					String M;
					if (size == 3 && i == 2) {
						msg += '|';
						M = msg.substring(tmp + i).substring(0, msg.substring(tmp + i).indexOf('|'));
					} else {
						M = msg.substring(tmp + i).substring(0, msg.substring(tmp + i).indexOf('/'));
						tmp += M.length();
					}
					Data.add(M);
				}
			} else {
				int run = Integer.parseInt(msg.substring(tmp).substring(0, msg.substring(tmp).indexOf('=')));
				if (run != 0) {
					if (run != 10) {
						tmp += (run + "").length() + 1;
					}
					for (int i = 0; i < run; i++) {
						String M = msg.substring(tmp + i).substring(0, msg.substring(tmp + i).indexOf('/'));
						tmp += M.length();
						double d = Double.parseDouble(M);
						int mWidth = MainActivity.metrics.widthPixels; // 螢幕寬
						int mHeight = MainActivity.metrics.heightPixels; // 螢幕長
						if (d > 1) {
							int dd = (int) d;
							if (MainActivity.f) {
								d = getPercent(dd, mWidth);
								MainActivity.f = false;
							} else {
								d = getPercent(dd, mHeight);
								MainActivity.f = true;
							}
						}
						Data.add(d);
					}
				}
			}
		}
	}

	private static double getPercent(Integer num, Integer totalPeople) {
		String percent;
		Double p3 = 0.0;
		if (totalPeople == 0) {
			p3 = 0.0;
		} else {
			p3 = num * 0.01 / totalPeople;
		}
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMinimumFractionDigits(2);// 控制保留小数点后几位，2：表示保留2位小数点
		percent = nf.format(p3);
		percent = percent.substring(0, percent.length() - 1);
		return Double.parseDouble(percent);
	}

	public static String setReplace(String s) {
		String p = s;
		p = p.replace("|", " ");
		p = p.replace("║", " ");
		return p;
	}

	@SuppressWarnings("rawtypes")
	public static String getList(ArrayList Data, boolean bl) {
		String tmp = "", rep = "";
		if (Data.size() != 0) {
			for (int i = 0; i < Data.size(); i++) {
				if (bl && i == 2) {
					rep = (Data.get(i) + "").replace("\n", " ");
					tmp += rep;
				} else {
					rep = Data.get(i) + "/";
					tmp += setReplace(rep);
				}

			}
		} else {
			if (!bl) {
				tmp = "n/";
			} else {
				tmp = "//";
			}
		}
		return tmp;
	}

	public static void sandFile(Bitmap bmp) {
		new MainImageOutput(bmp).start();
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();
		// 取 drawable 的顏色格式
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		drawable.draw(canvas);
		return bitmap;
	}

	public static void speak(String s){
		ArrayList<String> speak_str = new ArrayList<String>();
		ArrayList<Integer> speak_num = new ArrayList<Integer>();
		String speak_s = "";
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
	}

	public static void speak_input(String title, String value){
		switch (title){
			case "姓名":
				if (MainActivity.info_data.size() > 0){
					MainActivity.info_data.set(0,value);
				}else {
					MainActivity.info_data.add(value);
					MainActivity.info_data.add("");
					MainActivity.info_data.add("");
				}
				break;
			case "年齡":
				if (MainActivity.info_data.size() > 0){
					MainActivity.info_data.set(1,value);
				}else {
					MainActivity.info_data.add("");
					MainActivity.info_data.add(value);
					MainActivity.info_data.add("");
				}
				break;
			case "性別":
				switch (value) {
					case "男":
						MainActivity.gender = 1;
						break;
					case "女":
						MainActivity.gender = 0;
						break;
					default:
						MainActivity.gender = 2;
						break;
				}
				break;
			case "身分證":
				MainActivity.identity = value;
				break;
			case "血壓":
				if(isNumeric(value) && value.length()>=4){
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
	private static boolean isNumeric(String value) {
		for (int i = value.length();--i>=0;){
			if (!Character.isDigit(value.charAt(i))){
				return false;
			}
		}
		return true;
	}

	public static void vital(String value, int count){

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

	public static void vital_ip(int i, String value){
		Calendar calendar = Calendar.getInstance();
		String HOUR = "" + calendar.get(Calendar.HOUR_OF_DAY);
		String MIN = "" + calendar.get(Calendar.MINUTE);
		if (HOUR.length() < 2) {
			HOUR = "0" + HOUR;
		}
		if (MIN.length() < 2) {
			MIN = "0" + MIN;
		}
		if(MainActivity.vita_data.size()>0){
			if(MainActivity.vita_data.get(0).length()==0 && MainActivity.vita_data.get(i).length()==0){
				MainActivity.vita_data.set(0,HOUR+":"+MIN);
				MainActivity.vita_data.set(i,value);
			}
			else if(MainActivity.vita_data.get(0).length()>0 && MainActivity.vita_data.get(i).length()==0){
				MainActivity.vita_data.set(i,value);
			}
			else if(MainActivity.vita_data.get(6).length()==0 && MainActivity.vita_data.get(i+6).length()==0){
				MainActivity.vita_data.set(6,HOUR+":"+MIN);
				MainActivity.vita_data.set(i+6,value);
			}
			else if(MainActivity.vita_data.get(6).length()>0 && MainActivity.vita_data.get(i+6).length()==0){
				MainActivity.vita_data.set(i+6,value);
			}
			else if(MainActivity.vita_data.get(12).length()==0 && MainActivity.vita_data.get(i+12).length()==0){
				MainActivity.vita_data.set(12,HOUR+":"+MIN);
				MainActivity.vita_data.set(i+12,value);
			}
			else if(MainActivity.vita_data.get(12).length()>0 && MainActivity.vita_data.get(i+12).length()==0){
				MainActivity.vita_data.set(i+12,value);
			}
		}else {
			for(int j = 0; j < 18; j++){
				MainActivity.vita_data.add("");
			}
			MainActivity.vita_data.set(0,HOUR+":"+MIN);
			MainActivity.vita_data.set(i,value);
		}
	}
}
