package org.yinan.web.routes;

import org.yinan.web.annotation.Controller;
import org.yinan.web.annotation.GetMapping;
import org.yinan.web.annotation.PostMapping;
import org.yinan.web.response.ObjectMethod;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author yinan
 * @date 19-6-10
 */
public interface IRouteConfig {
    String FILE = "file";
    String JAR = "jar";

    /**
     * 初始化方法
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void init(List<String> packageName) throws IOException, ClassNotFoundException;


    /**
     * 初始化方法，抽取指定包下的所有get方法和post方法
     * @param packageName 包名
     * @throws IOException
     * @throws ClassNotFoundException
     */
    default void init0(String packageName) throws IOException, ClassNotFoundException {
        List<Class> classes;
        classes = getClass(packageName);

        classes.forEach(clazz -> {
            Controller controller = (Controller) clazz.getAnnotation(Controller.class);
            if (controller == null) {
                return;
            }

            String classGetURI = "";
            String classPostURI = "";

            GetMapping classGetMapping = (GetMapping) clazz.getAnnotation(GetMapping.class);
            if (classGetMapping != null) {
                classGetURI = "/".equals(classGetMapping.value()) ? "" : classGetMapping.value();
            }

            PostMapping classPostMapping = (PostMapping) clazz.getAnnotation(PostMapping.class);
            if (classPostMapping != null) {
                classPostURI = "/".equals(classPostMapping.value()) ? "" : classPostMapping.value();
            }

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                GetMapping methodGetMapping = method.getAnnotation(GetMapping.class);
                PostMapping methodPostMapping = method.getAnnotation(PostMapping.class);


                if (methodGetMapping != null) {
                    String methodURI = classGetURI + methodGetMapping.value();
                    RoutesManager.INSTANCE.addGetMethod(methodURI, new ObjectMethod(clazz, method));
                }
                if (methodPostMapping != null) {
                    String methodURI = classPostURI + methodPostMapping.value();
                    RoutesManager.INSTANCE.addPostMethod(methodURI, new ObjectMethod(clazz, method));
                }
            }
        });
    }

    /**
     * 依据指定包名，查找该包名下的所有类文件
     * @param packageName 包名
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    default List<Class> getClass(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<Class> classes = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (FILE.equals(resource.getProtocol())) {
                doScanPackageClassesByFile(packageName, resource, classes);
            } else if (JAR.equals(resource.getProtocol())) {
                doScanPackageClassesByJar(path, resource, classes);
            }

        }




        return classes;
    }

    /**
     * 通过jar文件生成class
     * @param path 路径
     * @param resource 资源
     * @param classes 类集合
     * @throws IOException
     * @throws ClassNotFoundException
     */
    default void doScanPackageClassesByJar(String path, URL resource, List<Class> classes) throws IOException, ClassNotFoundException {
        JarFile jarFile = ((JarURLConnection)resource.openConnection()).getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String name = jarEntry.getName();
            if (!name.startsWith(path) || jarEntry.isDirectory()) {
                continue;
            }
            if (name.lastIndexOf("/") != path.length() || name.indexOf('$') != -1) {
                continue;
            }
            if (name.endsWith(".class")) {
                name = name.replace("/", ".");
                name = name.substring(0, name.length() - 6);
                classes.add(Thread.currentThread().getContextClassLoader().loadClass(name));
            }
        }
    }

    /**
     * 通过普通file文件生成class
     * @param packageName 包名
     * @param resource 资源
     * @param classes 类集合
     * @throws ClassNotFoundException
     */
    default void doScanPackageClassesByFile(String packageName, URL resource, List<Class> classes) throws ClassNotFoundException {
        classes.addAll(findClass(new File(resource.getFile()), packageName));
    }


    /**
     * 递归查找类文件
     * @param directory 文件夹名称
     * @param packageName 包名
     * @return 所有类文件，包含全名称类名，即包名 + 类名
     * @throws ClassNotFoundException
     */
    default List<Class> findClass(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClass(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + "." + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

}
