package com.grades;

public class Grades {
    private String subject;
    private int prelims;
    private int midterms;
    private int finals;

    public Grades(String subject, int prelims, int midterms, int finals) {
        this.subject = subject;
        this.prelims = prelims;
        this.midterms = midterms;
        this.finals = finals;
    }

    public String getSubject() { 
        return subject; 
    }
    
    public int getPrelims() { 
        return prelims; 
    }
    
    public int getMidterms() { 
        return midterms; 
    }
    
    public int getFinals() { 
        return finals; 
    }

    public String toJson() {
        // formatting it like this makes it easier to read manually later
        return "  {\n" +
               "    \"subject\": \"" + subject + "\",\n" +
               "    \"prelims\": " + prelims + ",\n" +
               "    \"midterms\": " + midterms + ",\n" +
               "    \"finals\": " + finals + "\n" +
               "  }";
    }
}