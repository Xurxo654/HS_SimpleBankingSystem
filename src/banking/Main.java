package banking;

import org.sqlite.JDBC;
import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static boolean isRun = true;
    static boolean isLoggedIn = false;
    static ArrayList<Account> accounts = new ArrayList<>();
    static Connection conn = null;

    public static void main(String[] args) {

        //database setup
        String url = "jdbc:sqlite:" + args[1];
        String card = "CREATE TABLE IF NOT EXISTS card ( \n" +
                "id INTEGER PRIMARY KEY , \n" +
                "number TEXT, \n"+
                "pin TEXT, \n" +
                "balance INTEGER DEFAULT 0);";

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try  {
            conn = dataSource.getConnection();
            Statement makeCard = conn.createStatement();
            makeCard.executeUpdate(card);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        loadAccounts();

        while (isRun) {
            startMenu();
        }
    }

    public static void startMenu() {
        System.out.println("1. create an account\n"+
                "2. Log into account\n"+
                "0. Exit");
        int option = sc.nextInt();
        sc.nextLine();

        switch (option) {
            case 1: createAccount();
                break;
            case 2: logIn();
                break;
            case 0: isRun = false;
                System.out.println("Bye!");
                break;
            default:
                System.out.println("invalid option");
        }
    }

    public static void menuLoggedIn(Account acc) {
        System.out.println("1. Balance\n"+
                "2. Add income\n"+
                "3. Do transfer\n"+
                "4. Close account\n"+
                "5. Log out\n"+
                "0. Exit");
        int option = sc.nextInt();
        sc.nextLine();

        switch (option) {
            case 1:
                System.out.println("Balance: " + acc.getBalance());
                break;
            case 2:
                enterIncome(acc);
                break;
            case 3:
                transfer(acc);
                break;
            case 4:
                closeAccount(acc);
                isLoggedIn = false;
                break;
            case 5:
                System.out.println("You have successfully logged out!");
                isLoggedIn = false;
                break;
            case 0: isRun = false;
                isLoggedIn = false;
                System.out.println("Bye!");
                break;
            default:
                System.out.println("invalid option");
        }
    }

    public static void createAccount() {
        Account temp = new Account();
        System.out.println("Your card has been created");
        temp.printInfo();
        accounts.add(temp);

        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(temp.sqlInsert());
            System.out.println("account added");
        } catch (SQLException e) {
            System.out.println("account not added");
        }



    }

    public static void loadAccounts() {

        try (Statement statement= conn.createStatement()) {
            try (ResultSet res = statement.executeQuery("SELECT * FROM card")) {
                while (res.next()) {
                    String acc = res.getString("number");
                    String pin = res.getString("pin");
                    int balance = res.getInt("balance");

                    accounts.add(new Account(acc, pin, balance));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void logIn() {
        System.out.println("Enter your card number:");
        String cc = sc.nextLine();
        System.out.println("Enter your PIN:");
        String pin = sc.nextLine();

        Account loggedIn = getByAccountNumber(cc);

        if (loggedIn != null) {
            if (loggedIn.isPin(pin)) {
                isLoggedIn = true;
                System.out.println("You have successfully logged in!\n");
                while (isLoggedIn) {
                    menuLoggedIn(loggedIn);
                }
            }
        } else {
            System.out.println("Wrong card number or PIN");
        }
    }

    public static Account getByAccountNumber(String accNum) {
        for (Account acc : accounts) {
            if (acc.getCcNum().equals(accNum)) {
                return acc;
            }
        }
        return null;
    }

    public static void enterIncome(Account acc) {
        System.out.println("Enter income:\n");
        int i = Integer.parseInt(sc.nextLine());

        acc.addIncome(i);

        updateBalance(acc);
    }

    public static void transfer(Account acc) {
        System.out.println("Transfer\nEnter card number:\n");
        String destinationAcc = sc.nextLine();
        if ( isLuhn(destinationAcc) ) {
            Account dest = getByAccountNumber(destinationAcc);
            if (dest != null) {
                System.out.println("Enter how much money you want to transfer:\n");
                int amount = Integer.parseInt(sc.nextLine());
                acc.transfer(dest, amount);
                updateBalance(acc);
                updateBalance(dest);
            } else {
                System.out.println("Such a card does not exist.");
            }
        } else {
            System.out.println("probably you made mistake in the card number.  Please try again!");
        }
    }

    public static boolean isLuhn(String cc) {
        int total = 0;
        for (int i = 0; i < cc.length() - 1; i++) {
            if ( i % 2 == 0) {
                int test = (cc.charAt(i) - '0') * 2;
                total += test > 9 ? test - 9 : test;
            } else {
                total += cc.charAt(i) - '0';
            }
        }
        total += cc.charAt(cc.length() - 1) - '0';

        return total % 10 == 0;
    }

    public static void closeAccount(Account acc) {
        String sqlDeleteAcc = "DELETE FROM card WHERE number = '" + acc.getCcNum() + "';";
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate(sqlDeleteAcc);
        } catch (SQLException e) {

        }
        accounts.remove(acc);

        System.out.println("The account has been closed");
    }

    public static void updateBalance(Account acc) {
        String updateIncome = "UPDATE card\n" +
                "SET balance = " + acc.getBalance() + "\n" +
                "WHERE number = " + acc.getCcNum() + ";";
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate(updateIncome);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
