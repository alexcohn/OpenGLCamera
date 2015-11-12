package com.example.openglcamera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.method.Touch;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

//import com.nepalimutu.pujanpaudel.app.priceoverrrflow.FragmentsCorner.BaseImagesContainer;
//import com.nepalimutu.pujanpaudel.app.priceoverrrflow.UltimateCamera.DialogHelper;
//import com.soundcloud.android.crop.Crop;

public class CameraPreview
		implements
		SurfaceHolder.Callback {
	public static CameraPreview reference;
	private int cameratype=Camera.CameraInfo.CAMERA_FACING_BACK;
	private Camera mCamera = null;
	public Camera.Parameters params;
	private SurfaceHolder sHolder;
	private String TAG="CameraPreview";
	public List<Camera.Size> supportedSizes;

	private boolean isCamOpen = false;
	private boolean isSurfaceReady = false;
	public boolean isSizeSupported = false;
	private int previewWidth, previewHeight;
	private List<String> mSupportedFlashModes;
	private boolean flashon=false;
	private final static String MYTAG = "CameraPreview";
	private ProgressDialog loading;
	private CameraHandlerThread mThread;
	private String LOG_TAG="Camera_Preview";

	public CameraPreview(int width, int height) {
		Log.i("campreview", "Width = " + String.valueOf(width));
		Log.i("campreview", "Height = " + String.valueOf(height));
		previewWidth = width;
		previewHeight = height;
		reference=this;
	}

	private boolean oldOpenCamera() {
		if (isCamOpen) {
			releaseCamera();
		}
		try{
			mCamera = Camera.open(cameratype);
			isCamOpen = true;
		}catch (Exception e){
			e.printStackTrace();
			Toast.makeText(TouchActivity.reference.getActivity(),"Can't Open the Camera",Toast.LENGTH_LONG).show();
		}
		startPreviewIfReady();
		return isCamOpen;
	}

	public boolean isCamOpen() {
		return isCamOpen;
	}

	public void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
		isCamOpen = false;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		sHolder = holder;
		newOpenCamera();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
	                           int height) {
		Log.d("Inside", "Surface Changed");
		isSurfaceReady = true;
		startPreviewIfReady();
	}

	private synchronized void startPreviewIfReady() {
		if (!isSurfaceReady || !isCamOpen) {
			return;
		}
		if (TouchActivity.reference.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {

			mCamera.setDisplayOrientation(90);

		} else {

			mCamera.setDisplayOrientation(0);

		}
		try{
			mCamera.setPreviewDisplay(sHolder);
			mCamera.startPreview();
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		releaseCamera();

	}

	/**
	 * Called from PreviewSurfaceView to set touch focus.
	 *
	 * @param - Rect - new area for auto focus
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void doTouchFocus(final Rect tfocusRect) {
		Log.i(TAG, "TouchFocus");
		try {
			final List<Camera.Area> focusList = new ArrayList<Camera.Area>();
			Camera.Area focusArea = new Camera.Area(tfocusRect, 1000);
			focusList.add(focusArea);

			Camera.Parameters para = mCamera.getParameters();
			para.setFocusAreas(focusList);
			para.setMeteringAreas(focusList);
			mCamera.setParameters(para);

			mCamera.autoFocus(myAutoFocusCallback);
		} catch (Exception e) {
			e.printStackTrace();
			Log.i(TAG, "Unable to autofocus");
		}

	}

	/**
	 * AutoFocus callback
	 */
	AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){

		@Override
		public void onAutoFocus(boolean arg0, Camera arg1) {
			if (arg0){
				mCamera.cancelAutoFocus();
			}
		}
	};




	public void capturePicture(){
		mCamera.takePicture(null, null, mPicture);


	}

	private File getOutputMediaFile(){

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "UltimateCameraGuideApp");

		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d("Camera Guide", "Required media storage does not exist");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		mediaFile = new File(mediaStorageDir.getPath() + File.separator +
				"IMG_"+ timeStamp + ".jpg");

		//DialogHelper.showDialog("Success!", "Your picture has been saved!", TouchActivity.reference.getActivity());

		return mediaFile;
	}

	private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			//This One is Just for Getting a File Named after it
			loading=new ProgressDialog(TouchActivity.reference.getActivity()); // BaseImagesContainer.reference);
			loading.setMessage("Getting Image Ready");
			loading.show();
			File pictureFile =getOutputMediaFile();
			if (pictureFile == null){
				Toast.makeText(TouchActivity.reference.getActivity(), "Image retrieval failed.", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
			if(cameratype==Camera.CameraInfo.CAMERA_FACING_BACK){
				bmp=rotateImage(90,bmp);
			}else{
				bmp=rotateImage(270,bmp);

			}
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.PNG,1, stream);
			byte[] flippedImageByteArray = stream.toByteArray();
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(flippedImageByteArray);
				fos.close();
				// Restart the camera preview.
				//safeCameraOpenInView(mCameraView);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Uri destination = Uri.fromFile(new File(TouchActivity.reference.getActivity().getCacheDir(), "cropped"));
			Uri source = Uri.fromFile(new File(pictureFile.getPath()));
//			Crop.of(source, destination).withMaxSize(800,800).start(TouchActivity.reference.getActivity());
		}
	};


	public Bitmap rotateImage(int angle, Bitmap bitmapSrc) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(bitmapSrc, 0, 0,
				bitmapSrc.getWidth(), bitmapSrc.getHeight(), matrix, true);
	}


	public void switchCamera(){
		mCamera.stopPreview();
		//NB: if you don't release the current camera before switching, you app will crash
		mCamera.release();

		//swap the id of the camera to be used
		if(cameratype==Camera.CameraInfo.CAMERA_FACING_BACK){
			cameratype=Camera.CameraInfo.CAMERA_FACING_FRONT;
		}else{
			cameratype=Camera.CameraInfo.CAMERA_FACING_BACK;
		}
		try{
			mCamera = Camera.open(cameratype);
		}catch (Exception e){
			e.printStackTrace();
			Toast.makeText(TouchActivity.reference.getActivity(),"Can't Open the Camera",Toast.LENGTH_LONG).show();
		}

		if (TouchActivity.reference.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {

			mCamera.setDisplayOrientation(90);

		} else {

			mCamera.setDisplayOrientation(0);

		}

		try{
			mCamera.setPreviewDisplay(sHolder);
			mCamera.startPreview();
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

	public void switchflash(){
		//Do the On Flash for now
		if(!flashon){
			mSupportedFlashModes = mCamera.getParameters().getSupportedFlashModes();
			if (mSupportedFlashModes != null && mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)){
				Camera.Parameters parameters = mCamera.getParameters();
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
				mCamera.setParameters(parameters);
			}
		}else{
			//flash on
			//do teh off now
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(parameters);
		}
		flashon=!flashon;

	}

	public void stopLoading(){
		loading.dismiss();
		//DialogHelper.showDialog("Oops!", "Your crop had been cancelled !", TouchActivity.reference.getActivity());

	}


	private void newOpenCamera() {
		if (mThread == null) {
			mThread = new CameraHandlerThread();
		}

		synchronized (mThread) {
			mThread.openCamera();
		}
	}
	private  class CameraHandlerThread extends HandlerThread {
		Handler mHandler = null;

		CameraHandlerThread() {
			super("CameraHandlerThread");
			start();
			mHandler = new Handler(getLooper());
		}

		synchronized void notifyCameraOpened() {
			//notify();
			if(mCamera==null){
				Log.d("Notify","Null Camera");
			}else {
				Log.d("Notify","Yess!!  Camera");
			}
		}

		void openCamera() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					oldOpenCamera();
					notifyCameraOpened();
				}
			});
		}
	}
}