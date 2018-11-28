package com.zcy.ghost.vivideo.presenter;

import android.util.DisplayMetrics;

import com.zcy.ghost.vivideo.app.App;
import com.zcy.ghost.vivideo.base.RxPresenter;
import com.zcy.ghost.vivideo.model.bean.GankHttpResponse;
import com.zcy.ghost.vivideo.model.bean.GankItemBean;
import com.zcy.ghost.vivideo.model.net.RetrofitHelper;
import com.zcy.ghost.vivideo.presenter.contract.WelfareContract;
import com.zcy.ghost.vivideo.utils.RxUtil;
import com.zcy.ghost.vivideo.utils.StringUtils;

import java.net.URLEncoder;
import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

/**
 * Description:
 * Creator: yxc
 * date: $date $time
 */

public class WelfarePresenter extends RxPresenter<WelfareContract.View> implements WelfareContract.Presenter {
    public static final String PATH = "福利";
    public static final int NUM_OF_PAGE = 20;

    private int currentPage = 1;

    @Inject
    public WelfarePresenter() {
    }

    @Override
    public void onRefresh() {
        currentPage = 1;
        Subscription rxSubscription = RetrofitHelper.getGankApis().getGirlList(PATH,NUM_OF_PAGE, currentPage)
                .compose(RxUtil.<GankHttpResponse<List<GankItemBean>>>rxSchedulerHelper())
                .compose(RxUtil.<List<GankItemBean>>handleGankResult())
                .subscribe(new Action1<List<GankItemBean>>() {
                    @Override
                    public void call(List<GankItemBean> gankItemBeen) {
                        setHeight(gankItemBeen);
                        mView.showContent(gankItemBeen);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mView.showError("数据加载失败ヽ(≧Д≦)ノ");
                    }
                });
        addSubscribe(rxSubscription);
    }

    @Override
    public void loadMore() {
        Subscription rxSubscription = RetrofitHelper.getGankApis().getGirlList(PATH,NUM_OF_PAGE, ++currentPage)
                .compose(RxUtil.<GankHttpResponse<List<GankItemBean>>>rxSchedulerHelper())
                .compose(RxUtil.<List<GankItemBean>>handleGankResult())
                .subscribe(new Action1<List<GankItemBean>>() {
                    @Override
                    public void call(List<GankItemBean> gankItemBeen) {
                        setHeight(gankItemBeen);
                        mView.showMoreContent(gankItemBeen);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mView.showError("加载更多数据失败ヽ(≧Д≦)ノ");
                    }
                });
        addSubscribe(rxSubscription);
    }

    private void setHeight(List<GankItemBean> list) {
        DisplayMetrics dm = App.getInstance().getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels / 2;//宽度为屏幕宽度一半
        for (GankItemBean gankItemBean : list) {
            gankItemBean.setHeight(width * StringUtils.getRandomNumber(3, 6) / 3);//随机的高度
        }
    }

}
