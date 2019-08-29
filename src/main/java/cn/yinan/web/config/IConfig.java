package cn.yinan.web.config;

import cn.yinan.web.listener.ConfigChangedListener;

/**
 * @author yinan
 * @date 19-6-10
 */
public interface IConfig {

    /**
     * 更新配置文件
     * @param configJson 最新的配置文件信息
     */
    void update(String configJson);

    /**
     * 添加监听器，这些监听器主要是用来处理配置文件的
     * @param listener 监听器
     */
    void addConfigChangedListener(ConfigChangedListener listener);

    /**
     * 通知监听器
     */
    void notifyListener();

    /**
     * 移除指定的listener
     * @param listener 监听器
     */
    void removeConfigChangedListener(ConfigChangedListener listener);
}
