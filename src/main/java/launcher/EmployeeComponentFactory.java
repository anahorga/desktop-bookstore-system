package launcher;

import controller.BookController;
import database.DatabaseConnectionFactory;
import javafx.stage.Stage;
import mapper.BookMapper;
import repository.book.BookRepository;
import repository.book.BookRepositoryCacheDecorator;
import repository.book.BookRepositoryMySQL;
import repository.book.Cache;
import repository.order.OrderRepository;
import repository.order.OrderRepositoryMySQL;
import service.book.BookService;
import service.book.BookServiceImpl;
import service.order.OrderService;
import service.order.OrderServiceImpl;
import view.BookView;
import view.model.BookDTO;

import java.sql.Connection;
import java.util.List;

public class EmployeeComponentFactory {

    private final BookView bookView;
    private final BookController bookController;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private static volatile EmployeeComponentFactory instance;
    private static Stage stage;
    private static Boolean componentsForTest;


    public static EmployeeComponentFactory getInstance(Boolean aComponentsForTest, Stage aPrimaryStage){

        if (instance == null) {
            synchronized(EmployeeComponentFactory.class) {
                if (instance == null) {
                    stage=aPrimaryStage;
                    componentsForTest=aComponentsForTest;
                    instance = new EmployeeComponentFactory(componentsForTest, stage);
                }
            }
        }

        return instance;
    }

    private EmployeeComponentFactory(Boolean componentsForTest, Stage primaryStage)
    {
        Connection connection= DatabaseConnectionFactory.getConnectionWrapper(componentsForTest).getConnection();
        this.bookRepository= new BookRepositoryCacheDecorator(new BookRepositoryMySQL(connection), new Cache<>());
        this.bookService=new BookServiceImpl(bookRepository);
        List<BookDTO> bookDTOs= BookMapper.convertBookListToBookDTOList(bookService.findAll());
        this.bookView=new BookView(primaryStage,bookDTOs);

        this.orderRepository=new OrderRepositoryMySQL(connection);
        this.orderService=new OrderServiceImpl(orderRepository);
        this.bookController=new BookController(bookView,bookService,orderService);

    }

    public BookView getBookView() {
        return bookView;
    }

    public BookController getBookController() {
        return bookController;
    }

    public BookRepository getBookRepository() {
        return bookRepository;
    }

    public BookService getBookService() {
        return bookService;
    }

    public static Stage getStage() {
        return stage;
    }

    public static Boolean getComponentsForTest() {
        return componentsForTest;
    }
}
