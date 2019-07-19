package com.little.picture.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.little.picture.PictureStartManager;
import com.little.picture.R;
import com.little.picture.adapter.PictureFolderAdapter;
import com.little.picture.adapter.PictureGridAdapter;
import com.little.picture.adapter.PicturePreviewAdapter;
import com.little.picture.listener.IOnCheckListener;
import com.little.picture.listener.IOnDeleteListener;
import com.little.picture.listener.IOnGestureListener;
import com.little.picture.listener.IOnItemClickListener;
import com.little.picture.model.ImageFolderEntity;
import com.little.picture.model.ImageListEntity;
import com.little.picture.view.ClipImageLayout;
import com.little.picture.view.PageIndicatorView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import static com.little.picture.util.StatusBarUtils.isMeizu;
import static com.little.picture.util.StatusBarUtils.isXiaomi;

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
    private boolean showPreviewTitle = false;//是否显示标题
    private boolean showDelete = false;//是否显示删除按钮
    private boolean showDotIndex = false;//是否显示圆点索引

    private int statusBarHeight;//状态栏高度
    private float rate = 1;//裁剪图片宽高比



    private IOnCheckListener onCheckListener;
    private IOnItemClickListener onItemClickListener;
    private IOnDeleteListener onDeleteListener;//删除监听

    private String previewPath = "";

    private List<ImageFolderEntity> folderImageFolderEntityList;
    private ArrayList<String> chooseImageList;//选中的图片

    private PictureGridAdapter pictureGridAdapter;
    private PicturePreviewAdapter picturePreviewAdapter;

    private int maxSize = 9;//最多能选择的图片数
    private String fromTag = "";//来源标志

    private Activity activity;//PicturePickActivity
    private boolean isOriginal = false;//是否使用原图
    private int folderShowIndex = -1;//当前文件夹索引


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
     * 显示大图预览 -外部调用
     * @param index 默认显示第几张
     * @return
     */
    public void showImagePreview(int index, List<String> imageList){
        this.imageList = imageList;
        getPreviewWindow(context, index);
    }

    public void getPreviewWindow(final Context context,int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.picture_popup_preview,null, false);
        final ViewPager viewPager = view.findViewById(R.id.picture_popup_preview_viewPager);
        final PageIndicatorView pageIndicatorView = view.findViewById(R.id.picture_popup_preview_pageIndicatorView);
        final LinearLayout titleLayout = view.findViewById(R.id.picture_ui_title_layout2);
        final LinearLayout footerLayout = view.findViewById(R.id.picture_ui_footer_layout);
        LinearLayout backLayout = view.findViewById(R.id.picture_ui_title_back_layout);
        final TextView doneText = view.findViewById(R.id.picture_ui_title_done);
        final LinearLayout deleteLayout = view.findViewById(R.id.picture_ui_title_delete_layout);
        final TextView indexText = view.findViewById(R.id.picture_ui_title_index);

        final Dialog dialog = new Dialog(context, R.style.DialogCentre);
        dialog.setContentView(view);

        setSameConfig(dialog,backLayout);


        picturePreviewAdapter = new PicturePreviewAdapter(context, imageList);
        picturePreviewAdapter.setOnGestureListener(new IOnGestureListener() {
            @Override
            public void onClick() {
                dialog.dismiss();
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
        if (showPreviewTitle){
            titleLayout.setVisibility(View.VISIBLE);
        }else {
            titleLayout.setVisibility(View.GONE);
        }

        footerLayout.setVisibility(View.GONE);
        doneText.setVisibility(View.GONE);

        if (imageList.size()>position){
            indexText.setText(""+(position+1)+"/"+ imageList.size());
            imageIndex = position;
            viewPager.setCurrentItem(position);
        }

        pageIndicatorView.setVisibility(View.VISIBLE);
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

        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteTip(dialog,indexText,viewPager);

            }
        });
    }


    /**
     * 显示大图预览弹窗
     * @param type 类型
     * @param previewList 图片集合
     * @param position 显示图片索引
     */
    public void showPicturePreview(int type, List<String> previewList, int position){
        getPicturePreviewWindow(context, type, previewList, position);
    }

    public void getPicturePreviewWindow(final Context context, final int type, final List<String> previewList, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.picture_popup_preview,null, false);
        ViewPager viewPager = view.findViewById(R.id.picture_popup_preview_viewPager);
        final LinearLayout titleLayout = view.findViewById(R.id.picture_ui_title_layout2);
        final LinearLayout footerLayout = view.findViewById(R.id.picture_ui_footer_layout);
        LinearLayout backLayout = view.findViewById(R.id.picture_ui_title_back_layout);
        final TextView doneText = view.findViewById(R.id.picture_ui_title_done);
        TextView previewText = view.findViewById(R.id.picture_ui_footer_preview);
        TextView folderText = view.findViewById(R.id.picture_ui_footer_folder);
        final TextView indexText = view.findViewById(R.id.picture_ui_title_index);
        final CheckBox chooseCheckBox = view.findViewById(R.id.picture_ui_footer_choose);
        final CheckBox originalCheckBox = view.findViewById(R.id.picture_ui_footer_original);
        final ClipImageLayout clipImageLayout = view.findViewById(R.id.picture_popup_preview_clipImageLayout);

        clipImageLayout.setImageUri(previewList.get(position));
        if (folderShowIndex == 0){
            previewList.remove(0);
        }
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
//                originalCheckBox.setVisibility(View.VISIBLE);
                setPreviewDoneText(doneText);
                break;
            case PREVIEW_CHOOSE:
                previewText.setVisibility(View.GONE);
                folderText.setVisibility(View.GONE);
                chooseCheckBox.setVisibility(View.VISIBLE);
