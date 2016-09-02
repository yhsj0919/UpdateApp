package xyz.yhsj.update.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * 类描述：FileUtil
 *
 * Created by LOVE on 2016/8/31 031.
 */
public class FileUtil {

    public static File updateDir = null;
    public static File updateFile = null;
    /***********
     * 保存升级APK的目录
     ***********/
    public static final String KonkaApplication = "UpdateApp";

    public static boolean isCreateFileSucess;

    /**
     * 方法描述：createFile方法
     *
     * @param app_name
     * @return
     * @see FileUtil
     */
    public static void createFile(String app_name) {

        if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
            isCreateFileSucess = true;

            updateDir = new File(Environment.getExternalStorageDirectory() + "/" + KonkaApplication + "/");
            updateFile = new File(updateDir + "/" + app_name + ".apk");

            if (!updateDir.exists()) {
                updateDir.mkdirs();
            }
            if (!updateFile.exists()) {
                try {
                    updateFile.createNewFile();
                } catch (IOException e) {
                    isCreateFileSucess = false;
                    e.printStackTrace();
                }
            }

        } else {
            isCreateFileSucess = false;
        }
    }
}