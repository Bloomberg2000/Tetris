package com.bloomberg.tetris.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 最高分储存模块
 */

public class Highscore extends SQLiteOpenHelper {
    // 预设字段 避免重复输入
    public static final String TABLE_HIGHSCORES = "highscores";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SCORE = "score";
    public static final String COLUMN_PLAYERNAME = "playername";
    private static final String DATABASE_NAME = "highscores.db";
    private static final int DATABASE_VERSION = 1;

    // 数据库创建SQL语句
    private static final String DB_CREATE_SQL = "create table "
            + TABLE_HIGHSCORES + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_SCORE
            + " integer, " + COLUMN_PLAYERNAME
            + " text);";

    // 构造方法需要四个参数
    // 上下文环境(Contest)，数据库名字，游标工厂（通常是 Null 默认），一个代表正在使用的数据库模型板本的整数。
    public Highscore(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //onCreare()方法创建数据库时自动调用
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 执行刚创建好的SQL语句
        db.execSQL(DB_CREATE_SQL);
    }

    //onUpgrade()方法
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //在日志中打印Warning
        Log.w(Highscore.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        //销毁数据库
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIGHSCORES);
        //创建新的数据库
        onCreate(db);
    }

}
