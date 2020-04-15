package com.little.picture.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.fos.fosmvp.common.utils.StringUtils;
import com.little.picture.R;
import com.little.picture.util.DensityUtils;

import java.util.List;


/**
 * 弹窗
 */

public class PAPopupManager {
    private Context context;
    public boolean dismissOutside = true;
    public boolean isAllScreen = false;//是否全屏
    public String dialogTitle = "温馨提示";//对话框标题
    /**
     * 提示对话框操作数量 优先级：确定>取消>其他
     * 0不显示，1显示确定，2显示确定、取消，3显示确定、取消、其他
     */
    public int optionCount = 2;
    public String confirmStr = "确定";//提示对话框确定按钮名称
    public String cancelStr = "取消";//提示对话框取消按钮名称
    public String otherStr = "忽略";//提示对话框其他按钮名称

    private IOnDialogListener onPopupListener;
    private IOnDismissListener onDismissListener;
    private IOnItemListener onItemListener;

    public PAPopupManager(Context context) {
        this.context = context;
    }

    public void showTipDialog(String title,String message) {
        View view = LayoutInflater.from(context).inflate(R.layout.picture_dialog_tip,null, false);
        TextView titleText =  view.findViewById(R.id.popup_dialog_tip_title);
        TextView messageText =  view.findViewById(R.id.popup_dialog_tip_content);
        TextView confirmText =  view.findViewById(R.id.popup_dialog_tip_confirm);
        TextView cancelText =  view.findViewById(R.id.popup_dialog_tip_cancel);
        TextView otherText =  view.findViewById(R.id.popup_dialog_tip_other);

        if (!StringUtils.isEmpty(title)){
            dialogTitle = title;
        }
        if(!StringUtils.isEmpty(dialogTitle)){
            titleText.setText(dialogTitle);
            titleText.setVisibility(View.VISIBLE);
        }else {
            titleText.setVisibility(View.GONE);
        }
        if(!StringUtils.isEmpty(message)){
            messageText.setText(message);
        }
        if (!StringUtils.isEmpty(confirmStr)){
            confirmText.setText(confirmStr);
        }
        if (!StringUtils.isEmpty(cancelStr)){
            cancelText.setText(cancelStr);
        }
        if (!StringUtils.isEmpty(otherStr)){
            otherText.setText(otherStr);
        }
        switch (optionCount){
            case 0:
                confirmText.setVisibility(View.GONE);
                cancelText.setVisibility(View.GONE);
                otherText.setVisibility(View.GONE);
                break;
            case 1:
                confirmText.setVisibility(View.VISIBLE);
                cancelText.setVisibility(View.GONE);
                otherText.setVisibility(View.GONE);
                break;
            case 2:
                confirmText.setVisibility(View.VISIBLE);
                cancelText.setVisibility(View.VISIBLE);
                otherText.setVisibility(View.GONE);
                break;
            case 3:
                confirmText.setVisibility(View.VISIBLE);
                cancelText.setVisibility(View.VISIBLE);
                otherText.setVisibility(View.VISIBLE);
                break;
        }
        final Dialog dialog = new Dialog(context, R.style.PictureDialogCentre);
        dialog.setContentView(view);

        setSameConfig(dialog,cancelText);
        confirmText.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (onPopupListener !=null){
                    onPopupListener.onConfirm();
                }
                dialog.dismiss();
            }

        });

        otherText.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (onPopupListener !=null){
                    onPopupListener.onOther();
                }
                dialog.dismiss();
            }

        });

    }

    public void showListDialog(List<String> stringList){
        View view = LayoutInflater.from(context).inflate(R.layout.picture_dialog_list,null, false);
        ListView listView = view.findViewById(R.id.popup_dialog_list_listview);
        TextView cancelText =  view.findViewById(R.id.popup_dialog_list_cancel);

        PopupListAdapter adapter = new PopupListAdapter(context,stringList);
        listView.setAdapter(adapter);

        if (stringList.size()>=8){
            listView.getLayoutParams().height = DensityUtils.dip2px(context,350);
        }

        final Dialog dialog = new Dialog(context, R.style.PictureActionSheetDialogStyle);
        dialog.setContentView(view);

        setBottomDialogConfig(dialog,cancelText);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if (onItemListener !=null){
                    onItemListener.onItem(arg2);
                }
                dialog.dismiss();
            }

        });
    }


    private void setSameConfig(final Dialog dialog,View cancelView){
        //点击其他区域消失
        if (dismissOutside){
            dialog.setCanceledOnTouchOutside(true);
        }
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        if (isAllScreen){
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            wlp.height = WindowManager.LayoutParams.MATCH_PARENT;
        }else {
            wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        }
        window.setAttributes(wlp);
        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (onDismissListener!=null){
                    onDismissListener.onDismiss();
                }
            }
        });

        if (cancelView!=null){
            cancelView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (onPopupListener!=null){
                        onPopupListener.onCancel();
                    }
                }
            });
        }

    }

    private void setBottomDialogConfig(final Dialog dialog,View cancelView){
        //点击其他区域消失
        if (dismissOutside){
            dialog.setCanceledOnTouchOutside(true);
        }
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (onDismissListener!=null){
                    onDismissListener.onDismiss();
                }
            }
        });

        if (cancelView!=null){
            cancelView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (onPopupListener!=null){
                        onPopupListener.onCancel();
                    }
                }
            });
        }

    }


    //将光标移至文字末尾
    public void setLastSelection(EditText etEdit){
        String content = etEdit.getText().toString();
        etEdit.setSelection(content.length());//将光标移至文字末尾
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isDismissOutside() {
        return dismissOutside;
    }

    public void setDismissOutside(boolean dismissOutside) {
        this.dismissOutside = dismissOutside;
    }

    public IOnDialogListener getOnPopupListener() {
        return onPopupListener;
    }

    public void setOnPopupListener(IOnDialogListener onPopupListener) {
        this.onPopupListener = onPopupListener;
    }

    public IOnDismissListener getOnDismissListener() {
        return onDismissListener;
    }

    public void setOnDismissListener(IOnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public IOnItemListener getOnItemListener() {
        return onItemListener;
    }

    public void setOnItemListener(IOnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    public void setAllScreen(boolean allScreen) {
        isAllScreen = allScreen;
    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    public void setOptionCount(int optionCount) {
        this.optionCount = optionCount;
    }

    public void setConfirmStr(String confirmStr) {
        this.confirmStr = confirmStr;
    }

    public void setCancelStr(String cancelStr) {
        this.cancelStr = cancelStr;
    }

    public void setOtherStr(String otherStr) {
        this.otherStr = otherStr;
    }


    
}
