package cn.yinan.web.routes;

import java.io.IOException;
import java.util.List;

/**
 * @author yinan
 * @date 19-6-10
 */
public class BaseRouteConfig implements IRouteConfig {

    private final static String BASE_PACKAGE_NAME = "cn.yinan.ddns.web.controller";

    @Override
    public void init(List<String> packageNames) {
        packageNames.add(BASE_PACKAGE_NAME);
        packageNames.forEach(packageName -> {
            try {
                init0(packageName);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
