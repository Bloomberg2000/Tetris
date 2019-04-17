package com.bloomberg.tetris.activities;

import com.bloomberg.tetris.others.BlockBoardView;
import com.bloomberg.tetris.others.WorkThread;
import com.bloomberg.tetris.components.Controls;
import com.bloomberg.tetris.components.Display;
import com.bloomberg.tetris.components.GameState;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Button;
import android.view.View.OnTouchListener;


public class GameActivity extends FragmentActivity {

    public Controls controls;
    public Display display;
    public GameState game;
    private WorkThread mainThread;
    private DefeatDialogFragment dialog;
    private boolean layoutSwap;

    // 游戏状态标记
    public static final int NEW_GAME = 0;
    public static final int RESUME_GAME = 1;

    // 游戏界面被创建时调用
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 无标题页面
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 是否交换下落按钮
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_layoutswap", false)) {
            setContentView(com.bloomberg.tetris.R.layout.activity_game_alt);
            layoutSwap = true;
        } else {
            setContentView(com.bloomberg.tetris.R.layout.activity_game);
            layoutSwap = false;
        }

        // 读取起始参数
        // getIntent得到一个Intent，是指上一个activity启动的intent，这个方法返回intent对象，
        // 然后调用intent.getExtras（）得到intent所附带的额外数据
        Bundle bundle = getIntent().getExtras();
        int value = NEW_GAME;

        // 创建游戏状态组件
        // 保存横竖屏状态
        game = (GameState) getLastCustomNonConfigurationInstance();
        if (game == null) {
            // 检查是否需要Remuse游戏
            if (bundle != null)
                value = bundle.getInt("mode");

            //建立新游戏 创建新的游戏状态（线程）
            if ((value == NEW_GAME)) {
                game = GameState.getNewInstance(this);
                // 从Bundle中读取初始级别
                game.setLevel(bundle.getInt("level"));
            } else {
                // Remuse读取对象当前的游戏进程
                game = GameState.getInstance(this);
            }
        }
        // 把游戏界面 操控等内容连接到Activity
        game.reconnect(this);
        // 创建会话对象
        dialog = new DefeatDialogFragment();
        // 创建控制对象
        controls = new Controls(this);
        // 创建显示对象
        display = new Display(this);

        // 初始化组件
        // 从Bundle中获取游戏模式 玩家姓名
        if (bundle != null) {
            value = bundle.getInt("mode");
            if (bundle.getString("playername") != null)
                game.setPlayerName(bundle.getString("playername"));
        } else {
            game.setPlayerName(getResources().getString(com.bloomberg.tetris.R.string.anonymous));
        }
        // 会话框不可通过返回键取消
        dialog.setCancelable(false);
        // 如果游戏结束 显示游戏结束对话框
        if (!game.isResumable()) {
            gameOver(game.getScore(), game.getTimeString(), game.getAPM());
        }

        //注册按钮回调方法
        //MotionEvent.ACTION_DOWN:  //按下 = 0
        //MotionEvent.ACTION_MOVE:  //移动 = 2
        //MotionEvent.ACTION_UP:    //抬起 = 1

        // 暂停键
        findViewById(com.bloomberg.tetris.R.id.pausebutton_1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                GameActivity.this.finish();
            }
        });
        // 注册BoardView
        findViewById(com.bloomberg.tetris.R.id.boardView).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    controls.boardPressed(event.getX(), event.getY());
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    controls.boardReleased();
                }
                return true;
            }
        });
        // 注册向右移动按钮
        findViewById(com.bloomberg.tetris.R.id.rightButton).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 获得操作
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    controls.rightButtonPressed();
                    findViewById(com.bloomberg.tetris.R.id.rightButton).setPressed(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    controls.rightButtonReleased();
                    findViewById(com.bloomberg.tetris.R.id.rightButton).setPressed(false);
                }
                return true;
            }
        });
        // 注册向左移动按钮
        findViewById(com.bloomberg.tetris.R.id.leftButton).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    controls.leftButtonPressed();
                    findViewById(com.bloomberg.tetris.R.id.leftButton).setPressed(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    controls.leftButtonReleased();
                    findViewById(com.bloomberg.tetris.R.id.leftButton).setPressed(false);
                }
                return true;
            }
        });
        // 软下落按钮
        findViewById(com.bloomberg.tetris.R.id.softDropButton).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    controls.downButtonPressed();
                    findViewById(com.bloomberg.tetris.R.id.softDropButton).setPressed(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    controls.downButtonReleased();
                    findViewById(com.bloomberg.tetris.R.id.softDropButton).setPressed(false);
                }
                return true;
            }
        });
        // 硬下落按钮
        findViewById(com.bloomberg.tetris.R.id.hardDropButton).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    controls.dropButtonPressed();
                    findViewById(com.bloomberg.tetris.R.id.hardDropButton).setPressed(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    controls.dropButtonReleased();
                    findViewById(com.bloomberg.tetris.R.id.hardDropButton).setPressed(false);
                }
                return true;
            }
        });

        // 向右旋转按钮
        findViewById(com.bloomberg.tetris.R.id.rotateRightButton).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    controls.rotateRightPressed();
                    findViewById(com.bloomberg.tetris.R.id.rotateRightButton).setPressed(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    controls.rotateRightReleased();
                    findViewById(com.bloomberg.tetris.R.id.rotateRightButton).setPressed(false);
                }
                return true;
            }
        });
        // 向左旋转按钮
        findViewById(com.bloomberg.tetris.R.id.rotateLeftButton).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    controls.rotateLeftPressed();
                    findViewById(com.bloomberg.tetris.R.id.rotateLeftButton).setPressed(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    controls.rotateLeftReleased();
                    findViewById(com.bloomberg.tetris.R.id.rotateLeftButton).setPressed(false);
                }
                return true;
            }
        });

        // 初始化BoardView
        ((BlockBoardView) findViewById(com.bloomberg.tetris.R.id.boardView)).init();
        ((BlockBoardView) findViewById(com.bloomberg.tetris.R.id.boardView)).setHost(this);
    }

    // 在完成构建时 被BlockBoardView调用
    public void startGame(BlockBoardView caller) {
        // 创建工作线程
        mainThread = new WorkThread(this, caller.getHolder());
        mainThread.setFirstTime(false);
        game.setRunning(true); // 设置游戏运行状态
        mainThread.setRunning(true); // 设置线程运行状态
        mainThread.start(); // 唤起线程
    }

    // 被BlockBoardView调用
    public void destroyWorkThread() {
        boolean retry = true;
        mainThread.setRunning(false);
        while (retry) {
            try {
                // join方法的主要作用就是同步，它可以使得线程之间的并行执行变为串行执行
                mainThread.join();
                retry = false;
            } catch (InterruptedException e) {

            }
        }
    }

    // 游戏结束时 被GameState调用
    public void putScore(long score) {
        String playerName = game.getPlayerName();
        if (playerName == null || playerName.equals(""))
            playerName = getResources().getString(com.bloomberg.tetris.R.string.anonymous);//"Anonymous";

        // 向下一个Activity传递数据
        Intent data = new Intent();
        data.putExtra(MainActivity.PLAYERNAME, playerName);
        data.putExtra(MainActivity.SCORE_KEY, score);
        setResult(MainActivity.RESULT_OK, data);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        game.setRunning(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        game.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 检查设置是否被修改 进而调整视图
        boolean tempswap = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_layoutswap", false);
        if (layoutSwap != tempswap) {
            layoutSwap = tempswap;
            if (layoutSwap) {
                setContentView(com.bloomberg.tetris.R.layout.activity_game_alt);
            } else {
                setContentView(com.bloomberg.tetris.R.layout.activity_game);
            }
        }
        game.setRunning(true);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return game;
    }

    // 游戏结束对话框
    public void gameOver(long score, String gameTime, int apm) {
        dialog.setData(score, gameTime, apm);
        dialog.show(getSupportFragmentManager(), "hamster");
    }

}
