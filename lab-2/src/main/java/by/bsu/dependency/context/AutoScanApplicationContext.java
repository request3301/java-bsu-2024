package by.bsu.dependency.context;

import by.bsu.dependency.annotation.Bean;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AutoScanApplicationContext extends SimpleApplicationContext {

    /**
     * Создает контекст, содержащий классы из пакета {@code packageName}, помеченные аннотацией {@code @Bean}.
     * <br/>
     * Если имя бина в анноации не указано ({@code name} пустой), оно берется из названия класса.
     * <br/>
     * Подразумевается, что у всех классов, переданных в списке, есть конструктор без аргументов.
     *
     * @param packageName имя сканируемого пакета
     */
    public AutoScanApplicationContext(String packageName) {
        super(getBeanClasses(packageName));
    }

    private static Stream<Class<?>> getBeanClasses(String packageName) {
        List<Class<?>> classes = getClassesInPackage(packageName);
        return classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Bean.class));
    }

    public static List<Class<?>> getClassesInPackage(String packageName) {
        // library did not work lol
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');

        try {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
            if (resource != null) {
                File directory = new File(resource.toURI());
                if (directory.exists()) {
                    File[] files = directory.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.getName().endsWith(".class")) {
                                String className = file.getName().substring(0, file.getName().length() - 6);
                                classes.add(Class.forName(packageName + '.' + className));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("shit happens...");
        }

        return classes;
    }
}
