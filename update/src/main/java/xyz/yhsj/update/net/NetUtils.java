package xyz.yhsj.update.net;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Set;

import xyz.yhsj.update.listener.NetCallBack;

/**
 * 网络请求基类
 * Created by LOVE on 2016/8/31 031.
 */

public class NetUtils {

    private String url;
    private HttpMetHod method;
    private HashMap<String, String> params;
    private NetCallBack callBack;


    public NetUtils(String url, HttpMetHod method, HashMap<String, String> params, NetCallBack callBack) {

        this.url = url;
        this.method = method;
        this.params = params;
        this.callBack = callBack;

        if (params == null) {
            this.params = new HashMap<>();
        }
        request();

    }


    /**
     * 请求网络
     */
    private void request() {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                StringBuffer paramsStr = new StringBuffer();

                Set<String> set = params.keySet();

                for (String s : set) {
                    paramsStr.append(s).append("=").append(params.get(s)).append("&");
                }

                System.out.println("paramsStr:" + paramsStr.toString());

                try {
                    URLConnection uc;

                    switch (method) {
                        case POST:
                            uc = new URL(url).openConnection();
                            uc.setDoOutput(true);
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(uc.getOutputStream(), "utf-8"));
                            bw.write(paramsStr.toString());
                            bw.flush();
                            break;
                        default:
                            uc = new URL(url + "?" + paramsStr.toString()).openConnection();
                            break;
                    }

                    System.out.println("url:" + uc.getURL());

                    BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream(), "utf-8"));

                    String line = null;

                    StringBuffer result = new StringBuffer();

                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }

                    return result.toString();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {

                if (callBack != null) {
                    if (s != null) {
                        callBack.onSuccess(s);
                    } else {
                        callBack.onFail();
                    }
                }
                super.onPostExecute(s);
            }
        }.execute();


    }


}
