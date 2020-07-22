package com.codepath.gameswap;

import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BGGAsyncTask extends AsyncTask<String, String, String> {

    public interface BGGResponse {
        enum BGGResponseType { SEARCH, DETAIL}
        void onFinish(String output, BGGResponseType responseType);
    }

    private BGGResponse delegate;
    private BGGResponse.BGGResponseType responseType;

    public BGGAsyncTask(BGGResponse delegate, BGGResponse.BGGResponseType responseType) {
        this.delegate = delegate;
        this.responseType = responseType;
    }

    @Override
    protected String doInBackground(String... strings) {
        String url = strings[0];
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.onFinish(result, responseType);
    }
}
