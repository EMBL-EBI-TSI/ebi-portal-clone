package uk.ac.ebi.tsc.util;

import static uk.ac.ebi.tsc.util.TestUtil.quote;

public class JsonUtil {
    
    public static String keyValue(String s1, String s2) {
        
        return String.format("    %s : %s"  , quote(s1)
                                            , quote(s2)
                            );
    }
    
    public static String obj(String ... keyValues) {
        
        return String.format("{ %s }", String.join(", ", keyValues));
    }
    
    // TODO? list
}
