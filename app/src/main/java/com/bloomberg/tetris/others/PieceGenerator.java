package com.bloomberg.tetris.others;

import java.util.Random;

// 方块随机生成类
public class PieceGenerator {
    // 生成策略标记
    public static final int STRAT_RANDOM = 0;
    public static final int STRAT_7BAG = 1;

    int randomStrategy; // 随机策略

    int bag[]; // 包
    int bagPointer; // 包"指针" 取数索引
    private Random random;

    public PieceGenerator(int strat) {
        // 初始化bag数组
        bag = new int[7];
        for (int i = 0; i < 7; i++) {
            bag[i] = i;
        }
        // Random对象构造 传入时间作为种子 防止伪随机
        random = new Random(System.currentTimeMillis());
        // 根据参数确定随机方式
        if (strat == STRAT_RANDOM) {
            this.randomStrategy = STRAT_RANDOM;
        } else {
            this.randomStrategy = STRAT_7BAG;
        }
        // 初始化Bag 打乱bag中的数据
        for (int i = 0; i < 6; i++) {
            // 保存随机数 该值介于[0,7-i)的区间
            int c = random.nextInt(7 - i);
            // 交换 bag[i] 和 bag[i+c];
            int temp = bag[i];
            bag[i] = bag[i + c];
            bag[i + c] = temp;
        }
        bagPointer = 0; // 取数索引为 0
    }

    public int next() {
        if (randomStrategy == STRAT_RANDOM)
            // 普通随机 直接生成[0,7)的随机数
            return random.nextInt(7);
        else {

            if (bagPointer < 7) {
                bagPointer++;
                return bag[bagPointer - 1];
            } else {
                // 重新打乱bag[]
                for (int i = 0; i < 6; i++) {
                    int c = random.nextInt(7 - i);
                    int t = bag[i];
                    bag[i] = bag[i + c];
                    bag[i + c] = t;
                }
                bagPointer = 1;
                return bag[bagPointer - 1];
            }
        }
    }
}