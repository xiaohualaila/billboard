package cn.com.billboard.model;


import cn.com.billboard.event.IBus;

public class AlarmRecordModel implements IBus.IEvent{

    public boolean isCalling;

    public String phoneNo;//报警类型

    public AlarmRecordModel(boolean isCalling, String phoneNo) {
        this.isCalling = isCalling;
        this.phoneNo = phoneNo;
    }


    @Override
    public int getTag() {
        return 10;
    }
}
