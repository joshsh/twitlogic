import java.io.*;
import org.antlr.runtime.*;
import org.antlr.runtime.debug.DebugEventSocketProxy;


public class __Test__ {

    public static void main(String args[]) throws Exception {
        TwitLogicLexer lex = new TwitLogicLexer(new ANTLRFileStream("/Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/output/__Test___input.txt"));
        CommonTokenStream tokens = new CommonTokenStream(lex);

        TwitLogicParser g = new TwitLogicParser(tokens, 49100, null);
        try {
            g.twiple();
        } catch (RecognitionException e) {
            e.printStackTrace();
        }
    }
}