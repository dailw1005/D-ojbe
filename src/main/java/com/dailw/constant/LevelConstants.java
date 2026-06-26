package com.dailw.constant;

import java.util.LinkedHashMap;
import java.util.Map;

public class LevelConstants {

    /** Lv0 -> 0, Lv1 -> 50, Lv2 -> 200, Lv3 -> 500 */
    public static final Map<Integer, Integer> LEVEL_THRESHOLDS = new LinkedHashMap<>();

    static {
        LEVEL_THRESHOLDS.put(0, 0);
        LEVEL_THRESHOLDS.put(1, 50);
        LEVEL_THRESHOLDS.put(2, 200);
        LEVEL_THRESHOLDS.put(3, 500);
    }

    public static final String[] LEVEL_TITLES = {
        "新手上路",   // Lv0
        "初出茅庐",   // Lv1
        "小有所成",   // Lv2
        "融会贯通",   // Lv3
    };

    /** 创建私有比赛所需等级 */
    public static final int CREATE_PRIVATE_LEVEL = 1;

    /** 创建公开比赛所需等级 */
    public static final int CREATE_PUBLIC_LEVEL = 2;

    /** 优先展示所需等级 */
    public static final int PRIORITY_DISPLAY_LEVEL = 3;

    /** 比赛排名前列奖励：冠军 */
    public static final int CONTEST_RANK_1_REPUTATION = 100;
    /** 比赛排名前列奖励：亚军 */
    public static final int CONTEST_RANK_2_REPUTATION = 60;
    /** 比赛排名前列奖励：季军 */
    public static final int CONTEST_RANK_3_REPUTATION = 40;
    /** 比赛排名前10%奖励 */
    public static final int CONTEST_TOP_10_REPUTATION = 30;
    /** 比赛排名前50%奖励 */
    public static final int CONTEST_TOP_50_REPUTATION = 10;
    /** 参与比赛奖励 */
    public static final int CONTEST_PARTICIPATION_REPUTATION = 5;
    /** 首次 AC 一题奖励 */
    public static final int FIRST_AC_REPUTATION = 10;

    /**
     * 根据声望值计算等级
     */
    public static int computeLevel(int reputation) {
        int level = 0;
        for (Map.Entry<Integer, Integer> entry : LEVEL_THRESHOLDS.entrySet()) {
            if (reputation >= entry.getValue()) {
                level = entry.getKey();
            }
        }
        return level;
    }

    /**
     * 获取下一等级所需声望
     */
    public static int nextLevelReputation(int currentLevel) {
        int next = currentLevel + 1;
        return LEVEL_THRESHOLDS.getOrDefault(next, Integer.MAX_VALUE);
    }

    /**
     * 获取等级称号
     */
    public static String levelTitle(int level) {
        if (level >= 0 && level < LEVEL_TITLES.length) {
            return LEVEL_TITLES[level];
        }
        return "未知";
    }
}
