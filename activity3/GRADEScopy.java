package activity3;

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
            sb.append("SUBJECTS,PRELIMS,MIDTERMS,FINALS\n");
            do {
                System.out.println("""

                [1] Enter Grades
                [2] Exit
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

                    }
                    case 2 -> {

                        System.out.print("YOU EXIT THE PROGRAM!");

                        isRunning = false;
                    }
                }
            } while (isRunning == true);
            try (FileWriter fw = new FileWriter("data.csv")) {

                fw.write(sb.toString());

            } catch (IOException e) {

                System.out.println(e.getMessage());

            }
        }
    }
}
