package cn.com.billboard.model;

import cn.com.library.event.IBus;

public class EventModel implements IBus.IEvent{

    public String flag;

    public String value;

    public EventModel(String flag, String value) {
        this.flag = flag;
        this.value = value;
    }


    @Override
    public int getTag() {
        return 10;
    }
}
