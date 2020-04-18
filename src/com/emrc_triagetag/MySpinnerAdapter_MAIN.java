package com.emrc_triagetag;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

@SuppressLint({ "InflateParams", "ViewHolder" })
public class MySpinnerAdapter_MAIN extends BaseAdapter {
	private Context context;
	private ArrayList<hospital> unit;

	public MySpinnerAdapter_MAIN(Context context, ArrayList<hospital> list) {
		this.context = context;
		this.unit = list;
	}

	// 清單
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return setView(position, convertView);
	}

	// 選完
	public View getView(int position, View convertView, ViewGroup parent) {
		return setView(position, convertView);
	}

	private View setView(int position, View convertView) {
		convertView = LayoutInflater.from(context).inflate(R.layout.style_spinner_25, null);
		TextView tv = (TextView) convertView.findViewById(R.id.textview);
		if (MainActivity.EN) {
			tv.setTextSize(25);
			if (MainActivity.textSize == 40) {
				tv.setTextSize(35);
			}
		} else {
			if (MainActivity.textSize == 40) {
				tv.setTextSize(35);
			}
		}
		switch (unit.get(position).level) {
		case 1:
			tv.setTextColor(Color.WHITE);
			tv.setBackgroundColor(Color.RED);
			break;
		case 2:
			tv.setBackgroundColor(Color.YELLOW);
			break;
		case 3:
			tv.setBackgroundColor(Color.GREEN);
			break;
		default:
			break;
		}
		String acer = "";
		if (position != 0)
			acer = unit.get(position).area.substring(0, 1);
		if (!acer.equals("")) {
			switch (acer) {
//			case "A":
//				acer = "臺北市";
//				break;
//			case "B":
//				acer = "臺中市";
//				break;
//			case "C":
//				acer = "基隆市";
//				break;
//			case "D":
//				acer = "臺南市";
//				break;
//			case "E":
//				acer = "高雄市";
//				break;
//			case "F":
//				acer = "新北市";
//				break;
//			case "G":
//				acer = "宜蘭縣";
//				break;
//			case "H":
//				acer = "桃園市";
//				break;
//			case "I":
//				acer = "嘉義市";
//				break;
//			case "J":
//				acer = "新竹縣";
//				break;
//			case "K":
//				acer = "苗栗縣";
//				break;
//			case "L":
//				acer = "臺中市";
//				break;
//			case "M":
//				acer = "南投縣";
//				break;
//			case "N":
//				acer = "彰化縣";
//				break;
//			case "O":
//				acer = "新竹市";
//				break;
//			case "P":
//				acer = "雲林縣";
//				break;
//			case "Q":
//				acer = "嘉義縣";
//				break;
//			case "R":
//				acer = "臺南市";
//				break;
//			case "S":
//				acer = "高雄市";
//				break;
//			case "T":
//				acer = "屏東縣";
//				break;
//			case "U":
//				acer = "花蓮縣";
//				break;
//			case "V":
//				acer = "臺東縣";
//				break;
//			default:
//				break;
			case "A":
				acer = "Taipei";
				break;
			case "B":
				acer = "Taichung";
				break;
			case "C":
				acer = "Keelung";
				break;
			case "D":
				acer = "Tainan";
				break;
			case "E":
				acer = "Kaohsiung";
				break;
			case "F":
				acer = "New Taipei";
				break;
			case "G":
				acer = "Yilan";
				break;
			case "H":
				acer = "Taoyuan";
				break;
			case "I":
				acer = "Chiayi";
				break;
			case "J":
				acer = "Hsinchu";
				break;
			case "K":
				acer = "Miaoli";
				break;
			case "L":
				acer = "Taichung";
				break;
			case "M":
				acer = "Nantou";
				break;
			case "N":
				acer = "Changhua";
				break;
			case "O":
				acer = "Hsinchu";
				break;
			case "P":
				acer = "Yunlin";
				break;
			case "Q":
				acer = "Chiayi";
				break;
			case "R":
				acer = "Tainan";
				break;
			case "S":
				acer = "Kaohsiung";
				break;
			case "T":
				acer = "Pingtung";
				break;
			case "U":
				acer = "Hualien ";
				break;
			case "V":
				acer = "Taitung";
				break;
			default:
				break;
			}
			acer = acer + "-";
		}
		tv.setText(acer + unit.get(position).unit);
		return convertView;
	}

	private String getString(int sArea1) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getCount() {
		return unit.size();
	}

	public Object getItem(int position) {
		return unit.get(position).unit;
	}

	public long getItemId(int position) {
		return position;
	}
}