package com.grades;

import java.util.Scanner;

public class MultiThreadedListApp {

    public static void main(String[] args) {
        SharedData sharedInfo = new SharedData();

        FileReaderThread reader = new FileReaderThread(sharedInfo);
        FileWriterThread writer = new FileWriterThread(sharedInfo);
        
        reader.start();
        writer.start();

        Scanner scan = new Scanner(System.in);
        String userInput = "";

        while (sharedInfo.isProgramRunning) {
            System.out.println("\n===== GRADE APP =====");
            System.out.println("1. View Current List");
            System.out.println("2. Add New Grade");
            System.out.println("3. Exit Program");
            System.out.print("Choice: ");
            
            userInput = scan.nextLine();

            if (userInput.equals("1")) {
                System.out.println("\n--- GRADES LIST ---");
                
                synchronized (sharedInfo.gradeList) {
                    if (sharedInfo.gradeList.isEmpty()) {
                        System.out.println("List is empty right now.");
                    } else {
                        for (Grades g : sharedInfo.gradeList) {
                            System.out.println("Subj: " + g.getSubject() + 
                                               " | P: " + g.getPrelims() + 
                                               " | M: " + g.getMidterms() + 
                                               " | F: " + g.getFinals());
                        }
                    }
                }
            } 
            else if (userInput.equals("2")) {
                System.out.println("\n--- ADD GRADE ---");
                
                System.out.print("Enter Subject: ");
                String newSubj = scan.nextLine();

                try {
                    System.out.print("Prelims Grade: ");
                    int p = Integer.parseInt(scan.nextLine());

                    System.out.print("Midterms Grade: ");
                    int m = Integer.parseInt(scan.nextLine());

                    System.out.print("Finals Grade: ");
                    int f = Integer.parseInt(scan.nextLine());

                    Grades newGradeObj = new Grades(newSubj, p, m, f);

                    // lock the list and add the new item
                    synchronized (sharedInfo.gradeList) {
                        sharedInfo.gradeList.add(newGradeObj);
                    }
                    
                    System.out.println("Successfully added to the list!");
                    
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format. Grade not added.");
                }
            } 
            else if (userInput.equals("3")) {
                sharedInfo.isProgramRunning = false;
                System.out.println("Closing application...");
            } 
            else {
                System.out.println("Invalid input, please try again.");
            }
        }
        
        scan.close();
    }
}