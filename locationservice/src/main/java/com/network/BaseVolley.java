package com.network;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class BaseVolley {
    private static RequestQueue mRequestQueue;
    private static HashMap<String, String> headers;

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

    private static String generateGetUrl(String url, Map<String, String> params) {
        Uri.Builder uriBuilder = Uri.parse(url).buildUpon();
        if (params != null) {
            Set<String> keySet = new TreeSet<>(params.keySet());
            for (String key : keySet) {
                String value = params.get(key);
                if (TextUtils.isEmpty(value)) {
                    value = "";
                }
                uriBuilder.appendQueryParameter(key, value);
            }
        }
        return uriBuilder.toString().substring(0,uriBuilder.toString().length());
    }

    private Flowable<BaseModel> baseModelFlowable(RequestFuture<BaseModel> future) {
        return Flowable.defer(
                () -> {
                    try {
                        return Flowable.just(future.get());
                    } catch (Exception e) {
                        Log.e("routes", e.getMessage());
                        return Flowable.error(e);
                    }
                });
    }

    public void startVolleyRequest(String url , JSONObject jsonObject, Class<?> modelClass ,DisposableSubscriber<BaseModel> d) {
        RequestFuture<BaseModel> future = RequestFuture.newFuture();
        VolleyGsonRequest<BaseModel> req = new VolleyGsonRequest(url,modelClass,jsonObject,future, future);
        req.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        BaseVolley.getRequestQueue().add(req);

        baseModelFlowable(future)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorResumeNext(new Function<Throwable, Publisher<? extends BaseModel>>() {
                        @Override
                        public Publisher<? extends BaseModel> apply(Throwable throwable) throws Exception {
                            Log.e("BaseVolley error",throwable.getMessage().toString());
                            return null;
                        }
                    })
                    .subscribe(d);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private void addRequest(final Request<?> request) {
        if (!request.shouldCache() && request.hasHadResponseDelivered()) {
            throw new UnsupportedOperationException(
                    "Cannot reuse Request which has already served the request");
        }
        mHandler.post(() -> mRequestQueue.add(request));
    }

    public void cancel() {
        mRequestQueue.cancelAll(this);
    }

}