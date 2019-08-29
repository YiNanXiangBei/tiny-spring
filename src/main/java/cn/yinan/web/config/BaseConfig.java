package cn.yinan.web.config;

import cn.yinan.web.listener.ConfigChangedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.yinan.common.config.Config;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yinan
 * @date 19-6-9
 */
public class BaseConfig implements IConfig {

    private static Logger logger = LoggerFactory.getLogger(BaseConfig.class);

    private static final String CONFIG_FILE;

    private static BaseConfig instance = new BaseConfig();

    /**
     * 配置变化监听器
     */
    private List<ConfigChangedListener> configChangedListeners = new ArrayList<>();

    /** 配置服务绑定主机host */
    private String configServerBind;

    /** 配置服务端口 */
    private Integer configServerPort;

    /** 配置服务管理员用户名 */
    private String configAdminUsername;

    /** 配置服务管理员密码 */
    private String configAdminPassword;

    static {
        String dataPath = System.getProperty("user.home") + "/" + ".yinan/";
        File file = new File(dataPath);
        if (!file.isDirectory()) {
            file.mkdir();
        }
        CONFIG_FILE = dataPath + "/config.json";
    }


    private BaseConfig() {
        // 配置服务器主机和端口配置初始化
        this.configServerPort = Config.getInstance().getIntValue("config.server.port");
        this.configServerBind = Config.getInstance().getStringValue("config.server.bind", "0.0.0.0");

        // 配置服务器管理员登录认证信息
        this.configAdminUsername = Config.getInstance().getStringValue("config.admin.username");
        this.configAdminPassword = Config.getInstance().getStringValue("config.admin.password");

        logger.info(
                "config init  configServerBind {}, configServerPort {}, configAdminUsername {}, configAdminPassword {}",
                configServerBind, configServerPort, configAdminUsername, configAdminPassword);

        update(null);
    }


    @Override
    public void update(String configJson) {

    }

    @Override
    public void addConfigChangedListener(ConfigChangedListener listener) {
        configChangedListeners.add(listener);
    }

    @Override
    public void notifyListener() {
        List<ConfigChangedListener> changedListeners = new ArrayList<>(configChangedListeners);
        changedListeners.forEach(ConfigChangedListener::onChanged);
    }

    @Override
    public void removeConfigChangedListener(ConfigChangedListener listener) {
        configChangedListeners.remove(listener);
    }

    public static BaseConfig getInstance() {
        return instance;
    }

    public String getConfigServerBind() {
        return configServerBind;
    }

    public void setConfigServerBind(String configServerBind) {
        this.configServerBind = configServerBind;
    }

    public Integer getConfigServerPort() {
        return configServerPort;
    }

    public void setConfigServerPort(Integer configServerPort) {
        this.configServerPort = configServerPort;
    }

    public String getConfigAdminUsername() {
        return configAdminUsername;
    }

    public void setConfigAdminUsername(String configAdminUsername) {
        this.configAdminUsername = configAdminUsername;
    }

    public String getConfigAdminPassword() {
        return configAdminPassword;
    }

    public void setConfigAdminPassword(String configAdminPassword) {
        this.configAdminPassword = configAdminPassword;
    }
}
