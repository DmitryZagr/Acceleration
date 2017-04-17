package com.devteam.acceleration.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by robert on 12.04.17.
 */

public class AnswersData {
    public static final int BOT_BUTTON = 0;
    public static final int SYS_BUTTON = 1;

    public static final List<AnswerModel> items = new ArrayList<AnswerModel>();

    public static void countReset() {
        AnswersData.count.set(0);
    }

    public static int getCount() {
        return AnswersData.count.get();
    }

    private static AtomicInteger count = new AtomicInteger(0);

    private static final String[] messages= new String[]{
            "Do something",
            "Send me picture",
            "Search web",
            "Go home"
    };

    static {
        // Add some sample items.
        for (int i = 1; i <= 12; i++) {
            addItem(createMessage(i));
        }
    }

    public static void addItem(AnswerModel item) {
        items.add(item);
        count.incrementAndGet();
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
            return content;
        }
    }
}
