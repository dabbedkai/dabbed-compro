package practice.week4;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Info {
    public static void main(String[] args) {

        StringBuilder sb = new StringBuilder();
    
        try(Scanner sc = new Scanner(System.in)){
            System.out.println("Enter first name: ");
              sb.append("First Name: " + sc.nextLine() + "\n");
            System.out.println("Enter last name name: ");
            sb.append("Last Name: " + sc.nextLine() + "\n");
            System.out.println("Enter age name: ");
            sb.append("Age: " + sc.nextLine() + "\n");
            System.out.println("Enter email name: ");
            sb.append("Email: " + sc.nextLine() + "\n");
            System.out.println("Enter phone number name: ");
            sb.append("Phone Number: " + sc.nextLine() + "\n");

        }
        try(FileWriter fw = new FileWriter("data.txt")){
            fw.write(sb.toString());
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }
}