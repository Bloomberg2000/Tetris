package com.bloomberg.tetris.components;

import android.content.Context;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import com.bloomberg.tetris.activities.GameActivity;
import com.bloomberg.tetris.pieces.Piece;

/**
 * 时间计算部分 来源GitHub CSDN
 */

// 游戏控制类
public class Controls extends Component {
    // 被操作的版
    private Board board;
    //private boolean initialized;
    private Vibrator vibrator; // 震动器对象
    private int vibrationOffset; // 储存震动持续时间
    private long shortVibeTime; // 上次进行短震动时间
    private int[] lineThresholds; // 各级别消除行数阈值 达到阈值升级

    // 用户控制
    private boolean playerSoftDrop; //软下落
    private boolean clearPlayerSoftDrop; //终端软下落
    private boolean playerHardDrop; // 硬下落
    private boolean leftMove; // 向左移动
    private boolean rightMove; // 向右移动
    private boolean continuousSoftDrop; // 连续软下落
    private boolean continuousLeftMove; // 连续左移
    private boolean continuousRightMove; // 连续右移
    private boolean clearLeftMove; //终端左移
    private boolean clearRightMove; // 终端右移
    private boolean leftRotation; // 向左旋转
    private boolean rightRotation; // 向右旋转
    private boolean buttonVibrationEnabled; // 触觉反馈开关
    private boolean eventVibrationEnabled; // 事件反馈开关
    private int initialHIntervalFactor; // 初始水平加速度
    private int initialVIntervalFactor; // 初始软下落加速度
    private Rect previewBox; // 预览框
    private boolean boardTouched; //触摸时间

