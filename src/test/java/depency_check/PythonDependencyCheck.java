package depency_check;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.nd4j.python4j.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.bytedeco.cpython.helper.python.Py_AddPath;


public class PythonDependencyCheck {
    static Map<String, String> requiredModules;

    @BeforeAll
    static void init(){
        try {
            Py_AddPath("./my_venv/lib/python3.10/site-packages");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        requiredModules = readRequirements("./requirements.txt");
    }

    public static Map<String, String> readRequirements(String requirementsFilePath){
        Map<String, String> requirementsMap = new HashMap<>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    requirementsFilePath));
            String line = reader.readLine();
            while (line != null) {
                String[] module = line.split("==");
                if(module.length > 1) {
                    requirementsMap.put(module[0], module[1]);
                }

                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requirementsMap;
    }

    boolean checkVersionSatisfied(String actualVersion, String minimumVersion){

        String[] actVersionSplit = actualVersion.split("\\.");
        String[] minVersionSplit = minimumVersion.split("\\.");

        boolean satisfied = true;

        if(!(actVersionSplit.length == minVersionSplit.length)){
            throw new IllegalArgumentException();
        }

        for(int i = actVersionSplit.length-1; i >=0; i--){
            boolean higherOrEqual = (Integer.parseInt(actVersionSplit[i]) >= Integer.parseInt(minVersionSplit[i]));
            satisfied = (satisfied && higherOrEqual) || (!satisfied && higherOrEqual);
        }
        return satisfied;
    }

    @Test
    void pythonVersionTest(){

        String minPythonVersion = "3.10.0";
        try(PythonGIL pythonGIL = PythonGIL.lock()) {
            try(PythonGC gc = PythonGC.watch()) {
                PythonVariable out = new PythonVariable<>("version", PythonTypes.STR);
                PythonExecutioner.exec(
                        """
                                import sys
                                version = sys.version.split(' ')[0]
                                """,
                        null, Collections.singletonList(out)
                );
                String version = (String)out.getValue();
                Assertions.assertTrue(checkVersionSatisfied(version, minPythonVersion));
            }
        }
    }

    @Test
    void pythonRequiredModulesAvailableTest(){
        try(PythonGIL pythonGIL = PythonGIL.lock()) {
            try(PythonGC gc = PythonGC.watch()) {
                PythonVariable out = new PythonVariable<>("packages", PythonTypes.DICT);
                PythonExecutioner.exec(
                        """
                                from pip._internal.operations import freeze
                                packages = {x.split('==')[0]:x.split('==')[-1] for x in freeze.freeze()}
                                """,
                        null, Collections.singletonList(out)
                );

                HashMap<String, String> installedModules = (HashMap)out.getValue();
                for(String module: requiredModules.keySet()){
                    Assertions.assertTrue(installedModules.containsKey(module),
                            "the required module \"" + module + "\" could not be found");
                    Assertions.assertTrue(checkVersionSatisfied(installedModules.get(module),
                            requiredModules.get(module)),
                            "the version of "+ module +" ("+installedModules.get(module)+
                                    ") is too low, it must be at least " + requiredModules.get(module));
                }
            }
        }
    }

}
