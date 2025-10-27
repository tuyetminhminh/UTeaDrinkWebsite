package net.codejava.utea.auth.service;

public interface EmailService {
    void send(String to, String subject, String html);
}
