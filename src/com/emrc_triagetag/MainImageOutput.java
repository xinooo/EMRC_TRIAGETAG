package com.emrc_triagetag;

import java.io.*;

import android.graphics.Bitmap;

public class MainImageOutput extends Thread {
	Bitmap bmp = null;

	public MainImageOutput(Bitmap bmp) {
		this.bmp = bmp;
	}

	public void run() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			DataInputStream din = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
			DataOutputStream dos = new DataOutputStream(MainActivity.skt.getOutputStream());
			int size = baos.toByteArray().length;
			byte[] data = new byte[size];
			din.read(data);
			dos.writeInt(size);
			dos.write(data);
			din.close();
		} catch (IOException e) {
			// System.err.println("CheckedIOTest: " + e);
		}
	}
}
