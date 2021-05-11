package io.github.API;

import io.github.API.messagedata.MsgResultAPI;
import io.github.API.messagedata.MsgStatus;
import io.github.API.messagedata.MsgStatusCategory;
import io.github.API.messagedata.MsgStatusOperation;
import lombok.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

final class EventManager {
    private final HashMap<String, Set<ISubscribeCallback>> list = new HashMap<>();


    private static class InstanceHolder {
        private static final EventManager INSTANCE = new EventManager();
    }

    static EventManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    void addEventListener(MessagingAPI api, ISubscribeCallback callback, String... channels) {
        for (var channel : channels)
            list.computeIfAbsent(channel, k -> new HashSet<>()).add(callback);
        callback.status(api, new MsgStatus(MsgStatusCategory.MsgConnectedCategory, MsgStatusOperation.MsgSubscribeOperation));
    }

    void removeEventListener(MessagingAPI api, ISubscribeCallback callback, String... channels) {
        if (channels.length == 0) {
            list.forEach((key, value) -> {
                value.remove(callback);
                if (value.isEmpty()) list.remove(key);
            });
        } else {
            for (var channel : channels) {
                Set<?> reference = list.get(channel);
                if (reference != null) {
                    reference.remove(callback);

                    if (reference.isEmpty()) list.remove(channel);
                }
            }
        }
        callback.status(api, new MsgStatus(MsgStatusCategory.MsgClosedCategory, MsgStatusOperation.MsgUnsubscribeOperation));
    }

    void publish(MessagingAPI api, String publisherUuid, @NonNull String channel, @NonNull String json) {
        Set<ISubscribeCallback> callbacks = list.get(channel);
        if (callbacks != null) {
            callbacks.forEach((k) -> {
                k.resolved(api, new MsgResultAPI(channel, json, publisherUuid));
            });
        }
    }

    boolean hasListeners(@NonNull String channel) {
        return list.get(channel) != null;
    }

    int numOfListeners(@NonNull String channel) {
        return list.get(channel).size();
    }

    void cleanup() {
        list.forEach((key, value) -> {
            value.forEach((callback) -> {
                callback.status(null, new MsgStatus(MsgStatusCategory.MsgClosedCategory, MsgStatusOperation.MsgClosingOperation));
            });
        });
        list.clear();
    }
}
