package by.bsu.dependency.context;

import by.bsu.dependency.annotation.Bean;
import by.bsu.dependency.annotation.BeanScope;
import by.bsu.dependency.annotation.Inject;
import by.bsu.dependency.annotation.PostConstruct;
import by.bsu.dependency.exceptions.ApplicationContextNotStartedException;
import by.bsu.dependency.exceptions.NoSuchBeanDefinitionException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleApplicationContext extends AbstractApplicationContext {

    protected static class BeanDefinition {
        String name;
        BeanScope scope;
        Class<?> clazz;

        public BeanDefinition(Class<?> clazz) {
            name = SimpleApplicationContext.ExtractBeanName(clazz);
            this.clazz = clazz;
            if (clazz.isAnnotationPresent(Bean.class)) {
                scope = clazz.getAnnotation(Bean.class).scope();
            } else {
                scope = BeanScope.SINGLETON;
            }
        }
    }

    protected Map<String, BeanDefinition> beanDefinitions;
    protected Map<String, Object> beans = new HashMap<>();
    ContextStatus status = ContextStatus.NOT_STARTED;

    /**
     * Создает контекст, содержащий классы, переданные в параметре.
     * <br/>
     * Если на классе нет аннотации {@code @Bean}, имя бина получается из названия класса, скоуп бина по дефолту
     * считается {@code Singleton}.
     * <br/>
     * Подразумевается, что у всех классов, переданных в списке, есть конструктор без аргументов.
     *
     * @param beanClasses классы, из которых требуется создать бины
     */
    public SimpleApplicationContext(Class<?>... beanClasses) {
        this(Arrays.stream(beanClasses));
    }

    public SimpleApplicationContext(Stream<Class<?>> beanClasses) {
        beanDefinitions = beanClasses.collect(
                Collectors.toMap(
                        SimpleApplicationContext::ExtractBeanName,
                        BeanDefinition::new
                )
        );
    }

    /**
     * Помимо прочего, метод должен заниматься внедрением зависимостей в создаваемые объекты
     */
    @Override
    public void start() {
        beanDefinitions.forEach((beanName, beanDefinition) -> {
            if (beanDefinition.scope == BeanScope.SINGLETON) {
                beans.put(beanName, createBeanInstance(beanDefinition.clazz));
            }
        });
        status = ContextStatus.STARTED;
    }

    @Override
    public boolean isRunning() {
        return status == ContextStatus.STARTED;
    }

    @Override
    public boolean containsBean(String name) {
        if (status == ContextStatus.NOT_STARTED) {
            throw new ApplicationContextNotStartedException();
        }
        return beanDefinitions.containsKey(name);
    }

    @Override
    public Object getBean(String name) {
        if (status == ContextStatus.NOT_STARTED) {
            throw new ApplicationContextNotStartedException();
        }
        if (!beanDefinitions.containsKey(name)) {
            throw new NoSuchBeanDefinitionException(name);
        }
        BeanDefinition beanDefinition = beanDefinitions.get(name);
        if (beanDefinition.scope == BeanScope.SINGLETON) {
            return beans.get(name);
        }
        return instantiateBean(beanDefinition);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(Class<T> clazz) {
        String name = ExtractBeanName(clazz);
        Object bean = getBean(name);
        return (T) bean;
    }

    @Override
    public boolean isPrototype(String name) {
        if (!beanDefinitions.containsKey(name)) {
            throw new NoSuchBeanDefinitionException(name);
        }
        return beanDefinitions.get(name).scope == BeanScope.PROTOTYPE;
    }

    @Override
    public boolean isSingleton(String name) {
        if (!beanDefinitions.containsKey(name)) {
            throw new NoSuchBeanDefinitionException(name);
        }
        return beanDefinitions.get(name).scope == BeanScope.SINGLETON;
    }

    private Object instantiateBean(BeanDefinition beanDefinition) {
        Object instance = createBeanInstance(beanDefinition.clazz);
        if (beanDefinition.scope == BeanScope.PROTOTYPE) {
            injectDependencies(instance);
        }
        callInitMethods(instance);
        return instance;
    }

    private <T> void injectDependencies(T instance) {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }
            field.setAccessible(true);
            try {
                field.set(instance, getBean(field.getType()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("shit happens...");
            }
        }
    }

    private <T> void callInitMethods(T instance) {
        Method[] methods = instance.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (!method.isAnnotationPresent(PostConstruct.class)) {
                continue;
            }
            method.setAccessible(true);
            try {
                method.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("shit happens...");
            }

        }
    }

    private <T> T createBeanInstance(Class<T> beanClass) {
        try {
            return beanClass.getConstructor().newInstance();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 InstantiationException e) {
            throw new RuntimeException("shit happens...");
        }
    }


    public static String ExtractBeanName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Bean.class) && !clazz.getAnnotation(Bean.class).name().isEmpty()) {
            return clazz.getAnnotation(Bean.class).name();
        }
        String name = clazz.getSimpleName();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
