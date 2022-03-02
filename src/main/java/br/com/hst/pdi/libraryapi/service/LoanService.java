package br.com.hst.pdi.libraryapi.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.hst.pdi.libraryapi.api.dto.LoanFilterDTO;
import br.com.hst.pdi.libraryapi.api.model.entity.Loan;

public interface LoanService {

    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO filterDTO, Pageable pageable);

}
