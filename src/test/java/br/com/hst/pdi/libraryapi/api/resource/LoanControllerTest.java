/**
 * (c) COPYRIGHT 2022 HST EQUIPAMENTOS
 * ELETRONICOS Ltda, Campinas (SP), Brasil
 * ALL RIGHTS RESERVED - TODOS OS DIREITOS RESERVADOS
 * CONFIDENTIAL, UNPUBLISHED PROPERTY OF HST E. E. Ltda
 * PROPRIEDADE CONFIDENCIAL NAO PUBLICADA DA HST Ltda.
 */

package br.com.hst.pdi.libraryapi.api.resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import br.com.hst.pdi.libraryapi.api.dto.LoanDTO;
import br.com.hst.pdi.libraryapi.api.dto.LoanFilterDTO;
import br.com.hst.pdi.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.hst.pdi.libraryapi.api.exception.BusinessException;
import br.com.hst.pdi.libraryapi.api.model.entity.Book;
import br.com.hst.pdi.libraryapi.api.model.entity.Loan;
import br.com.hst.pdi.libraryapi.service.BookService;
import br.com.hst.pdi.libraryapi.service.LoanService;
import br.com.hst.pdi.libraryapi.service.LoanServiceTest;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(controllers = LoanController.class)
public class LoanControllerTest {

  static final String LOAN_API = "/api/loans";

  @Autowired
  MockMvc mvc;

  @MockBean
  private BookService bookService;

  @MockBean
  private LoanService loanService;

  @Test
  @DisplayName("Deve realizar um empréstimo")
  public void createLoanTest() throws Exception {

    var dto = LoanDTO.builder().isbn("123").email("customer@email.com").customer("Fulano").build();
    var json = new ObjectMapper().writeValueAsString(dto);

    var book = Book.builder().id(1L).isbn("123").build();
    BDDMockito.given(bookService.getBookByIsbn("123"))
        .willReturn(Optional.of(book));

    Loan loan = Loan.builder().id(1L).customer("Fulano").book(book).loanDate(LocalDate.now()).build();
    BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

    var request = post(LOAN_API)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json);

    mvc.perform(request)
        .andExpect(status().isCreated())
        // .andExpect(jsonPath("id").value(1L))
        .andExpect(content().string("1"));
  }

  @Test
  @DisplayName("Deve retornar erro ao tentar fazer empréstimo de um livro inexistente")
  public void invalidIsbnCreateLoanTest() throws Exception {

    var dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
    var json = new ObjectMapper().writeValueAsString(dto);

    BDDMockito.given(bookService.getBookByIsbn("123"))
        .willReturn(Optional.empty());

    var request = post(LOAN_API)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json);

    mvc.perform(request)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("errors", Matchers.hasSize(1)))
        .andExpect(jsonPath("errors[0]").value("Book not found for passed isbn"));
  }

  @Test
  @DisplayName("Deve retornar erro ao tentar fazer empréstimo de um livro emprestado")
  public void loanedBookErrorTest() throws Exception {

    var dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
    var json = new ObjectMapper().writeValueAsString(dto);

    var book = Book.builder().id(1L).isbn("123").build();
    BDDMockito.given(bookService.getBookByIsbn("123"))
        .willReturn(Optional.of(book));

    BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
        .willThrow(new BusinessException("Book already loaned"));

    var request = post(LOAN_API)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json);

    mvc.perform(request)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("errors", Matchers.hasSize(1)))
        .andExpect(jsonPath("errors[0]").value("Book already loaned"));
  }

  @Test
  @DisplayName("Deve retornar um livro")
  public void returnBookTest() throws Exception {
    // cenário { returned: true }
    var dto = ReturnedLoanDTO.builder().returned(true).build();
    var json = new ObjectMapper().writeValueAsString(dto);

    Loan loan = Loan.builder().id(1L).build();

    BDDMockito.given(loanService.getById(Mockito.anyLong()))
        .willReturn(Optional.of(loan));

    mvc.perform(
        MockMvcRequestBuilders.patch(LOAN_API.concat("/1"))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isOk());

    verify(loanService, times(1)).update(loan);

  }

  @Test
  @DisplayName("Deve retornar 404 quando tentar devolver um livro inexistente")
  public void returnInexistentBookTest() throws Exception {
    var dto = ReturnedLoanDTO.builder().returned(true).build();
    var json = new ObjectMapper().writeValueAsString(dto);

    BDDMockito.given(loanService.getById(Mockito.anyLong()))
        .willReturn(Optional.empty());

    mvc.perform(
        MockMvcRequestBuilders.patch(LOAN_API.concat("/1"))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Deve filtrar empréstimos")
  public void findLoansTest() throws Exception {
    // cenário
    var id = 1L;

    var loan = LoanServiceTest.createLoan();
    loan.setId(id);
    var book = Book.builder().id(1L).isbn("321").build();
    loan.setBook(book);

    BDDMockito.given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
        .willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 10), 1L));

    var queryString = String.format("?isbn=%s&customer=%s&page=0&size=10",
        book.getIsbn(),
        loan.getCustomer());
    var request = MockMvcRequestBuilders
        .get(LOAN_API.concat(queryString))
        .accept(MediaType.APPLICATION_JSON);
    mvc
        .perform(request)
        .andExpect(status().isOk())
        .andExpect(jsonPath("content", Matchers.hasSize(1)))
        .andExpect(jsonPath("totalElements").value(1))
        .andExpect(jsonPath("pageable.pageSize").value(10))
        .andExpect(jsonPath("pageable.pageNumber").value(0));

  }
}
