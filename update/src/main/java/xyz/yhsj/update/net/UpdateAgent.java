package xyz.yhsj.update.net;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.List;

import xyz.yhsj.update.UpdateHelper;
import xyz.yhsj.update.bean.UpdateEntity;
import xyz.yhsj.update.listener.NetCallBack;
import xyz.yhsj.update.service.DownloadService;


public class UpdateAgent {
    private static UpdateAgent updater;
    //检查更新的弹窗
    private ProgressDialog checkDialog;
    //下载的进度弹窗
    private ProgressDialog downDialog;
    //下载进度的广播
    private UpdateProgressBroadcastReceiver broadcastReceiver;


    public static UpdateAgent getInstance() {
        if (updater == null) {
            updater = new UpdateAgent();
        }
        return updater;
    }


    /**
     * 检测更新
     *
     * @param activity
     */
    public void checkUpdate(final Activity activity) {

        if (UpdateHelper.getInstance().getCheckType() == UpdateHelper.CheckType.check_with_Dialog) {

            checkDialog = new ProgressDialog(activity);
            checkDialog.setMessage("正在检查更新...");
            checkDialog.setCancelable(false);

            if (!activity.isFinishing()) {
                checkDialog.show();
            } else {
                return;
            }

        }

        new NetUtils(
                UpdateHelper.getInstance().getUrl(),
                UpdateHelper.getInstance().getHttpMethod(),
                UpdateHelper.getInstance().getParams(),
                new NetCallBack() {
                    @Override
                    public void onSuccess(String result) {

                        if (checkDialog != null && checkDialog.isShowing()) {
                            checkDialog.dismiss();
                        }

                        UpdateEntity updateEntity = UpdateHelper.getInstance().getJsonParser().parse(result);
                        if (updateEntity == null || TextUtils.isEmpty(updateEntity.getUpdateUrl())) {
                            //通知前台更新状态
                            if (UpdateHelper.getInstance().getUpdateListener() != null) {
                                UpdateHelper.getInstance().getUpdateListener().UnUpdate();
                                //销毁监听器，防止因为单例模式下监听器未消毁导致的异常
                                UpdateHelper.getInstance().setUpdateListener(null);
                            }

                            if (UpdateHelper.getInstance().getUpdateTipType() == UpdateHelper.UpdateTipType.tip_dialog) {
                                showNoUpdateDialog(activity);
                            } else if (UpdateHelper.getInstance().getUpdateTipType() == UpdateHelper.UpdateTipType.tip_toast) {
                                Toast.makeText(activity, "已是最新版本", Toast.LENGTH_SHORT).show();
                            } else if (UpdateHelper.getInstance().getUpdateTipType() == UpdateHelper.UpdateTipType.tip_without) {

                            }

                        } else {
                            //通知前台更新状态
                            if (UpdateHelper.getInstance().getUpdateListener() != null) {
                                UpdateHelper.getInstance().getUpdateListener().Update(updateEntity);
                                //销毁监听器，防止因为单例模式下监听器未消毁导致的异常
                                UpdateHelper.getInstance().setUpdateListener(null);
                            }
                            showAlertDialog(activity, updateEntity);
                        }
                    }

                    @Override
                    public void onFail() {
                        if (checkDialog != null && checkDialog.isShowing()) {
                            checkDialog.dismiss();
                        }
                        if (UpdateHelper.getInstance().getUpdateTipType() == UpdateHelper.UpdateTipType.tip_dialog) {
                            showNoUpdateDialog(activity);
                        } else if (UpdateHelper.getInstance().getUpdateTipType() == UpdateHelper.UpdateTipType.tip_toast) {
                            Toast.makeText(activity, "已是最新版本", Toast.LENGTH_SHORT).show();
                        } else if (UpdateHelper.getInstance().getUpdateTipType() == UpdateHelper.UpdateTipType.tip_without) {

                        }
                    }
                });
    }

    /**
     * 没有更新的dialog
     *
     * @param activity
     */
    private void showNoUpdateDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("提示");
        builder.setMessage("已是最新版本");
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    /**
     * 有更新的dialog
     *
     * @param activity
     * @param updateEntity
     */
    private void showAlertDialog(final Activity activity, final UpdateEntity updateEntity) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("发现新版本 " + updateEntity.getVersionCode());
        builder.setMessage(updateEntity.getContent());
        builder.setCancelable(false);
        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadApp(activity, updateEntity);

            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 下载APP
     *
     * @param activity
     * @param updateEntity
     */
    private void downloadApp(Activity activity, UpdateEntity updateEntity) {
        //判断服务是否运行，防止重复启动产生错误
        if (!isServiceWork(activity, DownloadService.class.getName())) {

            Intent it = new Intent(activity, DownloadService.class);
            it.putExtra(DownloadService.KEY_APP_NAME, updateEntity.getName());
            it.putExtra(DownloadService.KEY_DOWN_URL, updateEntity.getUpdateUrl());

            //是否自动安装
            if (UpdateHelper.getInstance().getDownType() == UpdateHelper.DownType.down_auto_Install) {
                it.putExtra(DownloadService.KEY_AUTO_INSTALL, true);
            } else {
                it.putExtra(DownloadService.KEY_AUTO_INSTALL, false);
            }

            activity.startService(it);
            //判断是否显示进度弹窗
            if (UpdateHelper.getInstance().isShowProgressDialog()) {
                downProgressDialog(activity);
            }

        } else {
            Toast.makeText(activity, "正在进行下载任务，请稍后", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 下载时的进度条弹窗
     *
     * @param activity
     */
    private void downProgressDialog(final Activity activity) {

        // 动态注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_BROADCAST);
        broadcastReceiver = new UpdateProgressBroadcastReceiver();
        activity.registerReceiver(broadcastReceiver, filter);


        downDialog = new ProgressDialog(activity);
        downDialog.setTitle("正在下载");
        downDialog.setCancelable(false);
        downDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downDialog.setButton(
                ProgressDialog.BUTTON_POSITIVE,
                "后台",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        downDialog.setButton(
                ProgressDialog.BUTTON_NEGATIVE,
                "取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        UpdateHelper.getInstance().setDownload_Cancle(true);
                    }
                });

        downDialog.show();

        downDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                // 注销广播
                if (broadcastReceiver != null) {
                    activity.unregisterReceiver(broadcastReceiver);
                }
            }
        });

    }

    /**
     * 定义广播接收器（内部类）
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


    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName 是包名+服务的类名（例如：xyz.yhsj.upadte.service.DownloadService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public boolean isServiceWork(Context mContext, String serviceName) {

        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);

        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName();
            if (mName.equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

}
