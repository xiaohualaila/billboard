package cn.com.billboard.widget;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @ClassName: BaseViewPager
 * @Description:(能够自动循环的现实图片)
 * @author chengbo
 * @date 2015-4-2 下午4:43:31
 */
public class BaseViewPager extends ViewPager {
	private int interval = 10000;// 自动滚动的时间
	public boolean isOutScroll;// 是否需要自动滚动
	private MyHandler myHandler;
	private int ROLL = 0x10;

	public BaseViewPager(Context context) {
		super(context);
		init();
	}

	public BaseViewPager(Context context, AttributeSet set) {
		super(context, set);
		init();
	}

	private void init() {
		myHandler = new MyHandler(this);
	}

	public void setIsOutScroll(boolean isOutScroll) {
		this.isOutScroll = isOutScroll;
	}

	public void startScroll() {
		myHandler.removeMessages(ROLL);
		myHandler.sendEmptyMessageDelayed(ROLL, interval);
	}

	public void stopScroll() {
		myHandler.removeMessages(ROLL);
	}

	public static class MyHandler extends Handler {
		private WeakReference<BaseViewPager> mBaseWeakReference;

		public MyHandler(BaseViewPager baseViewPager) {
			mBaseWeakReference = new WeakReference<BaseViewPager>(baseViewPager);
		}

		@Override
		public void handleMessage(Message msg) {
			BaseViewPager tempBaseViewPager = mBaseWeakReference.get();
			if (tempBaseViewPager == null)
				return;
			if (msg.what == tempBaseViewPager.ROLL) {// 发送滚动的消息
				tempBaseViewPager.scrollOnce();
				tempBaseViewPager.startScroll();
			}

		}
	}

	/**
	 * scroll only once
	 */
	public void scrollOnce() {
		PagerAdapter adapter = getAdapter();
		int currentItem = getCurrentItem();
		if (adapter == null || (adapter.getCount()) <= 1) {
			return;
		}
		int nextItem = (currentItem + 1) % (adapter.getCount());
		setCurrentItem(nextItem, true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if (isOutScroll) {
				stopScroll();
			}
			break;
		case MotionEvent.ACTION_UP:
			if (isOutScroll) {
				startScroll();
			}
			break;
		}
		return super.onTouchEvent(event);
	}
}
