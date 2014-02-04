package com.example.openglcamera;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

public class SurfaceTextureActivity extends Activity implements TextureView.SurfaceTextureListener, OnClickListener {

private static final int CAMERA_PIC_REQUEST = 0;
private Camera mCamera;
private TextureView mTextureView;

protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mTextureView = new TextureView(this);
    mTextureView.setSurfaceTextureListener(this);
    mTextureView.setOnClickListener(this);

    setContentView(mTextureView);
}

@Override
public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

	int cameraId = 0;
	Camera.CameraInfo info = new Camera.CameraInfo();

	for (cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
		Camera.getCameraInfo(cameraId, info);
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
			break;
	}

	mCamera = Camera.open(cameraId);
    Matrix transform = new Matrix();

    Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
    int rotation = getWindowManager().getDefaultDisplay()
            .getRotation();
    
    Log.i("ST", "onSurfaceTextureAvailable(): CameraOrientation(" + cameraId + ")" + info.orientation + " " + previewSize.width + "x" + previewSize.height + " Rotation=" + rotation);

    switch (rotation) {
    case Surface.ROTATION_0: 
        mCamera.setDisplayOrientation(90);
    	mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
    			previewSize.height, previewSize.width, Gravity.CENTER));
        transform.setScale(-1, 1, previewSize.height/2, 0);
    	break;

    case Surface.ROTATION_90:
        mCamera.setDisplayOrientation(0);
    	mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
        		previewSize.width, previewSize.height, Gravity.CENTER));
        transform.setScale(-1, 1, previewSize.width/2, 0);
    	break;
    	
    case Surface.ROTATION_180:
    	mCamera.setDisplayOrientation(270);
    	mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
    			previewSize.height, previewSize.width, Gravity.CENTER));
        transform.setScale(-1, 1, previewSize.height/2, 0);
    	break;

    case Surface.ROTATION_270:
        mCamera.setDisplayOrientation(180);
    	mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
        		previewSize.width, previewSize.height, Gravity.CENTER));
        transform.setScale(-1, 1, previewSize.width/2, 0);
    	break;
    }
    
    try {
        mCamera.setPreviewTexture(surface);
    } catch (IOException t) {
    }
    
    mTextureView.setTransform(transform);
    Log.i("ST", "onSurfaceTextureAvailable(): Transform: " + transform.toString());

    mCamera.startPreview();

}

@Override
public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    // Ignored, the Camera does all the work for us
}

@Override
public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
	if (mCamera != null) { 
	    mCamera.stopPreview();
	    mCamera.release();
	    mCamera = null;
	}
    return true;
}

@Override
public void onPause() {
	onSurfaceTextureDestroyed(null);
	super.onPause();
}

private long ts = 0;
private long adts = 0;
private long nts = 1;

@Override
public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//	Log.i("ST", "onSurfaceTextureUpdated()");
	long newTs = surface.getTimestamp();
	if (ts != 0)
	{
		adts += newTs - ts;
		nts++;
	}
	ts = newTs;

	if (nts % 30 == 0)
	{
		Log.i("ST", "onSurfaceTextureUpdated(): average dts " + (adts*1.e-6/nts));
		adts = 0;
		nts = 0;
	}
}

@Override
public void onClick(View arg0) {
	Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	startActivityForResult(intent, CAMERA_PIC_REQUEST);
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CAMERA_PIC_REQUEST) {
    	Log.i("ST", "onActivityResult(" + resultCode + ")");
    }
}

}