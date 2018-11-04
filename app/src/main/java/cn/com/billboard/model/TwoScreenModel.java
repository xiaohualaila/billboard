package cn.com.billboard.model;

import java.util.List;

public class TwoScreenModel {

    /**
     * apkurl : http://www.e-2-e.cn/AppUpload/media.apk
     * halfdowndisplay : [{"title":"","order":"1","url":"http://192.168.1.25:8080/webPicture//admin/1811031108181980.jpg","isshow":"true","link":"","shapetype":"4"}]
     * downdisplay : [{"title":"","order":"1","url":"http://192.168.1.25:8080/webPicture//admin/1811031107292252.jpg","isshow":"true","link":"","shapetype":"2"},{"title":"","order":"2","url":"http://192.168.1.25:8080/webPicture//admin/1811031107429549.jpg","isshow":"true","link":"","shapetype":"2"}]
     * build : 3
     * updisplay : [{"title":"","order":"1","url":"http://192.168.1.25:8080/webPicture//admin/1811031107019793.jpg","isshow":"true","link":"","shapetype":"1"},{"title":"","order":"2","url":"http://192.168.1.25:8080/webPicture//admin/1811031107118871.jpg","isshow":"true","link":"","shapetype":"1"}]
     * halfupdisplay : [{"title":"","order":"1","url":"http://192.168.1.25:8080/webPicture//admin/1811031108019360.mp4","isshow":"true","link":"","shapetype":"3"}]
     */

    private String apkurl;
    private String build;
    private List<HalfdowndisplayBean> halfdowndisplay;
    private List<DowndisplayBean> downdisplay;
    private List<UpdisplayBean> updisplay;
    private List<HalfupdisplayBean> halfupdisplay;

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

    public List<HalfdowndisplayBean> getHalfdowndisplay() {
        return halfdowndisplay;
    }

    public void setHalfdowndisplay(List<HalfdowndisplayBean> halfdowndisplay) {
        this.halfdowndisplay = halfdowndisplay;
    }

    public List<DowndisplayBean> getDowndisplay() {
        return downdisplay;
    }

    public void setDowndisplay(List<DowndisplayBean> downdisplay) {
        this.downdisplay = downdisplay;
    }

    public List<UpdisplayBean> getUpdisplay() {
        return updisplay;
    }

    public void setUpdisplay(List<UpdisplayBean> updisplay) {
        this.updisplay = updisplay;
    }

    public List<HalfupdisplayBean> getHalfupdisplay() {
        return halfupdisplay;
    }

    public void setHalfupdisplay(List<HalfupdisplayBean> halfupdisplay) {
        this.halfupdisplay = halfupdisplay;
    }

    public static class HalfdowndisplayBean {
        /**
         * title :
         * order : 1
         * url : http://192.168.1.25:8080/webPicture//admin/1811031108181980.jpg
         * isshow : true
         * link :
         * shapetype : 4
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

    public static class DowndisplayBean {
        /**
         * title :
         * order : 1
         * url : http://192.168.1.25:8080/webPicture//admin/1811031107292252.jpg
         * isshow : true
         * link :
         * shapetype : 2
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

    public static class UpdisplayBean {
        /**
         * title :
         * order : 1
         * url : http://192.168.1.25:8080/webPicture//admin/1811031107019793.jpg
         * isshow : true
         * link :
         * shapetype : 1
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

    public static class HalfupdisplayBean {
        /**
         * title :
         * order : 1
         * url : http://192.168.1.25:8080/webPicture//admin/1811031108019360.mp4
         * isshow : true
         * link :
         * shapetype : 3
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
