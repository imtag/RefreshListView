package com.tfx.refreshlistview_demo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tfx.refreshlistview_library.RefreshListView;
import com.tfx.refreshlistview_library.RefreshListView.OnRefreshDataLinstener;

public class MainActivity extends Activity {

	private RefreshListView lv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
		initEvent();
	}

	private void initEvent() {
		// RefreshListView接口回调
		lv.setOnRefreshDataLinstener(new OnRefreshDataLinstener() {
			@Override
			public void refreshData() {
				//处理下拉刷新完成回调结果   
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						//刷新完成更新状态  updateRefreshState true刷新成功  false刷新失败
						lv.updateRefreshState(true);
					}
				}, 2000);
			}

			@Override
			public void loadMore() {
				//处理加载更多完成回调结果  
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						//加载更多完成更新状态  updateLoadMoreState
						lv.updateLoadMoreState();
					}
				}, 2000);
			}
		});
	}

	private void initData() {
		//绑定适配器
		MyAdapter adapter = new MyAdapter();
		lv.setAdapter(adapter);
	}

	//listview适配器
	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 20;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = new TextView(MainActivity.this);
			tv.setTextSize(24);
			tv.setGravity(Gravity.CENTER);
			tv.setPadding(10, 10, 10, 10);
			tv.setText("Andy " + position);
			return tv;
		}
	}

	private void initView() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		lv = (RefreshListView) findViewById(R.id.lv);
	}
}
