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

	// �M��
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return setView(position, convertView);
	}

	// �粒
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
//				acer = "�O�_��";
//				break;
//			case "B":
//				acer = "�O����";
//				break;
//			case "C":
//				acer = "�򶩥�";
//				break;
//			case "D":
//				acer = "�O�n��";
//				break;
//			case "E":
//				acer = "������";
//				break;
//			case "F":
//				acer = "�s�_��";
//				break;
//			case "G":
//				acer = "�y����";
//				break;
//			case "H":
//				acer = "��饫";
//				break;
//			case "I":
//				acer = "�Ÿq��";
//				break;
//			case "J":
//				acer = "�s�˿�";
//				break;
//			case "K":
//				acer = "�]�߿�";
//				break;
//			case "L":
//				acer = "�O����";
//				break;
//			case "M":
//				acer = "�n�뿤";
//				break;
//			case "N":
//				acer = "���ƿ�";
//				break;
//			case "O":
//				acer = "�s�˥�";
//				break;
//			case "P":
//				acer = "���L��";
//				break;
//			case "Q":
//				acer = "�Ÿq��";
//				break;
//			case "R":
//				acer = "�O�n��";
//				break;
//			case "S":
//				acer = "������";
//				break;
//			case "T":
//				acer = "�̪F��";
//				break;
//			case "U":
//				acer = "�Ὤ��";
//				break;
//			case "V":
//				acer = "�O�F��";
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