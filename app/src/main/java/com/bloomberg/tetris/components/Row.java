package com.bloomberg.tetris.components;

import com.bloomberg.tetris.others.Animator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

// 表示"行"的包装类
public class Row {
    private Row below; // 下方的行
    private Row above; // 上方的行
    private Square[] elements; // 方形类数组
    private Square emptySquare; // 空的正方形
    private int width; // 行宽
    private Animator animator; // 动画层
    private int fillStatus; // 已填充方块数

    //构造方法
    public Row(int width, Context c) {
        emptySquare = new Square(Square.type_empty, c); // 构造一个空的正方形
        animator = new Animator(c, this); // 构造视图动画类的对象
        this.width = width; // 储存行宽
        // 上下行默认置空
        below = null;
        above = null;
        fillStatus = 0; // 已填充方块数为0
        elements = new Square[width]; // 为数组分配长度为width个正方形的空间

        //数组中所有正方形均初始化为空正方形
        for (int i = 0; i < width; i++) {
            elements[i] = emptySquare;//调用clone()方法
        }
    }

    // 通过Square类型参数 确定"行"中各个索引位置的正方形
    public void set(Square s, int i) {
        if (s.isEmpty())
            return;
        if ((i >= 0) && (i < width)) {
            fillStatus++;
            elements[i] = s;
        }
    }

    // 获取索引位置的正方行
    public Square get(int i) {
        if ((i >= 0) && (i < width))
            return elements[i];
        return null;
    }

    //通过Square类型数组 确定"行"中各个索引位置的正方形
    public void set(Square[] squares) {
        // 把squares赋值给elements;
        elements = squares;
        fillStatus = 0; // 已填充方块数置为0
        if (elements != null)
            for (int i = 0; i < width; i++) {
                if (elements[i] != null) {
                    // 如果不是空块 已填充方块数加1
                    if (!elements[i].isEmpty()) {
                        fillStatus++;
                    }
                }
            }
    }

    // 设置上方的行
    public void setAbove(Row row) {
        this.above = row;
    }

    // 设置下方的行
    public void setBelow(Row row) {
        this.below = row;
    }

    // 返回下方的行
    public Row below() {
        return this.below;
    }

    // 返回上方的行
    public Row above() {
        return this.above;
    }

    // 删除本行
    public Row delete() {
        Row result = this.below;
        // 如果上方行存在则设置"上方行的下方行"为"本行的下方行"
        if (above != null)
            above.setBelow(below);
        // 如果下方行存在则设置"下方行的上方行"为"本行的上方行"
        if (below != null)
            below.setAbove(above);
        // 本行的上下行置空
        above = null;
        below = null;
        // 返回值为本行的下方行
        return result;
    }

    // 绘制行(从左上角开始绘制)
    public void draw(int x, int y, int squareSize, Canvas c) {
        animator.draw(x, y, squareSize, c);
    }

    //绘制图片（从左上角开始绘制）
    public Bitmap drawBitmap(int squareSize) {
        // 创建大小为（width*1）*方块的图片
        Bitmap bitmap = Bitmap.createBitmap(width * squareSize, squareSize, Bitmap.Config.ARGB_8888);
        // 图片置于画板上
        Canvas canvas = new Canvas(bitmap);
        //逐个绘制正方形
        for (int i = 0; i < width; i++) {
            if (elements[i] != null)
                elements[i].draw(i * squareSize, 0, squareSize, canvas, false);
        }
        return bitmap;
    }

    // 判断行是否已满
    public boolean isFull() {
        return fillStatus >= width;
    }

    // 遍历"行"
    public void cycle(long time, Board board) {
        animator.cycle(time, board);
    }

    // currentDropInterval当前下落速度
    public void clear(Board board, int currentDropInterval) {
        animator.start(board, currentDropInterval);
    }

    // 消除行
    public void finishClear(Board board) {
        fillStatus = 0;//已填充方块数置零
        // 正方形数组对象置为空
        for (int i = 0; i < width; i++) {
            elements[i] = emptySquare;
        }
        // 令本行的上下行相连接
        above().setBelow(below());
        below().setAbove(above());

        Row topRow = board.getTopRow();
        // 把本行设置为"板"的第一行
        setBelow(topRow);
        setAbove(topRow.above());
        topRow.above().setBelow(this);
        topRow.setAbove(this);
        //在"板"中彻底删除本行
        board.finishClear(this);
    }

    // 结束Animation 返回操作结果
    public boolean interrupt(Board board) {
        return animator.finish(board);
    }
}
