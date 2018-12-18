package cn.com.billboard.model;

import cn.com.library.event.IBus;

public class AlarmRecordModel implements IBus.IEvent{

    public boolean isCalling;

    public int phoneType;//报警类型

    public AlarmRecordModel(boolean isCalling, int phoneType) {
        this.isCalling = isCalling;
        this.phoneType = phoneType;
    }


    @Override
    public int getTag() {
        return 10;
    }
}
