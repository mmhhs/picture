package com.little.picture.util;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;

/**
 * 图片选取工具类
 */
public class ImageChooseUtil implements Serializable{
	private static final long serialVersionUID = 510488826187140949L;
	public static final int PHOTO_WITH_CAMERA = 116;//拍照
	public static final int CHOOSE_PICTURE = 117;//图库选取
	public static final int PHOTO_PICKED_WITH_CROP = 118;//裁剪

	public static int SCALE_WIDTH = 360;//缩放至的宽度
	public static int SCALE_HEIGHT = 640;//缩放至的高度
	public static int quality = 100;//图像质量
	private static String imagePathFolder = "";//存储图片的文件夹
	private static Uri imageUri;
	private  String imageUrl = "";//拍照存储原始路径
	private Activity activity;
	private Fragment fragment;
	private Context context;
	private MediaScannerConnection msc;

	/**
	 * 在Activity中使用
	 * @param activity
	 */
	public ImageChooseUtil(Activity activity){
		this.activity = activity;
		context = activity;
		imagePathFolder = context.getExternalFilesDir("")+"/cache/image/";
		File dir = new File(imagePathFolder);
		if(!dir.exists()){
			dir.mkdirs();
		}

	}

	/**
	 * 在fragment中使用
	 * @param fragment
	 */
	public ImageChooseUtil(Fragment fragment){
		this.fragment = fragment;
		context = fragment.getActivity();
		imagePathFolder = context.getExternalFilesDir("")+"/cache/image/";
		File dir = new File(imagePathFolder);
		if(!dir.exists()){
			dir.mkdirs();
		}
	}

	/**
	 * 获取拍照的照片原图路径
	 * @return
	 */
	public  String getTakePhotosUrl() {
		return imageUrl;
	}
	/**
	 * 获取拍照按比例压缩后的照片路径
	 * @return
	 */
	public  String getTakePhotoScaleUrl() {
		String result = "";
		try {
			result = ImageUtil.saveScaleImage(imageUrl,imagePathFolder,SCALE_WIDTH,SCALE_HEIGHT,quality);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			return result;
		}

	}

