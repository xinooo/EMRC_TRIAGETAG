package com.emrc_triagetag;

import android.*;
import android.annotation.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.hardware.camera2.*;
import android.media.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import java.io.*;
import java.nio.*;
import java.util.*;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class CameraTwoActivity extends Activity {
	private static final String TAG = "CameraTwo ";
	private static final int COLOR_FormatI420 = 1;
	private static final int COLOR_FormatNV21 = 2;
	private int[] digree = { 90, 0, 270, 180 };
	private int rotation;
	private Button mCanvas;
	private String mCameraID;// 摄像头Id 0 为后 1 为前
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private Handler mTagHandler, childHandler, mainHandler;
	private HandlerThread mBackgroundThread;
	private HomeListen mHomeListen = null;
	private boolean home = false;
	private ImageReader mCaptureBuffer, mImageReader;
	private CameraCaptureSession mCameraCaptureSession;
	private CameraDevice mCameraDevice;
	private CameraManager mCameraManager;// 摄像头管理器

	protected void onResume() {
		super.onResume();
		if (checkPermission()) {
			showPermission();
		} else {
			setStart();
		}
		if (home) {
			mHomeListen.start();
		}
	}

	protected void onPause() {
		super.onPause();
		if (home) {
			mHomeListen.stop();
		}
		if (!checkPermission()) {
			mSurfaceView.getHolder().setFixedSize(/* width */0, /* height */0);

			if (mCameraCaptureSession != null) {
				mCameraCaptureSession.close();
				mCameraCaptureSession = null;
			}
			mBackgroundThread.quitSafely();
			try {
				mBackgroundThread.join();
			} catch (InterruptedException ex) {
				Log.e(TAG, "Background worker thread was interrupted while joined", ex);
			}
			if (mCaptureBuffer != null)
				mCaptureBuffer.close();
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			finish();
			return true;
		default:
			break;
		}
		return false;
	}

	private boolean checkPermission() {
		int CAMERA = checkSelfPermission(Manifest.permission.CAMERA);
		int STORAGE = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		return permission(CAMERA) || permission(STORAGE);
	}

	@TargetApi(23)
	@SuppressLint("NewApi")
	private void showPermission() {
		if (checkPermission()) {
			// We don't have permission so prompt the user
			List<String> permissions = new ArrayList<String>();
			permissions.add(Manifest.permission.CAMERA);
			permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
		}
	}

	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
		case 0:
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// 許可授權
				setStart();
			} else {
				// 沒有權限
				toast("未授權應用使用權限!");
			}
			break;
		default:
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	private boolean permission(int mp) {
		return mp != PackageManager.PERMISSION_GRANTED;
	}

	@SuppressLint("InflateParams")
	private void setStart() {
		mBackgroundThread = new HandlerThread("background");
		mBackgroundThread.start();
		new Handler(mBackgroundThread.getLooper());
		new Handler(getMainLooper());
		mTagHandler = new Handler();
		mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		setContentView(R.layout.camera);
		mCanvas = (Button) findViewById(R.id.mainCanvas);
		mSurfaceView = (SurfaceView) findViewById(R.id.mainSurfaceView);
		mSurfaceView.setVisibility(View.VISIBLE);
		mCanvas.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO onClick
				// setEdit();
				mTagHandler.post(mSurfacTag);
			}
		});
		initVIew();
	}

	/**
	 * 初始化
	 */
	private void initVIew() {
		// mSurfaceView
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.setKeepScreenOn(true);
		// mSurfaceView添加回调
		mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
			// SurfaceView创建
			public void surfaceCreated(SurfaceHolder holder) {
				// 初始化Camera
				initCamera2();
			}

			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			}

			public void surfaceDestroyed(SurfaceHolder holder) { // SurfaceView销毁
				// 释放Camera资源
				if (null != mCameraDevice) {
					mCameraDevice.close();
					mCameraDevice = null;
				}
			}
		});
	}

	/**
	 * 初始化Camera2
	 */
	private void initCamera2() {
		// 获取摄像头管理
		mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		HandlerThread handlerThread = new HandlerThread("Camera2");
		handlerThread.start();
		childHandler = new Handler(handlerThread.getLooper());
		mainHandler = new Handler(getMainLooper());
		mCameraID = "" + CameraCharacteristics.LENS_FACING_FRONT;// 后摄像头
		try {
			mCameraManager.getCameraCharacteristics(mCameraID);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
		imagereader();

		try {
			if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
				return;
			}
			// 打开摄像头
			mCameraManager.openCamera(mCameraID, stateCallback, mainHandler);
			Log.e("Camera", "open");
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 摄像头创建监听
	 */
	private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
		public void onOpened(CameraDevice camera) {// 打开摄像头
			mCameraDevice = camera;
			// 开启预览
			takePreview();
		}

		public void onDisconnected(CameraDevice camera) {// 关闭摄像头
			if (null != mCameraDevice) {
				mCameraDevice.close();
				mCameraDevice = null;
			}
		}

		public void onError(CameraDevice camera, int error) {// 发生错误
			toast("前鏡頭開啟失敗");
		}
	};

	/**
	 * 开始预览
	 */
	private void takePreview() {
		try {
			// 创建预览需要的CaptureRequest.Builder
			final CaptureRequest.Builder previewRequestBuilder = mCameraDevice
					.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			// 将SurfaceView的surface作为CaptureRequest.Builder的目标
			previewRequestBuilder.addTarget(mSurfaceHolder.getSurface());
			// previewRequestBuilder.addTarget(mImageReader.getSurface());
			// 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
			mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()),
					new CameraCaptureSession.StateCallback() // ③
					{
						@Override
						public void onConfigured(CameraCaptureSession cameraCaptureSession) {
							if (null == mCameraDevice) {
								return;
							}
							// 当摄像头已经准备好时，开始显示预览
							mCameraCaptureSession = cameraCaptureSession;
							try {
								// setup3AControlsLocked(previewRequestBuilder);
								// 自动对焦
								previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
										CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
								// 打开闪光灯
								// previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
								// CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
								// 显示预览
								CaptureRequest previewRequest = previewRequestBuilder.build();

								mCameraCaptureSession.setRepeatingRequest(previewRequest, mPreCaptureCallback,
										childHandler);
							} catch (CameraAccessException e) {
								e.printStackTrace();
							}
						}

						public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
							toast("配置失敗");
						}
					}, childHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 拍照
	 */
	private void takePicture() {
		if (mCameraDevice == null) {
			return;
		}
		final CaptureRequest.Builder captureRequestBuilder;
		try {
			// TEMPLATE_PREVIEW：創建預覽的請求
			// TEMPLATE_STILL_CAPTURE：創建一個適合於靜態圖像捕獲的請求，圖像質量優先於幀速率。
			// TEMPLATE_RECORD：創建視頻錄製的請求
			// TEMPLATE_VIDEO_SNAPSHOT：創建視視頻錄製時截屏的請求
			// TEMPLATE_ZERO_SHUTTER_LAG：創建一個適用於零快門延遲的請求。在不影響預覽幀率的情況下最大化圖像質量。
			// TEMPLATE_MANUAL：創建一個基本捕獲請求，這種請求中所有的自動控制都是禁用的(自動曝光，自動白平衡、自動焦點)。
			captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
			// 将imageReader的surface作为CaptureRequest.Builder的目标
			captureRequestBuilder.addTarget(mImageReader.getSurface());
			// 自动对焦
			captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
					CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
			// 自动曝光
			// captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
			// CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
			// 获取手机方向
			rotation = getWindowManager().getDefaultDisplay().getRotation();
			// 拍照
			CaptureRequest mCaptureRequest = captureRequestBuilder.build();
			mCameraCaptureSession.capture(mCaptureRequest, mPreCaptureCallback, childHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private Runnable mSurfacTag = new Runnable() {
		public void run() {
			// TODO postDelayedTime
			takePicture();
			// mTagHandler.postDelayed(this, 1500);
		}
	};

	private CameraCaptureSession.CaptureCallback mPreCaptureCallback = new CameraCaptureSession.CaptureCallback() {
		@Override
		public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
				TotalCaptureResult result) {
		}

		@Override
		public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
				CaptureResult partialResult) {
		}

	};

	private void imagereader() {
		mImageReader = ImageReader.newInstance(854, 480, ImageFormat.YUV_420_888, 1);
		// 1080 16:9 1920 x 1080
		// 720 16:9 1280 x 720
		// 480 16:9 854 x 480
		// 1080 4:3 1440 x 1080
		// 720 4:3 960 x 720
		// 480 4:3 640 x 480
		mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
			// 可以在这里处理拍照得到的临时照片
			// 例如，写入本地
			public void onImageAvailable(ImageReader reader) {
				Log.e("TAG", "onImageAvailable");
				// 拿到拍照照片数据
				final Image image = reader.acquireNextImage();

				Rect rect = image.getCropRect();
				YuvImage yuvImage = new YuvImage(getDataFromImage(image, COLOR_FormatNV21), ImageFormat.NV21,
						rect.width(), rect.height(), null);

				try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
					yuvImage.compressToJpeg(rect, 100, stream);
					MainActivity.tmp_bmp = compBitmap(
							BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size()));
					// if (link)
					// new mSendImageFile().start();
					image.close();
					stream.close();
					Intent resultIntent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putString("result", "OK");
					resultIntent.putExtras(bundle);
					setResult(RESULT_OK, resultIntent);
					CameraTwoActivity.this.finish();
				} catch (FileNotFoundException ex) {
					Log.e(TAG, "Unable to open output file for writing", ex);
				} catch (IOException ex) {
					Log.e(TAG, "Failed to write the image to the output file", ex);
				}
			}
		}, mainHandler);
	}

	private void toast(String t) {
		Toast.makeText(this, t + "", Toast.LENGTH_SHORT).show();
	}

	private boolean isImageFormatSupported(Image image) {
		int format = image.getFormat();
		switch (format) {
		case ImageFormat.YUV_420_888:
		case ImageFormat.NV21:
		case ImageFormat.YV12:
			return true;
		}
		return false;
	}

	private byte[] getDataFromImage(Image image, int colorFormat) {
		if (colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21) {
			throw new IllegalArgumentException("only support COLOR_FormatI420 " + "and COLOR_FormatNV21");
		}
		if (!isImageFormatSupported(image)) {
			throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
		}
		Rect crop = image.getCropRect();
		int format = image.getFormat();
		int width = crop.width();
		int height = crop.height();
		Image.Plane[] planes = image.getPlanes();
		byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
		byte[] rowData = new byte[planes[0].getRowStride()];
		int channelOffset = 0;
		int outputStride = 1;
		for (int i = 0; i < planes.length; i++) {
			switch (i) {
			case 0:
				channelOffset = 0;
				outputStride = 1;
				break;
			case 1:
				if (colorFormat == COLOR_FormatI420) {
					channelOffset = width * height;
					outputStride = 1;
				} else if (colorFormat == COLOR_FormatNV21) {
					channelOffset = width * height + 1;
					outputStride = 2;
				}
				break;
			case 2:
				if (colorFormat == COLOR_FormatI420) {
					channelOffset = (int) (width * height * 1.25);
					outputStride = 1;
				} else if (colorFormat == COLOR_FormatNV21) {
					channelOffset = width * height;
					outputStride = 2;
				}
				break;
			}
			ByteBuffer buffer = planes[i].getBuffer();
			int rowStride = planes[i].getRowStride();
			int pixelStride = planes[i].getPixelStride();
			int shift = (i == 0) ? 0 : 1;
			int w = width >> shift;
			int h = height >> shift;
			buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
			for (int row = 0; row < h; row++) {
				int length;
				if (pixelStride == 1 && outputStride == 1) {
					length = w;
					buffer.get(data, channelOffset, length);
					channelOffset += length;
				} else {
					length = (w - 1) * pixelStride + 1;
					buffer.get(rowData, 0, length);
					for (int col = 0; col < w; col++) {
						data[channelOffset] = rowData[col * pixelStride];
						channelOffset += outputStride;
					}
				}
				if (row < h - 1) {
					buffer.position(buffer.position() + rowStride - length);
				}
			}
		}
		return data;
	}

	private Bitmap compBitmap(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		if (baos.toByteArray().length / 1024 > 1024) {// 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, 50, baos);// 这里压缩50%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 800f;// 这里设置高度为800f
		float ww = 480f;// 这里设置宽度为480f
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置缩放比例
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
	}

	private Bitmap compressImage(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		int options = 100;
		while (((baos.toByteArray().length / 1024) > 100) && options != 0) { // 100kb
			baos.reset();
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);
			if (options > 10) {
				options -= 10;
			} else {
				options--;
			}
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
		// if (rotation == 3) {
		Matrix matrix = new Matrix();
		matrix.postRotate(digree[rotation]);
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		// }
		return bitmap;
	}
}
