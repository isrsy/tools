package com.jiuqi.nr.definition.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.junit.jupiter.api.Test;

import com.github.crab2died.ExcelUtils;
import com.github.crab2died.sheet.wrapper.SimpleSheetWrapper;


public class ClassUtil {

    /**
     * 从指定的package中获取所有的Class
     * @param packageName
     * @return
     */
    public static List<Class<?>> getClasses(String packageName) {

        // 第一个class类的集合
        List<Class<?>> classes = new ArrayList<Class<?>>();
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    classes.addAll(findClassByDirectory(packageName, filePath));
                } else if ("jar".equals(protocol)) {
                    classes.addAll(findClassInJar(packageName, url));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     * @param packageName
     * @param packagePath
     */
    public static List<Class<?>> findClassByDirectory(String packageName, String packagePath) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return new ArrayList<>(0);
        }

        File[] dirs = dir.listFiles();
        List<Class<?>> classes = new ArrayList<Class<?>>();
        // 循环所有文件
        for (File file : dirs) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                classes.addAll(findClassByDirectory(packageName + "." + file.getName(), file.getAbsolutePath()));
            } else if (file.getName().endsWith(".class")) {
                // 如果是java类文件，去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Class.forName(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return classes;
    }

    public static List<Class<?>> findClassInJar(String packageName, URL url) {

        List<Class<?>> classes = new ArrayList<Class<?>>();

        String packageDirName = packageName.replace('.', '/');
        // 定义一个JarFile
        JarFile jar;
        try {
            // 获取jar
            jar = ((JarURLConnection) url.openConnection()).getJarFile();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                String name = entry.getName();
                if (name.charAt(0) == '/') {
                    // 获取后面的字符串
                    name = name.substring(1);
                }

                // 如果前半部分和定义的包名相同
                if (name.startsWith(packageDirName) && name.endsWith(".class")) {
                    // 去掉后面的".class"
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    try {
                        // 添加到classes
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    @Test
    void test() {
        List<Class<?>> classes = getClasses("com.jiuqi.nr.definition.facade");
        String beanFilePath = "/Users/rsy/WorkSpace/release-2.4.0/nr-definition/nr.definition/src/main/java/";

        for (Class<?> aClass : classes) {
//            System.out.println(aClass.getName());
            System.out.println(beanFilePath + aClass.getName().replace(".","/") + ".java");
        }
    }

    public static void main(String[] args) throws IOException {
        List<Class<?>> classes = getClasses("com.jiuqi.nr.definition.facade");
        String beanFilePath = "/Users/rsy/WorkSpace/release-2.4.0/nr-definition/nr.definition/src/main/java/";
        List<String> header = new ArrayList<>();
        header.add("返回类型");
        header.add("存取器");
        header.add("注释");
        List<SimpleSheetWrapper> simpleSheetWrappers = new ArrayList<>();
        for (Class<?> aClass : classes) {
            List<List> list2 = new ArrayList<>();
            if (aClass.isInterface() && !aClass.getSimpleName().contains("Design")) {
                DocVO docVO = DocUtil.execute(beanFilePath + aClass.getName().replace(".","/") + ".java");
                if (docVO == null)
                    continue;
                if (Objects.nonNull(docVO) && Objects.nonNull(docVO.getFieldVOList())) {
                    List<DocVO.FieldVO> fieldVOList = docVO.getFieldVOList();
                    for (DocVO.FieldVO fieldVO : fieldVOList) {
                        List<String> list = new ArrayList<>();
                        list.add("`"+fieldVO.getFieldType()+"`");
                        list.add("`"+fieldVO.getFieldName()+"`");
                        list.add(fieldVO.getDescribe());
                        list2.add(list);
                    }
                    SimpleSheetWrapper simpleSheetWrapper = new SimpleSheetWrapper();
                    simpleSheetWrapper.setHeader(header);
                    simpleSheetWrapper.setSheetName(aClass.getSimpleName());
                    simpleSheetWrapper.setData(list2);
                    simpleSheetWrappers.add(simpleSheetWrapper);

                }
            }
        }
        try (OutputStream fos = new FileOutputStream("/Users/rsy/WorkSpace/D.xlsx")) {
            ExcelUtils.getInstance().simpleSheet2Excel(simpleSheetWrappers, true, fos);
        } catch (Exception e) {

        }

    }

}