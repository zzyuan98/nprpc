package com.fixbug.controller;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

/**
 * @author zzyuan
 * @create 2022-04-07 0:08
 */
public class NrpcController implements RpcController {

    private String errTest;

    private boolean isfailed;

    @Override
    public void reset() {
        this.isfailed = false;
        this.errTest = "";
    }

    @Override
    public boolean failed() {
        return isfailed;
    }

    @Override
    public String errorText() {
        return errTest;
    }

    @Override
    public void startCancel() {

    }

    @Override
    public void setFailed(String s) {
        this.isfailed = true;
        this.errTest = s;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void notifyOnCancel(RpcCallback<Object> rpcCallback) {

    }
}
