package br.com.hst.pdi.libraryapi.service.impl;

import java.util.Optional;

import br.com.hst.pdi.libraryapi.api.model.entity.Loan;
import br.com.hst.pdi.libraryapi.api.model.repository.LoanRepository;
import br.com.hst.pdi.libraryapi.exception.BusinessException;
import br.com.hst.pdi.libraryapi.service.LoanService;

public class LoanServiceImpl implements LoanService{


    private LoanRepository repository;

    public LoanServiceImpl(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan) {
        if(repository.existsByBookAndNotReturned(loan.getBook())){
            throw new BusinessException("Book already loaned");
        }
        return repository.save(loan);
    }

    @Override
    public Optional<Loan> getById(Long id) {
        return Optional.empty();
    }

    @Override
    public Loan update(Loan loan) {
        // TODO Auto-generated method stub
        return null;
    }

}
