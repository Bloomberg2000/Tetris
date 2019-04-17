package com.bloomberg.tetris.others;

import com.bloomberg.tetris.activities.GameActivity;

import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.view.SurfaceHolder;

// 游戏进程处理类
public class WorkThread extends Thread {
    private SurfaceHolder surfaceHolder; // SurfaceHolder对象
    private boolean runFlag = false; // 运行标记
    boolean firstTime = true; // 是否为初次
    public long lastFrameDuration = 0; // 最后一帧持续时间
    private long lastFrameStartingTime = 0; // 最后一帧开始时间
    int fpslimit; // 帧率限制
    long lastDelay; // 进程休眠时间
    private GameActivity host; // 宿主Activity

    // 游戏进程构造方法
    public WorkThread(GameActivity ga, SurfaceHolder sh) {
        // 分配宿主进程
        host = ga;
        // 分配游戏界面处理
        this.surfaceHolder = sh;
        /**
         * 帧率处理思路及部分代码来自CSDN
         */
        try {
            // 从string.xml读取用户设置的目标帧率
            fpslimit = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(host).getString("pref_fpslimittext", "35"));
        } catch (NumberFormatException e) {
            // 数据格式错误调节为25
            fpslimit = 25;
        }
        // 如果用户设置的帧率 < 5 则设置为5
        if (fpslimit < 5)
            fpslimit = 5; //PPT模式

        lastDelay = 100;
    }

    // 设置运行模式
    public void setRunning(boolean run) {
        this.runFlag = run;
    }

    // 进程启动重载方法
    @Override
    public void run() {
        Canvas canvas;
        long tempTime = System.currentTimeMillis(); // 获取当前时间
        long fpsUpdateTime = tempTime + 200; //帧率每200ms更新一次
        int frames = 0; // 帧
        int frameCounter[] = {0, 0, 0, 0, 0}; // 记录帧
        int i = 0;

        while (this.runFlag) {

            if (firstTime) {
                firstTime = false;
                continue;
            }
            tempTime = System.currentTimeMillis(); //获取当前时间
            /**
             * 帧率处理思路及部分代码来自CSDN
             */
            try {
                // 从string.xml读取用户设置的目标帧率
                fpslimit = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(host).getString("pref_fpslimittext", "35"));
            } catch (NumberFormatException e) {
                fpslimit = 35;
            }
            if (fpslimit < 5) {
                fpslimit = 5;
            }

            //在设置中读取帧率限制模式是否开启 默认关闭
            if (PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_fpslimit", false)) {
                lastFrameDuration = tempTime - lastFrameStartingTime; // 最后一帧持续时间 为当前时间减去开始时间
                // 1000ms/帧率 即为每帧理论持续时间
                if (lastFrameDuration > (1000.0f / fpslimit)) {
                    // 如果持续时间过长 则减少进程休眠时间（但应保持>0）
                    lastDelay = Math.max(0, lastDelay - 25);
                } else {
                    // 持续时间短 增加进程休眠时间
                    lastDelay += 25;
                }

                if (lastDelay == 0) {
                    // 休眠时间为0 不做任何操作
                } else {
                    // 开始休眠
                    try {
                        Thread.sleep(lastDelay);
                    } catch (InterruptedException e) {
                        // 抛出异常暂不处理
                    }
                }
                // 记录当前帧开始时间
                lastFrameStartingTime = tempTime;
            }
            // 当前时间大于等于帧率刷新时间时
            if (tempTime >= fpsUpdateTime) {
                // 5 * 5 = 25
                i = (i + 1) % 5;
                fpsUpdateTime += 200; // 计算下次刷新时间
                // 计算总帧数
                frames = frameCounter[0] + frameCounter[1] + frameCounter[2] + frameCounter[3] + frameCounter[4];
                // 计数器置空
                frameCounter[i] = 0;
            }
            //每次经过帧率计数+1
            frameCounter[i]++;
            /**
             * 帧率处理到此结束
             */

            // 判断是否可以运行cycle()
            if (host.game.cycle(tempTime)) {
                host.controls.cycle(tempTime);
            }
            host.game.getBoard().cycle(tempTime);

            /**
             * Java并发编程部分来源于GitHub
             */
            canvas = null;
            try {
                // 获得canvas的大小并锁定canvas 其他线程不可进行
                canvas = this.surfaceHolder.lockCanvas(null);
                // synchronized是Java中的关键字，是一种同步锁
                synchronized (this.surfaceHolder) {
                    host.display.doDraw(canvas, frames);
                }
            } finally {
                // 解锁并释放画板
                if (canvas != null) {
                    this.surfaceHolder.unlockCanvasAndPost(canvas);

                }
            }
        }
    }

    // 设置是否为第一次运行
    public void setFirstTime(boolean b) {
        firstTime = b;
    }

}