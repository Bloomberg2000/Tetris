package com.bloomberg.tetris.components;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.bloomberg.tetris.others.PieceGenerator;
import com.bloomberg.tetris.activities.GameActivity;

import android.R.color;
import android.preference.PreferenceManager;

import com.bloomberg.tetris.pieces.IPiece;
import com.bloomberg.tetris.pieces.JPiece;
import com.bloomberg.tetris.pieces.LPiece;
import com.bloomberg.tetris.pieces.OPiece;
import com.bloomberg.tetris.pieces.Piece;
import com.bloomberg.tetris.pieces.SPiece;
import com.bloomberg.tetris.pieces.TPiece;
import com.bloomberg.tetris.pieces.ZPiece;

// 游戏状态类
public class GameState extends Component {

    // 游戏状态标记
    public final static int state_startable = 0;
    public final static int state_running = 1;
    public final static int state_paused = 2;
    public final static int state_finished = 3;

    private static GameState instance;

    private PieceGenerator rng; // 块生成器
    public Board board; // 游戏版
    private GregorianCalendar date; //  Calendar 的一个具体子类，提供了世界上大多数国家/地区使用的标准日历系统
    private SimpleDateFormat formatter;  // 日期格式转换器
    public int hourOffset; //

    // 游戏状态
    private String playerName; // 用户名
    private int activeIndex, previewIndex;
    private Piece[] activePieces; // 活动块
    private Piece[] previewPieces; // 预览块
    private boolean scheduleSpawn; // 计划生成块
    private long spawnTime; // 生成块时间
    private int stateOfTheGame; // 游戏状态
    private long score; // 得分
    private int clearedLines; // 已消除行数
    private int level; // 级别
    private int maxLevel; // 最高级别
    private long gameTime;     // 游戏时间 += (systemtime - currenttime) 在cycle开始时计算
    private long currentTime;  // 当前时间 = systemtime 在cycle开始时计算
    private long nextDropTime; // 下次自动下落完毕时间
    private long nextPlayerDropTime; // 下次用户
    private long nextPlayerMoveTime; // 下次获取用户移动早多的时间
    private int[] dropIntervals; // 下落时间间隔 =(1/gamespeed)
    private long playerDropInterval; // 软下落移动产生的时间间隔
    private long playerMoveInterval; // 水平移动产生的时间间隔
    // 每行消除得分
    private int singleLineScore;
    private int doubleLineScore;
    private int trippleLineScore;
    private int quadLineScore;
    private int multiTetrisScore; // 多块得分
    private boolean multitetris; // 多块标记

    private int hardDropBonus; // 硬下落奖励
    private int softDropBonus; // 软下落奖励
    private int spawn_delay; // 生成块延迟
    private int piece_start_x; // 块生成点
    private long actions; // 操作次数

    private long popupTime; // 文字弹出时间
    private String popupString; // 弹出字符串
    private int popupAttack; // 弹出过程时间
    private int popupSustain; // 弹出维持时间
    private int popupDecay; // 窗口消失时间
    private int softDropDistance;//软下落距离

