package cn.com.billboard.model;

import java.util.List;

public class MessageBodyBean {
    /**
     * tel1 : 2
     * fulldisplay : [{"title":"","order":"1","url":"http://www.xazhsq.cn:8080/webPicture/admin/2018-11-07/1811071310318379.jpg","isshow":"true","link":"","shapetype":"6"},{"title":"","order":"2","url":"http://www.xazhsq.cn:8080/webPicture/admin/2018-11-07/1811071310437581.jpg","isshow":"true","link":"","shapetype":"6"}]
     * tel2 : 3
     * apkurl : http://www.e-2-e.cn/AppUpload/media.apk
     * build : 2
     * stripedisplay : [{"title":"523452345234","order":"1","url":"http://www.xazhsq.cn:8080/webPicture","isshow":"true","link":"","shapetype":"5"}]
     * tel3 : 4
     * tel4 : 5
     */

    private String tel1;
    private String tel2;
    private String apkurl;
    private String build;
    private String tel3;
    private String tel4;
    private List<FulldisplayBean> fulldisplay;
    private List<StripedisplayBean> stripedisplay;

    public String getTel1() {
        return tel1;
    }

    public void setTel1(String tel1) {
        this.tel1 = tel1;
    }

    public String getTel2() {
        return tel2;
    }

    public void setTel2(String tel2) {
        this.tel2 = tel2;
    }

    public String getApkurl() {
        return apkurl;
    }

    public void setApkurl(String apkurl) {
        this.apkurl = apkurl;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getTel3() {
        return tel3;
    }

    public void setTel3(String tel3) {
        this.tel3 = tel3;
    }

    public String getTel4() {
        return tel4;
    }

    public void setTel4(String tel4) {
        this.tel4 = tel4;
    }

    public List<FulldisplayBean> getFulldisplay() {
        return fulldisplay;
    }

    public void setFulldisplay(List<FulldisplayBean> fulldisplay) {
        this.fulldisplay = fulldisplay;
    }

    public List<StripedisplayBean> getStripedisplay() {
        return stripedisplay;
    }

    public void setStripedisplay(List<StripedisplayBean> stripedisplay) {
        this.stripedisplay = stripedisplay;
    }

    public static class FulldisplayBean {
        /**
         * title :
         * order : 1
         * url : http://www.xazhsq.cn:8080/webPicture/admin/2018-11-07/1811071310318379.jpg
         * isshow : true
         * link :
         * shapetype : 6
         */

        private String title;
        private String order;
        private String url;
        private String isshow;
        private String link;
        private String shapetype;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getOrder() {
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getIsshow() {
            return isshow;
        }

        public void setIsshow(String isshow) {
            this.isshow = isshow;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getShapetype() {
            return shapetype;
        }

        public void setShapetype(String shapetype) {
            this.shapetype = shapetype;
        }
    }

    public static class StripedisplayBean {
        /**
         * title : 523452345234
         * order : 1
         * url : http://www.xazhsq.cn:8080/webPicture
         * isshow : true
         * link :
         * shapetype : 5
         */

        private String title;
        private String order;
        private String url;
        private String isshow;
        private String link;
        private String shapetype;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getOrder() {
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getIsshow() {
            return isshow;
        }

        public void setIsshow(String isshow) {
            this.isshow = isshow;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getShapetype() {
            return shapetype;
        }

        public void setShapetype(String shapetype) {
            this.shapetype = shapetype;
        }
    }
}
