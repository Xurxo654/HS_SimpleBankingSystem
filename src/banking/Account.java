package banking;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class Account {

    private static ArrayList<String> accounts = new ArrayList<>();

    private String accountNumber;
    private int balance;
    private int checkSum;
    private String ccNum;
    private String PIN;

    Random r = new Random();

    public Account() {
        this.accountNumber =  generateAccountNumber();
        accounts.add(this.accountNumber);
        this.PIN = generatePIN();
        this.balance = 0;
        this.checkSum = generateCheckSum();
        this.ccNum = ccNumber();
    }

    public Account(String cc, String pin, int balance) {
        this.accountNumber = cc.substring(6,15);
        this.PIN = pin;
        this.balance = balance;
        this.ccNum = cc;
    }

    private String generateAccountNumber() {
        int accNum = r.nextInt(999_999_999);
        DecimalFormat df = new DecimalFormat("000000000");
        String acc = df.format(accNum);
        while (accounts.contains(acc)) {
            acc = generateAccountNumber();
        }
        return acc;
    }

    private int generateCheckSum() {
        int total = 8; //based on BIN of 400000 first six digits will always be 8
        String accNum = String.valueOf(accountNumber);
        for (int i = 0; i < accNum.length() ; i++) {
            if ( i % 2 == 0) {
                int test = (accNum.charAt(i) - '0') * 2;
                total += test > 9 ? test - 9 : test;
            } else {
                total += accNum.charAt(i) - '0';
            }
        }
        return total % 10 == 0 ? 0 : 10 - total % 10;
    }

    private String generatePIN() {
        int pinInt = r.nextInt(1000);
        DecimalFormat df = new DecimalFormat("0000");
        return df.format(pinInt);
    }

    private String ccNumber() {
        return "400000" + accountNumber + checkSum;
    }

    public void printInfo() {
        System.out.println("Your Credit Card Number:\n" + ccNum);
        System.out.println("Your card PIN:\n" + PIN);
    }


    public boolean isPin(String pin) {
        return this.PIN.equals(pin);
    }

    public String getCcNum() {
        return ccNum;
    }

    public int getBalance() {
        return balance;
    }

    public String sqlInsert() {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO card (number, pin, balance) VALUES (");
        sb.append("'" + ccNum + "', ");
        sb.append("'" + PIN + "', ");
        sb.append(balance + ");");
        return sb.toString();
    }

    public void addIncome(int i) {
        this.balance += i;
    }

    public void transfer(Account dest, int amount) {
        if (amount < balance) {
            balance -= amount;
            dest.addIncome(amount);
        } else {
            System.out.println("Not enough money!");
        }
    }
}
