package com.grades;

import java.util.ArrayList;

public class SharedData {
    // this holds our main list and variables so both threads can see them
    public ArrayList<Grades> gradeList = new ArrayList<>();
    public boolean isProgramRunning = true;
    
    // trackers to know when to read or write
    public long lastTimeFileChanged = 0;
    public int lastSavedListSize = 0;
    
    public String fileName = "data.json";
}