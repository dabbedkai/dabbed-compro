
import java.io.*;
import java.util.ArrayList;

public class AttendanceAppp {

    public static void addStudent(ArrayList<Student> students, String name) {
        Student newStudent = new Student(name);
        students.add(newStudent);
        System.out.println("Added student: " + name);
    }

    public static boolean recordAttendance(ArrayList<Student> students, String studentName, int mark) {
        for (Student student : students) {
            if (student.getName().equalsIgnoreCase(studentName)) {
                student.getAttendanceMarks().add(mark);
                System.out.println("Recorded mark (" + mark + ") for " + studentName);
                return true;
            }
        }

        System.out.println("Error: Student '" + studentName + "' not found.");
        return false;
    }

    public static double getAttendancePercentage(Student student) {
        ArrayList<Integer> marks = student.getAttendanceMarks();
        if (marks.isEmpty()) {
            return 0.0;
        }

        double totalMarks = 0;
        for (Integer mark : marks) {
            totalMarks += mark;
        }

        return (totalMarks / marks.size()) * 100.0;
    }

    public static String getDisplayInfo(Student student) {
        double percentage = getAttendancePercentage(student);
        String formattedPercentage = String.format("%.1f", percentage);
        return "Name: " + student.getName()
                + "Attendance: " + formattedPercentage + "%"
                + "Records: " + student.getAttendanceMarks().toString();
    }

    public static void displayAllStudents(ArrayList<Student> students) {
        System.out.println("\nClassStatus");
        if (students.isEmpty()) {
            System.out.println("No students in the list.");
        } else {
            for (Student s : students) {
                System.out.println(getDisplayInfo(s));
            }
        }
    }

    public static void saveStudents(ArrayList<Student> students, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Student s : students) {

                StringBuilder sb = new StringBuilder();
                sb.append(s.getName());

                for (Integer mark : s.getAttendanceMarks()) {
                    sb.append(",").append(mark);
                }

                writer.write(sb.toString());
                writer.newLine();
            }
            System.out.println("Success: Data saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }

    public static ArrayList<Student> loadStudents(String filename) {
        ArrayList<Student> loadedList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(",");
                if (parts.length > 0) {

                    String name = parts[0].trim();
                    Student s = new Student(name);

                    for (int i = 1; i < parts.length; i++) {
                        try {
                            int mark = Integer.parseInt(parts[i].trim());
                            s.getAttendanceMarks().add(mark);
                        } catch (NumberFormatException nfe) {
                            System.err.println("Skipping invalid mark '" + parts[i] + "' for student " + name);
                        }
                    }
                    loadedList.add(s);
                }
            }
            System.out.println("Success: Data loaded from " + filename);
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found (" + filename + ")");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return loadedList;
    }

    public static void main(String[] args) {
        String filename = "attendance.txt";

        ArrayList<Student> studentList = new ArrayList<>();

        addStudent(studentList, "Alice");
        addStudent(studentList, "Bob");
        addStudent(studentList, "Charlie");

        recordAttendance(studentList, "Alice", 1);
        recordAttendance(studentList, "Alice", 1);

        recordAttendance(studentList, "Bob", 1);
        recordAttendance(studentList, "Bob", 0);

        recordAttendance(studentList, "Charlie", 0);
        recordAttendance(studentList, "Charlie", 0);

        System.out.println("--- Testing invalid student ---");
        boolean success = recordAttendance(studentList, "Dave", 1);

        if (!success) {
            System.out.println("(Handling: Logic detected that Dave does not exist)");
        }

        displayAllStudents(studentList);
        saveStudents(studentList, filename);
        ArrayList<Student> reloadedList = loadStudents(filename);

        displayAllStudents(reloadedList);

        loadStudents("ghost_file.txt");
    }
}
