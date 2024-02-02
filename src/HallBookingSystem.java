import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HallBookingSystem {

    // using regex pattern to validate user inputs
    // for a-z and 0-9 and follow letter-number, letter-number pattern
    String regexValidation = "^[a-zA-Z]-[0-9](,[a-zA-Z]-[0-9])*$";
    // for validate number 0-9 and positive only
    String regexNumberValidation = "^[1-9]\\d*$";
    // for letter a-z all uppercase and lowercase
    String regexLetter = "^[A-Za-z]+$";

    // create array to store each shift of showtime
    String[][] morningHall;
    String[][] afternoonHall;
    String[][] nightHall;
    // create array to store history of booking
    String[] history;
    String[] chair;
    int row;
    int seat;
    String shift = null;

    // create array 2d as the hall
    // create method for show menu of booking hall
    public void showMenu(Scanner input) {
        Table table = new Table(1, BorderStyle.UNICODE_HEAVY_BOX_WIDE, ShownBorders.SURROUND_HEADER_AND_COLUMNS);
        // validate row and columns
        String rowChar = validation(input, regexNumberValidation, "Config row: ");
        String seatChar = validation(input, regexNumberValidation, "Config seat: ");

        row = Integer.parseInt(rowChar);
        seat = Integer.parseInt(seatChar);

        morningHall = new String[row][seat];
        afternoonHall = new String[row][seat];
        nightHall = new String[row][seat];
        history = new String[row];

        // set value of each element of array from null to AV
        setArrayHall(morningHall);
        setArrayHall(nightHall);
        setArrayHall(afternoonHall);

        menu(table);

        boolean isValid = false;
        while (!isValid) {
            System.out.println(table.render());
            String option = validation(input, regexLetter, ">>> Please Select Menu Number: ");
            switch (option) {
                // if user chose booking
                case "a","A" -> {
                    // show time information
                    showTime("Showtime Information:");
                    // tell user to choose shift
                    System.out.print("Please select show time (A | B | C): ");
                    shift = input.nextLine();
                    System.out.println();
                    // after this tell user to choose showtime
                    shift(input,shift);
                }
                case "b", "B" -> {
                    // show all hall
                    showHall(morningHall, "<<<Hall A (Morning)>>>");
                    System.out.println();
                    showHall(afternoonHall, "<<<Hall B (Afternoon)>>>");
                    System.out.println();
                    showHall(nightHall, "<<<Hall C (Night)>>>");
                    System.out.println();
                }
                case "c", "C" -> {
                    // show time information
                    showTime("Show Hall:");
                    // tell user to choose shift
                    System.out.print("Please select show time (A | B | C): ");
                    shift = input.nextLine();
                }
                case "d", "D" -> {
                    System.out.println("Reboot Showtime");
                    reboot(morningHall, history, "<<<Reboot Successfully>>>");
                    reboot(afternoonHall, history, "");
                    reboot(nightHall, history, "");
                }
                case "e", "E" -> {
                    System.out.println("History");
                    history(history);

                }
                case "f", "F" -> {
                    System.out.println("Exit");
                    isValid = true;
                }
                default -> {
                    System.out.println();
                    System.out.println("Input Invalid!!!");
                    System.out.println();
                }
            }
        }
    }

    private void menu(Table table) {
        // menu
        table.setColumnWidth(0,50,60);
        table.addCell("[[Application Menu]]");
        table.addCell("<A> Booking");
        table.addCell("<B> Hall");
        table.addCell("<C> Showtime");
        table.addCell("<D> Reboot");
        table.addCell("<E> History");
        table.addCell("<F> Exit");
    }

    // create method for set up the hall after config
    private void showHall(String[][] hall, String message) {
        System.out.println(message);
        for (int i = 0; i < hall.length; i++) {
            for (int j = 0; j < hall[i].length; j++) {
                System.out.print("|" +i+ "-" +j+ "::" +hall[i][j]+ "| ");
            }
            System.out.println();
        }
    }

    // create method for show the showtime
    private void showTime(String message) {
        Table table = new Table(1, BorderStyle.UNICODE_HEAVY_BOX_WIDE, ShownBorders.SURROUND_HEADER_AND_COLUMNS);
        table.setColumnWidth(0,50,60);
        table.addCell(message);
        table.addCell("A) Morning (10:00PM - 12:30PM)");
        table.addCell("B) Afternoon (03:00 - 05:00PM)");
        table.addCell("C) Night (07:00PM - 09:30PM)");
        System.out.println(table.render());
    }

    // create method for choose shift booking
    private void shift(Scanner input, String shift) {
        switch (shift) {
            // morning shift
            case "a", "A" -> {
                shift = "Hall A";
                showHall(morningHall, "Hall A (Morning): ");
                booking(morningHall, input, shift);
            }
            // afternoon shift
            case "b", "B" -> {
                shift = "Hall B";
                showHall(afternoonHall, "Hall B (Afternoon): ");
                booking(afternoonHall, input, shift);
            }
            // night shift
            case "c", "C" -> {
                shift = "Hall C";
                showHall(nightHall, "Hall C (Night): ");
                booking(nightHall, input, shift);
            }
        }
    }

    // create method for booking
    private void booking(String[][] hall, Scanner input, String shift) {
        LocalDate currentDate = LocalDate.now();
        System.out.println();
        System.out.println("INSTRUCTION");
        System.out.println("Single: C-1");
        System.out.println("Multiple (separate by comma): C-1, C-2: ");
        String userInputs = validation(input, regexValidation, "");

        String id = validation(input, regexNumberValidation, "Please Enter ID: ");
        // convert user input to array
        // we need to split each seat by (,)
        // after split each seat we need to split seat element by (-)
        chair = userInputs.split(",");

        // firstChar store the value of row
        // secondChar store the value of seat
        String firstChar;
        String secondChar;

        int firstInt;
        int secondInt;
        for (String col : chair) {
            String[] elements = col.split("-");
            firstChar = elements[0];
            secondChar = elements[1];

            firstInt = convertLetterToNumber(firstChar) - 1;
            secondInt = Integer.parseInt(String.valueOf(secondChar)) - 1;

            if (firstInt <= row && secondInt <= seat) {
                if(Objects.equals(hall[firstInt][secondInt], "AV")) {
                    hall[firstInt][secondInt] = "BK";
                } else if (Objects.equals(hall[firstInt][secondInt], "BK")) {
                    System.out.println(hall[firstInt][secondInt] + "Seat is taken");
                }
            } else {
                System.out.println("<<<We don't have that seat please re booking thanks>>>");
                System.out.println();
            }

        }

        for (int i = 0; i < history.length; i++) {
            if (history[i] == null) {
                history[i] = shift + "\t" + userInputs + "\t\t" + id + "\t\t" + currentDate;
                break;
            }
        }
    }

    // create method for showing history
    private void history(String[] history){
        Table table = new Table(1, BorderStyle.UNICODE_HEAVY_BOX_WIDE, ShownBorders.SURROUND);
        table.setColumnWidth(0,50,60);
        for (int i = 0; i < history.length; i++) {
            if (history[i] != null) {
                table.addCell("NO: "  + (i+1));
                table.addCell("Hall\t\tseat\t\tID\t\tDate");
                table.addCell(history[i]);
                System.out.println(table.render());
                System.out.println();
            }
        }
    }

    // reboot
    private void reboot(String[][] hall, String[] history, String message) {
        System.out.println(message);
        for (int i = 0; i < hall.length; i++) {
            for (int j = 0; j < hall[i].length; j++) {
                hall[i][j] = "AV";
            }
            history[i] = null;
        }
    }

    private void setArrayHall(String[][] hall){
        for (int i = 0; i < hall.length; i++) {
            for (int j = 0; j < hall[i].length; j++) {
                hall[i][j] = "AV";
            }
        }
    }

    // method convert letter to number
    private int convertLetterToNumber(String letter) {
        return letter.toUpperCase().charAt(0) - 'A' + 1;
    }

    // create method for validation user input
    private String validation(Scanner input, String regex, String msg) {
        System.out.print(msg);
        boolean isValid = false;
        String userInput = "";

        // loop util the user input with the match condition
        while (!isValid) {
            userInput = input.nextLine();

            // check if the user input is matching with the pattern that we define
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(userInput);
            // if it is matching does if block
            if (matcher.matches()) {
                isValid = true;
            } else {
                System.out.println("Invalid input!!!");
                System.out.print("Please input again: ");
            }
        }
        return userInput;
    }

    public static void main(String[] args) {

        HallBookingSystem hall = new HallBookingSystem();
        Scanner input = new Scanner(System.in);
        hall.showMenu(input);
    }
}
