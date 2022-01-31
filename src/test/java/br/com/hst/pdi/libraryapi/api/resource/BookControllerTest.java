/**
 * (c) COPYRIGHT 2021 HST EQUIPAMENTOS
 * ELETRONICOS Ltda, Campinas (SP), Brasil
 * ALL RIGHTS RESERVED - TODOS OS DIREITOS RESERVADOS
 * CONFIDENTIAL, UNPUBLISHED PROPERTY OF HST E. E. Ltda
 * PROPRIEDADE CONFIDENCIAL NAO PUBLICADA DA HST Ltda.
 */

package br.com.hst.pdi.libraryapi.api.resource;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import br.com.hst.pdi.libraryapi.api.dto.BookDTO;
import br.com.hst.pdi.libraryapi.api.model.entity.Book;
import br.com.hst.pdi.libraryapi.exception.BusinessException;
import br.com.hst.pdi.libraryapi.service.BookService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

        static String BOOK_API = "/api/books";

        @Autowired
        MockMvc mvc;

        @MockBean
        BookService service;

        @Test
        @DisplayName("Deve criar um livro com sucesso.")
        public void createBookTest() throws Exception {

                BookDTO dto = createNewBook();

                Book savedBook = Book.builder().author("Autor")
                                .id(10L)
                                .title("As Aventuras")
                                .isbn("001")
                                .build();

                BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

                String json = new ObjectMapper().writeValueAsString(dto);

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .post(BOOK_API)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(json);

                mvc
                                .perform(request)
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("id").isNotEmpty())
                                .andExpect(jsonPath("title").value(dto.getTitle()))
                                .andExpect(jsonPath("author").value(dto.getAuthor()))
                                .andExpect(jsonPath("isbn").value(dto.getIsbn()));
        }

        @Test
        @DisplayName("Deve lançar erro de validação quando não houver dados suficientes para criação do livro.")
        public void createInvalidBookTest() throws Exception {
                String json = new ObjectMapper().writeValueAsString(new BookDTO());

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .post(BOOK_API)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(json);

                mvc
                                .perform(request)
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("errors", hasSize(3)));
        }

        @Test
        @DisplayName("Deve lançar erro ao tentar cadastrar um livro já utilizado por outro.")
        public void createBookWithDuplicatedIsbn() throws Exception {

                var dto = createNewBook();
                var json = new ObjectMapper().writeValueAsString(dto);

                String mensagemErro = "Isbn já cadastrado.";
                BDDMockito.given(service.save(Mockito.any(Book.class))).willThrow(new BusinessException(mensagemErro));

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .post(BOOK_API)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(json);

                mvc
                                .perform(request)
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("errors", hasSize(1)))
                                .andExpect(jsonPath("errors[0]").value(mensagemErro));

        }

        @Test
        @DisplayName("Deve obter informações de um livro.")
        public void getBookDetailsTest() throws Exception {
                // Cenário (given)
                Long id = 1L;

                Book book = Book.builder()
                                .id(id)
                                .title(createNewBook().getTitle())
                                .author(createNewBook().getAuthor())
                                .isbn(createNewBook().getIsbn())
                                .build();
                BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

                // execução (When)
                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .get(BOOK_API.concat("/" + id))
                                .accept(MediaType.APPLICATION_JSON);

                mvc.perform(request)
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("id").value(id))
                                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                                .andExpect(jsonPath("isbn").value(createNewBook().getIsbn()));
        }

        @Test
        @DisplayName("Deve retornar not found quando o livro procurado não existir")
        public void bookNotFoundTest() throws Exception {
                // cenário
                BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

                // execução
                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .get(BOOK_API.concat("/" + 1))
                                .accept(MediaType.APPLICATION_JSON);

                mvc.perform(request)
                                .andExpect(status().isNotFound());
        }

        private BookDTO createNewBook() {
                return BookDTO.builder()
                                .author("Autor")
                                .title("As Aventuras")
                                .isbn("001")
                                .build();
        }

        @Test
        @DisplayName("Deve deletar um livro.")
        public void deleteBookTest() throws Exception {

                BDDMockito.given(service.getById(anyLong())).willReturn(Optional.of(Book.builder().id(1L).build()));

                // execução
                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .delete(BOOK_API.concat("/" + 1))
                                .accept(MediaType.APPLICATION_JSON);

                mvc.perform(request)
                                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar resource not found quando não encontrar um livro pra deletar.")
        public void deleteInexistentBookTest() throws Exception {

                BDDMockito.given(service.getById(anyLong())).willReturn(Optional.empty());

                // execução
                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .delete(BOOK_API.concat("/" + 1))
                                .accept(MediaType.APPLICATION_JSON);

                mvc.perform(request)
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve atualizar livro")
        public void updateBookTest() throws Exception {
                Long id = 1L;
                String json = new ObjectMapper().writeValueAsString(createNewBook());
                Book updatingBook = Book.builder().id(1L).title("some title").author("some author").isbn("321").build();
                BDDMockito.given(service.getById(anyLong())).willReturn(Optional.of(updatingBook));
                Book updatedBook = Book.builder().id(id).author("Autor").title("As Aventuras").isbn("321").build();
                BDDMockito.given(service.update(updatingBook)).willReturn(updatedBook);

                // execução
                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .put(BOOK_API.concat("/" + 1))
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON);

                mvc.perform(request)
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("id").value(id))
                                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                                .andExpect(jsonPath("isbn").value("321"));
        }

        @Test
        @DisplayName("Deve retornar 404 ao tentar atualizar livro inexistente")
        public void updateInexistentBookTest() throws Exception {
                String json = new ObjectMapper().writeValueAsString(createNewBook());
                BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

                // execução
                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .put(BOOK_API.concat("/" + 1))
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON);

                mvc.perform(request)
                                .andExpect(status().isNotFound());

        }

        @Test
        @DisplayName("Deve filtrar livros")
        public void findBookTest() throws Exception {
                Long id = 1L;
                Book book = Book.builder()
                        .id(id)
                        .title(createNewBook().getTitle())
                        .author(createNewBook().getAuthor())
                        .isbn(createNewBook().getIsbn())
                        .build();

                BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                        .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1L));

                String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                        book.getTitle(),
                        book.getAuthor());

                var request = MockMvcRequestBuilders
                        .get(BOOK_API.concat(queryString))
                        .accept(MediaType.APPLICATION_JSON);
                
                mvc
                        .perform(request)
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("content", Matchers.hasSize(1)))
                        .andExpect(jsonPath("totalElements").value(1))
                        .andExpect(jsonPath("pageable.pageSize").value(100))
                        .andExpect(jsonPath("pageable.pageNumber").value(0))
                        ;
        }
}
