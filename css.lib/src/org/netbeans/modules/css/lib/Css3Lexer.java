// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2013-03-20 15:40:08

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
public class Css3Lexer extends Lexer {
    public static final int EOF=-1;
    public static final int NAMESPACE_SYM=4;
    public static final int SEMI=5;
    public static final int IDENT=6;
    public static final int STRING=7;
    public static final int URI=8;
    public static final int CHARSET_SYM=9;
    public static final int IMPORT_SYM=10;
    public static final int COMMA=11;
    public static final int MEDIA_SYM=12;
    public static final int LBRACE=13;
    public static final int RBRACE=14;
    public static final int AND=15;
    public static final int ONLY=16;
    public static final int NOT=17;
    public static final int GEN=18;
    public static final int LPAREN=19;
    public static final int COLON=20;
    public static final int RPAREN=21;
    public static final int AT_IDENT=22;
    public static final int WS=23;
    public static final int MOZ_DOCUMENT_SYM=24;
    public static final int MOZ_URL_PREFIX=25;
    public static final int MOZ_DOMAIN=26;
    public static final int MOZ_REGEXP=27;
    public static final int WEBKIT_KEYFRAMES_SYM=28;
    public static final int PERCENTAGE=29;
    public static final int PAGE_SYM=30;
    public static final int COUNTER_STYLE_SYM=31;
    public static final int FONT_FACE_SYM=32;
    public static final int TOPLEFTCORNER_SYM=33;
    public static final int TOPLEFT_SYM=34;
    public static final int TOPCENTER_SYM=35;
    public static final int TOPRIGHT_SYM=36;
    public static final int TOPRIGHTCORNER_SYM=37;
    public static final int BOTTOMLEFTCORNER_SYM=38;
    public static final int BOTTOMLEFT_SYM=39;
    public static final int BOTTOMCENTER_SYM=40;
    public static final int BOTTOMRIGHT_SYM=41;
    public static final int BOTTOMRIGHTCORNER_SYM=42;
    public static final int LEFTTOP_SYM=43;
    public static final int LEFTMIDDLE_SYM=44;
    public static final int LEFTBOTTOM_SYM=45;
    public static final int RIGHTTOP_SYM=46;
    public static final int RIGHTMIDDLE_SYM=47;
    public static final int RIGHTBOTTOM_SYM=48;
    public static final int SOLIDUS=49;
    public static final int PLUS=50;
    public static final int GREATER=51;
    public static final int TILDE=52;
    public static final int MINUS=53;
    public static final int HASH_SYMBOL=54;
    public static final int HASH=55;
    public static final int DOT=56;
    public static final int LBRACKET=57;
    public static final int DCOLON=58;
    public static final int SASS_EXTEND_ONLY_SELECTOR=59;
    public static final int STAR=60;
    public static final int PIPE=61;
    public static final int NAME=62;
    public static final int LESS_AND=63;
    public static final int OPEQ=64;
    public static final int INCLUDES=65;
    public static final int DASHMATCH=66;
    public static final int BEGINS=67;
    public static final int ENDS=68;
    public static final int CONTAINS=69;
    public static final int RBRACKET=70;
    public static final int IMPORTANT_SYM=71;
    public static final int NUMBER=72;
    public static final int LENGTH=73;
    public static final int EMS=74;
    public static final int REM=75;
    public static final int EXS=76;
    public static final int ANGLE=77;
    public static final int TIME=78;
    public static final int FREQ=79;
    public static final int RESOLUTION=80;
    public static final int DIMENSION=81;
    public static final int NL=82;
    public static final int COMMENT=83;
    public static final int SASS_DEFAULT=84;
    public static final int SASS_VAR=85;
    public static final int SASS_MIXIN=86;
    public static final int SASS_INCLUDE=87;
    public static final int LESS_DOTS=88;
    public static final int LESS_REST=89;
    public static final int LESS_WHEN=90;
    public static final int GREATER_OR_EQ=91;
    public static final int LESS=92;
    public static final int LESS_OR_EQ=93;
    public static final int SASS_EXTEND=94;
    public static final int SASS_OPTIONAL=95;
    public static final int SASS_DEBUG=96;
    public static final int SASS_WARN=97;
    public static final int SASS_IF=98;
    public static final int SASS_ELSE=99;
    public static final int OR=100;
    public static final int CP_EQ=101;
    public static final int CP_NOT_EQ=102;
    public static final int SASS_FOR=103;
    public static final int SASS_EACH=104;
    public static final int SASS_WHILE=105;
    public static final int SASS_FUNCTION=106;
    public static final int SASS_RETURN=107;
    public static final int SASS_CONTENT=108;
    public static final int HEXCHAR=109;
    public static final int NONASCII=110;
    public static final int UNICODE=111;
    public static final int ESCAPE=112;
    public static final int NMSTART=113;
    public static final int NMCHAR=114;
    public static final int URL=115;
    public static final int A=116;
    public static final int B=117;
    public static final int C=118;
    public static final int D=119;
    public static final int E=120;
    public static final int F=121;
    public static final int G=122;
    public static final int H=123;
    public static final int I=124;
    public static final int J=125;
    public static final int K=126;
    public static final int L=127;
    public static final int M=128;
    public static final int N=129;
    public static final int O=130;
    public static final int P=131;
    public static final int Q=132;
    public static final int R=133;
    public static final int S=134;
    public static final int T=135;
    public static final int U=136;
    public static final int V=137;
    public static final int W=138;
    public static final int X=139;
    public static final int Y=140;
    public static final int Z=141;
    public static final int CDO=142;
    public static final int CDC=143;
    public static final int INVALID=144;
    public static final int LINE_COMMENT=145;

    // delegates
    // delegators

    public Css3Lexer() {;} 
    public Css3Lexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public Css3Lexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g"; }

