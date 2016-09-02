# UpdateApp
android应用应用内更新解决方案

#配置


``` java

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
                        updateEntity.setName("QQ");
                        updateEntity.setContent("修复了各种各样的bug");
                        updateEntity.setUpdateUrl("https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk");
                        updateEntity.setVersionCode("6.0");

                        return updateEntity;
                    }
                });
```
#调用
```java
         UpdateHelper
                  .getInstance()
                  .setCheckType(UpdateHelper.CheckType.check_with_Dialog)
                  .check(MainActivity.this);

```
#进度获取
```java
     /**
     * 定义广播接收器
     *
     * @author LOVE
     */
    private class UpdateProgressBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int type = intent.getIntExtra(DownloadService.KEY_BROADCAST_TYPE, -1);

            if (type == DownloadService.DOWN_LOADING) {

                if (!downDialog.isShowing()) {
                    downDialog.show();
                }
                //更新弹窗进度
                downDialog.setMax(intent.getIntExtra(DownloadService.KEY_BROADCAST_TOTAL, 0) / 1000);
                downDialog.setProgress(intent.getIntExtra(DownloadService.KEY_BROADCAST_COUNT, 0) / 1000);

            }

            //关闭弹窗
            if (type == DownloadService.DOWN_SUCCESS) {
                if (downDialog != null) {
                    downDialog.dismiss();
                }
            }
            //关闭弹窗
            if (type == DownloadService.DOWN_ERROR) {

                if (downDialog != null) {
                    downDialog.dismiss();
                }
            }
        }
    }
    
```

```java
            // 动态注册广播
            IntentFilter filter = new IntentFilter();
            filter.addAction(DownloadService.ACTION_BROADCAST);
            broadcastReceiver = new UpdateUIBroadcastReceiver();
            registerReceiver(broadcastReceiver, filter);

```

#功能
1.检查进度弹窗
2.更新消息弹窗
3.service下载
4.下载进度弹窗
5.通知栏进度
6.自动安装
7.点击通知栏安装
8.下载进度广播