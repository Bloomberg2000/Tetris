package com.bloomberg.tetris.components;

import com.bloomberg.tetris.activities.GameActivity;
import com.bloomberg.tetris.pieces.Piece;

import android.R.color;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.preference.PreferenceManager;

public class Display extends Component {

    private int prevPhantomY; // 预测影响在Y轴的位置
    private boolean dropPhantom; // 下落预测开关
    private Paint paint; // 画笔
    private int gridRowBorder; // 引导线（行）
    private int gridColumnBorder; // 引导线（列）
    private int squaresize; // 方块大小
    private int rowOffset; // 行偏移
    private int rows; // 行数
    private int columnOffset; // 列偏移
    private int columns; // 列数
    private boolean landscapeInitialized; // 画面初始化
    private int prev_top;
    private int prev_bottom;
    private int prev_left;
    private int prev_right;
    private int textLeft;
    private int textTop;
    private int textRight;
    private int textBottom;
    private int textLines;
    private int textSizeH;
    private int textEmptySpacing;
    private Paint textPaint; // 文字画笔
    private Rect textRect; // 容纳文字的图形
    private int textHeight; // 文字高度
    private Paint popUptextPaint; // 文字弹出框画笔

    public Display(GameActivity ga) {
        super(ga);
        invalidatePhantom(); //启动操作预测显示
        setPhantomY(0); // 设置初始位置为0
        landscapeInitialized = false; // 画面未初始化
        paint = new Paint();
        rows = host.getResources().getInteger(com.bloomberg.tetris.R.integer.zeilen); // 初始化行数
        columns = host.getResources().getInteger(com.bloomberg.tetris.R.integer.spalten); // 初始化列数

        //以下数据暂时未知
        squaresize = 1; // unknown at this point!
        prev_top = 1; // unknown at this point!
        prev_bottom = 1; // unknown at this point!
        prev_left = 1; // unknown at this point!
        prev_right = 1; // unknown at this point!

        //
        rowOffset = host.getResources().getInteger(com.bloomberg.tetris.R.integer.zeilenoffset);
        columnOffset = host.getResources().getInteger(com.bloomberg.tetris.R.integer.spaltenoffset);

        // 文字画笔
        textPaint = new Paint();
        // 文字图形
        textRect = new Rect();
        // 文字画笔颜色
        textPaint.setColor(host.getResources().getColor(color.black));
        // 文字画笔透明度
        textPaint.setAlpha(host.getResources().getInteger(com.bloomberg.tetris.R.integer.textalpha));
        // 读取文字保真设置
        textPaint.setAntiAlias(PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_antialiasing", true));

        // 弹出窗口设置
        popUptextPaint = new Paint();
        popUptextPaint.setColor(host.getResources().getColor(color.black));
        popUptextPaint.setAntiAlias(PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_antialiasing", true));
        popUptextPaint.setTextSize(120);
        textSizeH = 1;
        textHeight = 2;
        if (PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_fps", false))
            textLines = 10;
        else
            textLines = 8;
    }


    public void doDraw(Canvas c, int fps) {
        if (c == null)
            return;
        // 开始绘图
        if (!landscapeInitialized) {
            // 读取fps设置
            int fpsEnabled = 0;
            if (PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_fps", false))
                fpsEnabled = 1;
            host.game.getBoard().invalidate();
            // 标记为已绘制
            landscapeInitialized = true;
            // 计算正方形大小 获取屏幕高度
            squaresize = ((c.getHeight() - 1) - 2 * rowOffset) / rows;
            int size2 = ((c.getHeight() - 1) - 2 * columnOffset) / (columns + 4 + host.getResources().getInteger(com.bloomberg.tetris.R.integer.padding_columns));
            if (size2 < squaresize) {
                squaresize = size2;
                rowOffset = ((c.getHeight() - 1) - squaresize * rows) / 2;
            } else
                columnOffset = ((c.getWidth() - 1) - squaresize * (host.getResources().getInteger(com.bloomberg.tetris.R.integer.padding_columns) + 4 + columns)) / 2;

            //根据屏幕大小计算各种元素的大小和偏移量
            gridRowBorder = rowOffset + squaresize * rows;
            gridColumnBorder = columnOffset + squaresize * columns;
            prev_top = rowOffset;
            prev_bottom = rowOffset + 4 * squaresize;
            prev_left = gridColumnBorder + host.getResources().getInteger(com.bloomberg.tetris.R.integer.padding_columns) * squaresize;
            prev_right = prev_left + 4 * squaresize;
            textLeft = prev_left;
            textTop = prev_bottom + 2 * squaresize;
            textRight = (c.getWidth() - 1) - columnOffset;
            textBottom = (c.getHeight() - 1) - rowOffset - squaresize;
            textSizeH = 1;

            /**
             * 自适应文本大小设置源于网络
             */
            // 自适应文本大小设置
            textPaint.setTextSize(textSizeH + 1);
            while (textPaint.measureText("00:00:00") < (textRight - textLeft)) {
                //stuff
                textPaint.getTextBounds("Level:32", 0, 6, textRect);
                textHeight = textRect.height();
                textEmptySpacing = ((textBottom - textTop) - (textLines * (textHeight + 3))) / (3 + fpsEnabled);
                if (textEmptySpacing < 10)
                    break;
                textSizeH++;
                textPaint.setTextSize(textSizeH + 1);
            }

            textPaint.setTextSize(textSizeH - 10);//此处为佛系DEBUG 网上代码并没有办法使文字大小合适 此处为没有道理的强行调整😂
            textPaint.getTextBounds((String) "Level:32", 0, 6, textRect);
            textHeight = textRect.height() + 10;
            textEmptySpacing = ((textBottom - textTop) - (textLines * (textHeight))) / (3 + fpsEnabled);
            host.controls.setPreviewRect(new Rect(prev_left, prev_top, prev_right, prev_bottom));
        }


        c.drawColor(Color.argb(0, 0, 0, 0), android.graphics.PorterDuff.Mode.CLEAR);
        // 在游戏板上绘制活动快 引导线 提示框等信息
        host.game.getBoard().draw(columnOffset, rowOffset, squaresize, c);
        drawActive(columnOffset, rowOffset, squaresize, c);
        if (PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_phantom", false))
            drawPhantom(columnOffset, rowOffset, squaresize, c);
        drawGrid(columnOffset, rowOffset, gridColumnBorder, gridRowBorder, c);
        if (host.controls.isBoardTouched())
            drawTouchIndicator();
        drawPreview(prev_left, prev_top, prev_right, prev_bottom, c);
        drawTextFillBox(c, fps);
        if (PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_popup", true))
            drawPopupText(c);
    }

    private void drawTouchIndicator() {

    }

    // 绘制引导线
    private void drawGrid(int x, int y, int xBorder, int yBorder, Canvas c) {

        paint.setColor(host.getResources().getColor(color.holo_blue_dark)); // 设置 u 阿妹色
        for (int zeilePixel = 0; zeilePixel <= rows; zeilePixel++) {
            c.drawLine(x, y + zeilePixel * squaresize, xBorder, y + zeilePixel * squaresize, paint);
        }
        for (int spaltePixel = 0; spaltePixel <= columns; spaltePixel++) {
            c.drawLine(x + spaltePixel * squaresize, y, x + spaltePixel * squaresize, yBorder, paint);
        }

        // 绘制游戏界面边缘
        paint.setColor(host.getResources().getColor(color.background_light));
        c.drawLine(x, y, x, yBorder, paint);
        c.drawLine(x, y, xBorder, y, paint);
        c.drawLine(xBorder, yBorder, xBorder, y, paint);
        c.drawLine(xBorder, yBorder, x, yBorder, paint);
    }

    // 绘制预览
    private void drawPreview(int left, int top, int right, int bottom, Canvas c) {
        // 块
        drawPreview(left, top, squaresize, c);

        // 引导线
        paint.setColor(host.getResources().getColor(color.holo_blue_dark)); // 引导线颜色
        for (int zeilePixel = 0; zeilePixel <= 4; zeilePixel++) {
            c.drawLine(left, top + zeilePixel * squaresize, right, top + zeilePixel * squaresize, paint);
        }
        for (int spaltePixel = 0; spaltePixel <= 4; spaltePixel++) {
            c.drawLine(left + spaltePixel * squaresize, top, left + spaltePixel * squaresize, bottom, paint);
        }

        // 绘制游戏界面边缘
        paint.setColor(host.getResources().getColor(color.background_light));
        c.drawLine(left, top, right, top, paint);
        c.drawLine(left, top, left, bottom, paint);
        c.drawLine(right, bottom, right, top, paint);
        c.drawLine(right, bottom, left, bottom, paint);
    }

    // 绘制游戏结束弹出框
    private void drawTextFillBox(Canvas c, int fps) {
        // draw Level Text
        c.drawText(host.getResources().getString(com.bloomberg.tetris.R.string.level_title), textLeft, textTop + textHeight, textPaint);
        c.drawText(host.game.getLevelString(), textLeft, textTop + 2 * textHeight, textPaint);

        // draw Score Text
        c.drawText(host.getResources().getString(com.bloomberg.tetris.R.string.score_title), textLeft, textTop + 3 * textHeight + textEmptySpacing, textPaint);
        c.drawText(host.game.getScoreString(), textLeft, textTop + 4 * textHeight + textEmptySpacing, textPaint);

        // draw Time Text
        c.drawText(host.getResources().getString(com.bloomberg.tetris.R.string.time_title), textLeft, textTop + 5 * textHeight + 2 * textEmptySpacing, textPaint);
        c.drawText(host.game.getTimeString(), textLeft, textTop + 6 * textHeight + 2 * textEmptySpacing, textPaint);

        // draw APM Text
        c.drawText(host.getResources().getString(com.bloomberg.tetris.R.string.apm_title), textLeft, textTop + 7 * textHeight + 3 * textEmptySpacing, textPaint);
        c.drawText(host.game.getAPMString(), textLeft, textTop + 8 * textHeight + 3 * textEmptySpacing, textPaint);
    }

    // 绘制活动方块
    private void drawActive(int spaltenOffset, int zeilenOffset, int spaltenAbstand,
                            Canvas c) {
        host.game.getActivePiece().drawOnBoard(spaltenOffset, zeilenOffset, spaltenAbstand, c);
    }

    // 绘制预测欢迎
    private void drawPhantom(int spaltenOffset, int zeilenOffset, int spaltenAbstand, Canvas c) {
        // 获取活动块
        Piece active = host.game.getActivePiece();
        //获取块坐标
        int y = active.getY();
        int x = active.getX();
        active.setPhantom(true);

        //如果欢迎设置为True
        if (dropPhantom) {
            // 保存当先行和行索引
            int backup__currentRowIndex = host.game.getBoard().getCurrentRowIndex();
            Row backup__currentRow = host.game.getBoard().getCurrentRow();

            // 找到可能下落的未知
            int cnt = y + 1;
            while (active.setPositionSimpleCollision(x, cnt, host.game.getBoard())) {
                cnt++;
            }
            // 恢复原来的行和行索引
            host.game.getBoard().setCurrentRowIndex(backup__currentRowIndex);
            host.game.getBoard().setCurrentRow(backup__currentRow);
        } else {
            active.setPositionSimple(x, prevPhantomY);
        }
        // 绘制预测影像
        prevPhantomY = active.getY();
        active.drawOnBoard(spaltenOffset, zeilenOffset, spaltenAbstand, c);
        active.setPositionSimple(x, y);
        active.setPhantom(false);
        dropPhantom = false;
    }


    private void drawPreview(int spaltenOffset, int zeilenOffset, int spaltenAbstand,
                             Canvas c) {
        host.game.getPreviewPiece().drawOnPreview(spaltenOffset, zeilenOffset, spaltenAbstand, c);
    }

    // 绘制弹出窗口文字
    private void drawPopupText(Canvas c) {

        final int offset = 6;
        final int diagonaloffset = 6;

        // 获得文字内容
        String text = host.game.getPopupString();
        popUptextPaint.setTextSize(host.game.getPopupSize());
        popUptextPaint.setColor(host.getResources().getColor(color.black));
        popUptextPaint.setAlpha(host.game.getPopupAlpha());

        // 游戏区域宽度减去一般的文字宽度
        int left = columnOffset + (columns * squaresize / 2) - ((int) popUptextPaint.measureText(text) / 2);
        int top = rowOffset + (rows * squaresize / 2);


        c.drawText(text, offset + left, top, popUptextPaint); // right
        c.drawText(text, diagonaloffset + left, diagonaloffset + top, popUptextPaint); // bottom right
        c.drawText(text, left, offset + top, popUptextPaint); // bottom
        c.drawText(text, -diagonaloffset + left, diagonaloffset + top, popUptextPaint); // bottom left
        c.drawText(text, -offset + left, top, popUptextPaint); // left
        c.drawText(text, -diagonaloffset + left, -diagonaloffset + top, popUptextPaint); // top left
        c.drawText(text, left, -offset + top, popUptextPaint); // top
        c.drawText(text, diagonaloffset + left, -diagonaloffset + top, popUptextPaint); // top right

        // 设置画笔颜色和透明度
        popUptextPaint.setColor(host.game.getPopupColor());
        popUptextPaint.setAlpha(host.game.getPopupAlpha());
        c.drawText(text, left, top, popUptextPaint);

    }


    public void invalidatePhantom() {
        dropPhantom = true;
    }

    // 设置欢迎未知
    public void setPhantomY(int i) {
        prevPhantomY = i;
    }

}
