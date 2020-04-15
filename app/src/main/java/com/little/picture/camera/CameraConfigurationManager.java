/*
 * Copyright (C) 2010 ZXing authors
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
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.fos.fosmvp.common.utils.LogUtils;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * Camera参数配置
 */
final class CameraConfigurationManager {

	private static final String TAG = "CameraConfiguration";

	private final Context context;
	private Point screenResolution;
	private Point cameraResolution;

	private static final int MIN_PREVIEW_PIXELS = 480 * 320;
	private static final double MAX_ASPECT_DISTORTION = 0.15;
	private static final int TEN_DESIRED_ZOOM = 27;
	private static final Pattern COMMA_PATTERN = Pattern.compile(",");

	CameraConfigurationManager(Context context) {
		this.context = context;
	}

	/**
	 * Reads, one time, values from the camera that are needed by the app.
	 */
	@SuppressLint("NewApi")
	void initFromCameraParameters(Camera camera, int previewWidth, int previewHeight) {
		Camera.Parameters parameters = camera.getParameters();
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
//		Point theScreenResolution = new Point(display.getWidth(),
//				display.getHeight());
		Point theScreenResolution = new Point(previewWidth,
				previewHeight);
		screenResolution = theScreenResolution;
		LogUtils.e("CameraConfigurationManager screenResolution.x="+screenResolution.x+" y="+screenResolution.y);

		Log.i(TAG, "Screen resolution: " + screenResolution);

		/************** 竖屏更改4 ******************/
		Point screenResolutionForCamera = new Point();
		screenResolutionForCamera.x = screenResolution.x;
		screenResolutionForCamera.y = screenResolution.y;
		// preview size is always something like 480*320, other 320*480
		if (screenResolution.x < screenResolution.y) {
			screenResolutionForCamera.x = screenResolution.y;
			screenResolutionForCamera.y = screenResolution.x;
		}
		LogUtils.e("CameraConfigurationManager screenResolutionForCamera.x="+screenResolutionForCamera.x+" y="+screenResolutionForCamera.y);

		cameraResolution = CameraConfigurationUtils.findBestPreviewSizeValue(
				parameters, screenResolutionForCamera);

		LogUtils.e("CameraConfigurationManager cameraResolution.x="+cameraResolution.x+" y="+cameraResolution.y);
		Log.i(TAG, "Camera resolution: " + cameraResolution);

	}

	void setDesiredCameraParameters(Camera camera, boolean safeMode) {
		Camera.Parameters parameters = camera.getParameters();

		if (parameters == null) {
			Log.w(TAG,
					"Device error: no camera parameters are available. Proceeding without configuration.");
			return;
		}

		Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

		if (safeMode) {
			Log.w(TAG,
					"In camera config safe mode -- most settings will not be honored");
		}

		CameraConfigurationUtils.setFocus(parameters, true, true, safeMode);



		parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
		/****************** 竖屏更改2 *********************/
		setDisplayOrientation(camera, 90);

		Log.i(TAG, "Final camera parameters: " + parameters.flatten());

		setZoom(parameters);
		camera.setParameters(parameters);

		Camera.Parameters afterParameters = camera.getParameters();
		Camera.Size afterSize = afterParameters.getPreviewSize();
		if (afterSize != null
				&& (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
			Log.w(TAG, "Camera said it supported preview size "
					+ cameraResolution.x + 'x' + cameraResolution.y
					+ ", but after setting it, preview size is "
					+ afterSize.width + 'x' + afterSize.height);
			cameraResolution.x = afterSize.width;
			cameraResolution.y = afterSize.height;
		}
	}

	void setDisplayOrientation(Camera camera, int angle) {

		Method method;
		try {
			method = camera.getClass().getMethod("setDisplayOrientation",
					new Class[] { int.class });
			if (method != null)
				method.invoke(camera, new Object[] { angle });
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	Point getCameraResolution() {
		return cameraResolution;
	}

	Point getScreenResolution() {
		return screenResolution;
	}


	private void setZoom(Camera.Parameters parameters) {

		String zoomSupportedString = parameters.get("zoom-supported");
		if (zoomSupportedString != null && !Boolean.parseBoolean(zoomSupportedString)) {
			return;
		}

		int tenDesiredZoom = TEN_DESIRED_ZOOM;

		String maxZoomString = parameters.get("max-zoom");
		if (maxZoomString != null) {
			try {
				int tenMaxZoom = (int) (10.0 * Double.parseDouble(maxZoomString));
				if (tenDesiredZoom > tenMaxZoom) {
					tenDesiredZoom = tenMaxZoom;
				}
			} catch (NumberFormatException nfe) {
				Log.e(TAG, "Bad max-zoom: " + maxZoomString);
			}
		}

		String takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max");
		if (takingPictureZoomMaxString != null) {
			try {
				int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
				if (tenDesiredZoom > tenMaxZoom) {
					tenDesiredZoom = tenMaxZoom;
				}
			} catch (NumberFormatException nfe) {
				Log.e(TAG, "Bad taking-picture-zoom-max: " + takingPictureZoomMaxString);
			}
		}

		String motZoomValuesString = parameters.get("mot-zoom-values");
		if (motZoomValuesString != null) {
			tenDesiredZoom = findBestMotZoomValue(motZoomValuesString, tenDesiredZoom);
		}

		String motZoomStepString = parameters.get("mot-zoom-step");
		if (motZoomStepString != null) {
			try {
				double motZoomStep = Double.parseDouble(motZoomStepString.trim());
				int tenZoomStep = (int) (10.0 * motZoomStep);
				if (tenZoomStep > 1) {
					tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
				}
			} catch (NumberFormatException nfe) {
				// continue
			}
		}

		// Set zoom. This helps encourage the user to pull back.
		// Some devices like the Behold have a zoom parameter
		// if (maxZoomString != null || motZoomValuesString != null) {
		// parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
		// }
		if (parameters.isZoomSupported()) {
			Log.e(TAG, "max-zoom:" + parameters.getMaxZoom());
			Log.i("0000", "tenDesiredZoom:" + tenDesiredZoom);
			parameters.setZoom(parameters.getMaxZoom() / tenDesiredZoom);
		} else {
			Log.e(TAG, "Unsupported zoom.");
		}

		// Most devices, like the Hero, appear to expose this zoom parameter.
		// It takes on values like "27" which appears to mean 2.7x zoom
		// if (takingPictureZoomMaxString != null) {
		// parameters.set("taking-picture-zoom", tenDesiredZoom);
		// }
	}

	private static int findBestMotZoomValue(CharSequence stringValues, int tenDesiredZoom) {
		int tenBestValue = 0;
		for (String stringValue : COMMA_PATTERN.split(stringValues)) {
			stringValue = stringValue.trim();
			double value;
			try {
				value = Double.parseDouble(stringValue);
			} catch (NumberFormatException nfe) {
				return tenDesiredZoom;
			}
			int tenValue = (int) (10.0 * value);
			if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom - tenBestValue)) {
				tenBestValue = tenValue;
			}
		}
		return tenBestValue;
	}
}
