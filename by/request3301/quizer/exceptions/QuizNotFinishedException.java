package by.request3301.quizer.exceptions;

public class QuizNotFinishedException extends RuntimeException {
    public QuizNotFinishedException(String message) {
        super(message);
    }
}