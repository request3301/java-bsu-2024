package by.bsu.dependency.context;

import by.bsu.dependency.beans.*;
import by.bsu.dependency.exceptions.ApplicationContextNotStartedException;
import by.bsu.dependency.exceptions.NoSuchBeanDefinitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class SimpleApplicationContextTest {

    private ApplicationContext applicationContext;

    @BeforeEach
    void init() {
        applicationContext = new SimpleApplicationContext(
                SingletonBean.class,
                ImplicitSingletonBean.class,
                NonameBean.class,
                UnbeanedBean.class,
                PrototypeBean.class,
                StrangeNameBean.class
        );
    }

    @Test
    void testExtractBeanName() {
        assertThat(SimpleApplicationContext.ExtractBeanName(SingletonBean.class)).isEqualTo("singletonBean");
        assertThat(SimpleApplicationContext.ExtractBeanName(UnbeanedBean.class)).isEqualTo("unbeanedBean");
        assertThat(SimpleApplicationContext.ExtractBeanName(StrangeNameBean.class)).isEqualTo("veryStrange");
        assertThat(SimpleApplicationContext.ExtractBeanName(NonameBean.class)).isEqualTo("nonameBean");
    }

    @Test
    void testIsRunning() {
        assertThat(applicationContext.isRunning()).isFalse();
        applicationContext.start();
        assertThat(applicationContext.isRunning()).isTrue();
    }

    @Test
    void testContextContainsNotStarted() {
        assertThrows(
                ApplicationContextNotStartedException.class,
                () -> applicationContext.containsBean("singletonBean")
        );
    }

    @Test
    void testContextContainsBeans() {
        applicationContext.start();

        assertThat(applicationContext.containsBean("singletonBean")).isTrue();
        assertThat(applicationContext.containsBean("prototypeBean")).isTrue();
        assertThat(applicationContext.containsBean("unbeanedBean")).isTrue();
        assertThat(applicationContext.containsBean("nonameBean")).isTrue();
        assertThat(applicationContext.containsBean("veryStrange")).isTrue();
        assertThat(applicationContext.containsBean("randomName")).isFalse();
    }

    @Test
    void testContextGetBeanNotStarted() {
        assertThrows(
                ApplicationContextNotStartedException.class,
                () -> applicationContext.getBean("singletonBean")
        );
    }

    @Test
    void testGetBeanNotFound() {
        applicationContext.start();

        assertThrows(
                NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean("randomName")
        );
    }

    @Test
    void testGetBeanSingleton() {
        applicationContext.start();

        Object instance_first = applicationContext.getBean("singletonBean");
        Object instance_second = applicationContext.getBean("singletonBean");
        assertThat(instance_first).isSameAs(instance_second);
    }

    @Test
    void testGetBeanStrangeName() {
        applicationContext.start();

        Object instance_first = applicationContext.getBean("veryStrange");
        assertThat(instance_first).isNotNull();
    }

    @Test
    void testGetBeanPrototype() {
        applicationContext.start();

        Object instance_first = applicationContext.getBean("prototypeBean");
        Object instance_second = applicationContext.getBean("prototypeBean");
        assertThat(instance_first).isNotSameAs(instance_second);
    }

    @Test
    void testGetBeanByClass() {
        applicationContext.start();

        SingletonBean instance_first = applicationContext.getBean(SingletonBean.class);
        Object instance_second = applicationContext.getBean("singletonBean");
        assertThat(instance_first).isSameAs(instance_second);
    }

    @Test
    void testIsSingletonReturns() {
        assertThat(applicationContext.isSingleton("singletonBean")).isTrue();
        assertThat(applicationContext.isSingleton("implicitSingletonBean")).isTrue();
        assertThat(applicationContext.isSingleton("prototypeBean")).isFalse();
    }

    @Test
    void testIsSingletonThrows() {
        assertThrows(
                NoSuchBeanDefinitionException.class,
                () -> applicationContext.isSingleton("randomName")
        );
    }

    @Test
    void testIsPrototypeReturns() {
        assertThat(applicationContext.isPrototype("singletonBean")).isFalse();
        assertThat(applicationContext.isPrototype("implicitSingletonBean")).isFalse();
        assertThat(applicationContext.isPrototype("prototypeBean")).isTrue();
    }

    @Test
    void testIsPrototypeThrows() {
        assertThrows(
                NoSuchBeanDefinitionException.class,
                () -> applicationContext.isPrototype("randomName")
        );
    }

    @Test
    void testInjection() {
        applicationContext.start();

        PrototypeBean prototypeBean = applicationContext.getBean(PrototypeBean.class);
        prototypeBean.callSingletonBean();
    }

    @Test
    void testInit() {
        applicationContext.start();

        PrototypeBean prototypeBean = applicationContext.getBean(PrototypeBean.class);
        assertThat(prototypeBean.getValue() == 42).isTrue();
    }
}
