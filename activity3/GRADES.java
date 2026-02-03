package activity3;

import java.util.*;

public class GRADES {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int[][] grades = new int[50][3];

        String[] gradeCategory = {"PRELIMS", "MIDTERMS", "FINALS"};

        String anotherGrade;

        do {

            System.out.println("""
                [1] Enter Grades
                [2] Display Grades
                [3] Exit
                """);

            System.out.println("Choose an option: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1 -> {
                    System.out.println("""
                [1] COMPRO2
                [2] DSA
                [3] OOP
                [4] EXIT
                """);
                    int gradeChoice = sc.nextInt();

                    switch (gradeChoice) {
                        case 1 -> {

                            for (int i = 0; i < grades.length; i++) {
                                System.out.print(gradeCategory[i] + ": ");
                                grades[0][i] = sc.nextInt();
                                sc.nextLine();
                            }

                        }
                        case 2 -> {

                            for (int i = 0; i < grades.length; i++) {
                                System.out.print(gradeCategory[i] + ": ");
                                grades[1][i] = sc.nextInt();
                                sc.nextLine();
                            }

                        }
                        case 3 -> {

                            for (int i = 0; i < grades.length; i++) {
                                System.out.print(gradeCategory[i] + ": ");
                                grades[2][i] = sc.nextInt();
                                sc.nextLine();
                            }

                        }
                    }
                }

                case 2 -> {
                    System.out.printf("%n%-15s%-15s%-15s%-15s%n","SUBJECTS", "COMPRO2", "DSA2", "OOP2");
                    System.out.println("-------------------------------------------------------");

                   for (int i = 0; i < grades.length; i++) {
                    System.out.printf("%-15s", gradeCategory[i]);
                      for (int j = 0; j < grades[i].length; j++) {
                          System.out.printf("%-15s", grades[j][i]);
                      }
                      System.out.println();
                   }
                
                }

                
                case 3 -> {
                    System.out.print("Thank you!");
                    System.exit(0);
                }

            }

            System.out.print("Would you like to repeat the services: ");
            anotherGrade = sc.nextLine();

        } while (anotherGrade.equalsIgnoreCase("y"));

    }
}

// for (int i = 0; i < grades.length; i++) {
//                     System.out.print("Element " + (i + 1) + ": ");
//                     grades[i][] = sc.nextInt();
//                 }
