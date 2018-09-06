package cn.com.billboard.model;

import java.util.List;

public class ScreenShowModel {

    private List<ScreenDetailModel> polices;
    private String policetime;
    private List<ScreenDetailModel> propertys;
    private String propertytime;
    private String screenip;
    private List<ScreenDetailModel> yuanyangs;
    private String yuanyangtime;


    public List<ScreenDetailModel> getPolices() {
        return polices;
    }

    public void setPolices(List<ScreenDetailModel> polices) {
        this.polices = polices;
    }

    public String getPolicetime() {
        return policetime;
    }

    public void setPolicetime(String policetime) {
        this.policetime = policetime;
    }

    public List<ScreenDetailModel> getPropertys() {
        return propertys;
    }

    public void setPropertys(List<ScreenDetailModel> propertys) {
        this.propertys = propertys;
    }

    public String getPropertytime() {
        return propertytime;
    }

    public void setPropertytime(String propertytime) {
        this.propertytime = propertytime;
    }

    public String getScreenip() {
        return screenip;
    }

    public void setScreenip(String screenip) {
        this.screenip = screenip;
    }

    public List<ScreenDetailModel> getYuanyangs() {
        return yuanyangs;
    }

    public void setYuanyangs(List<ScreenDetailModel> yuanyangs) {
        this.yuanyangs = yuanyangs;
    }

    public String getYuanyangtime() {
        return yuanyangtime;
    }

    public void setYuanyangtime(String yuanyangtime) {
        this.yuanyangtime = yuanyangtime;
    }

}
