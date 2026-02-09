package activity4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;



public class GRADEScopy {

    public static void main(String[] args) {

        int[][] grades = new int[50][3];

        int gradeCounter = 0;

        boolean isRunning = true;

        StringBuilder sb = new StringBuilder();

        List<String> gradeCategory = new ArrayList<>(50);

        String gradeSemester[] = {"PRELIMS", "MIDTERMS", "FINALS"};

        try (Scanner sc = new Scanner(System.in)) {
            do {
                System.out.println("""

                [1] Enter Grades
                [2] View Grades
                [3] Exit
                """);

                System.out.println("Choose an option: ");
                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1 -> {

                        System.out.println("Enter subject name: ");
                        String newSubject = sc.nextLine();
                        gradeCategory.add(newSubject);

                        System.out.println("\nEnter grade for " + newSubject + ": ");

                        for (int j = 0; j < grades[gradeCounter].length; j++) {

                            System.out.print(gradeSemester[j] + ": ");
                            grades[gradeCounter][j] = sc.nextInt();
                            sc.nextLine();

                        }

                        gradeCounter++;
                        sb.append(String.format("%s,%d,%d,%d%n", gradeCategory.get(gradeCounter - 1), grades[gradeCounter - 1][0], grades[gradeCounter - 1][1], grades[gradeCounter - 1][2]));

                        try (FileWriter fw = new FileWriter("data.csv")) {

                            fw.write(sb.toString());

                        } catch (IOException e) {

                            System.out.println(e.getMessage());

                        }

                    }
                    case 2 -> {

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
                    case 3 -> {

                        System.out.print("YOU EXIT THE PROGRAM!");

                        isRunning = false;
                    }
                }
            } while (isRunning == true);
            
        }
    }
}
