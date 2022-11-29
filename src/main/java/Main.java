import org.nd4j.python4j.*;

import java.io.IOException;

import static org.bytedeco.cpython.helper.python.Py_AddPath;

public class Main {
    public static void main(String[] args) {

        String LocalModulePath = System.getProperty("user.dir");
        try {
            Py_AddPath(LocalModulePath);
            Py_AddPath("./my_venv/lib/python3.10/site-packages");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try(PythonGIL pythonGIL = PythonGIL.lock()) {
            try(PythonGC gc = PythonGC.watch()) {
                NLPProcessor nlpp = new NLPProcessor();
                nlpp.processPhrase("This is a test");
                int n = nlpp.getNumberOfTokens();
                System.out.println("the number of tokens are:" + n);
            }
        }
    }
}
