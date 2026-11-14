package ua.knu.jobmatch.service;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String what) {
        super(what);
    }
}
