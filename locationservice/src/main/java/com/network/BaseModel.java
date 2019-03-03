package com.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BaseModel {

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    @SerializedName("message")
    @Expose
    private String msg;
    @SerializedName("error")
    @Expose
    private boolean error;


}