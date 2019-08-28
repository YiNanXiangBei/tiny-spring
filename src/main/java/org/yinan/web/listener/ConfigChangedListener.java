package org.yinan.web.listener;

/**
 * 配置文件监听器，随着配置文件更改而重新加载
 * @author yinan
 * @date 19-6-10
 */
public interface ConfigChangedListener {

    /**
     * 配置文件更改触发重新加载配置文件
     */
    void onChanged();

}