    //  控制区构造方法
    public Controls(GameActivity ga) {
        super(ga);

        lineThresholds = host.getResources().getIntArray(com.bloomberg.tetris.R.array.line_thresholds);

        shortVibeTime = 0;// 短震动时间

        vibrator = (Vibrator) host.getSystemService(Context.VIBRATOR_SERVICE); //获得系统震动服务

        //从设置中获取设置数据
        //触觉反馈默认关闭
        buttonVibrationEnabled = PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_vibration_button", false);
        //事件反馈默认关闭
        eventVibrationEnabled = PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_vibration_events", false);
        try {
            //获取震动持续时间
            vibrationOffset = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(host).getString("pref_vibDurOffset", "0"));
        } catch (NumberFormatException e) {
            vibrationOffset = 0;
        }
        //获取 水平&&竖直加速度 设置
        if (PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_accelerationH", true)) {
            initialHIntervalFactor = 2;
        } else {
            initialHIntervalFactor = 1;
        }
        if (PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_accelerationV", true)) {
            initialVIntervalFactor = 2;
        } else {
            initialVIntervalFactor = 1;
        }
        // 所有操作状态全部为空
        playerSoftDrop = false;
        leftMove = false;
        rightMove = false;
        leftRotation = false;
        rightRotation = false;
        clearLeftMove = false;
        clearRightMove = false;
        clearPlayerSoftDrop = false;
        continuousSoftDrop = false;
        continuousLeftMove = false;
        continuousRightMove = false;
        previewBox = null;
        boardTouched = false;
    }

    // 块撞到墙面震动
    public void vibrateWall() {
        if (vibrator == null)
            return;
        if (!eventVibrationEnabled)
            return;

        //设置手机振动 参数为 震动时间
        vibrator.vibrate(host.game.getMoveInterval() + vibrationOffset);
    }

    // 震动关闭
    public void cancelVibration() {
        vibrator.cancel();
    }

    // 落地震动
    public void vibrateBottom() {
        if (vibrator == null)
            return;
        if (!eventVibrationEnabled)
            return;
        vibrator.cancel();
        //以给定的模式振动。
        // 在一组数组中传递，这些持续时间是以毫秒为单位打开或关闭振动器的持续时间。
        // 第一个值表示在打开振动器之前等待的毫秒数。
        // 下一个值指示将振荡器保持在关闭之前的毫秒数。
        // 随后的值交替在毫秒内持续时间，使振动器关闭或使振动器打开。
        vibrator.vibrate(new long[]{0, 5 + vibrationOffset, 30 + vibrationOffset, 20 + vibrationOffset}, -1);
    }

    // 短震动
    public void vibrateShort() {
        if (vibrator == null)
            return;
        if (!buttonVibrationEnabled)
            return;
        if ((host.game.getTime() - shortVibeTime) > (host.getResources().getInteger(com.bloomberg.tetris.R.integer.shortVibeInterval) + vibrationOffset)) {
            shortVibeTime = host.game.getTime();//上次进行短震动短时间为当前时间
            // 设置震动
            vibrator.vibrate(vibrationOffset);
        }
    }


    // 向左旋转被按下
    public void rotateLeftPressed() {
        leftRotation = true; // 标记
        host.game.action(); // 记录
        vibrateShort(); // 短震动
    }

    // 向左旋转被释放
    public void rotateLeftReleased() {

    }

    // 向右旋转被按下
    public void rotateRightPressed() {
        rightRotation = true; // 标记
        host.game.action(); // 记录
        vibrateShort(); // 短震动
    }

    // 向右旋转被释放
    public void rotateRightReleased() {
        //Thread.yield();
    }

    // 软下落按钮被按下
    public void downButtonPressed() {
        host.game.action(); // 记录
        playerSoftDrop = true; // 标记
        clearPlayerSoftDrop = false; // 标记
        vibrateShort(); // 震动
        // 设置用户移动操作时间
        host.game.setNextPlayerDropTime(host.game.getTime());
    }


    // 软下落按钮被释放
    public void downButtonReleased() {
        clearPlayerSoftDrop = true; // 标记
        vibrateShort(); // 震动
        //Thread.yield();
    }

    // 硬下落按钮被按下
    public void dropButtonPressed() {
        if (!host.game.getActivePiece().isActive()) {
            return;
        }
        host.game.action(); // 记录
        playerHardDrop = true; // 标记
        // 短震动
        if (buttonVibrationEnabled & !eventVibrationEnabled) {
            vibrateShort();
        }
    }

    // 硬下落按钮被释放
    public void dropButtonReleased() {

    }

    // 左移按钮被按下
    public void leftButtonPressed() {
        host.game.action(); //记录
        clearLeftMove = false; // 标记
        leftMove = true;
        rightMove = false;
        // 设置用户移动操作时间
        host.game.setNextPlayerMoveTime(host.game.getTime());
    }

    // 左移按钮被释放
    public void leftButtonReleased() {
        clearLeftMove = true; //标记
        cancelVibration(); // ring 之震动
    }

    // 右移按钮被按下
    public void rightButtonPressed() {
        host.game.action(); //记录
        clearRightMove = false; // 标记
        rightMove = true;
        leftMove = false;
        // 设置用户移动操作时间
        host.game.setNextPlayerMoveTime(host.game.getTime());
    }

    // 右移按钮被释放
    public void rightButtonReleased() {
        clearRightMove = true; // 标记
        cancelVibration(); // 取消震动
    }

    // 运行循环
    public void cycle(long tempTime) {
        long gameTime = host.game.getTime();  // 获取游戏时间
        Piece active = host.game.getActivePiece(); // 获取可移动块
        Board board = host.game.getBoard(); // 获取板
        int maxLevel = host.game.getMaxLevel(); // 获取最高级别

        // 左旋
        if (leftRotation) {
            leftRotation = false; // 标记
            active.turnLeft(board); // 旋转
            host.display.invalidatePhantom(); // 下落预测
        }

        // 右旋
        if (rightRotation) {
            rightRotation = false; // 标记
            active.turnRight(board); // 旋转
            host.display.invalidatePhantom(); // 下落预测
        }

        // 重设移动时间
        if ((!leftMove && !rightMove) && (!continuousLeftMove && !continuousRightMove))
            host.game.setNextPlayerMoveTime(gameTime);

        // 左移
        if (leftMove) {
            continuousLeftMove = true; // 持续左移标记
            leftMove = false; // 左移标记关闭
            if (active.moveLeft(board)) {
                // 如果移动成功
                vibrateShort(); // 短震动
                host.display.invalidatePhantom(); // 下落预测
                // 设置用户移动操作时间
                host.game.setNextPlayerMoveTime(host.game.getNextPlayerMoveTime() + initialHIntervalFactor * host.game.getMoveInterval());
            } else {
                // 移动失败
                vibrateWall(); // 墙面震动
                host.game.setNextPlayerMoveTime(gameTime);
            }

        } else if (continuousLeftMove) {
            if (gameTime >= host.game.getNextPlayerMoveTime()) {
                if (active.moveLeft(board)) {
                    // 如果成功移动
                    vibrateShort(); // 短震动
                    host.display.invalidatePhantom(); // 下落预测
                    // 设置用户移动操作时间
                    host.game.setNextPlayerMoveTime(host.game.getNextPlayerMoveTime() + host.game.getMoveInterval());
                } else { // failed move
                    vibrateWall();
                    host.game.setNextPlayerMoveTime(gameTime);
                }
            }
            // 停止左移
            if (clearLeftMove) {
                continuousLeftMove = false;
                clearLeftMove = false;
            }
        }

        // 右移 与左移完全相同不单独注释
        if (rightMove) {
            continuousRightMove = true;
            rightMove = false;
            if (active.moveRight(board)) {
                vibrateShort();
                host.display.invalidatePhantom();
                host.game.setNextPlayerMoveTime(host.game.getNextPlayerMoveTime() + initialHIntervalFactor * host.game.getMoveInterval());
            } else {
                vibrateWall();
                host.game.setNextPlayerMoveTime(gameTime);
            }

        } else if (continuousRightMove) {
            if (gameTime >= host.game.getNextPlayerMoveTime()) {
                if (active.moveRight(board)) {
                    vibrateShort();
                    host.display.invalidatePhantom();
                    host.game.setNextPlayerMoveTime(host.game.getNextPlayerMoveTime() + host.game.getMoveInterval());
                } else {
                    vibrateWall();
                    host.game.setNextPlayerMoveTime(gameTime);
                }
            }

            if (clearRightMove) {
                continuousRightMove = false;
                clearRightMove = false;
            }
        }
        /**
         *  host.game.setNextDropTime 自动操作下落完毕时间
         *  host.game.setNextPlayerDropTime 用户操作下落完毕时间
         */

        // 硬下落
        if (playerHardDrop) {
            board.interruptClearAnimation(); // 中断进行中的动画
            int hardDropDistance = active.hardDrop(false, board); // 获取下落行数
            vibrateBottom(); //落地震动
            host.game.clearLines(true, hardDropDistance);// 消除检测
            host.game.pieceTransition(eventVibrationEnabled); // 过渡
            board.invalidate(); // 此块不可移动

            playerHardDrop = false; // 标记

            // 处理消除后的用户级别
            if ((host.game.getLevel() < maxLevel) && (host.game.getClearedLines() > lineThresholds[Math.min(host.game.getLevel(), maxLevel - 1)]))
                host.game.nextLevel();
            // 处理用户操作时间
            host.game.setNextDropTime(gameTime + host.game.getAutoDropInterval());
            host.game.setNextPlayerDropTime(gameTime);
        } else if (playerSoftDrop) {
            // 软下落
            playerSoftDrop = false;// 标记
            continuousSoftDrop = true;
            if (!active.drop(board)) {
                // 成功下落完毕（落下且定住）
                vibrateBottom(); // 震动
                host.game.clearLines(false, 0); // 消除检测
                host.game.pieceTransition(eventVibrationEnabled); // 过渡
                board.invalidate(); // 此块不可移动
            } else {
                // 还悬浮在空中
                host.game.incSoftDropCounter();
            }
            // 处理消除后的用户级别
            if ((host.game.getLevel() < maxLevel) && (host.game.getClearedLines() > lineThresholds[Math.min(host.game.getLevel(), maxLevel - 1)]))
                host.game.nextLevel();
            // 处理用户操作时间
            host.game.setNextDropTime(host.game.getNextPlayerDropTime() + host.game.getAutoDropInterval());
            host.game.setNextPlayerDropTime(host.game.getNextPlayerDropTime() + initialVIntervalFactor * host.game.getSoftDropInterval());
        } else if (continuousSoftDrop) {
            // 持续软下落
            if (gameTime >= host.game.getNextPlayerDropTime()) {
                if (!active.drop(board)) {
                    // 成功下落完毕（落下且定住）
                    vibrateBottom(); // 震动
                    host.game.clearLines(false, 0); // 消除检测
                    host.game.pieceTransition(eventVibrationEnabled); // 过渡
                    board.invalidate(); // 此块不可移动
                } else {
                    // 还悬浮在空中
                    host.game.incSoftDropCounter();
                }
                // 处理消除后的用户级别
                if ((host.game.getLevel() < maxLevel) && (host.game.getClearedLines() > lineThresholds[Math.min(host.game.getLevel(), maxLevel - 1)]))
                    host.game.nextLevel();
                // 处理用户操作时间
                host.game.setNextDropTime(host.game.getNextPlayerDropTime() + host.game.getAutoDropInterval());
                host.game.setNextPlayerDropTime(host.game.getNextPlayerDropTime() + host.game.getSoftDropInterval());
            } else if (gameTime >= host.game.getNextDropTime()) {
                // 如果自动下落比用户操作速度快 执行自动下落
                if (!active.drop(board)) {
                    // 成功下落完毕（落下且定住）
                    vibrateBottom(); // 震动
                    host.game.clearLines(false, 0); // 消除检测
                    host.game.pieceTransition(eventVibrationEnabled); // 过渡
                    board.invalidate(); // 此块不可移动
                }
                // 处理消除后的用户级别
                if ((host.game.getLevel() < maxLevel) && (host.game.getClearedLines() > lineThresholds[Math.min(host.game.getLevel(), maxLevel - 1)]))
                    host.game.nextLevel();
                // 处理用户操作时间
                host.game.setNextDropTime(host.game.getNextDropTime() + host.game.getAutoDropInterval());
                host.game.setNextPlayerDropTime(host.game.getNextDropTime() + host.game.getSoftDropInterval());
            }

            // 停止持续谢咯
            if (clearPlayerSoftDrop) {
                continuousSoftDrop = false;
                clearPlayerSoftDrop = false;
            }

            // 如果没有用户操作处理自动下落
        } else if (gameTime >= host.game.getNextDropTime()) {
            if (!active.drop(board)) {
                // 成功下落完毕（落下且定住）
                vibrateBottom(); // 震动
                host.game.clearLines(false, 0); // 消除检测
                host.game.pieceTransition(eventVibrationEnabled); // 过渡
                board.invalidate(); // 此块不可移动
            }
            // 处理消除后的用户级别
            if ((host.game.getLevel() < maxLevel) && (host.game.getClearedLines() > lineThresholds[Math.min(host.game.getLevel(), maxLevel - 1)]))
                host.game.nextLevel();
            // 处理用户操作时间
            host.game.setNextDropTime(host.game.getNextDropTime() + host.game.getAutoDropInterval());
            host.game.setNextPlayerDropTime(host.game.getNextDropTime());

        } else {
            host.game.setNextPlayerDropTime(gameTime);
        }
    }

    // 设置板
    public void setBoard(Board instance2) {
        this.board = instance2;
    }

    // 获取板
    public Board getBoard() {
        return this.board;
    }

    // 没有用 重新连接Activity
    @Override
    public void reconnect(GameActivity gameActivity) {
        super.reconnect(gameActivity);
        vibrator = (Vibrator) gameActivity.getSystemService(Context.VIBRATOR_SERVICE);

        buttonVibrationEnabled = PreferenceManager.getDefaultSharedPreferences(gameActivity).getBoolean("pref_vibration_button", false);
        eventVibrationEnabled = PreferenceManager.getDefaultSharedPreferences(gameActivity).getBoolean("pref_vibration_events", false);
        try {
            vibrationOffset = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(gameActivity).getString("pref_vibDurOffset", "0"));
        } catch (NumberFormatException e) {
            vibrationOffset = 0;
        }
    }

    // 没有用 断开连接
    @Override
    public void disconnect() {
        super.disconnect();
        vibrator = null;
    }

    // 板被触摸
    public void boardPressed(float x, float y) {
        if (previewBox == null)
            return;

        boardTouched = true;
        //  判断触摸是否在previewBox内
        if (previewBox.contains((int) x, (int) y))
            host.game.hold();
    }

    // 板被释放
    public void boardReleased() {
        boardTouched = false;
    }

    // 设置预览
    public void setPreviewRect(Rect rect) {
        previewBox = rect;
    }

    // 返回板是否被触摸
    public boolean isBoardTouched() {
        return boardTouched;
    }

}
