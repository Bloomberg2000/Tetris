package com.bloomberg.tetris.components;

import com.bloomberg.tetris.activities.GameActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;


public class Board extends Component {

    private int height;//高度
    private int width;//宽度
    private Row topRow; // 顶端行
    private Row currentRow; // 当前行
    private int currentIndex; // 当前行索引
    private Row tempRow; // 临时行

    private boolean valid; // 是否可以直接绘制图片
    private Bitmap blockMap; //图片
    private Canvas blockVas; // 画板

    // 构造方法
    public Board(GameActivity ga) {
        super(ga);
        width = host.getResources().getInteger(com.bloomberg.tetris.R.integer.spalten); // 板宽度为10
        height = host.getResources().getInteger(com.bloomberg.tetris.R.integer.zeilen);// 板长度为18
        valid = false;// 默认初始化时 不可以直接绘制图片

        //初始化板
        topRow = new Row(width, host);//在host GameActivity中初始化宽度为板宽度
        currentIndex = 0; // 当前行索引为0
        tempRow = topRow; // 临时行为topRow
        currentRow = topRow; // 当前行为topRow
        for (int i = 1; i < height; i++) {
            currentRow = new Row(width, host); // 为第i行行分配空间
            currentIndex = i; // 目前索引为i
            currentRow.setAbove(tempRow); // 第i行的上一行为临时行
            tempRow.setBelow(currentRow); // 临时行的下一行为第i行
            tempRow = currentRow; // 临时行为第i行
        }
        //类似于循环链表 令第一行和最后一行首位相接
        topRow.setAbove(currentRow); //topRow的上一行为当前行
        currentRow.setBelow(topRow); //当前行的下一行为topRow
    }

    // 从左上角开始绘制Board
    public void draw(int x, int y, int squareSize, Canvas canvas) {
        // topRow为空抛出异常
        if (topRow == null) {
            throw new RuntimeException("BlockBoard was not initialized!");
        }
        if (valid) {
            // 根据blockMap在画板canvans上绘制BitMap
            canvas.drawBitmap(blockMap, x, y, null);
            return;
        }

        // 本区域捕捉 java.lang.OutOfMemoryError: 位图超出VM预算
        try {
            // 创建32位Bitmap
            blockMap = Bitmap.createBitmap(width * squareSize, height * squareSize, Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            valid = false; // 设置为不可绘制
            //从第一行开始逐行向下 直接在canvas画板上绘制Bitmap
            tempRow = topRow;
            for (int i = 0; i < height; i++) {
                if (tempRow != null) {
                    canvas.drawBitmap(tempRow.drawBitmap(squareSize), x, y + i * squareSize, null);
                    tempRow = tempRow.below();
                }
            }
            return;
        }
        // 把BitMap放置在画板blockVas上
        blockVas = new Canvas(blockMap);
        valid = true; //可以开始绘制

        // 在blockVas画板上绘制行
        tempRow = topRow;
        for (int i = 0; i < height; i++) {
            if (tempRow != null) {
                tempRow.draw(0, i * squareSize, squareSize, blockVas);
                tempRow = tempRow.below();
            }
        }

        // 根据blockMap在画板canvans上绘制BitMap
        canvas.drawBitmap(blockMap, x, y, null);
    }

    // 获取板宽度
    public int getWidth() {
        return width;
    }

    // 获取板高度
    public int getHeight() {
        return height;
    }

    // 递归获取索引位置正方形
    public Square get(int x, int y) {
        if (x < 0)
            return null;
        if (x > (width - 1))
            return null;
        if (y < 0)
            return null;
        if (y > (height - 1))
            return null;
        if (currentIndex == y) {
            return currentRow.get(x);
        } else if (currentIndex < y) {
            if (currentRow.below() == null)
                return null;
            else {
                currentRow = currentRow.below();
                currentIndex++;
                return get(x, y);
            }
        } else {
            if (currentRow.above() == null)
                return null;
            else {
                currentRow = currentRow.above();
                currentIndex--;
                return get(x, y);
            }
        }
    }

    // 递归把已知正方形放置在索引位置
    public void set(int x, int y, Square square) {
        if (x < 0)
            return;
        if (x > (width - 1))
            return;
        if (y < 0)
            return;
        if (y > (height - 1))
            return;
        if (square == null)
            return;
        if (square.isEmpty())
            return;
        valid = false;
        if (currentIndex == y)
            currentRow.set(square, x);
        else if (currentIndex < y) {
            currentRow = currentRow.below();
            currentIndex++;
            set(x, y, square);
        } else {
            currentRow = currentRow.above();
            currentIndex--;
            set(x, y, square);
        }
    }

    // 遍历"板"
    public void cycle(long time) {
        // 从最后一行开始
        if (topRow == null)
            throw new RuntimeException("BlockBoard was not initialized!");
        tempRow = topRow.above(); // 定位到最后一行
        for (int i = 0; i < height; i++) {
            tempRow.cycle(time, this);
            //逐行向上执行
            tempRow = tempRow.above();
            if (tempRow == null) {
                return;
            }
        }
    }

    // 统计消除的行数
    public int clearLines(int dim) {
        valid = false;
        Row clearPointer = currentRow; //要指向消除行的"指针"
        int clearCounter = 0;//消除技术
        for (int i = 0; i < dim; i++) {
            // 逐行向上遍历 行已满 清除数量+1
            if (clearPointer.isFull()) {
                clearCounter++;
                clearPointer.clear(this, host.game.getAutoDropInterval());
            }
            clearPointer = clearPointer.above();
        }
        // 回到第一行
        currentRow = topRow;
        currentIndex = 0;
        return clearCounter;
    }

    //获得第一行
    public Row getTopRow() {
        return topRow;
    }

    //完成消除
    public void finishClear(Row row) {
        valid = false; // 置为不可绘制
        topRow = row; //要删除的行为第一行
        currentIndex++; // 当前索引加1
        host.display.invalidatePhantom(); // 透明化
    }

    //
    public void interruptClearAnimation() {
        // 从最后一行开始 逐行向上检测
        if (topRow == null)
            throw new RuntimeException("BlockBoard was not initialized!");
        Row tempRow = topRow.above();
        for (int i = 0; i < height; i++) {
            if (tempRow.interrupt(this)) {
                //如果消除了相应的行
                tempRow = topRow.above();
                i = 0;
                valid = false;
            } else {
                tempRow = tempRow.above();
            }
            if (tempRow == null) {
                return;
            }
        }
        host.display.invalidatePhantom(); // 透明化
    }

    // 令板不可绘制
    public void invalidate() {
        valid = false;
    }

    // 获取当前行索引
    public int getCurrentRowIndex() {
        return currentIndex;
    }

    // 获取当前行
    public Row getCurrentRow() {
        return currentRow;
    }

    // 设置当前行索引
    public void setCurrentRowIndex(int index) {
        currentIndex = index;
    }

    // 设置当前行
    public void setCurrentRow(Row row) {
        currentRow = row;
    }

}
