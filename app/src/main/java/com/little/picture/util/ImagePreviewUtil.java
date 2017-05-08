package com.little.picture.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.little.picture.PicturePickActivity;
import com.little.picture.R;
import com.little.picture.adapter.PictureFolderAdapter;
import com.little.picture.adapter.PictureGridAdapter;
import com.little.picture.adapter.PicturePreviewAdapter;
import com.little.picture.listener.IOnCheckListener;
import com.little.picture.listener.IOnDeleteListener;
import com.little.picture.listener.IOnGestureListener;
import com.little.picture.listener.IOnItemClickListener;
import com.little.picture.model.ImageEntity;
import com.little.picture.view.ClipImageLayout;
import com.little.picture.view.PageIndicatorView;

import java.util.ArrayList;
import java.util.List;

/**
 * 大图预览
 */
public class ImagePreviewUtil {
    public static final int PREVIE_GRIDLIST = 3001;//小缩略图列表
    public static final int PREVIEW_FOLDER = 3002;//文件夹内所有图片大图预览
    public static final int PREVIEW_CHOOSE = 3003;//选中图片大图预览
    public static final int PREVIEW_EDIT = 3004;//编辑界面大图预览 裁剪
    public static final int PREVIEW_TAKE = 3005;//拍照结果大图预览

    private Context context;
    private List<String> imageList;//图片集合
    private View contentView;//承载视图
    private int  imageIndex = 0;//当前图片索引
    private boolean showPreviewTitle = true;//是否显示标题
    private boolean showDelete = true;//是否显示删除按钮
    private boolean showDotIndex = false;//是否显示圆点索引
    private PicturePreviewAdapter picturePreviewAdapter;
    private IOnDeleteListener onDeleteListener;//删除监听
    private int statusBarHeight;//状态栏高度

    private IOnItemClickListener onItemClickListener;
    private List<ImageEntity> folderImageEntityList;

    private String previewPath = "";
    private ArrayList<String> chooseImageList;
    private PictureGridAdapter pictureGridAdapter;
    private int maxSize = 9;//最多能选择的图片数
    private IOnCheckListener onCheckListener;
    private Activity activity;
    private boolean isOriginal = false;//是否使用原图

    /**
     * 构造器
     * @param context 上下文
     * @param view 承载视图
     */
    public ImagePreviewUtil(Context context, View view) {
        this.context = context;
        this.contentView = view;
    }

    /**
     * 显示大图预览
     * @param index 默认显示第几张
     * @return
     */
    public PopupWindow showImagePreview(int index, List<String> imageList){
        this.imageList = imageList;
        PopupWindow popupWindow = getPreviewWindow(context, index);
        popupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);
        return popupWindow;
    }

    public PopupWindow getPreviewWindow(final Context context,int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.picture_popup_preview,null, false);
        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.picture_popup_preview_viewPager);
        final PageIndicatorView pageIndicatorView = (PageIndicatorView) view.findViewById(R.id.picture_popup_preview_pageIndicatorView);
        final LinearLayout titleLayout = (LinearLayout) view.findViewById(R.id.picture_ui_title_layout);
        final LinearLayout footerLayout = (LinearLayout) view.findViewById(R.id.picture_ui_footer_layout);
        LinearLayout backLayout = (LinearLayout) view.findViewById(R.id.picture_ui_title_back_layout);
        final RelativeLayout containerLayout = (RelativeLayout) view.findViewById(R.id.picture_popup_preview_layout);
        final TextView doneText = (TextView) view.findViewById(R.id.picture_ui_title_done);
        final LinearLayout deleteLayout = (LinearLayout) view.findViewById(R.id.picture_ui_title_delete_layout);
        final TextView indexText = (TextView) view.findViewById(R.id.picture_ui_title_index);
        picturePreviewAdapter = new PicturePreviewAdapter(context, imageList);
        picturePreviewAdapter.setOnGestureListener(new IOnGestureListener() {
            @Override
            public void onClick() {
                if (showPreviewTitle) {
                    titleLayout.setVisibility(View.GONE);
                    showPreviewTitle = false;
                } else {
                    titleLayout.setVisibility(View.VISIBLE);
                    showPreviewTitle = true;
                }
            }

            @Override
            public void onDoubleClick() {

            }

            @Override
            public void onLongPress() {

            }
        });
        viewPager.setAdapter(picturePreviewAdapter);
        if (showDelete){
            deleteLayout.setVisibility(View.VISIBLE);
        }else {
            deleteLayout.setVisibility(View.GONE);
        }
        if (showDotIndex){
            pageIndicatorView.setVisibility(View.VISIBLE);
        }else {
            pageIndicatorView.setVisibility(View.GONE);
        }
        footerLayout.setVisibility(View.GONE);
        doneText.setVisibility(View.GONE);
        if (imageList.size()>position){
            indexText.setText(""+(position+1)+"/"+ imageList.size());
            imageIndex = position;
            viewPager.setCurrentItem(position);
        }
        pageIndicatorView.setPageTotal(imageList.size());
        pageIndicatorView.setPageSelect(imageIndex);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                indexText.setText("" + (position + 1) + "/" + imageList.size());
                imageIndex = position;
                pageIndicatorView.setPageSelect(imageIndex);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    int[] xy = {1,1};
                    containerLayout.getLocationOnScreen(xy);
                    if (xy[1]<statusBarHeight){
                        containerLayout.setPadding(0, statusBarHeight - xy[1], 0, 0);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, 100);
        final PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable()); //使按返回键能够消失
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                showPreviewTitle = true;
            }
        });
        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
