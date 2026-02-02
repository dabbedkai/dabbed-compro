package practice.week4;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Reader {

    public static void main(String[] args) {

        ArrayList<String> infos = new ArrayList<>();
        // Use try-with-resources to ensure the Scanner is closed automatically
        try (Scanner myReader = new Scanner(new File("data.txt"))) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                infos.add(data);
            }

            for (String info : infos) {
                System.out.println(info);
            }

            System.out.println(infos);
        } catch (FileNotFoundException e) {
            System.out.println("No file found");
            e.printStackTrace();
        }
    }
}
