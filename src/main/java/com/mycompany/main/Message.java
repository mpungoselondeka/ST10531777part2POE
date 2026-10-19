 package com.mycompany.main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Message {

    public static final String RECIPIENT_SUCCESS =
            "Cell phone number successfully captured.";

    public static final String RECIPIENT_FAILURE =
            "Cell phone number is incorrectly formatted "
            + "or does not contain an international code. "
            + "Please correct the number and try again.";

    public static final String LENGTH_OK = "Message ready to send.";

    public static final String SENT_SUCCESS = "Message successfully sent.";
    public static final String DELETE_PROMPT = "Press 0 to delete the message.";
    public static final String DISREGARDED = "Message deleted.";
    public static final String STORED = "Message successfully stored.";
    public static final String STORE_FAILED = "Message could not be stored.";
    public static final String INVALID_OPTION = "Invalid option.";

    public static final int MAX_MESSAGE_LENGTH = 250;

    private static String jsonFile = "storedMessages.json";

    private static final Random random = new Random();

    private static final ArrayList<Message> sentMessages = new ArrayList<>();
    private static final ArrayList<Message> storedMessages = new ArrayList<>();

    // Counts every message the user has entered (sent, stored or disregarded)
    private static int messagesEntered = 0;

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    // Normal constructor: the ID and message number are auto generated
    public Message(String recipient, String messageText) {
        this(generateMessageID(),
                returnTotalMessages() + returnTotalStoredMessages(),
                recipient,
                messageText);
        messagesEntered++;
    }

    // Constructor that lets the caller supply the ID and message number.
    // Used by the unit tests so the hash can be checked against known values.
    public Message(String messageID, int messageNumber,
            String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = createMessageHash();
    }

    // Generates a unique ten-digit message ID (leading zeros allowed)
    private static String generateMessageID() {
        String id;

        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                sb.append(random.nextInt(10));
            }
            id = sb.toString();
        } while (idExists(id));

        return id;
    }

    private static boolean idExists(String id) {
        for (Message m : sentMessages) {
            if (m.messageID.equals(id)) {
                return true;
            }
        }
        for (Message m : storedMessages) {
            if (m.messageID.equals(id)) {
                return true;
            }
        }
        return false;
    }

    // Ensures the message ID is not more than ten characters
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    // Returns the "Message ID is created" text
    public String returnMessageIDGenerated() {
        return "Message ID generated: " + messageID;
    }

    // Ensures the message is not more than 250 characters
    public static String checkMessageLength(String text) {
        int length = (text == null) ? 0 : text.length();

        if (length <= MAX_MESSAGE_LENGTH) {
            return LENGTH_OK;
        }

        return "Message exceeds 250 characters by "
                + (length - MAX_MESSAGE_LENGTH)
                + "; please reduce the size.";
    }

    // Ensures the cell number starts with an international code
    // (+ and 1 to 3 digits, e.g. +27) followed by no more than
    // ten digits, e.g. +27831234567. That means 4 to 13 digits
    // after the "+" in total, so a bare code like "+27" is rejected.
    public static String checkRecipientCell(String cell) {
        if (cell != null
                && cell.matches("^\\+[0-9]{4,13}$")) {
            return RECIPIENT_SUCCESS;
        }

        return RECIPIENT_FAILURE;
    }

    // Creates the message hash, e.g. 00:0:HITONIGHT
    public String createMessageHash() {
        String[] words = messageText.trim().split("\\s+");

        String firstWord = words[0].replaceAll("[^A-Za-z0-9]", "");
        String lastWord = words[words.length - 1].replaceAll("[^A-Za-z0-9]", "");

        String hash = messageID.substring(0, 2) + ":"
                + messageNumber + ":"
                + firstWord + lastWord;

        return hash.toUpperCase();
    }

    // Performs the chosen action:
    // 1 = send, 2 = disregard, 3 = store to send later
    public String SentMessage(int choice) {
        switch (choice) {
            case 1:
                sentMessages.add(this);
                return SENT_SUCCESS;

            case 2:
                return DELETE_PROMPT;

            case 3:
                return storeMessage() ? STORED : STORE_FAILED;

            default:
                return INVALID_OPTION;
        }
    }

    // Lets the user choose to send, disregard or store the message
    public String SentMessage(Scanner input) {
        while (true) {
            System.out.println();
            System.out.println("What would you like to do with this message?");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            System.out.print("Choose an option: ");

            String choice = input.nextLine().trim();

            if (choice.equals("1")) {
                return SentMessage(1);

            } else if (choice.equals("2")) {
                System.out.println(SentMessage(2));
                System.out.print("> ");
                String confirm = input.nextLine().trim();

                if (confirm.equals("0")) {
                    return DISREGARDED;
                }
                // anything else: go back to the options

            } else if (choice.equals("3")) {
                return SentMessage(3);

            } else {
                System.out.println("Invalid option. Please enter 1, 2 or 3.");
            }
        }
    }

    // Returns all the messages sent while the program is running
    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent.";
        }

        StringBuilder sb = new StringBuilder();

        for (Message m : sentMessages) {
            sb.append(m.getDetails()).append("\n");
        }

        return sb.toString();
    }

    // Total number of messages sent
    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    public static int returnTotalStoredMessages() {
        return storedMessages.size();
    }

    // Total number of messages the user has entered (for the message limit)
    public static int returnTotalEntered() {
        return messagesEntered;
    }

    // Clears all saved messages and counters (used by the unit tests)
    public static void resetMessages() {
        sentMessages.clear();
        storedMessages.clear();
        messagesEntered = 0;
    }

    // Changes the JSON file used by storeMessage (used by the unit tests)
    public static void setJsonFile(String path) {
        jsonFile = path;
    }

    // Stores the message in a JSON file
    public boolean storeMessage() {
        storedMessages.add(this);

        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(storedMessagesToJson());
            return true;

        } catch (IOException e) {
            storedMessages.remove(this);
            System.out.println("Error saving JSON file: " + e.getMessage());
            return false;
        }
    }

    private static String storedMessagesToJson() {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);

            json.append("  {\n");
            json.append("    \"messageID\": \"")
                    .append(jsonEscape(m.messageID)).append("\",\n");
            json.append("    \"messageNumber\": ")
                    .append(m.messageNumber).append(",\n");
            json.append("    \"recipient\": \"")
                    .append(jsonEscape(m.recipient)).append("\",\n");
            json.append("    \"message\": \"")
                    .append(jsonEscape(m.messageText)).append("\",\n");
            json.append("    \"messageHash\": \"")
                    .append(jsonEscape(m.messageHash)).append("\"\n");
            json.append("  }");

            if (i < storedMessages.size() - 1) {
                json.append(",");
            }

            json.append("\n");
        }

        json.append("]\n");
        return json.toString();
    }

    private static String jsonEscape(String text) {
        StringBuilder sb = new StringBuilder();

        for (char c : text.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }

        return sb.toString();
    }

    // Message details in the required order:
    // Message ID, Message Hash, Recipient, Message
    public String getDetails() {
        return "---------------------------\n"
                + "Message ID:   " + messageID + "\n"
                + "Message Hash: " + messageHash + "\n"
                + "Recipient:    " + recipient + "\n"
                + "Message:      " + messageText + "\n"
                + "---------------------------";
    }
}