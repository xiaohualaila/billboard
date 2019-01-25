package cn.com.billboard.model;


import cn.com.billboard.event.IBus;

public class AlarmRecordModel implements IBus.IEvent{

    public boolean isCalling;

    public String phoneNo;

    public int phoneType;//报警类型
    public AlarmRecordModel(boolean isCalling, String phoneNo,int phoneType) {
        this.isCalling = isCalling;
        this.phoneNo = phoneNo;
        this.phoneType = phoneType;
    }


    @Override
    public int getTag() {
        return 10;
    }
}
