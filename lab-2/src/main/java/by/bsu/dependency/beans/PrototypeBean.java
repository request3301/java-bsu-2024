package by.bsu.dependency.beans;

import by.bsu.dependency.annotation.Bean;
import by.bsu.dependency.annotation.BeanScope;
import by.bsu.dependency.annotation.Inject;
import by.bsu.dependency.annotation.PostConstruct;

@Bean(name = "prototypeBean", scope = BeanScope.PROTOTYPE)
public class PrototypeBean {
    @Inject
    private SingletonBean singletonBean;

    private int value;

    @PostConstruct
    private void init() {
        value = singletonBean.getValue();
    }

    public void callSingletonBean() {
        System.out.println("prototype bean called singleton bean");
        singletonBean.doSomething();
    }

    public int getValue() {
        return value;
    }

    public void doSomething() {
        System.out.println("prototype bean did something. it has value " + getValue());
    }
}
