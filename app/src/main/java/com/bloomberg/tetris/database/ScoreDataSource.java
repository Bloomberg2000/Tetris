package com.bloomberg.tetris.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * 分数储存封装类
 */
public class ScoreDataSource {
    // 数据库对象
    private SQLiteDatabase database;
    // 最高分对象
    private Highscore dbHelper;
    // 存储 HighScore 创建的数据库的各列名称
    private String[] ColumesOfDB = { Highscore.COLUMN_ID, Highscore.COLUMN_SCORE, Highscore.COLUMN_PLAYERNAME };

    // 构造方法 创建最高分对象
    public ScoreDataSource(Context context) {
        dbHelper = new Highscore(context);
    }

    // 以读写方式打开数据库
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    // 关闭数据库对象
    public void close() {
        dbHelper.close();
    }

    public Score createScore(long score, String playerName) {
        // 储存键值对
        ContentValues values = new ContentValues();
        // ContentValues.put(key, value);
        values.put(Highscore.COLUMN_SCORE, score);
        values.put(Highscore.COLUMN_PLAYERNAME, playerName);
        // database.insert() 返回插入的行号
        long insertId = database.insert(Highscore.TABLE_HIGHSCORES, null, values);
        // Cursor(光标)是每行的集合
        // 查询 _id == insertId 的行 根据score降序排列
        Cursor cursor = database.query(Highscore.TABLE_HIGHSCORES, ColumesOfDB, Highscore.COLUMN_ID + " = " + insertId,
                null, null, null, Highscore.COLUMN_SCORE + " DESC");
        // moveToFirst() 定位到Cursor开头
        cursor.moveToFirst();
        // 把本次分数转为Score类型
        Score newScore = cursorToScore(cursor);
        cursor.close();
        return newScore;
    }

    // 删除分数 功能未在程序中调用
    public void deleteScore(Score score) {
        long id = score.getId();
        database.delete(Highscore.TABLE_HIGHSCORES, Highscore.COLUMN_ID + " = " + id, null);
    }

    // 光标转为指针
    private Score cursorToScore(Cursor cursor) {
        Score score = new Score();
        score.setId(cursor.getLong(0));
        score.setScore(cursor.getLong(1));
        score.setName(cursor.getString(2));
        return score;
    }

    // 在HighScore中读取所有行 返回指向这些行的光标
    public Cursor getCursor() {
        return database.query(Highscore.TABLE_HIGHSCORES, ColumesOfDB, null, null, null, null,
                Highscore.COLUMN_SCORE + " DESC");
    }

}
