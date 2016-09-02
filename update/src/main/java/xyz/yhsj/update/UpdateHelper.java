package xyz.yhsj.update;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;

import xyz.yhsj.update.net.HttpMetHod;
import xyz.yhsj.update.net.UpdateAgent;

/**
 * 升级帮助类
 * Created by LOVE on 2016/8/31 031.
 */
public class UpdateHelper {


    private Context mContext;
    private String checkUrl;
    private HashMap<String, String> params;
    private ParseData jsonParser;
    private boolean showProgressDialog;

    private static UpdateHelper instance;

    //双重嵌套一级是否强制更新
    private boolean updateForce = false;

    //联网请求方式
    private HttpMetHod httpMetHod = HttpMetHod.GET;

    //二级（1.手动更新2.自动更新（有网更新，只有WiFi更新，只有WiFi下载））
    public enum CheckType {
        check_with_Dialog,
        check_no_Dialog
    }

    public enum DownType {
        down_auto_Install,
        down_click_Install
    }

    //默认需要检测更新
    private CheckType checkType = CheckType.check_no_Dialog;
    private DownType downType = DownType.down_auto_Install;

    public static UpdateHelper getInstance() {
        if (instance == null) {
            throw new RuntimeException("UpdateHelper not initialized!");
        } else {
            return instance;
        }
    }

    public static void init(Context appContext) {
        instance = new UpdateHelper(appContext);
    }

    public UpdateHelper(Context context) {
        this.mContext = context;

    }

    public UpdateHelper get(String url) {
        this.checkUrl = url;
        this.httpMetHod = HttpMetHod.GET;
        return this;
    }

    public UpdateHelper post(String url, HashMap<String, String> params) {
        this.checkUrl = url;
        this.params = params;
        this.httpMetHod = HttpMetHod.POST;
        return this;
    }

    public UpdateHelper setJsonParser(ParseData jsonParser) {
        this.jsonParser = jsonParser;
        return this;
    }

    public UpdateHelper setCheckType(CheckType checkType) {
        this.checkType = checkType;
        return this;
    }

    public UpdateHelper setDownType(DownType downType) {
        this.downType = downType;
        return this;
    }

    public UpdateHelper showProgressDialog(boolean showProgressDialog) {
        this.showProgressDialog = showProgressDialog;
        return this;
    }


    public Context getContext() {
        if (mContext == null) {
            throw new RuntimeException("should call UpdateHelper.init first");
        }
        return mContext;
    }

    public CheckType getCheckType() {
        return checkType;
    }

    public DownType getDownType() {
        return downType;
    }

    public String getUrl() {
        if (TextUtils.isEmpty(checkUrl)) {
            throw new IllegalArgumentException("Url is null");
        }
        return checkUrl;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public HttpMetHod getHttpMethod() {
        return httpMetHod;
    }

    public ParseData getJsonParser() {
        if (jsonParser == null) {
            throw new IllegalStateException("update parser is null");
        }
        return jsonParser;
    }


    public boolean isShowProgressDialog() {
        return showProgressDialog;
    }


    public void check(Activity activity) {
        UpdateAgent.getInstance().checkUpdate(activity);
    }

}
