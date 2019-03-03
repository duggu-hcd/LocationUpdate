package com.network;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class VolleyGsonRequest<T> extends JsonRequest<T> {
    private Class<T> clazz;
    private Map<String, String> headers = new HashMap<>();
    public VolleyGsonRequest(String url, Class<T> cls, JSONObject jsonRequest, Listener<T> listener, ErrorListener errorListener) {
        super(jsonRequest == null || jsonRequest.length() == 0 ? Request.Method.GET : Request.Method.POST, url,
                (jsonRequest == null) ? null : jsonRequest.toString(), listener, errorListener);
        clazz = cls;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        if (clazz == null) return Response.error(new ParseError());
        try {
            String dataStr = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

            return Response.success(new Gson().fromJson(dataStr, clazz),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }
}
