package br.com.hst.pdi.libraryapi.api.resource;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.hst.pdi.libraryapi.api.dto.LoanDTO;
import br.com.hst.pdi.libraryapi.api.model.entity.Loan;
import br.com.hst.pdi.libraryapi.service.BookService;
import br.com.hst.pdi.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService service;
    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO dto) {
        var book = bookService
                    .getBookByIsbn(dto.getIsbn())
                    .orElseThrow( () -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Book not found for passed isbn"));
        var entity = Loan.builder()
                        .book(book)
                        .customer(dto.getCustomer())
                        .loanDate(LocalDate.now())
                        .build();

        entity = service.save(entity);
        
        return entity.getId();
    }
}