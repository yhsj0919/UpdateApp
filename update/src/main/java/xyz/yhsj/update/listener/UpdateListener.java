package xyz.yhsj.update.listener;

import xyz.yhsj.update.bean.UpdateEntity;

/**
 * Created by LOVE on 2016/9/12 012.
 */

public interface UpdateListener {
    void Update(UpdateEntity updateEntity);

    void UnUpdate();

}
