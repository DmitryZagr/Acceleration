package com.devteam.acceleration.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by robert on 12.04.17.
 */

public class MessageData {
    public static final int INCOMING_MESSAGE = 0;
    public static final int OUTGOING_MESSAGE = 1;
    public static final List<MessageModel> items = new ArrayList<MessageModel>();
    public static AtomicInteger count = new AtomicInteger(0);

    private static final String[] messages= new String[]{
            "Hello!", "How are you?", "Now we are going to talk about...Lorem Ipsum. Lorem Ipsum" +
            " - это текст-\"рыба\", \" \n" +
            "\"часто используемый в печати и вэб-дизайне. Lorem Ipsum является \" \n" +
            "\"стандартной \"рыбой\" для текстов на латинице с начала XVI века.",
            "Flushing caches. Disabling v-sync. SetSwapInterval() interval: 0 not set."
    };

    static {
        // Add some sample items.
        for (int i = 1; i <= 3; i++) {
            addItem(createMessage(i));
        }
    }

    public static void addItem(MessageModel item) {
        items.add(item);
        count.incrementAndGet();
//        ITEM_MAP.put(item.id, item);
    }

    private static MessageModel createMessage(int position) {
        Random generator = new Random();
        return new MessageModel(String.valueOf(position),
                                messages[generator.nextInt(messages.length)],
                                generator.nextInt(2));
    }

    public static class MessageModel {
        public final String id;
        public final String content;
        public final int type;

        public MessageModel(String id, String content, int type) {
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
