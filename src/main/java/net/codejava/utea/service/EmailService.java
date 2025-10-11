package net.codejava.utea.service;

public interface EmailService {
    void send(String to, String subject, String html);
}
