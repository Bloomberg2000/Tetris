package com.bloomberg.tetris.others;

import com.bloomberg.tetris.R;
import com.bloomberg.tetris.components.Board;
import com.bloomberg.tetris.components.Row;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

// 以行为单位的动画类
public class Animator {

    //Idle（空闲）模式标记
    public static final int stageIdle = 0;
    //Flash（闪烁）模式标记
    public static final int stageFlash = 1;

    // 配置
    private long flashInterval; // 闪烁间隔
    private long flashFinishTime; // 闪烁结束时间
    private int squareSize; //正方形大小

    // 状态
    private long startTime; //动画开始时间
    private int stage; // 动画模式标记
    private boolean drawEnable; // 可绘制开关
    private long nextFlash; // 下一次闪烁时间

    // 数据
    private Row row; //行
    private Bitmap bitmapRow;
    private int flashCount; // 闪烁次数
    private int rawFlashInterval; // 初始闪烁间隔

    // 构造方法
    public Animator(Context c, Row row) {
        // 初始闪烁间隔为200
        rawFlashInterval = c.getResources().getInteger(R.integer.clearAnimation_flashInterval);
        // 设置闪烁次数
        flashCount = c.getResources().getInteger(R.integer.clearAnimation_flashCount);
        stage = stageIdle; // stage初始化为空闲状态
        this.row = row; // 用参数中的"行"初始化Animator中的行
        drawEnable = true;// 默认需要绘制
        startTime = 0; // 开始时间为 0
        flashFinishTime = 0; // 闪烁结束时间为 0
        nextFlash = 0; // 下次闪烁时间为 0
        flashInterval = 0; // 闪烁间隔为 0
        squareSize = 0; // 正方形尺寸为 0
    }

    // 遍历判断动画结束
    public void cycle(long time, Board board) {
        // 动画模式为空闲模式 不进行操作
        if (stage == stageIdle) {
            return;
        }
        // 当前时间大鱼闪烁结束时间
        if (time >= flashFinishTime) {
            // 清除改行
            finish(board);
        } else if (time >= nextFlash) {
            nextFlash += flashInterval; // 下次闪烁时间为上次闪烁时间 + 时间间隔
            drawEnable = !drawEnable;
            board.invalidate(); //令"板"暂时不可绘制
        }
    }

    // 动画开始
    // currentDropInterval 当前下落速率
    public void start(Board board, int currentDropInterval) {
        // 在bitmapRow上绘制行
        bitmapRow = row.drawBitmap(squareSize);
        // 置于闪烁状态
        stage = stageFlash;
        // 开始时间为系统当前时间
        startTime = System.currentTimeMillis();
        // 在"初始闪烁间隔"和"下落速率与闪烁时间的差"中选择较小的作为时间间隔
        flashInterval = Math.min(rawFlashInterval, (int) ((float) currentDropInterval / (float) flashCount));
        // 闪烁结束时间
        flashFinishTime = startTime + 2 * flashInterval * flashCount;
        // 下次闪烁时间 开始时间+闪烁间隔
        nextFlash = startTime + flashInterval;
        drawEnable = false;
        board.invalidate();
    }

    //结束动画
    public boolean finish(Board board) {
        // 如果处于空闲模式 返回false
        if (stage == stageIdle) {
            return false;
        }
        // 置于闲置状态
        stage = stageIdle;
        // 清除该行
        row.finishClear(board);
        // 置于可绘制状态
        drawEnable = true;
        return true;
    }

    // 绘制
    public void draw(int x, int y, int size, Canvas c) {
        this.squareSize = size; // 设置正当性大小
        if (drawEnable) {
            // 画板处于闲置状态开始绘制行
            if (stage == stageIdle) {
                bitmapRow = row.drawBitmap(size);
            }
            // bitmapRow 不为空 在画板上绘制BitMap
            if (bitmapRow != null)
                c.drawBitmap(bitmapRow, x, y, null);
        }
    }
}