import java.sql.*;
import java.util.*;


public class BankingSystem {
    public static void main(String[] args) throws SQLException {
        Connection connection = null;
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        boolean done = false;
        boolean loggedIn = false;
        int input;
        int customer_id = 0;

        Account user = new Account();
        String sql = "";
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank?user=root&password=PASSWORDHERE");  // Connect to database
            Statement statement = connection.createStatement();
            while(!done) {
                if(!loggedIn) {
                    printNotLoggedInOptions();
                    input = scanner.nextInt();
                    switch (input) {
                        // Create an account
                        case 1: {
                            // Ask user for their name
                            System.out.println("What is your first name?");
                            String firstName = scanner.next();
                            System.out.println("What is your last name?");
                            String lastName = scanner.next();

                            // Create their card information
                            user.createCardNumber();
                            user.createPin();

                            // Tell user card information
                            System.out.println("Your card number: ");
                            System.out.println(user.getCardNumber());
                            System.out.println("Your PIN is: ");
                            System.out.println(user.getPin());

                            // Adding account into data base
                            // Get highest ID
                            String idsql = "SELECT MAX(customer_id) as customer_id FROM customer;";
                            ResultSet idResult = statement.executeQuery(idsql);
                            int highestID = 0;
                            if(idResult.next()) {
                                highestID = idResult.getInt("customer_id");
                            }
                            // Prepared statement to send data to database
                            sql = "INSERT INTO customer(customer_id, first_name, last_name, card_number, pin, logged_in, balance) VALUES( ? , ? , ? , ? , ? , false, ?)";
                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setInt(1, highestID + 1);
                            preparedStatement.setString(2, firstName);
                            preparedStatement.setString(3, lastName);
                            preparedStatement.setString(4, user.getCardNumber());
                            preparedStatement.setString(5, user.getPin());
                            preparedStatement.setInt(6, random.nextInt(50000));
                            preparedStatement.execute();

                            break;
                        }
                        // Log into account
                        case 2: {
                            System.out.println("Enter your card number: ");
                            String cardNumberInput = scanner.next();
                            System.out.println("Enter your pin: ");
                            String pinInput = scanner.next();

                            String logInSQL = "SELECT customer_id FROM customer WHERE card_number = ? AND pin = ?";
                            PreparedStatement preparedStatement = connection.prepareStatement(logInSQL);
                            preparedStatement.setString(1 , cardNumberInput);
                            preparedStatement.setString(2 ,pinInput);
                            ResultSet resultSet = preparedStatement.executeQuery();
                            if(resultSet.next()) {
                                customer_id = resultSet.getInt("customer_id");
                                loggedIn = true;
                                System.out.println("You have successfully logged in!");
                            } else {
                                System.out.println("Wrong card number or pin!");
                            }

                            break;
                        }
                        // Exit
                        case 0: {
                            System.out.println("Bye!");
                            done = true;
                            break;
                        }
                    }
                } else { // LOGGED IN
                    printLoggedInOptions();
                    input = scanner.nextInt();
                    switch (input) {
                        // Balance
                        case 1: {
                            String balanceSQL = "SELECT balance FROM customer WHERE customer_id = ?";
                            PreparedStatement preparedStatement = connection.prepareStatement(balanceSQL);
                            preparedStatement.setInt(1, customer_id);
                            ResultSet resultSet = preparedStatement.executeQuery();
                            if(resultSet.next()) {
                                System.out.println("Your balance is: " + resultSet.getInt("balance"));
                            } else {
                                System.out.println("Error");
                            }
                            break;
                        }
                        // Deposit
                        case 2: {
                            System.out.println("How much money do you want to deposit? ");
                            float deposit = scanner.nextFloat();
                            String depositSQL = "UPDATE customer SET balance = balance + ? WHERE customer_id = ?";
                            PreparedStatement preparedStatement = connection.prepareStatement(depositSQL);
                            preparedStatement.setFloat(1, deposit);
                            preparedStatement.setInt(2, customer_id);
                            preparedStatement.execute();
                            break;
                        }
                        // Withdraw
                        case 3: {
                            System.out.println("How much money do you want to withdraw? ");
                            float withdraw = scanner.nextFloat();
                            String withdrawSQL = "UPDATE customer SET balance = balance - ? WHERE customer_id = ?";
                            PreparedStatement preparedStatement = connection.prepareStatement(withdrawSQL);
                            preparedStatement.setFloat(1, withdraw);
                            preparedStatement.setInt(2, customer_id);
                            preparedStatement.execute();
                            break;
                        }
                        // Log out
                        case 4: {
                            System.out.println("Logged out!");
                            loggedIn = false;
                            break;
                        }
                        // Delete account
                        case 9: {
                            String deleteSQL = "DELETE FROM customer WHERE customer_id = ?";
                            PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL);
                            preparedStatement.setInt(1, customer_id);
                            preparedStatement.execute();
                            System.out.println("You have deleted your account!");
                            loggedIn = false;
                            break;
                        }
                        case 0: {
                            done = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

public static void printNotLoggedInOptions() {
    System.out.println("1. Create an account");
    System.out.println("2. Log into account");

    System.out.println("0. Exit");
}

public static void printLoggedInOptions() {
    System.out.println("1. Check Balance");
    System.out.println("2. Deposit Money");
    System.out.println("3. Withdraw Money");
    System.out.println("4. Log out");

    System.out.println("0. Exit");
    }
}

class Account {

    Random random = new Random();

    int BIN = 400000; // 6 bin + 10 other
    int balance = 0;
    String cardNumber = Integer.toString(BIN);
    String pin = "";

    public void createCardNumber() {
        int[] numbers = new int[16];
        numbers[0] = 4;
        numbers[1] = 0;
        numbers[2] = 0;
        numbers[3] = 0;
        numbers[4] = 0;
        numbers[5] = 0;
        StringBuilder originalNumbers = new StringBuilder();
        originalNumbers.append("400000");
        // Generate random card number
        for(int i = 6; i < 15; i++) { // Leave the last digit for checksum
            numbers[i] = random.nextInt(10); // 0-9
            originalNumbers.append(numbers[i]);
        }

        // 1- Multiply odd digits by 2
        for(int i = 0; i < numbers.length - 1; i++) { // Loop through every number excluding final
            if(i % 2 == 0) { // Every odd number starting from 1 = every even index
                numbers[i] = numbers[i] * 2;
            }
        }
;
        // 2 - Subtract 9 to numbers over 9
        int sum = 0;
        for(int i = 0; i < numbers.length - 1; i++) { // Loop through every number excluding final
            if(numbers[i] > 9) {
                numbers[i] -= 9;
            }
            sum += numbers[i];
        }

        // 3 - Add all numbers and subtract from a 10 multiple
        int increment10 = 0;
        while(true) {
            if(increment10 - sum > 9 || increment10 - sum < 0) {
                increment10 += 10;
            } else {
                originalNumbers.append(increment10 - sum);
                break;
            }
        }
        this.cardNumber = originalNumbers.toString();
    }

    public void createPin() {
        this.pin ="";
        for(int i = 0; i < 4; i++) {
            this.pin = this.pin + Integer.toString(random.nextInt(10));
        }
    }

    public String getCardNumber() {
        return this.cardNumber;
    }

    public String getPin() {
        return this.pin;
    }

    public int getBalance() {
        return this.balance;
    }
}
