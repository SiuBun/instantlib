package com.baselib.entity;


import java.util.List;

public class MtimeFilmeBean {

    private int totalComingMovie;
    private List<FilmItemBean> ms;

    public int getTotalComingMovie() {
        return totalComingMovie;
    }

    public void setTotalComingMovie(int totalComingMovie) {
        this.totalComingMovie = totalComingMovie;
    }

    public List<FilmItemBean> getMs() {
        return ms;
    }

    public void setMs(List<FilmItemBean> ms) {
        this.ms = ms;
    }

    @Override
    public String toString() {
        return "MtimeFilmeBean{" +
                "totalComingMovie=" + totalComingMovie +
                ", ms=" + ms +
                '}';
    }
}
