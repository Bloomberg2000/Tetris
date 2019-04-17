package com.bloomberg.tetris.others;

import com.bloomberg.tetris.activities.GameActivity;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

public class BlockBoardView extends SurfaceView implements Callback {

    private GameActivity host;

    // 重载的三个构造函数
    public BlockBoardView(Context context) {
        super(context);
    }

    public BlockBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BlockBoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // 设定宿主Activity
    public void setHost(GameActivity ga) {
        host = ga;
    }

    // 初始化
    public void init() {
        // 使SurfaceView被放在窗口顶层 Z轴顶部
        setZOrderOnTop(true);
        // 访问SurfaceView的底层图形是通过SurfaceHolder接口来实现的，通过 getHolder()方法可以得到这个
        // SurfaceHolder对象
        getHolder().addCallback(this); // 为这个容器添加回调接口
        getHolder().setFormat(PixelFormat.TRANSPARENT); // 使窗口支持透明（设置透明）
    }

    // 接口中的抽象方法 必须实现
    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

    }

    // 游戏界面加载完毕 开始游戏
    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        host.startGame(this);
    }

    // 游戏界面销毁 游戏进程销毁
    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        host.destroyWorkThread();
    }
}
