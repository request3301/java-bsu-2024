package by.bsu.dependency.exceptions;

public class NoSuchBeanDefinitionException extends RuntimeException {
    public NoSuchBeanDefinitionException(String name) {
        super(String.format("Bean with definition '%s' does not exist in context.", name));
    }
}