//                alertDialog.setIcon(R.drawable.icon_dialog);
//                alertDialog.setTitle("提示");
                alertDialog.setMessage(context.getString(R.string.picture_delete));
                alertDialog.setPositiveButton(context.getString(R.string.picture_confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                imageList.remove(imageIndex);
                                picturePreviewAdapter.notifyDataSetChanged();
                                if (imageList.size() == 0) {
                                    popupWindow.dismiss();
                                } else {
                                    if ((imageIndex) < imageList.size()) {

                                    } else {
                                        imageIndex = imageIndex - 1;
                                    }
                                    indexText.setText("" + (imageIndex + 1) + "/" + imageList.size());
                                    viewPager.setCurrentItem(imageIndex);
                                }
                                if (onDeleteListener != null) {
                                    onDeleteListener.onDelete(imageIndex);
                                }
                            }
                        });
                alertDialog.setNegativeButton(context.getString(R.string.picture_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                alertDialog.show();

            }
        });
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        return popupWindow;
    }


    /**
     * 显示大图预览弹窗
     * @param type 类型
     * @param previewList 图片集合
     * @param position 显示图片索引
     */
    public void showPicturePreview(int type, List<String> previewList, int position){
        PopupWindow popupWindow = getPicturePreviewWindow(context, type, previewList, position);
        popupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);
    }

    public PopupWindow getPicturePreviewWindow(final Context context, final int type, final List<String> previewList, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.picture_popup_preview,null, false);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.picture_popup_preview_viewPager);
        final LinearLayout titleLayout = (LinearLayout) view.findViewById(R.id.picture_ui_title_layout);
        final LinearLayout footerLayout = (LinearLayout) view.findViewById(R.id.picture_ui_footer_layout);
        LinearLayout backLayout = (LinearLayout) view.findViewById(R.id.picture_ui_title_back_layout);
        final RelativeLayout containerLayout = (RelativeLayout) view.findViewById(R.id.picture_popup_preview_layout);
        final TextView doneText = (TextView) view.findViewById(R.id.picture_ui_title_done);
        TextView previewText = (TextView) view.findViewById(R.id.picture_ui_footer_preview);
        TextView folderText = (TextView) view.findViewById(R.id.picture_ui_footer_folder);
        final TextView indexText = (TextView) view.findViewById(R.id.picture_ui_title_index);
        final CheckBox chooseCheckBox = (CheckBox) view.findViewById(R.id.picture_ui_footer_choose);
        final CheckBox originalCheckBox = (CheckBox) view.findViewById(R.id.picture_ui_footer_original);
        final ClipImageLayout clipImageLayout = (ClipImageLayout) view.findViewById(R.id.picture_popup_preview_clipImageLayout);

        clipImageLayout.setImageUri(previewList.get(position));
        picturePreviewAdapter = new PicturePreviewAdapter(context,previewList);
        picturePreviewAdapter.setOnGestureListener(new IOnGestureListener() {
            @Override
            public void onClick() {
                if (showPreviewTitle) {
                    titleLayout.setVisibility(View.GONE);
                    footerLayout.setVisibility(View.GONE);
                    showPreviewTitle = false;
                } else {
                    titleLayout.setVisibility(View.VISIBLE);
                    footerLayout.setVisibility(View.VISIBLE);
                    showPreviewTitle = true;
                }
            }

            @Override
            public void onDoubleClick() {

            }

            @Override
            public void onLongPress() {

            }
        });
        viewPager.setAdapter(picturePreviewAdapter);

        clipImageLayout.setVisibility(View.GONE);
        switch (type){
            case PREVIEW_FOLDER:
                previewText.setVisibility(View.GONE);
                folderText.setVisibility(View.GONE);
                chooseCheckBox.setVisibility(View.VISIBLE);
                originalCheckBox.setVisibility(View.VISIBLE);
                setPreviewDoneText(doneText);
                break;
            case PREVIEW_CHOOSE:
                previewText.setVisibility(View.GONE);
                folderText.setVisibility(View.GONE);
                chooseCheckBox.setVisibility(View.VISIBLE);
                originalCheckBox.setVisibility(View.VISIBLE);
                setPreviewDoneText(doneText);
                break;
            case PREVIEW_TAKE:
                previewText.setVisibility(View.GONE);
                folderText.setVisibility(View.GONE);
                footerLayout.setVisibility(View.INVISIBLE);
                break;
            case PREVIEW_EDIT:
                viewPager.setVisibility(View.GONE);
                footerLayout.setVisibility(View.GONE);
                clipImageLayout.setVisibility(View.VISIBLE);
                break;
        }

        if (previewList.size()>position){
            if (type==PREVIEW_EDIT){
                indexText.setText(context.getString(R.string.picture_clip));
            }else {
                indexText.setText(""+(position+1)+"/"+previewList.size());
            }
            previewPath = previewList.get(position);
            if (isSelected(previewList.get(position))){
                chooseCheckBox.setChecked(true);
            }else {
                chooseCheckBox.setChecked(false);
            }
            if (isOriginal){
                originalCheckBox.setChecked(true);
            }else {
                originalCheckBox.setChecked(false);
            }
            viewPager.setCurrentItem(position);
        }

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (type==PREVIEW_EDIT){
                    indexText.setText(context.getString(R.string.picture_clip));
                }else {
                    indexText.setText(""+(position+1)+"/"+previewList.size());
                }
                previewPath = previewList.get(position);
                if (isSelected(previewList.get(position))){
                    chooseCheckBox.setChecked(true);
                }else {
                    chooseCheckBox.setChecked(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        chooseCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelected(previewPath)) {
                    setSelected(previewPath, false, doneText);
                } else {
                    if (chooseImageList.size() < maxSize) {
                        setSelected(previewPath, true, doneText);
                    } else {
                        ToastUtil.addToast(context, "" + context.getString(R.string.picture_max) + maxSize);
                        chooseCheckBox.setChecked(false);
                    }
                }
            }
        });
        originalCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOriginal){
                    isOriginal = false;
                    originalCheckBox.setChecked(false);
                }else {
                    isOriginal = true;
                    originalCheckBox.setChecked(true);
                }
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    int[] xy = {1,1};
                    containerLayout.getLocationOnScreen(xy);
                    if (xy[1]<statusBarHeight){
                        containerLayout.setPadding(0, statusBarHeight - xy[1], 0, 0);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, 100);
        final PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable()); //使按返回键能够消失
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (pictureGridAdapter !=null){
                    pictureGridAdapter.notifyDataSetChanged();
                }
                showPreviewTitle = true;
            }
        });
        doneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    popupWindow.dismiss();
                    if (type== PREVIEW_EDIT){
                        Bitmap bitmap = clipImageLayout.clip();
                        String clipImagePath = ImageChooseUtil.getImagePathFolder()+"clip.jpg";
                        ImageUtil.saveJPGE_After(bitmap,100,clipImagePath);
                        ArrayList<String> clipList = new ArrayList<String>();
                        clipList.add(clipImagePath);
                        sendPicturePickBroadcast(clipList);
                    }else if (type== PREVIEW_TAKE){
                        ArrayList<String> preList = new ArrayList<String>();
                        preList.add(previewList.get(0));
                        sendPicturePickBroadcast(preList);
                    }else {
                        if (!isOriginal){
                            ArrayList<String> imageList = new ArrayList<String>();
                            for (String path : chooseImageList){
                                String imagePath = ImageUtil.saveScaleImage(path,ImageChooseUtil.getImagePathFolder(),ImageChooseUtil.SCALE_WIDTH,ImageChooseUtil.SCALE_HEIGHT,100);
                                imageList.add(imagePath);
                            }
                            sendPicturePickBroadcast(imageList);
                        }else {
                            sendPicturePickBroadcast(chooseImageList);
                        }
                    }
                    activity.finish();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        return popupWindow;
    }

    private boolean isSelected(String path){
        boolean result = false;
        if (chooseImageList!=null&&chooseImageList.size()>0){
            for(String imagePath:chooseImageList){
                if (path.equals(imagePath)){
                    result = true;
                }
            }
        }
        return result;
    }

    private void setSelected(String path,boolean isChecked,TextView doneText){
        if (isChecked){
            if (!isSelected(path)){
                if (chooseImageList.size()<maxSize){
                    chooseImageList.add(path);
                }else {
                    ToastUtil.addToast(context, "" + context.getString(R.string.picture_max) + maxSize);
                }
            }
        }else {
            if (isSelected(path)){
                chooseImageList.remove(path);
            }
        }
        setPreviewDoneText(doneText);
        if (onCheckListener !=null){
            onCheckListener.onCheck(chooseImageList);
        }
    }

    private void setPreviewDoneText(TextView doneText){
        if (chooseImageList!=null&&chooseImageList.size()>0){
            doneText.setText(""+context.getString(R.string.picture_done)+"("+chooseImageList.size()+"/"+maxSize+")");
            doneText.setEnabled(true);
        }else {
            doneText.setText(""+context.getString(R.string.picture_done));
            doneText.setEnabled(false);
        }
    }

    /**
     * 发送结果广播
     * @param imageList 选中的图片列表
     */
    public void sendPicturePickBroadcast(ArrayList<String> imageList){
        Intent intent = new Intent();
        intent.setAction(PicturePickActivity.PICTURE_PICK_IMAGE);
        intent.putStringArrayListExtra(PicturePickActivity.PICTURE_PICK_IMAGE, imageList);
        context.sendBroadcast(intent);
    }

    /**
     * 显示文件夹选择弹窗
     */
    public void showFolderWindow(List<ImageEntity> folderImageEntityList){
        this.folderImageEntityList = folderImageEntityList;
        PopupWindow folderPopupWindow = getFolderWindow(context,0);
        folderPopupWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
    }

    private PopupWindow getFolderWindow(Context context,int animStyle) {
        View view = LayoutInflater.from(context).inflate(R.layout.picture_popup_folder,null, false);
        ListView listView = (ListView) view.findViewById(R.id.picture_popup_folder_listView);
        LinearLayout containerLayout = (LinearLayout) view.findViewById(R.id.picture_popup_folder_layout);
        PictureFolderAdapter pictureFolderAdapter = new PictureFolderAdapter(context, folderImageEntityList);
        listView.setAdapter(pictureFolderAdapter);
        final PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable()); //使按返回键能够消失
        if(animStyle>0){
            popupWindow.setAnimationStyle(animStyle);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if (onItemClickListener!=null){
                    onItemClickListener.onItemClick(arg2);
                }
                popupWindow.dismiss();
            }

        });
        containerLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }

        });

        return popupWindow;
    }



    public boolean isShowPreviewTitle() {
        return showPreviewTitle;
    }

    public void setShowPreviewTitle(boolean showPreviewTitle) {
        this.showPreviewTitle = showPreviewTitle;
    }

    public boolean isShowDelete() {
        return showDelete;
    }

    public void setShowDelete(boolean showDelete) {
        this.showDelete = showDelete;
    }

    public boolean isShowDotIndex() {
        return showDotIndex;
    }

    public void setShowDotIndex(boolean showDotIndex) {
        this.showDotIndex = showDotIndex;
    }

    public IOnDeleteListener getOnDeleteListener() {
        return onDeleteListener;
    }

    public void setOnDeleteListener(IOnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    public int getStatusBarHeight() {
        return statusBarHeight;
    }

    public void setStatusBarHeight(int statusBarHeight) {
        this.statusBarHeight = statusBarHeight;
    }

    public IOnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(IOnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public List<ImageEntity> getFolderImageEntityList() {
        return folderImageEntityList;
    }

    public void setFolderImageEntityList(List<ImageEntity> folderImageEntityList) {
        this.folderImageEntityList = folderImageEntityList;
    }

    public String getPreviewPath() {
        return previewPath;
    }

    public void setPreviewPath(String previewPath) {
        this.previewPath = previewPath;
    }

    public ArrayList<String> getChooseImageList() {
        return chooseImageList;
    }

    public void setChooseImageList(ArrayList<String> chooseImageList) {
        this.chooseImageList = chooseImageList;
    }

    public PictureGridAdapter getPictureGridAdapter() {
        return pictureGridAdapter;
    }

    public void setPictureGridAdapter(PictureGridAdapter pictureGridAdapter) {
        this.pictureGridAdapter = pictureGridAdapter;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public IOnCheckListener getOnCheckListener() {
        return onCheckListener;
    }

    public void setOnCheckListener(IOnCheckListener onCheckListener) {
        this.onCheckListener = onCheckListener;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public boolean isOriginal() {
        return isOriginal;
    }

    public void setIsOriginal(boolean isOriginal) {
        this.isOriginal = isOriginal;
    }
}