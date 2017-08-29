package com.olegsagenadatrytwo.firebaseauthandnotification;

/**
 * Created by omcna on 8/28/2017.
 */

public interface BasePresenter<V extends BaseView> {
    void attachView(V view);
    void detachView();
}
