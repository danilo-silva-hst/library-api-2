package br.com.hst.pdi.libraryapi.service;

import java.util.Optional;

import br.com.hst.pdi.libraryapi.api.model.entity.Loan;

public interface LoanService {

    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

}
