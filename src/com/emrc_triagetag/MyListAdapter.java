package com.emrc_triagetag;

import java.util.ArrayList;
import java.util.Collections;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyListAdapter extends BaseAdapter {
	private Context context;
	private boolean db, ok = false;
	public  ArrayList<String> sListView = new ArrayList<String>();
	public  ArrayList<View> aListView = new ArrayList<View>();

	MyListAdapter(ArrayList<View> lv) {
		ok = true;
		for (int s = 0; s < lv.size(); s++) {
			aListView.add(lv.get(s));
		}
	}

	MyListAdapter(Context context, boolean db, ArrayList<String> ls) {
		this.context = context;
		this.db = db;
		for (int i = 0; i < ls.size(); i++) {
			sListView.add(ls.get(i));
		}
		if (!db) {
			Collections.reverse(sListView);
		}
	}

	private View setView(int position, View convertView) {
		if (ok) {
			convertView = aListView.get(position);
		} else {
			if (db && position == 0) {
				convertView = setRYGB(context, convertView);
			} else {
				convertView = setMyAdapter(context, position, db, convertView);
			}
		}
		return convertView;
	}

	public int getCount() {
		if (ok) {
			return aListView.size();
		} else {
			return sListView.size();
		}
	}

	public Object getItem(int position) {
		return sListView.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	private View setRYGB(Context context, View vi) {
		String mmsg = sListView.get(0).toString();
		int resource = R.layout.style_textview4;
		if (MainActivity.EN) {
			resource = R.layout.style_textview6;
		}
		vi = LayoutInflater.from(context).inflate(resource, null);
		TextView h_red = (TextView) vi.findViewById(R.id.tv_h_red);
		TextView h_yellow = (TextView) vi.findViewById(R.id.tv_h_yellow);
		TextView h_green = (TextView) vi.findViewById(R.id.tv_h_green);
		TextView h_black = (TextView) vi.findViewById(R.id.tv_h_black);
		TextView s_red = (TextView) vi.findViewById(R.id.tv_s_red);
		TextView s_yellow = (TextView) vi.findViewById(R.id.tv_s_yellow);
		TextView s_green = (TextView) vi.findViewById(R.id.tv_s_green);
		TextView s_black = (TextView) vi.findViewById(R.id.tv_s_black);
		// 17030600001/1/五身份證/5/ 其他處置 (保暖).|║0=n/║0=n/║n/║2/32/10/A1267767/
		int r = 0;
		String hr = MainActivity.getLine(mmsg, r, '/');
		r += hr.length() + 1;
		String hy = MainActivity.getLine(mmsg, r, '/');
		r += hy.length() + 1;
		String hg = MainActivity.getLine(mmsg, r, '/');
		r += hg.length() + 1;
		String hb = MainActivity.getLine(mmsg, r, '/');
		r += hb.length() + 1;
		String sr = MainActivity.getLine(mmsg, r, '/');
		r += sr.length() + 1;
		String sy = MainActivity.getLine(mmsg, r, '/');
		r += sy.length() + 1;
		String sg = MainActivity.getLine(mmsg, r, '/');
		r += sg.length() + 1;
		String sb = MainActivity.getLine(mmsg, r, '/');

		h_red.setText(hr);
		h_yellow.setText(hy);
		h_green.setText(hg);
		h_black.setText(hb);
		s_red.setText(sr);
		s_yellow.setText(sy);
		s_green.setText(sg);
		s_black.setText(sb);
		return vi;
	}

	@SuppressLint("InflateParams")
	private View setMyAdapter(Context context, int i, boolean db, View vi) {
		vi = LayoutInflater.from(context).inflate(R.layout.style_textview3, null);
		RelativeLayout t_bg = (RelativeLayout) vi.findViewById(R.id.t3_relativeLayout);
		ImageView t_image = (ImageView) vi.findViewById(R.id.t3_image);
		TextView t_name = (TextView) vi.findViewById(R.id.t3_name);
		TextView t_hosp = (TextView) vi.findViewById(R.id.t3_hospital);
		TextView t_other = (TextView) vi.findViewById(R.id.t3_other);
		String mmsg = sListView.get(i);
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
		r += 2;
		int emrc_count = Integer.parseInt(MainActivity.getLine(mmsg,r, '/'));
		r += (emrc_count + "").length() + 1;
		String hosp = MainActivity.getLine(mmsg, r, '/');
		r += (hosp + "").length() + 1;
		String iden = MainActivity.getLine(mmsg, r, '/');
		r += (iden + "").length() + 1;
		String inc = MainActivity.getLine(mmsg, r, '/');
		r += (inc + "").length() + 1;
		String age1 = MainActivity.getLine(mmsg, r, '/');
		r += (age1 + "").length() + 1;
		String mh = MainActivity.getLine(mmsg, r, '/');
		r += (mh + "").length() + 1;
		String QRC = MainActivity.getLine(mmsg, r, '/');	
		
		if (hosp.length() != 0) {
//			MainActivity.toast(hosp, context);
			try {
				if (Integer.parseInt(hosp) == 0) {
					hosp = context.getResources().getString(R.string.s_List_38);
					setFlickerAnimation(t_image);
				} else {
					hosp = MainActivity.Treatment_unit.get(Integer.parseInt(hosp)).unit;
					t_image.setVisibility(View.GONE);
				}
			} catch (Exception e) {
				hosp = MainActivity.Treatment_unit.get(getTrainsportSelection(hosp)).unit;
				t_image.setVisibility(View.GONE);
			}
		} else {
			hosp = context.getResources().getString(R.string.s_List_38);
			setFlickerAnimation(t_image);
		}
		if (name.length() == 0) {
			t_name.setText(number);
			t_name.setTextSize(25);
		} else {
			t_name.setText(name);
			t_name.setTextSize(25);
		}
		t_hosp.setText(hosp);
		if (db) {
			// 去除標號
			other = MainActivity.getLine(other, 0, '|');
		}
		other += MainActivity.getEMRC(context, emrc_count, leve_count);
		t_other.setText(other);
		switch (leve_count + "") {
		case "0":
			t_name.setTextColor(Color.WHITE);
			t_hosp.setTextColor(Color.WHITE);
			t_other.setTextColor(Color.WHITE);
			t_bg.setBackgroundColor(Color.BLACK);
			break;
		case "1":
			t_name.setTextColor(Color.WHITE);
			t_hosp.setTextColor(Color.WHITE);
			t_other.setTextColor(Color.WHITE);
			t_bg.setBackgroundColor(Color.RED);
			break;
		case "2":
			t_bg.setBackgroundColor(Color.YELLOW);
			break;
		case "3":
			t_bg.setBackgroundColor(Color.GREEN);
			break;
		default:
			t_name.setTextColor(Color.BLACK);
			t_hosp.setTextColor(Color.BLACK);
			t_other.setTextColor(Color.BLACK);
			t_bg.setBackgroundColor(Color.WHITE);
			break;
		}
		return vi;
	}

	public int getTrainsportSelection(String area) {
		int s = 0;
		for (int i = 0; i < MainActivity.Treatment_unit.size(); i++) {
			if (MainActivity.Treatment_unit.get(i).area.equals(area)) {
				s = i;
				break;
			}
		}
		return s;
	}

	// 圖片閃爍
	public void setFlickerAnimation(ImageView iv_chat_head) {
		Animation animation = new AlphaAnimation(1, 0); // Change alpha from
														// fully visible to
														// invisible
		animation.setDuration(500); // duration - half a second
		animation.setRepeatCount(Animation.INFINITE); // Repeat animation
														// infinitely
		animation.setRepeatMode(Animation.REVERSE);
		iv_chat_head.setAnimation(animation);
	}

	@SuppressLint("InflateParams")
	public View getView(int position, View convertView, ViewGroup parent) {
		return setView(position, convertView);
	}
}