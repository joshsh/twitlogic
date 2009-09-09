// $ANTLR 3.1.2 /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g 2009-09-06 16:57:59

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class TwitLogicLexer extends Lexer {
    public static final int QUOTED_STRING=6;
    public static final int EOF=-1;
    public static final int WS=7;
    public static final int SCREEN_NAME=4;
    public static final int URL=8;
    public static final int HASHTAG=5;

    // delegates
    // delegators

    public TwitLogicLexer() {;} 
    public TwitLogicLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public TwitLogicLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g"; }

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:52:5: ( ( ' ' | '\\t' | '\\n' | '\\r' )+ )
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:52:9: ( ' ' | '\\t' | '\\n' | '\\r' )+
            {
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:52:9: ( ' ' | '\\t' | '\\n' | '\\r' )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='\t' && LA1_0<='\n')||LA1_0=='\r'||LA1_0==' ') ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);

            skip();

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "HASHTAG"
    public final void mHASHTAG() throws RecognitionException {
        try {
            int _type = HASHTAG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:54:9: ( '#' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '_' )+ )
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:54:10: '#' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '_' )+
            {
            match('#'); 
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:54:13: ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '_' )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='-'||(LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='Z')||LA2_0=='_'||(LA2_0>='a' && LA2_0<='z')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:
            	    {
            	    if ( input.LA(1)=='-'||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HASHTAG"

    // $ANTLR start "SCREEN_NAME"
    public final void mSCREEN_NAME() throws RecognitionException {
        try {
            int _type = SCREEN_NAME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:56:13: ( '@' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '_' )+ )
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:56:14: '@' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '_' )+
            {
            match('@'); 
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:56:17: ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '_' )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='-'||(LA3_0>='0' && LA3_0<='9')||(LA3_0>='A' && LA3_0<='Z')||LA3_0=='_'||(LA3_0>='a' && LA3_0<='z')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:
            	    {
            	    if ( input.LA(1)=='-'||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt3 >= 1 ) break loop3;
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SCREEN_NAME"

    // $ANTLR start "QUOTED_STRING"
    public final void mQUOTED_STRING() throws RecognitionException {
        try {
            int _type = QUOTED_STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:60:15: ( '\\\"' ( . )* '\\\"' )
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:60:17: '\\\"' ( . )* '\\\"'
            {
            match('\"'); 
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:60:22: ( . )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0=='\"') ) {
                    alt4=2;
                }
                else if ( ((LA4_0>='\u0000' && LA4_0<='!')||(LA4_0>='#' && LA4_0<='\uFFFF')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:60:22: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QUOTED_STRING"

    // $ANTLR start "URL"
    public final void mURL() throws RecognitionException {
        try {
            int _type = URL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:64:6: ( ( 'a' .. 'z' )+ '://' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' )+ ( '.' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' ) )* ( '/' ( ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '_' | '#' | '&' | '+' | '.' | '/' | '=' | '?' | '~' )* ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '/' ) )? )? )
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:64:8: ( 'a' .. 'z' )+ '://' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' )+ ( '.' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' ) )* ( '/' ( ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '_' | '#' | '&' | '+' | '.' | '/' | '=' | '?' | '~' )* ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '/' ) )? )?
            {
            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:64:8: ( 'a' .. 'z' )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='a' && LA5_0<='z')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:64:9: 'a' .. 'z'
            	    {
            	    matchRange('a','z'); 

            	    }
            	    break;

            	default :
            	    if ( cnt5 >= 1 ) break loop5;
                        EarlyExitException eee =
                            new EarlyExitException(5, input);
                        throw eee;
                }
                cnt5++;
            } while (true);

            match("://"); 

            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:65:9: ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0=='-'||(LA6_0>='0' && LA6_0<='9')||(LA6_0>='A' && LA6_0<='Z')||(LA6_0>='a' && LA6_0<='z')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:
            	    {
            	    if ( input.LA(1)=='-'||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);

            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:65:43: ( '.' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' ) )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0=='.') ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:65:44: '.' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' )
            	    {
            	    match('.'); 
            	    if ( input.LA(1)=='-'||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);

            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:66:9: ( '/' ( ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '_' | '#' | '&' | '+' | '.' | '/' | '=' | '?' | '~' )* ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '/' ) )? )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='/') ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:66:10: '/' ( ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '_' | '#' | '&' | '+' | '.' | '/' | '=' | '?' | '~' )* ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '/' ) )?
                    {
                    match('/'); 
                    // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:66:14: ( ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '_' | '#' | '&' | '+' | '.' | '/' | '=' | '?' | '~' )* ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '/' ) )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0=='#'||LA9_0=='&'||LA9_0=='+'||(LA9_0>='-' && LA9_0<='9')||LA9_0=='='||LA9_0=='?'||(LA9_0>='A' && LA9_0<='Z')||LA9_0=='_'||(LA9_0>='a' && LA9_0<='z')||LA9_0=='~') ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:67:13: ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '_' | '#' | '&' | '+' | '.' | '/' | '=' | '?' | '~' )* ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '/' )
                            {
                            // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:67:13: ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '-' | '_' | '#' | '&' | '+' | '.' | '/' | '=' | '?' | '~' )*
                            loop8:
                            do {
                                int alt8=2;
                                int LA8_0 = input.LA(1);

                                if ( (LA8_0=='-'||(LA8_0>='/' && LA8_0<='9')||(LA8_0>='A' && LA8_0<='Z')||(LA8_0>='a' && LA8_0<='z')) ) {
                                    int LA8_1 = input.LA(2);

                                    if ( (LA8_1=='#'||LA8_1=='&'||LA8_1=='+'||(LA8_1>='-' && LA8_1<='9')||LA8_1=='='||LA8_1=='?'||(LA8_1>='A' && LA8_1<='Z')||LA8_1=='_'||(LA8_1>='a' && LA8_1<='z')||LA8_1=='~') ) {
                                        alt8=1;
                                    }


                                }
                                else if ( (LA8_0=='#'||LA8_0=='&'||LA8_0=='+'||LA8_0=='.'||LA8_0=='='||LA8_0=='?'||LA8_0=='_'||LA8_0=='~') ) {
                                    alt8=1;
                                }


                                switch (alt8) {
                            	case 1 :
                            	    // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:
                            	    {
                            	    if ( input.LA(1)=='#'||input.LA(1)=='&'||input.LA(1)=='+'||(input.LA(1)>='-' && input.LA(1)<='9')||input.LA(1)=='='||input.LA(1)=='?'||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z')||input.LA(1)=='~' ) {
                            	        input.consume();

                            	    }
                            	    else {
                            	        MismatchedSetException mse = new MismatchedSetException(null,input);
                            	        recover(mse);
                            	        throw mse;}


                            	    }
                            	    break;

                            	default :
                            	    break loop8;
                                }
                            } while (true);

                            if ( input.LA(1)=='-'||(input.LA(1)>='/' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                                input.consume();

                            }
                            else {
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "URL"

    public void mTokens() throws RecognitionException {
        // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:1:8: ( WS | HASHTAG | SCREEN_NAME | QUOTED_STRING | URL )
        int alt11=5;
        switch ( input.LA(1) ) {
        case '\t':
        case '\n':
        case '\r':
        case ' ':
            {
            alt11=1;
            }
            break;
        case '#':
            {
            alt11=2;
            }
            break;
        case '@':
            {
            alt11=3;
            }
            break;
        case '\"':
            {
            alt11=4;
            }
            break;
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
        case 'n':
        case 'o':
        case 'p':
        case 'q':
        case 'r':
        case 's':
        case 't':
        case 'u':
        case 'v':
        case 'w':
        case 'x':
        case 'y':
        case 'z':
            {
            alt11=5;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("", 11, 0, input);

            throw nvae;
        }

        switch (alt11) {
            case 1 :
                // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:1:10: WS
                {
                mWS(); 

                }
                break;
            case 2 :
                // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:1:13: HASHTAG
                {
                mHASHTAG(); 

                }
                break;
            case 3 :
                // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:1:21: SCREEN_NAME
                {
                mSCREEN_NAME(); 

                }
                break;
            case 4 :
                // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:1:33: QUOTED_STRING
                {
                mQUOTED_STRING(); 

                }
                break;
            case 5 :
                // /Users/josh/projects/fortytwo/twitlogic/src/main/antlr3/net/fortytwo/twitlogic/TwitLogic.g:1:47: URL
                {
                mURL(); 

                }
                break;

        }

    }


 

}