package repository.user;
import model.Report;
import model.User;
import model.builder.ReportBuilder;
import model.builder.UserBuilder;
import model.validation.Notification;
import repository.security.RightsRolesRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static database.Constants.Tables.USER;
import static java.util.Collections.singletonList;

public class UserRepositoryMySQL implements UserRepository {

    private final Connection connection;
    private final RightsRolesRepository rightsRolesRepository;


    public UserRepositoryMySQL(Connection connection, RightsRolesRepository rightsRolesRepository) {
        this.connection = connection;
        this.rightsRolesRepository = rightsRolesRepository;
    }

    @Override
    public List<User> findAll() {

        List<User> users=new ArrayList<>();
        String sql="select user.id,username,password \n" +
                "from user,user_role,role\n" +
                "where user.id=user_role.user_id and user_role.role_id=role.id and role=\"employee\"";
        try{
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            ResultSet resultSet=preparedStatement.executeQuery();
            while(resultSet.next()) {
                users.add(getUserFromResultSet(resultSet));
            }

        }catch(SQLException e)
        {
            e.printStackTrace();
        }
        return users;
    }

    private User getUserFromResultSet(ResultSet resultSet) throws SQLException{

       return new UserBuilder().setUsername(resultSet.getString("username"))
                .setPassword(resultSet.getString("password"))
               .setId(resultSet.getLong("id"))
                .setRoles(rightsRolesRepository.findRolesForUser(resultSet.getLong("id")))
                .build();
    }

    // SQL Injection Attacks should not work after fixing functions
    // Be careful that the last character in sql injection payload is an empty space
    // alexandru.ghiurutan95@gmail.com' and 1=1; --
    // ' or username LIKE '%admin%'; --

    @Override
    public Notification<User> findByUsernameAndPassword(String username, String password) {

        Notification<User> findByUsernameAndPasswordNotification = new Notification<>();
        String fetchUserSql ="select * from user where username = ? and password = ?";
        try {


            PreparedStatement preparedStatement= connection.prepareStatement(fetchUserSql);

            preparedStatement.setString(1,username);
            preparedStatement.setString(2,password);

            ResultSet userResultSet=preparedStatement.executeQuery();


            if (userResultSet.next())
            {
                User user = getUserFromResultSet(userResultSet);
                findByUsernameAndPasswordNotification.setResult(user);
            } else {
                findByUsernameAndPasswordNotification.addError("Invalid username or password!");
                return findByUsernameAndPasswordNotification;
            }

        } catch (SQLException e) {
            System.out.println(e.toString());
            findByUsernameAndPasswordNotification.addError("Something is wrong with the Database!");
        }

        return findByUsernameAndPasswordNotification;
    }

    @Override
    public boolean save(User user) {
        try {
            PreparedStatement insertUserStatement = connection
                    .prepareStatement("INSERT INTO user values (null, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            insertUserStatement.setString(1, user.getUsername());
            insertUserStatement.setString(2, user.getPassword());
            insertUserStatement.executeUpdate();

            ResultSet rs = insertUserStatement.getGeneratedKeys();
            rs.next();
            long userId = rs.getLong(1);
            user.setId(userId);

            rightsRolesRepository.addRolesToUser(user, user.getRoles());

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public void removeAll() {
        try {
            Statement statement = connection.createStatement();
            String sql = "DELETE from user where id >= 0";
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean existsByUsername(String email) {

        String fetchUserSql ="Select * from user where username = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(fetchUserSql);
            preparedStatement.setString(1,email);
            ResultSet userResultSet = preparedStatement.executeQuery();
            return userResultSet.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int addEmployee(User user) {
        try {
            PreparedStatement insertUserStatement = connection
                    .prepareStatement("INSERT INTO user values (null, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            insertUserStatement.setString(1, user.getUsername());
            insertUserStatement.setString(2, user.getPassword());
            insertUserStatement.executeUpdate();

            ResultSet rs = insertUserStatement.getGeneratedKeys();
            rs.next();
            long userId = rs.getLong(1);
            user.setId(userId);

            rightsRolesRepository.addRolesToUser(user, user.getRoles());

            return (int) userId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public Notification<Report> generateReport(User user) {
       String sql="SELECT sum(stock)as carti_vandute,user_id,username,sum(price) as pret_total\n" +
               "FROM library.order,user\n" +
               "where user_id=user.id \n" +
               "and MONTH(order_date) = MONTH(CURDATE()) AND YEAR(order_date) = YEAR(CURDATE()) and user_id= ? \n" +
               "group by user_id";
       Notification<Report> notificationReport=new Notification<>();
       try
       {

           PreparedStatement preparedStatement=connection.prepareStatement(sql);
           preparedStatement.setLong(1,user.getId());
           ResultSet resultSet=preparedStatement.executeQuery();
           if(resultSet.next())
           {
               Report report = new ReportBuilder().setId(resultSet.getLong("user_id"))
                       .setStock(resultSet.getInt("carti_vandute"))
                       .setPrice(resultSet.getInt("pret_total"))
                       .setUsername(resultSet.getString("username"))
                       .build();
               notificationReport.setResult(report);
           }
           else
           {
               notificationReport.addError("The employee does not have any orders history");
                return notificationReport;
           }
       }
       catch(SQLException e)
       {
           e.printStackTrace();
           notificationReport.addError("Something is wrong with the database! Please try again.");

       }
        return notificationReport;
    }

}