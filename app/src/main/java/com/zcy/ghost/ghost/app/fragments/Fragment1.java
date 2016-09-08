package com.zcy.ghost.ghost.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.jude.easyrecyclerview.decoration.SpaceDecoration;
import com.jude.rollviewpager.RollPagerView;
import com.jude.rollviewpager.hintview.IconHintView;
import com.zcy.ghost.ghost.R;
import com.zcy.ghost.ghost.adapter.BannerAdapter;
import com.zcy.ghost.ghost.adapter.VideoAdapter;
import com.zcy.ghost.ghost.app.BaseFragment;
import com.zcy.ghost.ghost.app.activitys.VideoInfoActivity;
import com.zcy.ghost.ghost.bean.VideoInfo;
import com.zcy.ghost.ghost.bean.VideoResult;
import com.zcy.ghost.ghost.net.Network;
import com.zcy.ghost.ghost.utils.LogUtils;
import com.zcy.ghost.ghost.utils.ScreenUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Description:
 * Creator: yxc
 * date: $date $time
 */
public class Fragment1 extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    final String TAG = Fragment1.class.getSimpleName();

    @BindView(R.id.recyclerView)
    EasyRecyclerView recyclerView;
    @BindView(R.id.title)
    RelativeLayout title;
    @BindView(R.id.title_name)
    TextView titleName;
    RollPagerView banner;
    View headerView;

    Unbinder unbinder;
    VideoAdapter adapter;
    int page = 0;
    Handler handler = new Handler();
    VideoResult homeResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_one, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        initView(inflater);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    @Override
    protected void initView(LayoutInflater inflater) {
        title.setVisibility(View.GONE);
        titleName.setText(mResources.getText(R.string.omnibus));
        headerView = inflater.inflate(R.layout.choice_header, null);
        banner = ButterKnife.findById(headerView, R.id.banner);
        banner.setPlayDelay(2000);
        recyclerView.setAdapterWithProgress(adapter = new VideoAdapter(getContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SpaceDecoration itemDecoration = new SpaceDecoration(ScreenUtil.dip2px(getContext(), 8));
        itemDecoration.setPaddingEdgeSide(true);
        itemDecoration.setPaddingStart(true);
        itemDecoration.setPaddingHeaderFooter(false);
        recyclerView.addItemDecoration(itemDecoration);
    }

    @Override
    protected void initEvent() {
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFastDoubleClick()) {
                    recyclerView.scrollToPosition(0);
                }
            }
        });
        recyclerView.setRefreshListener(this);
        onRefresh();
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (getHeaderScroll() <= ScreenUtil.dip2px(mContext, 150)) {
                    new Handler().postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            float percentage = (float) getHeaderScroll() / ScreenUtil.dip2px(mContext, 150);
                            title.setAlpha(percentage);
                            if (percentage > 0)
                                title.setVisibility(View.VISIBLE);
                            else
                                title.setVisibility(View.GONE);

                        }
                    }, 300);
                } else {
                    title.setAlpha(1.0f);
                    title.setVisibility(View.VISIBLE);
                }
            }
        });
        adapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(getContext(), VideoInfoActivity.class);
                intent.putExtra("dataId", adapter.getItem(position).dataId);
                startActivity(intent);
            }
        });
    }

    public int getHeaderScroll() {
        if (headerView == null) {
            return 0;
        }
        int distance = headerView.getTop();
        distance = Math.abs(distance);
        return distance;
    }

    private void initData() {
    }

    @Override
    public void onRefresh() {
        page = 0;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //刷新
                adapter.clear();
                getPageHomeInfo();
            }
        }, 1000);
    }

    Observer<VideoResult> observer = new Observer<VideoResult>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(getActivity(), R.string.loading_failed, Toast.LENGTH_SHORT).show();
            adapter.stopMore();
        }

        @Override
        public void onNext(VideoResult videoResult) {
            homeResult = videoResult;
            setAdapter();
        }
    };

    private void getPageHomeInfo() {
        subscription = Network.getVideoApi(getContext())
                .getHomePage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private void setAdapter() {
        if (homeResult != null) {
            adapter.clear();
            if (adapter.getHeaderCount() == 0) {
                adapter.addHeader(new RecyclerArrayAdapter.ItemView() {
                    @Override
                    public View onCreateView(ViewGroup parent) {
                        banner.setHintView(new IconHintView(getContext(), R.mipmap.ic_page_indicator_focused, R.mipmap.ic_page_indicator, ScreenUtil.dip2px(getContext(), 10)));
                        banner.setHintPadding(0, 0, 0, ScreenUtil.dip2px(getContext(), 8));
                        banner.setAdapter(new BannerAdapter(getContext(), homeResult.ret.list.get(0).childList));
                        return headerView;
                    }

                    @Override
                    public void onBindView(View headerView) {

                    }
                });
            }
            List<VideoInfo> videoInfos;
            for (int i = 1; i < homeResult.ret.list.size(); i++) {
                if (homeResult.ret.list.get(i).title.equals("精彩推荐")) {
                    videoInfos = homeResult.ret.list.get(i).childList;
                    videoInfos.get(0).setFirst(true);
                    videoInfos.get(0).setType(homeResult.ret.list.get(i).title);
                    adapter.addAll(videoInfos);
                    break;
                }
                LogUtils.e(TAG, i + "");
            }

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unsubscribe();
        unbinder.unbind();
    }
}
