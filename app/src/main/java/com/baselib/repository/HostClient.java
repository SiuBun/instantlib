package com.baselib.repository;

import com.baselib.entity.MtimeFilmeBean;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface HostClient {
    /**
     * 时光网热映电影
     */
    @GET("Showtime/LocationMovies.api?locationId=561")
    Observable<MtimeFilmeBean> getHotFilm();
}