    // 构造方法
    private GameState(GameActivity ga) {
        super(ga);
        actions = 0; // 操作次数置为0
        board = new Board(host); // 在宿主Activity上新建版
        date = new GregorianCalendar(); // 新建日期类
        formatter = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        date.setTimeInMillis(60000);
        if (formatter.format(date.getTime()).startsWith("23")) {
            hourOffset = 1;
        } else if (formatter.format(date.getTime()).startsWith("01")) {
            hourOffset = -1;
        } else {
            hourOffset = 0;
        }
        // 下落间隔
        dropIntervals = host.getResources().getIntArray(com.bloomberg.tetris.R.array.intervals);
        // 从xml中读取相应数据
        singleLineScore = host.getResources().getInteger(com.bloomberg.tetris.R.integer.singleLineScore);
        doubleLineScore = host.getResources().getInteger(com.bloomberg.tetris.R.integer.doubleLineScore);
        trippleLineScore = host.getResources().getInteger(com.bloomberg.tetris.R.integer.trippleLineScore);
        multiTetrisScore = host.getResources().getInteger(com.bloomberg.tetris.R.integer.multiTetrisScore);
        quadLineScore = host.getResources().getInteger(com.bloomberg.tetris.R.integer.quadLineScore);
        hardDropBonus = host.getResources().getInteger(com.bloomberg.tetris.R.integer.hardDropBonus);
        softDropBonus = host.getResources().getInteger(com.bloomberg.tetris.R.integer.softDropBonus);
        softDropDistance = 0; // 软下落距离初始化为0
        spawn_delay = host.getResources().getInteger(com.bloomberg.tetris.R.integer.spawn_delay);
        piece_start_x = host.getResources().getInteger(com.bloomberg.tetris.R.integer.piece_start_x);
        popupAttack = host.getResources().getInteger(com.bloomberg.tetris.R.integer.popup_attack);
        popupSustain = host.getResources().getInteger(com.bloomberg.tetris.R.integer.popup_sustain);
        popupDecay = host.getResources().getInteger(com.bloomberg.tetris.R.integer.popup_decay);
        popupString = "";
        popupTime = -(popupAttack + popupSustain + popupDecay);
        clearedLines = 0;
        level = 0;
        score = 0;
        maxLevel = host.getResources().getInteger(com.bloomberg.tetris.R.integer.levels);

        // 下次下落间隔
        nextDropTime = host.getResources().getIntArray(com.bloomberg.tetris.R.array.intervals)[0];

        //在设置中读取 用户设置的移动速度
        playerDropInterval = (int) (1000.0f / PreferenceManager.getDefaultSharedPreferences(host).getInt("pref_softdropspeed", 60));
        playerMoveInterval = (int) (1000.0f / PreferenceManager.getDefaultSharedPreferences(host).getInt("pref_movespeed", 60));
        nextPlayerDropTime = (int) (1000.0f / PreferenceManager.getDefaultSharedPreferences(host).getInt("pref_softdropspeed", 60));
        nextPlayerMoveTime = (int) (1000.0f / PreferenceManager.getDefaultSharedPreferences(host).getInt("pref_movespeed", 60));

        //游戏时间初始化为0
        gameTime = 0;

        // 读取随机生成模式 选择合适的生成方式
        if (PreferenceManager.getDefaultSharedPreferences(host).getString("pref_rng", "sevenbag").equals("sevenbag") ||
                PreferenceManager.getDefaultSharedPreferences(host).getString("pref_rng", "7-Bag-Randomization (default)").equals("7-Bag-Randomization (default)"))
            rng = new PieceGenerator(PieceGenerator.STRAT_7BAG);
        else
            rng = new PieceGenerator(PieceGenerator.STRAT_RANDOM);

        // 初始化块 移动块和预览块
        activePieces = new Piece[7];
        previewPieces = new Piece[7];

        activePieces[0] = new IPiece(host);
        activePieces[1] = new JPiece(host);
        activePieces[2] = new LPiece(host);
        activePieces[3] = new OPiece(host);
        activePieces[4] = new SPiece(host);
        activePieces[5] = new TPiece(host);
        activePieces[6] = new ZPiece(host);

        previewPieces[0] = new IPiece(host);
        previewPieces[1] = new JPiece(host);
        previewPieces[2] = new LPiece(host);
        previewPieces[3] = new OPiece(host);
        previewPieces[4] = new SPiece(host);
        previewPieces[5] = new TPiece(host);
        previewPieces[6] = new ZPiece(host);

        // 生成随机数 获取索引块
        activeIndex = rng.next();
        previewIndex = rng.next();
        activePieces[activeIndex].setActive(true);

        //设施游戏状态为可以启动
        stateOfTheGame = state_startable;
        scheduleSpawn = false; // 暂不计划生成块
        spawnTime = 0; // 生成块时间为0
    }

    // 设置用户名
    public void setPlayerName(String string) {
        playerName = string;
    }

    // 获取游戏板
    public Board getBoard() {
        return board;
    }

    // 获得用户名
    public String getPlayerName() {
        return playerName;
    }

    // 获得自动下落时间间隔
    public int getAutoDropInterval() {
        return dropIntervals[Math.min(level, maxLevel)];
    }

    // 获得用户操作间隔
    public long getMoveInterval() {
        return playerMoveInterval;
    }

    // 获得软下落间隔
    public long getSoftDropInterval() {
        return playerDropInterval;
    }

    // 设置运行模式（running/paused）
    public void setRunning(boolean b) {
        if (b) {
            currentTime = System.currentTimeMillis();
            if (stateOfTheGame != state_finished)
                stateOfTheGame = state_running;
        } else {
            if (stateOfTheGame == state_running)
                stateOfTheGame = state_paused;
        }
    }

