package com.grades;

import java.io.File;
import java.io.FileWriter;

public class FileWriterThread extends Thread {
    
    private SharedData shared;

    public FileWriterThread(SharedData shared) {
        this.shared = shared;
    }

    @Override
    public void run() {
        while (shared.isProgramRunning) {
            try {
                // pause for 5 seconds
                Thread.sleep(5000);
                
                boolean needsToSave = false;
                String jsonOutput = "[\n";

                // lock the list to check if the size changed
                synchronized (shared.gradeList) {
                    if (shared.gradeList.size() != shared.lastSavedListSize) {
                        needsToSave = true;
                        
                        // manually build the JSON string
                        for (int i = 0; i < shared.gradeList.size(); i++) {
                            jsonOutput += shared.gradeList.get(i).toJson();
                            
                            if (i < shared.gradeList.size() - 1) {
                                jsonOutput += ",\n";
                            } else {
                                jsonOutput += "\n";
                            }
                        }
                    }
                }

                // If the size was different, write it to the file
                if (needsToSave) {
                    jsonOutput += "]\n";
                    System.out.println("\n[WRITER] New grade detected! Saving to file...");
                    
                    FileWriter myWriter = new FileWriter(shared.fileName);
                    myWriter.write(jsonOutput);
                    myWriter.close();
                    
                    // update our trackers so we don't save or read again unnecessarily
                    File file = new File(shared.fileName);
                    shared.lastTimeFileChanged = file.lastModified();
                    
                    synchronized (shared.gradeList) {
                        shared.lastSavedListSize = shared.gradeList.size();
                    }
                    
                    System.out.println("[WRITER] File saved successfully!");
                }
            } catch (Exception e) {
                System.out.println("[WRITER ERROR] Could not save: " + e.getMessage());
            }
        }
    }
}