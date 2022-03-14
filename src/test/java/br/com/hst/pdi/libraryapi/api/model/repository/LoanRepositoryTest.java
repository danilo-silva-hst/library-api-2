/**
 * (c) COPYRIGHT 2022 HST EQUIPAMENTOS
 * ELETRONICOS Ltda, Campinas (SP), Brasil
 * ALL RIGHTS RESERVED - TODOS OS DIREITOS RESERVADOS
 * CONFIDENTIAL, UNPUBLISHED PROPERTY OF HST E. E. Ltda
 * PROPRIEDADE CONFIDENCIAL NAO PUBLICADA DA HST Ltda.
 */

package br.com.hst.pdi.libraryapi.api.model.repository;

import static br.com.hst.pdi.libraryapi.api.model.repository.BookRepositoryTest.createNewBook;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.hst.pdi.libraryapi.api.model.entity.Loan;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

  @Autowired
  private LoanRepository repository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  @DisplayName("deve verificar se existe empréstimo não devolvido para o livro")
  public void existsByBookAndNotReturnedTest() {

    // cenário
    Loan loan = createAndPersistLoan(LocalDate.now());
    var book = loan.getBook();

    // execução
    var exists = repository.existsByBookAndNotReturned(book);

    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Deve buscar empréstimo pelo isbn do livro ou customer")
  public void findByBookIsbnOrCustomerTest() {
    // cenário
    Loan loan = createAndPersistLoan(LocalDate.now());
    Page<Loan> result = repository.findByBookIsbnOrCustomer("123", "fulano", PageRequest.of(0, 10));

    //verificação
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent()).contains(loan);
    assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  private Loan createAndPersistLoan(LocalDate loanDate) {
    var book = createNewBook("123");
    entityManager.persist(book);

    var loan = Loan.builder()
        .book(book)
        .customer("Fulano")
        .loanDate(loanDate)
        .build();
    entityManager.persist(loan);

    return loan;
  }

  @Test
  @DisplayName("Deve obter empréstimos cuja data empréstimo seja menor ou igual a três dias atrás e não retornado")
  public void findByLoansDateLessThanAndNotReturnedTest(){
    var loan = createAndPersistLoan(LocalDate.now().minusDays(5));
    var result = repository.findByLoansDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

    assertThat(result).hasSize(1).contains(loan);
  }

  @Test
  @DisplayName("Deve retornar vazio quando não houverem empréstimos atrasados")
  public void notFindByLoansDateLessThanAndNotReturnedTest() {
    var loan = createAndPersistLoan(LocalDate.now());
    var result = repository.findByLoansDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

    assertThat(result).isEmpty();
  }

}
