/**
 * (c) COPYRIGHT 2022 HST EQUIPAMENTOS
 * ELETRONICOS Ltda, Campinas (SP), Brasil
 * ALL RIGHTS RESERVED - TODOS OS DIREITOS RESERVADOS
 * CONFIDENTIAL, UNPUBLISHED PROPERTY OF HST E. E. Ltda
 * PROPRIEDADE CONFIDENCIAL NAO PUBLICADA DA HST Ltda.
 */

package br.com.hst.pdi.libraryapi.api.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.hst.pdi.libraryapi.api.model.entity.Book;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com isbn informado")
    public void returnTrueWhenIsbnExists() {
        // Cenário
        String isbn = "123";
        Book book = Book.builder().title("Aventuras").author("Fulano").isbn(isbn).build();
        entityManager.persist(book);

        // Execução
        var exists = repository.existsByIsbn(isbn);

        // Validação
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com isbn informado")
    public void returnFalseWhenIsbnDoesntExists() {
        // Cenário
        String isbn = "123";

        // Execução
        var exists = repository.existsByIsbn(isbn);

        // Validação
        assertThat(exists).isFalse();
    }
    
}
