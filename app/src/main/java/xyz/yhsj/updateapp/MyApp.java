package xyz.yhsj.updateapp;

import android.app.Application;

import xyz.yhsj.update.listener.ParseData;
import xyz.yhsj.update.UpdateHelper;
import xyz.yhsj.update.bean.UpdateEntity;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        UpdateHelper.init(this);

        UpdateHelper
                .getInstance()

                .get("http://gank.io/api/data/福利/2/1")

                .setDownType(UpdateHelper.DownType.down_click_Install)
                .showProgressDialog(true)
                .setJsonParser(new ParseData() {
                    @Override
                    public UpdateEntity parse(String httpResponse) {

                        UpdateEntity updateEntity = new UpdateEntity();
                        updateEntity.setAppName("QQ");
                        updateEntity.setContent("修复了各种各样的bug");
                        updateEntity.setUpdateUrl("https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk");
                        updateEntity.setVersionCode(6);
                        updateEntity.setVersionName("6.0");

                        return updateEntity;
                    }
                });

    }


}
