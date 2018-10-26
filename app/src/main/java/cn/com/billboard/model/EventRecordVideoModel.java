package cn.com.billboard.model;

import cn.com.library.event.IBus;

public class EventRecordVideoModel implements IBus.IEvent{

    public boolean isCalling;

    public int phoneType;

    public EventRecordVideoModel(boolean isCalling, int phoneType) {
        this.isCalling = isCalling;
        this.phoneType = phoneType;
    }


    @Override
    public int getTag() {
        return 10;
    }
}
