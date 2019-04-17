package com.bloomberg.tetris.activities;

import com.bloomberg.tetris.components.GameState;
import com.bloomberg.tetris.database.Highscore;
import com.bloomberg.tetris.database.ScoreDataSource;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Button;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends ListActivity {

    public static final int SCORE_REQUEST = 0x0;
    // 此键用于访问玩家名称，玩家的名字在游戏完成后作为游戏意图返回
    public static final String PLAYERNAME = "com.blockinger.game.activities.playername";
    // 此键用于访问玩家得分，玩家的得分在游戏完成后作为游戏意图返回
    public static final String SCORE_KEY = "com.blockinger.game.activities.score";
    // 分数储存封装类对象
    public ScoreDataSource datasource;
    // CursorAdapter是与数据库交互Adapter的最常用的类
    private SimpleCursorAdapter adapter;
    // 初始速率调节框
    private AlertDialog.Builder startLevelDialog;
    // 储存初始速率
    private int startLevel;
    private View dialogView;
    // 弹出窗口滑动条对象
    private SeekBar leveldialogBar;
    // 弹出窗口文字对象
    private TextView leveldialogtext;

    // Activity启动时自动调用
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.bloomberg.tetris.R.layout.activity_main);
        // 用于读取设置数据
        PreferenceManager.setDefaultValues(this, com.bloomberg.tetris.R.xml.simple_preferences, true);
        PreferenceManager.setDefaultValues(this, com.bloomberg.tetris.R.xml.advanced_preferences, true);

        // 数据库管理模块
        Cursor mc;
        // 分数储存封装类对象构造方法
        datasource = new ScoreDataSource(this);
        // 打开数据库
        datasource.open();
        // 在HighScore中读取所有行 返回指向这些行的光标储存在Cursor中
        mc = datasource.getCursor();
        // 使用 SimpleCursorAdapter 把Cursor通过ListView展示
        // 从mc中读取分数和用户名存在String[]中与ListView中text1，text2连接
        // CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER。设置标志用来添加一个监听器，监听着参数cursor的数据是否有更变。
        // SimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags)
        adapter = new SimpleCursorAdapter(
                this,
                com.bloomberg.tetris.R.layout.blockinger_list_item,
                mc,
                new String[]{Highscore.COLUMN_SCORE, Highscore.COLUMN_PLAYERNAME},
                new int[]{com.bloomberg.tetris.R.id.text1, com.bloomberg.tetris.R.id.text2},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        // 把数据映射到界面里边
        setListAdapter(adapter);

        // 创建初始速率对话框
        // 速率级别默认为0
        startLevel = 0;
        // 创建弹出对话框对象
        startLevelDialog = new AlertDialog.Builder(this);
        // 设置标题
        startLevelDialog.setTitle(com.bloomberg.tetris.R.string.startLevelDialogTitle);
        // 按返回键不可退出
        startLevelDialog.setCancelable(false);
        // 取消按钮配置
        startLevelDialog.setNegativeButton(com.bloomberg.tetris.R.string.startLevelDialogCancel, new DialogInterface.OnClickListener() {
            //点击时关闭对话框
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // 游戏开始按钮配置
        startLevelDialog.setPositiveButton(com.bloomberg.tetris.R.string.startLevelDialogStart, new DialogInterface.OnClickListener() {
            //点击时调用start方法开始游戏
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.start();
            }
        });
    }

    //onCreateOptionsMenu()创建菜单Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //  实例化menu目录下的Menu布局文件
        getMenuInflater().inflate(com.bloomberg.tetris.R.menu.main, menu);
        return true;
    }

    //菜单目录被选择时运行次方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.bloomberg.tetris.R.id.action_settings:
                //Intent 解决Android应用的各项组件之间的通讯 核心作用就是“跳转”
                Intent intent = new Intent(this, SettingsActivity.class);
                //启动新的Activity
                startActivity(intent);
                return true;
            case com.bloomberg.tetris.R.id.action_exit:
                GameState.destroy();
                MainActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // 开始游戏
    public void start() {
        //跳转到GameActivity
        Intent intent = new Intent(this, GameActivity.class);
        //Bundle用于传递数据和对象
        Bundle b = new Bundle();
        //保存游戏模式
        b.putInt("mode", GameActivity.NEW_GAME);
        //保存初始级别
        b.putInt("level", startLevel);
        //保存用户名
        b.putString("playername", ((TextView) findViewById(com.bloomberg.tetris.R.id.nicknameEditView)).getText().toString());
        //把Bundle中的数据传递给intent
        intent.putExtras(b);
        //startActivityForResult(Intent intent, int requestCode);
        // 第一个参数：一个Intent对象，用于携带将跳转至下一个界面中使用的数据，使用putExtra(A,B)方法，此处存储的数据类型特别多，基本类型全部支持。
        // 第二个参数：如果>= 0,当Activity结束时requestCode将归还在onActivityResult()中。以便确定返回的数据是从哪个Activity中返回，用来标识目标activity。
        startActivityForResult(intent, SCORE_REQUEST);
    }

    //GameActivity完成后被调用
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != SCORE_REQUEST)
            return;
        if (resultCode != RESULT_OK)
            return;
        //获取用户名
        String playerName = data.getStringExtra(PLAYERNAME);
        //获取分数
        long score = data.getLongExtra(SCORE_KEY, 0);
        //将分数储存在数据库中
        datasource.open();
        datasource.createScore(score, playerName);
    }

    //点击开始新游戏按钮时调用此方法
    public void onClickStart(View view) {
        //将布局填充成View对象
        dialogView = getLayoutInflater().inflate(com.bloomberg.tetris.R.layout.seek_bar_dialog, null);
        //绑定文字为动态对象
        leveldialogtext = dialogView.findViewById(com.bloomberg.tetris.R.id.leveldialogleveldisplay);
        //绑定滚动条为动态对象
        leveldialogBar = dialogView.findViewById(com.bloomberg.tetris.R.id.levelseekbar);
        //设置滚动条移动事件
        leveldialogBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            // arg0 被操作的滚动条
            // arg1 progress 目前数值(默认范围：0-100)
            // arg2 变化是否由用户操作产生
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                //显示当前数值
                leveldialogtext.setText("" + arg1);
                //储存速率级别
                startLevel = arg1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

        });
        //更新滚动条
        leveldialogBar.setProgress(startLevel);
        //更新滚动条数据
        leveldialogtext.setText("" + startLevel);
        //弹出对话框
        startLevelDialog.setView(dialogView);
        startLevelDialog.show();
    }

    //点击继续游戏按钮时调用此方法
    public void onClickResume(View view) {
        //跳转到GameActivity
        Intent intent = new Intent(this, GameActivity.class);
        //Bundle用于传递数据和对象
        Bundle b = new Bundle();
        //保存游戏模式
        b.putInt("mode", GameActivity.RESUME_GAME); //Your id
        //保存用户名
        b.putString("playername", ((TextView) findViewById(com.bloomberg.tetris.R.id.nicknameEditView)).getText().toString()); //Your id
        //把Bundle中的数据传递给intent
        intent.putExtras(b);
        //startActivityForResult(Intent intent, int requestCode);
        // 第一个参数：一个Intent对象，用于携带将跳转至下一个界面中使用的数据，使用putExtra(A,B)方法，此处存储的数据类型特别多，基本类型全部支持。
        // 第二个参数：如果>= 0,当Activity结束时requestCode将归还在onActivityResult()中。以便确定返回的数据是从哪个Activity中返回，用来标识目标activity。
        startActivityForResult(intent, SCORE_REQUEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        datasource.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        datasource.close();
    }

    //从后台回到应用时调用
    @Override
    protected void onResume() {
        super.onResume();
        //重新读取数据库刷新数据
        datasource.open();
        Cursor cursor = datasource.getCursor();
        adapter.changeCursor(cursor);
        //判断上局游戏是否结束 决定继续游戏按钮是否可用
        if (!GameState.isFinished()) {
            findViewById(com.bloomberg.tetris.R.id.resumeButton).setEnabled(true);
        } else {
            findViewById(com.bloomberg.tetris.R.id.resumeButton).setEnabled(false);
        }
    }
}
