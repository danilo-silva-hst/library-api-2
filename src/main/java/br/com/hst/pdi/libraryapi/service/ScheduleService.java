/**
 * (c) COPYRIGHT 2022 HST EQUIPAMENTOS
 * ELETRONICOS Ltda, Campinas (SP), Brasil
 * ALL RIGHTS RESERVED - TODOS OS DIREITOS RESERVADOS
 * CONFIDENTIAL, UNPUBLISHED PROPERTY OF HST E. E. Ltda
 * PROPRIEDADE CONFIDENCIAL NAO PUBLICADA DA HST Ltda.
 */

package br.com.hst.pdi.libraryapi.service;

import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

  private final String CRON_LATE_LOANS = "0 0 0 1/1 * *";

  @Value("${application.mail.lateloans.message}")
  private String message;
  private final LoanService loanService;
  private final EmailService emailService;

  @Scheduled(cron = CRON_LATE_LOANS)
  public void sendMailToLateLoans() {
    var allLateLoans = loanService.getAllLateLoans();
    var mailsList = allLateLoans.stream().map( loan -> loan.getCustomerEmail() ).collect(Collectors.toList());

    emailService.sendMails(message, mailsList);
  }
}
