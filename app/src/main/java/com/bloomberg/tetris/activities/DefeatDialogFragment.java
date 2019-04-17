package com.bloomberg.tetris.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DefeatDialogFragment extends DialogFragment {
    //定义三个字符序列储存得分 事件 每分钟操作次数
    private CharSequence scoreString;
    private CharSequence timeString;
    private CharSequence apmString;
    //score储存得分
    private long score;

    //构造方法
    public DefeatDialogFragment() {
        super();
        scoreString = "unknown";
        timeString = "unknown";
        apmString = "unknown";
    }

    //设置数据
    public void setData(long scoreArg, String time, int apm) {
        scoreString = String.valueOf(scoreArg);
        timeString = time;
        apmString = String.valueOf(apm);
        score = scoreArg;
    }

    //弹出对话框时自动调用
    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        //创建对话框对象
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //设置标题
        builder.setTitle(com.bloomberg.tetris.R.string.defeatDialogTitle);
        //设置对话框内容
        builder.setMessage(
                getResources().getString(com.bloomberg.tetris.R.string.scoreLabel) +
                        "\n    " + scoreString + "\n\n" +
                        getResources().getString(com.bloomberg.tetris.R.string.timeLabel) +
                        "\n    " + timeString + "\n\n" +
                        getResources().getString(com.bloomberg.tetris.R.string.apmLabel) +
                        "\n    " + apmString + "\n\n"
        );
        //是在android的alertDialog中封装好的一些Button setNeutralButton一般用做“确认”
        builder.setNeutralButton(com.bloomberg.tetris.R.string.defeatDialogReturn, new DialogInterface.OnClickListener() {
            //调用GameActivity的方法向MainActivity传递数据
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((GameActivity) getActivity()).putScore(score);
            }
        });
        return builder.create();
    }
}
