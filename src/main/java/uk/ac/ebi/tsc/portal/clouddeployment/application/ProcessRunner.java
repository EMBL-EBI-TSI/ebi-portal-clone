package uk.ac.ebi.tsc.portal.clouddeployment.application;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import uk.ac.ebi.tsc.portal.clouddeployment.utils.InputStreamLogger;

public class ProcessRunner {
    
    Either<Tuple2<Integer,String>, Integer> run(String ... cmd) {
            
        try 
        {
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            Process p = processBuilder.start();
            
            int exitStatus = p.waitFor();
            
            String errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
            
            return exitStatus == 0 ? Either.right(new Integer(exitStatus))
                                   : Either.left(Tuple.of(exitStatus, errorOutput))
                                   ;
        }
        catch (Exception e) 
        {
            return Either.left(Tuple.of(-1, e.toString()));
        } 
    }
}
