package com.mycompany.main;

import java.util.Scanner;

public class Main {

    static Scanner input = new Scanner(System.in);

    static String registeredUsername;
    static String registeredPassword;
    static String registeredCellPhone;

    static int messageLimit;

    // ================= USER REGISTRATION =================

    public static boolean checkUserName(String username) {
        return username.length() == 5 && username.contains("_");
    }

    public static boolean checkPasswordComplexity(String password) {
        return password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*[0-9].*")
                && password.matches(".*[^a-zA-Z0-9].*");
    }

    public static boolean checkCellPhoneNumber(String number) {
        return number.matches("^\\+27[0-9]{9}$");
    }

    public static void registerUser() {
        String username;
        String password;
        String cellphone;

        while (true) {
            System.out.print("Enter your username: ");
            username = input.nextLine();

            if (checkUserName(username)) {
                break;
            }

            System.out.println("Username is incorrectly formatted. Please try again.");
        }

        while (true) {
            System.out.print("Enter your password: ");
            password = input.nextLine();

            if (checkPasswordComplexity(password)) {
                break;
            }

            System.out.println("Password is incorrectly formatted. Please try again.");
        }

        while (true) {
            System.out.print("Enter your cellphone number (+27...): ");
            cellphone = input.nextLine();

            if (checkCellPhoneNumber(cellphone)) {
                break;
            }

            System.out.println("Cellphone number is incorrectly formatted. Please try again.");
        }

        registeredUsername = username;
        registeredPassword = password;
        registeredCellPhone = cellphone;

        System.out.println("User registered successfully.");
    }

    // ================= LOGIN =================

    public static boolean loginUser(String username, String password) {
        return username.equals(registeredUsername)
                && password.equals(registeredPassword);
    }

    public static String returnLoginStatus(boolean loginSuccessful) {
        if (loginSuccessful) {
            return "Welcome, it is great to see you again.";
        }

        return "Username or password incorrect, please try again.";
    }

    public static void displayWelcomeMessage() {
        System.out.println("Welcome to QuickChat.");
    }

    // ================= MESSAGE LIMIT =================

    public static void askMessageLimit() {
        while (true) {
            System.out.print("How many messages do you wish to enter? ");
            String answer = input.nextLine().trim();

            try {
                int number = Integer.parseInt(answer);

                if (number > 0) {
                    messageLimit = number;
                    break;
                }

                System.out.println("Please enter a number greater than 0.");

            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a whole number.");
            }
        }
    }

    // ================= SEND MESSAGE =================

    public static void sendMessage() {
        if (Message.returnTotalEntered() >= messageLimit) {
            System.out.println("You have reached your limit of "
                    + messageLimit + " message(s).");
            return;
        }

        String recipient;

        while (true) {
            System.out.print("Enter the recipient's cell number (e.g. +27718693002): ");
            recipient = input.nextLine().trim();

            String result = Message.checkRecipientCell(recipient);
            System.out.println(result);

            if (result.equals(Message.RECIPIENT_SUCCESS)) {
                break;
            }
        }

        String text;

        while (true) {
            System.out.print("Enter your message: ");
            text = input.nextLine();

            if (text.trim().isEmpty()) {
                System.out.println("Message cannot be empty.");
                continue;
            }

            String lengthResult = Message.checkMessageLength(text);
            System.out.println(lengthResult);

            if (lengthResult.equals(Message.LENGTH_OK)) {
                break;
            }
        }

        Message message = new Message(recipient, text);

        if (!message.checkMessageID()) {
            System.out.println("Message ID is invalid. Message was not created.");
            return;
        }

        System.out.println(message.returnMessageIDGenerated());

        String result = message.SentMessage(input);
        System.out.println(result);

        if (result.equals(Message.SENT_SUCCESS)) {
            System.out.println(message.getDetails());
        }

        if (Message.returnTotalEntered() >= messageLimit) {
            System.out.println();
            System.out.println("All " + messageLimit + " message(s) have been entered.");
            System.out.println("Total messages sent: " + Message.returnTotalMessages());
        }
    }

    // ================= MAIN MENU =================

    public static void runMenu() {
        while (true) {
            System.out.println();
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.print("Choose an option: ");

            String choice = input.nextLine().trim();

            if (choice.equals("1")) {
                sendMessage();

            } else if (choice.equals("2")) {
                System.out.println("Coming Soon.");

            } else if (choice.equals("3")) {
                System.out.println();
                System.out.println("Total messages sent: " + Message.returnTotalMessages());
                System.out.println(Message.printMessages());
                System.out.println("Goodbye.");
                break;

            } else {
                System.out.println("Invalid option. Please enter 1, 2 or 3.");
            }
        }
    }

    // ================= MAIN METHOD =================

    public static void main(String[] args) {
        registerUser();

        boolean loginSuccessful = false;

        while (!loginSuccessful) {
            System.out.print("Enter your username to login: ");
            String loginUsername = input.nextLine();

            System.out.print("Enter your password to login: ");
            String loginPassword = input.nextLine();

            loginSuccessful = loginUser(loginUsername, loginPassword);
            System.out.println(returnLoginStatus(loginSuccessful));
        }

        displayWelcomeMessage();
        askMessageLimit();
        runMenu();
    }
}

        
