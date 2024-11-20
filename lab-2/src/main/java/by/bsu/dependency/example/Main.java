package by.bsu.dependency.example;

import by.bsu.dependency.beans.PrototypeBean;
import by.bsu.dependency.beans.SingletonBean;
import by.bsu.dependency.context.ApplicationContext;
import by.bsu.dependency.context.SimpleApplicationContext;

public class Main {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new SimpleApplicationContext(
                SingletonBean.class, PrototypeBean.class
        );
        applicationContext.start();

        SingletonBean singletonBean = (SingletonBean) applicationContext.getBean("singletonBean");
        PrototypeBean prototypeBean = (PrototypeBean) applicationContext.getBean("prototypeBean");

        singletonBean.doSomething();
        prototypeBean.doSomething();
        prototypeBean.callSingletonBean();
    }
}
