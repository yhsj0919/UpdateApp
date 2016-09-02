package xyz.yhsj.update;

/**
 * 数据解析
 * Created by LOVE on 2016/8/31 031.
 */
public interface ParseData {
    <T> T parse(String httpResponse);
}
