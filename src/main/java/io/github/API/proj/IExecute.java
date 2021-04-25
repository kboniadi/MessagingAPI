package io.github.API.proj;

interface IExecute {
    void execute();
}
interface IChannel {
    IMessage channel(String channel);
}
interface IChannels {
    IExecute channels(String... channels);
}
interface IMessage {
    IExecute message(String json);
}