    // 检测行消除
    public void clearLines(boolean playerHardDrop, int hardDropDistance) {
        if (host == null)
            return;
        // 获取当前活动块并防止固定转为非活动块
        activePieces[activeIndex].place(board);
        // 获取消除的行数
        int cleared = board.clearLines(activePieces[activeIndex].getDim());
        // 统计消除的行数
        clearedLines += cleared;
        // 处理加分
        // 加分后令窗口弹出时间为当前游戏时间
        long addScore;
        switch (cleared) {
            case 1:
                addScore = singleLineScore;
                multitetris = false;
                popupTime = gameTime;
                break;
            case 2:
                addScore = doubleLineScore;
                multitetris = false;
                popupTime = gameTime;
                break;
            case 3:
                addScore = trippleLineScore;
                multitetris = false;
                popupTime = gameTime;
                break;
            case 4:
                if (multitetris)
                    addScore = multiTetrisScore;
                else
                    addScore = quadLineScore;
                multitetris = true;
                popupTime = gameTime;
                break;
            default:
                addScore = 0;
                // 如果当前设定下的时间差不足以显示动画
                if ((gameTime - popupTime) < (popupAttack + popupSustain)) {
                    // 向前调整
                    popupTime = gameTime - (popupAttack + popupSustain);
                }
                break;
        }
        // 发生了消除行为
        if (cleared > 0) {
            //处理下落奖励分数
            if (playerHardDrop) {
                addScore += hardDropDistance * hardDropBonus;
            } else {
                addScore += softDropDistance * softDropBonus;
            }
        }
        // 进行加分操作
        score += addScore;
        // 创造弹出窗口文字
        if (addScore != 0)
            popupString = "+" + addScore;
    }

    // 块过渡
    public void pieceTransition(boolean eventVibrationEnabled) {
        if (host == null)
            return;
        scheduleSpawn = true; // 开始计划生成块
        //仅在事件振动开关时进行过渡
        if (eventVibrationEnabled) {
            spawnTime = gameTime + spawn_delay; // 对生成块的时间进行延迟
        } else {
            spawnTime = gameTime;
        }
        //
        activePieces[activeIndex].reset(host); // 重设被操作过的块的状态
        activeIndex = previewIndex;
        previewIndex = rng.next(); // 开始准备预览下一块
        activePieces[activeIndex].reset(host); // 重设被操作的块的状态
    }

    // 未实现的方法
    public void hold() {
        if (host == null)
            return;
    }

    public void finishTransition() {
        if (host == null)
            return;

        scheduleSpawn = false;
        host.display.invalidatePhantom();
        activePieces[activeIndex].setActive(true);
        setNextDropTime(gameTime + dropIntervals[Math.min(level, maxLevel)]);
        setNextPlayerDropTime(gameTime);
        setNextPlayerMoveTime(gameTime);
        softDropDistance = 0;

        // 检查是否失败
        if (!activePieces[activeIndex].setPosition(piece_start_x, 0, false, board)) {
            stateOfTheGame = state_finished;
            host.gameOver(score, getTimeString(), (int) ((float) actions * (60000.0f / gameTime)));
        }
    }

    // 游戏是否结束（是否可恢复）
    public boolean isResumable() {
        return (stateOfTheGame != state_finished);
    }

    // 获得得分
    public String getScoreString() {
        return "" + score;
    }

    // 获得活动块
    public Piece getActivePiece() {
        return activePieces[activeIndex];
    }

    // 如果controls可以执行cycle()返回true
    public boolean cycle(long tempTime) {
        if (stateOfTheGame != state_running) {
            return false;
        }
        gameTime += (tempTime - currentTime);
        currentTime = tempTime;
        // 即时放置
        if (scheduleSpawn) {
            if (gameTime >= spawnTime) {
                finishTransition();
            }
            return false;
        }
        return true;
    }

    // 获得级别
    public String getLevelString() {
        return "" + level;
    }

    // 获得时间
    public String getTimeString() {
        date.setTimeInMillis(gameTime + hourOffset * (3600000));
        date.set(Calendar.HOUR, 0);
        return formatter.format(date.getTime());
    }

    // 获得APM
    public String getAPMString() {
        if (host == null)
            return "";
        return String.valueOf((int) ((float) actions * (60000.0f / gameTime)));
    }

