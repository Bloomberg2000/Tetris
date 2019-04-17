package com.bloomberg.tetris.components;

import com.bloomberg.tetris.activities.GameActivity;

// 组件抽象类
public abstract class Component {
    // 组件所属的GameActivity
    protected GameActivity host;

    public Component(GameActivity gameActivity) {
        host = gameActivity;
    }

    public void reconnect(GameActivity gameActivity) {
        host = gameActivity;
    }

    public void disconnect() {
        host = null;
    }

}
