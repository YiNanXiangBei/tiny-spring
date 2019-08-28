package org.yinan.web.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.web.middleware.BaseMiddleware;
import org.yinan.web.middleware.MiddlewareManager;
import org.yinan.web.response.ObjectMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yinan
 * @date 19-6-10
 */
public class RoutesManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoutesManager.class);

    private Map<String, ObjectMethod> getUrls = new ConcurrentHashMap<>();

    private Map<String, ObjectMethod> postUrls = new ConcurrentHashMap<>();

    private List<IRouteConfig> routeConfigs = new ArrayList<>();

    public static RoutesManager INSTANCE = new RoutesManager();

    private RoutesManager() {

    }



    public  void addGetMethod(String uri, ObjectMethod objectMethod) {
        getUrls.put(uri, objectMethod);
    }

    public  void removeGetMethod(String uri) {
        getUrls.remove(uri);
    }

    public ObjectMethod gotGetMethod(String url) {
        return getUrls.get(url);
    }

    public  void addPostMethod(String uri, ObjectMethod objectMethod) {
        postUrls.put(uri, objectMethod);
    }

    public  void removePostMethod(String uri) {
        postUrls.remove(uri);
    }

    public ObjectMethod gotPostMethod(String url) {
        return postUrls.get(url);
    }

    public void addRouteConfig(IRouteConfig routeConfig) {
        routeConfigs.add(routeConfig);
    }

    public List<IRouteConfig> getRouteConfigs() {
        return new ArrayList<>(routeConfigs);
    }

    public void addRouteConfig(List<IRouteConfig> newRouteConfigs) {
        routeConfigs.addAll(newRouteConfigs);
    }

    public void activeConfigs() {
        //初始化基础拦截器
        LOGGER.info("init base middleware ...");
        MiddlewareManager.addMiddleware(new BaseMiddleware());
        //初始化扫描所有的controller
//        List<IRouteConfig> configs = getRouteConfigs();
//        LOGGER.info("init route config ...");
//        for (IRouteConfig config : configs) {
//            try {
//                config.init();
//            } catch (IOException | ClassNotFoundException e) {
//                LOGGER.error("route config init error: {}", e.getMessage());
//            }
//        }
    }

}
