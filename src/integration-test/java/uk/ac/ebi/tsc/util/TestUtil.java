package uk.ac.ebi.tsc.util;

public class TestUtil {
    
    static String bearer(String token) {
        
        return "Bearer " + token;
    }
    
    static String quote(String s) {
        
        return '"' + s + '"';
    }
}