    // 重新连接Activity
    @Override
    public void reconnect(GameActivity gameActivity) {
        super.reconnect(gameActivity);
        // 获得用户偏好设置
        playerDropInterval = (int) (1000.0f / PreferenceManager.getDefaultSharedPreferences(gameActivity).getInt("pref_softdropspeed", 60));
        playerMoveInterval = (int) (1000.0f / PreferenceManager.getDefaultSharedPreferences(gameActivity).getInt("pref_movespeed", 60));

        if (PreferenceManager.getDefaultSharedPreferences(gameActivity).getString("pref_rng", "sevenbag").equals("sevenbag") ||
                PreferenceManager.getDefaultSharedPreferences(gameActivity).getString("pref_rng", "7-Bag-Randomization (default)").equals("7-Bag-Randomization (default)"))
            rng = new PieceGenerator(PieceGenerator.STRAT_7BAG);
        else
            rng = new PieceGenerator(PieceGenerator.STRAT_RANDOM);
        // 连接board和activity
        board.reconnect(gameActivity);
        setRunning(true);
    }

    // 与Activity断开连接
    public void disconnect() {
        setRunning(false);
        board.disconnect();
        super.disconnect();
    }

    // 获得预览块
    public Piece getPreviewPiece() {
        return previewPieces[previewIndex];
    }

    // 获得时间
    public long getTime() {
        return gameTime;
    }

    // 下一级别
    public void nextLevel() {
        level++;
    }

    // 获得当前级别
    public int getLevel() {
        return level;
    }

    // 获得最高级别
    public int getMaxLevel() {
        return maxLevel;
    }

    // 获得消除行数
    public int getClearedLines() {
        return clearedLines;
    }

    // 记录操作数
    public void action() {
        actions++;
    }

    public void setNextPlayerDropTime(long time) {
        nextPlayerDropTime = time;
    }

    public void setNextPlayerMoveTime(long time) {
        nextPlayerMoveTime = time;
    }

    public void setNextDropTime(long l) {
        nextDropTime = l;
    }

    public long getNextPlayerDropTime() {
        return nextPlayerDropTime;
    }

    public long getNextDropTime() {
        return nextDropTime;
    }

    public long getNextPlayerMoveTime() {
        return nextPlayerMoveTime;
    }

    // 销毁游戏进程
    public static void destroy() {
        if (instance != null)
            instance.disconnect();
        instance = null;
    }

    // 获取当前游戏进程
    public static GameState getInstance(GameActivity ga) {
        if (instance == null)
            instance = new GameState(ga);
        return instance;
    }

    // 新建游戏进程
    public static GameState getNewInstance(GameActivity ga) {
        instance = new GameState(ga);
        return instance;
    }

    // 是否有游戏进程
    public static boolean hasInstance() {
        return (instance != null);
    }

    // 获取分数
    public long getScore() {
        return score;
    }

    // 获得每分钟操作次数
    public int getAPM() {
        return (int) ((float) actions * (60000.0f / gameTime));
    }

    // 游戏是否结束
    public static boolean isFinished() {
        if (instance == null)
            return true;
        return !instance.isResumable();
    }

    // 设置初始级别
    public void setLevel(int int1) {
        level = int1;
        nextDropTime = host.getResources().getIntArray(com.bloomberg.tetris.R.array.intervals)[int1]; //设置第一块下落时间
        clearedLines = 10 * int1; //令消除行数增加 便于升级操作
    }

    // 获取弹出文字内容
    public String getPopupString() {
        return popupString;
    }

    // 获取弹出文字透明度
    public int getPopupAlpha() {
        long x = gameTime - popupTime;

        if (x < (popupAttack + popupSustain)) {
            return 255;
        }

        if (x < (popupAttack + popupSustain + popupDecay))
            return (int) (255.0f * (1.0f + (((float) (popupAttack + popupSustain - x)) / ((float) popupDecay))));

        return 0;
    }

    // 获取弹出文字大小
    public float getPopupSize() {
        long x = gameTime - popupTime;

        if (x < popupAttack) {
            return (int) (60.0f * (1.0f + (((float) x) / ((float) popupAttack))));
        }
        return 120;
    }

    // 获得弹出文字颜色
    public int getPopupColor() {
        if (host == null)
            return 0;

        if (multitetris) {
            return host.getResources().getColor(com.bloomberg.tetris.R.color.yellow);
        }
        return host.getResources().getColor(color.white);
    }

    // 软下落距离计数器
    public void incSoftDropCounter() {
        softDropDistance++;
    }

}