package com.hrtrack.service;


import com.hrtrack.exception.EmailingException;
import jakarta.mail.MessagingException;
import org.thymeleaf.context.Context;

public interface EmailService {
    public void sendMail(String to, String subject, String template, Context context) throws EmailingException, MessagingException;
}
