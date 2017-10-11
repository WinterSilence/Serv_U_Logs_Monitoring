package myProject.view.viewUtils;

import javafx.scene.paint.Color;

public class MyCustomColors {
    private static Color yesterdayTextRowColor;
    private static Color todayTextRowColor;

    public static Color getYesterdayTextRowColor() {
        return yesterdayTextRowColor;
    }

    public static void setYesterdayTextRowColor(Color yesterdayTextRowColor) {
        MyCustomColors.yesterdayTextRowColor = yesterdayTextRowColor;
    }

    public static Color getTodayTextRowColor() {
        return todayTextRowColor;
    }

    public static void setTodayTextRowColor(Color todayTextRowColor) {
        MyCustomColors.todayTextRowColor = todayTextRowColor;
    }
}
