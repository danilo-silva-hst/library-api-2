package br.com.hst.pdi.libraryapi.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.hst.pdi.libraryapi.api.model.entity.Book;
import br.com.hst.pdi.libraryapi.api.model.repository.BookRepository;
import br.com.hst.pdi.libraryapi.exception.BusinessException;
import br.com.hst.pdi.libraryapi.service.BookService;

@Service
public class BookServiceImpl implements BookService {

    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if(repository.existsByIsbn(book.getIsbn())){
            throw new BusinessException("Isbn já cadastrado.");
        }
        return repository.save(book);
    }

    @Override
    public Optional<Book> getById(Long id) {
        return Optional.empty();
    }

    @Override
    public void delete(Book book) {
        // TODO Auto-generated method stub
    }

    @Override
    public Book update(Book book){
        return null;
    }
}
