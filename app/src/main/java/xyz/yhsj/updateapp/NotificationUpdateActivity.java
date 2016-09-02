package xyz.yhsj.updateapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import xyz.yhsj.update.service.DownloadService;


public class NotificationUpdateActivity extends AppCompatActivity {
    private Button btn_cancel;// btn_update,
    private TextView tv_progress;

    private ProgressBar mProgressBar;
    // 获取到下载url后，直接复制给MapApp,里面的全局变量
    private String downloadUrl;


    UpdateUIBroadcastReceiver broadcastReceiver;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update);
        // btn_update = (Button) findViewById(R.id.update);
        btn_cancel = (Button) findViewById(R.id.cancel);
        tv_progress = (TextView) findViewById(R.id.currentPos);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar1);
        btn_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });


        // 动态注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_BROADCAST);
        broadcastReceiver = new UpdateUIBroadcastReceiver();
        registerReceiver(broadcastReceiver, filter);

    }


    /**
     * 定义广播接收器（内部类）
     *
     * @author lenovo
     */
    private class UpdateUIBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int type = intent.getIntExtra(DownloadService.KEY_BROADCAST_TYPE, -1);

            if (type == DownloadService.DOWN_LOADING) {

                mProgressBar.setMax(intent.getIntExtra(DownloadService.KEY_BROADCAST_TOTAL, 0));
                mProgressBar.setProgress(intent.getIntExtra(DownloadService.KEY_BROADCAST_COUNT, 0));

                System.out.println(intent.getIntExtra(DownloadService.KEY_BROADCAST_TOTAL, 0) + ">>>>>>"
                        + intent.getIntExtra(DownloadService.KEY_BROADCAST_COUNT, 0) + ">>>>>>>>>"
                        + intent.getStringExtra(DownloadService.KEY_BROADCAST_PERCENT));
            }


//            mProgressBar.setProgress(intent.getExtras().getInt("count"));
        }

    }

    @Override
    protected void onDestroy() {
        System.out.println("onDestroy");
        super.onDestroy();
        // 注销广播
        unregisterReceiver(broadcastReceiver);
    }


}