    // $ANTLR start "GEN"
    public final void mGEN() throws RecognitionException {
        try {
            int _type = GEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1346:25: ( '@@@' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1346:27: '@@@'
            {
            match("@@@"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GEN"

    // $ANTLR start "HEXCHAR"
    public final void mHEXCHAR() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1348:25: ( ( 'a' .. 'f' | 'A' .. 'F' | '0' .. '9' ) )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1348:27: ( 'a' .. 'f' | 'A' .. 'F' | '0' .. '9' )
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "HEXCHAR"

    // $ANTLR start "NONASCII"
    public final void mNONASCII() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1350:25: ( '\\u0080' .. '\\uFFFF' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1350:27: '\\u0080' .. '\\uFFFF'
            {
            matchRange('\u0080','\uFFFF'); if (state.failed) return ;

            }

        }
        finally {
        }
    }
    // $ANTLR end "NONASCII"

    // $ANTLR start "UNICODE"
    public final void mUNICODE() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1352:25: ( '\\\\' HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )? ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1352:27: '\\\\' HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )? ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
            {
            match('\\'); if (state.failed) return ;
            mHEXCHAR(); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1353:33: ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( ((LA5_0>='0' && LA5_0<='9')||(LA5_0>='A' && LA5_0<='F')||(LA5_0>='a' && LA5_0<='f')) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1353:34: HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )?
                    {
                    mHEXCHAR(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1354:37: ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( ((LA4_0>='0' && LA4_0<='9')||(LA4_0>='A' && LA4_0<='F')||(LA4_0>='a' && LA4_0<='f')) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1354:38: HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )?
                            {
                            mHEXCHAR(); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1355:41: ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )?
                            int alt3=2;
                            int LA3_0 = input.LA(1);

                            if ( ((LA3_0>='0' && LA3_0<='9')||(LA3_0>='A' && LA3_0<='F')||(LA3_0>='a' && LA3_0<='f')) ) {
                                alt3=1;
                            }
                            switch (alt3) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1355:42: HEXCHAR ( HEXCHAR ( HEXCHAR )? )?
                                    {
                                    mHEXCHAR(); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1356:45: ( HEXCHAR ( HEXCHAR )? )?
                                    int alt2=2;
                                    int LA2_0 = input.LA(1);

                                    if ( ((LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='F')||(LA2_0>='a' && LA2_0<='f')) ) {
                                        alt2=1;
                                    }
                                    switch (alt2) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1356:46: HEXCHAR ( HEXCHAR )?
                                            {
                                            mHEXCHAR(); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1356:54: ( HEXCHAR )?
                                            int alt1=2;
                                            int LA1_0 = input.LA(1);

                                            if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='F')||(LA1_0>='a' && LA1_0<='f')) ) {
                                                alt1=1;
                                            }
                                            switch (alt1) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1356:54: HEXCHAR
                                                    {
                                                    mHEXCHAR(); if (state.failed) return ;

                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1360:33: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='\t' && LA6_0<='\n')||(LA6_0>='\f' && LA6_0<='\r')||LA6_0==' ') ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
            	        input.consume();
            	    state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "UNICODE"

    // $ANTLR start "ESCAPE"
    public final void mESCAPE() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1362:25: ( UNICODE | '\\\\' ~ ( '\\r' | '\\n' | '\\f' | HEXCHAR ) )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='\\') ) {
                int LA7_1 = input.LA(2);

                if ( ((LA7_1>='\u0000' && LA7_1<='\t')||LA7_1=='\u000B'||(LA7_1>='\u000E' && LA7_1<='/')||(LA7_1>=':' && LA7_1<='@')||(LA7_1>='G' && LA7_1<='`')||(LA7_1>='g' && LA7_1<='\uFFFF')) ) {
                    alt7=2;
                }
                else if ( ((LA7_1>='0' && LA7_1<='9')||(LA7_1>='A' && LA7_1<='F')||(LA7_1>='a' && LA7_1<='f')) ) {
                    alt7=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1362:27: UNICODE
                    {
                    mUNICODE(); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1362:37: '\\\\' ~ ( '\\r' | '\\n' | '\\f' | HEXCHAR )
                    {
                    match('\\'); if (state.failed) return ;
                    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||input.LA(1)=='\u000B'||(input.LA(1)>='\u000E' && input.LA(1)<='/')||(input.LA(1)>=':' && input.LA(1)<='@')||(input.LA(1)>='G' && input.LA(1)<='`')||(input.LA(1)>='g' && input.LA(1)<='\uFFFF') ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "ESCAPE"

    // $ANTLR start "NMSTART"
    public final void mNMSTART() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1364:25: ( '_' | 'a' .. 'z' | 'A' .. 'Z' | NONASCII | ESCAPE )
            int alt8=5;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='_') ) {
                alt8=1;
            }
            else if ( ((LA8_0>='a' && LA8_0<='z')) ) {
                alt8=2;
            }
            else if ( ((LA8_0>='A' && LA8_0<='Z')) ) {
                alt8=3;
            }
            else if ( ((LA8_0>='\u0080' && LA8_0<='\uFFFF')) ) {
                alt8=4;
            }
            else if ( (LA8_0=='\\') ) {
                alt8=5;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1364:27: '_'
                    {
                    match('_'); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1365:27: 'a' .. 'z'
                    {
                    matchRange('a','z'); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1366:27: 'A' .. 'Z'
                    {
                    matchRange('A','Z'); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1367:27: NONASCII
                    {
                    mNONASCII(); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1368:27: ESCAPE
                    {
                    mESCAPE(); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "NMSTART"

    // $ANTLR start "NMCHAR"
    public final void mNMCHAR() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1371:25: ( '_' | 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '-' | NONASCII | ESCAPE )
            int alt9=7;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='_') ) {
                alt9=1;
            }
            else if ( ((LA9_0>='a' && LA9_0<='z')) ) {
                alt9=2;
            }
            else if ( ((LA9_0>='A' && LA9_0<='Z')) ) {
                alt9=3;
            }
            else if ( ((LA9_0>='0' && LA9_0<='9')) ) {
                alt9=4;
            }
            else if ( (LA9_0=='-') ) {
                alt9=5;
            }
            else if ( ((LA9_0>='\u0080' && LA9_0<='\uFFFF')) ) {
                alt9=6;
            }
            else if ( (LA9_0=='\\') ) {
                alt9=7;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1371:27: '_'
                    {
                    match('_'); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1372:27: 'a' .. 'z'
                    {
                    matchRange('a','z'); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1373:27: 'A' .. 'Z'
                    {
                    matchRange('A','Z'); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1374:27: '0' .. '9'
                    {
                    matchRange('0','9'); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1375:27: '-'
                    {
                    match('-'); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1376:27: NONASCII
                    {
                    mNONASCII(); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1377:27: ESCAPE
                    {
                    mESCAPE(); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "NMCHAR"

    // $ANTLR start "NAME"
    public final void mNAME() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1380:25: ( ( NMCHAR )+ )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1380:27: ( NMCHAR )+
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1380:27: ( NMCHAR )+
            int cnt10=0;
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0=='-'||(LA10_0>='0' && LA10_0<='9')||(LA10_0>='A' && LA10_0<='Z')||LA10_0=='\\'||LA10_0=='_'||(LA10_0>='a' && LA10_0<='z')||(LA10_0>='\u0080' && LA10_0<='\uFFFF')) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1380:27: NMCHAR
            	    {
            	    mNMCHAR(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt10 >= 1 ) break loop10;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(10, input);
                        throw eee;
                }
                cnt10++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "NAME"

    // $ANTLR start "URL"
    public final void mURL() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1382:25: ( ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | NMCHAR )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1382:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | NMCHAR )*
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1382:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | NMCHAR )*
            loop11:
            do {
                int alt11=18;
                alt11 = dfa11.predict(input);
                switch (alt11) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:31: '['
            	    {
            	    match('['); if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:35: '!'
            	    {
            	    match('!'); if (state.failed) return ;

            	    }
            	    break;
            	case 3 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:39: '#'
            	    {
            	    match('#'); if (state.failed) return ;

            	    }
            	    break;
            	case 4 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:43: '$'
            	    {
            	    match('$'); if (state.failed) return ;

            	    }
            	    break;
            	case 5 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:47: '%'
            	    {
            	    match('%'); if (state.failed) return ;

            	    }
            	    break;
            	case 6 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:51: '&'
            	    {
            	    match('&'); if (state.failed) return ;

            	    }
            	    break;
            	case 7 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:55: '*'
            	    {
            	    match('*'); if (state.failed) return ;

            	    }
            	    break;
            	case 8 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:59: '~'
            	    {
            	    match('~'); if (state.failed) return ;

            	    }
            	    break;
            	case 9 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:63: '.'
            	    {
            	    match('.'); if (state.failed) return ;

            	    }
            	    break;
            	case 10 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:67: ':'
            	    {
            	    match(':'); if (state.failed) return ;

            	    }
            	    break;
            	case 11 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:71: '/'
            	    {
            	    match('/'); if (state.failed) return ;

            	    }
            	    break;
            	case 12 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:75: '?'
            	    {
            	    match('?'); if (state.failed) return ;

            	    }
            	    break;
            	case 13 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:79: '='
            	    {
            	    match('='); if (state.failed) return ;

            	    }
            	    break;
            	case 14 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:83: ';'
            	    {
            	    match(';'); if (state.failed) return ;

            	    }
            	    break;
            	case 15 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:87: ','
            	    {
            	    match(','); if (state.failed) return ;

            	    }
            	    break;
            	case 16 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:91: '+'
            	    {
            	    match('+'); if (state.failed) return ;

            	    }
            	    break;
            	case 17 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1384:31: NMCHAR
            	    {
            	    mNMCHAR(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "URL"

    // $ANTLR start "A"
    public final void mA() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1391:17: ( ( 'a' | 'A' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '1' )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0=='A'||LA16_0=='a') ) {
                alt16=1;
            }
            else if ( (LA16_0=='\\') ) {
                alt16=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1391:21: ( 'a' | 'A' )
                    {
                    if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1392:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '1'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1392:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0=='0') ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1392:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1392:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt14=2;
                            int LA14_0 = input.LA(1);

                            if ( (LA14_0=='0') ) {
                                alt14=1;
                            }
                            switch (alt14) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1392:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1392:36: ( '0' ( '0' )? )?
                                    int alt13=2;
                                    int LA13_0 = input.LA(1);

                                    if ( (LA13_0=='0') ) {
                                        alt13=1;
                                    }
                                    switch (alt13) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1392:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1392:41: ( '0' )?
                                            int alt12=2;
                                            int LA12_0 = input.LA(1);

                                            if ( (LA12_0=='0') ) {
                                                alt12=1;
                                            }
                                            switch (alt12) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1392:41: '0'
                                                    {
                                                    match('0'); if (state.failed) return ;

                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }


                            }
                            break;

                    }

                    if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('1'); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "A"

    // $ANTLR start "B"
    public final void mB() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1394:17: ( ( 'b' | 'B' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '2' )
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0=='B'||LA21_0=='b') ) {
                alt21=1;
            }
            else if ( (LA21_0=='\\') ) {
                alt21=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;
            }
            switch (alt21) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1394:21: ( 'b' | 'B' )
                    {
                    if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1395:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '2'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1395:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt20=2;
                    int LA20_0 = input.LA(1);

                    if ( (LA20_0=='0') ) {
                        alt20=1;
                    }
                    switch (alt20) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1395:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1395:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt19=2;
                            int LA19_0 = input.LA(1);

                            if ( (LA19_0=='0') ) {
                                alt19=1;
                            }
                            switch (alt19) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1395:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1395:36: ( '0' ( '0' )? )?
                                    int alt18=2;
                                    int LA18_0 = input.LA(1);

                                    if ( (LA18_0=='0') ) {
                                        alt18=1;
                                    }
                                    switch (alt18) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1395:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1395:41: ( '0' )?
                                            int alt17=2;
                                            int LA17_0 = input.LA(1);

                                            if ( (LA17_0=='0') ) {
                                                alt17=1;
                                            }
                                            switch (alt17) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1395:41: '0'
                                                    {
                                                    match('0'); if (state.failed) return ;

                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }


                            }
                            break;

                    }

                    if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('2'); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "B"

    // $ANTLR start "C"
    public final void mC() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1397:17: ( ( 'c' | 'C' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '3' )
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0=='C'||LA26_0=='c') ) {
                alt26=1;
            }
            else if ( (LA26_0=='\\') ) {
                alt26=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;
            }
            switch (alt26) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1397:21: ( 'c' | 'C' )
                    {
                    if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1398:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '3'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1398:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt25=2;
                    int LA25_0 = input.LA(1);

                    if ( (LA25_0=='0') ) {
                        alt25=1;
                    }
                    switch (alt25) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1398:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1398:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt24=2;
                            int LA24_0 = input.LA(1);

                            if ( (LA24_0=='0') ) {
                                alt24=1;
                            }
                            switch (alt24) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1398:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1398:36: ( '0' ( '0' )? )?
                                    int alt23=2;
                                    int LA23_0 = input.LA(1);

                                    if ( (LA23_0=='0') ) {
                                        alt23=1;
                                    }
                                    switch (alt23) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1398:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1398:41: ( '0' )?
                                            int alt22=2;
                                            int LA22_0 = input.LA(1);

                                            if ( (LA22_0=='0') ) {
                                                alt22=1;
                                            }
                                            switch (alt22) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1398:41: '0'
                                                    {
                                                    match('0'); if (state.failed) return ;

                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }


                            }
                            break;

                    }

                    if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('3'); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "C"

    // $ANTLR start "D"
    public final void mD() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1400:17: ( ( 'd' | 'D' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '4' )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0=='D'||LA31_0=='d') ) {
                alt31=1;
            }
            else if ( (LA31_0=='\\') ) {
                alt31=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;
            }
            switch (alt31) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1400:21: ( 'd' | 'D' )
                    {
                    if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1401:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '4'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1401:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt30=2;
                    int LA30_0 = input.LA(1);

                    if ( (LA30_0=='0') ) {
                        alt30=1;
                    }
                    switch (alt30) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1401:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1401:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt29=2;
                            int LA29_0 = input.LA(1);

                            if ( (LA29_0=='0') ) {
                                alt29=1;
                            }
                            switch (alt29) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1401:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1401:36: ( '0' ( '0' )? )?
                                    int alt28=2;
                                    int LA28_0 = input.LA(1);

                                    if ( (LA28_0=='0') ) {
                                        alt28=1;
                                    }
                                    switch (alt28) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1401:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1401:41: ( '0' )?
                                            int alt27=2;
                                            int LA27_0 = input.LA(1);

                                            if ( (LA27_0=='0') ) {
                                                alt27=1;
                                            }
                                            switch (alt27) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1401:41: '0'
                                                    {
                                                    match('0'); if (state.failed) return ;

                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }


                            }
                            break;

                    }

                    if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('4'); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "D"

    // $ANTLR start "E"
    public final void mE() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1403:17: ( ( 'e' | 'E' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '5' )
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0=='E'||LA36_0=='e') ) {
                alt36=1;
            }
            else if ( (LA36_0=='\\') ) {
                alt36=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;
            }
            switch (alt36) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1403:21: ( 'e' | 'E' )
                    {
                    if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1404:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '5'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1404:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt35=2;
                    int LA35_0 = input.LA(1);

                    if ( (LA35_0=='0') ) {
                        alt35=1;
                    }
                    switch (alt35) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1404:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1404:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt34=2;
                            int LA34_0 = input.LA(1);

                            if ( (LA34_0=='0') ) {
                                alt34=1;
                            }
                            switch (alt34) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1404:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1404:36: ( '0' ( '0' )? )?
                                    int alt33=2;
                                    int LA33_0 = input.LA(1);

                                    if ( (LA33_0=='0') ) {
                                        alt33=1;
                                    }
                                    switch (alt33) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1404:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1404:41: ( '0' )?
                                            int alt32=2;
                                            int LA32_0 = input.LA(1);

                                            if ( (LA32_0=='0') ) {
                                                alt32=1;
                                            }
                                            switch (alt32) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1404:41: '0'
                                                    {
                                                    match('0'); if (state.failed) return ;

                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }


                            }
                            break;

                    }

                    if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('5'); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "E"

    // $ANTLR start "F"
    public final void mF() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1406:17: ( ( 'f' | 'F' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '6' )
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0=='F'||LA41_0=='f') ) {
                alt41=1;
            }
            else if ( (LA41_0=='\\') ) {
                alt41=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;
            }
            switch (alt41) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1406:21: ( 'f' | 'F' )
                    {
                    if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1407:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '6'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1407:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt40=2;
                    int LA40_0 = input.LA(1);

                    if ( (LA40_0=='0') ) {
                        alt40=1;
                    }
                    switch (alt40) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1407:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1407:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt39=2;
                            int LA39_0 = input.LA(1);

                            if ( (LA39_0=='0') ) {
                                alt39=1;
                            }
                            switch (alt39) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1407:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1407:36: ( '0' ( '0' )? )?
                                    int alt38=2;
                                    int LA38_0 = input.LA(1);

                                    if ( (LA38_0=='0') ) {
                                        alt38=1;
                                    }
                                    switch (alt38) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1407:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1407:41: ( '0' )?
                                            int alt37=2;
                                            int LA37_0 = input.LA(1);

                                            if ( (LA37_0=='0') ) {
                                                alt37=1;
                                            }
                                            switch (alt37) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1407:41: '0'
                                                    {
                                                    match('0'); if (state.failed) return ;

                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }


                            }
                            break;

                    }

                    if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('6'); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "F"

    // $ANTLR start "G"
    public final void mG() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1409:17: ( ( 'g' | 'G' ) | '\\\\' ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' ) )
            int alt47=2;
            int LA47_0 = input.LA(1);

            if ( (LA47_0=='G'||LA47_0=='g') ) {
                alt47=1;
            }
            else if ( (LA47_0=='\\') ) {
                alt47=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }
            switch (alt47) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1409:21: ( 'g' | 'G' )
                    {
                    if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1410:21: '\\\\' ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1411:25: ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' )
                    int alt46=3;
                    switch ( input.LA(1) ) {
                    case 'g':
                        {
                        alt46=1;
                        }
                        break;
                    case 'G':
                        {
                        alt46=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt46=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 46, 0, input);

                        throw nvae;
                    }

                    switch (alt46) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1412:31: 'g'
                            {
                            match('g'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1413:31: 'G'
                            {
                            match('G'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7'
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt45=2;
                            int LA45_0 = input.LA(1);

                            if ( (LA45_0=='0') ) {
                                alt45=1;
                            }
                            switch (alt45) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt44=2;
                                    int LA44_0 = input.LA(1);

                                    if ( (LA44_0=='0') ) {
                                        alt44=1;
                                    }
                                    switch (alt44) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:41: ( '0' ( '0' )? )?
                                            int alt43=2;
                                            int LA43_0 = input.LA(1);

                                            if ( (LA43_0=='0') ) {
                                                alt43=1;
                                            }
                                            switch (alt43) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:46: ( '0' )?
                                                    int alt42=2;
                                                    int LA42_0 = input.LA(1);

                                                    if ( (LA42_0=='0') ) {
                                                        alt42=1;
                                                    }
                                                    switch (alt42) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            match('7'); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "G"

    // $ANTLR start "H"
    public final void mH() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1417:17: ( ( 'h' | 'H' ) | '\\\\' ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' ) )
            int alt53=2;
            int LA53_0 = input.LA(1);

            if ( (LA53_0=='H'||LA53_0=='h') ) {
                alt53=1;
            }
            else if ( (LA53_0=='\\') ) {
                alt53=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 53, 0, input);

                throw nvae;
            }
            switch (alt53) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1417:21: ( 'h' | 'H' )
                    {
                    if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1418:19: '\\\\' ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1419:25: ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' )
                    int alt52=3;
                    switch ( input.LA(1) ) {
                    case 'h':
                        {
                        alt52=1;
                        }
                        break;
                    case 'H':
                        {
                        alt52=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt52=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 52, 0, input);

                        throw nvae;
                    }

                    switch (alt52) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1420:31: 'h'
                            {
                            match('h'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1421:31: 'H'
                            {
                            match('H'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1422:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8'
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1422:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt51=2;
                            int LA51_0 = input.LA(1);

                            if ( (LA51_0=='0') ) {
                                alt51=1;
                            }
                            switch (alt51) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1422:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1422:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt50=2;
                                    int LA50_0 = input.LA(1);

                                    if ( (LA50_0=='0') ) {
                                        alt50=1;
                                    }
                                    switch (alt50) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1422:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1422:41: ( '0' ( '0' )? )?
                                            int alt49=2;
                                            int LA49_0 = input.LA(1);

                                            if ( (LA49_0=='0') ) {
                                                alt49=1;
                                            }
                                            switch (alt49) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1422:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1422:46: ( '0' )?
                                                    int alt48=2;
                                                    int LA48_0 = input.LA(1);

                                                    if ( (LA48_0=='0') ) {
                                                        alt48=1;
                                                    }
                                                    switch (alt48) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1422:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            match('8'); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "H"

    // $ANTLR start "I"
    public final void mI() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1425:17: ( ( 'i' | 'I' ) | '\\\\' ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' ) )
            int alt59=2;
            int LA59_0 = input.LA(1);

            if ( (LA59_0=='I'||LA59_0=='i') ) {
                alt59=1;
            }
            else if ( (LA59_0=='\\') ) {
                alt59=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 59, 0, input);

                throw nvae;
            }
            switch (alt59) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1425:21: ( 'i' | 'I' )
                    {
                    if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1426:19: '\\\\' ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1427:25: ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' )
                    int alt58=3;
                    switch ( input.LA(1) ) {
                    case 'i':
                        {
                        alt58=1;
                        }
                        break;
                    case 'I':
                        {
                        alt58=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt58=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 58, 0, input);

                        throw nvae;
                    }

                    switch (alt58) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1428:31: 'i'
                            {
                            match('i'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1429:31: 'I'
                            {
                            match('I'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9'
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt57=2;
                            int LA57_0 = input.LA(1);

                            if ( (LA57_0=='0') ) {
                                alt57=1;
                            }
                            switch (alt57) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt56=2;
                                    int LA56_0 = input.LA(1);

                                    if ( (LA56_0=='0') ) {
                                        alt56=1;
                                    }
                                    switch (alt56) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:41: ( '0' ( '0' )? )?
                                            int alt55=2;
                                            int LA55_0 = input.LA(1);

                                            if ( (LA55_0=='0') ) {
                                                alt55=1;
                                            }
                                            switch (alt55) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:46: ( '0' )?
                                                    int alt54=2;
                                                    int LA54_0 = input.LA(1);

                                                    if ( (LA54_0=='0') ) {
                                                        alt54=1;
                                                    }
                                                    switch (alt54) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            match('9'); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "I"

    // $ANTLR start "J"
    public final void mJ() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1433:17: ( ( 'j' | 'J' ) | '\\\\' ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) ) )
            int alt65=2;
            int LA65_0 = input.LA(1);

            if ( (LA65_0=='J'||LA65_0=='j') ) {
                alt65=1;
            }
            else if ( (LA65_0=='\\') ) {
                alt65=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 65, 0, input);

                throw nvae;
            }
            switch (alt65) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1433:21: ( 'j' | 'J' )
                    {
                    if ( input.LA(1)=='J'||input.LA(1)=='j' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1434:19: '\\\\' ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1435:25: ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) )
                    int alt64=3;
                    switch ( input.LA(1) ) {
                    case 'j':
                        {
                        alt64=1;
                        }
                        break;
                    case 'J':
                        {
                        alt64=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt64=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 64, 0, input);

                        throw nvae;
                    }

                    switch (alt64) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1436:31: 'j'
                            {
                            match('j'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1437:31: 'J'
                            {
                            match('J'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt63=2;
                            int LA63_0 = input.LA(1);

                            if ( (LA63_0=='0') ) {
                                alt63=1;
                            }
                            switch (alt63) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt62=2;
                                    int LA62_0 = input.LA(1);

                                    if ( (LA62_0=='0') ) {
                                        alt62=1;
                                    }
                                    switch (alt62) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:41: ( '0' ( '0' )? )?
                                            int alt61=2;
                                            int LA61_0 = input.LA(1);

                                            if ( (LA61_0=='0') ) {
                                                alt61=1;
                                            }
                                            switch (alt61) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:46: ( '0' )?
                                                    int alt60=2;
                                                    int LA60_0 = input.LA(1);

                                                    if ( (LA60_0=='0') ) {
                                                        alt60=1;
                                                    }
                                                    switch (alt60) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
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
        finally {
        }
    }
    // $ANTLR end "J"

    // $ANTLR start "K"
    public final void mK() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1441:17: ( ( 'k' | 'K' ) | '\\\\' ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) ) )
            int alt71=2;
            int LA71_0 = input.LA(1);

            if ( (LA71_0=='K'||LA71_0=='k') ) {
                alt71=1;
            }
            else if ( (LA71_0=='\\') ) {
                alt71=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 71, 0, input);

                throw nvae;
            }
            switch (alt71) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1441:21: ( 'k' | 'K' )
                    {
                    if ( input.LA(1)=='K'||input.LA(1)=='k' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1442:19: '\\\\' ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1443:25: ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) )
                    int alt70=3;
                    switch ( input.LA(1) ) {
                    case 'k':
                        {
                        alt70=1;
                        }
                        break;
                    case 'K':
                        {
                        alt70=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt70=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 70, 0, input);

                        throw nvae;
                    }

                    switch (alt70) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1444:31: 'k'
                            {
                            match('k'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1445:31: 'K'
                            {
                            match('K'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt69=2;
                            int LA69_0 = input.LA(1);

                            if ( (LA69_0=='0') ) {
                                alt69=1;
                            }
                            switch (alt69) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt68=2;
                                    int LA68_0 = input.LA(1);

                                    if ( (LA68_0=='0') ) {
                                        alt68=1;
                                    }
                                    switch (alt68) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:41: ( '0' ( '0' )? )?
                                            int alt67=2;
                                            int LA67_0 = input.LA(1);

                                            if ( (LA67_0=='0') ) {
                                                alt67=1;
                                            }
                                            switch (alt67) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:46: ( '0' )?
                                                    int alt66=2;
                                                    int LA66_0 = input.LA(1);

                                                    if ( (LA66_0=='0') ) {
                                                        alt66=1;
                                                    }
                                                    switch (alt66) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
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
        finally {
        }
    }
    // $ANTLR end "K"

    // $ANTLR start "L"
    public final void mL() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1449:17: ( ( 'l' | 'L' ) | '\\\\' ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) ) )
            int alt77=2;
            int LA77_0 = input.LA(1);

            if ( (LA77_0=='L'||LA77_0=='l') ) {
                alt77=1;
            }
            else if ( (LA77_0=='\\') ) {
                alt77=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 77, 0, input);

                throw nvae;
            }
            switch (alt77) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1449:21: ( 'l' | 'L' )
                    {
                    if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1450:19: '\\\\' ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1451:25: ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) )
                    int alt76=3;
                    switch ( input.LA(1) ) {
                    case 'l':
                        {
                        alt76=1;
                        }
                        break;
                    case 'L':
                        {
                        alt76=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt76=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 76, 0, input);

                        throw nvae;
                    }

                    switch (alt76) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1452:31: 'l'
                            {
                            match('l'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1453:31: 'L'
                            {
                            match('L'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt75=2;
                            int LA75_0 = input.LA(1);

                            if ( (LA75_0=='0') ) {
                                alt75=1;
                            }
                            switch (alt75) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt74=2;
                                    int LA74_0 = input.LA(1);

                                    if ( (LA74_0=='0') ) {
                                        alt74=1;
                                    }
                                    switch (alt74) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:41: ( '0' ( '0' )? )?
                                            int alt73=2;
                                            int LA73_0 = input.LA(1);

                                            if ( (LA73_0=='0') ) {
                                                alt73=1;
                                            }
                                            switch (alt73) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:46: ( '0' )?
                                                    int alt72=2;
                                                    int LA72_0 = input.LA(1);

                                                    if ( (LA72_0=='0') ) {
                                                        alt72=1;
                                                    }
                                                    switch (alt72) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
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
        finally {
        }
    }
    // $ANTLR end "L"

    // $ANTLR start "M"
    public final void mM() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1457:17: ( ( 'm' | 'M' ) | '\\\\' ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) ) )
            int alt83=2;
            int LA83_0 = input.LA(1);

            if ( (LA83_0=='M'||LA83_0=='m') ) {
                alt83=1;
            }
            else if ( (LA83_0=='\\') ) {
                alt83=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 83, 0, input);

                throw nvae;
            }
            switch (alt83) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1457:21: ( 'm' | 'M' )
                    {
                    if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1458:19: '\\\\' ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1459:25: ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) )
                    int alt82=3;
                    switch ( input.LA(1) ) {
                    case 'm':
                        {
                        alt82=1;
                        }
                        break;
                    case 'M':
                        {
                        alt82=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt82=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 82, 0, input);

                        throw nvae;
                    }

                    switch (alt82) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1460:31: 'm'
                            {
                            match('m'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1461:31: 'M'
                            {
                            match('M'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt81=2;
                            int LA81_0 = input.LA(1);

                            if ( (LA81_0=='0') ) {
                                alt81=1;
                            }
                            switch (alt81) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt80=2;
                                    int LA80_0 = input.LA(1);

                                    if ( (LA80_0=='0') ) {
                                        alt80=1;
                                    }
                                    switch (alt80) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:41: ( '0' ( '0' )? )?
                                            int alt79=2;
                                            int LA79_0 = input.LA(1);

                                            if ( (LA79_0=='0') ) {
                                                alt79=1;
                                            }
                                            switch (alt79) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:46: ( '0' )?
                                                    int alt78=2;
                                                    int LA78_0 = input.LA(1);

                                                    if ( (LA78_0=='0') ) {
                                                        alt78=1;
                                                    }
                                                    switch (alt78) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
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
        finally {
        }
    }
    // $ANTLR end "M"

    // $ANTLR start "N"
    public final void mN() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1465:17: ( ( 'n' | 'N' ) | '\\\\' ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) ) )
            int alt89=2;
            int LA89_0 = input.LA(1);

            if ( (LA89_0=='N'||LA89_0=='n') ) {
                alt89=1;
            }
            else if ( (LA89_0=='\\') ) {
                alt89=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 89, 0, input);

                throw nvae;
            }
            switch (alt89) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1465:21: ( 'n' | 'N' )
                    {
                    if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:19: '\\\\' ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1467:25: ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) )
                    int alt88=3;
                    switch ( input.LA(1) ) {
                    case 'n':
                        {
                        alt88=1;
                        }
                        break;
                    case 'N':
                        {
                        alt88=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt88=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 88, 0, input);

                        throw nvae;
                    }

                    switch (alt88) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1468:31: 'n'
                            {
                            match('n'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1469:31: 'N'
                            {
                            match('N'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt87=2;
                            int LA87_0 = input.LA(1);

                            if ( (LA87_0=='0') ) {
                                alt87=1;
                            }
                            switch (alt87) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt86=2;
                                    int LA86_0 = input.LA(1);

                                    if ( (LA86_0=='0') ) {
                                        alt86=1;
                                    }
                                    switch (alt86) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:41: ( '0' ( '0' )? )?
                                            int alt85=2;
                                            int LA85_0 = input.LA(1);

                                            if ( (LA85_0=='0') ) {
                                                alt85=1;
                                            }
                                            switch (alt85) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:46: ( '0' )?
                                                    int alt84=2;
                                                    int LA84_0 = input.LA(1);

                                                    if ( (LA84_0=='0') ) {
                                                        alt84=1;
                                                    }
                                                    switch (alt84) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
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
        finally {
        }
    }
    // $ANTLR end "N"

    // $ANTLR start "O"
    public final void mO() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1473:17: ( ( 'o' | 'O' ) | '\\\\' ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) ) )
            int alt95=2;
            int LA95_0 = input.LA(1);

            if ( (LA95_0=='O'||LA95_0=='o') ) {
                alt95=1;
            }
            else if ( (LA95_0=='\\') ) {
                alt95=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 95, 0, input);

                throw nvae;
            }
            switch (alt95) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1473:21: ( 'o' | 'O' )
                    {
                    if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1474:19: '\\\\' ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1475:25: ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) )
                    int alt94=3;
                    switch ( input.LA(1) ) {
                    case 'o':
                        {
                        alt94=1;
                        }
                        break;
                    case 'O':
                        {
                        alt94=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt94=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 94, 0, input);

                        throw nvae;
                    }

                    switch (alt94) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1476:31: 'o'
                            {
                            match('o'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1477:31: 'O'
                            {
                            match('O'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt93=2;
                            int LA93_0 = input.LA(1);

                            if ( (LA93_0=='0') ) {
                                alt93=1;
                            }
                            switch (alt93) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt92=2;
                                    int LA92_0 = input.LA(1);

                                    if ( (LA92_0=='0') ) {
                                        alt92=1;
                                    }
                                    switch (alt92) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:41: ( '0' ( '0' )? )?
                                            int alt91=2;
                                            int LA91_0 = input.LA(1);

                                            if ( (LA91_0=='0') ) {
                                                alt91=1;
                                            }
                                            switch (alt91) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:46: ( '0' )?
                                                    int alt90=2;
                                                    int LA90_0 = input.LA(1);

                                                    if ( (LA90_0=='0') ) {
                                                        alt90=1;
                                                    }
                                                    switch (alt90) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
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
        finally {
        }
    }
    // $ANTLR end "O"

    // $ANTLR start "P"
    public final void mP() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1481:17: ( ( 'p' | 'P' ) | '\\\\' ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) ) )
            int alt101=2;
            int LA101_0 = input.LA(1);

            if ( (LA101_0=='P'||LA101_0=='p') ) {
                alt101=1;
            }
            else if ( (LA101_0=='\\') ) {
                alt101=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 101, 0, input);

                throw nvae;
            }
            switch (alt101) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1481:21: ( 'p' | 'P' )
                    {
                    if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1482:19: '\\\\' ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1483:25: ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) )
                    int alt100=3;
                    switch ( input.LA(1) ) {
                    case 'p':
                        {
                        alt100=1;
                        }
                        break;
                    case 'P':
                        {
                        alt100=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt100=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 100, 0, input);

                        throw nvae;
                    }

                    switch (alt100) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1484:31: 'p'
                            {
                            match('p'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1485:31: 'P'
                            {
                            match('P'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt99=2;
                            int LA99_0 = input.LA(1);

                            if ( (LA99_0=='0') ) {
                                alt99=1;
                            }
                            switch (alt99) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt98=2;
                                    int LA98_0 = input.LA(1);

                                    if ( (LA98_0=='0') ) {
                                        alt98=1;
                                    }
                                    switch (alt98) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:41: ( '0' ( '0' )? )?
                                            int alt97=2;
                                            int LA97_0 = input.LA(1);

                                            if ( (LA97_0=='0') ) {
                                                alt97=1;
                                            }
                                            switch (alt97) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:46: ( '0' )?
                                                    int alt96=2;
                                                    int LA96_0 = input.LA(1);

                                                    if ( (LA96_0=='0') ) {
                                                        alt96=1;
                                                    }
                                                    switch (alt96) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:66: ( '0' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:67: '0'
                            {
                            match('0'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "P"

    // $ANTLR start "Q"
    public final void mQ() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1489:17: ( ( 'q' | 'Q' ) | '\\\\' ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) ) )
            int alt107=2;
            int LA107_0 = input.LA(1);

            if ( (LA107_0=='Q'||LA107_0=='q') ) {
                alt107=1;
            }
            else if ( (LA107_0=='\\') ) {
                alt107=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 107, 0, input);

                throw nvae;
            }
            switch (alt107) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1489:21: ( 'q' | 'Q' )
                    {
                    if ( input.LA(1)=='Q'||input.LA(1)=='q' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1490:19: '\\\\' ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1491:25: ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) )
                    int alt106=3;
                    switch ( input.LA(1) ) {
                    case 'q':
                        {
                        alt106=1;
                        }
                        break;
                    case 'Q':
                        {
                        alt106=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt106=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 106, 0, input);

                        throw nvae;
                    }

                    switch (alt106) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1492:31: 'q'
                            {
                            match('q'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1493:31: 'Q'
                            {
                            match('Q'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt105=2;
                            int LA105_0 = input.LA(1);

                            if ( (LA105_0=='0') ) {
                                alt105=1;
                            }
                            switch (alt105) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt104=2;
                                    int LA104_0 = input.LA(1);

                                    if ( (LA104_0=='0') ) {
                                        alt104=1;
                                    }
                                    switch (alt104) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:41: ( '0' ( '0' )? )?
                                            int alt103=2;
                                            int LA103_0 = input.LA(1);

                                            if ( (LA103_0=='0') ) {
                                                alt103=1;
                                            }
                                            switch (alt103) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:46: ( '0' )?
                                                    int alt102=2;
                                                    int LA102_0 = input.LA(1);

                                                    if ( (LA102_0=='0') ) {
                                                        alt102=1;
                                                    }
                                                    switch (alt102) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:66: ( '1' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:67: '1'
                            {
                            match('1'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "Q"

    // $ANTLR start "R"
    public final void mR() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1497:17: ( ( 'r' | 'R' ) | '\\\\' ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) ) )
            int alt113=2;
            int LA113_0 = input.LA(1);

            if ( (LA113_0=='R'||LA113_0=='r') ) {
                alt113=1;
            }
            else if ( (LA113_0=='\\') ) {
                alt113=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 113, 0, input);

                throw nvae;
            }
            switch (alt113) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1497:21: ( 'r' | 'R' )
                    {
                    if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1498:19: '\\\\' ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1499:25: ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) )
                    int alt112=3;
                    switch ( input.LA(1) ) {
                    case 'r':
                        {
                        alt112=1;
                        }
                        break;
                    case 'R':
                        {
                        alt112=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt112=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 112, 0, input);

                        throw nvae;
                    }

                    switch (alt112) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1500:31: 'r'
                            {
                            match('r'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1501:31: 'R'
                            {
                            match('R'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt111=2;
                            int LA111_0 = input.LA(1);

                            if ( (LA111_0=='0') ) {
                                alt111=1;
                            }
                            switch (alt111) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt110=2;
                                    int LA110_0 = input.LA(1);

                                    if ( (LA110_0=='0') ) {
                                        alt110=1;
                                    }
                                    switch (alt110) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:41: ( '0' ( '0' )? )?
                                            int alt109=2;
                                            int LA109_0 = input.LA(1);

                                            if ( (LA109_0=='0') ) {
                                                alt109=1;
                                            }
                                            switch (alt109) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:46: ( '0' )?
                                                    int alt108=2;
                                                    int LA108_0 = input.LA(1);

                                                    if ( (LA108_0=='0') ) {
                                                        alt108=1;
                                                    }
                                                    switch (alt108) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:66: ( '2' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:67: '2'
                            {
                            match('2'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "R"

    // $ANTLR start "S"
    public final void mS() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1505:17: ( ( 's' | 'S' ) | '\\\\' ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) ) )
            int alt119=2;
            int LA119_0 = input.LA(1);

            if ( (LA119_0=='S'||LA119_0=='s') ) {
                alt119=1;
            }
            else if ( (LA119_0=='\\') ) {
                alt119=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 119, 0, input);

                throw nvae;
            }
            switch (alt119) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1505:21: ( 's' | 'S' )
                    {
                    if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1506:19: '\\\\' ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1507:25: ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) )
                    int alt118=3;
                    switch ( input.LA(1) ) {
                    case 's':
                        {
                        alt118=1;
                        }
                        break;
                    case 'S':
                        {
                        alt118=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt118=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 118, 0, input);

                        throw nvae;
                    }

                    switch (alt118) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1508:31: 's'
                            {
                            match('s'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1509:31: 'S'
                            {
                            match('S'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt117=2;
                            int LA117_0 = input.LA(1);

                            if ( (LA117_0=='0') ) {
                                alt117=1;
                            }
                            switch (alt117) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt116=2;
                                    int LA116_0 = input.LA(1);

                                    if ( (LA116_0=='0') ) {
                                        alt116=1;
                                    }
                                    switch (alt116) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:41: ( '0' ( '0' )? )?
                                            int alt115=2;
                                            int LA115_0 = input.LA(1);

                                            if ( (LA115_0=='0') ) {
                                                alt115=1;
                                            }
                                            switch (alt115) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:46: ( '0' )?
                                                    int alt114=2;
                                                    int LA114_0 = input.LA(1);

                                                    if ( (LA114_0=='0') ) {
                                                        alt114=1;
                                                    }
                                                    switch (alt114) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:66: ( '3' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:67: '3'
                            {
                            match('3'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "S"

    // $ANTLR start "T"
    public final void mT() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1513:17: ( ( 't' | 'T' ) | '\\\\' ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) ) )
            int alt125=2;
            int LA125_0 = input.LA(1);

            if ( (LA125_0=='T'||LA125_0=='t') ) {
                alt125=1;
            }
            else if ( (LA125_0=='\\') ) {
                alt125=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 125, 0, input);

                throw nvae;
            }
            switch (alt125) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1513:21: ( 't' | 'T' )
                    {
                    if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1514:19: '\\\\' ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1515:25: ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) )
                    int alt124=3;
                    switch ( input.LA(1) ) {
                    case 't':
                        {
                        alt124=1;
                        }
                        break;
                    case 'T':
                        {
                        alt124=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt124=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 124, 0, input);

                        throw nvae;
                    }

                    switch (alt124) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1516:31: 't'
                            {
                            match('t'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1517:31: 'T'
                            {
                            match('T'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt123=2;
                            int LA123_0 = input.LA(1);

                            if ( (LA123_0=='0') ) {
                                alt123=1;
                            }
                            switch (alt123) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt122=2;
                                    int LA122_0 = input.LA(1);

                                    if ( (LA122_0=='0') ) {
                                        alt122=1;
                                    }
                                    switch (alt122) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:41: ( '0' ( '0' )? )?
                                            int alt121=2;
                                            int LA121_0 = input.LA(1);

                                            if ( (LA121_0=='0') ) {
                                                alt121=1;
                                            }
                                            switch (alt121) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:46: ( '0' )?
                                                    int alt120=2;
                                                    int LA120_0 = input.LA(1);

                                                    if ( (LA120_0=='0') ) {
                                                        alt120=1;
                                                    }
                                                    switch (alt120) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:66: ( '4' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:67: '4'
                            {
                            match('4'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "T"

    // $ANTLR start "U"
    public final void mU() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1521:17: ( ( 'u' | 'U' ) | '\\\\' ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) ) )
            int alt131=2;
            int LA131_0 = input.LA(1);

            if ( (LA131_0=='U'||LA131_0=='u') ) {
                alt131=1;
            }
            else if ( (LA131_0=='\\') ) {
                alt131=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 131, 0, input);

                throw nvae;
            }
            switch (alt131) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1521:21: ( 'u' | 'U' )
                    {
                    if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1522:19: '\\\\' ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1523:25: ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) )
                    int alt130=3;
                    switch ( input.LA(1) ) {
                    case 'u':
                        {
                        alt130=1;
                        }
                        break;
                    case 'U':
                        {
                        alt130=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt130=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 130, 0, input);

                        throw nvae;
                    }

                    switch (alt130) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1524:31: 'u'
                            {
                            match('u'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1525:31: 'U'
                            {
                            match('U'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt129=2;
                            int LA129_0 = input.LA(1);

                            if ( (LA129_0=='0') ) {
                                alt129=1;
                            }
                            switch (alt129) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt128=2;
                                    int LA128_0 = input.LA(1);

                                    if ( (LA128_0=='0') ) {
                                        alt128=1;
                                    }
                                    switch (alt128) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:41: ( '0' ( '0' )? )?
                                            int alt127=2;
                                            int LA127_0 = input.LA(1);

                                            if ( (LA127_0=='0') ) {
                                                alt127=1;
                                            }
                                            switch (alt127) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:46: ( '0' )?
                                                    int alt126=2;
                                                    int LA126_0 = input.LA(1);

                                                    if ( (LA126_0=='0') ) {
                                                        alt126=1;
                                                    }
                                                    switch (alt126) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:66: ( '5' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:67: '5'
                            {
                            match('5'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "U"

    // $ANTLR start "V"
    public final void mV() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1529:17: ( ( 'v' | 'V' ) | '\\\\' ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) ) )
            int alt137=2;
            int LA137_0 = input.LA(1);

            if ( (LA137_0=='V'||LA137_0=='v') ) {
                alt137=1;
            }
            else if ( (LA137_0=='\\') ) {
                alt137=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 137, 0, input);

                throw nvae;
            }
            switch (alt137) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1529:21: ( 'v' | 'V' )
                    {
                    if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1530:19: '\\\\' ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1531:25: ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) )
                    int alt136=3;
                    switch ( input.LA(1) ) {
                    case 'v':
                        {
                        alt136=1;
                        }
                        break;
                    case 'V':
                        {
                        alt136=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt136=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 136, 0, input);

                        throw nvae;
                    }

                    switch (alt136) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1531:31: 'v'
                            {
                            match('v'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1532:31: 'V'
                            {
                            match('V'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1533:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1533:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt135=2;
                            int LA135_0 = input.LA(1);

                            if ( (LA135_0=='0') ) {
                                alt135=1;
                            }
                            switch (alt135) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1533:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1533:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt134=2;
                                    int LA134_0 = input.LA(1);

                                    if ( (LA134_0=='0') ) {
                                        alt134=1;
                                    }
                                    switch (alt134) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1533:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1533:41: ( '0' ( '0' )? )?
                                            int alt133=2;
                                            int LA133_0 = input.LA(1);

                                            if ( (LA133_0=='0') ) {
                                                alt133=1;
                                            }
                                            switch (alt133) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1533:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1533:46: ( '0' )?
                                                    int alt132=2;
                                                    int LA132_0 = input.LA(1);

                                                    if ( (LA132_0=='0') ) {
                                                        alt132=1;
                                                    }
                                                    switch (alt132) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1533:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1533:66: ( '6' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1533:67: '6'
                            {
                            match('6'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "V"

    // $ANTLR start "W"
    public final void mW() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1536:17: ( ( 'w' | 'W' ) | '\\\\' ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) ) )
            int alt143=2;
            int LA143_0 = input.LA(1);

            if ( (LA143_0=='W'||LA143_0=='w') ) {
                alt143=1;
            }
            else if ( (LA143_0=='\\') ) {
                alt143=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 143, 0, input);

                throw nvae;
            }
            switch (alt143) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1536:21: ( 'w' | 'W' )
                    {
                    if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1537:19: '\\\\' ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1538:25: ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) )
                    int alt142=3;
                    switch ( input.LA(1) ) {
                    case 'w':
                        {
                        alt142=1;
                        }
                        break;
                    case 'W':
                        {
                        alt142=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt142=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 142, 0, input);

                        throw nvae;
                    }

                    switch (alt142) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1539:31: 'w'
                            {
                            match('w'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1540:31: 'W'
                            {
                            match('W'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1541:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1541:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt141=2;
                            int LA141_0 = input.LA(1);

                            if ( (LA141_0=='0') ) {
                                alt141=1;
                            }
                            switch (alt141) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1541:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1541:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt140=2;
                                    int LA140_0 = input.LA(1);

                                    if ( (LA140_0=='0') ) {
                                        alt140=1;
                                    }
                                    switch (alt140) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1541:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1541:41: ( '0' ( '0' )? )?
                                            int alt139=2;
                                            int LA139_0 = input.LA(1);

                                            if ( (LA139_0=='0') ) {
                                                alt139=1;
                                            }
                                            switch (alt139) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1541:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1541:46: ( '0' )?
                                                    int alt138=2;
                                                    int LA138_0 = input.LA(1);

                                                    if ( (LA138_0=='0') ) {
                                                        alt138=1;
                                                    }
                                                    switch (alt138) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1541:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1541:66: ( '7' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1541:67: '7'
                            {
                            match('7'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "W"

    // $ANTLR start "X"
    public final void mX() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1544:17: ( ( 'x' | 'X' ) | '\\\\' ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) ) )
            int alt149=2;
            int LA149_0 = input.LA(1);

            if ( (LA149_0=='X'||LA149_0=='x') ) {
                alt149=1;
            }
            else if ( (LA149_0=='\\') ) {
                alt149=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 149, 0, input);

                throw nvae;
            }
            switch (alt149) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1544:21: ( 'x' | 'X' )
                    {
                    if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1545:19: '\\\\' ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1546:25: ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) )
                    int alt148=3;
                    switch ( input.LA(1) ) {
                    case 'x':
                        {
                        alt148=1;
                        }
                        break;
                    case 'X':
                        {
                        alt148=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt148=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 148, 0, input);

                        throw nvae;
                    }

                    switch (alt148) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1547:31: 'x'
                            {
                            match('x'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1548:31: 'X'
                            {
                            match('X'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt147=2;
                            int LA147_0 = input.LA(1);

                            if ( (LA147_0=='0') ) {
                                alt147=1;
                            }
                            switch (alt147) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt146=2;
                                    int LA146_0 = input.LA(1);

                                    if ( (LA146_0=='0') ) {
                                        alt146=1;
                                    }
                                    switch (alt146) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:41: ( '0' ( '0' )? )?
                                            int alt145=2;
                                            int LA145_0 = input.LA(1);

                                            if ( (LA145_0=='0') ) {
                                                alt145=1;
                                            }
                                            switch (alt145) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:46: ( '0' )?
                                                    int alt144=2;
                                                    int LA144_0 = input.LA(1);

                                                    if ( (LA144_0=='0') ) {
                                                        alt144=1;
                                                    }
                                                    switch (alt144) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:66: ( '8' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:67: '8'
                            {
                            match('8'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "X"

    // $ANTLR start "Y"
    public final void mY() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1552:17: ( ( 'y' | 'Y' ) | '\\\\' ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) ) )
            int alt155=2;
            int LA155_0 = input.LA(1);

            if ( (LA155_0=='Y'||LA155_0=='y') ) {
                alt155=1;
            }
            else if ( (LA155_0=='\\') ) {
                alt155=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 155, 0, input);

                throw nvae;
            }
            switch (alt155) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1552:21: ( 'y' | 'Y' )
                    {
                    if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1553:19: '\\\\' ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1554:25: ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) )
                    int alt154=3;
                    switch ( input.LA(1) ) {
                    case 'y':
                        {
                        alt154=1;
                        }
                        break;
                    case 'Y':
                        {
                        alt154=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt154=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 154, 0, input);

                        throw nvae;
                    }

                    switch (alt154) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1555:31: 'y'
                            {
                            match('y'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1556:31: 'Y'
                            {
                            match('Y'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt153=2;
                            int LA153_0 = input.LA(1);

                            if ( (LA153_0=='0') ) {
                                alt153=1;
                            }
                            switch (alt153) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt152=2;
                                    int LA152_0 = input.LA(1);

                                    if ( (LA152_0=='0') ) {
                                        alt152=1;
                                    }
                                    switch (alt152) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:41: ( '0' ( '0' )? )?
                                            int alt151=2;
                                            int LA151_0 = input.LA(1);

                                            if ( (LA151_0=='0') ) {
                                                alt151=1;
                                            }
                                            switch (alt151) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:46: ( '0' )?
                                                    int alt150=2;
                                                    int LA150_0 = input.LA(1);

                                                    if ( (LA150_0=='0') ) {
                                                        alt150=1;
                                                    }
                                                    switch (alt150) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:66: ( '9' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:67: '9'
                            {
                            match('9'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "Y"

    // $ANTLR start "Z"
    public final void mZ() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1560:17: ( ( 'z' | 'Z' ) | '\\\\' ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) ) )
            int alt161=2;
            int LA161_0 = input.LA(1);

            if ( (LA161_0=='Z'||LA161_0=='z') ) {
                alt161=1;
            }
            else if ( (LA161_0=='\\') ) {
                alt161=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 161, 0, input);

                throw nvae;
            }
            switch (alt161) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1560:21: ( 'z' | 'Z' )
                    {
                    if ( input.LA(1)=='Z'||input.LA(1)=='z' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1561:19: '\\\\' ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1562:25: ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) )
                    int alt160=3;
                    switch ( input.LA(1) ) {
                    case 'z':
                        {
                        alt160=1;
                        }
                        break;
                    case 'Z':
                        {
                        alt160=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt160=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 160, 0, input);

                        throw nvae;
                    }

                    switch (alt160) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1563:31: 'z'
                            {
                            match('z'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1564:31: 'Z'
                            {
                            match('Z'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt159=2;
                            int LA159_0 = input.LA(1);

                            if ( (LA159_0=='0') ) {
                                alt159=1;
                            }
                            switch (alt159) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt158=2;
                                    int LA158_0 = input.LA(1);

                                    if ( (LA158_0=='0') ) {
                                        alt158=1;
                                    }
                                    switch (alt158) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:41: ( '0' ( '0' )? )?
                                            int alt157=2;
                                            int LA157_0 = input.LA(1);

                                            if ( (LA157_0=='0') ) {
                                                alt157=1;
                                            }
                                            switch (alt157) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:46: ( '0' )?
                                                    int alt156=2;
                                                    int LA156_0 = input.LA(1);

                                                    if ( (LA156_0=='0') ) {
                                                        alt156=1;
                                                    }
                                                    switch (alt156) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

                                                            }
                                                            break;

                                                    }


                                                    }
                                                    break;

                                            }


                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }

                            if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
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
        finally {
        }
    }
    // $ANTLR end "Z"

    // $ANTLR start "CDO"
    public final void mCDO() throws RecognitionException {
        try {
            int _type = CDO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1577:17: ( '<!--' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1577:19: '<!--'
            {
            match("<!--"); if (state.failed) return ;

            if ( state.backtracking==0 ) {

                                      _channel = 3;   // CDO on channel 3 in case we want it later
                                  
            }

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CDO"

    // $ANTLR start "CDC"
    public final void mCDC() throws RecognitionException {
        try {
            int _type = CDC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1590:17: ( '-->' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1590:19: '-->'
            {
            match("-->"); if (state.failed) return ;

            if ( state.backtracking==0 ) {

                                      _channel = 4;   // CDC on channel 4 in case we want it later
                                  
            }

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CDC"

    // $ANTLR start "INCLUDES"
    public final void mINCLUDES() throws RecognitionException {
        try {
            int _type = INCLUDES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1597:17: ( '~=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1597:19: '~='
            {
            match("~="); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INCLUDES"

    // $ANTLR start "DASHMATCH"
    public final void mDASHMATCH() throws RecognitionException {
        try {
            int _type = DASHMATCH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1598:17: ( '|=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1598:19: '|='
            {
            match("|="); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DASHMATCH"

    // $ANTLR start "BEGINS"
    public final void mBEGINS() throws RecognitionException {
        try {
            int _type = BEGINS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1599:17: ( '^=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1599:19: '^='
            {
            match("^="); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BEGINS"

    // $ANTLR start "ENDS"
    public final void mENDS() throws RecognitionException {
        try {
            int _type = ENDS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1600:17: ( '$=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1600:19: '$='
            {
            match("$="); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ENDS"

    // $ANTLR start "CONTAINS"
    public final void mCONTAINS() throws RecognitionException {
        try {
            int _type = CONTAINS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1601:17: ( '*=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1601:19: '*='
            {
            match("*="); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CONTAINS"

    // $ANTLR start "GREATER"
    public final void mGREATER() throws RecognitionException {
        try {
            int _type = GREATER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1603:17: ( '>' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1603:19: '>'
            {
            match('>'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATER"

    // $ANTLR start "LBRACE"
    public final void mLBRACE() throws RecognitionException {
        try {
            int _type = LBRACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1604:17: ( '{' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1604:19: '{'
            {
            match('{'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LBRACE"

    // $ANTLR start "RBRACE"
    public final void mRBRACE() throws RecognitionException {
        try {
            int _type = RBRACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1605:17: ( '}' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1605:19: '}'
            {
            match('}'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RBRACE"

    // $ANTLR start "LBRACKET"
    public final void mLBRACKET() throws RecognitionException {
        try {
            int _type = LBRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1606:17: ( '[' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1606:19: '['
            {
            match('['); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LBRACKET"

    // $ANTLR start "RBRACKET"
    public final void mRBRACKET() throws RecognitionException {
        try {
            int _type = RBRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1607:17: ( ']' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1607:19: ']'
            {
            match(']'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RBRACKET"

    // $ANTLR start "OPEQ"
    public final void mOPEQ() throws RecognitionException {
        try {
            int _type = OPEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1608:17: ( '=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1608:19: '='
            {
            match('='); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OPEQ"

    // $ANTLR start "SEMI"
    public final void mSEMI() throws RecognitionException {
        try {
            int _type = SEMI;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1609:17: ( ';' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1609:19: ';'
            {
            match(';'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SEMI"

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1610:17: ( ':' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1610:19: ':'
            {
            match(':'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COLON"

    // $ANTLR start "DCOLON"
    public final void mDCOLON() throws RecognitionException {
        try {
            int _type = DCOLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1611:17: ( '::' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1611:19: '::'
            {
            match("::"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DCOLON"

    // $ANTLR start "SOLIDUS"
    public final void mSOLIDUS() throws RecognitionException {
        try {
            int _type = SOLIDUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1612:17: ( '/' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1612:19: '/'
            {
            match('/'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SOLIDUS"

    // $ANTLR start "MINUS"
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1613:17: ( '-' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1613:19: '-'
            {
            match('-'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINUS"

    // $ANTLR start "PLUS"
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1614:17: ( '+' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1614:19: '+'
            {
            match('+'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "STAR"
    public final void mSTAR() throws RecognitionException {
        try {
            int _type = STAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1615:17: ( '*' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1615:19: '*'
            {
            match('*'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STAR"

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1616:17: ( '(' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1616:19: '('
            {
            match('('); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LPAREN"

    // $ANTLR start "RPAREN"
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1617:17: ( ')' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1617:19: ')'
            {
            match(')'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RPAREN"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1618:17: ( ',' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1618:19: ','
            {
            match(','); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1619:17: ( '.' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1619:19: '.'
            {
            match('.'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOT"

    // $ANTLR start "TILDE"
    public final void mTILDE() throws RecognitionException {
        try {
            int _type = TILDE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1620:8: ( '~' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1620:10: '~'
            {
            match('~'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TILDE"

    // $ANTLR start "PIPE"
    public final void mPIPE() throws RecognitionException {
        try {
            int _type = PIPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1621:17: ( '|' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1621:19: '|'
            {
            match('|'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PIPE"

    // $ANTLR start "CP_EQ"
    public final void mCP_EQ() throws RecognitionException {
        try {
            int _type = CP_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1623:17: ( '==' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1623:19: '=='
            {
            match("=="); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CP_EQ"

    // $ANTLR start "CP_NOT_EQ"
    public final void mCP_NOT_EQ() throws RecognitionException {
        try {
            int _type = CP_NOT_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1624:17: ( '!=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1624:19: '!='
            {
            match("!="); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CP_NOT_EQ"

    // $ANTLR start "LESS"
    public final void mLESS() throws RecognitionException {
        try {
            int _type = LESS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1625:17: ( '<' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1625:19: '<'
            {
            match('<'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESS"

    // $ANTLR start "GREATER_OR_EQ"
    public final void mGREATER_OR_EQ() throws RecognitionException {
        try {
            int _type = GREATER_OR_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1626:17: ( '>=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1626:19: '>='
            {
            match(">="); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATER_OR_EQ"

    // $ANTLR start "LESS_OR_EQ"
    public final void mLESS_OR_EQ() throws RecognitionException {
        try {
            int _type = LESS_OR_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1627:17: ( '=<' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1627:19: '=<'
            {
            match("=<"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESS_OR_EQ"

    // $ANTLR start "LESS_WHEN"
    public final void mLESS_WHEN() throws RecognitionException {
        try {
            int _type = LESS_WHEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1628:17: ( 'WHEN' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1628:19: 'WHEN'
            {
            match("WHEN"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESS_WHEN"

    // $ANTLR start "LESS_AND"
    public final void mLESS_AND() throws RecognitionException {
        try {
            int _type = LESS_AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1629:17: ( '&' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1629:19: '&'
            {
            match('&'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESS_AND"

    // $ANTLR start "LESS_DOTS"
    public final void mLESS_DOTS() throws RecognitionException {
        try {
            int _type = LESS_DOTS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1630:17: ( '...' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1630:19: '...'
            {
            match("..."); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESS_DOTS"

    // $ANTLR start "LESS_REST"
    public final void mLESS_REST() throws RecognitionException {
        try {
            int _type = LESS_REST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1631:17: ( '@rest...' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1631:19: '@rest...'
            {
            match("@rest..."); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESS_REST"

    // $ANTLR start "INVALID"
    public final void mINVALID() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1636:21: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1636:22: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "INVALID"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1637:17: ( '\\'' (~ ( '\\n' | '\\r' | '\\f' | '\\'' ) )* ( '\\'' | ) | '\"' (~ ( '\\n' | '\\r' | '\\f' | '\"' ) )* ( '\"' | ) )
            int alt166=2;
            int LA166_0 = input.LA(1);

            if ( (LA166_0=='\'') ) {
                alt166=1;
            }
            else if ( (LA166_0=='\"') ) {
                alt166=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 166, 0, input);

                throw nvae;
            }
            switch (alt166) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1637:19: '\\'' (~ ( '\\n' | '\\r' | '\\f' | '\\'' ) )* ( '\\'' | )
                    {
                    match('\''); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1637:24: (~ ( '\\n' | '\\r' | '\\f' | '\\'' ) )*
                    loop162:
                    do {
                        int alt162=2;
                        int LA162_0 = input.LA(1);

                        if ( ((LA162_0>='\u0000' && LA162_0<='\t')||LA162_0=='\u000B'||(LA162_0>='\u000E' && LA162_0<='&')||(LA162_0>='(' && LA162_0<='\uFFFF')) ) {
                            alt162=1;
                        }


                        switch (alt162) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1637:26: ~ ( '\\n' | '\\r' | '\\f' | '\\'' )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||input.LA(1)=='\u000B'||(input.LA(1)>='\u000E' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop162;
                        }
                    } while (true);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1638:21: ( '\\'' | )
                    int alt163=2;
                    int LA163_0 = input.LA(1);

                    if ( (LA163_0=='\'') ) {
                        alt163=1;
                    }
                    else {
                        alt163=2;}
                    switch (alt163) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1639:27: '\\''
                            {
                            match('\''); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1640:27: 
                            {
                            if ( state.backtracking==0 ) {
                               _type = INVALID; 
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1643:19: '\"' (~ ( '\\n' | '\\r' | '\\f' | '\"' ) )* ( '\"' | )
                    {
                    match('\"'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1643:23: (~ ( '\\n' | '\\r' | '\\f' | '\"' ) )*
                    loop164:
                    do {
                        int alt164=2;
                        int LA164_0 = input.LA(1);

                        if ( ((LA164_0>='\u0000' && LA164_0<='\t')||LA164_0=='\u000B'||(LA164_0>='\u000E' && LA164_0<='!')||(LA164_0>='#' && LA164_0<='\uFFFF')) ) {
                            alt164=1;
                        }


                        switch (alt164) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1643:25: ~ ( '\\n' | '\\r' | '\\f' | '\"' )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||input.LA(1)=='\u000B'||(input.LA(1)>='\u000E' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop164;
                        }
                    } while (true);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1644:21: ( '\"' | )
                    int alt165=2;
                    int LA165_0 = input.LA(1);

                    if ( (LA165_0=='\"') ) {
                        alt165=1;
                    }
                    else {
                        alt165=2;}
                    switch (alt165) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1645:27: '\"'
                            {
                            match('\"'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1646:27: 
                            {
                            if ( state.backtracking==0 ) {
                               _type = INVALID; 
                            }

                            }
                            break;

                    }


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "ONLY"
    public final void mONLY() throws RecognitionException {
        try {
            int _type = ONLY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1651:8: ( 'ONLY' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1651:10: 'ONLY'
            {
            match("ONLY"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ONLY"

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1652:6: ( 'NOT' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1652:8: 'NOT'
            {
            match("NOT"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1653:6: ( 'AND' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1653:8: 'AND'
            {
            match("AND"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1654:5: ( 'OR' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1654:7: 'OR'
            {
            match("OR"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "IDENT"
    public final void mIDENT() throws RecognitionException {
        try {
            int _type = IDENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1659:17: ( ( '-' )? NMSTART ( NMCHAR )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1659:19: ( '-' )? NMSTART ( NMCHAR )*
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1659:19: ( '-' )?
            int alt167=2;
            int LA167_0 = input.LA(1);

            if ( (LA167_0=='-') ) {
                alt167=1;
            }
            switch (alt167) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1659:19: '-'
                    {
                    match('-'); if (state.failed) return ;

                    }
                    break;

            }

            mNMSTART(); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1659:32: ( NMCHAR )*
            loop168:
            do {
                int alt168=2;
                int LA168_0 = input.LA(1);

                if ( (LA168_0=='-'||(LA168_0>='0' && LA168_0<='9')||(LA168_0>='A' && LA168_0<='Z')||LA168_0=='\\'||LA168_0=='_'||(LA168_0>='a' && LA168_0<='z')||(LA168_0>='\u0080' && LA168_0<='\uFFFF')) ) {
                    alt168=1;
                }


                switch (alt168) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1659:32: NMCHAR
            	    {
            	    mNMCHAR(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop168;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IDENT"

    // $ANTLR start "HASH_SYMBOL"
    public final void mHASH_SYMBOL() throws RecognitionException {
        try {
            int _type = HASH_SYMBOL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1664:17: ( '#' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1664:19: '#'
            {
            match('#'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HASH_SYMBOL"

    // $ANTLR start "HASH"
    public final void mHASH() throws RecognitionException {
        try {
            int _type = HASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1665:17: ( HASH_SYMBOL NAME )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1665:19: HASH_SYMBOL NAME
            {
            mHASH_SYMBOL(); if (state.failed) return ;
            mNAME(); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HASH"

    // $ANTLR start "IMPORTANT_SYM"
    public final void mIMPORTANT_SYM() throws RecognitionException {
        try {
            int _type = IMPORTANT_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1667:17: ( '!' ( WS | COMMENT )* 'IMPORTANT' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1667:19: '!' ( WS | COMMENT )* 'IMPORTANT'
            {
            match('!'); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1667:23: ( WS | COMMENT )*
            loop169:
            do {
                int alt169=3;
                int LA169_0 = input.LA(1);

                if ( (LA169_0=='\t'||LA169_0==' ') ) {
                    alt169=1;
                }
                else if ( (LA169_0=='/') ) {
                    alt169=2;
                }


                switch (alt169) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1667:24: WS
            	    {
            	    mWS(); if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1667:27: COMMENT
            	    {
            	    mCOMMENT(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop169;
                }
            } while (true);

            match("IMPORTANT"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IMPORTANT_SYM"

    // $ANTLR start "IMPORT_SYM"
    public final void mIMPORT_SYM() throws RecognitionException {
        try {
            int _type = IMPORT_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1669:21: ( '@IMPORT' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1669:23: '@IMPORT'
            {
            match("@IMPORT"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IMPORT_SYM"

    // $ANTLR start "PAGE_SYM"
    public final void mPAGE_SYM() throws RecognitionException {
        try {
            int _type = PAGE_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1670:21: ( '@PAGE' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1670:23: '@PAGE'
            {
            match("@PAGE"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PAGE_SYM"

    // $ANTLR start "MEDIA_SYM"
    public final void mMEDIA_SYM() throws RecognitionException {
        try {
            int _type = MEDIA_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1671:21: ( '@MEDIA' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1671:23: '@MEDIA'
            {
            match("@MEDIA"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MEDIA_SYM"

    // $ANTLR start "NAMESPACE_SYM"
    public final void mNAMESPACE_SYM() throws RecognitionException {
        try {
            int _type = NAMESPACE_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1672:21: ( '@NAMESPACE' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1672:23: '@NAMESPACE'
            {
            match("@NAMESPACE"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NAMESPACE_SYM"

    // $ANTLR start "CHARSET_SYM"
    public final void mCHARSET_SYM() throws RecognitionException {
        try {
            int _type = CHARSET_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1673:21: ( '@CHARSET' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1673:23: '@CHARSET'
            {
            match("@CHARSET"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CHARSET_SYM"

    // $ANTLR start "COUNTER_STYLE_SYM"
    public final void mCOUNTER_STYLE_SYM() throws RecognitionException {
        try {
            int _type = COUNTER_STYLE_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1674:21: ( '@COUNTER-STYLE' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1674:23: '@COUNTER-STYLE'
            {
            match("@COUNTER-STYLE"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COUNTER_STYLE_SYM"

    // $ANTLR start "FONT_FACE_SYM"
    public final void mFONT_FACE_SYM() throws RecognitionException {
        try {
            int _type = FONT_FACE_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1675:21: ( '@FONT-FACE' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1675:23: '@FONT-FACE'
            {
            match("@FONT-FACE"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FONT_FACE_SYM"

    // $ANTLR start "TOPLEFTCORNER_SYM"
    public final void mTOPLEFTCORNER_SYM() throws RecognitionException {
        try {
            int _type = TOPLEFTCORNER_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1677:23: ( '@TOP-LEFT-CORNER' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1677:24: '@TOP-LEFT-CORNER'
            {
            match("@TOP-LEFT-CORNER"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TOPLEFTCORNER_SYM"

    // $ANTLR start "TOPLEFT_SYM"
    public final void mTOPLEFT_SYM() throws RecognitionException {
        try {
            int _type = TOPLEFT_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1678:23: ( '@TOP-LEFT' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1678:24: '@TOP-LEFT'
            {
            match("@TOP-LEFT"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TOPLEFT_SYM"

    // $ANTLR start "TOPCENTER_SYM"
    public final void mTOPCENTER_SYM() throws RecognitionException {
        try {
            int _type = TOPCENTER_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1679:23: ( '@TOP-CENTER' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1679:24: '@TOP-CENTER'
            {
            match("@TOP-CENTER"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TOPCENTER_SYM"

    // $ANTLR start "TOPRIGHT_SYM"
    public final void mTOPRIGHT_SYM() throws RecognitionException {
        try {
            int _type = TOPRIGHT_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1680:23: ( '@TOP-RIGHT' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1680:24: '@TOP-RIGHT'
            {
            match("@TOP-RIGHT"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TOPRIGHT_SYM"

    // $ANTLR start "TOPRIGHTCORNER_SYM"
    public final void mTOPRIGHTCORNER_SYM() throws RecognitionException {
        try {
            int _type = TOPRIGHTCORNER_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1681:23: ( '@TOP-RIGHT-CORNER' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1681:24: '@TOP-RIGHT-CORNER'
            {
            match("@TOP-RIGHT-CORNER"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TOPRIGHTCORNER_SYM"

    // $ANTLR start "BOTTOMLEFTCORNER_SYM"
    public final void mBOTTOMLEFTCORNER_SYM() throws RecognitionException {
        try {
            int _type = BOTTOMLEFTCORNER_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1682:23: ( '@BOTTOM-LEFT-CORNER' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1682:24: '@BOTTOM-LEFT-CORNER'
            {
            match("@BOTTOM-LEFT-CORNER"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BOTTOMLEFTCORNER_SYM"

    // $ANTLR start "BOTTOMLEFT_SYM"
    public final void mBOTTOMLEFT_SYM() throws RecognitionException {
        try {
            int _type = BOTTOMLEFT_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1683:23: ( '@BOTTOM-LEFT' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1683:24: '@BOTTOM-LEFT'
            {
            match("@BOTTOM-LEFT"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BOTTOMLEFT_SYM"

    // $ANTLR start "BOTTOMCENTER_SYM"
    public final void mBOTTOMCENTER_SYM() throws RecognitionException {
        try {
            int _type = BOTTOMCENTER_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1684:23: ( '@BOTTOM-CENTER' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1684:24: '@BOTTOM-CENTER'
            {
            match("@BOTTOM-CENTER"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BOTTOMCENTER_SYM"

    // $ANTLR start "BOTTOMRIGHT_SYM"
    public final void mBOTTOMRIGHT_SYM() throws RecognitionException {
        try {
            int _type = BOTTOMRIGHT_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1685:23: ( '@BOTTOM-RIGHT' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1685:24: '@BOTTOM-RIGHT'
            {
            match("@BOTTOM-RIGHT"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BOTTOMRIGHT_SYM"

    // $ANTLR start "BOTTOMRIGHTCORNER_SYM"
    public final void mBOTTOMRIGHTCORNER_SYM() throws RecognitionException {
        try {
            int _type = BOTTOMRIGHTCORNER_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1686:23: ( '@BOTTOM-RIGHT-CORNER' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1686:24: '@BOTTOM-RIGHT-CORNER'
            {
            match("@BOTTOM-RIGHT-CORNER"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BOTTOMRIGHTCORNER_SYM"

    // $ANTLR start "LEFTTOP_SYM"
    public final void mLEFTTOP_SYM() throws RecognitionException {
        try {
            int _type = LEFTTOP_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1687:23: ( '@LEFT-TOP' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1687:24: '@LEFT-TOP'
            {
            match("@LEFT-TOP"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LEFTTOP_SYM"

    // $ANTLR start "LEFTMIDDLE_SYM"
    public final void mLEFTMIDDLE_SYM() throws RecognitionException {
        try {
            int _type = LEFTMIDDLE_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1688:23: ( '@LEFT-MIDDLE' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1688:24: '@LEFT-MIDDLE'
            {
            match("@LEFT-MIDDLE"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LEFTMIDDLE_SYM"

    // $ANTLR start "LEFTBOTTOM_SYM"
    public final void mLEFTBOTTOM_SYM() throws RecognitionException {
        try {
            int _type = LEFTBOTTOM_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1689:23: ( '@LEFT-BOTTOM' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1689:24: '@LEFT-BOTTOM'
            {
            match("@LEFT-BOTTOM"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LEFTBOTTOM_SYM"

    // $ANTLR start "RIGHTTOP_SYM"
    public final void mRIGHTTOP_SYM() throws RecognitionException {
        try {
            int _type = RIGHTTOP_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1690:23: ( '@RIGHT-TOP' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1690:24: '@RIGHT-TOP'
            {
            match("@RIGHT-TOP"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RIGHTTOP_SYM"

    // $ANTLR start "RIGHTMIDDLE_SYM"
    public final void mRIGHTMIDDLE_SYM() throws RecognitionException {
        try {
            int _type = RIGHTMIDDLE_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1691:23: ( '@RIGHT-MIDDLE' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1691:24: '@RIGHT-MIDDLE'
            {
            match("@RIGHT-MIDDLE"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RIGHTMIDDLE_SYM"

    // $ANTLR start "RIGHTBOTTOM_SYM"
    public final void mRIGHTBOTTOM_SYM() throws RecognitionException {
        try {
            int _type = RIGHTBOTTOM_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:23: ( '@RIGHT-BOTTOM' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:24: '@RIGHT-BOTTOM'
            {
            match("@RIGHT-BOTTOM"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RIGHTBOTTOM_SYM"

    // $ANTLR start "MOZ_DOCUMENT_SYM"
    public final void mMOZ_DOCUMENT_SYM() throws RecognitionException {
        try {
            int _type = MOZ_DOCUMENT_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1694:23: ( '@-MOZ-DOCUMENT' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1694:25: '@-MOZ-DOCUMENT'
            {
            match("@-MOZ-DOCUMENT"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MOZ_DOCUMENT_SYM"

    // $ANTLR start "WEBKIT_KEYFRAMES_SYM"
    public final void mWEBKIT_KEYFRAMES_SYM() throws RecognitionException {
        try {
            int _type = WEBKIT_KEYFRAMES_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1695:23: ( '@-WEBKIT-KEYFRAMES' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1695:25: '@-WEBKIT-KEYFRAMES'
            {
            match("@-WEBKIT-KEYFRAMES"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WEBKIT_KEYFRAMES_SYM"

    // $ANTLR start "SASS_CONTENT"
    public final void mSASS_CONTENT() throws RecognitionException {
        try {
            int _type = SASS_CONTENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1698:21: ( '@CONTENT' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1698:23: '@CONTENT'
            {
            match("@CONTENT"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_CONTENT"

    // $ANTLR start "SASS_MIXIN"
    public final void mSASS_MIXIN() throws RecognitionException {
        try {
            int _type = SASS_MIXIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1699:21: ( '@MIXIN' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1699:23: '@MIXIN'
            {
            match("@MIXIN"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_MIXIN"

    // $ANTLR start "SASS_INCLUDE"
    public final void mSASS_INCLUDE() throws RecognitionException {
        try {
            int _type = SASS_INCLUDE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1700:21: ( '@INCLUDE' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1700:23: '@INCLUDE'
            {
            match("@INCLUDE"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_INCLUDE"

    // $ANTLR start "SASS_EXTEND"
    public final void mSASS_EXTEND() throws RecognitionException {
        try {
            int _type = SASS_EXTEND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1701:21: ( '@EXTEND' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1701:23: '@EXTEND'
            {
            match("@EXTEND"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_EXTEND"

    // $ANTLR start "SASS_DEBUG"
    public final void mSASS_DEBUG() throws RecognitionException {
        try {
            int _type = SASS_DEBUG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1702:21: ( '@DEBUG' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1702:23: '@DEBUG'
            {
            match("@DEBUG"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_DEBUG"

    // $ANTLR start "SASS_WARN"
    public final void mSASS_WARN() throws RecognitionException {
        try {
            int _type = SASS_WARN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1703:21: ( '@WARN' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1703:23: '@WARN'
            {
            match("@WARN"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_WARN"

    // $ANTLR start "SASS_IF"
    public final void mSASS_IF() throws RecognitionException {
        try {
            int _type = SASS_IF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1704:21: ( '@IF' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1704:23: '@IF'
            {
            match("@IF"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_IF"

    // $ANTLR start "SASS_ELSE"
    public final void mSASS_ELSE() throws RecognitionException {
        try {
            int _type = SASS_ELSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1705:21: ( '@ELSE' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1705:23: '@ELSE'
            {
            match("@ELSE"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_ELSE"

    // $ANTLR start "SASS_FOR"
    public final void mSASS_FOR() throws RecognitionException {
        try {
            int _type = SASS_FOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1707:21: ( '@FOR' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1707:23: '@FOR'
            {
            match("@FOR"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_FOR"

    // $ANTLR start "SASS_FUNCTION"
    public final void mSASS_FUNCTION() throws RecognitionException {
        try {
            int _type = SASS_FUNCTION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1708:21: ( '@FUNCTION' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1708:23: '@FUNCTION'
            {
            match("@FUNCTION"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_FUNCTION"

    // $ANTLR start "SASS_RETURN"
    public final void mSASS_RETURN() throws RecognitionException {
        try {
            int _type = SASS_RETURN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1709:21: ( '@RETURN' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1709:23: '@RETURN'
            {
            match("@RETURN"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_RETURN"

    // $ANTLR start "SASS_EACH"
    public final void mSASS_EACH() throws RecognitionException {
        try {
            int _type = SASS_EACH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1711:21: ( '@EACH' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1711:23: '@EACH'
            {
            match("@EACH"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_EACH"

    // $ANTLR start "SASS_WHILE"
    public final void mSASS_WHILE() throws RecognitionException {
        try {
            int _type = SASS_WHILE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1712:21: ( '@WHILE' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1712:23: '@WHILE'
            {
            match("@WHILE"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_WHILE"

    // $ANTLR start "AT_IDENT"
    public final void mAT_IDENT() throws RecognitionException {
        try {
            int _type = AT_IDENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:14: ( '@' ( NMCHAR )+ )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:16: '@' ( NMCHAR )+
            {
            match('@'); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:20: ( NMCHAR )+
            int cnt170=0;
            loop170:
            do {
                int alt170=2;
                int LA170_0 = input.LA(1);

                if ( (LA170_0=='-'||(LA170_0>='0' && LA170_0<='9')||(LA170_0>='A' && LA170_0<='Z')||LA170_0=='\\'||LA170_0=='_'||(LA170_0>='a' && LA170_0<='z')||(LA170_0>='\u0080' && LA170_0<='\uFFFF')) ) {
                    alt170=1;
                }


                switch (alt170) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:20: NMCHAR
            	    {
            	    mNMCHAR(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt170 >= 1 ) break loop170;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(170, input);
                        throw eee;
                }
                cnt170++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AT_IDENT"

    // $ANTLR start "SASS_VAR"
    public final void mSASS_VAR() throws RecognitionException {
        try {
            int _type = SASS_VAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1716:21: ( '$' ( NMCHAR )+ )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1716:23: '$' ( NMCHAR )+
            {
            match('$'); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1716:27: ( NMCHAR )+
            int cnt171=0;
            loop171:
            do {
                int alt171=2;
                int LA171_0 = input.LA(1);

                if ( (LA171_0=='-'||(LA171_0>='0' && LA171_0<='9')||(LA171_0>='A' && LA171_0<='Z')||LA171_0=='\\'||LA171_0=='_'||(LA171_0>='a' && LA171_0<='z')||(LA171_0>='\u0080' && LA171_0<='\uFFFF')) ) {
                    alt171=1;
                }


                switch (alt171) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1716:27: NMCHAR
            	    {
            	    mNMCHAR(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt171 >= 1 ) break loop171;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(171, input);
                        throw eee;
                }
                cnt171++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_VAR"

    // $ANTLR start "SASS_DEFAULT"
    public final void mSASS_DEFAULT() throws RecognitionException {
        try {
            int _type = SASS_DEFAULT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1717:21: ( '!DEFAULT' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1717:23: '!DEFAULT'
            {
            match("!DEFAULT"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_DEFAULT"

    // $ANTLR start "SASS_OPTIONAL"
    public final void mSASS_OPTIONAL() throws RecognitionException {
        try {
            int _type = SASS_OPTIONAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1718:21: ( '!OPTIONAL' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1718:23: '!OPTIONAL'
            {
            match("!OPTIONAL"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_OPTIONAL"

    // $ANTLR start "SASS_EXTEND_ONLY_SELECTOR"
    public final void mSASS_EXTEND_ONLY_SELECTOR() throws RecognitionException {
        try {
            int _type = SASS_EXTEND_ONLY_SELECTOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1721:21: ( '%' ( NMCHAR )+ )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1721:23: '%' ( NMCHAR )+
            {
            match('%'); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1721:27: ( NMCHAR )+
            int cnt172=0;
            loop172:
            do {
                int alt172=2;
                int LA172_0 = input.LA(1);

                if ( (LA172_0=='-'||(LA172_0>='0' && LA172_0<='9')||(LA172_0>='A' && LA172_0<='Z')||LA172_0=='\\'||LA172_0=='_'||(LA172_0>='a' && LA172_0<='z')||(LA172_0>='\u0080' && LA172_0<='\uFFFF')) ) {
                    alt172=1;
                }


                switch (alt172) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1721:27: NMCHAR
            	    {
            	    mNMCHAR(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt172 >= 1 ) break loop172;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(172, input);
                        throw eee;
                }
                cnt172++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SASS_EXTEND_ONLY_SELECTOR"

    // $ANTLR start "EMS"
    public final void mEMS() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1733:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1733:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "EMS"

    // $ANTLR start "EXS"
    public final void mEXS() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1734:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1734:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "EXS"

    // $ANTLR start "LENGTH"
    public final void mLENGTH() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "LENGTH"

    // $ANTLR start "REM"
    public final void mREM() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1736:18: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1736:19: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "REM"

    // $ANTLR start "ANGLE"
    public final void mANGLE() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1737:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1737:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "ANGLE"

    // $ANTLR start "TIME"
    public final void mTIME() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1738:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1738:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "TIME"

    // $ANTLR start "FREQ"
    public final void mFREQ() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1739:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1739:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "FREQ"

    // $ANTLR start "DIMENSION"
    public final void mDIMENSION() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1740:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1740:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "DIMENSION"

    // $ANTLR start "PERCENTAGE"
    public final void mPERCENTAGE() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1741:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1741:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "PERCENTAGE"

    // $ANTLR start "RESOLUTION"
    public final void mRESOLUTION() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1742:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1742:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "RESOLUTION"

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1745:5: ( ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ ) ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | ) )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1745:9: ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ ) ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | )
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1745:9: ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ )
            int alt177=2;
            int LA177_0 = input.LA(1);

            if ( ((LA177_0>='0' && LA177_0<='9')) ) {
                alt177=1;
            }
            else if ( (LA177_0=='.') ) {
                alt177=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 177, 0, input);

                throw nvae;
            }
            switch (alt177) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1746:15: ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )?
                    {
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1746:15: ( '0' .. '9' )+
                    int cnt173=0;
                    loop173:
                    do {
                        int alt173=2;
                        int LA173_0 = input.LA(1);

                        if ( ((LA173_0>='0' && LA173_0<='9')) ) {
                            alt173=1;
                        }


                        switch (alt173) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1746:15: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt173 >= 1 ) break loop173;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(173, input);
                                throw eee;
                        }
                        cnt173++;
                    } while (true);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1746:25: ( '.' ( '0' .. '9' )+ )?
                    int alt175=2;
                    int LA175_0 = input.LA(1);

                    if ( (LA175_0=='.') ) {
                        alt175=1;
                    }
                    switch (alt175) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1746:26: '.' ( '0' .. '9' )+
                            {
                            match('.'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1746:30: ( '0' .. '9' )+
                            int cnt174=0;
                            loop174:
                            do {
                                int alt174=2;
                                int LA174_0 = input.LA(1);

                                if ( ((LA174_0>='0' && LA174_0<='9')) ) {
                                    alt174=1;
                                }


                                switch (alt174) {
                            	case 1 :
                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1746:30: '0' .. '9'
                            	    {
                            	    matchRange('0','9'); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    if ( cnt174 >= 1 ) break loop174;
                            	    if (state.backtracking>0) {state.failed=true; return ;}
                                        EarlyExitException eee =
                                            new EarlyExitException(174, input);
                                        throw eee;
                                }
                                cnt174++;
                            } while (true);


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1747:15: '.' ( '0' .. '9' )+
                    {
                    match('.'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1747:19: ( '0' .. '9' )+
                    int cnt176=0;
                    loop176:
                    do {
                        int alt176=2;
                        int LA176_0 = input.LA(1);

                        if ( ((LA176_0>='0' && LA176_0<='9')) ) {
                            alt176=1;
                        }


                        switch (alt176) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1747:19: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt176 >= 1 ) break loop176;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(176, input);
                                throw eee;
                        }
                        cnt176++;
                    } while (true);


                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1749:9: ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | )
            int alt184=13;
            alt184 = dfa184.predict(input);
            switch (alt184) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1750:15: ( D P ( I | C ) )=> D P ( I | C M )
                    {
                    mD(); if (state.failed) return ;
                    mP(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1752:17: ( I | C M )
                    int alt178=2;
                    switch ( input.LA(1) ) {
                    case 'I':
                    case 'i':
                        {
                        alt178=1;
                        }
                        break;
                    case '\\':
                        {
                        switch ( input.LA(2) ) {
                        case 'I':
                        case 'i':
                            {
                            alt178=1;
                            }
                            break;
                        case '0':
                            {
                            int LA178_4 = input.LA(3);

                            if ( (LA178_4=='0') ) {
                                int LA178_6 = input.LA(4);

                                if ( (LA178_6=='0') ) {
                                    int LA178_7 = input.LA(5);

                                    if ( (LA178_7=='0') ) {
                                        int LA178_8 = input.LA(6);

                                        if ( (LA178_8=='4'||LA178_8=='6') ) {
                                            int LA178_5 = input.LA(7);

                                            if ( (LA178_5=='9') ) {
                                                alt178=1;
                                            }
                                            else if ( (LA178_5=='3') ) {
                                                alt178=2;
                                            }
                                            else {
                                                if (state.backtracking>0) {state.failed=true; return ;}
                                                NoViableAltException nvae =
                                                    new NoViableAltException("", 178, 5, input);

                                                throw nvae;
                                            }
                                        }
                                        else {
                                            if (state.backtracking>0) {state.failed=true; return ;}
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 178, 8, input);

                                            throw nvae;
                                        }
                                    }
                                    else if ( (LA178_7=='4'||LA178_7=='6') ) {
                                        int LA178_5 = input.LA(6);

                                        if ( (LA178_5=='9') ) {
                                            alt178=1;
                                        }
                                        else if ( (LA178_5=='3') ) {
                                            alt178=2;
                                        }
                                        else {
                                            if (state.backtracking>0) {state.failed=true; return ;}
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 178, 5, input);

                                            throw nvae;
                                        }
                                    }
                                    else {
                                        if (state.backtracking>0) {state.failed=true; return ;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 178, 7, input);

                                        throw nvae;
                                    }
                                }
                                else if ( (LA178_6=='4'||LA178_6=='6') ) {
                                    int LA178_5 = input.LA(5);

                                    if ( (LA178_5=='9') ) {
                                        alt178=1;
                                    }
                                    else if ( (LA178_5=='3') ) {
                                        alt178=2;
                                    }
                                    else {
                                        if (state.backtracking>0) {state.failed=true; return ;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 178, 5, input);

                                        throw nvae;
                                    }
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 178, 6, input);

                                    throw nvae;
                                }
                            }
                            else if ( (LA178_4=='4'||LA178_4=='6') ) {
                                int LA178_5 = input.LA(4);

                                if ( (LA178_5=='9') ) {
                                    alt178=1;
                                }
                                else if ( (LA178_5=='3') ) {
                                    alt178=2;
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 178, 5, input);

                                    throw nvae;
                                }
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 178, 4, input);

                                throw nvae;
                            }
                            }
                            break;
                        case '4':
                        case '6':
                            {
                            int LA178_5 = input.LA(3);

                            if ( (LA178_5=='9') ) {
                                alt178=1;
                            }
                            else if ( (LA178_5=='3') ) {
                                alt178=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 178, 5, input);

                                throw nvae;
                            }
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 178, 2, input);

                            throw nvae;
                        }

                        }
                        break;
                    case 'C':
                    case 'c':
                        {
                        alt178=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 178, 0, input);

                        throw nvae;
                    }

                    switch (alt178) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1753:22: I
                            {
                            mI(); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1753:26: C M
                            {
                            mC(); if (state.failed) return ;
                            mM(); if (state.failed) return ;

                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {
                       _type = RESOLUTION; 
                    }

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1757:15: ( E ( M | X ) )=> E ( M | X )
                    {
                    mE(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1759:17: ( M | X )
                    int alt179=2;
                    switch ( input.LA(1) ) {
                    case 'M':
                    case 'm':
                        {
                        alt179=1;
                        }
                        break;
                    case '\\':
                        {
                        switch ( input.LA(2) ) {
                        case '4':
                        case '6':
                        case 'M':
                        case 'm':
                            {
                            alt179=1;
                            }
                            break;
                        case '0':
                            {
                            switch ( input.LA(3) ) {
                            case '0':
                                {
                                switch ( input.LA(4) ) {
                                case '0':
                                    {
                                    switch ( input.LA(5) ) {
                                    case '0':
                                        {
                                        int LA179_7 = input.LA(6);

                                        if ( (LA179_7=='4'||LA179_7=='6') ) {
                                            alt179=1;
                                        }
                                        else if ( (LA179_7=='5'||LA179_7=='7') ) {
                                            alt179=2;
                                        }
                                        else {
                                            if (state.backtracking>0) {state.failed=true; return ;}
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 179, 7, input);

                                            throw nvae;
                                        }
                                        }
                                        break;
                                    case '4':
                                    case '6':
                                        {
                                        alt179=1;
                                        }
                                        break;
                                    case '5':
                                    case '7':
                                        {
                                        alt179=2;
                                        }
                                        break;
                                    default:
                                        if (state.backtracking>0) {state.failed=true; return ;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 179, 6, input);

                                        throw nvae;
                                    }

                                    }
                                    break;
                                case '4':
                                case '6':
                                    {
                                    alt179=1;
                                    }
                                    break;
                                case '5':
                                case '7':
                                    {
                                    alt179=2;
                                    }
                                    break;
                                default:
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 179, 5, input);

                                    throw nvae;
                                }

                                }
                                break;
                            case '4':
                            case '6':
                                {
                                alt179=1;
                                }
                                break;
                            case '5':
                            case '7':
                                {
                                alt179=2;
                                }
                                break;
                            default:
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 179, 4, input);

                                throw nvae;
                            }

                            }
                            break;
                        case '5':
                        case '7':
                        case 'X':
                        case 'x':
                            {
                            alt179=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 179, 2, input);

                            throw nvae;
                        }

                        }
                        break;
                    case 'X':
                    case 'x':
                        {
                        alt179=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 179, 0, input);

                        throw nvae;
                    }

                    switch (alt179) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1760:23: M
                            {
                            mM(); if (state.failed) return ;
                            if ( state.backtracking==0 ) {
                               _type = EMS;          
                            }

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1761:23: X
                            {
                            mX(); if (state.failed) return ;
                            if ( state.backtracking==0 ) {
                               _type = EXS;          
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1763:15: ( P ( X | T | C ) )=> P ( X | T | C )
                    {
                    mP(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1765:17: ( X | T | C )
                    int alt180=3;
                    alt180 = dfa180.predict(input);
                    switch (alt180) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1766:23: X
                            {
                            mX(); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1767:23: T
                            {
                            mT(); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1768:23: C
                            {
                            mC(); if (state.failed) return ;

                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {
                       _type = LENGTH;       
                    }

                    }
                    break;
                case 4 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1771:15: ( C M )=> C M
                    {
                    mC(); if (state.failed) return ;
                    mM(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = LENGTH;       
                    }

                    }
                    break;
                case 5 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1773:15: ( M ( M | S ) )=> M ( M | S )
                    {
                    mM(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1775:17: ( M | S )
                    int alt181=2;
                    switch ( input.LA(1) ) {
                    case 'M':
                    case 'm':
                        {
                        alt181=1;
                        }
                        break;
                    case '\\':
                        {
                        switch ( input.LA(2) ) {
                        case '4':
                        case '6':
                        case 'M':
                        case 'm':
                            {
                            alt181=1;
                            }
                            break;
                        case '0':
                            {
                            switch ( input.LA(3) ) {
                            case '0':
                                {
                                switch ( input.LA(4) ) {
                                case '0':
                                    {
                                    switch ( input.LA(5) ) {
                                    case '0':
                                        {
                                        int LA181_7 = input.LA(6);

                                        if ( (LA181_7=='4'||LA181_7=='6') ) {
                                            alt181=1;
                                        }
                                        else if ( (LA181_7=='5'||LA181_7=='7') ) {
                                            alt181=2;
                                        }
                                        else {
                                            if (state.backtracking>0) {state.failed=true; return ;}
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 181, 7, input);

                                            throw nvae;
                                        }
                                        }
                                        break;
                                    case '4':
                                    case '6':
                                        {
                                        alt181=1;
                                        }
                                        break;
                                    case '5':
                                    case '7':
                                        {
                                        alt181=2;
                                        }
                                        break;
                                    default:
                                        if (state.backtracking>0) {state.failed=true; return ;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 181, 6, input);

                                        throw nvae;
                                    }

                                    }
                                    break;
                                case '4':
                                case '6':
                                    {
                                    alt181=1;
                                    }
                                    break;
                                case '5':
                                case '7':
                                    {
                                    alt181=2;
                                    }
                                    break;
                                default:
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 181, 5, input);

                                    throw nvae;
                                }

                                }
                                break;
                            case '4':
                            case '6':
                                {
                                alt181=1;
                                }
                                break;
                            case '5':
                            case '7':
                                {
                                alt181=2;
                                }
                                break;
                            default:
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 181, 4, input);

                                throw nvae;
                            }

                            }
                            break;
                        case '5':
                        case '7':
                        case 'S':
                        case 's':
                            {
                            alt181=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 181, 2, input);

                            throw nvae;
                        }

                        }
                        break;
                    case 'S':
                    case 's':
                        {
                        alt181=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 181, 0, input);

                        throw nvae;
                    }

                    switch (alt181) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1776:23: M
                            {
                            mM(); if (state.failed) return ;
                            if ( state.backtracking==0 ) {
                               _type = LENGTH;       
                            }

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1778:23: S
                            {
                            mS(); if (state.failed) return ;
                            if ( state.backtracking==0 ) {
                               _type = TIME;         
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 6 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1780:15: ( I N )=> I N
                    {
                    mI(); if (state.failed) return ;
                    mN(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = LENGTH;       
                    }

                    }
                    break;
                case 7 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1783:15: ( D E G )=> D E G
                    {
                    mD(); if (state.failed) return ;
                    mE(); if (state.failed) return ;
                    mG(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = ANGLE;        
                    }

                    }
                    break;
                case 8 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1788:15: ( R ( A | E ) )=> R ( A D | E M )
                    {
                    mR(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1790:17: ( A D | E M )
                    int alt182=2;
                    switch ( input.LA(1) ) {
                    case 'A':
                    case 'a':
                        {
                        alt182=1;
                        }
                        break;
                    case '\\':
                        {
                        int LA182_2 = input.LA(2);

                        if ( (LA182_2=='0') ) {
                            int LA182_4 = input.LA(3);

                            if ( (LA182_4=='0') ) {
                                int LA182_6 = input.LA(4);

                                if ( (LA182_6=='0') ) {
                                    int LA182_7 = input.LA(5);

                                    if ( (LA182_7=='0') ) {
                                        int LA182_8 = input.LA(6);

                                        if ( (LA182_8=='4'||LA182_8=='6') ) {
                                            int LA182_5 = input.LA(7);

                                            if ( (LA182_5=='1') ) {
                                                alt182=1;
                                            }
                                            else if ( (LA182_5=='5') ) {
                                                alt182=2;
                                            }
                                            else {
                                                if (state.backtracking>0) {state.failed=true; return ;}
                                                NoViableAltException nvae =
                                                    new NoViableAltException("", 182, 5, input);

                                                throw nvae;
                                            }
                                        }
                                        else {
                                            if (state.backtracking>0) {state.failed=true; return ;}
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 182, 8, input);

                                            throw nvae;
                                        }
                                    }
                                    else if ( (LA182_7=='4'||LA182_7=='6') ) {
                                        int LA182_5 = input.LA(6);

                                        if ( (LA182_5=='1') ) {
                                            alt182=1;
                                        }
                                        else if ( (LA182_5=='5') ) {
                                            alt182=2;
                                        }
                                        else {
                                            if (state.backtracking>0) {state.failed=true; return ;}
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 182, 5, input);

                                            throw nvae;
                                        }
                                    }
                                    else {
                                        if (state.backtracking>0) {state.failed=true; return ;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 182, 7, input);

                                        throw nvae;
                                    }
                                }
                                else if ( (LA182_6=='4'||LA182_6=='6') ) {
                                    int LA182_5 = input.LA(5);

                                    if ( (LA182_5=='1') ) {
                                        alt182=1;
                                    }
                                    else if ( (LA182_5=='5') ) {
                                        alt182=2;
                                    }
                                    else {
                                        if (state.backtracking>0) {state.failed=true; return ;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 182, 5, input);

                                        throw nvae;
                                    }
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 182, 6, input);

                                    throw nvae;
                                }
                            }
                            else if ( (LA182_4=='4'||LA182_4=='6') ) {
                                int LA182_5 = input.LA(4);

                                if ( (LA182_5=='1') ) {
                                    alt182=1;
                                }
                                else if ( (LA182_5=='5') ) {
                                    alt182=2;
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 182, 5, input);

                                    throw nvae;
                                }
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 182, 4, input);

                                throw nvae;
                            }
                        }
                        else if ( (LA182_2=='4'||LA182_2=='6') ) {
                            int LA182_5 = input.LA(3);

                            if ( (LA182_5=='1') ) {
                                alt182=1;
                            }
                            else if ( (LA182_5=='5') ) {
                                alt182=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 182, 5, input);

                                throw nvae;
                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 182, 2, input);

                            throw nvae;
                        }
                        }
                        break;
                    case 'E':
                    case 'e':
                        {
                        alt182=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 182, 0, input);

                        throw nvae;
                    }

                    switch (alt182) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1791:20: A D
                            {
                            mA(); if (state.failed) return ;
                            mD(); if (state.failed) return ;
                            if ( state.backtracking==0 ) {
                              _type = ANGLE;         
                            }

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1792:20: E M
                            {
                            mE(); if (state.failed) return ;
                            mM(); if (state.failed) return ;
                            if ( state.backtracking==0 ) {
                              _type = REM;           
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 9 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1795:15: ( S )=> S
                    {
                    mS(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = TIME;         
                    }

                    }
                    break;
                case 10 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1797:15: ( ( K )? H Z )=> ( K )? H Z
                    {
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1798:17: ( K )?
                    int alt183=2;
                    int LA183_0 = input.LA(1);

                    if ( (LA183_0=='K'||LA183_0=='k') ) {
                        alt183=1;
                    }
                    else if ( (LA183_0=='\\') ) {
                        switch ( input.LA(2) ) {
                            case 'K':
                            case 'k':
                                {
                                alt183=1;
                                }
                                break;
                            case '0':
                                {
                                int LA183_4 = input.LA(3);

                                if ( (LA183_4=='0') ) {
                                    int LA183_6 = input.LA(4);

                                    if ( (LA183_6=='0') ) {
                                        int LA183_7 = input.LA(5);

                                        if ( (LA183_7=='0') ) {
                                            int LA183_8 = input.LA(6);

                                            if ( (LA183_8=='4'||LA183_8=='6') ) {
                                                int LA183_5 = input.LA(7);

                                                if ( (LA183_5=='B'||LA183_5=='b') ) {
                                                    alt183=1;
                                                }
                                            }
                                        }
                                        else if ( (LA183_7=='4'||LA183_7=='6') ) {
                                            int LA183_5 = input.LA(6);

                                            if ( (LA183_5=='B'||LA183_5=='b') ) {
                                                alt183=1;
                                            }
                                        }
                                    }
                                    else if ( (LA183_6=='4'||LA183_6=='6') ) {
                                        int LA183_5 = input.LA(5);

                                        if ( (LA183_5=='B'||LA183_5=='b') ) {
                                            alt183=1;
                                        }
                                    }
                                }
                                else if ( (LA183_4=='4'||LA183_4=='6') ) {
                                    int LA183_5 = input.LA(4);

                                    if ( (LA183_5=='B'||LA183_5=='b') ) {
                                        alt183=1;
                                    }
                                }
                                }
                                break;
                            case '4':
                            case '6':
                                {
                                int LA183_5 = input.LA(3);

                                if ( (LA183_5=='B'||LA183_5=='b') ) {
                                    alt183=1;
                                }
                                }
                                break;
                        }

                    }
                    switch (alt183) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1798:17: K
                            {
                            mK(); if (state.failed) return ;

                            }
                            break;

                    }

                    mH(); if (state.failed) return ;
                    mZ(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = FREQ;         
                    }

                    }
                    break;
                case 11 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1800:15: IDENT
                    {
                    mIDENT(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = DIMENSION;    
                    }

                    }
                    break;
                case 12 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1802:15: '%'
                    {
                    match('%'); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = PERCENTAGE;   
                    }

                    }
                    break;
                case 13 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1805:9: 
                    {
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
    // $ANTLR end "NUMBER"

    // $ANTLR start "URI"
    public final void mURI() throws RecognitionException {
        try {
            int _type = URI;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1811:5: ( U R L '(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1811:9: U R L '(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
            {
            mU(); if (state.failed) return ;
            mR(); if (state.failed) return ;
            mL(); if (state.failed) return ;
            match('('); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1813:13: ( ( WS )=> WS )?
            int alt185=2;
            int LA185_0 = input.LA(1);

            if ( (LA185_0=='\t'||LA185_0==' ') ) {
                int LA185_1 = input.LA(2);

                if ( (synpred11_Css3()) ) {
                    alt185=1;
                }
            }
            switch (alt185) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1813:14: ( WS )=> WS
                    {
                    mWS(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1813:25: ( URL | STRING )
            int alt186=2;
            int LA186_0 = input.LA(1);

            if ( (LA186_0=='\t'||(LA186_0>=' ' && LA186_0<='!')||(LA186_0>='#' && LA186_0<='&')||(LA186_0>=')' && LA186_0<=';')||LA186_0=='='||LA186_0=='?'||(LA186_0>='A' && LA186_0<='\\')||LA186_0=='_'||(LA186_0>='a' && LA186_0<='z')||LA186_0=='~'||(LA186_0>='\u0080' && LA186_0<='\uFFFF')) ) {
                alt186=1;
            }
            else if ( (LA186_0=='\"'||LA186_0=='\'') ) {
                alt186=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 186, 0, input);

                throw nvae;
            }
            switch (alt186) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1813:26: URL
                    {
                    mURL(); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1813:30: STRING
                    {
                    mSTRING(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1813:38: ( WS )?
            int alt187=2;
            int LA187_0 = input.LA(1);

            if ( (LA187_0=='\t'||LA187_0==' ') ) {
                alt187=1;
            }
            switch (alt187) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1813:38: WS
                    {
                    mWS(); if (state.failed) return ;

                    }
                    break;

            }

            match(')'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "URI"

    // $ANTLR start "MOZ_URL_PREFIX"
    public final void mMOZ_URL_PREFIX() throws RecognitionException {
        try {
            int _type = MOZ_URL_PREFIX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1818:2: ( 'URL-PREFIX(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1819:2: 'URL-PREFIX(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
            {
            match("URL-PREFIX("); if (state.failed) return ;

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1820:13: ( ( WS )=> WS )?
            int alt188=2;
            int LA188_0 = input.LA(1);

            if ( (LA188_0=='\t'||LA188_0==' ') ) {
                int LA188_1 = input.LA(2);

                if ( (synpred12_Css3()) ) {
                    alt188=1;
                }
            }
            switch (alt188) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1820:14: ( WS )=> WS
                    {
                    mWS(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1820:25: ( URL | STRING )
            int alt189=2;
            int LA189_0 = input.LA(1);

            if ( (LA189_0=='\t'||(LA189_0>=' ' && LA189_0<='!')||(LA189_0>='#' && LA189_0<='&')||(LA189_0>=')' && LA189_0<=';')||LA189_0=='='||LA189_0=='?'||(LA189_0>='A' && LA189_0<='\\')||LA189_0=='_'||(LA189_0>='a' && LA189_0<='z')||LA189_0=='~'||(LA189_0>='\u0080' && LA189_0<='\uFFFF')) ) {
                alt189=1;
            }
            else if ( (LA189_0=='\"'||LA189_0=='\'') ) {
                alt189=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 189, 0, input);

                throw nvae;
            }
            switch (alt189) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1820:26: URL
                    {
                    mURL(); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1820:30: STRING
                    {
                    mSTRING(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1820:38: ( WS )?
            int alt190=2;
            int LA190_0 = input.LA(1);

            if ( (LA190_0=='\t'||LA190_0==' ') ) {
                alt190=1;
            }
            switch (alt190) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1820:38: WS
                    {
                    mWS(); if (state.failed) return ;

                    }
                    break;

            }

            match(')'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MOZ_URL_PREFIX"

    // $ANTLR start "MOZ_DOMAIN"
    public final void mMOZ_DOMAIN() throws RecognitionException {
        try {
            int _type = MOZ_DOMAIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1826:2: ( 'DOMAIN(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1827:2: 'DOMAIN(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
            {
            match("DOMAIN("); if (state.failed) return ;

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1828:13: ( ( WS )=> WS )?
            int alt191=2;
            int LA191_0 = input.LA(1);

            if ( (LA191_0=='\t'||LA191_0==' ') ) {
                int LA191_1 = input.LA(2);

                if ( (synpred13_Css3()) ) {
                    alt191=1;
                }
            }
            switch (alt191) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1828:14: ( WS )=> WS
                    {
                    mWS(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1828:25: ( URL | STRING )
            int alt192=2;
            int LA192_0 = input.LA(1);

            if ( (LA192_0=='\t'||(LA192_0>=' ' && LA192_0<='!')||(LA192_0>='#' && LA192_0<='&')||(LA192_0>=')' && LA192_0<=';')||LA192_0=='='||LA192_0=='?'||(LA192_0>='A' && LA192_0<='\\')||LA192_0=='_'||(LA192_0>='a' && LA192_0<='z')||LA192_0=='~'||(LA192_0>='\u0080' && LA192_0<='\uFFFF')) ) {
                alt192=1;
            }
            else if ( (LA192_0=='\"'||LA192_0=='\'') ) {
                alt192=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 192, 0, input);

                throw nvae;
            }
            switch (alt192) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1828:26: URL
                    {
                    mURL(); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1828:30: STRING
                    {
                    mSTRING(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1828:38: ( WS )?
            int alt193=2;
            int LA193_0 = input.LA(1);

            if ( (LA193_0=='\t'||LA193_0==' ') ) {
                alt193=1;
            }
            switch (alt193) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1828:38: WS
                    {
                    mWS(); if (state.failed) return ;

                    }
                    break;

            }

            match(')'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MOZ_DOMAIN"

    // $ANTLR start "MOZ_REGEXP"
    public final void mMOZ_REGEXP() throws RecognitionException {
        try {
            int _type = MOZ_REGEXP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1834:2: ( 'REGEXP(' ( ( WS )=> WS )? STRING ( WS )? ')' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1835:2: 'REGEXP(' ( ( WS )=> WS )? STRING ( WS )? ')'
            {
            match("REGEXP("); if (state.failed) return ;

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1836:13: ( ( WS )=> WS )?
            int alt194=2;
            int LA194_0 = input.LA(1);

            if ( (LA194_0=='\t'||LA194_0==' ') && (synpred14_Css3())) {
                alt194=1;
            }
            switch (alt194) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1836:14: ( WS )=> WS
                    {
                    mWS(); if (state.failed) return ;

                    }
                    break;

            }

            mSTRING(); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1836:32: ( WS )?
            int alt195=2;
            int LA195_0 = input.LA(1);

            if ( (LA195_0=='\t'||LA195_0==' ') ) {
                alt195=1;
            }
            switch (alt195) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1836:32: WS
                    {
                    mWS(); if (state.failed) return ;

                    }
                    break;

            }

            match(')'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MOZ_REGEXP"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1849:5: ( ( ' ' | '\\t' )+ )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1850:5: ( ' ' | '\\t' )+
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1850:5: ( ' ' | '\\t' )+
            int cnt196=0;
            loop196:
            do {
                int alt196=2;
                int LA196_0 = input.LA(1);

                if ( (LA196_0=='\t'||LA196_0==' ') ) {
                    alt196=1;
                }


                switch (alt196) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
            	        input.consume();
            	    state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt196 >= 1 ) break loop196;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(196, input);
                        throw eee;
                }
                cnt196++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "NL"
    public final void mNL() throws RecognitionException {
        try {
            int _type = NL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1854:5: ( ( '\\r' ( '\\n' )? | '\\n' ) )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:5: ( '\\r' ( '\\n' )? | '\\n' )
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:5: ( '\\r' ( '\\n' )? | '\\n' )
            int alt198=2;
            int LA198_0 = input.LA(1);

            if ( (LA198_0=='\r') ) {
                alt198=1;
            }
            else if ( (LA198_0=='\n') ) {
                alt198=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 198, 0, input);

                throw nvae;
            }
            switch (alt198) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:6: '\\r' ( '\\n' )?
                    {
                    match('\r'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:11: ( '\\n' )?
                    int alt197=2;
                    int LA197_0 = input.LA(1);

                    if ( (LA197_0=='\n') ) {
                        alt197=1;
                    }
                    switch (alt197) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:11: '\\n'
                            {
                            match('\n'); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:19: '\\n'
                    {
                    match('\n'); if (state.failed) return ;

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
    // $ANTLR end "NL"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1861:5: ( '/*' ( options {greedy=false; } : ( . )* ) '*/' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:5: '/*' ( options {greedy=false; } : ( . )* ) '*/'
            {
            match("/*"); if (state.failed) return ;

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:10: ( options {greedy=false; } : ( . )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:40: ( . )*
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:40: ( . )*
            loop199:
            do {
                int alt199=2;
                int LA199_0 = input.LA(1);

                if ( (LA199_0=='*') ) {
                    int LA199_1 = input.LA(2);

                    if ( (LA199_1=='/') ) {
                        alt199=2;
                    }
                    else if ( ((LA199_1>='\u0000' && LA199_1<='.')||(LA199_1>='0' && LA199_1<='\uFFFF')) ) {
                        alt199=1;
                    }


                }
                else if ( ((LA199_0>='\u0000' && LA199_0<=')')||(LA199_0>='+' && LA199_0<='\uFFFF')) ) {
                    alt199=1;
                }


                switch (alt199) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:40: .
            	    {
            	    matchAny(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop199;
                }
            } while (true);


            }

            match("*/"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMENT"

    // $ANTLR start "LINE_COMMENT"
    public final void mLINE_COMMENT() throws RecognitionException {
        try {
            int _type = LINE_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1866:5: ( '//' ( options {greedy=false; } : ( . )* ) NL )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1867:5: '//' ( options {greedy=false; } : ( . )* ) NL
            {
            match("//"); if (state.failed) return ;

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1867:9: ( options {greedy=false; } : ( . )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1867:39: ( . )*
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1867:39: ( . )*
            loop200:
            do {
                int alt200=2;
                int LA200_0 = input.LA(1);

                if ( (LA200_0=='\r') ) {
                    alt200=2;
                }
                else if ( (LA200_0=='\n') ) {
                    alt200=2;
                }
                else if ( ((LA200_0>='\u0000' && LA200_0<='\t')||(LA200_0>='\u000B' && LA200_0<='\f')||(LA200_0>='\u000E' && LA200_0<='\uFFFF')) ) {
                    alt200=1;
                }


                switch (alt200) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1867:39: .
            	    {
            	    matchAny(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop200;
                }
            } while (true);


            }

            mNL(); if (state.failed) return ;
            if ( state.backtracking==0 ) {

              	_channel = HIDDEN;    
                  
            }

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LINE_COMMENT"

    public void mTokens() throws RecognitionException {
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:8: ( GEN | CDO | CDC | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS | GREATER | LBRACE | RBRACE | LBRACKET | RBRACKET | OPEQ | SEMI | COLON | DCOLON | SOLIDUS | MINUS | PLUS | STAR | LPAREN | RPAREN | COMMA | DOT | TILDE | PIPE | CP_EQ | CP_NOT_EQ | LESS | GREATER_OR_EQ | LESS_OR_EQ | LESS_WHEN | LESS_AND | LESS_DOTS | LESS_REST | STRING | ONLY | NOT | AND | OR | IDENT | HASH_SYMBOL | HASH | IMPORTANT_SYM | IMPORT_SYM | PAGE_SYM | MEDIA_SYM | NAMESPACE_SYM | CHARSET_SYM | COUNTER_STYLE_SYM | FONT_FACE_SYM | TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM | MOZ_DOCUMENT_SYM | WEBKIT_KEYFRAMES_SYM | SASS_CONTENT | SASS_MIXIN | SASS_INCLUDE | SASS_EXTEND | SASS_DEBUG | SASS_WARN | SASS_IF | SASS_ELSE | SASS_FOR | SASS_FUNCTION | SASS_RETURN | SASS_EACH | SASS_WHILE | AT_IDENT | SASS_VAR | SASS_DEFAULT | SASS_OPTIONAL | SASS_EXTEND_ONLY_SELECTOR | NUMBER | URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP | WS | NL | COMMENT | LINE_COMMENT )
        int alt201=97;
        alt201 = dfa201.predict(input);
        switch (alt201) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:10: GEN
                {
                mGEN(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:14: CDO
                {
                mCDO(); if (state.failed) return ;

                }
                break;
            case 3 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:18: CDC
                {
                mCDC(); if (state.failed) return ;

                }
                break;
            case 4 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:22: INCLUDES
                {
                mINCLUDES(); if (state.failed) return ;

                }
                break;
            case 5 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:31: DASHMATCH
                {
                mDASHMATCH(); if (state.failed) return ;

                }
                break;
            case 6 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:41: BEGINS
                {
                mBEGINS(); if (state.failed) return ;

                }
                break;
            case 7 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:48: ENDS
                {
                mENDS(); if (state.failed) return ;

                }
                break;
            case 8 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:53: CONTAINS
                {
                mCONTAINS(); if (state.failed) return ;

                }
                break;
            case 9 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:62: GREATER
                {
                mGREATER(); if (state.failed) return ;

                }
                break;
            case 10 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:70: LBRACE
                {
                mLBRACE(); if (state.failed) return ;

                }
                break;
            case 11 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:77: RBRACE
                {
                mRBRACE(); if (state.failed) return ;

                }
                break;
            case 12 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:84: LBRACKET
                {
                mLBRACKET(); if (state.failed) return ;

                }
                break;
            case 13 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:93: RBRACKET
                {
                mRBRACKET(); if (state.failed) return ;

                }
                break;
            case 14 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:102: OPEQ
                {
                mOPEQ(); if (state.failed) return ;

                }
                break;
            case 15 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:107: SEMI
                {
                mSEMI(); if (state.failed) return ;

                }
                break;
            case 16 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:112: COLON
                {
                mCOLON(); if (state.failed) return ;

                }
                break;
            case 17 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:118: DCOLON
                {
                mDCOLON(); if (state.failed) return ;

                }
                break;
            case 18 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:125: SOLIDUS
                {
                mSOLIDUS(); if (state.failed) return ;

                }
                break;
            case 19 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:133: MINUS
                {
                mMINUS(); if (state.failed) return ;

                }
                break;
            case 20 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:139: PLUS
                {
                mPLUS(); if (state.failed) return ;

                }
                break;
            case 21 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:144: STAR
                {
                mSTAR(); if (state.failed) return ;

                }
                break;
            case 22 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:149: LPAREN
                {
                mLPAREN(); if (state.failed) return ;

                }
                break;
            case 23 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:156: RPAREN
                {
                mRPAREN(); if (state.failed) return ;

                }
                break;
            case 24 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:163: COMMA
                {
                mCOMMA(); if (state.failed) return ;

                }
                break;
            case 25 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:169: DOT
                {
                mDOT(); if (state.failed) return ;

                }
                break;
            case 26 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:173: TILDE
                {
                mTILDE(); if (state.failed) return ;

                }
                break;
            case 27 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:179: PIPE
                {
                mPIPE(); if (state.failed) return ;

                }
                break;
            case 28 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:184: CP_EQ
                {
                mCP_EQ(); if (state.failed) return ;

                }
                break;
            case 29 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:190: CP_NOT_EQ
                {
                mCP_NOT_EQ(); if (state.failed) return ;

                }
                break;
            case 30 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:200: LESS
                {
                mLESS(); if (state.failed) return ;

                }
                break;
            case 31 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:205: GREATER_OR_EQ
                {
                mGREATER_OR_EQ(); if (state.failed) return ;

                }
                break;
            case 32 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:219: LESS_OR_EQ
                {
                mLESS_OR_EQ(); if (state.failed) return ;

                }
                break;
            case 33 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:230: LESS_WHEN
                {
                mLESS_WHEN(); if (state.failed) return ;

                }
                break;
            case 34 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:240: LESS_AND
                {
                mLESS_AND(); if (state.failed) return ;

                }
                break;
            case 35 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:249: LESS_DOTS
                {
                mLESS_DOTS(); if (state.failed) return ;

                }
                break;
            case 36 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:259: LESS_REST
                {
                mLESS_REST(); if (state.failed) return ;

                }
                break;
            case 37 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:269: STRING
                {
                mSTRING(); if (state.failed) return ;

                }
                break;
            case 38 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:276: ONLY
                {
                mONLY(); if (state.failed) return ;

                }
                break;
            case 39 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:281: NOT
                {
                mNOT(); if (state.failed) return ;

                }
                break;
            case 40 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:285: AND
                {
                mAND(); if (state.failed) return ;

                }
                break;
            case 41 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:289: OR
                {
                mOR(); if (state.failed) return ;

                }
                break;
            case 42 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:292: IDENT
                {
                mIDENT(); if (state.failed) return ;

                }
                break;
            case 43 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:298: HASH_SYMBOL
                {
                mHASH_SYMBOL(); if (state.failed) return ;

                }
                break;
            case 44 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:310: HASH
                {
                mHASH(); if (state.failed) return ;

                }
                break;
            case 45 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:315: IMPORTANT_SYM
                {
                mIMPORTANT_SYM(); if (state.failed) return ;

                }
                break;
            case 46 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:329: IMPORT_SYM
                {
                mIMPORT_SYM(); if (state.failed) return ;

                }
                break;
            case 47 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:340: PAGE_SYM
                {
                mPAGE_SYM(); if (state.failed) return ;

                }
                break;
            case 48 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:349: MEDIA_SYM
                {
                mMEDIA_SYM(); if (state.failed) return ;

                }
                break;
            case 49 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:359: NAMESPACE_SYM
                {
                mNAMESPACE_SYM(); if (state.failed) return ;

                }
                break;
            case 50 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:373: CHARSET_SYM
                {
                mCHARSET_SYM(); if (state.failed) return ;

                }
                break;
            case 51 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:385: COUNTER_STYLE_SYM
                {
                mCOUNTER_STYLE_SYM(); if (state.failed) return ;

                }
                break;
            case 52 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:403: FONT_FACE_SYM
                {
                mFONT_FACE_SYM(); if (state.failed) return ;

                }
                break;
            case 53 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:417: TOPLEFTCORNER_SYM
                {
                mTOPLEFTCORNER_SYM(); if (state.failed) return ;

                }
                break;
            case 54 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:435: TOPLEFT_SYM
                {
                mTOPLEFT_SYM(); if (state.failed) return ;

                }
                break;
            case 55 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:447: TOPCENTER_SYM
                {
                mTOPCENTER_SYM(); if (state.failed) return ;

                }
                break;
            case 56 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:461: TOPRIGHT_SYM
                {
                mTOPRIGHT_SYM(); if (state.failed) return ;

                }
                break;
            case 57 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:474: TOPRIGHTCORNER_SYM
                {
                mTOPRIGHTCORNER_SYM(); if (state.failed) return ;

                }
                break;
            case 58 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:493: BOTTOMLEFTCORNER_SYM
                {
                mBOTTOMLEFTCORNER_SYM(); if (state.failed) return ;

                }
                break;
            case 59 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:514: BOTTOMLEFT_SYM
                {
                mBOTTOMLEFT_SYM(); if (state.failed) return ;

                }
                break;
            case 60 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:529: BOTTOMCENTER_SYM
                {
                mBOTTOMCENTER_SYM(); if (state.failed) return ;

                }
                break;
            case 61 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:546: BOTTOMRIGHT_SYM
                {
                mBOTTOMRIGHT_SYM(); if (state.failed) return ;

                }
                break;
            case 62 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:562: BOTTOMRIGHTCORNER_SYM
                {
                mBOTTOMRIGHTCORNER_SYM(); if (state.failed) return ;

                }
                break;
            case 63 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:584: LEFTTOP_SYM
                {
                mLEFTTOP_SYM(); if (state.failed) return ;

                }
                break;
            case 64 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:596: LEFTMIDDLE_SYM
                {
                mLEFTMIDDLE_SYM(); if (state.failed) return ;

                }
                break;
            case 65 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:611: LEFTBOTTOM_SYM
                {
                mLEFTBOTTOM_SYM(); if (state.failed) return ;

                }
                break;
            case 66 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:626: RIGHTTOP_SYM
                {
                mRIGHTTOP_SYM(); if (state.failed) return ;

                }
                break;
            case 67 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:639: RIGHTMIDDLE_SYM
                {
                mRIGHTMIDDLE_SYM(); if (state.failed) return ;

                }
                break;
            case 68 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:655: RIGHTBOTTOM_SYM
                {
                mRIGHTBOTTOM_SYM(); if (state.failed) return ;

                }
                break;
            case 69 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:671: MOZ_DOCUMENT_SYM
                {
                mMOZ_DOCUMENT_SYM(); if (state.failed) return ;

                }
                break;
            case 70 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:688: WEBKIT_KEYFRAMES_SYM
                {
                mWEBKIT_KEYFRAMES_SYM(); if (state.failed) return ;

                }
                break;
            case 71 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:709: SASS_CONTENT
                {
                mSASS_CONTENT(); if (state.failed) return ;

                }
                break;
            case 72 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:722: SASS_MIXIN
                {
                mSASS_MIXIN(); if (state.failed) return ;

                }
                break;
            case 73 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:733: SASS_INCLUDE
                {
                mSASS_INCLUDE(); if (state.failed) return ;

                }
                break;
            case 74 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:746: SASS_EXTEND
                {
                mSASS_EXTEND(); if (state.failed) return ;

                }
                break;
            case 75 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:758: SASS_DEBUG
                {
                mSASS_DEBUG(); if (state.failed) return ;

                }
                break;
            case 76 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:769: SASS_WARN
                {
                mSASS_WARN(); if (state.failed) return ;

                }
                break;
            case 77 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:779: SASS_IF
                {
                mSASS_IF(); if (state.failed) return ;

                }
                break;
            case 78 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:787: SASS_ELSE
                {
                mSASS_ELSE(); if (state.failed) return ;

                }
                break;
            case 79 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:797: SASS_FOR
                {
                mSASS_FOR(); if (state.failed) return ;

                }
                break;
            case 80 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:806: SASS_FUNCTION
                {
                mSASS_FUNCTION(); if (state.failed) return ;

                }
                break;
            case 81 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:820: SASS_RETURN
                {
                mSASS_RETURN(); if (state.failed) return ;

                }
                break;
            case 82 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:832: SASS_EACH
                {
                mSASS_EACH(); if (state.failed) return ;

                }
                break;
            case 83 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:842: SASS_WHILE
                {
                mSASS_WHILE(); if (state.failed) return ;

                }
                break;
            case 84 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:853: AT_IDENT
                {
                mAT_IDENT(); if (state.failed) return ;

                }
                break;
            case 85 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:862: SASS_VAR
                {
                mSASS_VAR(); if (state.failed) return ;

                }
                break;
            case 86 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:871: SASS_DEFAULT
                {
                mSASS_DEFAULT(); if (state.failed) return ;

                }
                break;
            case 87 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:884: SASS_OPTIONAL
                {
                mSASS_OPTIONAL(); if (state.failed) return ;

                }
                break;
            case 88 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:898: SASS_EXTEND_ONLY_SELECTOR
                {
                mSASS_EXTEND_ONLY_SELECTOR(); if (state.failed) return ;

                }
                break;
            case 89 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:924: NUMBER
                {
                mNUMBER(); if (state.failed) return ;

                }
                break;
            case 90 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:931: URI
                {
                mURI(); if (state.failed) return ;

                }
                break;
            case 91 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:935: MOZ_URL_PREFIX
                {
                mMOZ_URL_PREFIX(); if (state.failed) return ;

                }
                break;
            case 92 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:950: MOZ_DOMAIN
                {
                mMOZ_DOMAIN(); if (state.failed) return ;

                }
                break;
            case 93 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:961: MOZ_REGEXP
                {
                mMOZ_REGEXP(); if (state.failed) return ;

                }
                break;
            case 94 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:972: WS
                {
                mWS(); if (state.failed) return ;

                }
                break;
            case 95 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:975: NL
                {
                mNL(); if (state.failed) return ;

                }
                break;
            case 96 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:978: COMMENT
                {
                mCOMMENT(); if (state.failed) return ;

                }
                break;
            case 97 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:986: LINE_COMMENT
                {
                mLINE_COMMENT(); if (state.failed) return ;

                }
                break;

        }

    }

    // $ANTLR start synpred1_Css3
    public final void synpred1_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1750:15: ( D P ( I | C ) )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1750:16: D P ( I | C )
        {
        mD(); if (state.failed) return ;
        mP(); if (state.failed) return ;
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1750:20: ( I | C )
        int alt202=2;
        switch ( input.LA(1) ) {
        case 'I':
        case 'i':
            {
            alt202=1;
            }
            break;
        case '\\':
            {
            switch ( input.LA(2) ) {
            case 'I':
            case 'i':
                {
                alt202=1;
                }
                break;
            case '0':
                {
                int LA202_4 = input.LA(3);

                if ( (LA202_4=='0') ) {
                    int LA202_6 = input.LA(4);

                    if ( (LA202_6=='0') ) {
                        int LA202_7 = input.LA(5);

                        if ( (LA202_7=='0') ) {
                            int LA202_8 = input.LA(6);

                            if ( (LA202_8=='4'||LA202_8=='6') ) {
                                int LA202_5 = input.LA(7);

                                if ( (LA202_5=='9') ) {
                                    alt202=1;
                                }
                                else if ( (LA202_5=='3') ) {
                                    alt202=2;
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 202, 5, input);

                                    throw nvae;
                                }
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 202, 8, input);

                                throw nvae;
                            }
                        }
                        else if ( (LA202_7=='4'||LA202_7=='6') ) {
                            int LA202_5 = input.LA(6);

                            if ( (LA202_5=='9') ) {
                                alt202=1;
                            }
                            else if ( (LA202_5=='3') ) {
                                alt202=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 202, 5, input);

                                throw nvae;
                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 202, 7, input);

                            throw nvae;
                        }
                    }
                    else if ( (LA202_6=='4'||LA202_6=='6') ) {
                        int LA202_5 = input.LA(5);

                        if ( (LA202_5=='9') ) {
                            alt202=1;
                        }
                        else if ( (LA202_5=='3') ) {
                            alt202=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 202, 5, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 202, 6, input);

                        throw nvae;
                    }
                }
                else if ( (LA202_4=='4'||LA202_4=='6') ) {
                    int LA202_5 = input.LA(4);

                    if ( (LA202_5=='9') ) {
                        alt202=1;
                    }
                    else if ( (LA202_5=='3') ) {
                        alt202=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 202, 5, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 202, 4, input);

                    throw nvae;
                }
                }
                break;
            case '4':
            case '6':
                {
                int LA202_5 = input.LA(3);

                if ( (LA202_5=='9') ) {
                    alt202=1;
                }
                else if ( (LA202_5=='3') ) {
                    alt202=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 202, 5, input);

                    throw nvae;
                }
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 202, 2, input);

                throw nvae;
            }

            }
            break;
        case 'C':
        case 'c':
            {
            alt202=2;
            }
            break;
        default:
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 202, 0, input);

            throw nvae;
        }

        switch (alt202) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1750:21: I
                {
                mI(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1750:23: C
                {
                mC(); if (state.failed) return ;

                }
                break;

        }


        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1757:15: ( E ( M | X ) )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1757:16: E ( M | X )
        {
        mE(); if (state.failed) return ;
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1757:18: ( M | X )
        int alt203=2;
        switch ( input.LA(1) ) {
        case 'M':
        case 'm':
            {
            alt203=1;
            }
            break;
        case '\\':
            {
            switch ( input.LA(2) ) {
            case '4':
            case '6':
            case 'M':
            case 'm':
                {
                alt203=1;
                }
                break;
            case '0':
                {
                switch ( input.LA(3) ) {
                case '0':
                    {
                    switch ( input.LA(4) ) {
                    case '0':
                        {
                        switch ( input.LA(5) ) {
                        case '0':
                            {
                            int LA203_7 = input.LA(6);

                            if ( (LA203_7=='4'||LA203_7=='6') ) {
                                alt203=1;
                            }
                            else if ( (LA203_7=='5'||LA203_7=='7') ) {
                                alt203=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 203, 7, input);

                                throw nvae;
                            }
                            }
                            break;
                        case '4':
                        case '6':
                            {
                            alt203=1;
                            }
                            break;
                        case '5':
                        case '7':
                            {
                            alt203=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 203, 6, input);

                            throw nvae;
                        }

                        }
                        break;
                    case '4':
                    case '6':
                        {
                        alt203=1;
                        }
                        break;
                    case '5':
                    case '7':
                        {
                        alt203=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 203, 5, input);

                        throw nvae;
                    }

                    }
                    break;
                case '4':
                case '6':
                    {
                    alt203=1;
                    }
                    break;
                case '5':
                case '7':
                    {
                    alt203=2;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 203, 4, input);

                    throw nvae;
                }

                }
                break;
            case '5':
            case '7':
            case 'X':
            case 'x':
                {
                alt203=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 203, 2, input);

                throw nvae;
            }

            }
            break;
        case 'X':
        case 'x':
            {
            alt203=2;
            }
            break;
        default:
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 203, 0, input);

            throw nvae;
        }

        switch (alt203) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1757:19: M
                {
                mM(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1757:21: X
                {
                mX(); if (state.failed) return ;

                }
                break;

        }


        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1763:15: ( P ( X | T | C ) )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1763:16: P ( X | T | C )
        {
        mP(); if (state.failed) return ;
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1763:17: ( X | T | C )
        int alt204=3;
        alt204 = dfa204.predict(input);
        switch (alt204) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1763:18: X
                {
                mX(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1763:20: T
                {
                mT(); if (state.failed) return ;

                }
                break;
            case 3 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1763:22: C
                {
                mC(); if (state.failed) return ;

                }
                break;

        }


        }
    }
    // $ANTLR end synpred3_Css3

    // $ANTLR start synpred4_Css3
    public final void synpred4_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1771:15: ( C M )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1771:16: C M
        {
        mC(); if (state.failed) return ;
        mM(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Css3

    // $ANTLR start synpred5_Css3
    public final void synpred5_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1773:15: ( M ( M | S ) )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1773:16: M ( M | S )
        {
        mM(); if (state.failed) return ;
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1773:18: ( M | S )
        int alt205=2;
        switch ( input.LA(1) ) {
        case 'M':
        case 'm':
            {
            alt205=1;
            }
            break;
        case '\\':
            {
            switch ( input.LA(2) ) {
            case '4':
            case '6':
            case 'M':
            case 'm':
                {
                alt205=1;
                }
                break;
            case '0':
                {
                switch ( input.LA(3) ) {
                case '0':
                    {
                    switch ( input.LA(4) ) {
                    case '0':
                        {
                        switch ( input.LA(5) ) {
                        case '0':
                            {
                            int LA205_7 = input.LA(6);

                            if ( (LA205_7=='4'||LA205_7=='6') ) {
                                alt205=1;
                            }
                            else if ( (LA205_7=='5'||LA205_7=='7') ) {
                                alt205=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 205, 7, input);

                                throw nvae;
                            }
                            }
                            break;
                        case '4':
                        case '6':
                            {
                            alt205=1;
                            }
                            break;
                        case '5':
                        case '7':
                            {
                            alt205=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 205, 6, input);

                            throw nvae;
                        }

                        }
                        break;
                    case '4':
                    case '6':
                        {
                        alt205=1;
                        }
                        break;
                    case '5':
                    case '7':
                        {
                        alt205=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 205, 5, input);

                        throw nvae;
                    }

                    }
                    break;
                case '4':
                case '6':
                    {
                    alt205=1;
                    }
                    break;
                case '5':
                case '7':
                    {
                    alt205=2;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 205, 4, input);

                    throw nvae;
                }

                }
                break;
            case '5':
            case '7':
            case 'S':
            case 's':
                {
                alt205=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 205, 2, input);

                throw nvae;
            }

            }
            break;
        case 'S':
        case 's':
            {
            alt205=2;
            }
            break;
        default:
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 205, 0, input);

            throw nvae;
        }

        switch (alt205) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1773:19: M
                {
                mM(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1773:21: S
                {
                mS(); if (state.failed) return ;

                }
                break;

        }


        }
    }
    // $ANTLR end synpred5_Css3

    // $ANTLR start synpred6_Css3
    public final void synpred6_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1780:15: ( I N )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1780:16: I N
        {
        mI(); if (state.failed) return ;
        mN(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Css3

    // $ANTLR start synpred7_Css3
    public final void synpred7_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1783:15: ( D E G )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1783:16: D E G
        {
        mD(); if (state.failed) return ;
        mE(); if (state.failed) return ;
        mG(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Css3

    // $ANTLR start synpred8_Css3
    public final void synpred8_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1788:15: ( R ( A | E ) )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1788:16: R ( A | E )
        {
        mR(); if (state.failed) return ;
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1788:18: ( A | E )
        int alt206=2;
        switch ( input.LA(1) ) {
        case 'A':
        case 'a':
            {
            alt206=1;
            }
            break;
        case '\\':
            {
            int LA206_2 = input.LA(2);

            if ( (LA206_2=='0') ) {
                int LA206_4 = input.LA(3);

                if ( (LA206_4=='0') ) {
                    int LA206_6 = input.LA(4);

                    if ( (LA206_6=='0') ) {
                        int LA206_7 = input.LA(5);

                        if ( (LA206_7=='0') ) {
                            int LA206_8 = input.LA(6);

                            if ( (LA206_8=='4'||LA206_8=='6') ) {
                                int LA206_5 = input.LA(7);

                                if ( (LA206_5=='1') ) {
                                    alt206=1;
                                }
                                else if ( (LA206_5=='5') ) {
                                    alt206=2;
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 206, 5, input);

                                    throw nvae;
                                }
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 206, 8, input);

                                throw nvae;
                            }
                        }
                        else if ( (LA206_7=='4'||LA206_7=='6') ) {
                            int LA206_5 = input.LA(6);

                            if ( (LA206_5=='1') ) {
                                alt206=1;
                            }
                            else if ( (LA206_5=='5') ) {
                                alt206=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 206, 5, input);

                                throw nvae;
                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 206, 7, input);

                            throw nvae;
                        }
                    }
                    else if ( (LA206_6=='4'||LA206_6=='6') ) {
                        int LA206_5 = input.LA(5);

                        if ( (LA206_5=='1') ) {
                            alt206=1;
                        }
                        else if ( (LA206_5=='5') ) {
                            alt206=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 206, 5, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 206, 6, input);

                        throw nvae;
                    }
                }
                else if ( (LA206_4=='4'||LA206_4=='6') ) {
                    int LA206_5 = input.LA(4);

                    if ( (LA206_5=='1') ) {
                        alt206=1;
                    }
                    else if ( (LA206_5=='5') ) {
                        alt206=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 206, 5, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 206, 4, input);

                    throw nvae;
                }
            }
            else if ( (LA206_2=='4'||LA206_2=='6') ) {
                int LA206_5 = input.LA(3);

                if ( (LA206_5=='1') ) {
                    alt206=1;
                }
                else if ( (LA206_5=='5') ) {
                    alt206=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 206, 5, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 206, 2, input);

                throw nvae;
            }
            }
            break;
        case 'E':
        case 'e':
            {
            alt206=2;
            }
            break;
        default:
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 206, 0, input);

            throw nvae;
        }

        switch (alt206) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1788:19: A
                {
                mA(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1788:21: E
                {
                mE(); if (state.failed) return ;

                }
                break;

        }


        }
    }
    // $ANTLR end synpred8_Css3

    // $ANTLR start synpred9_Css3
    public final void synpred9_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1795:15: ( S )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1795:16: S
        {
        mS(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_Css3

    // $ANTLR start synpred10_Css3
    public final void synpred10_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1797:15: ( ( K )? H Z )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1797:16: ( K )? H Z
        {
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1797:16: ( K )?
        int alt207=2;
        int LA207_0 = input.LA(1);

        if ( (LA207_0=='K'||LA207_0=='k') ) {
            alt207=1;
        }
        else if ( (LA207_0=='\\') ) {
            switch ( input.LA(2) ) {
                case 'K':
                case 'k':
                    {
                    alt207=1;
                    }
                    break;
                case '0':
                    {
                    int LA207_4 = input.LA(3);

                    if ( (LA207_4=='0') ) {
                        int LA207_6 = input.LA(4);

                        if ( (LA207_6=='0') ) {
                            int LA207_7 = input.LA(5);

                            if ( (LA207_7=='0') ) {
                                int LA207_8 = input.LA(6);

                                if ( (LA207_8=='4'||LA207_8=='6') ) {
                                    int LA207_5 = input.LA(7);

                                    if ( (LA207_5=='B'||LA207_5=='b') ) {
                                        alt207=1;
                                    }
                                }
                            }
                            else if ( (LA207_7=='4'||LA207_7=='6') ) {
                                int LA207_5 = input.LA(6);

                                if ( (LA207_5=='B'||LA207_5=='b') ) {
                                    alt207=1;
                                }
                            }
                        }
                        else if ( (LA207_6=='4'||LA207_6=='6') ) {
                            int LA207_5 = input.LA(5);

                            if ( (LA207_5=='B'||LA207_5=='b') ) {
                                alt207=1;
                            }
                        }
                    }
                    else if ( (LA207_4=='4'||LA207_4=='6') ) {
                        int LA207_5 = input.LA(4);

                        if ( (LA207_5=='B'||LA207_5=='b') ) {
                            alt207=1;
                        }
                    }
                    }
                    break;
                case '4':
                case '6':
                    {
                    int LA207_5 = input.LA(3);

                    if ( (LA207_5=='B'||LA207_5=='b') ) {
                        alt207=1;
                    }
                    }
                    break;
            }

        }
        switch (alt207) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1797:16: K
                {
                mK(); if (state.failed) return ;

                }
                break;

        }

        mH(); if (state.failed) return ;
        mZ(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_Css3

    // $ANTLR start synpred11_Css3
    public final void synpred11_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1813:14: ( WS )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1813:15: WS
        {
        mWS(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_Css3

    // $ANTLR start synpred12_Css3
    public final void synpred12_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1820:14: ( WS )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1820:15: WS
        {
        mWS(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_Css3

    // $ANTLR start synpred13_Css3
    public final void synpred13_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1828:14: ( WS )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1828:15: WS
        {
        mWS(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_Css3

    // $ANTLR start synpred14_Css3
    public final void synpred14_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1836:14: ( WS )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1836:15: WS
        {
        mWS(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_Css3

    public final boolean synpred5_Css3() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred10_Css3() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred10_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred11_Css3() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred11_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred14_Css3() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred14_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred7_Css3() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred7_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred8_Css3() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred8_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred6_Css3() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred6_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_Css3() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_Css3() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred13_Css3() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred13_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred9_Css3() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred9_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred1_Css3() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred12_Css3() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred12_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred4_Css3() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred4_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA11 dfa11 = new DFA11(this);
    protected DFA184 dfa184 = new DFA184(this);
    protected DFA180 dfa180 = new DFA180(this);
    protected DFA201 dfa201 = new DFA201(this);
    protected DFA204 dfa204 = new DFA204(this);
    static final String DFA11_eotS =
        "\1\1\22\uffff";
    static final String DFA11_eofS =
        "\23\uffff";
    static final String DFA11_minS =
        "\1\41\22\uffff";
    static final String DFA11_maxS =
        "\1\uffff\22\uffff";
    static final String DFA11_acceptS =
        "\1\uffff\1\22\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\1\15\1\16\1\17\1\20\1\21";
    static final String DFA11_specialS =
        "\23\uffff}>";
    static final String[] DFA11_transitionS = {
            "\1\3\1\uffff\1\4\1\5\1\6\1\7\3\uffff\1\10\1\21\1\20\1\22\1\12"+
            "\1\14\12\22\1\13\1\17\1\uffff\1\16\1\uffff\1\15\1\uffff\32\22"+
            "\1\2\1\22\2\uffff\1\22\1\uffff\32\22\3\uffff\1\11\1\uffff\uff80"+
            "\22",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
    static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
    static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
    static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
    static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
    static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
    static final short[][] DFA11_transition;

    static {
        int numStates = DFA11_transitionS.length;
        DFA11_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
        }
    }

    class DFA11 extends DFA {

        public DFA11(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 11;
            this.eot = DFA11_eot;
            this.eof = DFA11_eof;
            this.min = DFA11_min;
            this.max = DFA11_max;
            this.accept = DFA11_accept;
            this.special = DFA11_special;
            this.transition = DFA11_transition;
        }
        public String getDescription() {
            return "()* loopback of 1382:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | NMCHAR )*";
        }
    }
    static final String DFA184_eotS =
        "\1\30\1\14\1\uffff\6\14\1\uffff\2\14\1\uffff\7\14\1\uffff\2\14\2"+
        "\uffff\1\14\1\uffff\16\14\2\uffff\4\14\27\uffff\1\14\1\uffff\3\14"+
        "\1\uffff\1\14\1\uffff\1\14\5\uffff\1\14\1\uffff\6\14\3\uffff\16"+
        "\14\5\uffff\2\14\1\uffff\1\14\4\uffff\2\14\1\uffff\1\14\3\uffff"+
        "\2\14\4\uffff\2\14\1\uffff\1\14\3\uffff\2\14\3\uffff\2\14\3\uffff"+
        "\4\14\3\uffff\2\14\3\uffff\2\14\3\uffff\5\14\3\uffff\20\14\1\uffff"+
        "\2\14\2\uffff\7\14\3\uffff\3\14\3\uffff\2\14\2\uffff\3\14\3\uffff"+
        "\2\14\3\uffff\6\14\2\uffff\7\14\2\uffff\2\14\1\uffff\1\14\2\uffff"+
        "\13\14\1\uffff\16\14\1\uffff\2\14\2\uffff\4\14\2\uffff\3\14\3\uffff"+
        "\3\14\3\uffff\2\14\2\uffff\3\14\3\uffff\2\14\2\uffff\2\14\1\uffff"+
        "\4\14\2\uffff\2\14\2\uffff\5\14\2\uffff\2\14\1\uffff\3\14\2\uffff"+
        "\11\14\1\uffff\15\14\1\uffff\2\14\2\uffff\4\14\2\uffff\3\14\3\uffff"+
        "\3\14\3\uffff\2\14\2\uffff\3\14\3\uffff\2\14\2\uffff\2\14\1\uffff"+
        "\4\14\2\uffff\2\14\2\uffff\5\14\2\uffff\2\14\1\uffff\3\14\2\uffff"+
        "\10\14\1\uffff\13\14\1\uffff\2\14\2\uffff\4\14\2\uffff\2\14\3\uffff"+
        "\2\14\3\uffff\1\14\2\uffff\2\14\3\uffff\1\14\2\uffff\2\14\1\uffff"+
        "\3\14\2\uffff\2\14\2\uffff\3\14\2\uffff\1\14\1\uffff\3\14\2\uffff"+
        "\5\14\16\uffff\1\14\1\uffff\2\14\2\uffff\1\14\2\uffff\1\14\3\uffff"+
        "\2\14\10\uffff";
    static final String DFA184_eofS =
        "\u0225\uffff";
    static final String DFA184_minS =
        "\1\45\1\105\1\0\1\115\1\103\2\115\1\116\1\101\1\0\1\110\1\132\1"+
        "\uffff\1\105\1\115\1\103\2\115\1\116\1\101\1\0\1\110\1\132\2\uffff"+
        "\1\103\1\0\1\107\1\103\1\107\1\103\1\60\1\63\1\103\1\115\1\60\1"+
        "\115\2\116\2\101\2\0\2\110\2\132\27\0\1\104\1\0\1\115\1\104\1\115"+
        "\1\uffff\1\132\1\0\1\132\5\0\1\115\1\0\1\115\2\103\2\60\1\65\3\0"+
        "\1\60\1\63\1\60\1\105\3\115\1\116\1\110\1\132\1\115\1\110\1\103"+
        "\1\101\1\0\1\uffff\3\0\1\60\1\104\1\0\1\70\1\uffff\3\0\1\60\1\64"+
        "\1\0\1\63\1\uffff\2\0\1\60\1\104\1\uffff\3\0\1\60\1\104\1\0\1\63"+
        "\1\uffff\2\0\1\60\1\105\3\0\1\60\1\61\3\0\2\132\1\60\1\70\1\uffff"+
        "\2\0\1\60\1\101\1\uffff\2\0\1\60\1\63\3\0\2\60\1\65\1\103\1\107"+
        "\1\uffff\2\0\1\60\1\67\1\60\1\63\1\60\1\105\3\115\1\116\1\110\1"+
        "\132\1\115\1\110\1\103\1\101\1\0\2\107\2\0\1\104\1\115\1\104\1\115"+
        "\1\60\1\104\1\70\3\0\1\60\1\64\1\63\3\0\1\60\1\104\2\0\1\60\1\104"+
        "\1\63\3\0\1\60\1\105\2\0\1\uffff\1\60\1\64\1\60\1\61\1\104\1\115"+
        "\2\0\1\60\1\104\1\60\1\70\1\132\1\60\1\101\2\0\1\60\1\63\1\0\1\115"+
        "\2\0\1\60\1\104\2\60\1\65\1\103\1\107\2\115\1\60\1\67\1\0\1\64\1"+
        "\63\1\60\1\105\3\115\1\116\1\110\1\132\1\115\1\110\1\103\1\101\1"+
        "\0\2\107\2\0\1\104\1\115\1\104\1\115\2\0\1\60\1\104\1\70\3\0\1\60"+
        "\1\64\1\63\3\0\1\60\1\104\2\0\1\60\1\104\1\63\3\0\1\60\1\105\2\0"+
        "\1\60\1\64\1\0\1\60\1\61\1\104\1\115\2\0\1\60\1\104\2\0\1\60\1\70"+
        "\1\132\1\60\1\101\2\0\1\60\1\63\1\0\1\115\1\60\1\104\2\0\1\64\1"+
        "\60\1\65\1\103\1\107\2\115\1\60\1\67\1\0\1\63\1\60\1\105\3\115\1"+
        "\116\1\110\1\132\1\115\1\110\1\103\1\101\1\0\2\107\2\0\1\104\1\115"+
        "\1\104\1\115\2\0\1\64\1\104\1\70\3\0\2\64\1\63\3\0\1\64\1\104\2"+
        "\0\1\64\1\104\1\63\3\0\1\64\1\105\2\0\1\60\1\64\1\0\1\64\1\61\1"+
        "\104\1\115\2\0\1\60\1\104\2\0\1\64\1\70\1\132\1\65\1\101\2\0\1\64"+
        "\1\63\1\0\1\115\1\60\1\104\2\0\1\60\1\65\1\103\1\107\2\115\1\64"+
        "\1\67\1\0\1\105\3\115\1\116\1\110\1\132\1\115\1\110\1\103\1\101"+
        "\1\0\2\107\2\0\1\104\1\115\1\104\1\115\2\0\1\104\1\70\3\0\1\64\1"+
        "\63\3\0\1\104\2\0\1\104\1\63\3\0\1\105\2\0\2\64\1\0\1\61\1\104\1"+
        "\115\2\0\1\64\1\104\2\0\1\70\1\132\1\101\2\0\1\63\1\0\1\115\1\64"+
        "\1\104\2\0\1\103\1\107\2\115\1\67\16\0\1\64\1\0\1\104\1\115\2\0"+
        "\1\104\2\0\1\132\3\0\1\115\1\104\10\0";
    static final String DFA184_maxS =
        "\1\uffff\1\160\1\uffff\2\170\1\155\1\163\1\156\1\145\1\0\1\150\1"+
        "\172\1\uffff\1\160\2\170\1\155\1\163\1\156\1\145\1\0\1\150\1\172"+
        "\2\uffff\1\151\1\uffff\1\147\1\151\1\147\1\170\1\67\1\144\1\170"+
        "\1\163\1\63\1\163\2\156\2\145\2\0\2\150\2\172\1\0\1\uffff\4\0\1"+
        "\uffff\6\0\1\uffff\2\0\1\uffff\4\0\1\uffff\1\0\1\144\1\uffff\1\155"+
        "\1\144\1\155\1\uffff\1\172\1\uffff\1\172\1\0\1\uffff\2\0\1\uffff"+
        "\1\155\1\0\1\155\2\151\1\67\1\60\1\65\1\0\1\uffff\1\0\1\67\1\144"+
        "\1\63\1\160\1\170\1\155\1\163\1\156\1\150\1\172\1\163\1\150\1\170"+
        "\1\145\1\0\1\uffff\3\0\1\67\1\144\1\0\1\70\1\uffff\3\0\1\67\1\70"+
        "\1\0\1\63\1\uffff\2\0\1\66\1\144\1\uffff\3\0\1\67\1\144\1\0\1\63"+
        "\1\uffff\2\0\1\66\1\145\1\0\1\uffff\1\0\1\66\1\65\1\0\1\uffff\1"+
        "\0\2\172\1\66\1\70\1\uffff\2\0\1\67\1\141\1\uffff\2\0\1\66\1\71"+
        "\1\0\1\uffff\1\0\1\67\1\60\1\65\1\151\1\147\1\uffff\2\0\1\66\2\67"+
        "\1\144\1\63\1\160\1\170\1\155\1\163\1\156\1\150\1\172\1\163\1\150"+
        "\1\170\1\145\1\0\2\147\2\0\1\144\1\155\1\144\1\155\1\67\1\144\1"+
        "\70\3\0\1\67\1\70\1\63\3\0\1\66\1\144\2\0\1\67\1\144\1\63\3\0\1"+
        "\66\1\145\2\0\1\uffff\1\66\1\64\1\66\1\65\1\144\1\155\2\0\1\66\1"+
        "\144\1\66\1\70\1\172\1\67\1\141\2\0\1\66\1\71\1\0\1\155\2\0\1\66"+
        "\1\144\1\67\1\60\1\65\1\151\1\147\2\155\1\66\1\67\1\0\1\67\1\144"+
        "\1\63\1\160\1\170\1\155\1\163\1\156\1\150\1\172\1\163\1\150\1\170"+
        "\1\145\1\0\2\147\2\0\1\144\1\155\1\144\1\155\2\0\1\67\1\144\1\70"+
        "\3\0\1\67\1\70\1\63\3\0\1\66\1\144\2\0\1\67\1\144\1\63\3\0\1\66"+
        "\1\145\2\0\1\66\1\64\1\0\1\66\1\65\1\144\1\155\2\0\1\66\1\144\2"+
        "\0\1\66\1\70\1\172\1\67\1\141\2\0\1\66\1\71\1\0\1\155\1\66\1\144"+
        "\2\0\1\67\1\60\1\65\1\151\1\147\2\155\1\66\1\67\1\0\1\144\1\63\1"+
        "\160\1\170\1\155\1\163\1\156\1\150\1\172\1\163\1\150\1\170\1\145"+
        "\1\0\2\147\2\0\1\144\1\155\1\144\1\155\2\0\1\67\1\144\1\70\3\0\1"+
        "\67\1\70\1\63\3\0\1\66\1\144\2\0\1\67\1\144\1\63\3\0\1\66\1\145"+
        "\2\0\1\66\1\64\1\0\1\66\1\65\1\144\1\155\2\0\1\66\1\144\2\0\1\66"+
        "\1\70\1\172\1\67\1\141\2\0\1\66\1\71\1\0\1\155\1\66\1\144\2\0\1"+
        "\60\1\65\1\151\1\147\2\155\1\66\1\67\1\0\1\160\1\170\1\155\1\163"+
        "\1\156\1\150\1\172\1\163\1\150\1\170\1\145\1\0\2\147\2\0\1\144\1"+
        "\155\1\144\1\155\2\0\1\144\1\70\3\0\1\70\1\63\3\0\1\144\2\0\1\144"+
        "\1\63\3\0\1\145\2\0\1\66\1\64\1\0\1\65\1\144\1\155\2\0\1\66\1\144"+
        "\2\0\1\70\1\172\1\141\2\0\1\71\1\0\1\155\1\66\1\144\2\0\1\151\1"+
        "\147\2\155\1\67\16\0\1\64\1\0\1\144\1\155\2\0\1\144\2\0\1\172\3"+
        "\0\1\155\1\144\10\0";
    static final String DFA184_acceptS =
        "\14\uffff\1\13\12\uffff\1\14\1\15\62\uffff\1\11\42\uffff\1\2\7\uffff"+
        "\1\3\7\uffff\1\4\4\uffff\1\5\7\uffff\1\6\20\uffff\1\12\4\uffff\1"+
        "\1\14\uffff\1\7\65\uffff\1\10\u0140\uffff";
    static final String DFA184_specialS =
        "\2\uffff\1\175\6\uffff\1\25\12\uffff\1\24\5\uffff\1\154\16\uffff"+
        "\1\45\1\46\4\uffff\1\10\1\u00a1\1\55\1\6\1\53\1\37\1\11\1\136\1"+
        "\u00ac\1\35\1\140\1\u00ae\1\146\1\u00b1\1\145\1\u00b6\1\u00bb\1"+
        "\131\1\u00b5\1\132\1\36\1\12\1\34\1\uffff\1\u0089\5\uffff\1\u00bf"+
        "\1\uffff\1\33\1\151\1\32\1\126\1\u00a4\1\uffff\1\125\6\uffff\1\30"+
        "\1\150\1\26\16\uffff\1\u00a0\1\uffff\1\27\1\31\1\142\2\uffff\1\141"+
        "\2\uffff\1\124\1\123\1\u00c8\2\uffff\1\u00d1\2\uffff\1\u008c\1\u008d"+
        "\3\uffff\1\50\1\47\1\u00b9\2\uffff\1\u00cb\2\uffff\1\144\1\143\2"+
        "\uffff\1\170\1\130\1\171\2\uffff\1\u0098\1\u00be\1\u0096\5\uffff"+
        "\1\u00a8\1\u00a5\3\uffff\1\u00bc\1\u00bd\2\uffff\1\56\1\u00ce\1"+
        "\57\6\uffff\1\14\1\13\20\uffff\1\54\2\uffff\1\u00b8\1\u00ba\7\uffff"+
        "\1\176\1\177\1\70\3\uffff\1\64\1\u0091\1\u00d3\2\uffff\1\7\1\5\3"+
        "\uffff\1\67\1\66\1\u009d\2\uffff\1\102\1\73\7\uffff\1\75\1\74\7"+
        "\uffff\1\42\1\43\2\uffff\1\0\1\uffff\1\4\1\3\13\uffff\1\u0092\16"+
        "\uffff\1\u0090\2\uffff\1\62\1\63\4\uffff\1\u0095\1\u0094\3\uffff"+
        "\1\135\1\127\1\u00c0\3\uffff\1\u00c7\1\105\1\u00cd\2\uffff\1\u0080"+
        "\1\167\3\uffff\1\17\1\20\1\117\2\uffff\1\u00c5\1\u00c4\2\uffff\1"+
        "\u0097\4\uffff\1\u008a\1\u008b\2\uffff\1\106\1\107\5\uffff\1\153"+
        "\1\152\2\uffff\1\u00c9\3\uffff\1\156\1\157\11\uffff\1\u00a6\15\uffff"+
        "\1\u00aa\2\uffff\1\u0085\1\u0084\4\uffff\1\u00b2\1\u00b0\3\uffff"+
        "\1\u00b4\1\u00b3\1\147\3\uffff\1\155\1\16\1\u0083\2\uffff\1\u00a7"+
        "\1\u00ab\3\uffff\1\u009b\1\u009a\1\22\2\uffff\1\u009e\1\u009f\2"+
        "\uffff\1\u0093\4\uffff\1\113\1\114\2\uffff\1\u00cc\1\u00ca\5\uffff"+
        "\1\122\1\121\2\uffff\1\65\3\uffff\1\115\1\112\10\uffff\1\120\13"+
        "\uffff\1\40\2\uffff\1\u00cf\1\u00d0\4\uffff\1\60\1\61\2\uffff\1"+
        "\163\1\165\1\76\2\uffff\1\44\1\u0087\1\15\1\uffff\1\160\1\161\2"+
        "\uffff\1\134\1\133\1\173\1\uffff\1\174\1\172\2\uffff\1\23\3\uffff"+
        "\1\u00a3\1\u00a2\2\uffff\1\u0082\1\u0081\3\uffff\1\52\1\51\1\uffff"+
        "\1\41\3\uffff\1\u00c3\1\u00c6\5\uffff\1\166\1\110\1\111\1\u00d2"+
        "\1\u00c2\1\77\1\21\1\u008e\1\u008f\1\2\1\1\1\104\1\u00af\1\u00ad"+
        "\1\uffff\1\u00a9\2\uffff\1\103\1\101\1\uffff\1\72\1\71\1\uffff\1"+
        "\162\1\164\1\u00b7\2\uffff\1\u0088\1\u0086\1\u00c1\1\100\1\u0099"+
        "\1\u009c\1\137\1\116}>";
    static final String[] DFA184_transitionS = {
            "\1\27\7\uffff\1\14\23\uffff\2\14\1\20\1\15\1\16\2\14\1\26\1"+
            "\22\1\14\1\25\1\14\1\21\2\14\1\17\1\14\1\23\1\24\7\14\1\uffff"+
            "\1\2\2\uffff\1\14\1\uffff\2\14\1\5\1\1\1\3\2\14\1\13\1\7\1\14"+
            "\1\12\1\14\1\6\2\14\1\4\1\14\1\10\1\11\7\14\5\uffff\uff80\14",
            "\1\35\12\uffff\1\34\13\uffff\1\32\10\uffff\1\33\12\uffff\1"+
            "\31",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\37\3\14\1\40\1\43\1\40"+
            "\1\43\20\14\1\56\1\46\1\14\1\54\1\14\1\44\2\14\1\41\1\14\1\50"+
            "\1\52\24\14\1\55\1\45\1\14\1\53\1\14\1\42\2\14\1\36\1\14\1\47"+
            "\1\51\uff8c\14",
            "\1\62\12\uffff\1\63\3\uffff\1\60\20\uffff\1\57\12\uffff\1\61",
            "\1\72\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1\67"+
            "\20\uffff\1\66\3\uffff\1\64",
            "\1\75\16\uffff\1\74\20\uffff\1\73",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\111\3\uffff\1\112\26\uffff\1\107\4\uffff\1\106\3\uffff\1"+
            "\110",
            "\1\uffff",
            "\1\116\23\uffff\1\115\13\uffff\1\114",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "",
            "\1\35\12\uffff\1\34\13\uffff\1\32\10\uffff\1\33\12\uffff\1"+
            "\31",
            "\1\62\12\uffff\1\63\3\uffff\1\60\20\uffff\1\57\12\uffff\1\61",
            "\1\72\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1\67"+
            "\20\uffff\1\66\3\uffff\1\64",
            "\1\75\16\uffff\1\74\20\uffff\1\73",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\111\3\uffff\1\112\26\uffff\1\107\4\uffff\1\106\3\uffff\1"+
            "\110",
            "\1\uffff",
            "\1\116\23\uffff\1\115\13\uffff\1\114",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "",
            "",
            "\1\126\5\uffff\1\125\22\uffff\1\123\6\uffff\1\124\5\uffff\1"+
            "\122",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\131\3\14\1\133\1\132\1"+
            "\133\1\132\30\14\1\130\37\14\1\127\uff8f\14",
            "\1\136\24\uffff\1\135\12\uffff\1\134",
            "\1\126\5\uffff\1\125\22\uffff\1\123\6\uffff\1\124\5\uffff\1"+
            "\122",
            "\1\136\24\uffff\1\135\12\uffff\1\134",
            "\1\72\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1\67"+
            "\20\uffff\1\66\3\uffff\1\64",
            "\1\137\3\uffff\1\140\1\141\1\140\1\141",
            "\1\144\1\142\1\143\2\uffff\1\150\1\146\10\uffff\1\152\1\uffff"+
            "\1\151\35\uffff\1\147\1\uffff\1\145",
            "\1\72\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1\67"+
            "\20\uffff\1\66\3\uffff\1\64",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\153\1\uffff\1\154\1\155",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\111\3\uffff\1\112\26\uffff\1\107\4\uffff\1\106\3\uffff\1"+
            "\110",
            "\1\111\3\uffff\1\112\26\uffff\1\107\4\uffff\1\106\3\uffff\1"+
            "\110",
            "\1\uffff",
            "\1\uffff",
            "\1\116\23\uffff\1\115\13\uffff\1\114",
            "\1\116\23\uffff\1\115\13\uffff\1\114",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\162\3\14\1\163\1\165\1"+
            "\163\1\165\25\14\1\160\12\14\1\164\24\14\1\157\12\14\1\161\uff87"+
            "\14",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\172\3\14\1\175\1\173\1"+
            "\175\1\173\34\14\1\174\3\14\1\170\33\14\1\171\3\14\1\167\uff87"+
            "\14",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u0081\3\14\1\u0082\1\14"+
            "\1\u0082\26\14\1\u0080\37\14\1\177\uff92\14",
            "\1\uffff",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u0087\3\14\1\u0088\1\u008a"+
            "\1\u0088\1\u008a\25\14\1\u0085\5\14\1\u0089\31\14\1\u0084\5"+
            "\14\1\u0086\uff8c\14",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u008e\3\14\1\u008f\1\14"+
            "\1\u008f\27\14\1\u008d\37\14\1\u008c\uff91\14",
            "\1\uffff",
            "\1\u0092\27\uffff\1\u0091\7\uffff\1\u0090",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u0093\3\14\1\u0094\1\14"+
            "\1\u0094\uffc9\14",
            "\1\u0097\16\uffff\1\u0096\20\uffff\1\u0095",
            "\1\u0092\27\uffff\1\u0091\7\uffff\1\u0090",
            "\1\u0097\16\uffff\1\u0096\20\uffff\1\u0095",
            "",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u009a\3\14\1\u009b\1\14"+
            "\1\u009b\21\14\1\u0099\37\14\1\u0098\uff97\14",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u009f\4\14\1\u00a0\1\14"+
            "\1\u00a0\42\14\1\u009e\37\14\1\u009d\uff85\14",
            "\1\uffff",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u00a4\3\14\1\u00a5\1\14"+
            "\1\u00a5\22\14\1\u00a3\37\14\1\u00a2\uff96\14",
            "\1\u00a8\16\uffff\1\u00a7\20\uffff\1\u00a6",
            "\1\uffff",
            "\1\u00a8\16\uffff\1\u00a7\20\uffff\1\u00a6",
            "\1\126\5\uffff\1\125\22\uffff\1\123\6\uffff\1\124\5\uffff\1"+
            "\122",
            "\1\126\5\uffff\1\125\22\uffff\1\123\6\uffff\1\124\5\uffff\1"+
            "\122",
            "\1\u00a9\3\uffff\1\u00ab\1\u00aa\1\u00ab\1\u00aa",
            "\1\u00ac",
            "\1\u00ad",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u00b1\3\14\1\u00b2\1\14"+
            "\1\u00b2\20\14\1\u00b0\37\14\1\u00af\uff98\14",
            "\1\uffff",
            "\1\u00b3\3\uffff\1\u00b4\1\u00b5\1\u00b4\1\u00b5",
            "\1\u00b8\1\u00b6\1\u00b7\2\uffff\1\u00bc\1\u00ba\10\uffff\1"+
            "\u00be\1\uffff\1\u00bd\35\uffff\1\u00bb\1\uffff\1\u00b9",
            "\1\u00bf\1\uffff\1\u00c0\1\u00c1",
            "\1\u00c3\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u00c2\12\uffff"+
            "\1\31",
            "\1\62\12\uffff\1\63\3\uffff\1\60\20\uffff\1\57\12\uffff\1\61",
            "\1\75\16\uffff\1\74\20\uffff\1\73",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\116\23\uffff\1\115\13\uffff\1\114",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\116\23\uffff\1\115\13\uffff\1\114",
            "\1\u00c5\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1"+
            "\u00c4\20\uffff\1\66\3\uffff\1\64",
            "\1\u00c8\3\uffff\1\u00c9\26\uffff\1\107\4\uffff\1\u00c6\3\uffff"+
            "\1\u00c7",
            "\1\uffff",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00ca\3\uffff\1\u00cb\1\u00cc\1\u00cb\1\u00cc",
            "\1\u00ce\37\uffff\1\u00cd",
            "\1\uffff",
            "\1\u00cf",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00d0\3\uffff\1\u00d2\1\u00d1\1\u00d2\1\u00d1",
            "\1\u00d4\3\uffff\1\u00d3",
            "\1\uffff",
            "\1\u00d5",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\u00d6\3\uffff\1\u00d7\1\uffff\1\u00d7",
            "\1\u00d9\37\uffff\1\u00d8",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00da\3\uffff\1\u00db\1\u00dc\1\u00db\1\u00dc",
            "\1\u00de\37\uffff\1\u00dd",
            "\1\uffff",
            "\1\u00df",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\u00e0\3\uffff\1\u00e1\1\uffff\1\u00e1",
            "\1\u00e3\37\uffff\1\u00e2",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u00e5\3\14\1\u00e6\1\14"+
            "\1\u00e6\uffc9\14",
            "\1\uffff",
            "\1\u00e7\3\uffff\1\u00e8\1\uffff\1\u00e8",
            "\1\u00e9\3\uffff\1\u00ea",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u00ed\3\14\1\u00ee\1\14"+
            "\1\u00ee\26\14\1\u00ec\37\14\1\u00eb\uff92\14",
            "\1\uffff",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\1\u00ef\3\uffff\1\u00f0\1\uffff\1\u00f0",
            "\1\u00f1",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\u00f2\4\uffff\1\u00f3\1\uffff\1\u00f3",
            "\1\u00f5\37\uffff\1\u00f4",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\u00f6\3\uffff\1\u00f7\1\uffff\1\u00f7",
            "\1\u00f9\5\uffff\1\u00f8",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u00fc\3\14\1\u00fd\1\14"+
            "\1\u00fd\26\14\1\u00fb\37\14\1\u00fa\uff92\14",
            "\1\uffff",
            "\1\u00fe\3\uffff\1\u0100\1\u00ff\1\u0100\1\u00ff",
            "\1\u0101",
            "\1\u0102",
            "\1\u0104\5\uffff\1\125\22\uffff\1\123\6\uffff\1\u0103\5\uffff"+
            "\1\122",
            "\1\136\24\uffff\1\135\12\uffff\1\134",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\u0105\3\uffff\1\u0106\1\uffff\1\u0106",
            "\1\u0107",
            "\1\u0108\3\uffff\1\u0109\1\u010a\1\u0109\1\u010a",
            "\1\u010d\1\u010b\1\u010c\2\uffff\1\u0111\1\u010f\10\uffff\1"+
            "\u0113\1\uffff\1\u0112\35\uffff\1\u0110\1\uffff\1\u010e",
            "\1\u0114\1\uffff\1\u0115\1\u0116",
            "\1\u0118\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u0117\12\uffff"+
            "\1\31",
            "\1\62\12\uffff\1\63\3\uffff\1\60\20\uffff\1\57\12\uffff\1\61",
            "\1\75\16\uffff\1\74\20\uffff\1\73",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\116\23\uffff\1\115\13\uffff\1\114",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\116\23\uffff\1\115\13\uffff\1\114",
            "\1\u011a\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1"+
            "\u0119\20\uffff\1\66\3\uffff\1\64",
            "\1\u011d\3\uffff\1\u011e\26\uffff\1\107\4\uffff\1\u011b\3\uffff"+
            "\1\u011c",
            "\1\uffff",
            "\1\136\24\uffff\1\135\12\uffff\1\134",
            "\1\136\24\uffff\1\135\12\uffff\1\134",
            "\1\uffff",
            "\1\uffff",
            "\1\u0120\27\uffff\1\u0091\7\uffff\1\u011f",
            "\1\u0097\16\uffff\1\u0096\20\uffff\1\u0095",
            "\1\u0120\27\uffff\1\u0091\7\uffff\1\u011f",
            "\1\u0097\16\uffff\1\u0096\20\uffff\1\u0095",
            "\1\u0121\3\uffff\1\u0122\1\u0123\1\u0122\1\u0123",
            "\1\u0125\37\uffff\1\u0124",
            "\1\u0126",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0127\3\uffff\1\u0129\1\u0128\1\u0129\1\u0128",
            "\1\u012b\3\uffff\1\u012a",
            "\1\u012c",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u012d\3\uffff\1\u012e\1\uffff\1\u012e",
            "\1\u0130\37\uffff\1\u012f",
            "\1\uffff",
            "\1\uffff",
            "\1\u0131\3\uffff\1\u0132\1\u0133\1\u0132\1\u0133",
            "\1\u0135\37\uffff\1\u0134",
            "\1\u0136",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0137\3\uffff\1\u0138\1\uffff\1\u0138",
            "\1\u013a\37\uffff\1\u0139",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\u013b\3\uffff\1\u013c\1\uffff\1\u013c",
            "\1\u013d",
            "\1\u013e\3\uffff\1\u013f\1\uffff\1\u013f",
            "\1\u0140\3\uffff\1\u0141",
            "\1\u0143\27\uffff\1\u0091\7\uffff\1\u0142",
            "\1\u0097\16\uffff\1\u0096\20\uffff\1\u0095",
            "\1\uffff",
            "\1\uffff",
            "\1\u0144\3\uffff\1\u0145\1\uffff\1\u0145",
            "\1\u0147\37\uffff\1\u0146",
            "\1\u0148\3\uffff\1\u0149\1\uffff\1\u0149",
            "\1\u014a",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\1\u014b\4\uffff\1\u014c\1\uffff\1\u014c",
            "\1\u014e\37\uffff\1\u014d",
            "\1\uffff",
            "\1\uffff",
            "\1\u014f\3\uffff\1\u0150\1\uffff\1\u0150",
            "\1\u0152\5\uffff\1\u0151",
            "\1\uffff",
            "\1\u00a8\16\uffff\1\u00a7\20\uffff\1\u00a6",
            "\1\uffff",
            "\1\uffff",
            "\1\u0153\3\uffff\1\u0154\1\uffff\1\u0154",
            "\1\u0156\37\uffff\1\u0155",
            "\1\u0157\3\uffff\1\u0159\1\u0158\1\u0159\1\u0158",
            "\1\u015a",
            "\1\u015b",
            "\1\u015d\5\uffff\1\125\22\uffff\1\123\6\uffff\1\u015c\5\uffff"+
            "\1\122",
            "\1\136\24\uffff\1\135\12\uffff\1\134",
            "\1\u00a8\16\uffff\1\u00a7\20\uffff\1\u00a6",
            "\1\u00a8\16\uffff\1\u00a7\20\uffff\1\u00a6",
            "\1\u015e\3\uffff\1\u015f\1\uffff\1\u015f",
            "\1\u0160",
            "\1\uffff",
            "\1\u0161\1\u0162\1\u0161\1\u0162",
            "\1\u0165\1\u0163\1\u0164\2\uffff\1\u0169\1\u0167\10\uffff\1"+
            "\u016b\1\uffff\1\u016a\35\uffff\1\u0168\1\uffff\1\u0166",
            "\1\u016c\1\uffff\1\u016d\1\u016e",
            "\1\u0170\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u016f\12\uffff"+
            "\1\31",
            "\1\62\12\uffff\1\63\3\uffff\1\60\20\uffff\1\57\12\uffff\1\61",
            "\1\75\16\uffff\1\74\20\uffff\1\73",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\116\23\uffff\1\115\13\uffff\1\114",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\116\23\uffff\1\115\13\uffff\1\114",
            "\1\u0172\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1"+
            "\u0171\20\uffff\1\66\3\uffff\1\64",
            "\1\u0175\3\uffff\1\u0176\26\uffff\1\107\4\uffff\1\u0173\3\uffff"+
            "\1\u0174",
            "\1\uffff",
            "\1\136\24\uffff\1\135\12\uffff\1\134",
            "\1\136\24\uffff\1\135\12\uffff\1\134",
            "\1\uffff",
            "\1\uffff",
            "\1\u0178\27\uffff\1\u0091\7\uffff\1\u0177",
            "\1\u0097\16\uffff\1\u0096\20\uffff\1\u0095",
            "\1\u0178\27\uffff\1\u0091\7\uffff\1\u0177",
            "\1\u0097\16\uffff\1\u0096\20\uffff\1\u0095",
            "\1\uffff",
            "\1\uffff",
            "\1\u0179\3\uffff\1\u017a\1\u017b\1\u017a\1\u017b",
            "\1\u017d\37\uffff\1\u017c",
            "\1\u017e",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u017f\3\uffff\1\u0181\1\u0180\1\u0181\1\u0180",
            "\1\u0183\3\uffff\1\u0182",
            "\1\u0184",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0185\3\uffff\1\u0186\1\uffff\1\u0186",
            "\1\u0188\37\uffff\1\u0187",
            "\1\uffff",
            "\1\uffff",
            "\1\u0189\3\uffff\1\u018a\1\u018b\1\u018a\1\u018b",
            "\1\u018d\37\uffff\1\u018c",
            "\1\u018e",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u018f\3\uffff\1\u0190\1\uffff\1\u0190",
            "\1\u0192\37\uffff\1\u0191",
            "\1\uffff",
            "\1\uffff",
            "\1\u0193\3\uffff\1\u0194\1\uffff\1\u0194",
            "\1\u0195",
            "\1\uffff",
            "\1\u0196\3\uffff\1\u0197\1\uffff\1\u0197",
            "\1\u0198\3\uffff\1\u0199",
            "\1\u019b\27\uffff\1\u0091\7\uffff\1\u019a",
            "\1\u0097\16\uffff\1\u0096\20\uffff\1\u0095",
            "\1\uffff",
            "\1\uffff",
            "\1\u019c\3\uffff\1\u019d\1\uffff\1\u019d",
            "\1\u019f\37\uffff\1\u019e",
            "\1\uffff",
            "\1\uffff",
            "\1\u01a0\3\uffff\1\u01a1\1\uffff\1\u01a1",
            "\1\u01a2",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\1\u01a3\4\uffff\1\u01a4\1\uffff\1\u01a4",
            "\1\u01a6\37\uffff\1\u01a5",
            "\1\uffff",
            "\1\uffff",
            "\1\u01a7\3\uffff\1\u01a8\1\uffff\1\u01a8",
            "\1\u01aa\5\uffff\1\u01a9",
            "\1\uffff",
            "\1\u00a8\16\uffff\1\u00a7\20\uffff\1\u00a6",
            "\1\u01ab\3\uffff\1\u01ac\1\uffff\1\u01ac",
            "\1\u01ae\37\uffff\1\u01ad",
            "\1\uffff",
            "\1\uffff",
            "\1\u01b0\1\u01af\1\u01b0\1\u01af",
            "\1\u01b1",
            "\1\u01b2",
            "\1\u01b4\5\uffff\1\125\22\uffff\1\123\6\uffff\1\u01b3\5\uffff"+
            "\1\122",
            "\1\136\24\uffff\1\135\12\uffff\1\134",
            "\1\u00a8\16\uffff\1\u00a7\20\uffff\1\u00a6",
            "\1\u00a8\16\uffff\1\u00a7\20\uffff\1\u00a6",
            "\1\u01b5\3\uffff\1\u01b6\1\uffff\1\u01b6",
            "\1\u01b7",
            "\1\uffff",
            "\1\u01ba\1\u01b8\1\u01b9\2\uffff\1\u01be\1\u01bc\10\uffff\1"+
            "\u01c0\1\uffff\1\u01bf\35\uffff\1\u01bd\1\uffff\1\u01bb",
            "\1\u01c1\1\uffff\1\u01c2\1\u01c3",
            "\1\u01c5\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u01c4\12\uffff"+
            "\1\31",
            "\1\62\12\uffff\1\63\3\uffff\1\60\20\uffff\1\57\12\uffff\1\61",
            "\1\75\16\uffff\1\74\20\uffff\1\73",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\116\23\uffff\1\115\13\uffff\1\114",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\116\23\uffff\1\115\13\uffff\1\114",
            "\1\u01c7\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1"+
            "\u01c6\20\uffff\1\66\3\uffff\1\64",
            "\1\u01ca\3\uffff\1\u01cb\26\uffff\1\107\4\uffff\1\u01c8\3\uffff"+
            "\1\u01c9",
            "\1\uffff",
            "\1\136\24\uffff\1\135\12\uffff\1\134",
            "\1\136\24\uffff\1\135\12\uffff\1\134",
            "\1\uffff",
            "\1\uffff",
            "\1\u01cd\27\uffff\1\u0091\7\uffff\1\u01cc",
            "\1\u0097\16\uffff\1\u0096\20\uffff\1\u0095",
            "\1\u01cd\27\uffff\1\u0091\7\uffff\1\u01cc",
            "\1\u0097\16\uffff\1\u0096\20\uffff\1\u0095",
            "\1\uffff",
            "\1\uffff",
            "\1\u01ce\1\u01cf\1\u01ce\1\u01cf",
            "\1\u01d1\37\uffff\1\u01d0",
            "\1\u01d2",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u01d4\1\u01d3\1\u01d4\1\u01d3",
            "\1\u01d6\3\uffff\1\u01d5",
            "\1\u01d7",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u01d8\1\uffff\1\u01d8",
            "\1\u01da\37\uffff\1\u01d9",
            "\1\uffff",
            "\1\uffff",
            "\1\u01db\1\u01dc\1\u01db\1\u01dc",
            "\1\u01de\37\uffff\1\u01dd",
            "\1\u01df",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u01e0\1\uffff\1\u01e0",
            "\1\u01e2\37\uffff\1\u01e1",
            "\1\uffff",
            "\1\uffff",
            "\1\u01e3\3\uffff\1\u01e4\1\uffff\1\u01e4",
            "\1\u01e5",
            "\1\uffff",
            "\1\u01e6\1\uffff\1\u01e6",
            "\1\u01e7\3\uffff\1\u01e8",
            "\1\u01ea\27\uffff\1\u0091\7\uffff\1\u01e9",
            "\1\u0097\16\uffff\1\u0096\20\uffff\1\u0095",
            "\1\uffff",
            "\1\uffff",
            "\1\u01eb\3\uffff\1\u01ec\1\uffff\1\u01ec",
            "\1\u01ee\37\uffff\1\u01ed",
            "\1\uffff",
            "\1\uffff",
            "\1\u01ef\1\uffff\1\u01ef",
            "\1\u01f0",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\1\u01f1\1\uffff\1\u01f1",
            "\1\u01f3\37\uffff\1\u01f2",
            "\1\uffff",
            "\1\uffff",
            "\1\u01f4\1\uffff\1\u01f4",
            "\1\u01f6\5\uffff\1\u01f5",
            "\1\uffff",
            "\1\u00a8\16\uffff\1\u00a7\20\uffff\1\u00a6",
            "\1\u01f7\3\uffff\1\u01f8\1\uffff\1\u01f8",
            "\1\u01fa\37\uffff\1\u01f9",
            "\1\uffff",
            "\1\uffff",
            "\1\u01fb",
            "\1\u01fc",
            "\1\u01fe\5\uffff\1\125\22\uffff\1\123\6\uffff\1\u01fd\5\uffff"+
            "\1\122",
            "\1\136\24\uffff\1\135\12\uffff\1\134",
            "\1\u00a8\16\uffff\1\u00a7\20\uffff\1\u00a6",
            "\1\u00a8\16\uffff\1\u00a7\20\uffff\1\u00a6",
            "\1\u01ff\1\uffff\1\u01ff",
            "\1\u0200",
            "\1\uffff",
            "\1\35\12\uffff\1\34\13\uffff\1\32\10\uffff\1\33\12\uffff\1"+
            "\31",
            "\1\62\12\uffff\1\63\3\uffff\1\60\20\uffff\1\57\12\uffff\1\61",
            "\1\75\16\uffff\1\74\20\uffff\1\73",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\116\23\uffff\1\115\13\uffff\1\114",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\116\23\uffff\1\115\13\uffff\1\114",
            "\1\72\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1\67"+
            "\20\uffff\1\66\3\uffff\1\64",
            "\1\111\3\uffff\1\112\26\uffff\1\107\4\uffff\1\106\3\uffff\1"+
            "\110",
            "\1\uffff",
            "\1\136\24\uffff\1\135\12\uffff\1\134",
            "\1\136\24\uffff\1\135\12\uffff\1\134",
            "\1\uffff",
            "\1\uffff",
            "\1\u0092\27\uffff\1\u0091\7\uffff\1\u0090",
            "\1\u0097\16\uffff\1\u0096\20\uffff\1\u0095",
            "\1\u0092\27\uffff\1\u0091\7\uffff\1\u0090",
            "\1\u0097\16\uffff\1\u0096\20\uffff\1\u0095",
            "\1\uffff",
            "\1\uffff",
            "\1\u0202\37\uffff\1\u0201",
            "\1\u0203",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0205\3\uffff\1\u0204",
            "\1\u0206",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0208\37\uffff\1\u0207",
            "\1\uffff",
            "\1\uffff",
            "\1\u020a\37\uffff\1\u0209",
            "\1\u020b",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u020d\37\uffff\1\u020c",
            "\1\uffff",
            "\1\uffff",
            "\1\u020e\1\uffff\1\u020e",
            "\1\u020f",
            "\1\uffff",
            "\1\u0210\3\uffff\1\u0211",
            "\1\u0213\27\uffff\1\u0091\7\uffff\1\u0212",
            "\1\u0097\16\uffff\1\u0096\20\uffff\1\u0095",
            "\1\uffff",
            "\1\uffff",
            "\1\u0214\1\uffff\1\u0214",
            "\1\u0216\37\uffff\1\u0215",
            "\1\uffff",
            "\1\uffff",
            "\1\u0217",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\1\u0219\37\uffff\1\u0218",
            "\1\uffff",
            "\1\uffff",
            "\1\u021b\5\uffff\1\u021a",
            "\1\uffff",
            "\1\u00a8\16\uffff\1\u00a7\20\uffff\1\u00a6",
            "\1\u021c\1\uffff\1\u021c",
            "\1\u021e\37\uffff\1\u021d",
            "\1\uffff",
            "\1\uffff",
            "\1\126\5\uffff\1\125\22\uffff\1\123\6\uffff\1\124\5\uffff\1"+
            "\122",
            "\1\136\24\uffff\1\135\12\uffff\1\134",
            "\1\u00a8\16\uffff\1\u00a7\20\uffff\1\u00a6",
            "\1\u00a8\16\uffff\1\u00a7\20\uffff\1\u00a6",
            "\1\u021f",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0220",
            "\1\uffff",
            "\1\u0092\27\uffff\1\u0091\7\uffff\1\u0090",
            "\1\u0097\16\uffff\1\u0096\20\uffff\1\u0095",
            "\1\uffff",
            "\1\uffff",
            "\1\u0222\37\uffff\1\u0221",
            "\1\uffff",
            "\1\uffff",
            "\1\121\1\uffff\1\120\35\uffff\1\117",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00a8\16\uffff\1\u00a7\20\uffff\1\u00a6",
            "\1\u0224\37\uffff\1\u0223",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff"
    };

    static final short[] DFA184_eot = DFA.unpackEncodedString(DFA184_eotS);
    static final short[] DFA184_eof = DFA.unpackEncodedString(DFA184_eofS);
    static final char[] DFA184_min = DFA.unpackEncodedStringToUnsignedChars(DFA184_minS);
    static final char[] DFA184_max = DFA.unpackEncodedStringToUnsignedChars(DFA184_maxS);
    static final short[] DFA184_accept = DFA.unpackEncodedString(DFA184_acceptS);
    static final short[] DFA184_special = DFA.unpackEncodedString(DFA184_specialS);
    static final short[][] DFA184_transition;

    static {
        int numStates = DFA184_transitionS.length;
        DFA184_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA184_transition[i] = DFA.unpackEncodedString(DFA184_transitionS[i]);
        }
    }

    class DFA184 extends DFA {

        public DFA184(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 184;
            this.eot = DFA184_eot;
            this.eof = DFA184_eof;
            this.min = DFA184_min;
            this.max = DFA184_max;
            this.accept = DFA184_accept;
            this.special = DFA184_special;
            this.transition = DFA184_transition;
        }
        public String getDescription() {
            return "1749:9: ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA184_248 = input.LA(1);

                         
                        int index184_248 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_248);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA184_522 = input.LA(1);

                         
                        int index184_522 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_522);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA184_521 = input.LA(1);

                         
                        int index184_521 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_521);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA184_251 = input.LA(1);

                         
                        int index184_251 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_251);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA184_250 = input.LA(1);

                         
                        int index184_250 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_250);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA184_217 = input.LA(1);

                         
                        int index184_217 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 126;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_217);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA184_50 = input.LA(1);

                         
                        int index184_50 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_50);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA184_216 = input.LA(1);

                         
                        int index184_216 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 126;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_216);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA184_47 = input.LA(1);

                         
                        int index184_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_47);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA184_53 = input.LA(1);

                        s = -1;
                        if ( (LA184_53=='x') ) {s = 119;}

                        else if ( (LA184_53=='X') ) {s = 120;}

                        else if ( (LA184_53=='t') ) {s = 121;}

                        else if ( (LA184_53=='0') ) {s = 122;}

                        else if ( (LA184_53=='5'||LA184_53=='7') ) {s = 123;}

                        else if ( (LA184_53=='T') ) {s = 124;}

                        else if ( ((LA184_53>='\u0000' && LA184_53<='\t')||LA184_53=='\u000B'||(LA184_53>='\u000E' && LA184_53<='/')||(LA184_53>='1' && LA184_53<='3')||(LA184_53>='8' && LA184_53<='S')||(LA184_53>='U' && LA184_53<='W')||(LA184_53>='Y' && LA184_53<='s')||(LA184_53>='u' && LA184_53<='w')||(LA184_53>='y' && LA184_53<='\uFFFF')) ) {s = 12;}

                        else if ( (LA184_53=='4'||LA184_53=='6') ) {s = 125;}

                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA184_68 = input.LA(1);

                        s = -1;
                        if ( (LA184_68=='n') ) {s = 140;}

                        else if ( (LA184_68=='N') ) {s = 141;}

                        else if ( ((LA184_68>='\u0000' && LA184_68<='\t')||LA184_68=='\u000B'||(LA184_68>='\u000E' && LA184_68<='/')||(LA184_68>='1' && LA184_68<='3')||LA184_68=='5'||(LA184_68>='7' && LA184_68<='M')||(LA184_68>='O' && LA184_68<='m')||(LA184_68>='o' && LA184_68<='\uFFFF')) ) {s = 12;}

                        else if ( (LA184_68=='0') ) {s = 142;}

                        else if ( (LA184_68=='4'||LA184_68=='6') ) {s = 143;}

                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA184_176 = input.LA(1);

                         
                        int index184_176 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 174;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_176);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA184_175 = input.LA(1);

                         
                        int index184_175 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 174;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_175);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA184_471 = input.LA(1);

                         
                        int index184_471 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_471);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA184_387 = input.LA(1);

                         
                        int index184_387 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_387);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA184_308 = input.LA(1);

                         
                        int index184_308 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_308);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA184_309 = input.LA(1);

                         
                        int index184_309 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_309);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA184_518 = input.LA(1);

                         
                        int index184_518 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_518);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA184_398 = input.LA(1);

                         
                        int index184_398 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_398);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA184_485 = input.LA(1);

                         
                        int index184_485 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_485);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA184_20 = input.LA(1);

                         
                        int index184_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 75;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_20);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA184_9 = input.LA(1);

                         
                        int index184_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 75;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_9);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA184_94 = input.LA(1);

                         
                        int index184_94 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 174;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_94);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA184_111 = input.LA(1);

                         
                        int index184_111 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_111);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA184_92 = input.LA(1);

                         
                        int index184_92 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 174;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_92);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA184_112 = input.LA(1);

                         
                        int index184_112 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_112);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA184_81 = input.LA(1);

                         
                        int index184_81 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_81);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA184_79 = input.LA(1);

                         
                        int index184_79 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_79);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA184_69 = input.LA(1);

                         
                        int index184_69 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 139;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_69);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA184_56 = input.LA(1);

                         
                        int index184_56 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_56);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA184_67 = input.LA(1);

                         
                        int index184_67 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 139;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_67);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA184_52 = input.LA(1);

                         
                        int index184_52 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_52);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA184_451 = input.LA(1);

                         
                        int index184_451 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 75;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_451);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA184_501 = input.LA(1);

                         
                        int index184_501 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_501);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA184_244 = input.LA(1);

                         
                        int index184_244 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_244);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA184_245 = input.LA(1);

                         
                        int index184_245 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_245);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA184_469 = input.LA(1);

                         
                        int index184_469 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_469);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA184_41 = input.LA(1);

                         
                        int index184_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 75;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_41);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA184_42 = input.LA(1);

                         
                        int index184_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 75;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_42);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA184_133 = input.LA(1);

                         
                        int index184_133 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_133);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA184_132 = input.LA(1);

                         
                        int index184_132 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_132);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA184_499 = input.LA(1);

                         
                        int index184_499 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_499);
                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA184_498 = input.LA(1);

                         
                        int index184_498 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_498);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA184_51 = input.LA(1);

                         
                        int index184_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_51);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA184_193 = input.LA(1);

                         
                        int index184_193 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 75;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_193);
                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA184_49 = input.LA(1);

                         
                        int index184_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_49);
                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA184_166 = input.LA(1);

                         
                        int index184_166 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_166);
                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA184_168 = input.LA(1);

                         
                        int index184_168 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_168);
                        if ( s>=0 ) return s;
                        break;
                    case 48 : 
                        int LA184_460 = input.LA(1);

                         
                        int index184_460 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_460);
                        if ( s>=0 ) return s;
                        break;
                    case 49 : 
                        int LA184_461 = input.LA(1);

                         
                        int index184_461 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_461);
                        if ( s>=0 ) return s;
                        break;
                    case 50 : 
                        int LA184_281 = input.LA(1);

                         
                        int index184_281 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_281);
                        if ( s>=0 ) return s;
                        break;
                    case 51 : 
                        int LA184_282 = input.LA(1);

                         
                        int index184_282 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_282);
                        if ( s>=0 ) return s;
                        break;
                    case 52 : 
                        int LA184_211 = input.LA(1);

                         
                        int index184_211 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_211);
                        if ( s>=0 ) return s;
                        break;
                    case 53 : 
                        int LA184_425 = input.LA(1);

                         
                        int index184_425 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_425);
                        if ( s>=0 ) return s;
                        break;
                    case 54 : 
                        int LA184_222 = input.LA(1);

                         
                        int index184_222 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_222);
                        if ( s>=0 ) return s;
                        break;
                    case 55 : 
                        int LA184_221 = input.LA(1);

                         
                        int index184_221 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_221);
                        if ( s>=0 ) return s;
                        break;
                    case 56 : 
                        int LA184_207 = input.LA(1);

                         
                        int index184_207 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_207);
                        if ( s>=0 ) return s;
                        break;
                    case 57 : 
                        int LA184_534 = input.LA(1);

                         
                        int index184_534 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_534);
                        if ( s>=0 ) return s;
                        break;
                    case 58 : 
                        int LA184_533 = input.LA(1);

                         
                        int index184_533 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_533);
                        if ( s>=0 ) return s;
                        break;
                    case 59 : 
                        int LA184_227 = input.LA(1);

                         
                        int index184_227 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 139;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_227);
                        if ( s>=0 ) return s;
                        break;
                    case 60 : 
                        int LA184_236 = input.LA(1);

                         
                        int index184_236 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_236);
                        if ( s>=0 ) return s;
                        break;
                    case 61 : 
                        int LA184_235 = input.LA(1);

                         
                        int index184_235 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_235);
                        if ( s>=0 ) return s;
                        break;
                    case 62 : 
                        int LA184_466 = input.LA(1);

                         
                        int index184_466 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_466);
                        if ( s>=0 ) return s;
                        break;
                    case 63 : 
                        int LA184_517 = input.LA(1);

                         
                        int index184_517 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_517);
                        if ( s>=0 ) return s;
                        break;
                    case 64 : 
                        int LA184_544 = input.LA(1);

                         
                        int index184_544 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_544);
                        if ( s>=0 ) return s;
                        break;
                    case 65 : 
                        int LA184_531 = input.LA(1);

                         
                        int index184_531 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_531);
                        if ( s>=0 ) return s;
                        break;
                    case 66 : 
                        int LA184_226 = input.LA(1);

                         
                        int index184_226 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 139;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_226);
                        if ( s>=0 ) return s;
                        break;
                    case 67 : 
                        int LA184_530 = input.LA(1);

                         
                        int index184_530 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_530);
                        if ( s>=0 ) return s;
                        break;
                    case 68 : 
                        int LA184_523 = input.LA(1);

                         
                        int index184_523 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_523);
                        if ( s>=0 ) return s;
                        break;
                    case 69 : 
                        int LA184_299 = input.LA(1);

                         
                        int index184_299 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_299);
                        if ( s>=0 ) return s;
                        break;
                    case 70 : 
                        int LA184_326 = input.LA(1);

                         
                        int index184_326 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_326);
                        if ( s>=0 ) return s;
                        break;
                    case 71 : 
                        int LA184_327 = input.LA(1);

                         
                        int index184_327 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_327);
                        if ( s>=0 ) return s;
                        break;
                    case 72 : 
                        int LA184_513 = input.LA(1);

                         
                        int index184_513 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_513);
                        if ( s>=0 ) return s;
                        break;
                    case 73 : 
                        int LA184_514 = input.LA(1);

                         
                        int index184_514 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_514);
                        if ( s>=0 ) return s;
                        break;
                    case 74 : 
                        int LA184_430 = input.LA(1);

                         
                        int index184_430 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_430);
                        if ( s>=0 ) return s;
                        break;
                    case 75 : 
                        int LA184_410 = input.LA(1);

                         
                        int index184_410 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_410);
                        if ( s>=0 ) return s;
                        break;
                    case 76 : 
                        int LA184_411 = input.LA(1);

                         
                        int index184_411 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_411);
                        if ( s>=0 ) return s;
                        break;
                    case 77 : 
                        int LA184_429 = input.LA(1);

                         
                        int index184_429 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_429);
                        if ( s>=0 ) return s;
                        break;
                    case 78 : 
                        int LA184_548 = input.LA(1);

                         
                        int index184_548 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_548);
                        if ( s>=0 ) return s;
                        break;
                    case 79 : 
                        int LA184_310 = input.LA(1);

                         
                        int index184_310 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_310);
                        if ( s>=0 ) return s;
                        break;
                    case 80 : 
                        int LA184_439 = input.LA(1);

                         
                        int index184_439 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 174;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_439);
                        if ( s>=0 ) return s;
                        break;
                    case 81 : 
                        int LA184_422 = input.LA(1);

                         
                        int index184_422 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_422);
                        if ( s>=0 ) return s;
                        break;
                    case 82 : 
                        int LA184_421 = input.LA(1);

                         
                        int index184_421 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_421);
                        if ( s>=0 ) return s;
                        break;
                    case 83 : 
                        int LA184_120 = input.LA(1);

                         
                        int index184_120 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_120);
                        if ( s>=0 ) return s;
                        break;
                    case 84 : 
                        int LA184_119 = input.LA(1);

                         
                        int index184_119 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_119);
                        if ( s>=0 ) return s;
                        break;
                    case 85 : 
                        int LA184_85 = input.LA(1);

                         
                        int index184_85 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_85);
                        if ( s>=0 ) return s;
                        break;
                    case 86 : 
                        int LA184_82 = input.LA(1);

                         
                        int index184_82 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_82);
                        if ( s>=0 ) return s;
                        break;
                    case 87 : 
                        int LA184_293 = input.LA(1);

                         
                        int index184_293 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_293);
                        if ( s>=0 ) return s;
                        break;
                    case 88 : 
                        int LA184_145 = input.LA(1);

                        s = -1;
                        if ( ((LA184_145>='\u0000' && LA184_145<='\t')||LA184_145=='\u000B'||(LA184_145>='\u000E' && LA184_145<='/')||(LA184_145>='1' && LA184_145<='3')||LA184_145=='5'||(LA184_145>='7' && LA184_145<='\uFFFF')) ) {s = 12;}

                        else if ( (LA184_145=='0') ) {s = 229;}

                        else if ( (LA184_145=='4'||LA184_145=='6') ) {s = 230;}

                        if ( s>=0 ) return s;
                        break;
                    case 89 : 
                        int LA184_64 = input.LA(1);

                         
                        int index184_64 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_64);
                        if ( s>=0 ) return s;
                        break;
                    case 90 : 
                        int LA184_66 = input.LA(1);

                         
                        int index184_66 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_66);
                        if ( s>=0 ) return s;
                        break;
                    case 91 : 
                        int LA184_478 = input.LA(1);

                         
                        int index184_478 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_478);
                        if ( s>=0 ) return s;
                        break;
                    case 92 : 
                        int LA184_477 = input.LA(1);

                         
                        int index184_477 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_477);
                        if ( s>=0 ) return s;
                        break;
                    case 93 : 
                        int LA184_292 = input.LA(1);

                         
                        int index184_292 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_292);
                        if ( s>=0 ) return s;
                        break;
                    case 94 : 
                        int LA184_54 = input.LA(1);

                         
                        int index184_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_54);
                        if ( s>=0 ) return s;
                        break;
                    case 95 : 
                        int LA184_547 = input.LA(1);

                         
                        int index184_547 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_547);
                        if ( s>=0 ) return s;
                        break;
                    case 96 : 
                        int LA184_57 = input.LA(1);

                         
                        int index184_57 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_57);
                        if ( s>=0 ) return s;
                        break;
                    case 97 : 
                        int LA184_116 = input.LA(1);

                         
                        int index184_116 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_116);
                        if ( s>=0 ) return s;
                        break;
                    case 98 : 
                        int LA184_113 = input.LA(1);

                         
                        int index184_113 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_113);
                        if ( s>=0 ) return s;
                        break;
                    case 99 : 
                        int LA184_141 = input.LA(1);

                         
                        int index184_141 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 139;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_141);
                        if ( s>=0 ) return s;
                        break;
                    case 100 : 
                        int LA184_140 = input.LA(1);

                         
                        int index184_140 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 139;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_140);
                        if ( s>=0 ) return s;
                        break;
                    case 101 : 
                        int LA184_61 = input.LA(1);

                         
                        int index184_61 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 126;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_61);
                        if ( s>=0 ) return s;
                        break;
                    case 102 : 
                        int LA184_59 = input.LA(1);

                         
                        int index184_59 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 126;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_59);
                        if ( s>=0 ) return s;
                        break;
                    case 103 : 
                        int LA184_382 = input.LA(1);

                         
                        int index184_382 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_382);
                        if ( s>=0 ) return s;
                        break;
                    case 104 : 
                        int LA184_93 = input.LA(1);

                        s = -1;
                        if ( (LA184_93=='g') ) {s = 175;}

                        else if ( (LA184_93=='G') ) {s = 176;}

                        else if ( ((LA184_93>='\u0000' && LA184_93<='\t')||LA184_93=='\u000B'||(LA184_93>='\u000E' && LA184_93<='/')||(LA184_93>='1' && LA184_93<='3')||LA184_93=='5'||(LA184_93>='7' && LA184_93<='F')||(LA184_93>='H' && LA184_93<='f')||(LA184_93>='h' && LA184_93<='\uFFFF')) ) {s = 12;}

                        else if ( (LA184_93=='0') ) {s = 177;}

                        else if ( (LA184_93=='4'||LA184_93=='6') ) {s = 178;}

                        if ( s>=0 ) return s;
                        break;
                    case 105 : 
                        int LA184_80 = input.LA(1);

                        s = -1;
                        if ( (LA184_80=='z') ) {s = 157;}

                        else if ( (LA184_80=='Z') ) {s = 158;}

                        else if ( ((LA184_80>='\u0000' && LA184_80<='\t')||LA184_80=='\u000B'||(LA184_80>='\u000E' && LA184_80<='/')||(LA184_80>='1' && LA184_80<='4')||LA184_80=='6'||(LA184_80>='8' && LA184_80<='Y')||(LA184_80>='[' && LA184_80<='y')||(LA184_80>='{' && LA184_80<='\uFFFF')) ) {s = 12;}

                        else if ( (LA184_80=='0') ) {s = 159;}

                        else if ( (LA184_80=='5'||LA184_80=='7') ) {s = 160;}

                        if ( s>=0 ) return s;
                        break;
                    case 106 : 
                        int LA184_334 = input.LA(1);

                         
                        int index184_334 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_334);
                        if ( s>=0 ) return s;
                        break;
                    case 107 : 
                        int LA184_333 = input.LA(1);

                         
                        int index184_333 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_333);
                        if ( s>=0 ) return s;
                        break;
                    case 108 : 
                        int LA184_26 = input.LA(1);

                        s = -1;
                        if ( (LA184_26=='p') ) {s = 87;}

                        else if ( (LA184_26=='P') ) {s = 88;}

                        else if ( ((LA184_26>='\u0000' && LA184_26<='\t')||LA184_26=='\u000B'||(LA184_26>='\u000E' && LA184_26<='/')||(LA184_26>='1' && LA184_26<='3')||(LA184_26>='8' && LA184_26<='O')||(LA184_26>='Q' && LA184_26<='o')||(LA184_26>='q' && LA184_26<='\uFFFF')) ) {s = 12;}

                        else if ( (LA184_26=='0') ) {s = 89;}

                        else if ( (LA184_26=='5'||LA184_26=='7') ) {s = 90;}

                        else if ( (LA184_26=='4'||LA184_26=='6') ) {s = 91;}

                        if ( s>=0 ) return s;
                        break;
                    case 109 : 
                        int LA184_386 = input.LA(1);

                         
                        int index184_386 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_386);
                        if ( s>=0 ) return s;
                        break;
                    case 110 : 
                        int LA184_341 = input.LA(1);

                         
                        int index184_341 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_341);
                        if ( s>=0 ) return s;
                        break;
                    case 111 : 
                        int LA184_342 = input.LA(1);

                         
                        int index184_342 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_342);
                        if ( s>=0 ) return s;
                        break;
                    case 112 : 
                        int LA184_473 = input.LA(1);

                         
                        int index184_473 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 126;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_473);
                        if ( s>=0 ) return s;
                        break;
                    case 113 : 
                        int LA184_474 = input.LA(1);

                         
                        int index184_474 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 126;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_474);
                        if ( s>=0 ) return s;
                        break;
                    case 114 : 
                        int LA184_536 = input.LA(1);

                         
                        int index184_536 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_536);
                        if ( s>=0 ) return s;
                        break;
                    case 115 : 
                        int LA184_464 = input.LA(1);

                         
                        int index184_464 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_464);
                        if ( s>=0 ) return s;
                        break;
                    case 116 : 
                        int LA184_537 = input.LA(1);

                         
                        int index184_537 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_537);
                        if ( s>=0 ) return s;
                        break;
                    case 117 : 
                        int LA184_465 = input.LA(1);

                         
                        int index184_465 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_465);
                        if ( s>=0 ) return s;
                        break;
                    case 118 : 
                        int LA184_512 = input.LA(1);

                         
                        int index184_512 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 174;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_512);
                        if ( s>=0 ) return s;
                        break;
                    case 119 : 
                        int LA184_304 = input.LA(1);

                         
                        int index184_304 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 126;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_304);
                        if ( s>=0 ) return s;
                        break;
                    case 120 : 
                        int LA184_144 = input.LA(1);

                         
                        int index184_144 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_144);
                        if ( s>=0 ) return s;
                        break;
                    case 121 : 
                        int LA184_146 = input.LA(1);

                         
                        int index184_146 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_146);
                        if ( s>=0 ) return s;
                        break;
                    case 122 : 
                        int LA184_482 = input.LA(1);

                         
                        int index184_482 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 139;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_482);
                        if ( s>=0 ) return s;
                        break;
                    case 123 : 
                        int LA184_479 = input.LA(1);

                         
                        int index184_479 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_479);
                        if ( s>=0 ) return s;
                        break;
                    case 124 : 
                        int LA184_481 = input.LA(1);

                         
                        int index184_481 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 139;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_481);
                        if ( s>=0 ) return s;
                        break;
                    case 125 : 
                        int LA184_2 = input.LA(1);

                        s = -1;
                        if ( (LA184_2=='p') ) {s = 30;}

                        else if ( (LA184_2=='0') ) {s = 31;}

                        else if ( (LA184_2=='4'||LA184_2=='6') ) {s = 32;}

                        else if ( (LA184_2=='P') ) {s = 33;}

                        else if ( (LA184_2=='m') ) {s = 34;}

                        else if ( (LA184_2=='5'||LA184_2=='7') ) {s = 35;}

                        else if ( (LA184_2=='M') ) {s = 36;}

                        else if ( (LA184_2=='i') ) {s = 37;}

                        else if ( (LA184_2=='I') ) {s = 38;}

                        else if ( (LA184_2=='r') ) {s = 39;}

                        else if ( (LA184_2=='R') ) {s = 40;}

                        else if ( (LA184_2=='s') ) {s = 41;}

                        else if ( (LA184_2=='S') ) {s = 42;}

                        else if ( (LA184_2=='k') ) {s = 43;}

                        else if ( (LA184_2=='K') ) {s = 44;}

                        else if ( (LA184_2=='h') ) {s = 45;}

                        else if ( (LA184_2=='H') ) {s = 46;}

                        else if ( ((LA184_2>='\u0000' && LA184_2<='\t')||LA184_2=='\u000B'||(LA184_2>='\u000E' && LA184_2<='/')||(LA184_2>='1' && LA184_2<='3')||(LA184_2>='8' && LA184_2<='G')||LA184_2=='J'||LA184_2=='L'||(LA184_2>='N' && LA184_2<='O')||LA184_2=='Q'||(LA184_2>='T' && LA184_2<='g')||LA184_2=='j'||LA184_2=='l'||(LA184_2>='n' && LA184_2<='o')||LA184_2=='q'||(LA184_2>='t' && LA184_2<='\uFFFF')) ) {s = 12;}

                        if ( s>=0 ) return s;
                        break;
                    case 126 : 
                        int LA184_205 = input.LA(1);

                         
                        int index184_205 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_205);
                        if ( s>=0 ) return s;
                        break;
                    case 127 : 
                        int LA184_206 = input.LA(1);

                         
                        int index184_206 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_206);
                        if ( s>=0 ) return s;
                        break;
                    case 128 : 
                        int LA184_303 = input.LA(1);

                         
                        int index184_303 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 126;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_303);
                        if ( s>=0 ) return s;
                        break;
                    case 129 : 
                        int LA184_494 = input.LA(1);

                         
                        int index184_494 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_494);
                        if ( s>=0 ) return s;
                        break;
                    case 130 : 
                        int LA184_493 = input.LA(1);

                         
                        int index184_493 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_493);
                        if ( s>=0 ) return s;
                        break;
                    case 131 : 
                        int LA184_388 = input.LA(1);

                         
                        int index184_388 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_388);
                        if ( s>=0 ) return s;
                        break;
                    case 132 : 
                        int LA184_370 = input.LA(1);

                         
                        int index184_370 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_370);
                        if ( s>=0 ) return s;
                        break;
                    case 133 : 
                        int LA184_369 = input.LA(1);

                         
                        int index184_369 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_369);
                        if ( s>=0 ) return s;
                        break;
                    case 134 : 
                        int LA184_542 = input.LA(1);

                         
                        int index184_542 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_542);
                        if ( s>=0 ) return s;
                        break;
                    case 135 : 
                        int LA184_470 = input.LA(1);

                         
                        int index184_470 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_470);
                        if ( s>=0 ) return s;
                        break;
                    case 136 : 
                        int LA184_541 = input.LA(1);

                         
                        int index184_541 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_541);
                        if ( s>=0 ) return s;
                        break;
                    case 137 : 
                        int LA184_71 = input.LA(1);

                        s = -1;
                        if ( ((LA184_71>='\u0000' && LA184_71<='\t')||LA184_71=='\u000B'||(LA184_71>='\u000E' && LA184_71<='/')||(LA184_71>='1' && LA184_71<='3')||LA184_71=='5'||(LA184_71>='7' && LA184_71<='\uFFFF')) ) {s = 12;}

                        else if ( (LA184_71=='0') ) {s = 147;}

                        else if ( (LA184_71=='4'||LA184_71=='6') ) {s = 148;}

                        if ( s>=0 ) return s;
                        break;
                    case 138 : 
                        int LA184_322 = input.LA(1);

                         
                        int index184_322 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_322);
                        if ( s>=0 ) return s;
                        break;
                    case 139 : 
                        int LA184_323 = input.LA(1);

                         
                        int index184_323 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_323);
                        if ( s>=0 ) return s;
                        break;
                    case 140 : 
                        int LA184_127 = input.LA(1);

                         
                        int index184_127 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 126;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_127);
                        if ( s>=0 ) return s;
                        break;
                    case 141 : 
                        int LA184_128 = input.LA(1);

                         
                        int index184_128 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 126;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_128);
                        if ( s>=0 ) return s;
                        break;
                    case 142 : 
                        int LA184_519 = input.LA(1);

                         
                        int index184_519 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 126;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_519);
                        if ( s>=0 ) return s;
                        break;
                    case 143 : 
                        int LA184_520 = input.LA(1);

                         
                        int index184_520 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 126;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_520);
                        if ( s>=0 ) return s;
                        break;
                    case 144 : 
                        int LA184_278 = input.LA(1);

                         
                        int index184_278 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 75;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_278);
                        if ( s>=0 ) return s;
                        break;
                    case 145 : 
                        int LA184_212 = input.LA(1);

                         
                        int index184_212 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_212);
                        if ( s>=0 ) return s;
                        break;
                    case 146 : 
                        int LA184_263 = input.LA(1);

                         
                        int index184_263 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 174;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_263);
                        if ( s>=0 ) return s;
                        break;
                    case 147 : 
                        int LA184_405 = input.LA(1);

                         
                        int index184_405 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_405);
                        if ( s>=0 ) return s;
                        break;
                    case 148 : 
                        int LA184_288 = input.LA(1);

                         
                        int index184_288 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_288);
                        if ( s>=0 ) return s;
                        break;
                    case 149 : 
                        int LA184_287 = input.LA(1);

                         
                        int index184_287 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_287);
                        if ( s>=0 ) return s;
                        break;
                    case 150 : 
                        int LA184_151 = input.LA(1);

                         
                        int index184_151 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_151);
                        if ( s>=0 ) return s;
                        break;
                    case 151 : 
                        int LA184_317 = input.LA(1);

                         
                        int index184_317 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_317);
                        if ( s>=0 ) return s;
                        break;
                    case 152 : 
                        int LA184_149 = input.LA(1);

                         
                        int index184_149 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_149);
                        if ( s>=0 ) return s;
                        break;
                    case 153 : 
                        int LA184_545 = input.LA(1);

                         
                        int index184_545 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_545);
                        if ( s>=0 ) return s;
                        break;
                    case 154 : 
                        int LA184_397 = input.LA(1);

                         
                        int index184_397 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_397);
                        if ( s>=0 ) return s;
                        break;
                    case 155 : 
                        int LA184_396 = input.LA(1);

                         
                        int index184_396 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_396);
                        if ( s>=0 ) return s;
                        break;
                    case 156 : 
                        int LA184_546 = input.LA(1);

                         
                        int index184_546 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_546);
                        if ( s>=0 ) return s;
                        break;
                    case 157 : 
                        int LA184_223 = input.LA(1);

                         
                        int index184_223 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_223);
                        if ( s>=0 ) return s;
                        break;
                    case 158 : 
                        int LA184_401 = input.LA(1);

                         
                        int index184_401 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 139;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_401);
                        if ( s>=0 ) return s;
                        break;
                    case 159 : 
                        int LA184_402 = input.LA(1);

                         
                        int index184_402 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 139;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_402);
                        if ( s>=0 ) return s;
                        break;
                    case 160 : 
                        int LA184_109 = input.LA(1);

                         
                        int index184_109 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 75;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_109);
                        if ( s>=0 ) return s;
                        break;
                    case 161 : 
                        int LA184_48 = input.LA(1);

                        s = -1;
                        if ( (LA184_48=='m') ) {s = 111;}

                        else if ( (LA184_48=='M') ) {s = 112;}

                        else if ( (LA184_48=='x') ) {s = 113;}

                        else if ( (LA184_48=='0') ) {s = 114;}

                        else if ( (LA184_48=='4'||LA184_48=='6') ) {s = 115;}

                        else if ( (LA184_48=='X') ) {s = 116;}

                        else if ( ((LA184_48>='\u0000' && LA184_48<='\t')||LA184_48=='\u000B'||(LA184_48>='\u000E' && LA184_48<='/')||(LA184_48>='1' && LA184_48<='3')||(LA184_48>='8' && LA184_48<='L')||(LA184_48>='N' && LA184_48<='W')||(LA184_48>='Y' && LA184_48<='l')||(LA184_48>='n' && LA184_48<='w')||(LA184_48>='y' && LA184_48<='\uFFFF')) ) {s = 12;}

                        else if ( (LA184_48=='5'||LA184_48=='7') ) {s = 117;}

                        if ( s>=0 ) return s;
                        break;
                    case 162 : 
                        int LA184_490 = input.LA(1);

                         
                        int index184_490 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_490);
                        if ( s>=0 ) return s;
                        break;
                    case 163 : 
                        int LA184_489 = input.LA(1);

                         
                        int index184_489 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_489);
                        if ( s>=0 ) return s;
                        break;
                    case 164 : 
                        int LA184_83 = input.LA(1);

                        s = -1;
                        if ( (LA184_83=='i') ) {s = 162;}

                        else if ( (LA184_83=='I') ) {s = 163;}

                        else if ( ((LA184_83>='\u0000' && LA184_83<='\t')||LA184_83=='\u000B'||(LA184_83>='\u000E' && LA184_83<='/')||(LA184_83>='1' && LA184_83<='3')||LA184_83=='5'||(LA184_83>='7' && LA184_83<='H')||(LA184_83>='J' && LA184_83<='h')||(LA184_83>='j' && LA184_83<='\uFFFF')) ) {s = 12;}

                        else if ( (LA184_83=='0') ) {s = 164;}

                        else if ( (LA184_83=='4'||LA184_83=='6') ) {s = 165;}

                        if ( s>=0 ) return s;
                        break;
                    case 165 : 
                        int LA184_158 = input.LA(1);

                         
                        int index184_158 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_158);
                        if ( s>=0 ) return s;
                        break;
                    case 166 : 
                        int LA184_352 = input.LA(1);

                         
                        int index184_352 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 174;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_352);
                        if ( s>=0 ) return s;
                        break;
                    case 167 : 
                        int LA184_391 = input.LA(1);

                         
                        int index184_391 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 126;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_391);
                        if ( s>=0 ) return s;
                        break;
                    case 168 : 
                        int LA184_157 = input.LA(1);

                         
                        int index184_157 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_157);
                        if ( s>=0 ) return s;
                        break;
                    case 169 : 
                        int LA184_527 = input.LA(1);

                         
                        int index184_527 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_527);
                        if ( s>=0 ) return s;
                        break;
                    case 170 : 
                        int LA184_366 = input.LA(1);

                         
                        int index184_366 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 75;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_366);
                        if ( s>=0 ) return s;
                        break;
                    case 171 : 
                        int LA184_392 = input.LA(1);

                         
                        int index184_392 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 126;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_392);
                        if ( s>=0 ) return s;
                        break;
                    case 172 : 
                        int LA184_55 = input.LA(1);

                         
                        int index184_55 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_55);
                        if ( s>=0 ) return s;
                        break;
                    case 173 : 
                        int LA184_525 = input.LA(1);

                         
                        int index184_525 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 139;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_525);
                        if ( s>=0 ) return s;
                        break;
                    case 174 : 
                        int LA184_58 = input.LA(1);

                         
                        int index184_58 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_58);
                        if ( s>=0 ) return s;
                        break;
                    case 175 : 
                        int LA184_524 = input.LA(1);

                         
                        int index184_524 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 139;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_524);
                        if ( s>=0 ) return s;
                        break;
                    case 176 : 
                        int LA184_376 = input.LA(1);

                         
                        int index184_376 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_376);
                        if ( s>=0 ) return s;
                        break;
                    case 177 : 
                        int LA184_60 = input.LA(1);

                        s = -1;
                        if ( (LA184_60=='m') ) {s = 127;}

                        else if ( (LA184_60=='M') ) {s = 128;}

                        else if ( ((LA184_60>='\u0000' && LA184_60<='\t')||LA184_60=='\u000B'||(LA184_60>='\u000E' && LA184_60<='/')||(LA184_60>='1' && LA184_60<='3')||LA184_60=='5'||(LA184_60>='7' && LA184_60<='L')||(LA184_60>='N' && LA184_60<='l')||(LA184_60>='n' && LA184_60<='\uFFFF')) ) {s = 12;}

                        else if ( (LA184_60=='0') ) {s = 129;}

                        else if ( (LA184_60=='4'||LA184_60=='6') ) {s = 130;}

                        if ( s>=0 ) return s;
                        break;
                    case 178 : 
                        int LA184_375 = input.LA(1);

                         
                        int index184_375 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_375);
                        if ( s>=0 ) return s;
                        break;
                    case 179 : 
                        int LA184_381 = input.LA(1);

                         
                        int index184_381 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_381);
                        if ( s>=0 ) return s;
                        break;
                    case 180 : 
                        int LA184_380 = input.LA(1);

                         
                        int index184_380 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_380);
                        if ( s>=0 ) return s;
                        break;
                    case 181 : 
                        int LA184_65 = input.LA(1);

                         
                        int index184_65 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_65);
                        if ( s>=0 ) return s;
                        break;
                    case 182 : 
                        int LA184_62 = input.LA(1);

                         
                        int index184_62 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_62);
                        if ( s>=0 ) return s;
                        break;
                    case 183 : 
                        int LA184_538 = input.LA(1);

                         
                        int index184_538 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_538);
                        if ( s>=0 ) return s;
                        break;
                    case 184 : 
                        int LA184_196 = input.LA(1);

                         
                        int index184_196 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_196);
                        if ( s>=0 ) return s;
                        break;
                    case 185 : 
                        int LA184_134 = input.LA(1);

                         
                        int index184_134 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_134);
                        if ( s>=0 ) return s;
                        break;
                    case 186 : 
                        int LA184_197 = input.LA(1);

                         
                        int index184_197 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_197);
                        if ( s>=0 ) return s;
                        break;
                    case 187 : 
                        int LA184_63 = input.LA(1);

                        s = -1;
                        if ( (LA184_63=='m') ) {s = 132;}

                        else if ( (LA184_63=='M') ) {s = 133;}

                        else if ( (LA184_63=='s') ) {s = 134;}

                        else if ( (LA184_63=='0') ) {s = 135;}

                        else if ( (LA184_63=='4'||LA184_63=='6') ) {s = 136;}

                        else if ( (LA184_63=='S') ) {s = 137;}

                        else if ( ((LA184_63>='\u0000' && LA184_63<='\t')||LA184_63=='\u000B'||(LA184_63>='\u000E' && LA184_63<='/')||(LA184_63>='1' && LA184_63<='3')||(LA184_63>='8' && LA184_63<='L')||(LA184_63>='N' && LA184_63<='R')||(LA184_63>='T' && LA184_63<='l')||(LA184_63>='n' && LA184_63<='r')||(LA184_63>='t' && LA184_63<='\uFFFF')) ) {s = 12;}

                        else if ( (LA184_63=='5'||LA184_63=='7') ) {s = 138;}

                        if ( s>=0 ) return s;
                        break;
                    case 188 : 
                        int LA184_162 = input.LA(1);

                         
                        int index184_162 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_162);
                        if ( s>=0 ) return s;
                        break;
                    case 189 : 
                        int LA184_163 = input.LA(1);

                         
                        int index184_163 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_163);
                        if ( s>=0 ) return s;
                        break;
                    case 190 : 
                        int LA184_150 = input.LA(1);

                        s = -1;
                        if ( (LA184_150=='m') ) {s = 235;}

                        else if ( (LA184_150=='M') ) {s = 236;}

                        else if ( ((LA184_150>='\u0000' && LA184_150<='\t')||LA184_150=='\u000B'||(LA184_150>='\u000E' && LA184_150<='/')||(LA184_150>='1' && LA184_150<='3')||LA184_150=='5'||(LA184_150>='7' && LA184_150<='L')||(LA184_150>='N' && LA184_150<='l')||(LA184_150>='n' && LA184_150<='\uFFFF')) ) {s = 12;}

                        else if ( (LA184_150=='0') ) {s = 237;}

                        else if ( (LA184_150=='4'||LA184_150=='6') ) {s = 238;}

                        if ( s>=0 ) return s;
                        break;
                    case 191 : 
                        int LA184_77 = input.LA(1);

                        s = -1;
                        if ( (LA184_77=='h') ) {s = 152;}

                        else if ( (LA184_77=='H') ) {s = 153;}

                        else if ( ((LA184_77>='\u0000' && LA184_77<='\t')||LA184_77=='\u000B'||(LA184_77>='\u000E' && LA184_77<='/')||(LA184_77>='1' && LA184_77<='3')||LA184_77=='5'||(LA184_77>='7' && LA184_77<='G')||(LA184_77>='I' && LA184_77<='g')||(LA184_77>='i' && LA184_77<='\uFFFF')) ) {s = 12;}

                        else if ( (LA184_77=='0') ) {s = 154;}

                        else if ( (LA184_77=='4'||LA184_77=='6') ) {s = 155;}

                        if ( s>=0 ) return s;
                        break;
                    case 192 : 
                        int LA184_294 = input.LA(1);

                         
                        int index184_294 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_294);
                        if ( s>=0 ) return s;
                        break;
                    case 193 : 
                        int LA184_543 = input.LA(1);

                         
                        int index184_543 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 174;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_543);
                        if ( s>=0 ) return s;
                        break;
                    case 194 : 
                        int LA184_516 = input.LA(1);

                         
                        int index184_516 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_516);
                        if ( s>=0 ) return s;
                        break;
                    case 195 : 
                        int LA184_505 = input.LA(1);

                         
                        int index184_505 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_505);
                        if ( s>=0 ) return s;
                        break;
                    case 196 : 
                        int LA184_314 = input.LA(1);

                         
                        int index184_314 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 139;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_314);
                        if ( s>=0 ) return s;
                        break;
                    case 197 : 
                        int LA184_313 = input.LA(1);

                         
                        int index184_313 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 139;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_313);
                        if ( s>=0 ) return s;
                        break;
                    case 198 : 
                        int LA184_506 = input.LA(1);

                         
                        int index184_506 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_506);
                        if ( s>=0 ) return s;
                        break;
                    case 199 : 
                        int LA184_298 = input.LA(1);

                         
                        int index184_298 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_298);
                        if ( s>=0 ) return s;
                        break;
                    case 200 : 
                        int LA184_121 = input.LA(1);

                         
                        int index184_121 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_121);
                        if ( s>=0 ) return s;
                        break;
                    case 201 : 
                        int LA184_337 = input.LA(1);

                         
                        int index184_337 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 161;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_337);
                        if ( s>=0 ) return s;
                        break;
                    case 202 : 
                        int LA184_415 = input.LA(1);

                         
                        int index184_415 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_415);
                        if ( s>=0 ) return s;
                        break;
                    case 203 : 
                        int LA184_137 = input.LA(1);

                         
                        int index184_137 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_137);
                        if ( s>=0 ) return s;
                        break;
                    case 204 : 
                        int LA184_414 = input.LA(1);

                         
                        int index184_414 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 228;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_414);
                        if ( s>=0 ) return s;
                        break;
                    case 205 : 
                        int LA184_300 = input.LA(1);

                         
                        int index184_300 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_300);
                        if ( s>=0 ) return s;
                        break;
                    case 206 : 
                        int LA184_167 = input.LA(1);

                        s = -1;
                        if ( (LA184_167=='m') ) {s = 250;}

                        else if ( (LA184_167=='M') ) {s = 251;}

                        else if ( ((LA184_167>='\u0000' && LA184_167<='\t')||LA184_167=='\u000B'||(LA184_167>='\u000E' && LA184_167<='/')||(LA184_167>='1' && LA184_167<='3')||LA184_167=='5'||(LA184_167>='7' && LA184_167<='L')||(LA184_167>='N' && LA184_167<='l')||(LA184_167>='n' && LA184_167<='\uFFFF')) ) {s = 12;}

                        else if ( (LA184_167=='0') ) {s = 252;}

                        else if ( (LA184_167=='4'||LA184_167=='6') ) {s = 253;}

                        if ( s>=0 ) return s;
                        break;
                    case 207 : 
                        int LA184_454 = input.LA(1);

                         
                        int index184_454 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_454);
                        if ( s>=0 ) return s;
                        break;
                    case 208 : 
                        int LA184_455 = input.LA(1);

                         
                        int index184_455 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_455);
                        if ( s>=0 ) return s;
                        break;
                    case 209 : 
                        int LA184_124 = input.LA(1);

                         
                        int index184_124 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_124);
                        if ( s>=0 ) return s;
                        break;
                    case 210 : 
                        int LA184_515 = input.LA(1);

                         
                        int index184_515 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 110;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_515);
                        if ( s>=0 ) return s;
                        break;
                    case 211 : 
                        int LA184_213 = input.LA(1);

                         
                        int index184_213 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 118;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index184_213);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 184, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA180_eotS =
        "\12\uffff";
    static final String DFA180_eofS =
        "\12\uffff";
    static final String DFA180_minS =
        "\1\103\1\uffff\1\60\2\uffff\1\60\1\64\2\60\1\64";
    static final String DFA180_maxS =
        "\1\170\1\uffff\1\170\2\uffff\1\67\1\70\3\67";
    static final String DFA180_acceptS =
        "\1\uffff\1\1\1\uffff\1\2\1\3\5\uffff";
    static final String DFA180_specialS =
        "\12\uffff}>";
    static final String[] DFA180_transitionS = {
            "\1\4\20\uffff\1\3\3\uffff\1\1\3\uffff\1\2\6\uffff\1\4\20\uffff"+
            "\1\3\3\uffff\1\1",
            "",
            "\1\5\3\uffff\1\4\1\6\1\4\1\6\34\uffff\1\3\3\uffff\1\1\33\uffff"+
            "\1\3\3\uffff\1\1",
            "",
            "",
            "\1\7\3\uffff\1\4\1\6\1\4\1\6",
            "\1\3\3\uffff\1\1",
            "\1\10\3\uffff\1\4\1\6\1\4\1\6",
            "\1\11\3\uffff\1\4\1\6\1\4\1\6",
            "\1\4\1\6\1\4\1\6"
    };

    static final short[] DFA180_eot = DFA.unpackEncodedString(DFA180_eotS);
    static final short[] DFA180_eof = DFA.unpackEncodedString(DFA180_eofS);
    static final char[] DFA180_min = DFA.unpackEncodedStringToUnsignedChars(DFA180_minS);
    static final char[] DFA180_max = DFA.unpackEncodedStringToUnsignedChars(DFA180_maxS);
    static final short[] DFA180_accept = DFA.unpackEncodedString(DFA180_acceptS);
    static final short[] DFA180_special = DFA.unpackEncodedString(DFA180_specialS);
    static final short[][] DFA180_transition;

    static {
        int numStates = DFA180_transitionS.length;
        DFA180_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA180_transition[i] = DFA.unpackEncodedString(DFA180_transitionS[i]);
        }
    }

    class DFA180 extends DFA {

        public DFA180(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 180;
            this.eot = DFA180_eot;
            this.eof = DFA180_eof;
            this.min = DFA180_min;
            this.max = DFA180_max;
            this.accept = DFA180_accept;
            this.special = DFA180_special;
            this.transition = DFA180_transition;
        }
        public String getDescription() {
            return "1765:17: ( X | T | C )";
        }
    }
    static final String DFA201_eotS =
        "\2\uffff\1\73\1\75\1\77\1\101\2\uffff\1\105\1\107\4\uffff\1\112"+
        "\1\uffff\1\114\1\117\4\uffff\1\121\1\uffff\1\36\2\uffff\3\36\1\uffff"+
        "\2\36\1\uffff\1\143\2\uffff\2\36\3\uffff\17\71\35\uffff\2\36\1\u0082"+
        "\4\36\1\uffff\5\36\2\uffff\2\36\3\71\1\u0095\25\71\2\36\1\uffff"+
        "\1\u00af\1\u00b0\2\36\1\uffff\12\36\3\71\1\uffff\10\71\1\u00ca\16"+
        "\71\1\u00d9\1\u00da\3\uffff\15\36\3\71\1\u00eb\7\71\1\uffff\11\71"+
        "\1\u00fe\1\u00ff\1\71\1\u0101\1\71\2\uffff\15\36\1\uffff\2\71\1"+
        "\uffff\1\u0111\1\u0112\20\71\2\uffff\1\u0125\1\uffff\1\u0126\14"+
        "\36\1\u0131\1\71\2\uffff\16\71\1\u0143\2\71\1\u0146\2\uffff\10\36"+
        "\3\uffff\1\u014c\1\71\1\u014e\1\71\1\u0150\14\71\1\uffff\2\71\1"+
        "\uffff\5\36\1\uffff\1\71\1\uffff\1\71\1\uffff\1\71\1\u0167\1\u0169"+
        "\5\71\1\u016f\7\71\3\36\1\u0178\1\71\1\u017a\1\uffff\1\71\1\uffff"+
        "\1\71\1\u017e\3\71\1\uffff\2\71\1\u0184\4\71\1\36\1\uffff\1\71\1"+
        "\uffff\1\71\1\u018c\1\71\1\uffff\5\71\1\uffff\4\71\1\uffff\2\71"+
        "\1\uffff\1\71\1\u019b\2\71\1\u019e\1\u019f\10\71\1\uffff\1\71\1"+
        "\u01aa\2\uffff\1\u01ab\1\u01ac\2\71\1\u01af\3\71\1\u01b3\1\71\3"+
        "\uffff\1\u01b5\1\71\1\uffff\3\71\1\uffff\1\71\1\uffff\1\71\1\u01bc"+
        "\4\71\1\uffff\1\u01c1\3\71\1\uffff\2\71\1\u01c7\1\u01c8\1\71\2\uffff"+
        "\1\u01ca\1\uffff";
    static final String DFA201_eofS =
        "\u01cb\uffff";
    static final String DFA201_minS =
        "\1\11\1\55\1\41\1\55\2\75\1\uffff\1\55\2\75\4\uffff\1\74\1\uffff"+
        "\1\72\1\52\4\uffff\1\56\1\11\1\110\2\uffff\1\116\1\117\1\116\1\uffff"+
        "\2\122\1\0\1\55\2\uffff\1\117\1\105\3\uffff\1\145\1\106\1\101\1"+
        "\105\1\101\1\110\3\117\2\105\1\115\1\101\1\105\1\101\35\uffff\1"+
        "\105\1\114\1\55\1\124\1\104\2\114\1\0\1\114\1\122\1\60\1\122\1\65"+
        "\2\uffff\1\115\1\107\1\163\1\120\1\103\1\55\1\107\1\104\1\130\1"+
        "\115\1\101\3\116\1\120\1\124\1\106\1\107\1\124\1\117\1\105\1\124"+
        "\1\123\1\103\1\102\1\122\1\111\1\116\1\131\1\uffff\2\55\2\50\1\0"+
        "\1\114\1\60\1\114\1\62\1\50\1\60\1\65\1\122\1\101\1\105\1\164\1"+
        "\117\1\114\1\uffff\1\105\2\111\1\105\1\122\1\116\2\124\1\55\1\103"+
        "\1\55\2\124\1\110\1\125\1\132\1\102\2\105\1\110\1\125\1\116\1\114"+
        "\2\55\3\uffff\1\50\1\60\1\50\1\103\1\60\1\62\1\114\1\120\1\60\1"+
        "\65\1\122\1\111\1\130\1\56\1\122\1\125\1\55\1\101\1\116\2\123\1"+
        "\124\1\105\1\55\1\uffff\1\124\1\103\1\117\1\55\1\124\1\122\1\55"+
        "\1\113\1\116\2\55\1\107\1\55\1\105\2\uffff\1\60\1\103\2\50\1\60"+
        "\1\62\1\114\1\122\2\65\1\122\1\116\1\120\1\uffff\1\124\1\104\1\uffff"+
        "\2\55\1\120\2\105\1\116\1\106\1\111\2\105\1\111\1\115\1\102\1\55"+
        "\1\116\1\104\1\111\1\104\2\uffff\1\55\1\uffff\1\55\1\60\1\103\2"+
        "\50\1\65\1\62\1\114\1\105\1\65\1\122\2\50\1\55\1\105\2\uffff\1\101"+
        "\1\124\1\122\1\124\1\101\1\117\1\106\1\116\1\107\1\55\1\117\1\111"+
        "\1\117\1\102\1\55\1\117\1\124\1\55\2\uffff\1\64\1\103\2\50\1\62"+
        "\1\114\1\106\1\122\3\uffff\1\55\1\103\3\55\1\103\1\116\2\124\1\110"+
        "\1\103\1\120\1\104\1\124\1\117\1\111\1\117\1\uffff\1\103\1\55\1"+
        "\uffff\1\103\2\50\1\114\1\111\1\uffff\1\105\1\uffff\1\123\1\uffff"+
        "\1\105\2\55\1\105\1\124\2\105\1\111\1\55\1\104\1\124\1\120\1\104"+
        "\1\124\1\125\1\113\2\50\1\130\1\55\1\124\1\55\1\uffff\1\103\1\uffff"+
        "\1\122\1\55\1\106\1\116\1\107\1\uffff\1\114\1\117\1\55\1\104\1\124"+
        "\1\115\1\105\1\50\1\uffff\1\131\1\uffff\1\117\1\55\1\103\1\uffff"+
        "\2\124\1\110\1\105\1\115\1\uffff\1\114\1\117\1\105\1\131\1\uffff"+
        "\1\114\1\122\1\uffff\1\117\1\55\1\105\1\124\2\55\1\105\1\115\1\116"+
        "\1\106\1\105\1\116\1\122\1\103\1\uffff\1\122\1\55\2\uffff\2\55\1"+
        "\124\1\122\1\55\1\105\1\116\1\117\1\55\1\103\3\uffff\1\55\1\101"+
        "\1\uffff\1\122\1\105\1\122\1\uffff\1\117\1\uffff\1\115\1\55\1\122"+
        "\1\116\1\122\1\105\1\uffff\1\55\1\105\1\116\1\123\1\uffff\1\122"+
        "\1\105\2\55\1\122\2\uffff\1\55\1\uffff";
    static final String DFA201_maxS =
        "\2\uffff\1\41\1\uffff\2\75\1\uffff\1\uffff\2\75\4\uffff\1\75\1\uffff"+
        "\1\72\1\57\4\uffff\1\71\1\117\1\110\2\uffff\1\122\1\117\1\116\1"+
        "\uffff\2\162\2\uffff\2\uffff\1\117\1\105\3\uffff\1\145\1\116\1\101"+
        "\1\111\1\101\1\117\1\125\2\117\1\105\1\111\1\127\1\130\1\105\1\110"+
        "\35\uffff\1\105\1\114\1\uffff\1\124\1\104\2\154\1\uffff\1\154\1"+
        "\162\1\67\1\162\1\65\2\uffff\1\115\1\107\1\163\1\120\1\103\1\uffff"+
        "\1\107\1\104\1\130\1\115\1\101\1\125\1\122\1\116\1\120\1\124\1\106"+
        "\1\107\1\124\1\117\1\105\1\124\1\123\1\103\1\102\1\122\1\111\1\116"+
        "\1\131\1\uffff\2\uffff\2\50\1\uffff\1\154\1\67\1\154\1\62\1\55\1"+
        "\67\1\65\1\162\1\101\1\105\1\164\1\117\1\114\1\uffff\1\105\2\111"+
        "\1\105\1\122\1\116\2\124\1\uffff\1\103\1\55\2\124\1\110\1\125\1"+
        "\132\1\102\2\105\1\110\1\125\1\116\1\114\2\uffff\3\uffff\1\50\1"+
        "\66\1\50\1\143\1\67\1\62\1\154\1\120\1\67\1\65\1\162\1\111\1\130"+
        "\1\56\1\122\1\125\1\uffff\1\101\1\116\2\123\1\124\1\105\1\55\1\uffff"+
        "\1\124\1\122\1\117\1\55\1\124\1\122\1\55\1\113\1\116\2\uffff\1\107"+
        "\1\uffff\1\105\2\uffff\1\66\1\143\2\50\1\67\1\62\1\154\1\122\1\67"+
        "\1\65\1\162\1\116\1\120\1\uffff\1\124\1\104\1\uffff\2\uffff\1\120"+
        "\2\105\1\116\1\106\1\111\2\105\1\111\1\115\1\124\1\55\1\116\1\104"+
        "\1\111\1\104\2\uffff\1\uffff\1\uffff\1\uffff\1\66\1\143\2\50\1\67"+
        "\1\62\1\154\1\105\1\65\1\162\2\50\1\uffff\1\105\2\uffff\1\101\1"+
        "\124\1\122\1\124\1\101\1\117\1\106\1\116\1\107\1\55\1\117\1\111"+
        "\1\117\1\124\1\uffff\1\117\1\124\1\uffff\2\uffff\1\66\1\143\2\50"+
        "\1\62\1\154\1\106\1\162\3\uffff\1\uffff\1\103\1\uffff\1\55\1\uffff"+
        "\1\103\1\116\2\124\1\110\1\122\1\120\1\104\1\124\1\117\1\111\1\117"+
        "\1\uffff\1\103\1\55\1\uffff\1\143\2\50\1\154\1\111\1\uffff\1\105"+
        "\1\uffff\1\123\1\uffff\1\105\2\uffff\1\105\1\124\2\105\1\111\1\uffff"+
        "\1\104\1\124\1\120\1\104\1\124\1\125\1\113\2\50\1\130\1\uffff\1"+
        "\124\1\uffff\1\uffff\1\103\1\uffff\1\122\1\uffff\1\106\1\116\1\107"+
        "\1\uffff\1\114\1\117\1\uffff\1\104\1\124\1\115\1\105\1\50\1\uffff"+
        "\1\131\1\uffff\1\117\1\uffff\1\103\1\uffff\2\124\1\110\1\105\1\115"+
        "\1\uffff\1\114\1\117\1\105\1\131\1\uffff\1\114\1\122\1\uffff\1\117"+
        "\1\uffff\1\105\1\124\2\uffff\1\105\1\115\1\116\1\106\1\105\1\116"+
        "\1\122\1\103\1\uffff\1\122\1\uffff\2\uffff\2\uffff\1\124\1\122\1"+
        "\uffff\1\105\1\116\1\117\1\uffff\1\103\3\uffff\1\uffff\1\101\1\uffff"+
        "\1\122\1\105\1\122\1\uffff\1\117\1\uffff\1\115\1\uffff\1\122\1\116"+
        "\1\122\1\105\1\uffff\1\uffff\1\105\1\116\1\123\1\uffff\1\122\1\105"+
        "\2\uffff\1\122\2\uffff\1\uffff\1\uffff";
    static final String DFA201_acceptS =
        "\6\uffff\1\6\3\uffff\1\12\1\13\1\14\1\15\1\uffff\1\17\2\uffff\1"+
        "\24\1\26\1\27\1\30\3\uffff\1\42\1\45\3\uffff\1\52\4\uffff\1\130"+
        "\1\131\2\uffff\1\136\1\137\1\1\17\uffff\1\124\1\2\1\36\1\3\1\23"+
        "\1\4\1\32\1\5\1\33\1\7\1\125\1\10\1\25\1\37\1\11\1\34\1\40\1\16"+
        "\1\21\1\20\1\140\1\141\1\22\1\43\1\31\1\35\1\126\1\127\1\55\15\uffff"+
        "\1\53\1\54\35\uffff\1\51\22\uffff\1\115\31\uffff\1\47\1\50\1\132"+
        "\30\uffff\1\117\16\uffff\1\41\1\46\15\uffff\1\44\2\uffff\1\57\22"+
        "\uffff\1\116\1\122\1\uffff\1\114\17\uffff\1\60\1\110\22\uffff\1"+
        "\113\1\123\10\uffff\1\134\1\135\1\56\21\uffff\1\121\2\uffff\1\112"+
        "\5\uffff\1\111\1\uffff\1\62\1\uffff\1\107\26\uffff\1\120\1\uffff"+
        "\1\66\5\uffff\1\77\10\uffff\1\61\1\uffff\1\64\3\uffff\1\70\5\uffff"+
        "\1\102\4\uffff\1\133\2\uffff\1\67\16\uffff\1\73\2\uffff\1\100\1"+
        "\101\12\uffff\1\75\1\103\1\104\2\uffff\1\63\3\uffff\1\74\1\uffff"+
        "\1\105\6\uffff\1\65\4\uffff\1\71\5\uffff\1\106\1\72\1\uffff\1\76";
    static final String DFA201_specialS =
        "\41\uffff\1\1\73\uffff\1\2\51\uffff\1\0\u0143\uffff}>";
    static final String[] DFA201_transitionS = {
            "\1\47\1\50\2\uffff\1\50\22\uffff\1\47\1\27\1\32\1\42\1\7\1\43"+
            "\1\31\1\32\1\23\1\24\1\10\1\22\1\25\1\3\1\26\1\21\12\44\1\20"+
            "\1\17\1\2\1\16\1\11\1\uffff\1\1\1\35\2\36\1\45\11\36\1\34\1"+
            "\33\2\36\1\46\2\36\1\40\1\36\1\30\3\36\1\14\1\41\1\15\1\6\1"+
            "\36\1\uffff\24\36\1\37\5\36\1\12\1\5\1\13\1\4\1\uffff\uff80"+
            "\36",
            "\1\65\2\uffff\12\71\6\uffff\1\51\1\71\1\62\1\57\1\67\1\66\1"+
            "\60\2\71\1\53\2\71\1\63\1\55\1\56\1\71\1\54\1\71\1\64\1\71\1"+
            "\61\2\71\1\70\3\71\1\uffff\1\71\2\uffff\1\71\1\uffff\21\71\1"+
            "\52\10\71\5\uffff\uff80\71",
            "\1\72",
            "\1\74\23\uffff\32\36\1\uffff\1\36\2\uffff\1\36\1\uffff\32\36"+
            "\5\uffff\uff80\36",
            "\1\76",
            "\1\100",
            "",
            "\1\103\2\uffff\12\103\3\uffff\1\102\3\uffff\32\103\1\uffff"+
            "\1\103\2\uffff\1\103\1\uffff\32\103\5\uffff\uff80\103",
            "\1\104",
            "\1\106",
            "",
            "",
            "",
            "",
            "\1\111\1\110",
            "",
            "\1\113",
            "\1\115\4\uffff\1\116",
            "",
            "",
            "",
            "",
            "\1\120\1\uffff\12\44",
            "\1\125\26\uffff\1\125\16\uffff\1\125\15\uffff\1\122\6\uffff"+
            "\1\123\4\uffff\1\125\5\uffff\1\124",
            "\1\126",
            "",
            "",
            "\1\127\3\uffff\1\130",
            "\1\131",
            "\1\132",
            "",
            "\1\134\11\uffff\1\135\25\uffff\1\133",
            "\1\136\11\uffff\1\135\25\uffff\1\133",
            "\12\36\1\uffff\1\36\2\uffff\42\36\1\140\4\36\1\142\1\36\1\142"+
            "\35\36\1\141\37\36\1\137\uff8a\36",
            "\1\144\2\uffff\12\144\7\uffff\32\144\1\uffff\1\144\2\uffff"+
            "\1\144\1\uffff\32\144\5\uffff\uff80\144",
            "",
            "",
            "\1\145",
            "\1\146",
            "",
            "",
            "",
            "\1\147",
            "\1\152\6\uffff\1\150\1\151",
            "\1\153",
            "\1\154\3\uffff\1\155",
            "\1\156",
            "\1\157\6\uffff\1\160",
            "\1\161\5\uffff\1\162",
            "\1\163",
            "\1\164",
            "\1\165",
            "\1\167\3\uffff\1\166",
            "\1\170\11\uffff\1\171",
            "\1\174\12\uffff\1\173\13\uffff\1\172",
            "\1\175",
            "\1\176\6\uffff\1\177",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u0080",
            "\1\u0081",
            "\1\36\2\uffff\12\36\7\uffff\32\36\1\uffff\1\36\2\uffff\1\36"+
            "\1\uffff\32\36\5\uffff\uff80\36",
            "\1\u0083",
            "\1\u0084",
            "\1\u0086\17\uffff\1\u0087\17\uffff\1\u0085",
            "\1\u0086\17\uffff\1\u0087\17\uffff\1\u0085",
            "\12\36\1\uffff\1\36\2\uffff\42\36\1\u0089\4\36\1\u008b\1\36"+
            "\1\u008b\32\36\1\u008a\37\36\1\u0088\uff8d\36",
            "\1\u008c\17\uffff\1\u0087\17\uffff\1\u0085",
            "\1\134\11\uffff\1\135\25\uffff\1\133",
            "\1\u008d\4\uffff\1\u008e\1\uffff\1\u008e",
            "\1\134\11\uffff\1\135\25\uffff\1\133",
            "\1\u008f",
            "",
            "",
            "\1\u0090",
            "\1\u0091",
            "\1\u0092",
            "\1\u0093",
            "\1\u0094",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u0096",
            "\1\u0097",
            "\1\u0098",
            "\1\u0099",
            "\1\u009a",
            "\1\u009c\6\uffff\1\u009b",
            "\1\u009d\3\uffff\1\u009e",
            "\1\u009f",
            "\1\u00a0",
            "\1\u00a1",
            "\1\u00a2",
            "\1\u00a3",
            "\1\u00a4",
            "\1\u00a5",
            "\1\u00a6",
            "\1\u00a7",
            "\1\u00a8",
            "\1\u00a9",
            "\1\u00aa",
            "\1\u00ab",
            "\1\u00ac",
            "\1\u00ad",
            "\1\u00ae",
            "",
            "\1\36\2\uffff\12\36\7\uffff\32\36\1\uffff\1\36\2\uffff\1\36"+
            "\1\uffff\32\36\5\uffff\uff80\36",
            "\1\36\2\uffff\12\36\7\uffff\32\36\1\uffff\1\36\2\uffff\1\36"+
            "\1\uffff\32\36\5\uffff\uff80\36",
            "\1\u00b1",
            "\1\u00b1",
            "\12\36\1\uffff\1\36\2\uffff\42\36\1\u00b3\3\36\1\u00b5\1\36"+
            "\1\u00b5\25\36\1\u00b4\37\36\1\u00b2\uff93\36",
            "\1\u0086\17\uffff\1\u0087\17\uffff\1\u0085",
            "\1\u00b6\4\uffff\1\u00b7\1\uffff\1\u00b7",
            "\1\u0086\17\uffff\1\u0087\17\uffff\1\u0085",
            "\1\u00b8",
            "\1\u00b1\4\uffff\1\u00b9",
            "\1\u00ba\4\uffff\1\u00bb\1\uffff\1\u00bb",
            "\1\u00bc",
            "\1\134\11\uffff\1\135\25\uffff\1\133",
            "\1\u00bd",
            "\1\u00be",
            "\1\u00bf",
            "\1\u00c0",
            "\1\u00c1",
            "",
            "\1\u00c2",
            "\1\u00c3",
            "\1\u00c4",
            "\1\u00c5",
            "\1\u00c6",
            "\1\u00c7",
            "\1\u00c8",
            "\1\u00c9",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u00cb",
            "\1\u00cc",
            "\1\u00cd",
            "\1\u00ce",
            "\1\u00cf",
            "\1\u00d0",
            "\1\u00d1",
            "\1\u00d2",
            "\1\u00d3",
            "\1\u00d4",
            "\1\u00d5",
            "\1\u00d6",
            "\1\u00d7",
            "\1\u00d8",
            "\1\36\2\uffff\12\36\7\uffff\32\36\1\uffff\1\36\2\uffff\1\36"+
            "\1\uffff\32\36\5\uffff\uff80\36",
            "\1\36\2\uffff\12\36\7\uffff\32\36\1\uffff\1\36\2\uffff\1\36"+
            "\1\uffff\32\36\5\uffff\uff80\36",
            "",
            "",
            "",
            "\1\u00b1",
            "\1\u00db\3\uffff\1\u00dc\1\uffff\1\u00dc",
            "\1\u00b1",
            "\1\u00de\37\uffff\1\u00dd",
            "\1\u00df\4\uffff\1\u00e0\1\uffff\1\u00e0",
            "\1\u00e1",
            "\1\u0086\17\uffff\1\u0087\17\uffff\1\u0085",
            "\1\u00e2",
            "\1\u00e3\4\uffff\1\u00e4\1\uffff\1\u00e4",
            "\1\u00e5",
            "\1\134\11\uffff\1\135\25\uffff\1\133",
            "\1\u00e6",
            "\1\u00e7",
            "\1\u00e8",
            "\1\u00e9",
            "\1\u00ea",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u00ec",
            "\1\u00ed",
            "\1\u00ee",
            "\1\u00ef",
            "\1\u00f0",
            "\1\u00f1",
            "\1\u00f2",
            "",
            "\1\u00f3",
            "\1\u00f5\10\uffff\1\u00f4\5\uffff\1\u00f6",
            "\1\u00f7",
            "\1\u00f8",
            "\1\u00f9",
            "\1\u00fa",
            "\1\u00fb",
            "\1\u00fc",
            "\1\u00fd",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u0100",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u0102",
            "",
            "",
            "\1\u0103\3\uffff\1\u0104\1\uffff\1\u0104",
            "\1\u0106\37\uffff\1\u0105",
            "\1\u00b1",
            "\1\u00b1",
            "\1\u0107\4\uffff\1\u0108\1\uffff\1\u0108",
            "\1\u0109",
            "\1\u0086\17\uffff\1\u0087\17\uffff\1\u0085",
            "\1\u010a",
            "\1\u010b\1\uffff\1\u010b",
            "\1\u010c",
            "\1\134\11\uffff\1\135\25\uffff\1\133",
            "\1\u010d",
            "\1\u010e",
            "",
            "\1\u010f",
            "\1\u0110",
            "",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u0113",
            "\1\u0114",
            "\1\u0115",
            "\1\u0116",
            "\1\u0117",
            "\1\u0118",
            "\1\u0119",
            "\1\u011a",
            "\1\u011b",
            "\1\u011c",
            "\1\u011f\12\uffff\1\u011e\6\uffff\1\u011d",
            "\1\u0120",
            "\1\u0121",
            "\1\u0122",
            "\1\u0123",
            "\1\u0124",
            "",
            "",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u0127\3\uffff\1\u0128\1\uffff\1\u0128",
            "\1\u012a\37\uffff\1\u0129",
            "\1\u00b1",
            "\1\u00b1",
            "\1\u012b\1\uffff\1\u012b",
            "\1\u012c",
            "\1\u0086\17\uffff\1\u0087\17\uffff\1\u0085",
            "\1\u012d",
            "\1\u012e",
            "\1\134\11\uffff\1\135\25\uffff\1\133",
            "\1\u012f",
            "\1\u0130",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u0132",
            "",
            "",
            "\1\u0133",
            "\1\u0134",
            "\1\u0135",
            "\1\u0136",
            "\1\u0137",
            "\1\u0138",
            "\1\u0139",
            "\1\u013a",
            "\1\u013b",
            "\1\u013c",
            "\1\u013d",
            "\1\u013e",
            "\1\u013f",
            "\1\u0142\12\uffff\1\u0141\6\uffff\1\u0140",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u0144",
            "\1\u0145",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "",
            "",
            "\1\u0147\1\uffff\1\u0147",
            "\1\u0149\37\uffff\1\u0148",
            "\1\u00b1",
            "\1\u00b1",
            "\1\u014a",
            "\1\u0086\17\uffff\1\u0087\17\uffff\1\u0085",
            "\1\u014b",
            "\1\134\11\uffff\1\135\25\uffff\1\133",
            "",
            "",
            "",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u014d",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u014f",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u0151",
            "\1\u0152",
            "\1\u0153",
            "\1\u0154",
            "\1\u0155",
            "\1\u0157\10\uffff\1\u0156\5\uffff\1\u0158",
            "\1\u0159",
            "\1\u015a",
            "\1\u015b",
            "\1\u015c",
            "\1\u015d",
            "\1\u015e",
            "",
            "\1\u015f",
            "\1\u0160",
            "",
            "\1\u0162\37\uffff\1\u0161",
            "\1\u00b1",
            "\1\u00b1",
            "\1\u0086\17\uffff\1\u0087\17\uffff\1\u0085",
            "\1\u0163",
            "",
            "\1\u0164",
            "",
            "\1\u0165",
            "",
            "\1\u0166",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u0168\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1"+
            "\71\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u016a",
            "\1\u016b",
            "\1\u016c",
            "\1\u016d",
            "\1\u016e",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u0170",
            "\1\u0171",
            "\1\u0172",
            "\1\u0173",
            "\1\u0174",
            "\1\u0175",
            "\1\u0176",
            "\1\u00b1",
            "\1\u00b1",
            "\1\u0177",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u0179",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "",
            "\1\u017b",
            "",
            "\1\u017c",
            "\1\u017d\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1"+
            "\71\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u017f",
            "\1\u0180",
            "\1\u0181",
            "",
            "\1\u0182",
            "\1\u0183",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u0185",
            "\1\u0186",
            "\1\u0187",
            "\1\u0188",
            "\1\u0189",
            "",
            "\1\u018a",
            "",
            "\1\u018b",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u018d",
            "",
            "\1\u018e",
            "\1\u018f",
            "\1\u0190",
            "\1\u0191",
            "\1\u0192",
            "",
            "\1\u0193",
            "\1\u0194",
            "\1\u0195",
            "\1\u0196",
            "",
            "\1\u0197",
            "\1\u0198",
            "",
            "\1\u0199",
            "\1\u019a\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1"+
            "\71\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u019c",
            "\1\u019d",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u01a0",
            "\1\u01a1",
            "\1\u01a2",
            "\1\u01a3",
            "\1\u01a4",
            "\1\u01a5",
            "\1\u01a6",
            "\1\u01a7",
            "",
            "\1\u01a8",
            "\1\u01a9\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1"+
            "\71\1\uffff\32\71\5\uffff\uff80\71",
            "",
            "",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u01ad",
            "\1\u01ae",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u01b0",
            "\1\u01b1",
            "\1\u01b2",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u01b4",
            "",
            "",
            "",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u01b6",
            "",
            "\1\u01b7",
            "\1\u01b8",
            "\1\u01b9",
            "",
            "\1\u01ba",
            "",
            "\1\u01bb",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u01bd",
            "\1\u01be",
            "\1\u01bf",
            "\1\u01c0",
            "",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u01c2",
            "\1\u01c3",
            "\1\u01c4",
            "",
            "\1\u01c5",
            "\1\u01c6",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            "\1\u01c9",
            "",
            "",
            "\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71"+
            "\1\uffff\32\71\5\uffff\uff80\71",
            ""
    };

    static final short[] DFA201_eot = DFA.unpackEncodedString(DFA201_eotS);
    static final short[] DFA201_eof = DFA.unpackEncodedString(DFA201_eofS);
    static final char[] DFA201_min = DFA.unpackEncodedStringToUnsignedChars(DFA201_minS);
    static final char[] DFA201_max = DFA.unpackEncodedStringToUnsignedChars(DFA201_maxS);
    static final short[] DFA201_accept = DFA.unpackEncodedString(DFA201_acceptS);
    static final short[] DFA201_special = DFA.unpackEncodedString(DFA201_specialS);
    static final short[][] DFA201_transition;

    static {
        int numStates = DFA201_transitionS.length;
        DFA201_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA201_transition[i] = DFA.unpackEncodedString(DFA201_transitionS[i]);
        }
    }

    class DFA201 extends DFA {

        public DFA201(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 201;
            this.eot = DFA201_eot;
            this.eof = DFA201_eof;
            this.min = DFA201_min;
            this.max = DFA201_max;
            this.accept = DFA201_accept;
            this.special = DFA201_special;
            this.transition = DFA201_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( GEN | CDO | CDC | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS | GREATER | LBRACE | RBRACE | LBRACKET | RBRACKET | OPEQ | SEMI | COLON | DCOLON | SOLIDUS | MINUS | PLUS | STAR | LPAREN | RPAREN | COMMA | DOT | TILDE | PIPE | CP_EQ | CP_NOT_EQ | LESS | GREATER_OR_EQ | LESS_OR_EQ | LESS_WHEN | LESS_AND | LESS_DOTS | LESS_REST | STRING | ONLY | NOT | AND | OR | IDENT | HASH_SYMBOL | HASH | IMPORTANT_SYM | IMPORT_SYM | PAGE_SYM | MEDIA_SYM | NAMESPACE_SYM | CHARSET_SYM | COUNTER_STYLE_SYM | FONT_FACE_SYM | TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM | MOZ_DOCUMENT_SYM | WEBKIT_KEYFRAMES_SYM | SASS_CONTENT | SASS_MIXIN | SASS_INCLUDE | SASS_EXTEND | SASS_DEBUG | SASS_WARN | SASS_IF | SASS_ELSE | SASS_FOR | SASS_FUNCTION | SASS_RETURN | SASS_EACH | SASS_WHILE | AT_IDENT | SASS_VAR | SASS_DEFAULT | SASS_OPTIONAL | SASS_EXTEND_ONLY_SELECTOR | NUMBER | URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP | WS | NL | COMMENT | LINE_COMMENT );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA201_135 = input.LA(1);

                        s = -1;
                        if ( (LA201_135=='l') ) {s = 178;}

                        else if ( (LA201_135=='0') ) {s = 179;}

                        else if ( (LA201_135=='L') ) {s = 180;}

                        else if ( ((LA201_135>='\u0000' && LA201_135<='\t')||LA201_135=='\u000B'||(LA201_135>='\u000E' && LA201_135<='/')||(LA201_135>='1' && LA201_135<='3')||LA201_135=='5'||(LA201_135>='7' && LA201_135<='K')||(LA201_135>='M' && LA201_135<='k')||(LA201_135>='m' && LA201_135<='\uFFFF')) ) {s = 30;}

                        else if ( (LA201_135=='4'||LA201_135=='6') ) {s = 181;}

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA201_33 = input.LA(1);

                        s = -1;
                        if ( (LA201_33=='u') ) {s = 95;}

                        else if ( (LA201_33=='0') ) {s = 96;}

                        else if ( (LA201_33=='U') ) {s = 97;}

                        else if ( ((LA201_33>='\u0000' && LA201_33<='\t')||LA201_33=='\u000B'||(LA201_33>='\u000E' && LA201_33<='/')||(LA201_33>='1' && LA201_33<='4')||LA201_33=='6'||(LA201_33>='8' && LA201_33<='T')||(LA201_33>='V' && LA201_33<='t')||(LA201_33>='v' && LA201_33<='\uFFFF')) ) {s = 30;}

                        else if ( (LA201_33=='5'||LA201_33=='7') ) {s = 98;}

                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA201_93 = input.LA(1);

                        s = -1;
                        if ( (LA201_93=='r') ) {s = 136;}

                        else if ( (LA201_93=='0') ) {s = 137;}

                        else if ( (LA201_93=='R') ) {s = 138;}

                        else if ( ((LA201_93>='\u0000' && LA201_93<='\t')||LA201_93=='\u000B'||(LA201_93>='\u000E' && LA201_93<='/')||(LA201_93>='1' && LA201_93<='4')||LA201_93=='6'||(LA201_93>='8' && LA201_93<='Q')||(LA201_93>='S' && LA201_93<='q')||(LA201_93>='s' && LA201_93<='\uFFFF')) ) {s = 30;}

                        else if ( (LA201_93=='5'||LA201_93=='7') ) {s = 139;}

                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 201, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA204_eotS =
        "\12\uffff";
    static final String DFA204_eofS =
        "\12\uffff";
    static final String DFA204_minS =
        "\1\103\1\uffff\1\60\2\uffff\1\60\1\64\2\60\1\64";
    static final String DFA204_maxS =
        "\1\170\1\uffff\1\170\2\uffff\1\67\1\70\3\67";
    static final String DFA204_acceptS =
        "\1\uffff\1\1\1\uffff\1\2\1\3\5\uffff";
    static final String DFA204_specialS =
        "\12\uffff}>";
    static final String[] DFA204_transitionS = {
            "\1\4\20\uffff\1\3\3\uffff\1\1\3\uffff\1\2\6\uffff\1\4\20\uffff"+
            "\1\3\3\uffff\1\1",
            "",
            "\1\5\3\uffff\1\4\1\6\1\4\1\6\34\uffff\1\3\3\uffff\1\1\33\uffff"+
            "\1\3\3\uffff\1\1",
            "",
            "",
            "\1\7\3\uffff\1\4\1\6\1\4\1\6",
            "\1\3\3\uffff\1\1",
            "\1\10\3\uffff\1\4\1\6\1\4\1\6",
            "\1\11\3\uffff\1\4\1\6\1\4\1\6",
            "\1\4\1\6\1\4\1\6"
    };

    static final short[] DFA204_eot = DFA.unpackEncodedString(DFA204_eotS);
    static final short[] DFA204_eof = DFA.unpackEncodedString(DFA204_eofS);
    static final char[] DFA204_min = DFA.unpackEncodedStringToUnsignedChars(DFA204_minS);
    static final char[] DFA204_max = DFA.unpackEncodedStringToUnsignedChars(DFA204_maxS);
    static final short[] DFA204_accept = DFA.unpackEncodedString(DFA204_acceptS);
    static final short[] DFA204_special = DFA.unpackEncodedString(DFA204_specialS);
    static final short[][] DFA204_transition;

    static {
        int numStates = DFA204_transitionS.length;
        DFA204_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA204_transition[i] = DFA.unpackEncodedString(DFA204_transitionS[i]);
        }
    }

    class DFA204 extends DFA {

        public DFA204(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 204;
            this.eot = DFA204_eot;
            this.eof = DFA204_eof;
            this.min = DFA204_min;
            this.max = DFA204_max;
            this.accept = DFA204_accept;
            this.special = DFA204_special;
            this.transition = DFA204_transition;
        }
        public String getDescription() {
            return "1763:17: ( X | T | C )";
        }
    }
 

}