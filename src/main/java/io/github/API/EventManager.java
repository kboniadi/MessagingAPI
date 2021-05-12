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
    private final HashMap<String, Set<ISubscribeCallback>> list;
    private final Set<ISubscribeCallback> globalList;


    private static class InstanceHolder {
        private static final EventManager INSTANCE = new EventManager();
    }

    private EventManager() {
        list = new HashMap<>();
        globalList = new HashSet<>();
    }

    static EventManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    void addEventListener(MessagingAPI api, ISubscribeCallback callback, String... channels) {
        if (channels.length == 0) {
            globalList.add(callback);
        } else {
            for (var channel : channels)
                list.computeIfAbsent(channel, k -> new HashSet<>()).add(callback);
            callback.status(api, new MsgStatus(MsgStatusCategory.MsgConnectedCategory, MsgStatusOperation.MsgSubscribeOperation));
        }
        callback.status(api, new MsgStatus(MsgStatusCategory.MsgConnectedCategory, MsgStatusOperation.MsgSubscribeOperation));
    }

    void removeEventListener(MessagingAPI api, ISubscribeCallback callback, String... channels) throws IllegalArgumentException {
        boolean present = globalList.contains(callback);
        if (channels.length == 0 && present) {
            globalList.remove(callback);
        } else if (channels.length == 0) {
            list.forEach((key, value) -> {
                value.remove(callback);
                if (value.isEmpty()) list.remove(key);
            });
        } else if (!present) {
            for (var channel : channels) {
                Set<?> reference = list.get(channel);
                if (reference != null) {
                    reference.remove(callback);

                    if (reference.isEmpty()) list.remove(channel);
                }
            }
        } else {
            throw new IllegalArgumentException("you can not remove individual channels for this callback");
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
        globalList.forEach((k) -> {
            k.resolved(api, new MsgResultAPI(channel, json, publisherUuid));
        });
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
        globalList.forEach((callback) -> {
            callback.status(null, new MsgStatus(MsgStatusCategory.MsgClosedCategory, MsgStatusOperation.MsgClosingOperation));
        });
        list.clear();
        globalList.clear();
    }
}
