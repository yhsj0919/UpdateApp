package xyz.yhsj.update.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import xyz.yhsj.update.R;
import xyz.yhsj.update.UpdateHelper;
import xyz.yhsj.update.utils.FileUtil;

/***
 * 升级服务
 * <p>
 * Created by LOVE on 2016/8/31 031.
 */
public class DownloadService extends Service {

    public static final String Install_Apk = "Install_Apk";

    //下载进度步进
    private static final int down_step_custom = 3;
    // 超时
    private static final int TIMEOUT = 10 * 1000;
    //handler状态
    public static final int DOWN_SUCCESS = 0;
    public static final int DOWN_LOADING = 1;
    public static final int DOWN_ERROR = -1;
    //通知
    private NotificationManager mNotificationManager1 = null;
    private NotificationCompat.Builder builder;
    private final int NotificationID = 0x10000;
    //app名称
    private String app_name;
    //自动安装
    private boolean auto_Install;
    //appUrl
    private static String down_url;
    //参数
    //app名称
    public static final String KEY_APP_NAME = "Key_App_Name";
    //appUrl
    public static final String KEY_DOWN_URL = "Key_Down_Url";
    //自动安装
    public static final String KEY_AUTO_INSTALL = "Key_Auto_Install";

    //广播的action
    public static final String ACTION_BROADCAST = "xyz.yhsj.update.service.DownloadService";
    //广播
    private final Intent broadcast_intent = new Intent(ACTION_BROADCAST);
    //消息类型
    public static final String KEY_BROADCAST_TYPE = "Key_Broadcast_Type";
    //总长度
    public static final String KEY_BROADCAST_TOTAL = "Key_Broadcast_Total";
    //下载的长度
    public static final String KEY_BROADCAST_COUNT = "Key_Broadcast_Count";
    //百分比
    public static final String KEY_BROADCAST_PERCENT = "Key_Broadcast_Percent";


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * 方法描述：onStartCommand方法
     *
     * @param intent, int flags, int startId
     * @return int
     * @see DownloadService
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (intent != null) {
            app_name = intent.getStringExtra(KEY_APP_NAME);
            down_url = intent.getStringExtra(KEY_DOWN_URL);
            auto_Install = intent.getBooleanExtra(KEY_AUTO_INSTALL, false);
        }

        // create file,应该在这个地方加一个返回值的判断SD卡是否准备好，文件是否创建成功，等等！
        FileUtil.createFile(app_name);

        if (FileUtil.isCreateFileSucess == true && !TextUtils.isEmpty(down_url)) {
            createNotification();

            createThread();
        } else {
            Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
            /***************stop service************/
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }


    /*********
     * update UI
     ******/
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_SUCCESS:

                    // 震动提示
                    try {
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(500L);// 参数是震动时间(long类型)
                    } catch (Exception e) {

                    }

                    if (auto_Install) {
                        /*****安装APK******/
                        installApk();
                        mNotificationManager1.cancel(NotificationID);
                    } else {
                        /*********下载完成，点击安装***********/
                        Uri uri = Uri.fromFile(FileUtil.updateFile);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "application/vnd.android.package-archive");
                        PendingIntent pendingIntent = PendingIntent.getActivity(DownloadService.this, 0, intent, 0);
                        builder.setAutoCancel(true);
                        builder.setContentIntent(pendingIntent);
                        builder.setContentText("下载完成，点击安装");
                        mNotificationManager1.notify(NotificationID, builder.build());

