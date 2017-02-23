package com.hezd.zxing.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.hezd.zxing.R;
import com.hezd.zxing.camera.CameraManager;
import com.hezd.zxing.decoding.CaptureActivityHandler;
import com.hezd.zxing.decoding.InactivityTimer;
import com.hezd.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

/**
 * 
 * 
 * @ClassName: QRCodeScanActivity
 * 
 * @Description: TODO(扫码相机页面展示)
 * 
 * @author hezd
 * 
 * @date 2014-3-19 上午11:44:05
 * 
 * 
 */
public class QRCodeScanActivity extends Activity implements Callback,
		OnClickListener{
	public static final int SCAN_QR = 11;
	private final static String TAG="QRCodeScanActivity";
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	// private TextView txtResult;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;

	private String resultString;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.capture);
		getViews();
		setViews();
		setListeners();
	}

	public void getViews() {
		// TODO Auto-generated method stub 
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		CameraManager.init(this);
		viewfinderView.setCameraManager(CameraManager.get());
//		mCancelScanBtn = (Button) findViewById(R.id.btn_cancel_scan);

	}

	public void setViews() {
		// CameraManager
		CameraManager.init(getApplication());
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	public void setListeners() {
		// TODO Auto-generated method stub
//		mCancelScanBtn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				QRCodeScanActivity.this.finish();
//			}
//		});
	}
	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
		try {
			if (handler != null) {
				handler.quitSynchronously();
				handler = null;
			}
			CameraManager.get().closeDriver();
		} catch (Exception e) {
		}
	}
	 
	/**
	 * 处理分享商品的结果。
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	}
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	public void handleDecode(Result obj, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		resultString = obj.getText();
		if (!TextUtils.isEmpty(resultString)) {
			Intent intent = new Intent();
			intent.putExtra("scan_result", resultString);
			setResult(SCAN_QR, intent);
		} else {
			Toast.makeText(this,"二维码内容为空",Toast.LENGTH_SHORT).show();
			setResult(RESULT_CANCELED);
		}
		finish();
//		boolean fromLiveScan = barcode != null;
//		// 如果是网址则进入网址处理
//		if (resultString.startsWith(AppApi.URL_BEGIN)) {
//			Intent intent = new Intent(mContext, WebViewActivity.class);
//			intent.putExtra("type", WebViewActivity.TYPE_URL_CODE);
//			intent.putExtra("content", resultString);
//			intent.putExtra("title", "扫码");
//			mContext.startActivity(intent);
//			finish();
//		}else{
//			ShowMessage.showToast(mContext, resultString);
//			finish();
//		}
//			if (resultString.startsWith(AppApi.QRCODE_LASHOU_START_URL_PAY)){
//				try {
//				    Uri uri = Uri.parse(resultString);
//					gid=uri.getQueryParameter("gid");
//					gt=uri.getQueryParameter("gt");
//					fr=uri.getQueryParameter("fr");
//				} catch (Exception e) { 
//					LogUtils.e("QRCodeScanActivity:"+e.getMessage());
//				}
//			}
//			// 当前缀是m.lashou.com时获取接口信息
//			else if (resultString.startsWith(AppApi.QRCODE_LASHOU_START_URL)) {
//				ShowProgressDialog.ShowProgressOn(this, "提示", "处理中,请稍候", false,false);
//				AppApi.getQrCodeInfo(this, this, resultString);
//			}
//			// 如果是网址就弹出对话框提示
//			else {
//				netDialog = new LashouMultiDialogRound(QRCodeScanActivity.this,
//						R.style.LashouDialog_null,
//						getString(R.string.upomp_bypay_prompt), getString(
//								R.string.qrcode_neturl_open, resultString),
//						getString(R.string.cancel), getString(R.string.ok),
//						netDialogCancelListener, netDialogOpenListener);
//				netDialog.show();
//			}
//		} else {
//			// 非网址，直接打印出来
//			dialog = new LashouMultiDialogRound(QRCodeScanActivity.this,
//					R.style.LashouDialog_null,
//					getString(R.string.upomp_bypay_prompt), getString(
//							R.string.qrcode_copy_text, resultString),
//					getString(R.string.cancel), getString(R.string.ok),
//					listener, copylistener);
//			dialog.show();
//		}
	}

//	private void launchLoginActivity(String goodsType, int requestCode) {
//		if("3".equals(goodsType)) {
//			Intent intent = new Intent(this, LoginActivity.class);
//			intent.putExtra(EXTRA_FROM, FLAG_GOODS_LOTTERY);
////			startActivityForResult(intent, GoodsDetailActivity.LOTTERY_REQUEST_CODE);
//		}else {
//			Intent intent = new Intent(this, LoginActivity.class);
//			if (requestCode == RESULT_BUY_RIGHT_NOW) {
//				intent.putExtra(EXTRA_FROM, FLAG_BUY_RIGHT_NOW);
//			} else if (requestCode == RESULT_BUY_TOGETHER_CODE) {
//				intent.putExtra(EXTRA_FROM, FLAG_BUY_GROUP);
//			}
//			startActivityForResult(intent, requestCode);
//		}
//	}
	private void continuePreview() {
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		initCamera(surfaceHolder);
		if (handler != null)
			handler.restartPreviewAndDecode();
	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.back:
//			// 返回
//			finish();
//			break;
		}
	}



 
	/** 打开url */
	public void handlerOpenUrl() {
//		Intent intent = new Intent(QRCodeScanActivity.this,
//				WebViewActivity.class);
//		intent.putExtra("type", WebViewActivity.TYPE_URL_CODE);
//		intent.putExtra("title", "扫码结果");
//		intent.putExtra("content", resultString);
//		startActivity(intent);
		//使用外部浏览器打开
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(resultString));
			startActivity(intent);
			continuePreview();
			this.finish();
		} catch (Exception e) {
		}
//		Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(resultString));  
//        it.setClassName("com.android.browser", "com.android.browser.BrowserActivity");  
//        startActivity(it);  
	}
}