package com.baselib.repository;

import com.baselib.entity.FilmDetailBean;
import com.baselib.entity.MtimeFilmeBean;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ModuleClient {
    /**
     * 获取电影详情
     * FilmDetailBasicBean 561为武汉地区
     *
     * @param movieId 电影bean里的id
     */
    @GET("movie/detail.api?locationId=561")
    Observable<FilmDetailBean> getFilmDetail(@Query("movieId") int movieId);
}