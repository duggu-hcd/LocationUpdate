package com.network;
import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.reactivestreams.Publisher;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class BaseVolley {
    private static RequestQueue mRequestQueue;

    public static void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context,new HurlStack());
    }

    private static RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }
    private Flowable<BaseModel> baseModelFlowable(final RequestFuture<BaseModel> future) {
        return Flowable.defer(
                new Callable<Publisher<? extends BaseModel>>() {
                    @Override
                    public Publisher<? extends BaseModel> call() throws Exception {
                        try {
                            return Flowable.just(future.get());
                        } catch (Exception e) {
                            Log.e("routes", e.getMessage());
                            return Flowable.error(e);
                        }
                    }
                });
    }

    public void startVolleyRequest(String url , JSONObject jsonObject, Class<?> modelClass ,DisposableSubscriber<BaseModel> d) {
        RequestFuture<BaseModel> future = RequestFuture.newFuture();
        VolleyGsonRequest<BaseModel> req = new VolleyGsonRequest(url,modelClass,jsonObject,future, future);
        req.setRetryPolicy(new DefaultRetryPolicy(1 * 1000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        BaseVolley.getRequestQueue().add(req);
        baseModelFlowable(future)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(d);
    }

}