	/** 拍照获取相片 **/
	public  void doTakePhoto() {
		try {
			Intent intent = new Intent();
			intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE); //  MediaStore.ACTION_IMAGE_CAPTURE
			intent.putExtra("return-data", true); // 有返回值
			imageUrl = imagePathFolder+"image.jpg";
			imageUri = Uri.fromFile(new File(imageUrl));
			// 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			// 直接使用，没有缩小
			if (activity !=null){
				activity.startActivityForResult(intent, PHOTO_WITH_CAMERA); // 用户点击了从相机获取
			}else {
				if (fragment !=null){
					fragment.startActivityForResult(intent, PHOTO_WITH_CAMERA); // 用户点击了从相机获取
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/** 拍照获取多张相片 **/
	public  void doTakePhotos() {
		try {
			Intent intent = new Intent();
			intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra("return-data", true); // 有返回值
			imageUrl = imagePathFolder +""+System.currentTimeMillis()+".jpg";
			imageUri = Uri.fromFile(new File(imageUrl));
			// 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			// 直接使用，没有缩小
			if (activity !=null){
				activity.startActivityForResult(intent, PHOTO_WITH_CAMERA); // 用户点击了从相机获取
			}else {
				if (fragment !=null){
					fragment.startActivityForResult(intent, PHOTO_WITH_CAMERA); // 用户点击了从相机获取
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	/** 从相册获取图片 **/
	public void doGalleryPhoto() {
		try {
			Intent openAlbumIntent = doPickPhotoFromGallery();
			// openAlbumIntent.setType("image/*");
			if (activity !=null){
				activity.startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
			}else {
				if (fragment !=null){
					fragment.startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static Intent doPickPhotoFromGallery() {
		Intent intent = new Intent();
//		int apiLevel = PublicTools.getSystemVersion();
//		if(apiLevel>=19){
//			intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//		}else{
//			intent.setAction(Intent.ACTION_PICK);
//		}
		intent.setAction(Intent.ACTION_PICK);
		// 使用Intent.ACTION_GET_CONTENT这个Action  ACTION_PICK
		intent.setType("image/*"); // 获取任意图片类型
		intent.putExtra("return-data", true); // 有返回值

		return intent;

	}

	/**
	 * 裁剪图片
	 * @param data
	 */
	public void doCropPhoto(Uri data){
		try {
			Intent intent = getCropImageIntent(data);
			if (activity !=null){
				activity.startActivityForResult(intent, PHOTO_PICKED_WITH_CROP);
			}else {
				if (fragment !=null){
					fragment.startActivityForResult(intent, PHOTO_PICKED_WITH_CROP);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static Intent getCropImageIntent(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");//可裁剪
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("scale", true);
//		   intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);//保存到相册
		intent.putExtra("return-data", true);//若为false则表示不返回数据
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true);
		return intent;
	}

	/**
	 * 获取相册图片的Uri
	 * @param data
	 * @return
	 */
	public Uri getGalleryUri(Intent data){
		Uri originalUri = null;
		try {

			originalUri = data.getData();
//    		if(PublicTools.getSystemVersion()>=19){
//    			File file = new File(getPath(context, originalUri));
//    			originalUri = Uri.fromFile(file);
//    		}

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			return originalUri;
		}


	}

	/**
	 * 获取相册图片的原始资源地址
	 * @param data
	 * @return
	 */
	public String getGalleryUrl(Intent data){
		String result = "";
		try {
			ContentResolver resolver = null;
			if (activity !=null){
				resolver = activity.getContentResolver();
			}else {
				if (fragment !=null){
					resolver = fragment.getActivity().getContentResolver();
				}
			}
			if(data==null){
				return "";
			}
			// 照片的原始资源
			Uri originalUri = data.getData();
			if(originalUri!=null)
			{
//    			if(PublicTools.getSystemVersion()>=19){
//    				result = getPath(context, originalUri);
//    			}else{
				String[] proj = {MediaStore.Images.Media.DATA};
				Cursor cursor = resolver.query(originalUri, proj, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				if (cursor.moveToFirst()) {
					result = cursor.getString(column_index);
				}
				cursor.close();
//    			}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			return result;
		}

	}



	/**
	 * 获取拍照的照片的Uri
	 * @return
	 */
	public Uri getTakeUri(){

		return imageUri;
	}

	/**
	 * 获取裁剪后的bitmap
	 * @param data
	 * @return
	 */
	public Bitmap getCropBitmap(Intent data){
		Bitmap bitmap = null;
		try {
			bitmap = data.getParcelableExtra("data");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			return bitmap;
		}

	}



	/**
	 * 获取按比例压缩后的照片路径
	 * @param imageUrl
	 * @return
	 */
	public String getPictureScaleUrl(String imageUrl) {
		String result = "";
		try {
			result = ImageUtil.saveScaleImage(imageUrl,imagePathFolder,SCALE_WIDTH,SCALE_HEIGHT,quality);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			return result;
		}

	}



	/**
	 * 把文件插入到系统图库
	 * @param context
	 * @param file
	 * @param fileName
	 */
	public void save2DCIM(final Context context,File file,String fileName){
		try {
			final String result = MediaStore.Images.Media.insertImage(context.getContentResolver(),
					file.getAbsolutePath(), fileName, null);
			if (StringUtil.isEmpty(result)){
				return;
			}
			// 最后通知图库更新
//			context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
			msc = new MediaScannerConnection(context, new MediaScannerConnection.MediaScannerConnectionClient() {

				public void onMediaScannerConnected() {
					if (msc!=null)
						msc.scanFile(getFilePathByContentResolver(context,Uri.parse(result)), "image/jpeg");
				}

				public void onScanCompleted(String path, Uri uri) {
					if (msc!=null)
						msc.disconnect();
				}
			});
			msc.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 根据Uri获取图片路径
	 * @param context
	 * @param uri
	 * @return
	 */
	public static String getFilePathByContentResolver(Context context, Uri uri) {
		if (null == uri) {
			return null;
		}
		Cursor c = context.getContentResolver().query(uri, null, null, null, null);
		String filePath  = null;
		if (null == c) {
			throw new IllegalArgumentException(
					"Query on " + uri + " returns null result.");
		}
		try {
			if ((c.getCount() != 1) || !c.moveToFirst()) {
			} else {
				filePath = c.getString(
						c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
			}
		} finally {
			c.close();
		}
		return filePath;
	}


	/**
	 * 加载本地图片
	 * @param url
	 * @return
	 */
	public static Bitmap getLoacalBitmap(String url) {
		try {
			FileInputStream fis = new FileInputStream(url);
			return BitmapFactory.decodeStream(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 截取图片的中间的200X200的区域
	 * @param bm
	 *
	 * @return
	 */
	public static  Bitmap cropCenter(Bitmap bm,int width,int height)
	{
		int dstWidth = width;
		int dstHeight = height;
		int startWidth = (bm.getWidth() - dstWidth)/2;
		int startHeight = ((bm.getHeight() - dstHeight) / 2);
		Rect src = new Rect(startWidth, startHeight, startWidth + dstWidth, startHeight + dstHeight);
		return dividePart(bm, src);
	}

	/**
	 * 剪切图片
	 * @param bmp 被剪切的图片
	 * @param src 剪切的位置
	 * @return 剪切后的图片
	 */
	public static  Bitmap dividePart(Bitmap bmp, Rect src)
	{
		int width = src.width();
		int height = src.height();
		Rect des = new Rect(0, 0, width, height);
		Bitmap croppedImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(croppedImage);
		canvas.drawBitmap(bmp, src, des, null);
		return croppedImage;
	}

	//4.4以上处理图库选择图片
	public static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] {
						split[1]
				};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return "";
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
									   String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}


	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}

	public static int getScaleWidth() {
		return SCALE_WIDTH;
	}

	public static void setScaleWidth(int scaleWidth) {
		SCALE_WIDTH = scaleWidth;
	}

	public static int getScaleHeight() {
		return SCALE_HEIGHT;
	}

	public static void setScaleHeight(int scaleHeight) {
		SCALE_HEIGHT = scaleHeight;
	}

	public static int getQuality() {
		return quality;
	}

	public static void setQuality(int quality) {
		ImageChooseUtil.quality = quality;
	}

	public static String getImagePathFolder() {
		return imagePathFolder;
	}

	public static void setImagePathFolder(String imagePathFolder) {
		ImageChooseUtil.imagePathFolder = imagePathFolder;
	}
}