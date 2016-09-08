package com.zcy.ghost.ghost.app.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.decoration.SpaceDecoration;
import com.zcy.ghost.ghost.R;
import com.zcy.ghost.ghost.adapter.FoundAdapter;
import com.zcy.ghost.ghost.app.BaseFragment;
import com.zcy.ghost.ghost.bean.VideoInfo;
import com.zcy.ghost.ghost.bean.VideoResult;
import com.zcy.ghost.ghost.net.Network;
import com.zcy.ghost.ghost.utils.LogUtils;
import com.zcy.ghost.ghost.utils.ScreenUtil;

import java.util.ArrayList;
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
public class Fragment2 extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    final String TAG = Fragment2.class.getSimpleName();

    @BindView(R.id.title_name)
    TextView titleName;
    @BindView(R.id.recyclerView)
    EasyRecyclerView recyclerView;

    Unbinder unbinder;

    FoundAdapter adapter;
    int page = 0;
    Handler handler = new Handler();
    VideoResult homeResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_two, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        initView(inflater);
        return rootView;
    }

    @Override
    protected void initView(LayoutInflater inflater) {
        titleName.setText(mResources.getText(R.string.discover));
        recyclerView.setAdapterWithProgress(adapter = new FoundAdapter(getContext()));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        SpaceDecoration itemDecoration = new SpaceDecoration(ScreenUtil.dip2px(getContext(), 8));
        itemDecoration.setPaddingEdgeSide(true);
        itemDecoration.setPaddingStart(true);
        itemDecoration.setPaddingHeaderFooter(false);
        recyclerView.addItemDecoration(itemDecoration);
    }

    @Override
    protected void initEvent() {
        recyclerView.setRefreshListener(this);
        onRefresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onRefresh() {
        page = 0;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //刷新
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
            List<VideoInfo> videoInfos = new ArrayList<>();
            for (int i = 1; i < homeResult.ret.list.size(); i++) {
                if (!TextUtils.isEmpty(homeResult.ret.list.get(i).title) && !homeResult.ret.list.get(i).title.equals("精彩推荐")) {
                    VideoInfo videoInfo = homeResult.ret.list.get(i).childList.get(0);
                    videoInfo.title = homeResult.ret.list.get(i).title;
                    videoInfos.add(videoInfo);
                }
                LogUtils.e(TAG, i + "");
            }
            adapter.addAll(videoInfos);

        }
    }
}
