package activity4;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Week4Activity1 {

    public static void main(String[] args) {

        int grades[][] = new int[50][3];

        String strAnotherP;
        char cAnotherP;

        int gradeCounter = 0;

        StringBuilder sb = new StringBuilder();

        ArrayList<String> gradeCategory = new ArrayList<>(Arrays.asList("COMPRO1", "COMPRO2", "OOP", "DSA", "MMW"));
        String gradeSemester[] = {"PRELIMS", "MIDTERMS", "FINALS"};

        try (Scanner sc = new Scanner(System.in)) {

            sb.append("SUBJECTS,PRELIMS,MIDTERMS,FINALS\n");

            for (int i = 0; i < gradeCategory.size(); i++) {

                System.out.println("Enter grade for " + gradeCategory.get(i) + ": ");

                for (int j = 0; j < grades[i].length; j++) {
                    System.out.print(gradeSemester[j] + ": ");
                    grades[i][j] = sc.nextInt();
                    sc.nextLine();
                }
                gradeCounter++;
                 sb.append(String.format("%s,%d,%d,%d%n",gradeCategory.get(i), grades[i][0], grades[i][1], grades[i][2]));
            }

            do { 
                System.out.println("ADD ANOTHER GRADE Y/N? ");
                strAnotherP = sc.nextLine();
                cAnotherP = strAnotherP.charAt(0);
                if (cAnotherP == 'Y' || cAnotherP == 'y') {

                    System.out.println("Enter subject name: ");
                    String newSubject = sc.nextLine();
                    gradeCategory.add(newSubject);
                    
                    System.out.println("Enter grade for " + newSubject + ": ");
                    
                    for (int j = 0; j < grades[gradeCounter].length; j++) {
                        System.out.print(gradeSemester[j] + ": ");
                        grades[gradeCounter][j] = sc.nextInt();
                        sc.nextLine();
                    }

                    sb.append(String.format("%s,%d,%d,%d%n",newSubject, grades[gradeCounter][0], grades[gradeCounter][1], grades[gradeCounter][2]));
                    gradeCounter++;
                }
            } while (cAnotherP == 'Y' || cAnotherP == 'y');

        } 
        try(FileWriter fw = new FileWriter("data.csv")){

            fw.write(sb.toString());

        }catch(IOException e){

            System.out.println(e.getMessage());

        }
    }
}
