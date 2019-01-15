package cn.com.billboard.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;


public class MyVideoView extends VideoView {

    private int width = 0, height = 0;

    public MyVideoView(Context context) {
        super(context);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getDefaultSize(getWidth(), widthMeasureSpec);
        int height = getDefaultSize(getHeight(), heightMeasureSpec);
        if (this.width > 0 && this.height > 0) {
            height = this.height * width / this.width;
        }
        setMeasuredDimension(width, height);
    }

    public void onMeasureSize(int width, int height){
        this.width = width;
        this.height = height;
    }
}
