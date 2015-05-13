package com.wonsuc.weatherappclient.common;

import android.os.Handler;
import android.os.Looper;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.Callable;

public class HttpClient {

    // OkHttpClient 객체는 멤버필드에서 final 형태로 한번만 생성해서 newCall 메서드를 통해 재사용한다.
    private final OkHttpClient client = new OkHttpClient();
    public Request request;
    public IOException ioexception;
    public Response response;

    private void resetCallback() {
        this.request = null;
        this.ioexception = null;
        this.response = null;
    }

    public Call get(String url, String params, final Callable<?> fail, final Callable<?> success) throws IOException {
        resetCallback();

        Request request = new Request.Builder()
                .url(url + "?" + params)
                .get()
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            Handler mainHandler = new Handler(Looper.getMainLooper());

            @Override
            public void onFailure(final Request request, final IOException e) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpClient.this.request = request;
                            HttpClient.this.ioexception = ioexception;
                            fail.call();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpClient.this.response = response;
                            success.call();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });
        return call;
    }
}
