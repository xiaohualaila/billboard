package cn.com.billboard.model;


import cn.com.billboard.event.IBus;

public class TipModel implements IBus.IEvent{

    public boolean isHandup;

    public TipModel(boolean isHandup) {
        this.isHandup = isHandup;
    }


    @Override
    public int getTag() {
        return 10;
    }
}
