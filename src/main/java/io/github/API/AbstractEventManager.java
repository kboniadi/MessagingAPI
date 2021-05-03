package io.github.API;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

abstract class AbstractEventManager {
    private final HashMap<String, Set<ISubscribeCallback>> list = new HashMap<>();


    protected AbstractEventManager() {
        // empty
    }

    public void addEventListener(ISubscribeCallback callback, String... channels) {
        Objects.requireNonNull(callback);
        Objects.requireNonNull(channels);

        for (var channel : channels)
            list.computeIfAbsent(channel, k -> new HashSet<>()).add(callback);
    }

    public void removeEventListener(ISubscribeCallback callback, String... channels) {
        Objects.requireNonNull(callback);
        Objects.requireNonNull(channels);
        for (var channel : channels) {
            Set<?> reference = list.get(channel);
            if (reference != null) {
                reference.remove(callback);

                if (reference.isEmpty()) list.remove(channel);
            }
        }
    }

    protected void publish(MessagingAPI api, String channel, String json) {
        Objects.requireNonNull(channel);
        Objects.requireNonNull(json);
        Set<ISubscribeCallback> callbacks = list.get(channel);
        if (callbacks != null) {
            callbacks.forEach(k -> {
                k.resolved(api, new MessageResultAPI(channel, json));
            });
        }
    }

    protected boolean hasListeners(String channel) {
        Objects.requireNonNull(channel);
        return list.get(channel) != null;
    }

    protected void cleanup() {
        list.clear();
    }
}
