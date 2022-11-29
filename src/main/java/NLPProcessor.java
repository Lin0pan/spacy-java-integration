import org.nd4j.python4j.*;

import java.io.IOException;
import java.util.Collections;

import static org.bytedeco.cpython.helper.python.Py_AddPath;

public class NLPProcessor {

    public NLPProcessor(){
        this.init();
    }

    public void init(){
        PythonExecutioner.exec("import nlpprocessor as nlpp");
        PythonExecutioner.exec("nlpp.load_model()");
    }

    public void processPhrase(String phrase){
        PythonVariable<String> p = new PythonVariable<>("phrase", PythonTypes.STR, phrase);
        PythonExecutioner.exec("nlpp.process_text(phrase)", Collections.singletonList(p), null);
    }

    public int getNumberOfTokens(){
        PythonVariable<Long> out = new PythonVariable<>("n", PythonTypes.INT);
        PythonExecutioner.exec("n = nlpp.get_num_tokens()", null, Collections.singletonList(out));
        return out.getValue().intValue();
    }

}