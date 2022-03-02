package br.com.hst.pdi.libraryapi.api.resource;

import java.time.LocalDate;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.hst.pdi.libraryapi.api.dto.BookDTO;
import br.com.hst.pdi.libraryapi.api.dto.LoanDTO;
import br.com.hst.pdi.libraryapi.api.dto.LoanFilterDTO;
import br.com.hst.pdi.libraryapi.api.dto.ReturnedLoanDTO;
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
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO dto) {
        var book = bookService
                .getBookByIsbn(dto.getIsbn())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));
        var entity = Loan.builder()
                .book(book)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        entity = service.save(entity);

        return entity.getId();
    }

    @PatchMapping("{id}")
    public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto) {
        var loan = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(dto.getReturned());
        service.update(loan);
    }

    @GetMapping
    public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageRequest) {
        var result = service.find(dto, pageRequest);
        var loans = result
                .getContent()
                .stream()
                .map(entity -> {
                    var book = entity.getBook();
                    var bookDTO = modelMapper.map(book, BookDTO.class);
                    var loanDTO = modelMapper.map(entity, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());

        return new PageImpl<LoanDTO>(loans, pageRequest, result.getTotalElements());
    }
}
