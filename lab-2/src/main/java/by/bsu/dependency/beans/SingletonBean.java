package by.bsu.dependency.beans;

import by.bsu.dependency.annotation.Bean;
import by.bsu.dependency.annotation.BeanScope;

@Bean(name = "singletonBean", scope = BeanScope.SINGLETON)
public class SingletonBean {
    public int getValue() {
        return 42;
    }

    public void doSomething() {
        System.out.println("singleton bean did something.");
    }
}