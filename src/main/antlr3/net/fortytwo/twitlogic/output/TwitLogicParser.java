// $ANTLR 3.1.2 /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g 2009-09-06 16:57:59

//import java.util.HashMap;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.debug.*;
import java.io.IOException;
public class TwitLogicParser extends DebugParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SCREEN_NAME", "HASHTAG", "QUOTED_STRING", "WS", "URL"
    };
    public static final int QUOTED_STRING=6;
    public static final int EOF=-1;
    public static final int WS=7;
    public static final int SCREEN_NAME=4;
    public static final int URL=8;
    public static final int HASHTAG=5;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "subject", "predicate", "object", "twiple"
    };
     
        public int ruleLevel = 0;
        public int getRuleLevel() { return ruleLevel; }
        public void incRuleLevel() { ruleLevel++; }
        public void decRuleLevel() { ruleLevel--; }
        public TwitLogicParser(TokenStream input) {
            this(input, DebugEventSocketProxy.DEFAULT_DEBUGGER_PORT, new RecognizerSharedState());
        }
        public TwitLogicParser(TokenStream input, int port, RecognizerSharedState state) {
            super(input, state);
            DebugEventSocketProxy proxy =
                new DebugEventSocketProxy(this, port, null);
            setDebugListener(proxy);
            try {
                proxy.handshake();
            }
            catch (IOException ioe) {
                reportError(ioe);
            }
        }
    public TwitLogicParser(TokenStream input, DebugEventListener dbg) {
        super(input, dbg, new RecognizerSharedState());

    }
    protected boolean evalPredicate(boolean result, String predicate) {
        dbg.semanticPredicate(result, predicate);
        return result;
    }


    public String[] getTokenNames() { return TwitLogicParser.tokenNames; }
    public String getGrammarFileName() { return "/Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g"; }


    /** Map variable name to Integer object holding value */
    //HashMap memory = new HashMap();



    // $ANTLR start "twiple"
    // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:12:1: twiple : subject predicate object ;
    public final void twiple() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "twiple");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(12, 1);

        try {
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:12:8: ( subject predicate object )
            dbg.enterAlt(1);

            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:12:10: subject predicate object
            {
            dbg.location(12,10);
            pushFollow(FOLLOW_subject_in_twiple22);
            subject();

            state._fsp--;

            dbg.location(12,18);
            pushFollow(FOLLOW_predicate_in_twiple24);
            predicate();

            state._fsp--;

            dbg.location(12,28);
            pushFollow(FOLLOW_object_in_twiple26);
            object();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(12, 35);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "twiple");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "twiple"


    // $ANTLR start "subject"
    // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:14:1: subject : ( SCREEN_NAME | HASHTAG );
    public final void subject() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "subject");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(14, 1);

        try {
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:14:9: ( SCREEN_NAME | HASHTAG )
            dbg.enterAlt(1);

            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:
            {
            dbg.location(14,9);
            if ( (input.LA(1)>=SCREEN_NAME && input.LA(1)<=HASHTAG) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(14, 33);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "subject");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "subject"


    // $ANTLR start "predicate"
    // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:16:1: predicate : HASHTAG ;
    public final void predicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "predicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(16, 1);

        try {
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:16:11: ( HASHTAG )
            dbg.enterAlt(1);

            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:16:13: HASHTAG
            {
            dbg.location(16,13);
            match(input,HASHTAG,FOLLOW_HASHTAG_in_predicate50); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(16, 21);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "predicate");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "predicate"


    // $ANTLR start "object"
    // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:18:1: object : ( SCREEN_NAME | HASHTAG | QUOTED_STRING );
    public final void object() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "object");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(18, 1);

        try {
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:18:9: ( SCREEN_NAME | HASHTAG | QUOTED_STRING )
            dbg.enterAlt(1);

            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:
            {
            dbg.location(18,9);
            if ( (input.LA(1)>=SCREEN_NAME && input.LA(1)<=QUOTED_STRING) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(18, 49);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "object");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "object"

    // Delegated rules


 

    public static final BitSet FOLLOW_subject_in_twiple22 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_predicate_in_twiple24 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_object_in_twiple26 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_subject0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASHTAG_in_predicate50 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_object0 = new BitSet(new long[]{0x0000000000000002L});

}