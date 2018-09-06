package cn.com.billboard.widget;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class BannersAdapter extends PagerAdapter {
	private List<View> views;

	public BannersAdapter(List<View> views) {
		this.views = views;
	}

	@Override
	public int getCount() {
		return views == null ? 0 : views.size();
	}

	public Object instantiateItem(ViewGroup container, int position) {
		container.addView(views.get(position));
		return views.get(position);
	}

	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(views.get(position));
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}


}
