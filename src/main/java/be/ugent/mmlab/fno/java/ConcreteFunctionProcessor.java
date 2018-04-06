package be.ugent.mmlab.fno.java;


import java.util.ArrayList;
import java.util.Map;

import org.eclipse.rdf4j.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Function Processor
 *
 * @author bjdmeest
 */
public class ConcreteFunctionProcessor {

    private static ConcreteFunctionProcessor instance = null;
    private static FunctionHandler handler;

    protected ConcreteFunctionProcessor() {
        // Exists only to defeat instantiation.
    }

    public static ConcreteFunctionProcessor getInstance() {
        if (instance == null) {
            String basePath = "";
//            try {
//                String classJar = ConcreteFunctionProcessor.class.getResource("/be/ugent/mmlab/rml/function/ConcreteFunctionProcessor.class").toString();
//                if (classJar.startsWith("jar:")) {
//                    basePath = (new File(ConcreteFunctionProcessor.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getParent() + "/";
//                } else {
//                    basePath = (new File(ConcreteFunctionProcessor.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getParent() + "/../../";
//                }
//            } catch (Exception e) {
//                log.error(e.getMessage());
//            }
            try {
                basePath = System.getProperty("user.dir");
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            log.debug("basePath: " + basePath);
            handler = new FunctionHandler(basePath + "/resources/functions");
            instance = new ConcreteFunctionProcessor();
        }
        return instance;
    }

    // Log
    private static final Logger log =
            LoggerFactory.getLogger(ConcreteFunctionProcessor.class);

    public ArrayList<Value> processFunction(
            String function, Map<String, Object> parameters) {
        FunctionModel fn = handler.get(function);

        if (fn == null) {
            log.error("An implementation of function " + function + " was not found in `resources/functions`.");
            //TODO: wmaroy
            return new ArrayList<>();
        }
        log.debug(parameters.toString());
        ArrayList<Value> result = fn.execute(parameters);
        if(result != null) {
            return result;
        } else {
            return new ArrayList<>();
        }
    }
}
