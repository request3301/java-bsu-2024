package by.bsu.dependency.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class AutoScanApplicationContextTest {

    private ApplicationContext applicationContext;

    @BeforeEach
    void init() {
        applicationContext = new AutoScanApplicationContext("by.bsu.dependency.beans");
        applicationContext.start();
    }

    @Test
    void testFindsBeans() {
        assertThat(applicationContext.containsBean("singletonBean")).isTrue();
    }

    @Test
    void testIgnoresNonBeans() {
        assertThat(applicationContext.containsBean("unbeanedBean")).isFalse();
    }
}
