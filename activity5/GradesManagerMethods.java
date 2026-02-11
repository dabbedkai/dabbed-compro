package activity5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GradesManagerMethods {

    private int[][] grades = new int[50][3];
    private int gradeCounter = 0;
    private StringBuilder sb = new StringBuilder();
    private List<String> gradeCategory = new ArrayList<>(50);
    private String[] gradeSemester = {"PRELIMS", "MIDTERMS", "FINALS"};

    public static void main(String[] args) {
        GradesManagerMethods manager = new GradesManagerMethods();
        manager.runGradeProgram();
    }

    public void runGradeProgram() {
        boolean isRunning = true;

        try (Scanner sc = new Scanner(System.in)) {
            do {
                displayMenu();
                System.out.print("Choose an option: ");
                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1 -> enterGrades(sc);
                    case 2 -> viewGrades();
                    case 3 -> searchGradeCategory();
                    case 4 -> isRunning = exitProgram();
                }
            } while (isRunning);
        }
    }

    public void displayMenu() {
        System.out.println("""

                [1] Enter Grades
                [2] View Grades
                [3] Search Grades
                [4] Exit
                """);
    }

    public void enterGrades(Scanner sc) {
        System.out.print("Enter subject name: ");
        String newSubject = sc.nextLine();
        gradeCategory.add(newSubject);

        System.out.println("\nEnter grade for " + newSubject + ": ");
        for (int j = 0; j < grades[gradeCounter].length; j++) {
            System.out.print(gradeSemester[j] + ": ");
            grades[gradeCounter][j] = sc.nextInt();
            sc.nextLine();
        }

        String line = String.format("%s,%d,%d,%d%n", gradeCategory.get(gradeCounter), grades[gradeCounter][0], grades[gradeCounter][1], grades[gradeCounter][2]);

        try (FileWriter fw = new FileWriter("data.csv", true)) {
            fw.write(line);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        gradeCounter++;
    }

    public void viewGrades() {
        try (Scanner myReader = new Scanner(new File("data.csv"))) {
            if (!myReader.hasNextLine()) {
                System.out.println("No grades found.");
            } else {
                System.out.println("------------------------------------------------------");
                System.out.printf("| %-20s | %-7s | %-7s | %-7s |%n", "Name", "Prelim", "Midterm", "Final");
                System.out.println("|----------------------+---------+---------+---------|");
                while (myReader.hasNextLine()) {
                    String line = myReader.nextLine();
                    String[] fields = line.split(",");

                    if (fields.length >= 4) {
                        String name = fields[0];
                        String grade1 = fields[1];
                        String grade2 = fields[2];
                        String grade3 = fields[3];

                        System.out.printf("| %-20s | %-7s | %-7s | %-7s |%n", name, grade1, grade2, grade3);
                    }
                }
                System.out.println("------------------------------------------------------");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found");
            e.printStackTrace();
        }
    }

    public void searchGradeCategory() {
        System.out.print("Enter subject to search: ");
        Scanner sc = new Scanner(System.in);
        String searchValue = sc.nextLine();

        try (Scanner myReader = new Scanner(new File("data.csv"))) {
            boolean found = false;
            System.out.println("------------------------------------------------------");
            System.out.printf("| %-20s | %-7s | %-7s | %-7s |%n", "Name", "Prelim", "Midterm", "Final");
            System.out.println("|----------------------+---------+---------+---------|");
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                String[] fields = line.split(",");

                if (fields.length >= 4) {
                    String name = fields[0];

                    if (name.equalsIgnoreCase(searchValue)) {
                        String grade1 = fields[1];
                        String grade2 = fields[2];
                        String grade3 = fields[3];

                        System.out.printf("| %-20s | %-7s | %-7s | %-7s |%n", name, grade1, grade2, grade3);
                        found = true;
                    }
                }
            }
            System.out.println("------------------------------------------------------");
            if (!found) {
                System.out.println("No matching grades found.");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found");
        }
    }

    public boolean exitProgram() {
        System.out.print("YOU EXIT THE PROGRAM!");
        return false;
    }
}


