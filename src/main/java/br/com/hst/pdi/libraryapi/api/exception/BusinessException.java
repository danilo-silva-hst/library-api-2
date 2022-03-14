/**
 * (c) COPYRIGHT 2022 HST EQUIPAMENTOS
 * ELETRONICOS Ltda, Campinas (SP), Brasil
 * ALL RIGHTS RESERVED - TODOS OS DIREITOS RESERVADOS
 * CONFIDENTIAL, UNPUBLISHED PROPERTY OF HST E. E. Ltda
 * PROPRIEDADE CONFIDENCIAL NAO PUBLICADA DA HST Ltda.
 */

package br.com.hst.pdi.libraryapi.api.exception;

public class BusinessException extends RuntimeException {
  public BusinessException(String s) {
    super(s);
  }
}
