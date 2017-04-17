package com.devteam.acceleration.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by robert on 12.04.17.
 */

public class AnswersData {
    public static final int BOT_BUTTON = 0;
    public static final int SYS_BUTTON = 1;

    public static final List<AnswerModel> ITEMS = new ArrayList<AnswerModel>();

    private static final int COUNT = 25;

    private static final String[] messages= new String[]{
            "do something",
            "send me picture",
            "search web",
            "go home"
    };

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createMessage(i));
        }
    }

    public static void addItem(AnswerModel item) {
        ITEMS.add(item);
//        ITEM_MAP.put(item.id, item);
    }

    private static AnswerModel createMessage(int position) {
        Random generator = new Random();
        return new AnswerModel(String.valueOf(position),
                                messages[generator.nextInt(messages.length)],
                                generator.nextInt(2));
    }

    public static class AnswerModel {
        public final String id;
        public final String content;
        public final int type;

        public AnswerModel(String id, String content, int type) {
            this.id = id;
            this.content = content;
            this.type = type;
        }

        @Override
        public String toString() {
            return id + ") " + content;
        }
    }
}