//                originalCheckBox.setVisibility(View.VISIBLE);
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
                clipImageLayout.setRate(rate);
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

        final Dialog dialog = new Dialog(context, R.style.DialogCentre);
        dialog.setContentView(view);

        setSameConfig(dialog,backLayout);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (folderShowIndex == 0){
                    previewList.add(0, "takePhoto");
                    folderShowIndex = -1;
                }
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
                    dialog.dismiss();
                    if (type== PREVIEW_EDIT){
                        Bitmap bitmap = clipImageLayout.clip();
                        String clipImagePath = PictureStartManager.getImageFolder()+"clip"+System.currentTimeMillis()+".jpg";
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
                                String imagePath = ImageUtil.saveScaleImage(path, PictureStartManager.getImageFolder(),PictureStartManager.SCALE_WIDTH,PictureStartManager.SCALE_HEIGHT,100);
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

    }

    private void setSameConfig(final Dialog dialog,View tvCancel){
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
//            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        if (isXiaomi()) {
            StatusBarUtils.setXiaomiStatusBar(window, true);
        } else if (isMeizu()) {
            StatusBarUtils.setMeizuStatusBar(window, true);
        }
        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });

        if (tvCancel!=null){
            tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

    }

    private void showDeleteTip(final Dialog dialog,final TextView indexText,final ViewPager viewPager){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("提示");
        alertDialog.setMessage(context.getString(R.string.picture_delete));
        alertDialog.setPositiveButton(context.getString(R.string.picture_confirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        imageList.remove(imageIndex);
                        picturePreviewAdapter.notifyDataSetChanged();
                        if (imageList.size() == 0) {
                            dialog.dismiss();

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
        ImageListEntity imageListEntity = new ImageListEntity();
        imageListEntity.setChooseImageList(imageList);
        imageListEntity.setFromTag(fromTag);
        EventBus.getDefault().post(imageListEntity);
    }

    /**
     * 显示文件夹选择弹窗
     */
    public void showFolderWindow(List<ImageFolderEntity> folderImageFolderEntityList){
        this.folderImageFolderEntityList = folderImageFolderEntityList;
        PopupWindow folderPopupWindow = getFolderWindow(context,0);
        folderPopupWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
    }

    private PopupWindow getFolderWindow(Context context,int animStyle) {
        View view = LayoutInflater.from(context).inflate(R.layout.picture_popup_folder,null, false);
        ListView listView = (ListView) view.findViewById(R.id.picture_popup_folder_listView);
        LinearLayout containerLayout = (LinearLayout) view.findViewById(R.id.picture_popup_folder_layout);
        PictureFolderAdapter pictureFolderAdapter = new PictureFolderAdapter(context, folderImageFolderEntityList);
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


    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
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

    public List<ImageFolderEntity> getFolderImageFolderEntityList() {
        return folderImageFolderEntityList;
    }

    public void setFolderImageFolderEntityList(List<ImageFolderEntity> folderImageFolderEntityList) {
        this.folderImageFolderEntityList = folderImageFolderEntityList;
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

    public void setFolderShowIndex(int folderShowIndex) {
        this.folderShowIndex = folderShowIndex;
    }

    public String getFromTag() {
        return fromTag;
    }

    public void setFromTag(String fromTag) {
        this.fromTag = fromTag;
    }
}