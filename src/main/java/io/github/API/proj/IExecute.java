package io.github.API.proj;

interface IExecute {
    void execute();
}
interface IChannel {
    IExecute channel(String channel);
}
interface IChannels {
    IExecute channels(String... channels);
}
interface IMessage {
    IChannel message(String json);
}
