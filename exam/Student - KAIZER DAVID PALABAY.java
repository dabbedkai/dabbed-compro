import java.util.ArrayList;

class Student {

    private String name;
    private ArrayList<Integer> attendanceMarks;

    public Student(String name) {
        this.name = name;
        this.attendanceMarks = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Integer> getAttendanceMarks() {
        return attendanceMarks;
    }
}