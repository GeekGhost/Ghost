package com.zcy.ghost.vivideo.model.http.api;


import com.zcy.ghost.vivideo.model.bean.GankHttpResponse;
import com.zcy.ghost.vivideo.model.bean.GankItemBean;

import java.util.List;

import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by codeest on 16/8/19.
 */

public interface GankApis {

    String HOST = "https://gank.io/api/";

    /**
     * 福利列表
     */
    @GET("data/{path}/{num}/{page}")
    Observable<GankHttpResponse<List<GankItemBean>>> getGirlList(@Path(value = "path", encoded = false) String path, @Path("num") int num, @Path("page") int page);

}
