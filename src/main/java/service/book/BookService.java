package service.book;

import model.Book;

import java.util.List;
import java.util.Optional;

public interface BookService {

    List<Book> findAll();

    Book findById(Long id);

    int save(Book book);
    boolean delete(Book book);
    boolean sell(Book book);

    int getAgeOfBook(Long id);
}
