/**
 * (c) COPYRIGHT 2022 HST EQUIPAMENTOS
 * ELETRONICOS Ltda, Campinas (SP), Brasil
 * ALL RIGHTS RESERVED - TODOS OS DIREITOS RESERVADOS
 * CONFIDENTIAL, UNPUBLISHED PROPERTY OF HST E. E. Ltda
 * PROPRIEDADE CONFIDENCIAL NAO PUBLICADA DA HST Ltda.
 */

package br.com.hst.pdi.libraryapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.hst.pdi.libraryapi.api.dto.LoanFilterDTO;
import br.com.hst.pdi.libraryapi.api.exception.BusinessException;
import br.com.hst.pdi.libraryapi.api.model.entity.Book;
import br.com.hst.pdi.libraryapi.api.model.entity.Loan;
import br.com.hst.pdi.libraryapi.api.model.repository.LoanRepository;
import br.com.hst.pdi.libraryapi.service.impl.LoanServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

  LoanService service;

  @MockBean
  LoanRepository repository;

  @BeforeEach
  public void setUp() {
    this.service = new LoanServiceImpl(repository);
  }

  @Test
  @DisplayName("Deve salvar um empréstimo")
  public void saveLoanTest() {

    Book book = Book.builder().id(1L).build();
    String customer = "Fulano";

    var savingLoan = Loan.builder()
        .book(book)
        .customer(customer)
        .loanDate(LocalDate.now())
        .build();

    var savedLoan = Loan.builder()
        .id(1L)
        .loanDate(LocalDate.now())
        .customer(customer)
        .book(book)
        .build();

    when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
    when(repository.save(savingLoan)).thenReturn(savedLoan);

    var loan = service.save(savingLoan);
    assertThat(loan.getId()).isEqualTo(savedLoan.getId());
    assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
    assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
    assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
  }

  @Test
  @DisplayName("Deve lançar erro de negócio ao salvar um empréstimo com livro já emprestado")
  public void loanedBookSaveTest() {

    Book book = Book.builder().id(1L).build();
    String customer = "Fulano";

    var savingLoan = Loan.builder()
        .book(book)
        .customer(customer)
        .loanDate(LocalDate.now())
        .build();

    when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

    var exception = catchThrowable(() -> service.save(savingLoan));

    assertThat(exception).isInstanceOf(BusinessException.class)
        .hasMessage("Book already loaned");

    verify(repository, never()).save(savingLoan);

  }

  @Test
  @DisplayName("Deve obter as informações de um empréstimo pelo ID")
  public void getLoanDetailsTest() {

    // cenário
    Long id = 1L;

    var loan = createLoan();
    loan.setId(id);

    Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));

    // Execução
    var result = service.getById(id);

    // Verificação
    assertThat(result.isPresent()).isTrue();
    assertThat(result.get().getId()).isEqualTo(id);
    assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
    assertThat(result.get().getBook()).isEqualTo(loan.getBook());
    assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

    verify(repository).findById(id);

  }

  public static Loan createLoan() {
    Book book = Book.builder().id(1L).build();
    String customer = "Fulano";

    return Loan.builder()
        .book(book)
        .customer(customer)
        .loanDate(LocalDate.now())
        .build();
  }

  @Test
  @DisplayName("Deve atualizar o empréstimo")
  public void updateLoanTest() {
    Loan loan = createLoan();
    String customer = "Fulano";
    loan.setId(1L);
    loan.setReturned(true);

    when(repository.save(loan)).thenReturn(loan);

    var updatedLoan = service.update(loan);

    assertThat(updatedLoan.getReturned()).isTrue();
    verify(repository).save(loan);
  }

  @Test
  @DisplayName("Deve filtrar empréstimos pelas propriedades.")
  public void findLoanTest() {
    // cenário
    LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Fulano").isbn("321").build();
    Loan loan = createLoan();
    loan.setId(1L);

    PageRequest pageRequest = PageRequest.of(0, 10);

    List<Loan> lista = Arrays.asList(loan);

    Page<Loan> page = new PageImpl<Loan>(lista, pageRequest, lista.size());
    when(repository.findByBookIsbnOrCustomer(Mockito.anyString(), Mockito.anyString(), Mockito.any(PageRequest.class)))
        .thenReturn(page);

    // Execução
    Page<Loan> result = service.find(loanFilterDTO, pageRequest);

    // Verificação
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent()).isEqualTo(lista);
    assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
    assertThat(result.getPageable().getPageSize()).isEqualTo(10);

  }
}
