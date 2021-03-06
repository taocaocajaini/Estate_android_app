package com.example.admin.estate_android_app.zbar.lib.decode;

import android.os.Handler;
import android.os.Message;

import com.example.admin.estate_android_app.R;
import com.example.admin.estate_android_app.ui.fragment.ScaningFragment;
import com.example.admin.estate_android_app.zbar.lib.CaptureActivity;
import com.example.admin.estate_android_app.zbar.lib.camera.CameraManager;

/**
 * 描述: 扫描消息转发
 */
public final class CaptureActivityHandler extends Handler {

	DecodeThread decodeThread = null;
	ScaningFragment activity = null;
	CaptureActivity activity1 = null;
	private State state;

	private enum State {
		PREVIEW, SUCCESS, DONE
	}

	public CaptureActivityHandler(ScaningFragment activity) {
		this.activity = activity;
		decodeThread = new DecodeThread(activity);
		decodeThread.start();
		state = State.SUCCESS;
		CameraManager.get().startPreview();
		restartPreviewAndDecode();
	}

	public CaptureActivityHandler(CaptureActivity activity) {
		this.activity1 = activity;
		decodeThread = new DecodeThread(activity1);
		decodeThread.start();
		state = State.SUCCESS;
		CameraManager.get().startPreview();
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {

		switch (message.what) {
		case R.id.auto_focus:
			if (state == State.PREVIEW) {
				CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
			}
			break;
		case R.id.restart_preview:
			restartPreviewAndDecode();
			break;
		case R.id.decode_succeeded:
			state = State.SUCCESS;
			if (activity != null) {
				activity.handleDecode((String) message.obj);// 解析成功，回调
			}else{
				activity1.handleDecode((String) message.obj);// 解析成功，回调
			}

			break;

		case R.id.decode_failed:
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					R.id.decode);
			break;
		}

	}

	public void quitSynchronously() {
		state = State.DONE;
		CameraManager.get().stopPreview();
		removeMessages(R.id.decode_succeeded);
		removeMessages(R.id.decode_failed);
		removeMessages(R.id.decode);
		removeMessages(R.id.auto_focus);
	}

	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					R.id.decode);
			CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
		}
	}

}
