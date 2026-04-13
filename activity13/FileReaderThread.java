package com.grades;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class FileReaderThread extends Thread {
    
    private SharedData shared;

    public FileReaderThread(SharedData shared) {
        this.shared = shared;
    }

    @Override
    public void run() {
        while (shared.isProgramRunning) {
            try {
                // pause for 3 seconds
                Thread.sleep(3000);
                
                File myFile = new File(shared.fileName);

                // check if file exists and was modified since we last checked
                if (myFile.exists() && myFile.lastModified() > shared.lastTimeFileChanged) {
                    System.out.println("\n[READER] Detected external changes in data.json. Updating list...");

                    Scanner fileReader = new Scanner(myFile);
                    ArrayList<Grades> tempList = new ArrayList<>();
                    
                    String currentSubj = "";
                    int currentP = 0;
                    int currentM = 0;
                    int currentF = 0;

                    // manual JSON parsing using basic String methods
                    while (fileReader.hasNextLine()) {
                        String line = fileReader.nextLine().trim();
                        
                        if (line.contains("\"subject\"")) {
                            // splits at the colon, gets the second part, removes quotes and commas
                            String rawValue = line.split(":")[1];
                            currentSubj = rawValue.replace("\"", "").replace(",", "").trim();
                        } 
                        else if (line.contains("\"prelims\"")) {
                            String rawValue = line.split(":")[1];
                            currentP = Integer.parseInt(rawValue.replace(",", "").trim());
                        } 
                        else if (line.contains("\"midterms\"")) {
                            String rawValue = line.split(":")[1];
                            currentM = Integer.parseInt(rawValue.replace(",", "").trim());
                        } 
                        else if (line.contains("\"finals\"")) {
                            String rawValue = line.split(":")[1];
                            currentF = Integer.parseInt(rawValue.replace(",", "").trim());
                        } 
                        else if (line.equals("}") || line.equals("},")) {
                            // end of an object, add it to our temporary list
                            tempList.add(new Grades(currentSubj, currentP, currentM, currentF));
                        }
                    }
                    fileReader.close();

                    // safely update the main list
                    synchronized (shared.gradeList) {
                        shared.gradeList.clear();
                        for (Grades g : tempList) {
                            shared.gradeList.add(g);
                        }
                        // update the size so the writer thread doesn't trigger
                        shared.lastSavedListSize = shared.gradeList.size();
                    }

                    shared.lastTimeFileChanged = myFile.lastModified();
                    System.out.println("[READER] Done updating! Press Enter to continue.");
                }
            } catch (Exception e) {
                System.out.println("[READER ERROR] Something went wrong: " + e.getMessage());
            }
        }
    }
}