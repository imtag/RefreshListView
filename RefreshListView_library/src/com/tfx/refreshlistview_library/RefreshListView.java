package com.tfx.refreshlistview_library;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RefreshListView extends ListView {
	private float downY;
	private View mRefreshHeadView;
	private View mRefreshFootView;
	private int measuredHeadViewHeight;
	private static final int PULLDOWN_STATE = 1; //下拉状态
	private static final int RELEASE_STATE = 2;//释放
	private static final int REFRESHING_STATE = 3;//刷新中
	private int STATE = PULLDOWN_STATE; //当前状态 默认下拉
	private TextView tv_head_desc;
	private ImageView iv_head_arrow;
	private ProgressBar pd_head_refresh;
	private RotateAnimation ra_up;
	private RotateAnimation ra_down;
	private boolean isLoadingMore = false; //正在加载更多
   
	public RefreshListView(Context context) {
		this(context, null);
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 初始化头
		initHead();
		// 初始化尾
		initFoot();
		//箭头动画
		initAnimation();
		//事件
		initEvent();
	}

	private void initEvent() {
		//listview的滑动事件监听
		this.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//滑动状态改变调用
				
				//空闲状态  并且不在加载中
				if( scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					if(getLastVisiblePosition() == getAdapter().getCount() - 1 && !isLoadingMore){
						//设置状态为正在加载
						isLoadingMore = true;
						
						//显示加载更多
						mRefreshFootView.setPadding(0, 0, 0, 0);
						//设置listview完全显示  加载更多
						setSelection(getAdapter().getCount());
						
						//如果实现了接口  回调loadMore加载更多
						if(mListener != null){
							mListener.loadMore();
						}
					}
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				//滑动调用 灵敏
			}
		});
	}

	private void initAnimation() {
		//箭头从向下变为向上动画
		ra_up = new RotateAnimation(0,180,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		ra_up.setDuration(500);
		ra_up.setFillAfter(true); //停留在结束
		
		//箭头从向上变为向下动画
		ra_down = new RotateAnimation(180,0,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		ra_down.setDuration(500);
		ra_down.setFillAfter(true); //停留在结束
	}

	//下拉刷新接口回调
	private OnRefreshDataLinstener mListener;
	private int measuredFootViewHeight;
	private float dy;
	public interface OnRefreshDataLinstener{
		//下啦刷新
		void refreshData();
		//加载更多
		void loadMore();
	};
	public void setOnRefreshDataLinstener(OnRefreshDataLinstener listener){
		mListener = listener;
	}
	
	//加载更多完成回调
	public void updateLoadMoreState(){
		mRefreshFootView.setPadding(0, -measuredFootViewHeight, 0, 0);
		isLoadingMore = false;
	}
	
	//下拉刷新完成回调   更改状态
	public void updateRefreshState(boolean isSuccess){
		//隐藏进度圆
		pd_head_refresh.setVisibility(View.GONE);
		if(isSuccess){
			//刷新成功信息
			tv_head_desc.setText("刷新成功");
		}
		
		//延时消息 
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() { 
				//更改状态
				STATE = PULLDOWN_STATE;
				//隐藏头
				mRefreshHeadView.setPadding(0, -measuredHeadViewHeight, 0, 0); 
				//显示箭头
				iv_head_arrow.setVisibility(View.VISIBLE);
				//更改文字描述
				tv_head_desc.setText("下拉刷新");
			}
		}, 600);
	}
	
	//覆盖touch事件
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN://按下
			downY = ev.getY();
			break;
			
		case MotionEvent.ACTION_MOVE://移动
			//如果当前正在刷新 不执行下面代码
			if(STATE == REFRESHING_STATE){
				break;
			}
			
			//没有响应按下坐标
			if(downY == 0 || downY == -1){
				downY = ev.getY();
			}
			    
			float moveY = ev.getY();
			
			//移动长度
			dy = moveY - downY;
			
			//显示的是第一条数据并且 从上往下滑
			if (getFirstVisiblePosition() == 0 && dy > 0) {
				//计算当前隐藏高度
				float hiddenHight = -measuredHeadViewHeight + dy;
				
				if(hiddenHight >= 0 && STATE != RELEASE_STATE){ 
					//刷新头已经全部显示 状态改为释放刷新
					STATE = RELEASE_STATE;
					processState();
				}else if(hiddenHight < 0 && STATE != PULLDOWN_STATE){
					//刷新头未全部显示 
					STATE = PULLDOWN_STATE;
					processState();
				}
				
				//改变隐藏的高度
				mRefreshHeadView.setPadding(0, (int)hiddenHight, 0, 0);
				
				return true;//自己处理事件
			}
			
			break;
			
		case MotionEvent.ACTION_UP:
			//松开
			if(STATE == RELEASE_STATE){
				//释放刷新
				STATE = REFRESHING_STATE;
				processState();
			}else if(STATE == PULLDOWN_STATE){
				//继续隐藏头
				mRefreshHeadView.setPadding(0, -measuredHeadViewHeight, 0, 0);
			}
			
			break;

		default:
			break;
		}
		return super.onTouchEvent(ev); //默认响应父类onTouchEvent事件
	}
	
	//处理状态
	private void processState() {
		switch (STATE) {
		case PULLDOWN_STATE://下拉
			tv_head_desc.setText("下拉刷新");
			iv_head_arrow.startAnimation(ra_down);
			break;
		case RELEASE_STATE://释放
			tv_head_desc.setText("释放立即刷新");
			iv_head_arrow.startAnimation(ra_up);
			break;
		case REFRESHING_STATE://刷新
			tv_head_desc.setText("正在刷新...");
			iv_head_arrow.clearAnimation();
			iv_head_arrow.setVisibility(View.GONE);
			pd_head_refresh.setVisibility(View.VISIBLE);
			mRefreshHeadView.setPadding(0, 0, 0, 0);
			
			//刷新数据业务
			if(mListener != null){
				//如果实现了接口回调 调用方法
				mListener.refreshData();
			}
			break;
		default:
			break;
		}
	}

	private void initHead() {
		mRefreshHeadView = View.inflate(getContext(), R.layout.listview_refresh_head, null);
		tv_head_desc = (TextView) mRefreshHeadView.findViewById(R.id.tv_refresh_head_desc);
		iv_head_arrow = (ImageView) mRefreshHeadView.findViewById(R.id.iv_refresh_head_arrow);
		pd_head_refresh = (ProgressBar) mRefreshHeadView.findViewById(R.id.pb_refresh_head_cricle);
		
		//隐藏头
		mRefreshHeadView.measure(0, 0);
		measuredHeadViewHeight = mRefreshHeadView.getMeasuredHeight();
		mRefreshHeadView.setPadding(0, -measuredHeadViewHeight, 0, 0); 
		
		//添加头 
		addHeaderView(mRefreshHeadView);
	}
 
	private void initFoot() {
		mRefreshFootView = View.inflate(getContext(), R.layout.listview_refresh_foot, null);
		
		//隐藏尾
		mRefreshFootView.measure(0, 0);
		measuredFootViewHeight = mRefreshFootView.getMeasuredHeight();
		mRefreshFootView.setPadding(0, -measuredFootViewHeight, 0, 0);
		
		//添加尾
		addFooterView(mRefreshFootView);
	}
}
