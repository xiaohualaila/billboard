package cn.com.billboard.model;

import cn.com.library.event.IBus;

public class EventMessageModel implements IBus.IEvent{

    public String message;

    public EventMessageModel(String message) {
        this.message = message;
    }


    @Override
    public int getTag() {
        return 10;
    }
}
