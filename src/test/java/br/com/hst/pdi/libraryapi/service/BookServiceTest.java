/**
 * (c) COPYRIGHT 2021 HST EQUIPAMENTOS
 * ELETRONICOS Ltda, Campinas (SP), Brasil
 * ALL RIGHTS RESERVED - TODOS OS DIREITOS RESERVADOS
 * CONFIDENTIAL, UNPUBLISHED PROPERTY OF HST E. E. Ltda
 * PROPRIEDADE CONFIDENCIAL NAO PUBLICADA DA HST Ltda.
 */

package br.com.hst.pdi.libraryapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.hst.pdi.libraryapi.api.model.entity.Book;
import br.com.hst.pdi.libraryapi.api.model.repository.BookRepository;
import br.com.hst.pdi.libraryapi.exception.BusinessException;
import br.com.hst.pdi.libraryapi.service.impl.BookServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro.")
    public void saveBookTest() {
        // Cenário
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when(repository.save(book)).thenReturn(
                Book.builder()
                        .id(1L)
                        .isbn(book.getIsbn())
                        .author(book.getAuthor())
                        .title(book.getTitle())
                        .build());

        // Execução
        Book savedBook = service.save(book);

        // Verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo(book.getIsbn());
        assertThat(savedBook.getTitle()).isEqualTo(book.getTitle());
        assertThat(savedBook.getAuthor()).isEqualTo(book.getAuthor());
    }

    private Book createValidBook() {
        return Book.builder().isbn("123").author("Fulano").title("As Aventuras").build();
    }

    @Test
    @DisplayName("Deve lançar erro de negocio ao tentar salvar um livro com isbn duplicado.")
    public void shouldNotSaveABookWithDuplicatedISBN() {
        // cenário
        var book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        // execução
        var exception = Assertions.catchThrowable(() -> service.save(book));

        //Verificações
        assertThat(exception)
            .isInstanceOf(BusinessException.class)
            .hasMessage("Isbn já cadastrado.");
        
        Mockito.verify(repository, Mockito.never()).save(book);
    }
}
