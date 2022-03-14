package br.com.hst.pdi.libraryapi;

import java.util.Arrays;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import br.com.hst.pdi.libraryapi.service.EmailService;

@SpringBootApplication
@EnableScheduling
public class LibraryApiApplication {

  @Autowired
  private EmailService emailService;

  @Bean
  public CommandLineRunner runner(){
    return args -> {
      List<String> emails = Arrays.asList("d6ae7f43b1-763591@inbox.mailtrap.io");
      emailService.sendMails("Testando serviço de emails.", emails);
      System.out.println("EMAILS ENVIADOS");
    };
  }

  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }

  public static void main(String[] args) {
    SpringApplication.run(LibraryApiApplication.class, args);
  }

}
