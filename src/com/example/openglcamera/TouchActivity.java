package com.example.openglcamera;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;


public class TouchActivity extends Activity {

	static TouchActivity reference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		reference = this;
		SurfaceView sv = new SurfaceView(this);
		sv.getHolder().addCallback(new CameraPreview(640, 480));
		setContentView(sv);
	}

	TouchActivity getActivity() {
		return this;
	}
}