                        Toast.makeText(DownloadService.this, "下载完成，下拉通知栏，点击安装", Toast.LENGTH_LONG).show();

                    }


                    //广播
                    broadcast_intent.putExtra(KEY_BROADCAST_TYPE, DOWN_SUCCESS);
                    sendBroadcast(broadcast_intent);

                    /***stop service*****/
                    stopSelf();
                    break;

                case DOWN_ERROR:

                    //广播
                    broadcast_intent.putExtra(KEY_BROADCAST_TYPE, DOWN_ERROR);
                    sendBroadcast(broadcast_intent);

                    builder.setAutoCancel(true);

                    Toast.makeText(DownloadService.this, "下载停止", Toast.LENGTH_SHORT).show();

                    mNotificationManager1.cancel(NotificationID);

                    /***stop service*****/
                    stopSelf();
                    break;

                default:
                    //stopService(updateIntent);
                    /******Stop service******/
                    //stopService(intentname)
                    //stopSelf();
                    break;
            }
        }
    };

    private void installApk() {
        /*********下载完成，点击安装***********/
        Uri uri = Uri.fromFile(FileUtil.updateFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);

        /**********加这个属性是因为使用Context的startActivity方法的话，就需要开启一个新的task**********/
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intent);
    }

    /**
     * 方法描述：createThread方法, 开线程下载
     *
     * @param
     * @return
     * @see DownloadService
     */
    public void createThread() {
        new DownLoadThread().start();
    }

    private class DownLoadThread extends Thread {
        @Override
        public void run() {
            Message message = new Message();
            try {
                long downloadSize = downloadUpdateFile(down_url, FileUtil.updateFile.toString());
                if (downloadSize > 0) {
                    // down success
                    message.what = DOWN_SUCCESS;
                    handler.sendMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
                message.what = DOWN_ERROR;
                handler.sendMessage(message);
            }
        }
    }


    /**
     * 方法描述：createNotification方法
     *
     * @param
     * @return
     * @see DownloadService
     */
    public void createNotification() {

        mNotificationManager1 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setTicker("正在下载新版本");
        builder.setContentTitle(app_name);
        builder.setContentText("正在下载,请稍后...");
        builder.setNumber(0);
        builder.setAutoCancel(false);
        mNotificationManager1.notify(NotificationID, builder.build());
    }

    /***
     * down file
     *
     * @return
     * @throws
     */
    public long downloadUpdateFile(String down_url, String file) throws Exception {

        int down_step = down_step_custom;// 提示step
        int totalSize;// 文件总大小
        int downloadCount = 0;// 已经下载好的大小
        int updateCount = 0;// 已经上传的文件大小

        InputStream inputStream;
        OutputStream outputStream;

        URL url = new URL(down_url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setConnectTimeout(TIMEOUT);
        httpURLConnection.setReadTimeout(TIMEOUT);
        // 获取下载文件的size
        totalSize = httpURLConnection.getContentLength();

        if (httpURLConnection.getResponseCode() == 404) {
            throw new Exception("fail!");
            //这个地方应该加一个下载失败的处理，但是，因为我们在外面加了一个try---catch，已经处理了Exception,
            //所以不用处理
        }

        inputStream = httpURLConnection.getInputStream();
        outputStream = new FileOutputStream(file, false);// 文件存在则覆盖掉

        byte buffer[] = new byte[1024];
        int readsize = 0;

        while ((readsize = inputStream.read(buffer)) != -1) {

//          /*********如果下载过程中出现错误，就弹出错误提示，并且把notificationManager取消*********/
//          if (httpURLConnection.getResponseCode() == 404) {
//              notificationManager.cancel(R.layout.notification_item);
//              throw new Exception("fail!");
//              //这个地方应该加一个下载失败的处理，但是，因为我们在外面加了一个try---catch，已经处理了Exception,
//              //所以不用处理
//          }

            outputStream.write(buffer, 0, readsize);
            downloadCount += readsize;// 时时获取下载到的大小


//            System.out.println(">>>>>>>>>>>>>>>>>>>>" + ((downloadCount * 100.0) / totalSize));

            if (UpdateHelper.getInstance().isDownload_Cancle()) {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                    inputStream.close();
                    outputStream.close();
                    UpdateHelper.getInstance().setDownload_Cancle(false);
                }
            }


            /*** 每次增张3%**/
            if (updateCount == 0 || (downloadCount * 100.0 / totalSize) >= updateCount) {
                updateCount += down_step;

                // 改变通知栏
                builder.setProgress(totalSize, downloadCount, false);
                builder.setContentInfo(getPercent(downloadCount, totalSize));
                mNotificationManager1.notify(NotificationID, builder.build());

                //广播
                broadcast_intent.putExtra(KEY_BROADCAST_TOTAL, totalSize);
                broadcast_intent.putExtra(KEY_BROADCAST_COUNT, downloadCount);
                broadcast_intent.putExtra(KEY_BROADCAST_PERCENT, getPercent(downloadCount, totalSize));
                broadcast_intent.putExtra(KEY_BROADCAST_TYPE, DOWN_LOADING);
                sendBroadcast(broadcast_intent);

            }

            if (downloadCount == totalSize) {

                System.out.println(">>>>>>>>>>>>>" + getPercent(downloadCount, totalSize));

                builder.setProgress(totalSize, downloadCount, false);
                builder.setContentInfo(getPercent(downloadCount, totalSize));
                mNotificationManager1.notify(NotificationID, builder.build());

                //广播
                broadcast_intent.putExtra(KEY_BROADCAST_TOTAL, totalSize);
                broadcast_intent.putExtra(KEY_BROADCAST_COUNT, downloadCount);
                broadcast_intent.putExtra(KEY_BROADCAST_PERCENT, getPercent(downloadCount, totalSize));
                broadcast_intent.putExtra(KEY_BROADCAST_TYPE, DOWN_LOADING);
                sendBroadcast(broadcast_intent);
            }
        }

        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
        }

        inputStream.close();
        outputStream.close();

        return downloadCount;
    }

    /**
     * @param x     当前值
     * @param total 总值
     * @return 当前百分比
     * @Description:返回百分之值
     */
    private String getPercent(int x, int total) {
        String result = "";// 接受百分比的值
        double x_double = x * 1.0;
        double tempresult = x_double / total;
        // 百分比格式，后面不足2位的用0补齐 ##.00%
        DecimalFormat df1 = new DecimalFormat("0.0%");
        result = df1.format(tempresult);
        return result;
    }

}