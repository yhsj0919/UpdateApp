package xyz.yhsj.update.bean;

/**
 * 应用更新的实体类
 * Created by LOVE on 2016/9/1 001.
 */
public class UpdateEntity {
    private String versionCode;
    private String updateUrl;
    private String content;
    private String appname;

    public String getName() {
        return appname;
    }

    public void setName(String name) {
        this.appname = name;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
