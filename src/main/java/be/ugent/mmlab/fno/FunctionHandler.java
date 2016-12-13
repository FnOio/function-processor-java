package be.ugent.mmlab.fno;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * Function Handler
 *
 * @author bjdmeest
 */
public class FunctionHandler {

    public String basePath;

    public Map<String, FunctionModel> functions = new HashMap();

    public FunctionHandler(String path) {
        this.basePath = path;
        this.loadFromDisk();
    }

    private boolean loadFromDisk() {
        JSONParser parser = new JSONParser();
        JSONObject a = null;
        try {
            a = (JSONObject) parser.parse(new FileReader(this.basePath + "/metadata.json"));
        } catch (ParseException | FileNotFoundException e) {
            e.printStackTrace();
        }
        JSONArray files = (JSONArray) a.get("files");
        for (int i = 0; i < files.size(); i++) {
            JSONObject fileObj = (JSONObject) files.get(i);
            Class cls = this.getClass(this.basePath + "/" + fileObj.getAsString("path"), fileObj.getAsString("name"), fileObj.getAsString("mime"));
            JSONArray fileFunctions = (JSONArray) fileObj.get("functions");
            for (int j = 0; j < fileFunctions.size(); j++) {
                JSONObject functionObj = (JSONObject) fileFunctions.get(j);
                JSONArray parameters = (JSONArray) functionObj.get("parameters");
                Class<?> params[] = new Class[parameters.size()];
                String[] args = new String[parameters.size()];
                for (int k = 0; k < parameters.size(); k++) {
                    JSONObject param = (JSONObject) parameters.get(k);
                    params[k] = this.getParamType(param.getAsString("type"));
                    args[k] = param.getAsString("url");
                }
                FunctionModel fn = null;
                try {
                    fn = new FunctionModel(functionObj.getAsString("url"), cls.getDeclaredMethod(functionObj.getAsString("name"), params), args);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                this.put(fn);
            }
        }
        return true;
    }

    private Class getParamType(String type) {
        switch (type) {
            case "xsd:string":
                return String.class;
            default:
                throw new Error("Couldn't derive type from " + type);
        }
    }

    private boolean put(FunctionModel fn) {
        this.functions.put(fn.getURI(), fn);
        return true;
    }

    public FunctionModel get(String URI) {
        FunctionModel res = null;
        if (this.functions.containsKey(URI)) {
            res = this.functions.get(URI);
        } else {
            // TODO download
        }

        return res;
    }

    private Class getClass(String path, String className, String mime) {
        File sourceFile = new File(path);

        switch (mime) {
            case "text/x-java-source":
                return this.getClassFromJAVA(sourceFile, className);
            case "application/java-archive":
                return this.getClassFromJAR(sourceFile, className);
        }

        return null;
    }

    private Class getClassFromJAVA(File sourceFile, String className) {
        Class<?> cls = null;

        // TODO let's not recompile every time
        // Compile source file.
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int res = compiler.run(null, null, null, sourceFile.getPath());

        if (res != 0) {
            return null;
        }

        // Load and instantiate compiled class.
        URLClassLoader classLoader = null;
        try {
            classLoader = URLClassLoader.newInstance(new URL[]{(new File(this.basePath)).toURI().toURL()});
            cls = Class.forName(className, true, classLoader);
        } catch (MalformedURLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return cls;
    }

    private Class getClassFromJAR(File sourceFile, String className) {
        Class<?> cls = null;

        URLClassLoader child = null;
        try {
            child = URLClassLoader.newInstance(new URL[]{sourceFile.toURI().toURL()});
            cls = Class.forName(className, true, child);
        } catch (MalformedURLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return cls;
    }

}
