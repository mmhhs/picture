/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.little.picture.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;


import com.fos.fosmvp.common.utils.LogUtils;

import java.io.IOException;

/**
 * This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CameraManager {

	private static final String TAG = CameraManager.class.getSimpleName();

	private static final int MIN_FRAME_WIDTH = 240;
	private static final int MIN_FRAME_HEIGHT = 240;
	private static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
	private static final int MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080

	private final Context context;
	private final CameraConfigurationManager configManager;
	public Camera mCamera;
	private AutoFocusManager autoFocusManager;
	private Rect framingRect;
	private Rect framingRectInPreview;
	private boolean initialized;
	private boolean previewing;
	private int requestedCameraId = -1;
	private int requestedFramingRectWidth;
	private int requestedFramingRectHeight;
	/**
	 * Preview frames are delivered here, which we pass on to the registered
	 * handler. Make sure to clear the handler so it will only receive one
	 * message.
	 */
	private final PreviewCallback previewCallback;

	private float frameWidth = 0.8f;//取景框宽度
	private float frameHeight = 0.8f;//取景框高度

	public CameraManager(Context context) {
		this.context = context;
		this.configManager = new CameraConfigurationManager(context);
		previewCallback = new PreviewCallback(configManager);
	}

	/**
	 * Opens the mCamera driver and initializes the hardware parameters.
	 * 
	 * @param holder
	 *            The surface object which the mCamera will draw preview frames
	 *            into.
	 * @throws IOException
	 *             Indicates the mCamera driver failed to open.
	 */
	public synchronized void openDriver(SurfaceHolder holder, int previewWidth, int previewHeight)
			throws IOException {

		if (mCamera == null) {

			if (requestedCameraId >= 0) {
				LogUtils.e("OpenCameraInterface requestedCameraId= "+requestedCameraId);
				mCamera = OpenCameraInterface.open(requestedCameraId);

			} else {
				mCamera = OpenCameraInterface.open();
			}

			if (mCamera == null) {
				throw new IOException();
			}
		}
		mCamera.setPreviewDisplay(holder);

		LogUtils.e("------openDriver= "+ mCamera);
		if (!initialized) {
			initialized = true;
			configManager.initFromCameraParameters(mCamera,previewWidth,previewHeight);
			if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
				setManualFramingRect(requestedFramingRectWidth,
						requestedFramingRectHeight);
				requestedFramingRectWidth = 0;
				requestedFramingRectHeight = 0;
			}
		}

		Camera.Parameters parameters = mCamera.getParameters();
		String parametersFlattened = parameters == null ? null : parameters
				.flatten(); // Save these, temporarily
		try {
			configManager.setDesiredCameraParameters(mCamera, false);
		} catch (RuntimeException re) {
			// Driver failed
			Log.w(TAG,
					"Camera rejected parameters. Setting only minimal safe-mode parameters");
			Log.i(TAG, "Resetting to saved mCamera params: "
					+ parametersFlattened);
			// Reset:
			if (parametersFlattened != null) {
				parameters = mCamera.getParameters();
				parameters.unflatten(parametersFlattened);
				try {
					mCamera.setParameters(parameters);
					configManager.setDesiredCameraParameters(mCamera, true);
				} catch (RuntimeException re2) {
					// Well, darn. Give up
					Log.w(TAG,
							"Camera rejected even safe-mode parameters! No configuration");
				}
			}
		}

	}

	public synchronized boolean isOpen() {
		LogUtils.e("----- isOpen mCamera= "+ mCamera);
		if (mCamera!=null){
			return true;
		}else {
			return false;
		}
	}

	/**
	 * Closes the mCamera driver if still in use.
	 */
	public synchronized void closeDriver() {
		if (mCamera != null) {
			LogUtils.e("--------closeDriver---------");
			mCamera.release();
			mCamera = null;
			// Make sure to clear these each time we close the mCamera, so that
			// any scanning rect
			// requested by intent is forgotten.
			framingRect = null;
			framingRectInPreview = null;
		}
	}

	/**
	 * Asks the mCamera hardware to begin drawing preview frames to the screen.
	 */
	public synchronized void startPreview() {
		if (mCamera != null && !previewing) {
			mCamera.startPreview();
			previewing = true;
			autoFocusManager = new AutoFocusManager(context, mCamera);
		}
	}

	/**
	 * Tells the mCamera to stop drawing preview frames.
	 */
	public synchronized void stopPreview() {
		if (autoFocusManager != null) {
			autoFocusManager.stop();
			autoFocusManager = null;
		}
		if (mCamera != null && previewing) {
			LogUtils.e("--------stopPreview---------");
			mCamera.stopPreview();
			previewCallback.setHandler(null, 0);
			previewing = false;
		}
	}

	/**
	 * A single preview frame will be returned to the handler supplied. The data
	 * will arrive as byte[] in the message.obj field, with width and height
	 * encoded as message.arg1 and message.arg2, respectively.
	 * 
	 * @param handler
	 *            The handler to send the message to.
	 * @param message
	 *            The what field of the message to be sent.
	 */
	public synchronized void requestPreviewFrame(Handler handler, int message) {
		Camera theCamera = mCamera;
		if (theCamera != null && previewing) {
			previewCallback.setHandler(handler, message);
			theCamera.setOneShotPreviewCallback(previewCallback);
		}
	}

	/**
	 * Calculates the framing rect which the UI should draw to show the user
	 * where to place the barcode. This target helps with alignment as well as
	 * forces the user to hold the device far enough away to ensure the image
	 * will be in focus. 计算这个条形码的扫描框；便于声明的同时，也强制用户通过改变距离来扫描到整个条形码
	 * 
	 * @return The rectangle to draw on screen in window coordinates.
	 */
	public synchronized Rect getFramingRect() {
		if (framingRect == null) {
			if (mCamera == null) {
				return null;
			}
			Point screenResolution = configManager.getScreenResolution();
			if (screenResolution == null) {
				// Called early, before init even finished
				return null;
			}

			//调整扫描框大小
			int width = (int)(findDesiredDimensionInRange(screenResolution.x,
					MIN_FRAME_WIDTH, MAX_FRAME_WIDTH)*frameWidth);
			int height = (int)(findDesiredDimensionInRange(screenResolution.y,
					MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT)*frameHeight);

			int leftOffset = (screenResolution.x - width) / 2;
			int topOffset = (screenResolution.y - height) / 2;
			framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
					topOffset + height);
			Log.d(TAG, "Calculated framing rect: " + framingRect);
		}else {
			Point screenResolution = configManager.getScreenResolution();
			int height = (int)(findDesiredDimensionInRange(screenResolution.y,
					MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT)*frameHeight);
			if (framingRect.height()!=height){
				framingRect = null;
				framingRectInPreview = null;
			}

		}
		return framingRect;
	}

	public void setFrameWidth(float frameWidth) {
		this.frameWidth = frameWidth;
	}

	public void setFrameHeight(float frameHeight) {
		this.frameHeight = frameHeight;
	}

	public float getFrameWidth() {
		return frameWidth;
	}

	public float getFrameHeight() {
		return frameHeight;
	}

	private static int findDesiredDimensionInRange(int resolution, int hardMin,
												   int hardMax) {
		int dim = 5 * resolution / 8; // Target 5/8 of each dimension
		if (dim < hardMin) {
			return hardMin;
		}
		if (dim > hardMax) {
			return hardMax;
		}
		return dim;
	}

	/**
	 * Like {@link #getFramingRect} but coordinates are in terms of the preview
	 * frame, not UI / screen.
	 * 
	 * @return {@link Rect} expressing barcode scan area in terms of the preview
	 *         size
	 */
	public synchronized Rect getFramingRectInPreview() {
		if (framingRectInPreview == null) {
			Rect framingRect = getFramingRect();
			if (framingRect == null) {
				return null;
			}
			Rect rect = new Rect(framingRect);
			Point cameraResolution = configManager.getCameraResolution();
			Point screenResolution = configManager.getScreenResolution();
			if (cameraResolution == null || screenResolution == null) {
				// Called early, before init even finished
				return null;
			}

			/******************** 竖屏更改1(cameraResolution.x/y互换) ************************/
			rect.left = rect.left * cameraResolution.y / screenResolution.x;
			rect.right = rect.right * cameraResolution.y / screenResolution.x;
			rect.top = rect.top * cameraResolution.x / screenResolution.y;
			rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;
			framingRectInPreview = rect;
		}
		return framingRectInPreview;
	}

	/**
	 * Allows third party apps to specify the mCamera ID, rather than determine
	 * it automatically based on available cameras and their orientation.
	 * 
	 * @param cameraId
	 *            mCamera ID of the mCamera to use. A negative value means
	 *            "no preference".
	 */
	public synchronized void setManualCameraId(int cameraId) {
		requestedCameraId = cameraId;
	}

	/**
	 * Allows third party apps to specify the scanning rectangle dimensions,
	 * rather than determine them automatically based on screen resolution.
	 * 
	 * @param width
	 *            The width in pixels to scan.
	 * @param height
	 *            The height in pixels to scan.
	 */
	public synchronized void setManualFramingRect(int width, int height) {
		if (initialized) {
			Point screenResolution = configManager.getScreenResolution();
			if (width > screenResolution.x) {
				width = screenResolution.x;
			}
			if (height > screenResolution.y) {
				height = screenResolution.y;
			}
			int leftOffset = (screenResolution.x - width) / 2;
			int topOffset = (screenResolution.y - height) / 2;
			framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
					topOffset + height);
			Log.d(TAG, "Calculated manual framing rect: " + framingRect);
			framingRectInPreview = null;
		} else {
			requestedFramingRectWidth = width;
			requestedFramingRectHeight = height;
		}
	}



	public Camera getmCamera() {
		return mCamera;
	}

	public void setmCamera(Camera mCamera) {
		this.mCamera = mCamera;
	}

	private static final int FRONT = 1;//前置摄像头标记
	private static final int BACK = 2;//后置摄像头标记
	private int currentCameraType = 2;//当前打开的摄像头标记

	public boolean checkCamera(Activity activity){
		return activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	@SuppressLint("NewApi")
	private Camera openCamera(int type){
		int frontIndex =-1;//前置摄像头的ID
		int backIndex = -1;//后置摄像头的ID
		int cameraCount = Camera.getNumberOfCameras();
		Camera.CameraInfo info = new Camera.CameraInfo();
		for(int cameraIndex = 0; cameraIndex<cameraCount; cameraIndex++){
			Camera.getCameraInfo(cameraIndex, info);
			if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
				frontIndex = cameraIndex;
			}else if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
				backIndex = cameraIndex;
			}
		}

		currentCameraType = type;
		if(type == FRONT && frontIndex != -1){
			requestedCameraId = frontIndex;
			return Camera.open(frontIndex);
		}else if(type == BACK && backIndex != -1){
			requestedCameraId = backIndex;
			return Camera.open(backIndex);
		}
		return null;
	}

	public void changeCamera(CameraPreview cameraPreview){
		try {
			mCamera.stopPreview();
			mCamera.release();
			if(currentCameraType == FRONT){
				mCamera = openCamera(BACK);
			}else if(currentCameraType == BACK){
				mCamera = openCamera(FRONT);
			}
			mCamera.setPreviewDisplay(cameraPreview.getHolder());
			mCamera.startPreview();
		}catch (Exception e){
			e.printStackTrace();
		}

	}
}
