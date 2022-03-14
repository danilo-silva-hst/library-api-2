/**
 * (c) COPYRIGHT 2022 HST EQUIPAMENTOS
 * ELETRONICOS Ltda, Campinas (SP), Brasil
 * ALL RIGHTS RESERVED - TODOS OS DIREITOS RESERVADOS
 * CONFIDENTIAL, UNPUBLISHED PROPERTY OF HST E. E. Ltda
 * PROPRIEDADE CONFIDENCIAL NAO PUBLICADA DA HST Ltda.
 */

package br.com.hst.pdi.libraryapi.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import br.com.hst.pdi.libraryapi.service.EmailService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  @Value("${application.mail.default-remetent}")
  private String remetent;

  private final JavaMailSender javaMailSender;

  @Override
  public void sendMails(String message, List<String> mailsList) {
    String[] mails = mailsList.toArray(new String[mailsList.size()]);

    var mailMessage = new SimpleMailMessage();
    mailMessage.setFrom(remetent);
    mailMessage.setSubject("Livro com empréstimo atrasado");
    mailMessage.setText(message);
    mailMessage.setTo(mails);
    javaMailSender.send(mailMessage);
  }

}
