package com.bloomberg.tetris.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.bloomberg.tetris.R;

//正方形类声明与定义
public class Square {
    //定义常量用于区分不同颜色方块
    public static final int type_empty = 0;
    public static final int type_blue = 1;
    public static final int type_orange = 2;
    public static final int type_yellow = 3;
    public static final int type_red = 4;
    public static final int type_green = 5;
    public static final int type_magenta = 6;
    public static final int type_cyan = 7;

    private int type; // 方块类型标记
    //Paint类对象（画笔）
    private Paint paint;
    //Bitmap类对象（图片）
    private Bitmap bm;
    private Bitmap phantomBM; // 透明
    //Canvas类对象（画板）
    private Canvas canv;
    private Canvas phantomCanv;// 透明

    private int squaresize; //方块大小
    private int phantomAlpha; //透明度

    //构造方法
    public Square(int type, Context c) {
        this.type = type; //确定方块颜色类型
        paint = new Paint();//为画笔类对象分配空间
        phantomAlpha = c.getResources().getInteger(R.integer.phantom_alpha);//储存方块透明度
        squaresize = 0;//方块大小默认为0
        //根据类型为画笔设置不同的颜色
        switch (type) {
            case type_blue:
                paint.setColor(c.getResources().getColor(R.color.square_blue));
                break;
            case type_orange:
                paint.setColor(c.getResources().getColor(R.color.square_orange));
                break;
            case type_yellow:
                paint.setColor(c.getResources().getColor(R.color.square_yellow));
                break;
            case type_red:
                paint.setColor(c.getResources().getColor(R.color.square_red));
                break;
            case type_green:
                paint.setColor(c.getResources().getColor(R.color.square_green));
                break;
            case type_magenta:
                paint.setColor(c.getResources().getColor(R.color.square_magenta));
                break;
            case type_cyan:
                paint.setColor(c.getResources().getColor(R.color.square_cyan));
                break;
            case type_empty:
                return;
            default: // 错误时显示为白色
                paint.setColor(c.getResources().getColor(R.color.square_error));
                break;
        }
    }

    //根据传来的尺寸重绘
    public void reDraw(int size) {
        if (type == type_empty) {
            return;
        }
        squaresize = size; //保存方块大小
        //A代表Alpha，R表示red，G表示green，B表示blue.
        //ARGB_8888就是由4个8位组成即32位图
        bm = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        phantomBM = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        //用Bitmap初始化画板
        canv = new Canvas(bm);
        phantomCanv = new Canvas(phantomBM);
        paint.setAlpha(255);//设置画笔不透明度100%
        // 绘图
        // drawRect(float left, float top, float right, float bottom, Paint paint)
        // 各值为相对于x轴正方向和y轴负方向的距离
        // 0 ------------- X
        // |      | 4
        // |------
        // |  4
        // |
        // Y
        canv.drawRect(0, 0, squaresize, squaresize, paint);
        paint.setAlpha(phantomAlpha);//设置画笔不透明度93%
        // 绘图
        phantomCanv.drawRect(0, 0, squaresize, squaresize, paint);
    }

    // 复制一个相同的块
    public Square clone(Context c) {
        return new Square(type, c);
    }

    //判断方块是否为空
    public boolean isEmpty() {
        return type == type_empty;
    }

    // 绘制（正方形左上角）
    public void draw(int x, int y, int squareSize, Canvas c, boolean isPhantom) {
        if (type == type_empty)
            return;

        if (squareSize != squaresize)
            reDraw(squareSize);
        // 根据是否为透明选择不同绘制方法
        // bitmap：需要绘制的bitmap
        // left：绘制区域与左边界距离
        // top：绘制区域与上边界距离
        // paint：画笔，可为null
        if (isPhantom) {
            c.drawBitmap(phantomBM, x, y, null);
        } else {
            c.drawBitmap(bm, x, y, null);
        }
    }
}
