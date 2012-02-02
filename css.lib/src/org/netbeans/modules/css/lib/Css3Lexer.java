// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2012-02-02 13:27:50

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
    public static final int T__114=114;
    public static final int NAMESPACE_SYM=4;
    public static final int IDENT=5;
    public static final int STRING=6;
    public static final int URI=7;
    public static final int CHARSET_SYM=8;
    public static final int SEMI=9;
    public static final int IMPORT_SYM=10;
    public static final int MEDIA_SYM=11;
    public static final int LBRACE=12;
    public static final int RBRACE=13;
    public static final int COMMA=14;
    public static final int AND=15;
    public static final int ONLY=16;
    public static final int NOT=17;
    public static final int GEN=18;
    public static final int MOZ_DOCUMENT_SYM=19;
    public static final int MOZ_URL_PREFIX=20;
    public static final int MOZ_DOMAIN=21;
    public static final int MOZ_REGEXP=22;
    public static final int PAGE_SYM=23;
    public static final int COUNTER_STYLE_SYM=24;
    public static final int FONT_FACE_SYM=25;
    public static final int TOPLEFTCORNER_SYM=26;
    public static final int TOPLEFT_SYM=27;
    public static final int TOPCENTER_SYM=28;
    public static final int TOPRIGHT_SYM=29;
    public static final int TOPRIGHTCORNER_SYM=30;
    public static final int BOTTOMLEFTCORNER_SYM=31;
    public static final int BOTTOMLEFT_SYM=32;
    public static final int BOTTOMCENTER_SYM=33;
    public static final int BOTTOMRIGHT_SYM=34;
    public static final int BOTTOMRIGHTCORNER_SYM=35;
    public static final int LEFTTOP_SYM=36;
    public static final int LEFTMIDDLE_SYM=37;
    public static final int LEFTBOTTOM_SYM=38;
    public static final int RIGHTTOP_SYM=39;
    public static final int RIGHTMIDDLE_SYM=40;
    public static final int RIGHTBOTTOM_SYM=41;
    public static final int COLON=42;
    public static final int SOLIDUS=43;
    public static final int PLUS=44;
    public static final int GREATER=45;
    public static final int TILDE=46;
    public static final int MINUS=47;
    public static final int HASH=48;
    public static final int DOT=49;
    public static final int LBRACKET=50;
    public static final int DCOLON=51;
    public static final int STAR=52;
    public static final int PIPE=53;
    public static final int NAME=54;
    public static final int OPEQ=55;
    public static final int INCLUDES=56;
    public static final int DASHMATCH=57;
    public static final int BEGINS=58;
    public static final int ENDS=59;
    public static final int CONTAINS=60;
    public static final int RBRACKET=61;
    public static final int LPAREN=62;
    public static final int RPAREN=63;
    public static final int IMPORTANT_SYM=64;
    public static final int NUMBER=65;
    public static final int PERCENTAGE=66;
    public static final int LENGTH=67;
    public static final int EMS=68;
    public static final int EXS=69;
    public static final int ANGLE=70;
    public static final int TIME=71;
    public static final int FREQ=72;
    public static final int RESOLUTION=73;
    public static final int DIMENSION=74;
    public static final int WS=75;
    public static final int NL=76;
    public static final int COMMENT=77;
    public static final int HEXCHAR=78;
    public static final int NONASCII=79;
    public static final int UNICODE=80;
    public static final int ESCAPE=81;
    public static final int NMSTART=82;
    public static final int NMCHAR=83;
    public static final int URL=84;
    public static final int A=85;
    public static final int B=86;
    public static final int C=87;
    public static final int D=88;
    public static final int E=89;
    public static final int F=90;
    public static final int G=91;
    public static final int H=92;
    public static final int I=93;
    public static final int J=94;
    public static final int K=95;
    public static final int L=96;
    public static final int M=97;
    public static final int N=98;
    public static final int O=99;
    public static final int P=100;
    public static final int Q=101;
    public static final int R=102;
    public static final int S=103;
    public static final int T=104;
    public static final int U=105;
    public static final int V=106;
    public static final int W=107;
    public static final int X=108;
    public static final int Y=109;
    public static final int Z=110;
    public static final int CDO=111;
    public static final int CDC=112;
    public static final int INVALID=113;

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

    // $ANTLR start "T__114"
    public final void mT__114() throws RecognitionException {
        try {
            int _type = T__114;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:48:8: ( '#' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:48:10: '#'
            {
            match('#'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__114"

    // $ANTLR start "GEN"
    public final void mGEN() throws RecognitionException {
        try {
            int _type = GEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:25: ( '@@@' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:27: '@@@'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:729:25: ( ( 'a' .. 'f' | 'A' .. 'F' | '0' .. '9' ) )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:729:27: ( 'a' .. 'f' | 'A' .. 'F' | '0' .. '9' )
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:25: ( '\\u0080' .. '\\uFFFF' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:27: '\\u0080' .. '\\uFFFF'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:733:25: ( '\\\\' HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )? ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:733:27: '\\\\' HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )? ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
            {
            match('\\'); if (state.failed) return ;
            mHEXCHAR(); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:734:33: ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( ((LA5_0>='0' && LA5_0<='9')||(LA5_0>='A' && LA5_0<='F')||(LA5_0>='a' && LA5_0<='f')) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:734:34: HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )?
                    {
                    mHEXCHAR(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:37: ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( ((LA4_0>='0' && LA4_0<='9')||(LA4_0>='A' && LA4_0<='F')||(LA4_0>='a' && LA4_0<='f')) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:38: HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )?
                            {
                            mHEXCHAR(); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:41: ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )?
                            int alt3=2;
                            int LA3_0 = input.LA(1);

                            if ( ((LA3_0>='0' && LA3_0<='9')||(LA3_0>='A' && LA3_0<='F')||(LA3_0>='a' && LA3_0<='f')) ) {
                                alt3=1;
                            }
                            switch (alt3) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:42: HEXCHAR ( HEXCHAR ( HEXCHAR )? )?
                                    {
                                    mHEXCHAR(); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:737:45: ( HEXCHAR ( HEXCHAR )? )?
                                    int alt2=2;
                                    int LA2_0 = input.LA(1);

                                    if ( ((LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='F')||(LA2_0>='a' && LA2_0<='f')) ) {
                                        alt2=1;
                                    }
                                    switch (alt2) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:737:46: HEXCHAR ( HEXCHAR )?
                                            {
                                            mHEXCHAR(); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:737:54: ( HEXCHAR )?
                                            int alt1=2;
                                            int LA1_0 = input.LA(1);

                                            if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='F')||(LA1_0>='a' && LA1_0<='f')) ) {
                                                alt1=1;
                                            }
                                            switch (alt1) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:737:54: HEXCHAR
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

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:741:33: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:25: ( UNICODE | '\\\\' ~ ( '\\r' | '\\n' | '\\f' | HEXCHAR ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:27: UNICODE
                    {
                    mUNICODE(); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:37: '\\\\' ~ ( '\\r' | '\\n' | '\\f' | HEXCHAR )
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:25: ( '_' | 'a' .. 'z' | 'A' .. 'Z' | NONASCII | ESCAPE )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:27: '_'
                    {
                    match('_'); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:27: 'a' .. 'z'
                    {
                    matchRange('a','z'); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:747:27: 'A' .. 'Z'
                    {
                    matchRange('A','Z'); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:27: NONASCII
                    {
                    mNONASCII(); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:749:27: ESCAPE
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:25: ( '_' | 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '-' | NONASCII | ESCAPE )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:27: '_'
                    {
                    match('_'); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:27: 'a' .. 'z'
                    {
                    matchRange('a','z'); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:754:27: 'A' .. 'Z'
                    {
                    matchRange('A','Z'); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:755:27: '0' .. '9'
                    {
                    matchRange('0','9'); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:27: '-'
                    {
                    match('-'); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:757:27: NONASCII
                    {
                    mNONASCII(); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:758:27: ESCAPE
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:761:25: ( ( NMCHAR )+ )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:761:27: ( NMCHAR )+
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:761:27: ( NMCHAR )+
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
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:761:27: NMCHAR
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:763:25: ( ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | NMCHAR )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:763:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | NMCHAR )*
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:763:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | NMCHAR )*
            loop11:
            do {
                int alt11=13;
                alt11 = dfa11.predict(input);
                switch (alt11) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:31: '['
            	    {
            	    match('['); if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:35: '!'
            	    {
            	    match('!'); if (state.failed) return ;

            	    }
            	    break;
            	case 3 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:39: '#'
            	    {
            	    match('#'); if (state.failed) return ;

            	    }
            	    break;
            	case 4 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:43: '$'
            	    {
            	    match('$'); if (state.failed) return ;

            	    }
            	    break;
            	case 5 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:47: '%'
            	    {
            	    match('%'); if (state.failed) return ;

            	    }
            	    break;
            	case 6 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:51: '&'
            	    {
            	    match('&'); if (state.failed) return ;

            	    }
            	    break;
            	case 7 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:55: '*'
            	    {
            	    match('*'); if (state.failed) return ;

            	    }
            	    break;
            	case 8 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:59: '~'
            	    {
            	    match('~'); if (state.failed) return ;

            	    }
            	    break;
            	case 9 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:63: '.'
            	    {
            	    match('.'); if (state.failed) return ;

            	    }
            	    break;
            	case 10 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:67: ':'
            	    {
            	    match(':'); if (state.failed) return ;

            	    }
            	    break;
            	case 11 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:71: '/'
            	    {
            	    match('/'); if (state.failed) return ;

            	    }
            	    break;
            	case 12 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:765:31: NMCHAR
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:772:17: ( ( 'a' | 'A' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '1' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:772:21: ( 'a' | 'A' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '1'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0=='0') ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt14=2;
                            int LA14_0 = input.LA(1);

                            if ( (LA14_0=='0') ) {
                                alt14=1;
                            }
                            switch (alt14) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:36: ( '0' ( '0' )? )?
                                    int alt13=2;
                                    int LA13_0 = input.LA(1);

                                    if ( (LA13_0=='0') ) {
                                        alt13=1;
                                    }
                                    switch (alt13) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:41: ( '0' )?
                                            int alt12=2;
                                            int LA12_0 = input.LA(1);

                                            if ( (LA12_0=='0') ) {
                                                alt12=1;
                                            }
                                            switch (alt12) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:41: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:775:17: ( ( 'b' | 'B' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '2' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:775:21: ( 'b' | 'B' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:776:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '2'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:776:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt20=2;
                    int LA20_0 = input.LA(1);

                    if ( (LA20_0=='0') ) {
                        alt20=1;
                    }
                    switch (alt20) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:776:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:776:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt19=2;
                            int LA19_0 = input.LA(1);

                            if ( (LA19_0=='0') ) {
                                alt19=1;
                            }
                            switch (alt19) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:776:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:776:36: ( '0' ( '0' )? )?
                                    int alt18=2;
                                    int LA18_0 = input.LA(1);

                                    if ( (LA18_0=='0') ) {
                                        alt18=1;
                                    }
                                    switch (alt18) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:776:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:776:41: ( '0' )?
                                            int alt17=2;
                                            int LA17_0 = input.LA(1);

                                            if ( (LA17_0=='0') ) {
                                                alt17=1;
                                            }
                                            switch (alt17) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:776:41: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:778:17: ( ( 'c' | 'C' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '3' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:778:21: ( 'c' | 'C' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '3'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt25=2;
                    int LA25_0 = input.LA(1);

                    if ( (LA25_0=='0') ) {
                        alt25=1;
                    }
                    switch (alt25) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt24=2;
                            int LA24_0 = input.LA(1);

                            if ( (LA24_0=='0') ) {
                                alt24=1;
                            }
                            switch (alt24) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:36: ( '0' ( '0' )? )?
                                    int alt23=2;
                                    int LA23_0 = input.LA(1);

                                    if ( (LA23_0=='0') ) {
                                        alt23=1;
                                    }
                                    switch (alt23) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:41: ( '0' )?
                                            int alt22=2;
                                            int LA22_0 = input.LA(1);

                                            if ( (LA22_0=='0') ) {
                                                alt22=1;
                                            }
                                            switch (alt22) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:41: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:17: ( ( 'd' | 'D' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '4' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:21: ( 'd' | 'D' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:782:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '4'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:782:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt30=2;
                    int LA30_0 = input.LA(1);

                    if ( (LA30_0=='0') ) {
                        alt30=1;
                    }
                    switch (alt30) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:782:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:782:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt29=2;
                            int LA29_0 = input.LA(1);

                            if ( (LA29_0=='0') ) {
                                alt29=1;
                            }
                            switch (alt29) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:782:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:782:36: ( '0' ( '0' )? )?
                                    int alt28=2;
                                    int LA28_0 = input.LA(1);

                                    if ( (LA28_0=='0') ) {
                                        alt28=1;
                                    }
                                    switch (alt28) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:782:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:782:41: ( '0' )?
                                            int alt27=2;
                                            int LA27_0 = input.LA(1);

                                            if ( (LA27_0=='0') ) {
                                                alt27=1;
                                            }
                                            switch (alt27) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:782:41: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:784:17: ( ( 'e' | 'E' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '5' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:784:21: ( 'e' | 'E' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '5'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt35=2;
                    int LA35_0 = input.LA(1);

                    if ( (LA35_0=='0') ) {
                        alt35=1;
                    }
                    switch (alt35) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt34=2;
                            int LA34_0 = input.LA(1);

                            if ( (LA34_0=='0') ) {
                                alt34=1;
                            }
                            switch (alt34) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:36: ( '0' ( '0' )? )?
                                    int alt33=2;
                                    int LA33_0 = input.LA(1);

                                    if ( (LA33_0=='0') ) {
                                        alt33=1;
                                    }
                                    switch (alt33) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:41: ( '0' )?
                                            int alt32=2;
                                            int LA32_0 = input.LA(1);

                                            if ( (LA32_0=='0') ) {
                                                alt32=1;
                                            }
                                            switch (alt32) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:41: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:787:17: ( ( 'f' | 'F' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '6' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:787:21: ( 'f' | 'F' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '6'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt40=2;
                    int LA40_0 = input.LA(1);

                    if ( (LA40_0=='0') ) {
                        alt40=1;
                    }
                    switch (alt40) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt39=2;
                            int LA39_0 = input.LA(1);

                            if ( (LA39_0=='0') ) {
                                alt39=1;
                            }
                            switch (alt39) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:36: ( '0' ( '0' )? )?
                                    int alt38=2;
                                    int LA38_0 = input.LA(1);

                                    if ( (LA38_0=='0') ) {
                                        alt38=1;
                                    }
                                    switch (alt38) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:41: ( '0' )?
                                            int alt37=2;
                                            int LA37_0 = input.LA(1);

                                            if ( (LA37_0=='0') ) {
                                                alt37=1;
                                            }
                                            switch (alt37) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:41: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:790:17: ( ( 'g' | 'G' ) | '\\\\' ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:790:21: ( 'g' | 'G' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:21: '\\\\' ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:792:25: ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:793:31: 'g'
                            {
                            match('g'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:794:31: 'G'
                            {
                            match('G'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7'
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt45=2;
                            int LA45_0 = input.LA(1);

                            if ( (LA45_0=='0') ) {
                                alt45=1;
                            }
                            switch (alt45) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt44=2;
                                    int LA44_0 = input.LA(1);

                                    if ( (LA44_0=='0') ) {
                                        alt44=1;
                                    }
                                    switch (alt44) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:41: ( '0' ( '0' )? )?
                                            int alt43=2;
                                            int LA43_0 = input.LA(1);

                                            if ( (LA43_0=='0') ) {
                                                alt43=1;
                                            }
                                            switch (alt43) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:46: ( '0' )?
                                                    int alt42=2;
                                                    int LA42_0 = input.LA(1);

                                                    if ( (LA42_0=='0') ) {
                                                        alt42=1;
                                                    }
                                                    switch (alt42) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:798:17: ( ( 'h' | 'H' ) | '\\\\' ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:798:21: ( 'h' | 'H' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:799:19: '\\\\' ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:25: ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:801:31: 'h'
                            {
                            match('h'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:802:31: 'H'
                            {
                            match('H'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8'
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt51=2;
                            int LA51_0 = input.LA(1);

                            if ( (LA51_0=='0') ) {
                                alt51=1;
                            }
                            switch (alt51) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt50=2;
                                    int LA50_0 = input.LA(1);

                                    if ( (LA50_0=='0') ) {
                                        alt50=1;
                                    }
                                    switch (alt50) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:41: ( '0' ( '0' )? )?
                                            int alt49=2;
                                            int LA49_0 = input.LA(1);

                                            if ( (LA49_0=='0') ) {
                                                alt49=1;
                                            }
                                            switch (alt49) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:46: ( '0' )?
                                                    int alt48=2;
                                                    int LA48_0 = input.LA(1);

                                                    if ( (LA48_0=='0') ) {
                                                        alt48=1;
                                                    }
                                                    switch (alt48) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:806:17: ( ( 'i' | 'I' ) | '\\\\' ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:806:21: ( 'i' | 'I' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:807:19: '\\\\' ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:25: ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:809:31: 'i'
                            {
                            match('i'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:810:31: 'I'
                            {
                            match('I'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9'
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt57=2;
                            int LA57_0 = input.LA(1);

                            if ( (LA57_0=='0') ) {
                                alt57=1;
                            }
                            switch (alt57) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt56=2;
                                    int LA56_0 = input.LA(1);

                                    if ( (LA56_0=='0') ) {
                                        alt56=1;
                                    }
                                    switch (alt56) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:41: ( '0' ( '0' )? )?
                                            int alt55=2;
                                            int LA55_0 = input.LA(1);

                                            if ( (LA55_0=='0') ) {
                                                alt55=1;
                                            }
                                            switch (alt55) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:46: ( '0' )?
                                                    int alt54=2;
                                                    int LA54_0 = input.LA(1);

                                                    if ( (LA54_0=='0') ) {
                                                        alt54=1;
                                                    }
                                                    switch (alt54) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:17: ( ( 'j' | 'J' ) | '\\\\' ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:21: ( 'j' | 'J' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:815:19: '\\\\' ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:816:25: ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:817:31: 'j'
                            {
                            match('j'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:818:31: 'J'
                            {
                            match('J'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:819:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:819:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt63=2;
                            int LA63_0 = input.LA(1);

                            if ( (LA63_0=='0') ) {
                                alt63=1;
                            }
                            switch (alt63) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:819:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:819:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt62=2;
                                    int LA62_0 = input.LA(1);

                                    if ( (LA62_0=='0') ) {
                                        alt62=1;
                                    }
                                    switch (alt62) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:819:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:819:41: ( '0' ( '0' )? )?
                                            int alt61=2;
                                            int LA61_0 = input.LA(1);

                                            if ( (LA61_0=='0') ) {
                                                alt61=1;
                                            }
                                            switch (alt61) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:819:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:819:46: ( '0' )?
                                                    int alt60=2;
                                                    int LA60_0 = input.LA(1);

                                                    if ( (LA60_0=='0') ) {
                                                        alt60=1;
                                                    }
                                                    switch (alt60) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:819:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:822:17: ( ( 'k' | 'K' ) | '\\\\' ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:822:21: ( 'k' | 'K' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:823:19: '\\\\' ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:824:25: ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:825:31: 'k'
                            {
                            match('k'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:826:31: 'K'
                            {
                            match('K'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt69=2;
                            int LA69_0 = input.LA(1);

                            if ( (LA69_0=='0') ) {
                                alt69=1;
                            }
                            switch (alt69) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt68=2;
                                    int LA68_0 = input.LA(1);

                                    if ( (LA68_0=='0') ) {
                                        alt68=1;
                                    }
                                    switch (alt68) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:41: ( '0' ( '0' )? )?
                                            int alt67=2;
                                            int LA67_0 = input.LA(1);

                                            if ( (LA67_0=='0') ) {
                                                alt67=1;
                                            }
                                            switch (alt67) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:46: ( '0' )?
                                                    int alt66=2;
                                                    int LA66_0 = input.LA(1);

                                                    if ( (LA66_0=='0') ) {
                                                        alt66=1;
                                                    }
                                                    switch (alt66) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:830:17: ( ( 'l' | 'L' ) | '\\\\' ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:830:21: ( 'l' | 'L' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:19: '\\\\' ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:832:25: ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:31: 'l'
                            {
                            match('l'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:834:31: 'L'
                            {
                            match('L'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt75=2;
                            int LA75_0 = input.LA(1);

                            if ( (LA75_0=='0') ) {
                                alt75=1;
                            }
                            switch (alt75) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt74=2;
                                    int LA74_0 = input.LA(1);

                                    if ( (LA74_0=='0') ) {
                                        alt74=1;
                                    }
                                    switch (alt74) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:41: ( '0' ( '0' )? )?
                                            int alt73=2;
                                            int LA73_0 = input.LA(1);

                                            if ( (LA73_0=='0') ) {
                                                alt73=1;
                                            }
                                            switch (alt73) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:46: ( '0' )?
                                                    int alt72=2;
                                                    int LA72_0 = input.LA(1);

                                                    if ( (LA72_0=='0') ) {
                                                        alt72=1;
                                                    }
                                                    switch (alt72) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:17: ( ( 'm' | 'M' ) | '\\\\' ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:21: ( 'm' | 'M' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:839:19: '\\\\' ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:840:25: ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:841:31: 'm'
                            {
                            match('m'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:842:31: 'M'
                            {
                            match('M'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:843:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:843:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt81=2;
                            int LA81_0 = input.LA(1);

                            if ( (LA81_0=='0') ) {
                                alt81=1;
                            }
                            switch (alt81) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:843:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:843:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt80=2;
                                    int LA80_0 = input.LA(1);

                                    if ( (LA80_0=='0') ) {
                                        alt80=1;
                                    }
                                    switch (alt80) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:843:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:843:41: ( '0' ( '0' )? )?
                                            int alt79=2;
                                            int LA79_0 = input.LA(1);

                                            if ( (LA79_0=='0') ) {
                                                alt79=1;
                                            }
                                            switch (alt79) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:843:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:843:46: ( '0' )?
                                                    int alt78=2;
                                                    int LA78_0 = input.LA(1);

                                                    if ( (LA78_0=='0') ) {
                                                        alt78=1;
                                                    }
                                                    switch (alt78) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:843:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:846:17: ( ( 'n' | 'N' ) | '\\\\' ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:846:21: ( 'n' | 'N' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:847:19: '\\\\' ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:848:25: ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:31: 'n'
                            {
                            match('n'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:850:31: 'N'
                            {
                            match('N'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt87=2;
                            int LA87_0 = input.LA(1);

                            if ( (LA87_0=='0') ) {
                                alt87=1;
                            }
                            switch (alt87) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt86=2;
                                    int LA86_0 = input.LA(1);

                                    if ( (LA86_0=='0') ) {
                                        alt86=1;
                                    }
                                    switch (alt86) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:41: ( '0' ( '0' )? )?
                                            int alt85=2;
                                            int LA85_0 = input.LA(1);

                                            if ( (LA85_0=='0') ) {
                                                alt85=1;
                                            }
                                            switch (alt85) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:46: ( '0' )?
                                                    int alt84=2;
                                                    int LA84_0 = input.LA(1);

                                                    if ( (LA84_0=='0') ) {
                                                        alt84=1;
                                                    }
                                                    switch (alt84) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:854:17: ( ( 'o' | 'O' ) | '\\\\' ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:854:21: ( 'o' | 'O' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:855:19: '\\\\' ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:856:25: ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:857:31: 'o'
                            {
                            match('o'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:858:31: 'O'
                            {
                            match('O'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt93=2;
                            int LA93_0 = input.LA(1);

                            if ( (LA93_0=='0') ) {
                                alt93=1;
                            }
                            switch (alt93) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt92=2;
                                    int LA92_0 = input.LA(1);

                                    if ( (LA92_0=='0') ) {
                                        alt92=1;
                                    }
                                    switch (alt92) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:41: ( '0' ( '0' )? )?
                                            int alt91=2;
                                            int LA91_0 = input.LA(1);

                                            if ( (LA91_0=='0') ) {
                                                alt91=1;
                                            }
                                            switch (alt91) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:46: ( '0' )?
                                                    int alt90=2;
                                                    int LA90_0 = input.LA(1);

                                                    if ( (LA90_0=='0') ) {
                                                        alt90=1;
                                                    }
                                                    switch (alt90) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:862:17: ( ( 'p' | 'P' ) | '\\\\' ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:862:21: ( 'p' | 'P' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:19: '\\\\' ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:25: ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:31: 'p'
                            {
                            match('p'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:866:31: 'P'
                            {
                            match('P'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt99=2;
                            int LA99_0 = input.LA(1);

                            if ( (LA99_0=='0') ) {
                                alt99=1;
                            }
                            switch (alt99) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt98=2;
                                    int LA98_0 = input.LA(1);

                                    if ( (LA98_0=='0') ) {
                                        alt98=1;
                                    }
                                    switch (alt98) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:41: ( '0' ( '0' )? )?
                                            int alt97=2;
                                            int LA97_0 = input.LA(1);

                                            if ( (LA97_0=='0') ) {
                                                alt97=1;
                                            }
                                            switch (alt97) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:46: ( '0' )?
                                                    int alt96=2;
                                                    int LA96_0 = input.LA(1);

                                                    if ( (LA96_0=='0') ) {
                                                        alt96=1;
                                                    }
                                                    switch (alt96) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:66: ( '0' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:67: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:17: ( ( 'q' | 'Q' ) | '\\\\' ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:21: ( 'q' | 'Q' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:871:19: '\\\\' ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:25: ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:873:31: 'q'
                            {
                            match('q'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:874:31: 'Q'
                            {
                            match('Q'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt105=2;
                            int LA105_0 = input.LA(1);

                            if ( (LA105_0=='0') ) {
                                alt105=1;
                            }
                            switch (alt105) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt104=2;
                                    int LA104_0 = input.LA(1);

                                    if ( (LA104_0=='0') ) {
                                        alt104=1;
                                    }
                                    switch (alt104) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:41: ( '0' ( '0' )? )?
                                            int alt103=2;
                                            int LA103_0 = input.LA(1);

                                            if ( (LA103_0=='0') ) {
                                                alt103=1;
                                            }
                                            switch (alt103) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:46: ( '0' )?
                                                    int alt102=2;
                                                    int LA102_0 = input.LA(1);

                                                    if ( (LA102_0=='0') ) {
                                                        alt102=1;
                                                    }
                                                    switch (alt102) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:66: ( '1' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:67: '1'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:17: ( ( 'r' | 'R' ) | '\\\\' ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:21: ( 'r' | 'R' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:879:19: '\\\\' ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:880:25: ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:881:31: 'r'
                            {
                            match('r'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:31: 'R'
                            {
                            match('R'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt111=2;
                            int LA111_0 = input.LA(1);

                            if ( (LA111_0=='0') ) {
                                alt111=1;
                            }
                            switch (alt111) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt110=2;
                                    int LA110_0 = input.LA(1);

                                    if ( (LA110_0=='0') ) {
                                        alt110=1;
                                    }
                                    switch (alt110) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:41: ( '0' ( '0' )? )?
                                            int alt109=2;
                                            int LA109_0 = input.LA(1);

                                            if ( (LA109_0=='0') ) {
                                                alt109=1;
                                            }
                                            switch (alt109) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:46: ( '0' )?
                                                    int alt108=2;
                                                    int LA108_0 = input.LA(1);

                                                    if ( (LA108_0=='0') ) {
                                                        alt108=1;
                                                    }
                                                    switch (alt108) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:66: ( '2' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:67: '2'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:17: ( ( 's' | 'S' ) | '\\\\' ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:21: ( 's' | 'S' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:887:19: '\\\\' ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:888:25: ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:31: 's'
                            {
                            match('s'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:31: 'S'
                            {
                            match('S'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt117=2;
                            int LA117_0 = input.LA(1);

                            if ( (LA117_0=='0') ) {
                                alt117=1;
                            }
                            switch (alt117) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt116=2;
                                    int LA116_0 = input.LA(1);

                                    if ( (LA116_0=='0') ) {
                                        alt116=1;
                                    }
                                    switch (alt116) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:41: ( '0' ( '0' )? )?
                                            int alt115=2;
                                            int LA115_0 = input.LA(1);

                                            if ( (LA115_0=='0') ) {
                                                alt115=1;
                                            }
                                            switch (alt115) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:46: ( '0' )?
                                                    int alt114=2;
                                                    int LA114_0 = input.LA(1);

                                                    if ( (LA114_0=='0') ) {
                                                        alt114=1;
                                                    }
                                                    switch (alt114) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:66: ( '3' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:67: '3'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:17: ( ( 't' | 'T' ) | '\\\\' ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:21: ( 't' | 'T' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:895:19: '\\\\' ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:25: ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:897:31: 't'
                            {
                            match('t'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:31: 'T'
                            {
                            match('T'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt123=2;
                            int LA123_0 = input.LA(1);

                            if ( (LA123_0=='0') ) {
                                alt123=1;
                            }
                            switch (alt123) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt122=2;
                                    int LA122_0 = input.LA(1);

                                    if ( (LA122_0=='0') ) {
                                        alt122=1;
                                    }
                                    switch (alt122) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:41: ( '0' ( '0' )? )?
                                            int alt121=2;
                                            int LA121_0 = input.LA(1);

                                            if ( (LA121_0=='0') ) {
                                                alt121=1;
                                            }
                                            switch (alt121) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:46: ( '0' )?
                                                    int alt120=2;
                                                    int LA120_0 = input.LA(1);

                                                    if ( (LA120_0=='0') ) {
                                                        alt120=1;
                                                    }
                                                    switch (alt120) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:66: ( '4' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:67: '4'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:17: ( ( 'u' | 'U' ) | '\\\\' ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:21: ( 'u' | 'U' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:903:19: '\\\\' ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:904:25: ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:905:31: 'u'
                            {
                            match('u'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:906:31: 'U'
                            {
                            match('U'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:907:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:907:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt129=2;
                            int LA129_0 = input.LA(1);

                            if ( (LA129_0=='0') ) {
                                alt129=1;
                            }
                            switch (alt129) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:907:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:907:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt128=2;
                                    int LA128_0 = input.LA(1);

                                    if ( (LA128_0=='0') ) {
                                        alt128=1;
                                    }
                                    switch (alt128) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:907:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:907:41: ( '0' ( '0' )? )?
                                            int alt127=2;
                                            int LA127_0 = input.LA(1);

                                            if ( (LA127_0=='0') ) {
                                                alt127=1;
                                            }
                                            switch (alt127) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:907:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:907:46: ( '0' )?
                                                    int alt126=2;
                                                    int LA126_0 = input.LA(1);

                                                    if ( (LA126_0=='0') ) {
                                                        alt126=1;
                                                    }
                                                    switch (alt126) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:907:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:907:66: ( '5' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:907:67: '5'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:910:17: ( ( 'v' | 'V' ) | '\\\\' ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:910:21: ( 'v' | 'V' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:19: '\\\\' ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:912:25: ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:912:31: 'v'
                            {
                            match('v'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:913:31: 'V'
                            {
                            match('V'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt135=2;
                            int LA135_0 = input.LA(1);

                            if ( (LA135_0=='0') ) {
                                alt135=1;
                            }
                            switch (alt135) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt134=2;
                                    int LA134_0 = input.LA(1);

                                    if ( (LA134_0=='0') ) {
                                        alt134=1;
                                    }
                                    switch (alt134) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:41: ( '0' ( '0' )? )?
                                            int alt133=2;
                                            int LA133_0 = input.LA(1);

                                            if ( (LA133_0=='0') ) {
                                                alt133=1;
                                            }
                                            switch (alt133) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:46: ( '0' )?
                                                    int alt132=2;
                                                    int LA132_0 = input.LA(1);

                                                    if ( (LA132_0=='0') ) {
                                                        alt132=1;
                                                    }
                                                    switch (alt132) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:66: ( '6' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:67: '6'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:917:17: ( ( 'w' | 'W' ) | '\\\\' ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:917:21: ( 'w' | 'W' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:918:19: '\\\\' ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:919:25: ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:920:31: 'w'
                            {
                            match('w'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:921:31: 'W'
                            {
                            match('W'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt141=2;
                            int LA141_0 = input.LA(1);

                            if ( (LA141_0=='0') ) {
                                alt141=1;
                            }
                            switch (alt141) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt140=2;
                                    int LA140_0 = input.LA(1);

                                    if ( (LA140_0=='0') ) {
                                        alt140=1;
                                    }
                                    switch (alt140) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:41: ( '0' ( '0' )? )?
                                            int alt139=2;
                                            int LA139_0 = input.LA(1);

                                            if ( (LA139_0=='0') ) {
                                                alt139=1;
                                            }
                                            switch (alt139) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:46: ( '0' )?
                                                    int alt138=2;
                                                    int LA138_0 = input.LA(1);

                                                    if ( (LA138_0=='0') ) {
                                                        alt138=1;
                                                    }
                                                    switch (alt138) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:66: ( '7' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:67: '7'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:925:17: ( ( 'x' | 'X' ) | '\\\\' ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:925:21: ( 'x' | 'X' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:926:19: '\\\\' ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:927:25: ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:928:31: 'x'
                            {
                            match('x'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:929:31: 'X'
                            {
                            match('X'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt147=2;
                            int LA147_0 = input.LA(1);

                            if ( (LA147_0=='0') ) {
                                alt147=1;
                            }
                            switch (alt147) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt146=2;
                                    int LA146_0 = input.LA(1);

                                    if ( (LA146_0=='0') ) {
                                        alt146=1;
                                    }
                                    switch (alt146) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:41: ( '0' ( '0' )? )?
                                            int alt145=2;
                                            int LA145_0 = input.LA(1);

                                            if ( (LA145_0=='0') ) {
                                                alt145=1;
                                            }
                                            switch (alt145) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:46: ( '0' )?
                                                    int alt144=2;
                                                    int LA144_0 = input.LA(1);

                                                    if ( (LA144_0=='0') ) {
                                                        alt144=1;
                                                    }
                                                    switch (alt144) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:66: ( '8' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:67: '8'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:933:17: ( ( 'y' | 'Y' ) | '\\\\' ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:933:21: ( 'y' | 'Y' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:934:19: '\\\\' ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:935:25: ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:936:31: 'y'
                            {
                            match('y'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:937:31: 'Y'
                            {
                            match('Y'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt153=2;
                            int LA153_0 = input.LA(1);

                            if ( (LA153_0=='0') ) {
                                alt153=1;
                            }
                            switch (alt153) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt152=2;
                                    int LA152_0 = input.LA(1);

                                    if ( (LA152_0=='0') ) {
                                        alt152=1;
                                    }
                                    switch (alt152) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:41: ( '0' ( '0' )? )?
                                            int alt151=2;
                                            int LA151_0 = input.LA(1);

                                            if ( (LA151_0=='0') ) {
                                                alt151=1;
                                            }
                                            switch (alt151) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:46: ( '0' )?
                                                    int alt150=2;
                                                    int LA150_0 = input.LA(1);

                                                    if ( (LA150_0=='0') ) {
                                                        alt150=1;
                                                    }
                                                    switch (alt150) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:66: ( '9' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:67: '9'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:941:17: ( ( 'z' | 'Z' ) | '\\\\' ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:941:21: ( 'z' | 'Z' )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:942:19: '\\\\' ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:943:25: ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) )
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:944:31: 'z'
                            {
                            match('z'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:945:31: 'Z'
                            {
                            match('Z'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt159=2;
                            int LA159_0 = input.LA(1);

                            if ( (LA159_0=='0') ) {
                                alt159=1;
                            }
                            switch (alt159) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt158=2;
                                    int LA158_0 = input.LA(1);

                                    if ( (LA158_0=='0') ) {
                                        alt158=1;
                                    }
                                    switch (alt158) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:41: ( '0' ( '0' )? )?
                                            int alt157=2;
                                            int LA157_0 = input.LA(1);

                                            if ( (LA157_0=='0') ) {
                                                alt157=1;
                                            }
                                            switch (alt157) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:46: ( '0' )?
                                                    int alt156=2;
                                                    int LA156_0 = input.LA(1);

                                                    if ( (LA156_0=='0') ) {
                                                        alt156=1;
                                                    }
                                                    switch (alt156) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:958:17: ( '<!--' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:958:19: '<!--'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:971:17: ( '-->' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:971:19: '-->'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:978:17: ( '~=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:978:19: '~='
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:979:17: ( '|=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:979:19: '|='
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:980:17: ( '^=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:980:19: '^='
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:17: ( '$=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:19: '$='
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:982:17: ( '*=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:982:19: '*='
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:984:17: ( '>' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:984:19: '>'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:17: ( '{' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:19: '{'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:986:17: ( '}' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:986:19: '}'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:987:17: ( '[' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:987:19: '['
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:988:17: ( ']' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:988:19: ']'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:17: ( '=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:19: '='
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:990:17: ( ';' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:990:19: ';'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:991:17: ( ':' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:991:19: ':'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:17: ( '::' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:19: '::'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:17: ( '/' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:19: '/'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:994:17: ( '-' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:994:19: '-'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:17: ( '+' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:19: '+'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:996:17: ( '*' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:996:19: '*'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:17: ( '(' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:19: '('
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:998:17: ( ')' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:998:19: ')'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:999:17: ( ',' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:999:19: ','
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1000:17: ( '.' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1000:19: '.'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:8: ( '~' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:10: '~'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:17: ( '|' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:19: '|'
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

    // $ANTLR start "INVALID"
    public final void mINVALID() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1007:21: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1007:22: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1008:17: ( '\\'' (~ ( '\\n' | '\\r' | '\\f' | '\\'' ) )* ( '\\'' | ) | '\"' (~ ( '\\n' | '\\r' | '\\f' | '\"' ) )* ( '\"' | ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1008:19: '\\'' (~ ( '\\n' | '\\r' | '\\f' | '\\'' ) )* ( '\\'' | )
                    {
                    match('\''); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1008:24: (~ ( '\\n' | '\\r' | '\\f' | '\\'' ) )*
                    loop162:
                    do {
                        int alt162=2;
                        int LA162_0 = input.LA(1);

                        if ( ((LA162_0>='\u0000' && LA162_0<='\t')||LA162_0=='\u000B'||(LA162_0>='\u000E' && LA162_0<='&')||(LA162_0>='(' && LA162_0<='\uFFFF')) ) {
                            alt162=1;
                        }


                        switch (alt162) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1008:26: ~ ( '\\n' | '\\r' | '\\f' | '\\'' )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1009:21: ( '\\'' | )
                    int alt163=2;
                    int LA163_0 = input.LA(1);

                    if ( (LA163_0=='\'') ) {
                        alt163=1;
                    }
                    else {
                        alt163=2;}
                    switch (alt163) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1010:27: '\\''
                            {
                            match('\''); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1011:27: 
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1014:19: '\"' (~ ( '\\n' | '\\r' | '\\f' | '\"' ) )* ( '\"' | )
                    {
                    match('\"'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1014:23: (~ ( '\\n' | '\\r' | '\\f' | '\"' ) )*
                    loop164:
                    do {
                        int alt164=2;
                        int LA164_0 = input.LA(1);

                        if ( ((LA164_0>='\u0000' && LA164_0<='\t')||LA164_0=='\u000B'||(LA164_0>='\u000E' && LA164_0<='!')||(LA164_0>='#' && LA164_0<='\uFFFF')) ) {
                            alt164=1;
                        }


                        switch (alt164) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1014:25: ~ ( '\\n' | '\\r' | '\\f' | '\"' )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1015:21: ( '\"' | )
                    int alt165=2;
                    int LA165_0 = input.LA(1);

                    if ( (LA165_0=='\"') ) {
                        alt165=1;
                    }
                    else {
                        alt165=2;}
                    switch (alt165) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:27: '\"'
                            {
                            match('\"'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1017:27: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1022:8: ( O N L Y )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1022:10: O N L Y
            {
            mO(); if (state.failed) return ;
            mN(); if (state.failed) return ;
            mL(); if (state.failed) return ;
            mY(); if (state.failed) return ;

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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1023:6: ( N O T )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1023:8: N O T
            {
            mN(); if (state.failed) return ;
            mO(); if (state.failed) return ;
            mT(); if (state.failed) return ;

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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:6: ( A N D )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:8: A N D
            {
            mA(); if (state.failed) return ;
            mN(); if (state.failed) return ;
            mD(); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "IDENT"
    public final void mIDENT() throws RecognitionException {
        try {
            int _type = IDENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1029:17: ( ( '-' )? NMSTART ( NMCHAR )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1029:19: ( '-' )? NMSTART ( NMCHAR )*
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1029:19: ( '-' )?
            int alt167=2;
            int LA167_0 = input.LA(1);

            if ( (LA167_0=='-') ) {
                alt167=1;
            }
            switch (alt167) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1029:19: '-'
                    {
                    match('-'); if (state.failed) return ;

                    }
                    break;

            }

            mNMSTART(); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1029:32: ( NMCHAR )*
            loop168:
            do {
                int alt168=2;
                int LA168_0 = input.LA(1);

                if ( (LA168_0=='-'||(LA168_0>='0' && LA168_0<='9')||(LA168_0>='A' && LA168_0<='Z')||LA168_0=='\\'||LA168_0=='_'||(LA168_0>='a' && LA168_0<='z')||(LA168_0>='\u0080' && LA168_0<='\uFFFF')) ) {
                    alt168=1;
                }


                switch (alt168) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1029:32: NMCHAR
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

    // $ANTLR start "HASH"
    public final void mHASH() throws RecognitionException {
        try {
            int _type = HASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:17: ( '#' NAME )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:19: '#' NAME
            {
            match('#'); if (state.failed) return ;
            mNAME(); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HASH"

    // $ANTLR start "IMPORT_SYM"
    public final void mIMPORT_SYM() throws RecognitionException {
        try {
            int _type = IMPORT_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1036:21: ( '@' I M P O R T )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1036:23: '@' I M P O R T
            {
            match('@'); if (state.failed) return ;
            mI(); if (state.failed) return ;
            mM(); if (state.failed) return ;
            mP(); if (state.failed) return ;
            mO(); if (state.failed) return ;
            mR(); if (state.failed) return ;
            mT(); if (state.failed) return ;

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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1037:21: ( '@' P A G E )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1037:23: '@' P A G E
            {
            match('@'); if (state.failed) return ;
            mP(); if (state.failed) return ;
            mA(); if (state.failed) return ;
            mG(); if (state.failed) return ;
            mE(); if (state.failed) return ;

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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1038:21: ( '@' M E D I A )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1038:23: '@' M E D I A
            {
            match('@'); if (state.failed) return ;
            mM(); if (state.failed) return ;
            mE(); if (state.failed) return ;
            mD(); if (state.failed) return ;
            mI(); if (state.failed) return ;
            mA(); if (state.failed) return ;

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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1039:21: ( '@' N A M E S P A C E )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1039:23: '@' N A M E S P A C E
            {
            match('@'); if (state.failed) return ;
            mN(); if (state.failed) return ;
            mA(); if (state.failed) return ;
            mM(); if (state.failed) return ;
            mE(); if (state.failed) return ;
            mS(); if (state.failed) return ;
            mP(); if (state.failed) return ;
            mA(); if (state.failed) return ;
            mC(); if (state.failed) return ;
            mE(); if (state.failed) return ;

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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1040:21: ( '@charset' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1040:23: '@charset'
            {
            match("@charset"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1041:21: ( '@counter-style' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1041:23: '@counter-style'
            {
            match("@counter-style"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1042:21: ( '@font-face' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1042:23: '@font-face'
            {
            match("@font-face"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FONT_FACE_SYM"

    // $ANTLR start "IMPORTANT_SYM"
    public final void mIMPORTANT_SYM() throws RecognitionException {
        try {
            int _type = IMPORTANT_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:17: ( '!' ( WS )* I M P O R T A N T )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:19: '!' ( WS )* I M P O R T A N T
            {
            match('!'); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:23: ( WS )*
            loop169:
            do {
                int alt169=2;
                int LA169_0 = input.LA(1);

                if ( (LA169_0=='\t'||LA169_0==' ') ) {
                    alt169=1;
                }


                switch (alt169) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:23: WS
            	    {
            	    mWS(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop169;
                }
            } while (true);

            mI(); if (state.failed) return ;
            mM(); if (state.failed) return ;
            mP(); if (state.failed) return ;
            mO(); if (state.failed) return ;
            mR(); if (state.failed) return ;
            mT(); if (state.failed) return ;
            mA(); if (state.failed) return ;
            mN(); if (state.failed) return ;
            mT(); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IMPORTANT_SYM"

    // $ANTLR start "TOPLEFTCORNER_SYM"
    public final void mTOPLEFTCORNER_SYM() throws RecognitionException {
        try {
            int _type = TOPLEFTCORNER_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1047:23: ( '@top-left-corner' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1047:24: '@top-left-corner'
            {
            match("@top-left-corner"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1048:23: ( '@top-left' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1048:24: '@top-left'
            {
            match("@top-left"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1049:23: ( '@top-center' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1049:24: '@top-center'
            {
            match("@top-center"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1050:23: ( '@top-right' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1050:24: '@top-right'
            {
            match("@top-right"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1051:23: ( '@top-right-corner' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1051:24: '@top-right-corner'
            {
            match("@top-right-corner"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1052:23: ( '@bottom-left-corner' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1052:24: '@bottom-left-corner'
            {
            match("@bottom-left-corner"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1053:23: ( '@bottom-left' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1053:24: '@bottom-left'
            {
            match("@bottom-left"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1054:23: ( '@bottom-center' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1054:24: '@bottom-center'
            {
            match("@bottom-center"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1055:23: ( '@bottom-right' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1055:24: '@bottom-right'
            {
            match("@bottom-right"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1056:23: ( '@bottom-right-corner' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1056:24: '@bottom-right-corner'
            {
            match("@bottom-right-corner"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1057:23: ( '@left-top' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1057:24: '@left-top'
            {
            match("@left-top"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1058:23: ( '@left-middle' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1058:24: '@left-middle'
            {
            match("@left-middle"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1059:23: ( '@left-bottom' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1059:24: '@left-bottom'
            {
            match("@left-bottom"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1060:23: ( '@right-top' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1060:24: '@right-top'
            {
            match("@right-top"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:23: ( '@right-middle' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:24: '@right-middle'
            {
            match("@right-middle"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1062:23: ( '@right-bottom' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1062:24: '@right-bottom'
            {
            match("@right-bottom"); if (state.failed) return ;


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1064:23: ( '@-moz-document' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1064:25: '@-moz-document'
            {
            match("@-moz-document"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MOZ_DOCUMENT_SYM"

    // $ANTLR start "EMS"
    public final void mEMS() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1107:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1107:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1108:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1108:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1109:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1109:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "LENGTH"

    // $ANTLR start "ANGLE"
    public final void mANGLE() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1110:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1110:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1111:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1111:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1112:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1112:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1113:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1113:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1114:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1114:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1115:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1115:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1118:5: ( ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ ) ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R A D )=> R A D | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | ) )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1118:9: ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ ) ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R A D )=> R A D | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | )
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1118:9: ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ )
            int alt174=2;
            int LA174_0 = input.LA(1);

            if ( ((LA174_0>='0' && LA174_0<='9')) ) {
                alt174=1;
            }
            else if ( (LA174_0=='.') ) {
                alt174=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 174, 0, input);

                throw nvae;
            }
            switch (alt174) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1119:15: ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )?
                    {
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1119:15: ( '0' .. '9' )+
                    int cnt170=0;
                    loop170:
                    do {
                        int alt170=2;
                        int LA170_0 = input.LA(1);

                        if ( ((LA170_0>='0' && LA170_0<='9')) ) {
                            alt170=1;
                        }


                        switch (alt170) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1119:15: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); if (state.failed) return ;

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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1119:25: ( '.' ( '0' .. '9' )+ )?
                    int alt172=2;
                    int LA172_0 = input.LA(1);

                    if ( (LA172_0=='.') ) {
                        alt172=1;
                    }
                    switch (alt172) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1119:26: '.' ( '0' .. '9' )+
                            {
                            match('.'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1119:30: ( '0' .. '9' )+
                            int cnt171=0;
                            loop171:
                            do {
                                int alt171=2;
                                int LA171_0 = input.LA(1);

                                if ( ((LA171_0>='0' && LA171_0<='9')) ) {
                                    alt171=1;
                                }


                                switch (alt171) {
                            	case 1 :
                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1119:30: '0' .. '9'
                            	    {
                            	    matchRange('0','9'); if (state.failed) return ;

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
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1120:15: '.' ( '0' .. '9' )+
                    {
                    match('.'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1120:19: ( '0' .. '9' )+
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
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1120:19: '0' .. '9'
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


                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1122:9: ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R A D )=> R A D | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | )
            int alt180=13;
            alt180 = dfa180.predict(input);
            switch (alt180) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1123:15: ( D P ( I | C ) )=> D P ( I | C M )
                    {
                    mD(); if (state.failed) return ;
                    mP(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1125:17: ( I | C M )
                    int alt175=2;
                    switch ( input.LA(1) ) {
                    case 'I':
                    case 'i':
                        {
                        alt175=1;
                        }
                        break;
                    case '\\':
                        {
                        switch ( input.LA(2) ) {
                        case 'I':
                        case 'i':
                            {
                            alt175=1;
                            }
                            break;
                        case '0':
                            {
                            int LA175_4 = input.LA(3);

                            if ( (LA175_4=='0') ) {
                                int LA175_6 = input.LA(4);

                                if ( (LA175_6=='0') ) {
                                    int LA175_7 = input.LA(5);

                                    if ( (LA175_7=='0') ) {
                                        int LA175_8 = input.LA(6);

                                        if ( (LA175_8=='4'||LA175_8=='6') ) {
                                            int LA175_5 = input.LA(7);

                                            if ( (LA175_5=='9') ) {
                                                alt175=1;
                                            }
                                            else if ( (LA175_5=='3') ) {
                                                alt175=2;
                                            }
                                            else {
                                                if (state.backtracking>0) {state.failed=true; return ;}
                                                NoViableAltException nvae =
                                                    new NoViableAltException("", 175, 5, input);

                                                throw nvae;
                                            }
                                        }
                                        else {
                                            if (state.backtracking>0) {state.failed=true; return ;}
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 175, 8, input);

                                            throw nvae;
                                        }
                                    }
                                    else if ( (LA175_7=='4'||LA175_7=='6') ) {
                                        int LA175_5 = input.LA(6);

                                        if ( (LA175_5=='9') ) {
                                            alt175=1;
                                        }
                                        else if ( (LA175_5=='3') ) {
                                            alt175=2;
                                        }
                                        else {
                                            if (state.backtracking>0) {state.failed=true; return ;}
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 175, 5, input);

                                            throw nvae;
                                        }
                                    }
                                    else {
                                        if (state.backtracking>0) {state.failed=true; return ;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 175, 7, input);

                                        throw nvae;
                                    }
                                }
                                else if ( (LA175_6=='4'||LA175_6=='6') ) {
                                    int LA175_5 = input.LA(5);

                                    if ( (LA175_5=='9') ) {
                                        alt175=1;
                                    }
                                    else if ( (LA175_5=='3') ) {
                                        alt175=2;
                                    }
                                    else {
                                        if (state.backtracking>0) {state.failed=true; return ;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 175, 5, input);

                                        throw nvae;
                                    }
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 175, 6, input);

                                    throw nvae;
                                }
                            }
                            else if ( (LA175_4=='4'||LA175_4=='6') ) {
                                int LA175_5 = input.LA(4);

                                if ( (LA175_5=='9') ) {
                                    alt175=1;
                                }
                                else if ( (LA175_5=='3') ) {
                                    alt175=2;
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 175, 5, input);

                                    throw nvae;
                                }
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 175, 4, input);

                                throw nvae;
                            }
                            }
                            break;
                        case '4':
                        case '6':
                            {
                            int LA175_5 = input.LA(3);

                            if ( (LA175_5=='9') ) {
                                alt175=1;
                            }
                            else if ( (LA175_5=='3') ) {
                                alt175=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 175, 5, input);

                                throw nvae;
                            }
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 175, 2, input);

                            throw nvae;
                        }

                        }
                        break;
                    case 'C':
                    case 'c':
                        {
                        alt175=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 175, 0, input);

                        throw nvae;
                    }

                    switch (alt175) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1126:22: I
                            {
                            mI(); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1126:26: C M
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1130:15: ( E ( M | X ) )=> E ( M | X )
                    {
                    mE(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1132:17: ( M | X )
                    int alt176=2;
                    switch ( input.LA(1) ) {
                    case 'M':
                    case 'm':
                        {
                        alt176=1;
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
                            alt176=1;
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
                                        int LA176_7 = input.LA(6);

                                        if ( (LA176_7=='4'||LA176_7=='6') ) {
                                            alt176=1;
                                        }
                                        else if ( (LA176_7=='5'||LA176_7=='7') ) {
                                            alt176=2;
                                        }
                                        else {
                                            if (state.backtracking>0) {state.failed=true; return ;}
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 176, 7, input);

                                            throw nvae;
                                        }
                                        }
                                        break;
                                    case '4':
                                    case '6':
                                        {
                                        alt176=1;
                                        }
                                        break;
                                    case '5':
                                    case '7':
                                        {
                                        alt176=2;
                                        }
                                        break;
                                    default:
                                        if (state.backtracking>0) {state.failed=true; return ;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 176, 6, input);

                                        throw nvae;
                                    }

                                    }
                                    break;
                                case '4':
                                case '6':
                                    {
                                    alt176=1;
                                    }
                                    break;
                                case '5':
                                case '7':
                                    {
                                    alt176=2;
                                    }
                                    break;
                                default:
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 176, 5, input);

                                    throw nvae;
                                }

                                }
                                break;
                            case '4':
                            case '6':
                                {
                                alt176=1;
                                }
                                break;
                            case '5':
                            case '7':
                                {
                                alt176=2;
                                }
                                break;
                            default:
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 176, 4, input);

                                throw nvae;
                            }

                            }
                            break;
                        case '5':
                        case '7':
                        case 'X':
                        case 'x':
                            {
                            alt176=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 176, 2, input);

                            throw nvae;
                        }

                        }
                        break;
                    case 'X':
                    case 'x':
                        {
                        alt176=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 176, 0, input);

                        throw nvae;
                    }

                    switch (alt176) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1133:23: M
                            {
                            mM(); if (state.failed) return ;
                            if ( state.backtracking==0 ) {
                               _type = EMS;          
                            }

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1134:23: X
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1136:15: ( P ( X | T | C ) )=> P ( X | T | C )
                    {
                    mP(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1138:17: ( X | T | C )
                    int alt177=3;
                    alt177 = dfa177.predict(input);
                    switch (alt177) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1139:23: X
                            {
                            mX(); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1140:23: T
                            {
                            mT(); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1141:23: C
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1144:15: ( C M )=> C M
                    {
                    mC(); if (state.failed) return ;
                    mM(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = LENGTH;       
                    }

                    }
                    break;
                case 5 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1146:15: ( M ( M | S ) )=> M ( M | S )
                    {
                    mM(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1148:17: ( M | S )
                    int alt178=2;
                    switch ( input.LA(1) ) {
                    case 'M':
                    case 'm':
                        {
                        alt178=1;
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
                            alt178=1;
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
                                        int LA178_7 = input.LA(6);

                                        if ( (LA178_7=='4'||LA178_7=='6') ) {
                                            alt178=1;
                                        }
                                        else if ( (LA178_7=='5'||LA178_7=='7') ) {
                                            alt178=2;
                                        }
                                        else {
                                            if (state.backtracking>0) {state.failed=true; return ;}
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 178, 7, input);

                                            throw nvae;
                                        }
                                        }
                                        break;
                                    case '4':
                                    case '6':
                                        {
                                        alt178=1;
                                        }
                                        break;
                                    case '5':
                                    case '7':
                                        {
                                        alt178=2;
                                        }
                                        break;
                                    default:
                                        if (state.backtracking>0) {state.failed=true; return ;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 178, 6, input);

                                        throw nvae;
                                    }

                                    }
                                    break;
                                case '4':
                                case '6':
                                    {
                                    alt178=1;
                                    }
                                    break;
                                case '5':
                                case '7':
                                    {
                                    alt178=2;
                                    }
                                    break;
                                default:
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 178, 5, input);

                                    throw nvae;
                                }

                                }
                                break;
                            case '4':
                            case '6':
                                {
                                alt178=1;
                                }
                                break;
                            case '5':
                            case '7':
                                {
                                alt178=2;
                                }
                                break;
                            default:
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 178, 4, input);

                                throw nvae;
                            }

                            }
                            break;
                        case '5':
                        case '7':
                        case 'S':
                        case 's':
                            {
                            alt178=2;
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
                    case 'S':
                    case 's':
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
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1149:23: M
                            {
                            mM(); if (state.failed) return ;
                            if ( state.backtracking==0 ) {
                               _type = LENGTH;       
                            }

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1151:23: S
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1153:15: ( I N )=> I N
                    {
                    mI(); if (state.failed) return ;
                    mN(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = LENGTH;       
                    }

                    }
                    break;
                case 7 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1156:15: ( D E G )=> D E G
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1158:15: ( R A D )=> R A D
                    {
                    mR(); if (state.failed) return ;
                    mA(); if (state.failed) return ;
                    mD(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = ANGLE;        
                    }

                    }
                    break;
                case 9 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1161:15: ( S )=> S
                    {
                    mS(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = TIME;         
                    }

                    }
                    break;
                case 10 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1163:15: ( ( K )? H Z )=> ( K )? H Z
                    {
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1164:17: ( K )?
                    int alt179=2;
                    int LA179_0 = input.LA(1);

                    if ( (LA179_0=='K'||LA179_0=='k') ) {
                        alt179=1;
                    }
                    else if ( (LA179_0=='\\') ) {
                        switch ( input.LA(2) ) {
                            case 'K':
                            case 'k':
                                {
                                alt179=1;
                                }
                                break;
                            case '0':
                                {
                                int LA179_4 = input.LA(3);

                                if ( (LA179_4=='0') ) {
                                    int LA179_6 = input.LA(4);

                                    if ( (LA179_6=='0') ) {
                                        int LA179_7 = input.LA(5);

                                        if ( (LA179_7=='0') ) {
                                            int LA179_8 = input.LA(6);

                                            if ( (LA179_8=='4'||LA179_8=='6') ) {
                                                int LA179_5 = input.LA(7);

                                                if ( (LA179_5=='B'||LA179_5=='b') ) {
                                                    alt179=1;
                                                }
                                            }
                                        }
                                        else if ( (LA179_7=='4'||LA179_7=='6') ) {
                                            int LA179_5 = input.LA(6);

                                            if ( (LA179_5=='B'||LA179_5=='b') ) {
                                                alt179=1;
                                            }
                                        }
                                    }
                                    else if ( (LA179_6=='4'||LA179_6=='6') ) {
                                        int LA179_5 = input.LA(5);

                                        if ( (LA179_5=='B'||LA179_5=='b') ) {
                                            alt179=1;
                                        }
                                    }
                                }
                                else if ( (LA179_4=='4'||LA179_4=='6') ) {
                                    int LA179_5 = input.LA(4);

                                    if ( (LA179_5=='B'||LA179_5=='b') ) {
                                        alt179=1;
                                    }
                                }
                                }
                                break;
                            case '4':
                            case '6':
                                {
                                int LA179_5 = input.LA(3);

                                if ( (LA179_5=='B'||LA179_5=='b') ) {
                                    alt179=1;
                                }
                                }
                                break;
                        }

                    }
                    switch (alt179) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1164:17: K
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1166:15: IDENT
                    {
                    mIDENT(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = DIMENSION;    
                    }

                    }
                    break;
                case 12 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1168:15: '%'
                    {
                    match('%'); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = PERCENTAGE;   
                    }

                    }
                    break;
                case 13 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1171:9: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1177:5: ( U R L '(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1177:9: U R L '(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
            {
            mU(); if (state.failed) return ;
            mR(); if (state.failed) return ;
            mL(); if (state.failed) return ;
            match('('); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1179:13: ( ( WS )=> WS )?
            int alt181=2;
            int LA181_0 = input.LA(1);

            if ( (LA181_0=='\t'||LA181_0==' ') ) {
                int LA181_1 = input.LA(2);

                if ( (synpred11_Css3()) ) {
                    alt181=1;
                }
            }
            switch (alt181) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1179:14: ( WS )=> WS
                    {
                    mWS(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1179:25: ( URL | STRING )
            int alt182=2;
            int LA182_0 = input.LA(1);

            if ( (LA182_0=='\t'||(LA182_0>=' ' && LA182_0<='!')||(LA182_0>='#' && LA182_0<='&')||(LA182_0>=')' && LA182_0<='*')||(LA182_0>='-' && LA182_0<=':')||(LA182_0>='A' && LA182_0<='\\')||LA182_0=='_'||(LA182_0>='a' && LA182_0<='z')||LA182_0=='~'||(LA182_0>='\u0080' && LA182_0<='\uFFFF')) ) {
                alt182=1;
            }
            else if ( (LA182_0=='\"'||LA182_0=='\'') ) {
                alt182=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 182, 0, input);

                throw nvae;
            }
            switch (alt182) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1179:26: URL
                    {
                    mURL(); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1179:30: STRING
                    {
                    mSTRING(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1179:38: ( WS )?
            int alt183=2;
            int LA183_0 = input.LA(1);

            if ( (LA183_0=='\t'||LA183_0==' ') ) {
                alt183=1;
            }
            switch (alt183) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1179:38: WS
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1184:2: ( 'url-prefix(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1185:2: 'url-prefix(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
            {
            match("url-prefix("); if (state.failed) return ;

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1186:13: ( ( WS )=> WS )?
            int alt184=2;
            int LA184_0 = input.LA(1);

            if ( (LA184_0=='\t'||LA184_0==' ') ) {
                int LA184_1 = input.LA(2);

                if ( (synpred12_Css3()) ) {
                    alt184=1;
                }
            }
            switch (alt184) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1186:14: ( WS )=> WS
                    {
                    mWS(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1186:25: ( URL | STRING )
            int alt185=2;
            int LA185_0 = input.LA(1);

            if ( (LA185_0=='\t'||(LA185_0>=' ' && LA185_0<='!')||(LA185_0>='#' && LA185_0<='&')||(LA185_0>=')' && LA185_0<='*')||(LA185_0>='-' && LA185_0<=':')||(LA185_0>='A' && LA185_0<='\\')||LA185_0=='_'||(LA185_0>='a' && LA185_0<='z')||LA185_0=='~'||(LA185_0>='\u0080' && LA185_0<='\uFFFF')) ) {
                alt185=1;
            }
            else if ( (LA185_0=='\"'||LA185_0=='\'') ) {
                alt185=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 185, 0, input);

                throw nvae;
            }
            switch (alt185) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1186:26: URL
                    {
                    mURL(); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1186:30: STRING
                    {
                    mSTRING(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1186:38: ( WS )?
            int alt186=2;
            int LA186_0 = input.LA(1);

            if ( (LA186_0=='\t'||LA186_0==' ') ) {
                alt186=1;
            }
            switch (alt186) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1186:38: WS
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1192:2: ( 'domain(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1193:2: 'domain(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
            {
            match("domain("); if (state.failed) return ;

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1194:13: ( ( WS )=> WS )?
            int alt187=2;
            int LA187_0 = input.LA(1);

            if ( (LA187_0=='\t'||LA187_0==' ') ) {
                int LA187_1 = input.LA(2);

                if ( (synpred13_Css3()) ) {
                    alt187=1;
                }
            }
            switch (alt187) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1194:14: ( WS )=> WS
                    {
                    mWS(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1194:25: ( URL | STRING )
            int alt188=2;
            int LA188_0 = input.LA(1);

            if ( (LA188_0=='\t'||(LA188_0>=' ' && LA188_0<='!')||(LA188_0>='#' && LA188_0<='&')||(LA188_0>=')' && LA188_0<='*')||(LA188_0>='-' && LA188_0<=':')||(LA188_0>='A' && LA188_0<='\\')||LA188_0=='_'||(LA188_0>='a' && LA188_0<='z')||LA188_0=='~'||(LA188_0>='\u0080' && LA188_0<='\uFFFF')) ) {
                alt188=1;
            }
            else if ( (LA188_0=='\"'||LA188_0=='\'') ) {
                alt188=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 188, 0, input);

                throw nvae;
            }
            switch (alt188) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1194:26: URL
                    {
                    mURL(); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1194:30: STRING
                    {
                    mSTRING(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1194:38: ( WS )?
            int alt189=2;
            int LA189_0 = input.LA(1);

            if ( (LA189_0=='\t'||LA189_0==' ') ) {
                alt189=1;
            }
            switch (alt189) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1194:38: WS
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1200:2: ( 'regexp(' ( ( WS )=> WS )? STRING ( WS )? ')' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1201:2: 'regexp(' ( ( WS )=> WS )? STRING ( WS )? ')'
            {
            match("regexp("); if (state.failed) return ;

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1202:13: ( ( WS )=> WS )?
            int alt190=2;
            int LA190_0 = input.LA(1);

            if ( (LA190_0=='\t'||LA190_0==' ') && (synpred14_Css3())) {
                alt190=1;
            }
            switch (alt190) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1202:14: ( WS )=> WS
                    {
                    mWS(); if (state.failed) return ;

                    }
                    break;

            }

            mSTRING(); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1202:32: ( WS )?
            int alt191=2;
            int LA191_0 = input.LA(1);

            if ( (LA191_0=='\t'||LA191_0==' ') ) {
                alt191=1;
            }
            switch (alt191) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1202:32: WS
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1214:9: ( ( ' ' | '\\t' )+ )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1214:11: ( ' ' | '\\t' )+
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1214:11: ( ' ' | '\\t' )+
            int cnt192=0;
            loop192:
            do {
                int alt192=2;
                int LA192_0 = input.LA(1);

                if ( (LA192_0=='\t'||LA192_0==' ') ) {
                    alt192=1;
                }


                switch (alt192) {
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
            	    if ( cnt192 >= 1 ) break loop192;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(192, input);
                        throw eee;
                }
                cnt192++;
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1216:9: ( ( '\\r' ( '\\n' )? | '\\n' ) )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1216:11: ( '\\r' ( '\\n' )? | '\\n' )
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1216:11: ( '\\r' ( '\\n' )? | '\\n' )
            int alt194=2;
            int LA194_0 = input.LA(1);

            if ( (LA194_0=='\r') ) {
                alt194=1;
            }
            else if ( (LA194_0=='\n') ) {
                alt194=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 194, 0, input);

                throw nvae;
            }
            switch (alt194) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1216:12: '\\r' ( '\\n' )?
                    {
                    match('\r'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1216:17: ( '\\n' )?
                    int alt193=2;
                    int LA193_0 = input.LA(1);

                    if ( (LA193_0=='\n') ) {
                        alt193=1;
                    }
                    switch (alt193) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1216:17: '\\n'
                            {
                            match('\n'); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1216:25: '\\n'
                    {
                    match('\n'); if (state.failed) return ;

                    }
                    break;

            }

            if ( state.backtracking==0 ) {
               
              	//_channel = HIDDEN;    

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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1226:17: ( '/*' ( options {greedy=false; } : ( . )* ) '*/' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1226:19: '/*' ( options {greedy=false; } : ( . )* ) '*/'
            {
            match("/*"); if (state.failed) return ;

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1226:24: ( options {greedy=false; } : ( . )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1226:54: ( . )*
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1226:54: ( . )*
            loop195:
            do {
                int alt195=2;
                int LA195_0 = input.LA(1);

                if ( (LA195_0=='*') ) {
                    int LA195_1 = input.LA(2);

                    if ( (LA195_1=='/') ) {
                        alt195=2;
                    }
                    else if ( ((LA195_1>='\u0000' && LA195_1<='.')||(LA195_1>='0' && LA195_1<='\uFFFF')) ) {
                        alt195=1;
                    }


                }
                else if ( ((LA195_0>='\u0000' && LA195_0<=')')||(LA195_0>='+' && LA195_0<='\uFFFF')) ) {
                    alt195=1;
                }


                switch (alt195) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1226:54: .
            	    {
            	    matchAny(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop195;
                }
            } while (true);


            }

            match("*/"); if (state.failed) return ;

            if ( state.backtracking==0 ) {

              //                        _channel = 2;   // Comments on channel 2 in case we want to find them
                                  
            }

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMENT"

    public void mTokens() throws RecognitionException {
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:8: ( T__114 | GEN | CDO | CDC | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS | GREATER | LBRACE | RBRACE | LBRACKET | RBRACKET | OPEQ | SEMI | COLON | DCOLON | SOLIDUS | MINUS | PLUS | STAR | LPAREN | RPAREN | COMMA | DOT | TILDE | PIPE | STRING | ONLY | NOT | AND | IDENT | HASH | IMPORT_SYM | PAGE_SYM | MEDIA_SYM | NAMESPACE_SYM | CHARSET_SYM | COUNTER_STYLE_SYM | FONT_FACE_SYM | IMPORTANT_SYM | TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM | MOZ_DOCUMENT_SYM | NUMBER | URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP | WS | NL | COMMENT )
        int alt196=67;
        alt196 = dfa196.predict(input);
        switch (alt196) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:10: T__114
                {
                mT__114(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:17: GEN
                {
                mGEN(); if (state.failed) return ;

                }
                break;
            case 3 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:21: CDO
                {
                mCDO(); if (state.failed) return ;

                }
                break;
            case 4 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:25: CDC
                {
                mCDC(); if (state.failed) return ;

                }
                break;
            case 5 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:29: INCLUDES
                {
                mINCLUDES(); if (state.failed) return ;

                }
                break;
            case 6 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:38: DASHMATCH
                {
                mDASHMATCH(); if (state.failed) return ;

                }
                break;
            case 7 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:48: BEGINS
                {
                mBEGINS(); if (state.failed) return ;

                }
                break;
            case 8 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:55: ENDS
                {
                mENDS(); if (state.failed) return ;

                }
                break;
            case 9 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:60: CONTAINS
                {
                mCONTAINS(); if (state.failed) return ;

                }
                break;
            case 10 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:69: GREATER
                {
                mGREATER(); if (state.failed) return ;

                }
                break;
            case 11 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:77: LBRACE
                {
                mLBRACE(); if (state.failed) return ;

                }
                break;
            case 12 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:84: RBRACE
                {
                mRBRACE(); if (state.failed) return ;

                }
                break;
            case 13 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:91: LBRACKET
                {
                mLBRACKET(); if (state.failed) return ;

                }
                break;
            case 14 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:100: RBRACKET
                {
                mRBRACKET(); if (state.failed) return ;

                }
                break;
            case 15 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:109: OPEQ
                {
                mOPEQ(); if (state.failed) return ;

                }
                break;
            case 16 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:114: SEMI
                {
                mSEMI(); if (state.failed) return ;

                }
                break;
            case 17 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:119: COLON
                {
                mCOLON(); if (state.failed) return ;

                }
                break;
            case 18 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:125: DCOLON
                {
                mDCOLON(); if (state.failed) return ;

                }
                break;
            case 19 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:132: SOLIDUS
                {
                mSOLIDUS(); if (state.failed) return ;

                }
                break;
            case 20 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:140: MINUS
                {
                mMINUS(); if (state.failed) return ;

                }
                break;
            case 21 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:146: PLUS
                {
                mPLUS(); if (state.failed) return ;

                }
                break;
            case 22 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:151: STAR
                {
                mSTAR(); if (state.failed) return ;

                }
                break;
            case 23 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:156: LPAREN
                {
                mLPAREN(); if (state.failed) return ;

                }
                break;
            case 24 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:163: RPAREN
                {
                mRPAREN(); if (state.failed) return ;

                }
                break;
            case 25 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:170: COMMA
                {
                mCOMMA(); if (state.failed) return ;

                }
                break;
            case 26 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:176: DOT
                {
                mDOT(); if (state.failed) return ;

                }
                break;
            case 27 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:180: TILDE
                {
                mTILDE(); if (state.failed) return ;

                }
                break;
            case 28 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:186: PIPE
                {
                mPIPE(); if (state.failed) return ;

                }
                break;
            case 29 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:191: STRING
                {
                mSTRING(); if (state.failed) return ;

                }
                break;
            case 30 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:198: ONLY
                {
                mONLY(); if (state.failed) return ;

                }
                break;
            case 31 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:203: NOT
                {
                mNOT(); if (state.failed) return ;

                }
                break;
            case 32 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:207: AND
                {
                mAND(); if (state.failed) return ;

                }
                break;
            case 33 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:211: IDENT
                {
                mIDENT(); if (state.failed) return ;

                }
                break;
            case 34 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:217: HASH
                {
                mHASH(); if (state.failed) return ;

                }
                break;
            case 35 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:222: IMPORT_SYM
                {
                mIMPORT_SYM(); if (state.failed) return ;

                }
                break;
            case 36 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:233: PAGE_SYM
                {
                mPAGE_SYM(); if (state.failed) return ;

                }
                break;
            case 37 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:242: MEDIA_SYM
                {
                mMEDIA_SYM(); if (state.failed) return ;

                }
                break;
            case 38 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:252: NAMESPACE_SYM
                {
                mNAMESPACE_SYM(); if (state.failed) return ;

                }
                break;
            case 39 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:266: CHARSET_SYM
                {
                mCHARSET_SYM(); if (state.failed) return ;

                }
                break;
            case 40 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:278: COUNTER_STYLE_SYM
                {
                mCOUNTER_STYLE_SYM(); if (state.failed) return ;

                }
                break;
            case 41 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:296: FONT_FACE_SYM
                {
                mFONT_FACE_SYM(); if (state.failed) return ;

                }
                break;
            case 42 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:310: IMPORTANT_SYM
                {
                mIMPORTANT_SYM(); if (state.failed) return ;

                }
                break;
            case 43 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:324: TOPLEFTCORNER_SYM
                {
                mTOPLEFTCORNER_SYM(); if (state.failed) return ;

                }
                break;
            case 44 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:342: TOPLEFT_SYM
                {
                mTOPLEFT_SYM(); if (state.failed) return ;

                }
                break;
            case 45 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:354: TOPCENTER_SYM
                {
                mTOPCENTER_SYM(); if (state.failed) return ;

                }
                break;
            case 46 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:368: TOPRIGHT_SYM
                {
                mTOPRIGHT_SYM(); if (state.failed) return ;

                }
                break;
            case 47 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:381: TOPRIGHTCORNER_SYM
                {
                mTOPRIGHTCORNER_SYM(); if (state.failed) return ;

                }
                break;
            case 48 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:400: BOTTOMLEFTCORNER_SYM
                {
                mBOTTOMLEFTCORNER_SYM(); if (state.failed) return ;

                }
                break;
            case 49 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:421: BOTTOMLEFT_SYM
                {
                mBOTTOMLEFT_SYM(); if (state.failed) return ;

                }
                break;
            case 50 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:436: BOTTOMCENTER_SYM
                {
                mBOTTOMCENTER_SYM(); if (state.failed) return ;

                }
                break;
            case 51 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:453: BOTTOMRIGHT_SYM
                {
                mBOTTOMRIGHT_SYM(); if (state.failed) return ;

                }
                break;
            case 52 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:469: BOTTOMRIGHTCORNER_SYM
                {
                mBOTTOMRIGHTCORNER_SYM(); if (state.failed) return ;

                }
                break;
            case 53 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:491: LEFTTOP_SYM
                {
                mLEFTTOP_SYM(); if (state.failed) return ;

                }
                break;
            case 54 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:503: LEFTMIDDLE_SYM
                {
                mLEFTMIDDLE_SYM(); if (state.failed) return ;

                }
                break;
            case 55 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:518: LEFTBOTTOM_SYM
                {
                mLEFTBOTTOM_SYM(); if (state.failed) return ;

                }
                break;
            case 56 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:533: RIGHTTOP_SYM
                {
                mRIGHTTOP_SYM(); if (state.failed) return ;

                }
                break;
            case 57 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:546: RIGHTMIDDLE_SYM
                {
                mRIGHTMIDDLE_SYM(); if (state.failed) return ;

                }
                break;
            case 58 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:562: RIGHTBOTTOM_SYM
                {
                mRIGHTBOTTOM_SYM(); if (state.failed) return ;

                }
                break;
            case 59 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:578: MOZ_DOCUMENT_SYM
                {
                mMOZ_DOCUMENT_SYM(); if (state.failed) return ;

                }
                break;
            case 60 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:595: NUMBER
                {
                mNUMBER(); if (state.failed) return ;

                }
                break;
            case 61 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:602: URI
                {
                mURI(); if (state.failed) return ;

                }
                break;
            case 62 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:606: MOZ_URL_PREFIX
                {
                mMOZ_URL_PREFIX(); if (state.failed) return ;

                }
                break;
            case 63 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:621: MOZ_DOMAIN
                {
                mMOZ_DOMAIN(); if (state.failed) return ;

                }
                break;
            case 64 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:632: MOZ_REGEXP
                {
                mMOZ_REGEXP(); if (state.failed) return ;

                }
                break;
            case 65 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:643: WS
                {
                mWS(); if (state.failed) return ;

                }
                break;
            case 66 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:646: NL
                {
                mNL(); if (state.failed) return ;

                }
                break;
            case 67 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:649: COMMENT
                {
                mCOMMENT(); if (state.failed) return ;

                }
                break;

        }

    }

    // $ANTLR start synpred1_Css3
    public final void synpred1_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1123:15: ( D P ( I | C ) )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1123:16: D P ( I | C )
        {
        mD(); if (state.failed) return ;
        mP(); if (state.failed) return ;
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1123:20: ( I | C )
        int alt197=2;
        switch ( input.LA(1) ) {
        case 'I':
        case 'i':
            {
            alt197=1;
            }
            break;
        case '\\':
            {
            switch ( input.LA(2) ) {
            case 'I':
            case 'i':
                {
                alt197=1;
                }
                break;
            case '0':
                {
                int LA197_4 = input.LA(3);

                if ( (LA197_4=='0') ) {
                    int LA197_6 = input.LA(4);

                    if ( (LA197_6=='0') ) {
                        int LA197_7 = input.LA(5);

                        if ( (LA197_7=='0') ) {
                            int LA197_8 = input.LA(6);

                            if ( (LA197_8=='4'||LA197_8=='6') ) {
                                int LA197_5 = input.LA(7);

                                if ( (LA197_5=='9') ) {
                                    alt197=1;
                                }
                                else if ( (LA197_5=='3') ) {
                                    alt197=2;
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 197, 5, input);

                                    throw nvae;
                                }
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 197, 8, input);

                                throw nvae;
                            }
                        }
                        else if ( (LA197_7=='4'||LA197_7=='6') ) {
                            int LA197_5 = input.LA(6);

                            if ( (LA197_5=='9') ) {
                                alt197=1;
                            }
                            else if ( (LA197_5=='3') ) {
                                alt197=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 197, 5, input);

                                throw nvae;
                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 197, 7, input);

                            throw nvae;
                        }
                    }
                    else if ( (LA197_6=='4'||LA197_6=='6') ) {
                        int LA197_5 = input.LA(5);

                        if ( (LA197_5=='9') ) {
                            alt197=1;
                        }
                        else if ( (LA197_5=='3') ) {
                            alt197=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 197, 5, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 197, 6, input);

                        throw nvae;
                    }
                }
                else if ( (LA197_4=='4'||LA197_4=='6') ) {
                    int LA197_5 = input.LA(4);

                    if ( (LA197_5=='9') ) {
                        alt197=1;
                    }
                    else if ( (LA197_5=='3') ) {
                        alt197=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 197, 5, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 197, 4, input);

                    throw nvae;
                }
                }
                break;
            case '4':
            case '6':
                {
                int LA197_5 = input.LA(3);

                if ( (LA197_5=='9') ) {
                    alt197=1;
                }
                else if ( (LA197_5=='3') ) {
                    alt197=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 197, 5, input);

                    throw nvae;
                }
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 197, 2, input);

                throw nvae;
            }

            }
            break;
        case 'C':
        case 'c':
            {
            alt197=2;
            }
            break;
        default:
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 197, 0, input);

            throw nvae;
        }

        switch (alt197) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1123:21: I
                {
                mI(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1123:23: C
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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1130:15: ( E ( M | X ) )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1130:16: E ( M | X )
        {
        mE(); if (state.failed) return ;
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1130:18: ( M | X )
        int alt198=2;
        switch ( input.LA(1) ) {
        case 'M':
        case 'm':
            {
            alt198=1;
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
                alt198=1;
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
                            int LA198_7 = input.LA(6);

                            if ( (LA198_7=='4'||LA198_7=='6') ) {
                                alt198=1;
                            }
                            else if ( (LA198_7=='5'||LA198_7=='7') ) {
                                alt198=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 198, 7, input);

                                throw nvae;
                            }
                            }
                            break;
                        case '4':
                        case '6':
                            {
                            alt198=1;
                            }
                            break;
                        case '5':
                        case '7':
                            {
                            alt198=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 198, 6, input);

                            throw nvae;
                        }

                        }
                        break;
                    case '4':
                    case '6':
                        {
                        alt198=1;
                        }
                        break;
                    case '5':
                    case '7':
                        {
                        alt198=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 198, 5, input);

                        throw nvae;
                    }

                    }
                    break;
                case '4':
                case '6':
                    {
                    alt198=1;
                    }
                    break;
                case '5':
                case '7':
                    {
                    alt198=2;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 198, 4, input);

                    throw nvae;
                }

                }
                break;
            case '5':
            case '7':
            case 'X':
            case 'x':
                {
                alt198=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 198, 2, input);

                throw nvae;
            }

            }
            break;
        case 'X':
        case 'x':
            {
            alt198=2;
            }
            break;
        default:
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 198, 0, input);

            throw nvae;
        }

        switch (alt198) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1130:19: M
                {
                mM(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1130:21: X
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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1136:15: ( P ( X | T | C ) )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1136:16: P ( X | T | C )
        {
        mP(); if (state.failed) return ;
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1136:17: ( X | T | C )
        int alt199=3;
        alt199 = dfa199.predict(input);
        switch (alt199) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1136:18: X
                {
                mX(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1136:20: T
                {
                mT(); if (state.failed) return ;

                }
                break;
            case 3 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1136:22: C
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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1144:15: ( C M )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1144:16: C M
        {
        mC(); if (state.failed) return ;
        mM(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Css3

    // $ANTLR start synpred5_Css3
    public final void synpred5_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1146:15: ( M ( M | S ) )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1146:16: M ( M | S )
        {
        mM(); if (state.failed) return ;
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1146:18: ( M | S )
        int alt200=2;
        switch ( input.LA(1) ) {
        case 'M':
        case 'm':
            {
            alt200=1;
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
                alt200=1;
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
                            int LA200_7 = input.LA(6);

                            if ( (LA200_7=='4'||LA200_7=='6') ) {
                                alt200=1;
                            }
                            else if ( (LA200_7=='5'||LA200_7=='7') ) {
                                alt200=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 200, 7, input);

                                throw nvae;
                            }
                            }
                            break;
                        case '4':
                        case '6':
                            {
                            alt200=1;
                            }
                            break;
                        case '5':
                        case '7':
                            {
                            alt200=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 200, 6, input);

                            throw nvae;
                        }

                        }
                        break;
                    case '4':
                    case '6':
                        {
                        alt200=1;
                        }
                        break;
                    case '5':
                    case '7':
                        {
                        alt200=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 200, 5, input);

                        throw nvae;
                    }

                    }
                    break;
                case '4':
                case '6':
                    {
                    alt200=1;
                    }
                    break;
                case '5':
                case '7':
                    {
                    alt200=2;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 200, 4, input);

                    throw nvae;
                }

                }
                break;
            case '5':
            case '7':
            case 'S':
            case 's':
                {
                alt200=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 200, 2, input);

                throw nvae;
            }

            }
            break;
        case 'S':
        case 's':
            {
            alt200=2;
            }
            break;
        default:
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 200, 0, input);

            throw nvae;
        }

        switch (alt200) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1146:19: M
                {
                mM(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1146:21: S
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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1153:15: ( I N )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1153:16: I N
        {
        mI(); if (state.failed) return ;
        mN(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Css3

    // $ANTLR start synpred7_Css3
    public final void synpred7_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1156:15: ( D E G )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1156:16: D E G
        {
        mD(); if (state.failed) return ;
        mE(); if (state.failed) return ;
        mG(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Css3

    // $ANTLR start synpred8_Css3
    public final void synpred8_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1158:15: ( R A D )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1158:16: R A D
        {
        mR(); if (state.failed) return ;
        mA(); if (state.failed) return ;
        mD(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Css3

    // $ANTLR start synpred9_Css3
    public final void synpred9_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1161:15: ( S )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1161:16: S
        {
        mS(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_Css3

    // $ANTLR start synpred10_Css3
    public final void synpred10_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1163:15: ( ( K )? H Z )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1163:16: ( K )? H Z
        {
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1163:16: ( K )?
        int alt201=2;
        int LA201_0 = input.LA(1);

        if ( (LA201_0=='K'||LA201_0=='k') ) {
            alt201=1;
        }
        else if ( (LA201_0=='\\') ) {
            switch ( input.LA(2) ) {
                case 'K':
                case 'k':
                    {
                    alt201=1;
                    }
                    break;
                case '0':
                    {
                    int LA201_4 = input.LA(3);

                    if ( (LA201_4=='0') ) {
                        int LA201_6 = input.LA(4);

                        if ( (LA201_6=='0') ) {
                            int LA201_7 = input.LA(5);

                            if ( (LA201_7=='0') ) {
                                int LA201_8 = input.LA(6);

                                if ( (LA201_8=='4'||LA201_8=='6') ) {
                                    int LA201_5 = input.LA(7);

                                    if ( (LA201_5=='B'||LA201_5=='b') ) {
                                        alt201=1;
                                    }
                                }
                            }
                            else if ( (LA201_7=='4'||LA201_7=='6') ) {
                                int LA201_5 = input.LA(6);

                                if ( (LA201_5=='B'||LA201_5=='b') ) {
                                    alt201=1;
                                }
                            }
                        }
                        else if ( (LA201_6=='4'||LA201_6=='6') ) {
                            int LA201_5 = input.LA(5);

                            if ( (LA201_5=='B'||LA201_5=='b') ) {
                                alt201=1;
                            }
                        }
                    }
                    else if ( (LA201_4=='4'||LA201_4=='6') ) {
                        int LA201_5 = input.LA(4);

                        if ( (LA201_5=='B'||LA201_5=='b') ) {
                            alt201=1;
                        }
                    }
                    }
                    break;
                case '4':
                case '6':
                    {
                    int LA201_5 = input.LA(3);

                    if ( (LA201_5=='B'||LA201_5=='b') ) {
                        alt201=1;
                    }
                    }
                    break;
            }

        }
        switch (alt201) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1163:16: K
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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1179:14: ( WS )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1179:15: WS
        {
        mWS(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_Css3

    // $ANTLR start synpred12_Css3
    public final void synpred12_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1186:14: ( WS )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1186:15: WS
        {
        mWS(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_Css3

    // $ANTLR start synpred13_Css3
    public final void synpred13_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1194:14: ( WS )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1194:15: WS
        {
        mWS(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_Css3

    // $ANTLR start synpred14_Css3
    public final void synpred14_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1202:14: ( WS )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1202:15: WS
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
    protected DFA180 dfa180 = new DFA180(this);
    protected DFA177 dfa177 = new DFA177(this);
    protected DFA196 dfa196 = new DFA196(this);
    protected DFA199 dfa199 = new DFA199(this);
    static final String DFA11_eotS =
        "\1\1\15\uffff";
    static final String DFA11_eofS =
        "\16\uffff";
    static final String DFA11_minS =
        "\1\41\15\uffff";
    static final String DFA11_maxS =
        "\1\uffff\15\uffff";
    static final String DFA11_acceptS =
        "\1\uffff\1\15\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14";
    static final String DFA11_specialS =
        "\16\uffff}>";
    static final String[] DFA11_transitionS = {
            "\1\3\1\uffff\1\4\1\5\1\6\1\7\3\uffff\1\10\2\uffff\1\15\1\12"+
            "\1\14\12\15\1\13\6\uffff\32\15\1\2\1\15\2\uffff\1\15\1\uffff"+
            "\32\15\3\uffff\1\11\1\uffff\uff80\15",
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
            return "()* loopback of 763:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | NMCHAR )*";
        }
    }
    static final String DFA180_eotS =
        "\1\30\1\14\1\uffff\6\14\1\uffff\2\14\1\uffff\7\14\1\uffff\2\14\2"+
        "\uffff\1\14\1\uffff\16\14\2\uffff\4\14\27\uffff\1\14\1\uffff\1\14"+
        "\1\uffff\1\14\1\uffff\1\14\5\uffff\1\14\1\uffff\6\14\3\uffff\16"+
        "\14\5\uffff\2\14\1\uffff\1\14\4\uffff\2\14\1\uffff\1\14\3\uffff"+
        "\2\14\4\uffff\2\14\1\uffff\1\14\3\uffff\2\14\3\uffff\6\14\3\uffff"+
        "\2\14\3\uffff\2\14\3\uffff\5\14\3\uffff\20\14\1\uffff\2\14\2\uffff"+
        "\5\14\3\uffff\3\14\3\uffff\2\14\2\uffff\3\14\3\uffff\2\14\3\uffff"+
        "\12\14\2\uffff\2\14\1\uffff\1\14\2\uffff\13\14\1\uffff\16\14\1\uffff"+
        "\2\14\2\uffff\2\14\2\uffff\3\14\3\uffff\3\14\3\uffff\2\14\2\uffff"+
        "\3\14\3\uffff\2\14\2\uffff\2\14\1\uffff\3\14\2\uffff\5\14\2\uffff"+
        "\2\14\1\uffff\3\14\2\uffff\11\14\1\uffff\15\14\1\uffff\2\14\2\uffff"+
        "\2\14\2\uffff\3\14\3\uffff\3\14\3\uffff\2\14\2\uffff\3\14\3\uffff"+
        "\2\14\2\uffff\2\14\1\uffff\3\14\2\uffff\5\14\2\uffff\2\14\1\uffff"+
        "\3\14\2\uffff\10\14\1\uffff\13\14\1\uffff\2\14\2\uffff\2\14\2\uffff"+
        "\2\14\3\uffff\2\14\3\uffff\1\14\2\uffff\2\14\3\uffff\1\14\2\uffff"+
        "\2\14\1\uffff\2\14\2\uffff\3\14\2\uffff\1\14\1\uffff\3\14\2\uffff"+
        "\5\14\16\uffff\1\14\1\uffff\1\14\2\uffff\1\14\3\uffff\2\14\6\uffff";
    static final String DFA180_eofS =
        "\u01fe\uffff";
    static final String DFA180_minS =
        "\1\45\1\105\1\0\1\115\1\103\2\115\1\116\1\101\1\0\1\110\1\132\1"+
        "\uffff\1\105\1\115\1\103\2\115\1\116\1\101\1\0\1\110\1\132\2\uffff"+
        "\1\103\1\0\1\107\1\103\1\107\1\103\1\60\1\63\1\103\1\115\1\60\1"+
        "\115\2\116\2\101\2\0\2\110\2\132\27\0\1\104\1\0\1\104\1\uffff\1"+
        "\132\1\0\1\132\5\0\1\115\1\0\1\115\2\103\2\60\1\65\3\0\1\60\1\63"+
        "\1\60\1\105\3\115\1\116\1\110\1\132\1\115\1\110\1\103\1\101\1\0"+
        "\1\uffff\3\0\1\60\1\104\1\0\1\70\1\uffff\3\0\1\60\1\64\1\0\1\63"+
        "\1\uffff\2\0\1\60\1\104\1\uffff\3\0\1\60\1\104\1\0\1\63\1\uffff"+
        "\2\0\1\60\1\105\3\0\1\60\1\61\2\132\1\60\1\70\1\uffff\2\0\1\60\1"+
        "\101\1\uffff\2\0\1\60\1\63\3\0\2\60\1\65\1\103\1\107\1\uffff\2\0"+
        "\1\60\1\67\1\60\1\63\1\60\1\105\3\115\1\116\1\110\1\132\1\115\1"+
        "\110\1\103\1\101\1\0\2\107\2\0\2\104\1\60\1\104\1\70\3\0\1\60\1"+
        "\64\1\63\3\0\1\60\1\104\2\0\1\60\1\104\1\63\3\0\1\60\1\105\2\0\1"+
        "\uffff\1\60\1\64\1\60\1\61\1\104\1\60\1\70\1\132\1\60\1\101\2\0"+
        "\1\60\1\63\1\0\1\115\2\0\1\60\1\104\2\60\1\65\1\103\1\107\2\115"+
        "\1\60\1\67\1\0\1\64\1\63\1\60\1\105\3\115\1\116\1\110\1\132\1\115"+
        "\1\110\1\103\1\101\1\0\2\107\2\0\2\104\2\0\1\60\1\104\1\70\3\0\1"+
        "\60\1\64\1\63\3\0\1\60\1\104\2\0\1\60\1\104\1\63\3\0\1\60\1\105"+
        "\2\0\1\60\1\64\1\0\1\60\1\61\1\104\2\0\1\60\1\70\1\132\1\60\1\101"+
        "\2\0\1\60\1\63\1\0\1\115\1\60\1\104\2\0\1\64\1\60\1\65\1\103\1\107"+
        "\2\115\1\60\1\67\1\0\1\63\1\60\1\105\3\115\1\116\1\110\1\132\1\115"+
        "\1\110\1\103\1\101\1\0\2\107\2\0\2\104\2\0\1\64\1\104\1\70\3\0\2"+
        "\64\1\63\3\0\1\64\1\104\2\0\1\64\1\104\1\63\3\0\1\64\1\105\2\0\1"+
        "\60\1\64\1\0\1\64\1\61\1\104\2\0\1\64\1\70\1\132\1\65\1\101\2\0"+
        "\1\64\1\63\1\0\1\115\1\60\1\104\2\0\1\60\1\65\1\103\1\107\2\115"+
        "\1\64\1\67\1\0\1\105\3\115\1\116\1\110\1\132\1\115\1\110\1\103\1"+
        "\101\1\0\2\107\2\0\2\104\2\0\1\104\1\70\3\0\1\64\1\63\3\0\1\104"+
        "\2\0\1\104\1\63\3\0\1\105\2\0\2\64\1\0\1\61\1\104\2\0\1\70\1\132"+
        "\1\101\2\0\1\63\1\0\1\115\1\64\1\104\2\0\1\103\1\107\2\115\1\67"+
        "\16\0\1\64\1\0\1\104\2\0\1\132\3\0\1\115\1\104\6\0";
    static final String DFA180_maxS =
        "\1\uffff\1\160\1\uffff\2\170\1\155\1\163\1\156\1\141\1\0\1\150\1"+
        "\172\1\uffff\1\160\2\170\1\155\1\163\1\156\1\141\1\0\1\150\1\172"+
        "\2\uffff\1\151\1\uffff\1\147\1\151\1\147\1\170\1\67\1\144\1\170"+
        "\1\163\1\63\1\163\2\156\2\141\2\0\2\150\2\172\1\0\1\uffff\4\0\1"+
        "\uffff\6\0\1\uffff\2\0\1\uffff\4\0\1\uffff\1\0\1\144\1\uffff\1\144"+
        "\1\uffff\1\172\1\uffff\1\172\1\0\1\uffff\2\0\1\uffff\1\155\1\0\1"+
        "\155\2\151\1\67\1\60\1\65\1\0\1\uffff\1\0\1\67\1\144\1\63\1\160"+
        "\1\170\1\155\1\163\1\156\1\150\1\172\1\163\1\150\1\170\1\141\1\0"+
        "\1\uffff\3\0\1\67\1\144\1\0\1\70\1\uffff\3\0\1\67\1\70\1\0\1\63"+
        "\1\uffff\2\0\1\66\1\144\1\uffff\3\0\1\67\1\144\1\0\1\63\1\uffff"+
        "\2\0\1\66\1\145\1\0\1\uffff\1\0\1\66\1\61\2\172\1\66\1\70\1\uffff"+
        "\2\0\1\67\1\141\1\uffff\2\0\1\66\1\71\1\0\1\uffff\1\0\1\67\1\60"+
        "\1\65\1\151\1\147\1\uffff\2\0\1\66\2\67\1\144\1\63\1\160\1\170\1"+
        "\155\1\163\1\156\1\150\1\172\1\163\1\150\1\170\1\141\1\0\2\147\2"+
        "\0\2\144\1\67\1\144\1\70\3\0\1\67\1\70\1\63\3\0\1\66\1\144\2\0\1"+
        "\67\1\144\1\63\3\0\1\66\1\145\2\0\1\uffff\1\66\1\64\1\66\1\61\1"+
        "\144\1\66\1\70\1\172\1\67\1\141\2\0\1\66\1\71\1\0\1\155\2\0\1\66"+
        "\1\144\1\67\1\60\1\65\1\151\1\147\2\155\1\66\1\67\1\0\1\67\1\144"+
        "\1\63\1\160\1\170\1\155\1\163\1\156\1\150\1\172\1\163\1\150\1\170"+
        "\1\141\1\0\2\147\2\0\2\144\2\0\1\67\1\144\1\70\3\0\1\67\1\70\1\63"+
        "\3\0\1\66\1\144\2\0\1\67\1\144\1\63\3\0\1\66\1\145\2\0\1\66\1\64"+
        "\1\0\1\66\1\61\1\144\2\0\1\66\1\70\1\172\1\67\1\141\2\0\1\66\1\71"+
        "\1\0\1\155\1\66\1\144\2\0\1\67\1\60\1\65\1\151\1\147\2\155\1\66"+
        "\1\67\1\0\1\144\1\63\1\160\1\170\1\155\1\163\1\156\1\150\1\172\1"+
        "\163\1\150\1\170\1\141\1\0\2\147\2\0\2\144\2\0\1\67\1\144\1\70\3"+
        "\0\1\67\1\70\1\63\3\0\1\66\1\144\2\0\1\67\1\144\1\63\3\0\1\66\1"+
        "\145\2\0\1\66\1\64\1\0\1\66\1\61\1\144\2\0\1\66\1\70\1\172\1\67"+
        "\1\141\2\0\1\66\1\71\1\0\1\155\1\66\1\144\2\0\1\60\1\65\1\151\1"+
        "\147\2\155\1\66\1\67\1\0\1\160\1\170\1\155\1\163\1\156\1\150\1\172"+
        "\1\163\1\150\1\170\1\141\1\0\2\147\2\0\2\144\2\0\1\144\1\70\3\0"+
        "\1\70\1\63\3\0\1\144\2\0\1\144\1\63\3\0\1\145\2\0\1\66\1\64\1\0"+
        "\1\61\1\144\2\0\1\70\1\172\1\141\2\0\1\71\1\0\1\155\1\66\1\144\2"+
        "\0\1\151\1\147\2\155\1\67\16\0\1\64\1\0\1\144\2\0\1\172\3\0\1\155"+
        "\1\144\6\0";
    static final String DFA180_acceptS =
        "\14\uffff\1\13\12\uffff\1\14\1\15\60\uffff\1\11\42\uffff\1\2\7\uffff"+
        "\1\3\7\uffff\1\4\4\uffff\1\5\7\uffff\1\6\15\uffff\1\12\4\uffff\1"+
        "\1\14\uffff\1\7\63\uffff\1\10\u0120\uffff";
    static final String DFA180_specialS =
        "\2\uffff\1\u00a5\6\uffff\1\112\12\uffff\1\115\5\uffff\1\u00b4\16"+
        "\uffff\1\u008d\1\u008a\4\uffff\1\167\1\u0085\1\143\1\166\1\142\1"+
        "\163\1\117\1\u00a6\1\32\1\162\1\u00a8\1\33\1\102\1\u00a7\1\106\1"+
        "\63\1\u00b2\1\u00a3\1\64\1\u00a4\1\61\1\65\1\60\1\uffff\1\5\3\uffff"+
        "\1\u00b7\1\uffff\1\25\1\131\1\27\1\u0092\1\141\1\uffff\1\u0093\6"+
        "\uffff\1\u009b\1\103\1\u0098\16\uffff\1\u00b6\1\uffff\1\52\1\51"+
        "\1\23\2\uffff\1\24\2\uffff\1\0\1\2\1\144\2\uffff\1\145\2\uffff\1"+
        "\u0097\1\u0096\3\uffff\1\101\1\100\1\157\2\uffff\1\155\2\uffff\1"+
        "\152\1\153\2\uffff\1\u00c4\1\154\1\u00c3\7\uffff\1\10\1\7\3\uffff"+
        "\1\u008f\1\u0090\2\uffff\1\u008b\1\u009f\1\u0089\6\uffff\1\150\1"+
        "\146\20\uffff\1\74\2\uffff\1\u00b0\1\u00ac\5\uffff\1\105\1\107\1"+
        "\177\3\uffff\1\u00a2\1\72\1\127\2\uffff\1\u00b3\1\u00b1\3\uffff"+
        "\1\140\1\137\1\75\2\uffff\1\u00ae\1\u00af\13\uffff\1\44\1\43\2\uffff"+
        "\1\161\1\uffff\1\36\1\37\13\uffff\1\u008c\16\uffff\1\11\2\uffff"+
        "\1\125\1\130\2\uffff\1\u0081\1\175\3\uffff\1\u0087\1\u0088\1\35"+
        "\3\uffff\1\56\1\136\1\57\2\uffff\1\171\1\172\3\uffff\1\u00a0\1\u00ad"+
        "\1\126\2\uffff\1\147\1\151\2\uffff\1\120\3\uffff\1\42\1\41\5\uffff"+
        "\1\134\1\135\2\uffff\1\50\3\uffff\1\71\1\73\11\uffff\1\70\15\uffff"+
        "\1\u00a9\2\uffff\1\62\1\124\2\uffff\1\u00bc\1\u00ba\3\uffff\1\u00c1"+
        "\1\u00c2\1\104\3\uffff\1\116\1\u00bf\1\14\2\uffff\1\113\1\110\3"+
        "\uffff\1\53\1\54\1\u009c\2\uffff\1\45\1\46\2\uffff\1\55\3\uffff"+
        "\1\u00be\1\u00bb\5\uffff\1\u009a\1\u0099\2\uffff\1\123\3\uffff\1"+
        "\u0080\1\174\10\uffff\1\22\13\uffff\1\132\2\uffff\1\4\1\3\2\uffff"+
        "\1\114\1\111\2\uffff\1\77\1\76\1\u0086\2\uffff\1\133\1\6\1\u0091"+
        "\1\uffff\1\21\1\20\2\uffff\1\121\1\122\1\13\1\uffff\1\u00b9\1\u00b8"+
        "\2\uffff\1\u00b5\2\uffff\1\176\1\173\3\uffff\1\17\1\12\1\uffff\1"+
        "\u0095\3\uffff\1\u00c0\1\u00bd\5\uffff\1\u0083\1\160\1\156\1\34"+
        "\1\31\1\u0094\1\1\1\164\1\165\1\15\1\16\1\u008e\1\67\1\66\1\uffff"+
        "\1\170\1\uffff\1\u00aa\1\u00ab\1\uffff\1\u009d\1\u009e\1\40\2\uffff"+
        "\1\30\1\26\1\47\1\u00a1\1\u0082\1\u0084}>";
    static final String[] DFA180_transitionS = {
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
            "\1\110\32\uffff\1\107\4\uffff\1\106",
            "\1\uffff",
            "\1\114\23\uffff\1\113\13\uffff\1\112",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
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
            "\1\110\32\uffff\1\107\4\uffff\1\106",
            "\1\uffff",
            "\1\114\23\uffff\1\113\13\uffff\1\112",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "",
            "",
            "\1\124\5\uffff\1\123\22\uffff\1\121\6\uffff\1\122\5\uffff\1"+
            "\120",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\127\3\14\1\131\1\130\1"+
            "\131\1\130\30\14\1\126\37\14\1\125\uff8f\14",
            "\1\134\24\uffff\1\133\12\uffff\1\132",
            "\1\124\5\uffff\1\123\22\uffff\1\121\6\uffff\1\122\5\uffff\1"+
            "\120",
            "\1\134\24\uffff\1\133\12\uffff\1\132",
            "\1\72\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1\67"+
            "\20\uffff\1\66\3\uffff\1\64",
            "\1\135\3\uffff\1\136\1\137\1\136\1\137",
            "\1\142\1\140\1\141\2\uffff\1\146\1\144\10\uffff\1\150\1\uffff"+
            "\1\147\35\uffff\1\145\1\uffff\1\143",
            "\1\72\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1\67"+
            "\20\uffff\1\66\3\uffff\1\64",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\151\1\uffff\1\152\1\153",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\110\32\uffff\1\107\4\uffff\1\106",
            "\1\110\32\uffff\1\107\4\uffff\1\106",
            "\1\uffff",
            "\1\uffff",
            "\1\114\23\uffff\1\113\13\uffff\1\112",
            "\1\114\23\uffff\1\113\13\uffff\1\112",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\160\3\14\1\161\1\163\1"+
            "\161\1\163\25\14\1\156\12\14\1\162\24\14\1\155\12\14\1\157\uff87"+
            "\14",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\170\3\14\1\173\1\171\1"+
            "\173\1\171\34\14\1\172\3\14\1\166\33\14\1\167\3\14\1\165\uff87"+
            "\14",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\177\3\14\1\u0080\1\14\1"+
            "\u0080\26\14\1\176\37\14\1\175\uff92\14",
            "\1\uffff",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u0085\3\14\1\u0086\1\u0088"+
            "\1\u0086\1\u0088\25\14\1\u0083\5\14\1\u0087\31\14\1\u0082\5"+
            "\14\1\u0084\uff8c\14",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u008c\3\14\1\u008d\1\14"+
            "\1\u008d\27\14\1\u008b\37\14\1\u008a\uff91\14",
            "\1\uffff",
            "\1\u0090\27\uffff\1\u008f\7\uffff\1\u008e",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u0091\3\14\1\u0092\1\14"+
            "\1\u0092\uffc9\14",
            "\1\u0090\27\uffff\1\u008f\7\uffff\1\u008e",
            "",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u0095\3\14\1\u0096\1\14"+
            "\1\u0096\21\14\1\u0094\37\14\1\u0093\uff97\14",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u009a\4\14\1\u009b\1\14"+
            "\1\u009b\42\14\1\u0099\37\14\1\u0098\uff85\14",
            "\1\uffff",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u009f\3\14\1\u00a0\1\14"+
            "\1\u00a0\22\14\1\u009e\37\14\1\u009d\uff96\14",
            "\1\u00a3\16\uffff\1\u00a2\20\uffff\1\u00a1",
            "\1\uffff",
            "\1\u00a3\16\uffff\1\u00a2\20\uffff\1\u00a1",
            "\1\124\5\uffff\1\123\22\uffff\1\121\6\uffff\1\122\5\uffff\1"+
            "\120",
            "\1\124\5\uffff\1\123\22\uffff\1\121\6\uffff\1\122\5\uffff\1"+
            "\120",
            "\1\u00a4\3\uffff\1\u00a6\1\u00a5\1\u00a6\1\u00a5",
            "\1\u00a7",
            "\1\u00a8",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u00ac\3\14\1\u00ad\1\14"+
            "\1\u00ad\20\14\1\u00ab\37\14\1\u00aa\uff98\14",
            "\1\uffff",
            "\1\u00ae\3\uffff\1\u00af\1\u00b0\1\u00af\1\u00b0",
            "\1\u00b3\1\u00b1\1\u00b2\2\uffff\1\u00b7\1\u00b5\10\uffff\1"+
            "\u00b9\1\uffff\1\u00b8\35\uffff\1\u00b6\1\uffff\1\u00b4",
            "\1\u00ba\1\uffff\1\u00bb\1\u00bc",
            "\1\u00be\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u00bd\12\uffff"+
            "\1\31",
            "\1\62\12\uffff\1\63\3\uffff\1\60\20\uffff\1\57\12\uffff\1\61",
            "\1\75\16\uffff\1\74\20\uffff\1\73",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\114\23\uffff\1\113\13\uffff\1\112",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\114\23\uffff\1\113\13\uffff\1\112",
            "\1\u00c0\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1"+
            "\u00bf\20\uffff\1\66\3\uffff\1\64",
            "\1\u00c2\32\uffff\1\107\4\uffff\1\u00c1",
            "\1\uffff",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00c3\3\uffff\1\u00c4\1\u00c5\1\u00c4\1\u00c5",
            "\1\u00c7\37\uffff\1\u00c6",
            "\1\uffff",
            "\1\u00c8",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00c9\3\uffff\1\u00cb\1\u00ca\1\u00cb\1\u00ca",
            "\1\u00cd\3\uffff\1\u00cc",
            "\1\uffff",
            "\1\u00ce",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\u00cf\3\uffff\1\u00d0\1\uffff\1\u00d0",
            "\1\u00d2\37\uffff\1\u00d1",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00d3\3\uffff\1\u00d4\1\u00d5\1\u00d4\1\u00d5",
            "\1\u00d7\37\uffff\1\u00d6",
            "\1\uffff",
            "\1\u00d8",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\u00d9\3\uffff\1\u00da\1\uffff\1\u00da",
            "\1\u00dc\37\uffff\1\u00db",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u00de\3\14\1\u00df\1\14"+
            "\1\u00df\uffc9\14",
            "\1\uffff",
            "\1\u00e0\3\uffff\1\u00e1\1\uffff\1\u00e1",
            "\1\u00e2",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\1\u00e3\3\uffff\1\u00e4\1\uffff\1\u00e4",
            "\1\u00e5",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\u00e6\4\uffff\1\u00e7\1\uffff\1\u00e7",
            "\1\u00e9\37\uffff\1\u00e8",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\u00ea\3\uffff\1\u00eb\1\uffff\1\u00eb",
            "\1\u00ed\5\uffff\1\u00ec",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u00f0\3\14\1\u00f1\1\14"+
            "\1\u00f1\26\14\1\u00ef\37\14\1\u00ee\uff92\14",
            "\1\uffff",
            "\1\u00f2\3\uffff\1\u00f4\1\u00f3\1\u00f4\1\u00f3",
            "\1\u00f5",
            "\1\u00f6",
            "\1\u00f8\5\uffff\1\123\22\uffff\1\121\6\uffff\1\u00f7\5\uffff"+
            "\1\120",
            "\1\134\24\uffff\1\133\12\uffff\1\132",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\u00f9\3\uffff\1\u00fa\1\uffff\1\u00fa",
            "\1\u00fb",
            "\1\u00fc\3\uffff\1\u00fd\1\u00fe\1\u00fd\1\u00fe",
            "\1\u0101\1\u00ff\1\u0100\2\uffff\1\u0105\1\u0103\10\uffff\1"+
            "\u0107\1\uffff\1\u0106\35\uffff\1\u0104\1\uffff\1\u0102",
            "\1\u0108\1\uffff\1\u0109\1\u010a",
            "\1\u010c\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u010b\12\uffff"+
            "\1\31",
            "\1\62\12\uffff\1\63\3\uffff\1\60\20\uffff\1\57\12\uffff\1\61",
            "\1\75\16\uffff\1\74\20\uffff\1\73",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\114\23\uffff\1\113\13\uffff\1\112",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\114\23\uffff\1\113\13\uffff\1\112",
            "\1\u010e\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1"+
            "\u010d\20\uffff\1\66\3\uffff\1\64",
            "\1\u0110\32\uffff\1\107\4\uffff\1\u010f",
            "\1\uffff",
            "\1\134\24\uffff\1\133\12\uffff\1\132",
            "\1\134\24\uffff\1\133\12\uffff\1\132",
            "\1\uffff",
            "\1\uffff",
            "\1\u0112\27\uffff\1\u008f\7\uffff\1\u0111",
            "\1\u0112\27\uffff\1\u008f\7\uffff\1\u0111",
            "\1\u0113\3\uffff\1\u0114\1\u0115\1\u0114\1\u0115",
            "\1\u0117\37\uffff\1\u0116",
            "\1\u0118",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0119\3\uffff\1\u011b\1\u011a\1\u011b\1\u011a",
            "\1\u011d\3\uffff\1\u011c",
            "\1\u011e",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u011f\3\uffff\1\u0120\1\uffff\1\u0120",
            "\1\u0122\37\uffff\1\u0121",
            "\1\uffff",
            "\1\uffff",
            "\1\u0123\3\uffff\1\u0124\1\u0125\1\u0124\1\u0125",
            "\1\u0127\37\uffff\1\u0126",
            "\1\u0128",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0129\3\uffff\1\u012a\1\uffff\1\u012a",
            "\1\u012c\37\uffff\1\u012b",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\u012d\3\uffff\1\u012e\1\uffff\1\u012e",
            "\1\u012f",
            "\1\u0130\3\uffff\1\u0131\1\uffff\1\u0131",
            "\1\u0132",
            "\1\u0134\27\uffff\1\u008f\7\uffff\1\u0133",
            "\1\u0135\3\uffff\1\u0136\1\uffff\1\u0136",
            "\1\u0137",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\1\u0138\4\uffff\1\u0139\1\uffff\1\u0139",
            "\1\u013b\37\uffff\1\u013a",
            "\1\uffff",
            "\1\uffff",
            "\1\u013c\3\uffff\1\u013d\1\uffff\1\u013d",
            "\1\u013f\5\uffff\1\u013e",
            "\1\uffff",
            "\1\u00a3\16\uffff\1\u00a2\20\uffff\1\u00a1",
            "\1\uffff",
            "\1\uffff",
            "\1\u0140\3\uffff\1\u0141\1\uffff\1\u0141",
            "\1\u0143\37\uffff\1\u0142",
            "\1\u0144\3\uffff\1\u0146\1\u0145\1\u0146\1\u0145",
            "\1\u0147",
            "\1\u0148",
            "\1\u014a\5\uffff\1\123\22\uffff\1\121\6\uffff\1\u0149\5\uffff"+
            "\1\120",
            "\1\134\24\uffff\1\133\12\uffff\1\132",
            "\1\u00a3\16\uffff\1\u00a2\20\uffff\1\u00a1",
            "\1\u00a3\16\uffff\1\u00a2\20\uffff\1\u00a1",
            "\1\u014b\3\uffff\1\u014c\1\uffff\1\u014c",
            "\1\u014d",
            "\1\uffff",
            "\1\u014e\1\u014f\1\u014e\1\u014f",
            "\1\u0152\1\u0150\1\u0151\2\uffff\1\u0156\1\u0154\10\uffff\1"+
            "\u0158\1\uffff\1\u0157\35\uffff\1\u0155\1\uffff\1\u0153",
            "\1\u0159\1\uffff\1\u015a\1\u015b",
            "\1\u015d\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u015c\12\uffff"+
            "\1\31",
            "\1\62\12\uffff\1\63\3\uffff\1\60\20\uffff\1\57\12\uffff\1\61",
            "\1\75\16\uffff\1\74\20\uffff\1\73",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\114\23\uffff\1\113\13\uffff\1\112",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\114\23\uffff\1\113\13\uffff\1\112",
            "\1\u015f\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1"+
            "\u015e\20\uffff\1\66\3\uffff\1\64",
            "\1\u0161\32\uffff\1\107\4\uffff\1\u0160",
            "\1\uffff",
            "\1\134\24\uffff\1\133\12\uffff\1\132",
            "\1\134\24\uffff\1\133\12\uffff\1\132",
            "\1\uffff",
            "\1\uffff",
            "\1\u0163\27\uffff\1\u008f\7\uffff\1\u0162",
            "\1\u0163\27\uffff\1\u008f\7\uffff\1\u0162",
            "\1\uffff",
            "\1\uffff",
            "\1\u0164\3\uffff\1\u0165\1\u0166\1\u0165\1\u0166",
            "\1\u0168\37\uffff\1\u0167",
            "\1\u0169",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u016a\3\uffff\1\u016c\1\u016b\1\u016c\1\u016b",
            "\1\u016e\3\uffff\1\u016d",
            "\1\u016f",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0170\3\uffff\1\u0171\1\uffff\1\u0171",
            "\1\u0173\37\uffff\1\u0172",
            "\1\uffff",
            "\1\uffff",
            "\1\u0174\3\uffff\1\u0175\1\u0176\1\u0175\1\u0176",
            "\1\u0178\37\uffff\1\u0177",
            "\1\u0179",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u017a\3\uffff\1\u017b\1\uffff\1\u017b",
            "\1\u017d\37\uffff\1\u017c",
            "\1\uffff",
            "\1\uffff",
            "\1\u017e\3\uffff\1\u017f\1\uffff\1\u017f",
            "\1\u0180",
            "\1\uffff",
            "\1\u0181\3\uffff\1\u0182\1\uffff\1\u0182",
            "\1\u0183",
            "\1\u0185\27\uffff\1\u008f\7\uffff\1\u0184",
            "\1\uffff",
            "\1\uffff",
            "\1\u0186\3\uffff\1\u0187\1\uffff\1\u0187",
            "\1\u0188",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\1\u0189\4\uffff\1\u018a\1\uffff\1\u018a",
            "\1\u018c\37\uffff\1\u018b",
            "\1\uffff",
            "\1\uffff",
            "\1\u018d\3\uffff\1\u018e\1\uffff\1\u018e",
            "\1\u0190\5\uffff\1\u018f",
            "\1\uffff",
            "\1\u00a3\16\uffff\1\u00a2\20\uffff\1\u00a1",
            "\1\u0191\3\uffff\1\u0192\1\uffff\1\u0192",
            "\1\u0194\37\uffff\1\u0193",
            "\1\uffff",
            "\1\uffff",
            "\1\u0196\1\u0195\1\u0196\1\u0195",
            "\1\u0197",
            "\1\u0198",
            "\1\u019a\5\uffff\1\123\22\uffff\1\121\6\uffff\1\u0199\5\uffff"+
            "\1\120",
            "\1\134\24\uffff\1\133\12\uffff\1\132",
            "\1\u00a3\16\uffff\1\u00a2\20\uffff\1\u00a1",
            "\1\u00a3\16\uffff\1\u00a2\20\uffff\1\u00a1",
            "\1\u019b\3\uffff\1\u019c\1\uffff\1\u019c",
            "\1\u019d",
            "\1\uffff",
            "\1\u01a0\1\u019e\1\u019f\2\uffff\1\u01a4\1\u01a2\10\uffff\1"+
            "\u01a6\1\uffff\1\u01a5\35\uffff\1\u01a3\1\uffff\1\u01a1",
            "\1\u01a7\1\uffff\1\u01a8\1\u01a9",
            "\1\u01ab\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u01aa\12\uffff"+
            "\1\31",
            "\1\62\12\uffff\1\63\3\uffff\1\60\20\uffff\1\57\12\uffff\1\61",
            "\1\75\16\uffff\1\74\20\uffff\1\73",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\114\23\uffff\1\113\13\uffff\1\112",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\114\23\uffff\1\113\13\uffff\1\112",
            "\1\u01ad\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1"+
            "\u01ac\20\uffff\1\66\3\uffff\1\64",
            "\1\u01af\32\uffff\1\107\4\uffff\1\u01ae",
            "\1\uffff",
            "\1\134\24\uffff\1\133\12\uffff\1\132",
            "\1\134\24\uffff\1\133\12\uffff\1\132",
            "\1\uffff",
            "\1\uffff",
            "\1\u01b1\27\uffff\1\u008f\7\uffff\1\u01b0",
            "\1\u01b1\27\uffff\1\u008f\7\uffff\1\u01b0",
            "\1\uffff",
            "\1\uffff",
            "\1\u01b2\1\u01b3\1\u01b2\1\u01b3",
            "\1\u01b5\37\uffff\1\u01b4",
            "\1\u01b6",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u01b8\1\u01b7\1\u01b8\1\u01b7",
            "\1\u01ba\3\uffff\1\u01b9",
            "\1\u01bb",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u01bc\1\uffff\1\u01bc",
            "\1\u01be\37\uffff\1\u01bd",
            "\1\uffff",
            "\1\uffff",
            "\1\u01bf\1\u01c0\1\u01bf\1\u01c0",
            "\1\u01c2\37\uffff\1\u01c1",
            "\1\u01c3",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u01c4\1\uffff\1\u01c4",
            "\1\u01c6\37\uffff\1\u01c5",
            "\1\uffff",
            "\1\uffff",
            "\1\u01c7\3\uffff\1\u01c8\1\uffff\1\u01c8",
            "\1\u01c9",
            "\1\uffff",
            "\1\u01ca\1\uffff\1\u01ca",
            "\1\u01cb",
            "\1\u01cd\27\uffff\1\u008f\7\uffff\1\u01cc",
            "\1\uffff",
            "\1\uffff",
            "\1\u01ce\1\uffff\1\u01ce",
            "\1\u01cf",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\1\u01d0\1\uffff\1\u01d0",
            "\1\u01d2\37\uffff\1\u01d1",
            "\1\uffff",
            "\1\uffff",
            "\1\u01d3\1\uffff\1\u01d3",
            "\1\u01d5\5\uffff\1\u01d4",
            "\1\uffff",
            "\1\u00a3\16\uffff\1\u00a2\20\uffff\1\u00a1",
            "\1\u01d6\3\uffff\1\u01d7\1\uffff\1\u01d7",
            "\1\u01d9\37\uffff\1\u01d8",
            "\1\uffff",
            "\1\uffff",
            "\1\u01da",
            "\1\u01db",
            "\1\u01dd\5\uffff\1\123\22\uffff\1\121\6\uffff\1\u01dc\5\uffff"+
            "\1\120",
            "\1\134\24\uffff\1\133\12\uffff\1\132",
            "\1\u00a3\16\uffff\1\u00a2\20\uffff\1\u00a1",
            "\1\u00a3\16\uffff\1\u00a2\20\uffff\1\u00a1",
            "\1\u01de\1\uffff\1\u01de",
            "\1\u01df",
            "\1\uffff",
            "\1\35\12\uffff\1\34\13\uffff\1\32\10\uffff\1\33\12\uffff\1"+
            "\31",
            "\1\62\12\uffff\1\63\3\uffff\1\60\20\uffff\1\57\12\uffff\1\61",
            "\1\75\16\uffff\1\74\20\uffff\1\73",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\114\23\uffff\1\113\13\uffff\1\112",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1"+
            "\100",
            "\1\114\23\uffff\1\113\13\uffff\1\112",
            "\1\72\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1\67"+
            "\20\uffff\1\66\3\uffff\1\64",
            "\1\110\32\uffff\1\107\4\uffff\1\106",
            "\1\uffff",
            "\1\134\24\uffff\1\133\12\uffff\1\132",
            "\1\134\24\uffff\1\133\12\uffff\1\132",
            "\1\uffff",
            "\1\uffff",
            "\1\u0090\27\uffff\1\u008f\7\uffff\1\u008e",
            "\1\u0090\27\uffff\1\u008f\7\uffff\1\u008e",
            "\1\uffff",
            "\1\uffff",
            "\1\u01e1\37\uffff\1\u01e0",
            "\1\u01e2",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u01e4\3\uffff\1\u01e3",
            "\1\u01e5",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u01e7\37\uffff\1\u01e6",
            "\1\uffff",
            "\1\uffff",
            "\1\u01e9\37\uffff\1\u01e8",
            "\1\u01ea",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u01ec\37\uffff\1\u01eb",
            "\1\uffff",
            "\1\uffff",
            "\1\u01ed\1\uffff\1\u01ed",
            "\1\u01ee",
            "\1\uffff",
            "\1\u01ef",
            "\1\u01f1\27\uffff\1\u008f\7\uffff\1\u01f0",
            "\1\uffff",
            "\1\uffff",
            "\1\u01f2",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\1\u01f4\37\uffff\1\u01f3",
            "\1\uffff",
            "\1\uffff",
            "\1\u01f6\5\uffff\1\u01f5",
            "\1\uffff",
            "\1\u00a3\16\uffff\1\u00a2\20\uffff\1\u00a1",
            "\1\u01f7\1\uffff\1\u01f7",
            "\1\u01f9\37\uffff\1\u01f8",
            "\1\uffff",
            "\1\uffff",
            "\1\124\5\uffff\1\123\22\uffff\1\121\6\uffff\1\122\5\uffff\1"+
            "\120",
            "\1\134\24\uffff\1\133\12\uffff\1\132",
            "\1\u00a3\16\uffff\1\u00a2\20\uffff\1\u00a1",
            "\1\u00a3\16\uffff\1\u00a2\20\uffff\1\u00a1",
            "\1\u01fa",
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
            "\1\u01fb",
            "\1\uffff",
            "\1\u0090\27\uffff\1\u008f\7\uffff\1\u008e",
            "\1\uffff",
            "\1\uffff",
            "\1\117\1\uffff\1\116\35\uffff\1\115",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00a3\16\uffff\1\u00a2\20\uffff\1\u00a1",
            "\1\u01fd\37\uffff\1\u01fc",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff"
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
            return "1122:9: ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R A D )=> R A D | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA180_117 = input.LA(1);

                         
                        int index180_117 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_117);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA180_485 = input.LA(1);

                         
                        int index180_485 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_485);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA180_118 = input.LA(1);

                         
                        int index180_118 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_118);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA180_429 = input.LA(1);

                         
                        int index180_429 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_429);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA180_428 = input.LA(1);

                         
                        int index180_428 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_428);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA180_71 = input.LA(1);

                        s = -1;
                        if ( ((LA180_71>='\u0000' && LA180_71<='\t')||LA180_71=='\u000B'||(LA180_71>='\u000E' && LA180_71<='/')||(LA180_71>='1' && LA180_71<='3')||LA180_71=='5'||(LA180_71>='7' && LA180_71<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_71=='0') ) {s = 145;}

                        else if ( (LA180_71=='4'||LA180_71=='6') ) {s = 146;}

                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA180_442 = input.LA(1);

                         
                        int index180_442 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_442);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA180_153 = input.LA(1);

                         
                        int index180_153 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 151;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_153);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA180_152 = input.LA(1);

                         
                        int index180_152 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 151;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_152);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA180_266 = input.LA(1);

                         
                        int index180_266 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 73;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_266);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA180_466 = input.LA(1);

                         
                        int index180_466 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 151;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_466);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA180_451 = input.LA(1);

                         
                        int index180_451 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_451);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA180_367 = input.LA(1);

                         
                        int index180_367 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_367);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA180_488 = input.LA(1);

                         
                        int index180_488 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_488);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA180_489 = input.LA(1);

                         
                        int index180_489 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_489);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA180_465 = input.LA(1);

                         
                        int index180_465 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 151;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_465);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA180_446 = input.LA(1);

                         
                        int index180_446 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 124;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_446);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA180_445 = input.LA(1);

                         
                        int index180_445 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 124;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_445);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA180_413 = input.LA(1);

                         
                        int index180_413 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 169;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_413);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA180_111 = input.LA(1);

                         
                        int index180_111 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_111);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA180_114 = input.LA(1);

                         
                        int index180_114 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_114);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA180_77 = input.LA(1);

                         
                        int index180_77 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 151;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_77);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA180_505 = input.LA(1);

                         
                        int index180_505 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_505);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA180_79 = input.LA(1);

                         
                        int index180_79 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 151;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_79);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA180_504 = input.LA(1);

                         
                        int index180_504 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_504);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA180_483 = input.LA(1);

                         
                        int index180_483 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_483);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA180_55 = input.LA(1);

                         
                        int index180_55 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_55);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA180_58 = input.LA(1);

                         
                        int index180_58 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_58);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA180_482 = input.LA(1);

                         
                        int index180_482 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_482);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA180_280 = input.LA(1);

                         
                        int index180_280 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_280);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA180_238 = input.LA(1);

                         
                        int index180_238 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_238);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA180_239 = input.LA(1);

                         
                        int index180_239 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_239);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA180_501 = input.LA(1);

                         
                        int index180_501 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_501);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA180_308 = input.LA(1);

                         
                        int index180_308 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_308);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA180_307 = input.LA(1);

                         
                        int index180_307 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_307);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA180_233 = input.LA(1);

                         
                        int index180_233 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 151;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_233);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA180_232 = input.LA(1);

                         
                        int index180_232 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 151;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_232);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA180_380 = input.LA(1);

                         
                        int index180_380 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_380);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA180_381 = input.LA(1);

                         
                        int index180_381 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_381);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA180_506 = input.LA(1);

                         
                        int index180_506 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 169;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_506);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA180_318 = input.LA(1);

                         
                        int index180_318 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_318);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA180_110 = input.LA(1);

                         
                        int index180_110 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_110);
                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA180_109 = input.LA(1);

                         
                        int index180_109 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_109);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA180_375 = input.LA(1);

                         
                        int index180_375 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_375);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA180_376 = input.LA(1);

                         
                        int index180_376 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_376);
                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA180_384 = input.LA(1);

                         
                        int index180_384 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_384);
                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA180_284 = input.LA(1);

                         
                        int index180_284 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_284);
                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA180_286 = input.LA(1);

                         
                        int index180_286 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_286);
                        if ( s>=0 ) return s;
                        break;
                    case 48 : 
                        int LA180_69 = input.LA(1);

                         
                        int index180_69 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_69);
                        if ( s>=0 ) return s;
                        break;
                    case 49 : 
                        int LA180_67 = input.LA(1);

                         
                        int index180_67 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_67);
                        if ( s>=0 ) return s;
                        break;
                    case 50 : 
                        int LA180_350 = input.LA(1);

                         
                        int index180_350 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_350);
                        if ( s>=0 ) return s;
                        break;
                    case 51 : 
                        int LA180_62 = input.LA(1);

                         
                        int index180_62 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_62);
                        if ( s>=0 ) return s;
                        break;
                    case 52 : 
                        int LA180_65 = input.LA(1);

                         
                        int index180_65 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_65);
                        if ( s>=0 ) return s;
                        break;
                    case 53 : 
                        int LA180_68 = input.LA(1);

                        s = -1;
                        if ( (LA180_68=='n') ) {s = 138;}

                        else if ( (LA180_68=='N') ) {s = 139;}

                        else if ( ((LA180_68>='\u0000' && LA180_68<='\t')||LA180_68=='\u000B'||(LA180_68>='\u000E' && LA180_68<='/')||(LA180_68>='1' && LA180_68<='3')||LA180_68=='5'||(LA180_68>='7' && LA180_68<='M')||(LA180_68>='O' && LA180_68<='m')||(LA180_68>='o' && LA180_68<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_68=='0') ) {s = 140;}

                        else if ( (LA180_68=='4'||LA180_68=='6') ) {s = 141;}

                        if ( s>=0 ) return s;
                        break;
                    case 54 : 
                        int LA180_492 = input.LA(1);

                         
                        int index180_492 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_492);
                        if ( s>=0 ) return s;
                        break;
                    case 55 : 
                        int LA180_491 = input.LA(1);

                         
                        int index180_491 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_491);
                        if ( s>=0 ) return s;
                        break;
                    case 56 : 
                        int LA180_333 = input.LA(1);

                         
                        int index180_333 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 169;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_333);
                        if ( s>=0 ) return s;
                        break;
                    case 57 : 
                        int LA180_322 = input.LA(1);

                         
                        int index180_322 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_322);
                        if ( s>=0 ) return s;
                        break;
                    case 58 : 
                        int LA180_205 = input.LA(1);

                         
                        int index180_205 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_205);
                        if ( s>=0 ) return s;
                        break;
                    case 59 : 
                        int LA180_323 = input.LA(1);

                         
                        int index180_323 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_323);
                        if ( s>=0 ) return s;
                        break;
                    case 60 : 
                        int LA180_188 = input.LA(1);

                         
                        int index180_188 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 73;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_188);
                        if ( s>=0 ) return s;
                        break;
                    case 61 : 
                        int LA180_216 = input.LA(1);

                         
                        int index180_216 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_216);
                        if ( s>=0 ) return s;
                        break;
                    case 62 : 
                        int LA180_437 = input.LA(1);

                         
                        int index180_437 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_437);
                        if ( s>=0 ) return s;
                        break;
                    case 63 : 
                        int LA180_436 = input.LA(1);

                         
                        int index180_436 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_436);
                        if ( s>=0 ) return s;
                        break;
                    case 64 : 
                        int LA180_131 = input.LA(1);

                         
                        int index180_131 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_131);
                        if ( s>=0 ) return s;
                        break;
                    case 65 : 
                        int LA180_130 = input.LA(1);

                         
                        int index180_130 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_130);
                        if ( s>=0 ) return s;
                        break;
                    case 66 : 
                        int LA180_59 = input.LA(1);

                         
                        int index180_59 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 124;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_59);
                        if ( s>=0 ) return s;
                        break;
                    case 67 : 
                        int LA180_91 = input.LA(1);

                        s = -1;
                        if ( (LA180_91=='g') ) {s = 170;}

                        else if ( (LA180_91=='G') ) {s = 171;}

                        else if ( ((LA180_91>='\u0000' && LA180_91<='\t')||LA180_91=='\u000B'||(LA180_91>='\u000E' && LA180_91<='/')||(LA180_91>='1' && LA180_91<='3')||LA180_91=='5'||(LA180_91>='7' && LA180_91<='F')||(LA180_91>='H' && LA180_91<='f')||(LA180_91>='h' && LA180_91<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_91=='0') ) {s = 172;}

                        else if ( (LA180_91=='4'||LA180_91=='6') ) {s = 173;}

                        if ( s>=0 ) return s;
                        break;
                    case 68 : 
                        int LA180_361 = input.LA(1);

                         
                        int index180_361 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_361);
                        if ( s>=0 ) return s;
                        break;
                    case 69 : 
                        int LA180_198 = input.LA(1);

                         
                        int index180_198 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_198);
                        if ( s>=0 ) return s;
                        break;
                    case 70 : 
                        int LA180_61 = input.LA(1);

                         
                        int index180_61 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 124;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_61);
                        if ( s>=0 ) return s;
                        break;
                    case 71 : 
                        int LA180_199 = input.LA(1);

                         
                        int index180_199 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_199);
                        if ( s>=0 ) return s;
                        break;
                    case 72 : 
                        int LA180_371 = input.LA(1);

                         
                        int index180_371 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 124;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_371);
                        if ( s>=0 ) return s;
                        break;
                    case 73 : 
                        int LA180_433 = input.LA(1);

                         
                        int index180_433 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_433);
                        if ( s>=0 ) return s;
                        break;
                    case 74 : 
                        int LA180_9 = input.LA(1);

                         
                        int index180_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 73;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_9);
                        if ( s>=0 ) return s;
                        break;
                    case 75 : 
                        int LA180_370 = input.LA(1);

                         
                        int index180_370 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 124;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_370);
                        if ( s>=0 ) return s;
                        break;
                    case 76 : 
                        int LA180_432 = input.LA(1);

                         
                        int index180_432 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_432);
                        if ( s>=0 ) return s;
                        break;
                    case 77 : 
                        int LA180_20 = input.LA(1);

                         
                        int index180_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 73;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_20);
                        if ( s>=0 ) return s;
                        break;
                    case 78 : 
                        int LA180_365 = input.LA(1);

                         
                        int index180_365 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_365);
                        if ( s>=0 ) return s;
                        break;
                    case 79 : 
                        int LA180_53 = input.LA(1);

                        s = -1;
                        if ( (LA180_53=='x') ) {s = 117;}

                        else if ( (LA180_53=='X') ) {s = 118;}

                        else if ( (LA180_53=='t') ) {s = 119;}

                        else if ( (LA180_53=='0') ) {s = 120;}

                        else if ( (LA180_53=='5'||LA180_53=='7') ) {s = 121;}

                        else if ( (LA180_53=='T') ) {s = 122;}

                        else if ( ((LA180_53>='\u0000' && LA180_53<='\t')||LA180_53=='\u000B'||(LA180_53>='\u000E' && LA180_53<='/')||(LA180_53>='1' && LA180_53<='3')||(LA180_53>='8' && LA180_53<='S')||(LA180_53>='U' && LA180_53<='W')||(LA180_53>='Y' && LA180_53<='s')||(LA180_53>='u' && LA180_53<='w')||(LA180_53>='y' && LA180_53<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_53=='4'||LA180_53=='6') ) {s = 123;}

                        if ( s>=0 ) return s;
                        break;
                    case 80 : 
                        int LA180_303 = input.LA(1);

                         
                        int index180_303 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_303);
                        if ( s>=0 ) return s;
                        break;
                    case 81 : 
                        int LA180_449 = input.LA(1);

                         
                        int index180_449 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_449);
                        if ( s>=0 ) return s;
                        break;
                    case 82 : 
                        int LA180_450 = input.LA(1);

                         
                        int index180_450 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_450);
                        if ( s>=0 ) return s;
                        break;
                    case 83 : 
                        int LA180_399 = input.LA(1);

                         
                        int index180_399 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_399);
                        if ( s>=0 ) return s;
                        break;
                    case 84 : 
                        int LA180_351 = input.LA(1);

                         
                        int index180_351 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_351);
                        if ( s>=0 ) return s;
                        break;
                    case 85 : 
                        int LA180_269 = input.LA(1);

                         
                        int index180_269 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_269);
                        if ( s>=0 ) return s;
                        break;
                    case 86 : 
                        int LA180_296 = input.LA(1);

                         
                        int index180_296 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_296);
                        if ( s>=0 ) return s;
                        break;
                    case 87 : 
                        int LA180_206 = input.LA(1);

                         
                        int index180_206 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_206);
                        if ( s>=0 ) return s;
                        break;
                    case 88 : 
                        int LA180_270 = input.LA(1);

                         
                        int index180_270 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_270);
                        if ( s>=0 ) return s;
                        break;
                    case 89 : 
                        int LA180_78 = input.LA(1);

                        s = -1;
                        if ( (LA180_78=='z') ) {s = 152;}

                        else if ( (LA180_78=='Z') ) {s = 153;}

                        else if ( ((LA180_78>='\u0000' && LA180_78<='\t')||LA180_78=='\u000B'||(LA180_78>='\u000E' && LA180_78<='/')||(LA180_78>='1' && LA180_78<='4')||LA180_78=='6'||(LA180_78>='8' && LA180_78<='Y')||(LA180_78>='[' && LA180_78<='y')||(LA180_78>='{' && LA180_78<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_78=='0') ) {s = 154;}

                        else if ( (LA180_78=='5'||LA180_78=='7') ) {s = 155;}

                        if ( s>=0 ) return s;
                        break;
                    case 90 : 
                        int LA180_425 = input.LA(1);

                         
                        int index180_425 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 73;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_425);
                        if ( s>=0 ) return s;
                        break;
                    case 91 : 
                        int LA180_441 = input.LA(1);

                         
                        int index180_441 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_441);
                        if ( s>=0 ) return s;
                        break;
                    case 92 : 
                        int LA180_314 = input.LA(1);

                         
                        int index180_314 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 151;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_314);
                        if ( s>=0 ) return s;
                        break;
                    case 93 : 
                        int LA180_315 = input.LA(1);

                         
                        int index180_315 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 151;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_315);
                        if ( s>=0 ) return s;
                        break;
                    case 94 : 
                        int LA180_285 = input.LA(1);

                         
                        int index180_285 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_285);
                        if ( s>=0 ) return s;
                        break;
                    case 95 : 
                        int LA180_215 = input.LA(1);

                         
                        int index180_215 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_215);
                        if ( s>=0 ) return s;
                        break;
                    case 96 : 
                        int LA180_214 = input.LA(1);

                         
                        int index180_214 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_214);
                        if ( s>=0 ) return s;
                        break;
                    case 97 : 
                        int LA180_81 = input.LA(1);

                        s = -1;
                        if ( (LA180_81=='i') ) {s = 157;}

                        else if ( (LA180_81=='I') ) {s = 158;}

                        else if ( ((LA180_81>='\u0000' && LA180_81<='\t')||LA180_81=='\u000B'||(LA180_81>='\u000E' && LA180_81<='/')||(LA180_81>='1' && LA180_81<='3')||LA180_81=='5'||(LA180_81>='7' && LA180_81<='H')||(LA180_81>='J' && LA180_81<='h')||(LA180_81>='j' && LA180_81<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_81=='0') ) {s = 159;}

                        else if ( (LA180_81=='4'||LA180_81=='6') ) {s = 160;}

                        if ( s>=0 ) return s;
                        break;
                    case 98 : 
                        int LA180_51 = input.LA(1);

                         
                        int index180_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_51);
                        if ( s>=0 ) return s;
                        break;
                    case 99 : 
                        int LA180_49 = input.LA(1);

                         
                        int index180_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_49);
                        if ( s>=0 ) return s;
                        break;
                    case 100 : 
                        int LA180_119 = input.LA(1);

                         
                        int index180_119 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_119);
                        if ( s>=0 ) return s;
                        break;
                    case 101 : 
                        int LA180_122 = input.LA(1);

                         
                        int index180_122 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_122);
                        if ( s>=0 ) return s;
                        break;
                    case 102 : 
                        int LA180_171 = input.LA(1);

                         
                        int index180_171 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 169;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_171);
                        if ( s>=0 ) return s;
                        break;
                    case 103 : 
                        int LA180_299 = input.LA(1);

                         
                        int index180_299 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_299);
                        if ( s>=0 ) return s;
                        break;
                    case 104 : 
                        int LA180_170 = input.LA(1);

                         
                        int index180_170 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 169;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_170);
                        if ( s>=0 ) return s;
                        break;
                    case 105 : 
                        int LA180_300 = input.LA(1);

                         
                        int index180_300 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_300);
                        if ( s>=0 ) return s;
                        break;
                    case 106 : 
                        int LA180_138 = input.LA(1);

                         
                        int index180_138 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_138);
                        if ( s>=0 ) return s;
                        break;
                    case 107 : 
                        int LA180_139 = input.LA(1);

                         
                        int index180_139 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_139);
                        if ( s>=0 ) return s;
                        break;
                    case 108 : 
                        int LA180_143 = input.LA(1);

                        s = -1;
                        if ( ((LA180_143>='\u0000' && LA180_143<='\t')||LA180_143=='\u000B'||(LA180_143>='\u000E' && LA180_143<='/')||(LA180_143>='1' && LA180_143<='3')||LA180_143=='5'||(LA180_143>='7' && LA180_143<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_143=='0') ) {s = 222;}

                        else if ( (LA180_143=='4'||LA180_143=='6') ) {s = 223;}

                        if ( s>=0 ) return s;
                        break;
                    case 109 : 
                        int LA180_135 = input.LA(1);

                         
                        int index180_135 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_135);
                        if ( s>=0 ) return s;
                        break;
                    case 110 : 
                        int LA180_481 = input.LA(1);

                         
                        int index180_481 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_481);
                        if ( s>=0 ) return s;
                        break;
                    case 111 : 
                        int LA180_132 = input.LA(1);

                         
                        int index180_132 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_132);
                        if ( s>=0 ) return s;
                        break;
                    case 112 : 
                        int LA180_480 = input.LA(1);

                         
                        int index180_480 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_480);
                        if ( s>=0 ) return s;
                        break;
                    case 113 : 
                        int LA180_236 = input.LA(1);

                         
                        int index180_236 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_236);
                        if ( s>=0 ) return s;
                        break;
                    case 114 : 
                        int LA180_56 = input.LA(1);

                         
                        int index180_56 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_56);
                        if ( s>=0 ) return s;
                        break;
                    case 115 : 
                        int LA180_52 = input.LA(1);

                         
                        int index180_52 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_52);
                        if ( s>=0 ) return s;
                        break;
                    case 116 : 
                        int LA180_486 = input.LA(1);

                         
                        int index180_486 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 124;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_486);
                        if ( s>=0 ) return s;
                        break;
                    case 117 : 
                        int LA180_487 = input.LA(1);

                         
                        int index180_487 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 124;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_487);
                        if ( s>=0 ) return s;
                        break;
                    case 118 : 
                        int LA180_50 = input.LA(1);

                         
                        int index180_50 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_50);
                        if ( s>=0 ) return s;
                        break;
                    case 119 : 
                        int LA180_47 = input.LA(1);

                         
                        int index180_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_47);
                        if ( s>=0 ) return s;
                        break;
                    case 120 : 
                        int LA180_494 = input.LA(1);

                         
                        int index180_494 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_494);
                        if ( s>=0 ) return s;
                        break;
                    case 121 : 
                        int LA180_289 = input.LA(1);

                         
                        int index180_289 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 124;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_289);
                        if ( s>=0 ) return s;
                        break;
                    case 122 : 
                        int LA180_290 = input.LA(1);

                         
                        int index180_290 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 124;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_290);
                        if ( s>=0 ) return s;
                        break;
                    case 123 : 
                        int LA180_461 = input.LA(1);

                         
                        int index180_461 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_461);
                        if ( s>=0 ) return s;
                        break;
                    case 124 : 
                        int LA180_404 = input.LA(1);

                         
                        int index180_404 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_404);
                        if ( s>=0 ) return s;
                        break;
                    case 125 : 
                        int LA180_274 = input.LA(1);

                         
                        int index180_274 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_274);
                        if ( s>=0 ) return s;
                        break;
                    case 126 : 
                        int LA180_460 = input.LA(1);

                         
                        int index180_460 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_460);
                        if ( s>=0 ) return s;
                        break;
                    case 127 : 
                        int LA180_200 = input.LA(1);

                         
                        int index180_200 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_200);
                        if ( s>=0 ) return s;
                        break;
                    case 128 : 
                        int LA180_403 = input.LA(1);

                         
                        int index180_403 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_403);
                        if ( s>=0 ) return s;
                        break;
                    case 129 : 
                        int LA180_273 = input.LA(1);

                         
                        int index180_273 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_273);
                        if ( s>=0 ) return s;
                        break;
                    case 130 : 
                        int LA180_508 = input.LA(1);

                         
                        int index180_508 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_508);
                        if ( s>=0 ) return s;
                        break;
                    case 131 : 
                        int LA180_479 = input.LA(1);

                         
                        int index180_479 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 169;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_479);
                        if ( s>=0 ) return s;
                        break;
                    case 132 : 
                        int LA180_509 = input.LA(1);

                         
                        int index180_509 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_509);
                        if ( s>=0 ) return s;
                        break;
                    case 133 : 
                        int LA180_48 = input.LA(1);

                        s = -1;
                        if ( (LA180_48=='m') ) {s = 109;}

                        else if ( (LA180_48=='M') ) {s = 110;}

                        else if ( (LA180_48=='x') ) {s = 111;}

                        else if ( (LA180_48=='0') ) {s = 112;}

                        else if ( (LA180_48=='4'||LA180_48=='6') ) {s = 113;}

                        else if ( (LA180_48=='X') ) {s = 114;}

                        else if ( ((LA180_48>='\u0000' && LA180_48<='\t')||LA180_48=='\u000B'||(LA180_48>='\u000E' && LA180_48<='/')||(LA180_48>='1' && LA180_48<='3')||(LA180_48>='8' && LA180_48<='L')||(LA180_48>='N' && LA180_48<='W')||(LA180_48>='Y' && LA180_48<='l')||(LA180_48>='n' && LA180_48<='w')||(LA180_48>='y' && LA180_48<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_48=='5'||LA180_48=='7') ) {s = 115;}

                        if ( s>=0 ) return s;
                        break;
                    case 134 : 
                        int LA180_438 = input.LA(1);

                         
                        int index180_438 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_438);
                        if ( s>=0 ) return s;
                        break;
                    case 135 : 
                        int LA180_278 = input.LA(1);

                         
                        int index180_278 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_278);
                        if ( s>=0 ) return s;
                        break;
                    case 136 : 
                        int LA180_279 = input.LA(1);

                         
                        int index180_279 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_279);
                        if ( s>=0 ) return s;
                        break;
                    case 137 : 
                        int LA180_163 = input.LA(1);

                         
                        int index180_163 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_163);
                        if ( s>=0 ) return s;
                        break;
                    case 138 : 
                        int LA180_42 = input.LA(1);

                         
                        int index180_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 73;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_42);
                        if ( s>=0 ) return s;
                        break;
                    case 139 : 
                        int LA180_161 = input.LA(1);

                         
                        int index180_161 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_161);
                        if ( s>=0 ) return s;
                        break;
                    case 140 : 
                        int LA180_251 = input.LA(1);

                         
                        int index180_251 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 169;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_251);
                        if ( s>=0 ) return s;
                        break;
                    case 141 : 
                        int LA180_41 = input.LA(1);

                         
                        int index180_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 73;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_41);
                        if ( s>=0 ) return s;
                        break;
                    case 142 : 
                        int LA180_490 = input.LA(1);

                         
                        int index180_490 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_490);
                        if ( s>=0 ) return s;
                        break;
                    case 143 : 
                        int LA180_157 = input.LA(1);

                         
                        int index180_157 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_157);
                        if ( s>=0 ) return s;
                        break;
                    case 144 : 
                        int LA180_158 = input.LA(1);

                         
                        int index180_158 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_158);
                        if ( s>=0 ) return s;
                        break;
                    case 145 : 
                        int LA180_443 = input.LA(1);

                         
                        int index180_443 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_443);
                        if ( s>=0 ) return s;
                        break;
                    case 146 : 
                        int LA180_80 = input.LA(1);

                         
                        int index180_80 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_80);
                        if ( s>=0 ) return s;
                        break;
                    case 147 : 
                        int LA180_83 = input.LA(1);

                         
                        int index180_83 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_83);
                        if ( s>=0 ) return s;
                        break;
                    case 148 : 
                        int LA180_484 = input.LA(1);

                         
                        int index180_484 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_484);
                        if ( s>=0 ) return s;
                        break;
                    case 149 : 
                        int LA180_468 = input.LA(1);

                         
                        int index180_468 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_468);
                        if ( s>=0 ) return s;
                        break;
                    case 150 : 
                        int LA180_126 = input.LA(1);

                         
                        int index180_126 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 124;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_126);
                        if ( s>=0 ) return s;
                        break;
                    case 151 : 
                        int LA180_125 = input.LA(1);

                         
                        int index180_125 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 124;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_125);
                        if ( s>=0 ) return s;
                        break;
                    case 152 : 
                        int LA180_92 = input.LA(1);

                         
                        int index180_92 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 169;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_92);
                        if ( s>=0 ) return s;
                        break;
                    case 153 : 
                        int LA180_396 = input.LA(1);

                         
                        int index180_396 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 151;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_396);
                        if ( s>=0 ) return s;
                        break;
                    case 154 : 
                        int LA180_395 = input.LA(1);

                         
                        int index180_395 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 151;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_395);
                        if ( s>=0 ) return s;
                        break;
                    case 155 : 
                        int LA180_90 = input.LA(1);

                         
                        int index180_90 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 169;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_90);
                        if ( s>=0 ) return s;
                        break;
                    case 156 : 
                        int LA180_377 = input.LA(1);

                         
                        int index180_377 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_377);
                        if ( s>=0 ) return s;
                        break;
                    case 157 : 
                        int LA180_499 = input.LA(1);

                         
                        int index180_499 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 151;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_499);
                        if ( s>=0 ) return s;
                        break;
                    case 158 : 
                        int LA180_500 = input.LA(1);

                         
                        int index180_500 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 151;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_500);
                        if ( s>=0 ) return s;
                        break;
                    case 159 : 
                        int LA180_162 = input.LA(1);

                        s = -1;
                        if ( (LA180_162=='m') ) {s = 238;}

                        else if ( (LA180_162=='M') ) {s = 239;}

                        else if ( ((LA180_162>='\u0000' && LA180_162<='\t')||LA180_162=='\u000B'||(LA180_162>='\u000E' && LA180_162<='/')||(LA180_162>='1' && LA180_162<='3')||LA180_162=='5'||(LA180_162>='7' && LA180_162<='L')||(LA180_162>='N' && LA180_162<='l')||(LA180_162>='n' && LA180_162<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_162=='0') ) {s = 240;}

                        else if ( (LA180_162=='4'||LA180_162=='6') ) {s = 241;}

                        if ( s>=0 ) return s;
                        break;
                    case 160 : 
                        int LA180_294 = input.LA(1);

                         
                        int index180_294 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_294);
                        if ( s>=0 ) return s;
                        break;
                    case 161 : 
                        int LA180_507 = input.LA(1);

                         
                        int index180_507 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_507);
                        if ( s>=0 ) return s;
                        break;
                    case 162 : 
                        int LA180_204 = input.LA(1);

                         
                        int index180_204 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_204);
                        if ( s>=0 ) return s;
                        break;
                    case 163 : 
                        int LA180_64 = input.LA(1);

                         
                        int index180_64 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_64);
                        if ( s>=0 ) return s;
                        break;
                    case 164 : 
                        int LA180_66 = input.LA(1);

                         
                        int index180_66 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_66);
                        if ( s>=0 ) return s;
                        break;
                    case 165 : 
                        int LA180_2 = input.LA(1);

                        s = -1;
                        if ( (LA180_2=='p') ) {s = 30;}

                        else if ( (LA180_2=='0') ) {s = 31;}

                        else if ( (LA180_2=='4'||LA180_2=='6') ) {s = 32;}

                        else if ( (LA180_2=='P') ) {s = 33;}

                        else if ( (LA180_2=='m') ) {s = 34;}

                        else if ( (LA180_2=='5'||LA180_2=='7') ) {s = 35;}

                        else if ( (LA180_2=='M') ) {s = 36;}

                        else if ( (LA180_2=='i') ) {s = 37;}

                        else if ( (LA180_2=='I') ) {s = 38;}

                        else if ( (LA180_2=='r') ) {s = 39;}

                        else if ( (LA180_2=='R') ) {s = 40;}

                        else if ( (LA180_2=='s') ) {s = 41;}

                        else if ( (LA180_2=='S') ) {s = 42;}

                        else if ( (LA180_2=='k') ) {s = 43;}

                        else if ( (LA180_2=='K') ) {s = 44;}

                        else if ( (LA180_2=='h') ) {s = 45;}

                        else if ( (LA180_2=='H') ) {s = 46;}

                        else if ( ((LA180_2>='\u0000' && LA180_2<='\t')||LA180_2=='\u000B'||(LA180_2>='\u000E' && LA180_2<='/')||(LA180_2>='1' && LA180_2<='3')||(LA180_2>='8' && LA180_2<='G')||LA180_2=='J'||LA180_2=='L'||(LA180_2>='N' && LA180_2<='O')||LA180_2=='Q'||(LA180_2>='T' && LA180_2<='g')||LA180_2=='j'||LA180_2=='l'||(LA180_2>='n' && LA180_2<='o')||LA180_2=='q'||(LA180_2>='t' && LA180_2<='\uFFFF')) ) {s = 12;}

                        if ( s>=0 ) return s;
                        break;
                    case 166 : 
                        int LA180_54 = input.LA(1);

                         
                        int index180_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_54);
                        if ( s>=0 ) return s;
                        break;
                    case 167 : 
                        int LA180_60 = input.LA(1);

                        s = -1;
                        if ( (LA180_60=='m') ) {s = 125;}

                        else if ( (LA180_60=='M') ) {s = 126;}

                        else if ( ((LA180_60>='\u0000' && LA180_60<='\t')||LA180_60=='\u000B'||(LA180_60>='\u000E' && LA180_60<='/')||(LA180_60>='1' && LA180_60<='3')||LA180_60=='5'||(LA180_60>='7' && LA180_60<='L')||(LA180_60>='N' && LA180_60<='l')||(LA180_60>='n' && LA180_60<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_60=='0') ) {s = 127;}

                        else if ( (LA180_60=='4'||LA180_60=='6') ) {s = 128;}

                        if ( s>=0 ) return s;
                        break;
                    case 168 : 
                        int LA180_57 = input.LA(1);

                         
                        int index180_57 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_57);
                        if ( s>=0 ) return s;
                        break;
                    case 169 : 
                        int LA180_347 = input.LA(1);

                         
                        int index180_347 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 73;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_347);
                        if ( s>=0 ) return s;
                        break;
                    case 170 : 
                        int LA180_496 = input.LA(1);

                         
                        int index180_496 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_496);
                        if ( s>=0 ) return s;
                        break;
                    case 171 : 
                        int LA180_497 = input.LA(1);

                         
                        int index180_497 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_497);
                        if ( s>=0 ) return s;
                        break;
                    case 172 : 
                        int LA180_192 = input.LA(1);

                         
                        int index180_192 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_192);
                        if ( s>=0 ) return s;
                        break;
                    case 173 : 
                        int LA180_295 = input.LA(1);

                         
                        int index180_295 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 129;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_295);
                        if ( s>=0 ) return s;
                        break;
                    case 174 : 
                        int LA180_219 = input.LA(1);

                         
                        int index180_219 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_219);
                        if ( s>=0 ) return s;
                        break;
                    case 175 : 
                        int LA180_220 = input.LA(1);

                         
                        int index180_220 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_220);
                        if ( s>=0 ) return s;
                        break;
                    case 176 : 
                        int LA180_191 = input.LA(1);

                         
                        int index180_191 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_191);
                        if ( s>=0 ) return s;
                        break;
                    case 177 : 
                        int LA180_210 = input.LA(1);

                         
                        int index180_210 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 124;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_210);
                        if ( s>=0 ) return s;
                        break;
                    case 178 : 
                        int LA180_63 = input.LA(1);

                        s = -1;
                        if ( (LA180_63=='m') ) {s = 130;}

                        else if ( (LA180_63=='M') ) {s = 131;}

                        else if ( (LA180_63=='s') ) {s = 132;}

                        else if ( (LA180_63=='0') ) {s = 133;}

                        else if ( (LA180_63=='4'||LA180_63=='6') ) {s = 134;}

                        else if ( (LA180_63=='S') ) {s = 135;}

                        else if ( ((LA180_63>='\u0000' && LA180_63<='\t')||LA180_63=='\u000B'||(LA180_63>='\u000E' && LA180_63<='/')||(LA180_63>='1' && LA180_63<='3')||(LA180_63>='8' && LA180_63<='L')||(LA180_63>='N' && LA180_63<='R')||(LA180_63>='T' && LA180_63<='l')||(LA180_63>='n' && LA180_63<='r')||(LA180_63>='t' && LA180_63<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_63=='5'||LA180_63=='7') ) {s = 136;}

                        if ( s>=0 ) return s;
                        break;
                    case 179 : 
                        int LA180_209 = input.LA(1);

                         
                        int index180_209 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 124;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_209);
                        if ( s>=0 ) return s;
                        break;
                    case 180 : 
                        int LA180_26 = input.LA(1);

                        s = -1;
                        if ( (LA180_26=='p') ) {s = 85;}

                        else if ( (LA180_26=='P') ) {s = 86;}

                        else if ( ((LA180_26>='\u0000' && LA180_26<='\t')||LA180_26=='\u000B'||(LA180_26>='\u000E' && LA180_26<='/')||(LA180_26>='1' && LA180_26<='3')||(LA180_26>='8' && LA180_26<='O')||(LA180_26>='Q' && LA180_26<='o')||(LA180_26>='q' && LA180_26<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_26=='0') ) {s = 87;}

                        else if ( (LA180_26=='5'||LA180_26=='7') ) {s = 88;}

                        else if ( (LA180_26=='4'||LA180_26=='6') ) {s = 89;}

                        if ( s>=0 ) return s;
                        break;
                    case 181 : 
                        int LA180_457 = input.LA(1);

                         
                        int index180_457 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_457);
                        if ( s>=0 ) return s;
                        break;
                    case 182 : 
                        int LA180_107 = input.LA(1);

                         
                        int index180_107 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 73;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_107);
                        if ( s>=0 ) return s;
                        break;
                    case 183 : 
                        int LA180_75 = input.LA(1);

                        s = -1;
                        if ( (LA180_75=='h') ) {s = 147;}

                        else if ( (LA180_75=='H') ) {s = 148;}

                        else if ( ((LA180_75>='\u0000' && LA180_75<='\t')||LA180_75=='\u000B'||(LA180_75>='\u000E' && LA180_75<='/')||(LA180_75>='1' && LA180_75<='3')||LA180_75=='5'||(LA180_75>='7' && LA180_75<='G')||(LA180_75>='I' && LA180_75<='g')||(LA180_75>='i' && LA180_75<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_75=='0') ) {s = 149;}

                        else if ( (LA180_75=='4'||LA180_75=='6') ) {s = 150;}

                        if ( s>=0 ) return s;
                        break;
                    case 184 : 
                        int LA180_454 = input.LA(1);

                         
                        int index180_454 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_454);
                        if ( s>=0 ) return s;
                        break;
                    case 185 : 
                        int LA180_453 = input.LA(1);

                         
                        int index180_453 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_453);
                        if ( s>=0 ) return s;
                        break;
                    case 186 : 
                        int LA180_355 = input.LA(1);

                         
                        int index180_355 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_355);
                        if ( s>=0 ) return s;
                        break;
                    case 187 : 
                        int LA180_389 = input.LA(1);

                         
                        int index180_389 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_389);
                        if ( s>=0 ) return s;
                        break;
                    case 188 : 
                        int LA180_354 = input.LA(1);

                         
                        int index180_354 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_354);
                        if ( s>=0 ) return s;
                        break;
                    case 189 : 
                        int LA180_473 = input.LA(1);

                         
                        int index180_473 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_473);
                        if ( s>=0 ) return s;
                        break;
                    case 190 : 
                        int LA180_388 = input.LA(1);

                         
                        int index180_388 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_388);
                        if ( s>=0 ) return s;
                        break;
                    case 191 : 
                        int LA180_366 = input.LA(1);

                         
                        int index180_366 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 116;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_366);
                        if ( s>=0 ) return s;
                        break;
                    case 192 : 
                        int LA180_472 = input.LA(1);

                         
                        int index180_472 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 156;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_472);
                        if ( s>=0 ) return s;
                        break;
                    case 193 : 
                        int LA180_359 = input.LA(1);

                         
                        int index180_359 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_359);
                        if ( s>=0 ) return s;
                        break;
                    case 194 : 
                        int LA180_360 = input.LA(1);

                         
                        int index180_360 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 108;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_360);
                        if ( s>=0 ) return s;
                        break;
                    case 195 : 
                        int LA180_144 = input.LA(1);

                         
                        int index180_144 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_144);
                        if ( s>=0 ) return s;
                        break;
                    case 196 : 
                        int LA180_142 = input.LA(1);

                         
                        int index180_142 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 221;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_142);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 180, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA177_eotS =
        "\12\uffff";
    static final String DFA177_eofS =
        "\12\uffff";
    static final String DFA177_minS =
        "\1\103\1\uffff\1\60\2\uffff\1\60\1\64\2\60\1\64";
    static final String DFA177_maxS =
        "\1\170\1\uffff\1\170\2\uffff\1\67\1\70\3\67";
    static final String DFA177_acceptS =
        "\1\uffff\1\1\1\uffff\1\2\1\3\5\uffff";
    static final String DFA177_specialS =
        "\12\uffff}>";
    static final String[] DFA177_transitionS = {
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

    static final short[] DFA177_eot = DFA.unpackEncodedString(DFA177_eotS);
    static final short[] DFA177_eof = DFA.unpackEncodedString(DFA177_eofS);
    static final char[] DFA177_min = DFA.unpackEncodedStringToUnsignedChars(DFA177_minS);
    static final char[] DFA177_max = DFA.unpackEncodedStringToUnsignedChars(DFA177_maxS);
    static final short[] DFA177_accept = DFA.unpackEncodedString(DFA177_acceptS);
    static final short[] DFA177_special = DFA.unpackEncodedString(DFA177_specialS);
    static final short[][] DFA177_transition;

    static {
        int numStates = DFA177_transitionS.length;
        DFA177_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA177_transition[i] = DFA.unpackEncodedString(DFA177_transitionS[i]);
        }
    }

    class DFA177 extends DFA {

        public DFA177(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 177;
            this.eot = DFA177_eot;
            this.eof = DFA177_eof;
            this.min = DFA177_min;
            this.max = DFA177_max;
            this.accept = DFA177_accept;
            this.special = DFA177_special;
            this.transition = DFA177_transition;
        }
        public String getDescription() {
            return "1138:17: ( X | T | C )";
        }
    }
    static final String DFA196_eotS =
        "\1\uffff\1\51\2\uffff\1\71\1\73\1\75\2\uffff\1\77\7\uffff\1\101"+
        "\1\103\4\uffff\1\104\1\uffff\1\35\1\uffff\2\35\1\uffff\5\35\2\uffff"+
        "\2\35\36\uffff\1\35\1\uffff\13\35\1\uffff\2\35\1\uffff\3\35\1\uffff"+
        "\3\35\10\uffff\1\35\1\uffff\16\35\1\u00a6\1\uffff\1\u00a6\4\35\1"+
        "\u00af\1\uffff\1\u00af\6\35\1\uffff\7\35\5\uffff\1\u00c6\1\uffff"+
        "\1\u00c6\21\35\1\uffff\2\u00a6\6\35\1\uffff\7\35\1\uffff\11\35\6"+
        "\uffff\2\u00c6\25\35\1\u00a6\6\35\1\u00af\4\35\2\u00af\12\35\7\uffff"+
        "\2\35\1\u00c6\22\35\1\u00a6\6\35\1\u00af\4\35\2\u00af\12\35\7\uffff"+
        "\2\35\1\u00c6\17\35\1\u00a6\5\35\1\u00af\3\35\2\u00af\7\35\10\uffff"+
        "\2\35\1\u00c6\6\35\1\u00a6\3\35\1\u00af\2\35\2\u00af\5\35\1\u0184"+
        "\4\uffff\1\35\1\u00c6\2\35\1\u00a6\1\u00af\3\35\2\uffff\1\u018b"+
        "\2\uffff\1\u00c6\1\35\5\uffff\1\u0192\3\uffff\1\u0195\2\uffff";
    static final String DFA196_eofS =
        "\u0196\uffff";
    static final String DFA196_minS =
        "\1\11\2\55\1\uffff\1\55\2\75\2\uffff\1\75\7\uffff\1\72\1\52\4\uffff"+
        "\1\60\1\uffff\1\116\1\0\1\117\1\116\1\uffff\1\116\1\117\1\116\2"+
        "\122\2\uffff\1\157\1\145\5\uffff\1\150\1\uffff\2\157\1\145\1\151"+
        "\2\uffff\1\60\20\uffff\1\114\1\0\1\114\2\116\1\117\1\60\1\61\1\117"+
        "\1\122\1\65\1\122\1\124\1\0\1\124\1\104\1\0\1\104\2\114\1\0\1\114"+
        "\1\155\1\147\2\uffff\1\160\1\164\1\146\1\147\1\60\1\71\1\131\1\0"+
        "\1\131\2\114\1\60\1\105\1\60\1\61\1\65\1\116\1\117\2\116\1\117\1"+
        "\122\1\55\1\0\1\55\2\124\1\60\1\106\1\55\1\0\1\55\2\104\1\60\1\105"+
        "\2\50\1\0\1\50\1\114\1\60\1\114\1\62\1\141\1\145\1\55\2\164\1\150"+
        "\1\60\1\55\1\0\1\55\2\131\1\60\1\103\1\60\1\105\2\114\1\60\1\61"+
        "\1\65\1\116\1\117\2\116\1\117\1\122\1\uffff\2\55\1\60\1\64\1\60"+
        "\1\106\2\124\1\uffff\1\60\1\64\1\60\1\105\2\104\1\160\1\uffff\1"+
        "\50\1\60\1\50\1\103\1\60\1\62\1\114\1\151\1\170\1\143\1\157\1\55"+
        "\1\164\1\60\1\uffff\2\55\1\60\1\71\1\60\1\103\2\131\1\60\1\105\2"+
        "\114\1\64\1\61\1\65\1\116\1\117\2\116\1\117\1\122\1\60\1\64\1\11"+
        "\1\60\1\106\2\124\1\60\1\64\1\11\1\60\1\105\2\104\2\11\1\162\1\60"+
        "\1\103\2\50\1\60\1\62\1\114\1\156\1\160\1\145\1\uffff\1\151\1\155"+
        "\1\142\1\55\1\64\1\60\1\71\1\11\1\60\1\103\2\131\1\64\1\105\2\114"+
        "\1\61\1\65\1\116\1\117\2\116\1\117\1\122\1\60\1\64\1\11\1\64\1\106"+
        "\2\124\1\60\1\64\1\11\1\64\1\105\2\104\2\11\1\145\1\60\1\103\2\50"+
        "\1\65\1\62\1\114\2\50\1\146\1\147\1\55\3\uffff\1\142\1\60\1\71\1"+
        "\11\1\64\1\103\2\131\1\105\2\114\1\116\1\117\2\116\1\117\1\122\1"+
        "\65\1\64\1\11\1\106\2\124\2\64\1\11\1\105\2\104\2\11\1\146\1\64"+
        "\1\103\2\50\1\62\1\114\2\uffff\1\164\1\150\1\143\3\uffff\1\65\1"+
        "\71\1\11\1\103\2\131\2\114\1\64\1\11\2\124\1\64\1\11\2\104\2\11"+
        "\1\151\1\103\2\50\1\114\1\55\1\164\1\145\1\uffff\1\151\1\71\1\11"+
        "\2\131\2\11\1\170\2\50\2\uffff\1\55\1\146\1\147\1\11\1\50\2\uffff"+
        "\1\164\1\150\1\uffff\1\55\1\164\2\uffff\1\55\2\uffff";
    static final String DFA196_maxS =
        "\2\uffff\1\164\1\uffff\1\uffff\2\75\2\uffff\1\75\7\uffff\1\72\1"+
        "\52\4\uffff\1\71\1\uffff\1\156\1\uffff\1\157\1\156\1\uffff\1\156"+
        "\1\157\1\156\2\162\2\uffff\1\157\1\145\5\uffff\1\157\1\uffff\2\157"+
        "\1\145\1\151\2\uffff\1\160\20\uffff\1\154\1\uffff\1\154\2\156\1"+
        "\157\1\67\1\146\1\157\1\162\1\65\1\162\1\164\1\uffff\1\164\1\144"+
        "\1\uffff\1\144\2\154\1\uffff\1\154\1\155\1\147\2\uffff\1\160\1\164"+
        "\1\146\1\147\1\67\1\145\1\171\1\uffff\1\171\2\154\1\66\1\145\1\67"+
        "\1\146\1\65\1\156\1\157\2\156\1\157\1\162\3\uffff\2\164\1\66\1\146"+
        "\3\uffff\2\144\1\66\1\145\1\55\1\50\1\uffff\1\50\1\154\1\67\1\154"+
        "\1\62\1\141\1\145\1\55\2\164\1\150\1\67\3\uffff\2\171\1\66\1\143"+
        "\1\66\1\145\2\154\1\67\1\146\1\65\1\156\1\157\2\156\1\157\1\162"+
        "\1\uffff\2\uffff\1\67\1\64\1\66\1\146\2\164\1\uffff\1\66\1\64\1"+
        "\66\1\145\2\144\1\160\1\uffff\1\50\1\66\1\50\1\143\1\67\1\62\1\154"+
        "\1\151\1\170\1\162\1\157\1\55\1\164\1\67\1\uffff\2\uffff\1\67\1"+
        "\71\1\66\1\143\2\171\1\66\1\145\2\154\1\67\1\146\1\65\1\156\1\157"+
        "\2\156\1\157\1\162\1\67\1\64\1\uffff\1\66\1\146\2\164\1\66\1\64"+
        "\1\uffff\1\66\1\145\2\144\2\uffff\1\162\1\66\1\143\2\50\1\67\1\62"+
        "\1\154\1\156\1\160\1\145\1\uffff\1\151\1\155\1\164\1\55\2\67\1\71"+
        "\1\uffff\1\66\1\143\2\171\1\66\1\145\2\154\1\146\1\65\1\156\1\157"+
        "\2\156\1\157\1\162\1\67\1\64\1\uffff\1\66\1\146\2\164\1\66\1\64"+
        "\1\uffff\1\66\1\145\2\144\2\uffff\1\145\1\66\1\143\2\50\1\67\1\62"+
        "\1\154\2\50\1\146\1\147\1\55\3\uffff\1\164\1\67\1\71\1\uffff\1\66"+
        "\1\143\2\171\1\145\2\154\1\156\1\157\2\156\1\157\1\162\1\67\1\64"+
        "\1\uffff\1\146\2\164\1\66\1\64\1\uffff\1\145\2\144\2\uffff\1\146"+
        "\1\66\1\143\2\50\1\62\1\154\2\uffff\1\164\1\150\1\162\3\uffff\1"+
        "\67\1\71\1\uffff\1\143\2\171\2\154\1\64\1\uffff\2\164\1\64\1\uffff"+
        "\2\144\2\uffff\1\151\1\143\2\50\1\154\1\55\1\164\1\145\1\uffff\1"+
        "\151\1\71\1\uffff\2\171\2\uffff\1\170\2\50\2\uffff\1\55\1\146\1"+
        "\147\1\uffff\1\50\2\uffff\1\164\1\150\1\uffff\1\55\1\164\2\uffff"+
        "\1\55\2\uffff";
    static final String DFA196_acceptS =
        "\3\uffff\1\3\3\uffff\1\7\1\10\1\uffff\1\12\1\13\1\14\1\15\1\16\1"+
        "\17\1\20\2\uffff\1\25\1\27\1\30\1\31\1\uffff\1\35\4\uffff\1\41\5"+
        "\uffff\1\52\1\74\2\uffff\1\101\1\102\1\1\1\42\1\2\1\uffff\1\51\4"+
        "\uffff\1\73\1\43\1\uffff\1\44\1\45\1\46\1\4\1\24\1\5\1\33\1\6\1"+
        "\34\1\11\1\26\1\22\1\21\1\103\1\23\1\32\30\uffff\1\47\1\50\107\uffff"+
        "\1\37\10\uffff\1\40\7\uffff\1\75\16\uffff\1\36\60\uffff\1\55\65"+
        "\uffff\1\65\1\66\1\67\46\uffff\1\77\1\100\3\uffff\1\70\1\71\1\72"+
        "\32\uffff\1\62\12\uffff\1\53\1\54\5\uffff\1\57\1\56\2\uffff\1\76"+
        "\2\uffff\1\60\1\61\1\uffff\1\64\1\63";
    static final String DFA196_specialS =
        "\32\uffff\1\0\53\uffff\1\6\13\uffff\1\2\2\uffff\1\4\3\uffff\1\3"+
        "\14\uffff\1\10\17\uffff\1\7\6\uffff\1\11\7\uffff\1\1\15\uffff\1"+
        "\5\u0102\uffff}>";
    static final String[] DFA196_transitionS = {
            "\1\47\1\50\2\uffff\1\50\22\uffff\1\47\1\43\1\30\1\1\1\10\2\uffff"+
            "\1\30\1\24\1\25\1\11\1\23\1\26\1\4\1\27\1\22\12\44\1\21\1\20"+
            "\1\3\1\17\1\12\1\uffff\1\2\1\40\14\35\1\37\1\36\5\35\1\42\5"+
            "\35\1\15\1\32\1\16\1\7\1\35\1\uffff\1\34\2\35\1\45\11\35\1\33"+
            "\1\31\2\35\1\46\2\35\1\41\5\35\1\13\1\6\1\14\1\5\1\uffff\uff80"+
            "\35",
            "\1\52\2\uffff\12\52\7\uffff\32\52\1\uffff\1\52\2\uffff\1\52"+
            "\1\uffff\32\52\5\uffff\uff80\52",
            "\1\62\22\uffff\1\53\10\uffff\1\63\3\uffff\1\66\1\67\1\uffff"+
            "\1\65\13\uffff\1\64\5\uffff\1\57\1\54\2\uffff\1\55\2\uffff\1"+
            "\63\2\uffff\1\60\1\66\1\67\1\uffff\1\65\1\uffff\1\61\1\uffff"+
            "\1\56",
            "",
            "\1\70\23\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35"+
            "\5\uffff\uff80\35",
            "\1\72",
            "\1\74",
            "",
            "",
            "\1\76",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\100",
            "\1\102",
            "",
            "",
            "",
            "",
            "\12\44",
            "",
            "\1\107\15\uffff\1\106\21\uffff\1\105",
            "\12\35\1\uffff\1\35\2\uffff\42\35\1\113\3\35\1\114\1\117\1"+
            "\114\1\117\26\35\1\115\1\111\5\35\1\120\30\35\1\112\1\110\5"+
            "\35\1\116\uff8a\35",
            "\1\123\14\uffff\1\122\22\uffff\1\121",
            "\1\126\15\uffff\1\125\21\uffff\1\124",
            "",
            "\1\107\15\uffff\1\106\21\uffff\1\105",
            "\1\123\14\uffff\1\122\22\uffff\1\121",
            "\1\126\15\uffff\1\125\21\uffff\1\124",
            "\1\130\11\uffff\1\131\25\uffff\1\127",
            "\1\130\11\uffff\1\131\25\uffff\1\132",
            "",
            "",
            "\1\133",
            "\1\134",
            "",
            "",
            "",
            "",
            "",
            "\1\135\6\uffff\1\136",
            "",
            "\1\137",
            "\1\140",
            "\1\141",
            "\1\142",
            "",
            "",
            "\1\143\3\uffff\1\144\1\65\1\144\1\65\21\uffff\1\63\3\uffff"+
            "\1\66\1\67\1\uffff\1\65\30\uffff\1\63\3\uffff\1\66\1\67\1\uffff"+
            "\1\65",
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
            "\1\147\17\uffff\1\146\17\uffff\1\145",
            "\12\35\1\uffff\1\35\2\uffff\42\35\1\152\3\35\1\153\1\35\1\153"+
            "\27\35\1\151\37\35\1\150\uff91\35",
            "\1\147\17\uffff\1\146\17\uffff\1\145",
            "\1\107\15\uffff\1\106\21\uffff\1\105",
            "\1\107\15\uffff\1\106\21\uffff\1\105",
            "\1\123\14\uffff\1\122\22\uffff\1\121",
            "\1\154\3\uffff\1\155\1\156\1\155\1\156",
            "\1\161\23\uffff\1\163\1\162\36\uffff\1\160\1\157",
            "\1\123\14\uffff\1\122\22\uffff\1\121",
            "\1\130\11\uffff\1\131\25\uffff\1\132",
            "\1\164",
            "\1\130\11\uffff\1\131\25\uffff\1\132",
            "\1\167\7\uffff\1\166\27\uffff\1\165",
            "\12\35\1\uffff\1\35\2\uffff\42\35\1\172\3\35\1\173\1\35\1\173"+
            "\30\35\1\171\37\35\1\170\uff90\35",
            "\1\167\7\uffff\1\166\27\uffff\1\165",
            "\1\176\27\uffff\1\175\7\uffff\1\174",
            "\12\35\1\uffff\1\35\2\uffff\42\35\1\u0081\3\35\1\u0082\1\35"+
            "\1\u0082\27\35\1\u0080\37\35\1\177\uff91\35",
            "\1\176\27\uffff\1\175\7\uffff\1\174",
            "\1\u0084\17\uffff\1\u0085\17\uffff\1\u0083",
            "\1\u0084\17\uffff\1\u0085\17\uffff\1\u0086",
            "\12\35\1\uffff\1\35\2\uffff\42\35\1\u0088\4\35\1\u008a\1\35"+
            "\1\u008a\32\35\1\u0089\37\35\1\u0087\uff8d\35",
            "\1\u0084\17\uffff\1\u0085\17\uffff\1\u0086",
            "\1\u008b",
            "\1\u008c",
            "",
            "",
            "\1\u008d",
            "\1\u008e",
            "\1\u008f",
            "\1\u0090",
            "\1\u0091\3\uffff\1\144\1\65\1\144\1\65",
            "\1\63\12\uffff\1\66\1\67\36\uffff\1\66\1\67",
            "\1\u0094\2\uffff\1\u0093\34\uffff\1\u0092",
            "\12\35\1\uffff\1\35\2\uffff\42\35\1\u0097\3\35\1\u0098\1\35"+
            "\1\u0098\25\35\1\u0096\37\35\1\u0095\uff93\35",
            "\1\u0094\2\uffff\1\u0093\34\uffff\1\u0092",
            "\1\147\17\uffff\1\146\17\uffff\1\145",
            "\1\147\17\uffff\1\146\17\uffff\1\145",
            "\1\u0099\3\uffff\1\u009a\1\uffff\1\u009a",
            "\1\u009c\37\uffff\1\u009b",
            "\1\u009d\3\uffff\1\u009e\1\u009f\1\u009e\1\u009f",
            "\1\u00a2\23\uffff\1\u00a4\1\u00a3\36\uffff\1\u00a1\1\u00a0",
            "\1\u00a5",
            "\1\107\15\uffff\1\106\21\uffff\1\105",
            "\1\123\14\uffff\1\122\22\uffff\1\121",
            "\1\126\15\uffff\1\125\21\uffff\1\124",
            "\1\107\15\uffff\1\106\21\uffff\1\105",
            "\1\123\14\uffff\1\122\22\uffff\1\121",
            "\1\130\11\uffff\1\131\25\uffff\1\132",
            "\1\35\2\uffff\12\35\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35"+
            "\1\uffff\32\35\5\uffff\uff80\35",
            "\12\35\1\uffff\1\35\2\uffff\42\35\1\u00a9\4\35\1\u00aa\1\35"+
            "\1\u00aa\34\35\1\u00a8\37\35\1\u00a7\uff8b\35",
            "\1\35\2\uffff\12\35\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35"+
            "\1\uffff\32\35\5\uffff\uff80\35",
            "\1\167\7\uffff\1\166\27\uffff\1\165",
            "\1\167\7\uffff\1\166\27\uffff\1\165",
            "\1\u00ab\3\uffff\1\u00ac\1\uffff\1\u00ac",
            "\1\u00ae\37\uffff\1\u00ad",
            "\1\35\2\uffff\12\35\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35"+
            "\1\uffff\32\35\5\uffff\uff80\35",
            "\12\35\1\uffff\1\35\2\uffff\42\35\1\u00b0\3\35\1\u00b1\1\35"+
            "\1\u00b1\uffc9\35",
            "\1\35\2\uffff\12\35\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35"+
            "\1\uffff\32\35\5\uffff\uff80\35",
            "\1\176\27\uffff\1\175\7\uffff\1\174",
            "\1\176\27\uffff\1\175\7\uffff\1\174",
            "\1\u00b2\3\uffff\1\u00b3\1\uffff\1\u00b3",
            "\1\u00b5\37\uffff\1\u00b4",
            "\1\u00b7\4\uffff\1\u00b6",
            "\1\u00b7",
            "\12\35\1\uffff\1\35\2\uffff\42\35\1\u00b9\3\35\1\u00bb\1\35"+
            "\1\u00bb\25\35\1\u00ba\37\35\1\u00b8\uff93\35",
            "\1\u00b7",
            "\1\u0084\17\uffff\1\u0085\17\uffff\1\u0086",
            "\1\u00bc\4\uffff\1\u00bd\1\uffff\1\u00bd",
            "\1\u0084\17\uffff\1\u0085\17\uffff\1\u0086",
            "\1\u00be",
            "\1\u00bf",
            "\1\u00c0",
            "\1\u00c1",
            "\1\u00c2",
            "\1\u00c3",
            "\1\u00c4",
            "\1\u00c5\3\uffff\1\144\1\65\1\144\1\65",
            "\1\35\2\uffff\12\35\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35"+
            "\1\uffff\32\35\5\uffff\uff80\35",
            "\12\35\1\uffff\1\35\2\uffff\42\35\1\u00c9\4\35\1\u00ca\1\35"+
            "\1\u00ca\41\35\1\u00c8\37\35\1\u00c7\uff86\35",
            "\1\35\2\uffff\12\35\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35"+
            "\1\uffff\32\35\5\uffff\uff80\35",
            "\1\u0094\2\uffff\1\u0093\34\uffff\1\u0092",
            "\1\u0094\2\uffff\1\u0093\34\uffff\1\u0092",
            "\1\u00cb\3\uffff\1\u00cc\1\uffff\1\u00cc",
            "\1\u00ce\37\uffff\1\u00cd",
            "\1\u00cf\3\uffff\1\u00d0\1\uffff\1\u00d0",
            "\1\u00d2\37\uffff\1\u00d1",
            "\1\147\17\uffff\1\146\17\uffff\1\145",
            "\1\147\17\uffff\1\146\17\uffff\1\145",
            "\1\u00d3\3\uffff\1\u00d4\1\u00d5\1\u00d4\1\u00d5",
            "\1\u00d8\23\uffff\1\u00da\1\u00d9\36\uffff\1\u00d7\1\u00d6",
            "\1\u00db",
            "\1\107\15\uffff\1\106\21\uffff\1\105",
            "\1\123\14\uffff\1\122\22\uffff\1\121",
            "\1\126\15\uffff\1\125\21\uffff\1\124",
            "\1\107\15\uffff\1\106\21\uffff\1\105",
            "\1\123\14\uffff\1\122\22\uffff\1\121",
            "\1\130\11\uffff\1\131\25\uffff\1\132",
            "",
            "\1\35\2\uffff\12\35\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35"+
            "\1\uffff\32\35\5\uffff\uff80\35",
            "\1\35\2\uffff\12\35\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35"+
            "\1\uffff\32\35\5\uffff\uff80\35",
            "\1\u00dc\4\uffff\1\u00dd\1\uffff\1\u00dd",
            "\1\u00de",
            "\1\u00df\3\uffff\1\u00e0\1\uffff\1\u00e0",
            "\1\u00e2\37\uffff\1\u00e1",
            "\1\167\7\uffff\1\166\27\uffff\1\165",
            "\1\167\7\uffff\1\166\27\uffff\1\165",
            "",
            "\1\u00e3\3\uffff\1\u00e4\1\uffff\1\u00e4",
            "\1\u00e5",
            "\1\u00e6\3\uffff\1\u00e7\1\uffff\1\u00e7",
            "\1\u00e9\37\uffff\1\u00e8",
            "\1\u00eb\27\uffff\1\175\7\uffff\1\u00ea",
            "\1\u00eb\27\uffff\1\175\7\uffff\1\u00ea",
            "\1\u00ec",
            "",
            "\1\u00b7",
            "\1\u00ed\3\uffff\1\u00ee\1\uffff\1\u00ee",
            "\1\u00b7",
            "\1\u00f0\37\uffff\1\u00ef",
            "\1\u00f1\4\uffff\1\u00f2\1\uffff\1\u00f2",
            "\1\u00f3",
            "\1\u0084\17\uffff\1\u0085\17\uffff\1\u0086",
            "\1\u00f4",
            "\1\u00f5",
            "\1\u00f7\10\uffff\1\u00f6\5\uffff\1\u00f8",
            "\1\u00f9",
            "\1\u00fa",
            "\1\u00fb",
            "\1\u00fc\3\uffff\1\144\1\65\1\144\1\65",
            "",
            "\1\35\2\uffff\12\35\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35"+
            "\1\uffff\32\35\5\uffff\uff80\35",
            "\1\35\2\uffff\12\35\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35"+
            "\1\uffff\32\35\5\uffff\uff80\35",
            "\1\u00fd\4\uffff\1\u00fe\1\uffff\1\u00fe",
            "\1\u00ff",
            "\1\u0100\3\uffff\1\u0101\1\uffff\1\u0101",
            "\1\u0103\37\uffff\1\u0102",
            "\1\u0094\2\uffff\1\u0093\34\uffff\1\u0092",
            "\1\u0094\2\uffff\1\u0093\34\uffff\1\u0092",
            "\1\u0104\3\uffff\1\u0105\1\uffff\1\u0105",
            "\1\u0107\37\uffff\1\u0106",
            "\1\147\17\uffff\1\146\17\uffff\1\145",
            "\1\147\17\uffff\1\146\17\uffff\1\145",
            "\1\u0108\1\u0109\1\u0108\1\u0109",
            "\1\u010c\23\uffff\1\u010e\1\u010d\36\uffff\1\u010b\1\u010a",
            "\1\u010f",
            "\1\107\15\uffff\1\106\21\uffff\1\105",
            "\1\123\14\uffff\1\122\22\uffff\1\121",
            "\1\126\15\uffff\1\125\21\uffff\1\124",
            "\1\107\15\uffff\1\106\21\uffff\1\105",
            "\1\123\14\uffff\1\122\22\uffff\1\121",
            "\1\130\11\uffff\1\131\25\uffff\1\132",
            "\1\u0110\4\uffff\1\u0111\1\uffff\1\u0111",
            "\1\u0112",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u0113\3\uffff\1\u0114\1\uffff\1\u0114",
            "\1\u0116\37\uffff\1\u0115",
            "\1\167\7\uffff\1\166\27\uffff\1\165",
            "\1\167\7\uffff\1\166\27\uffff\1\165",
            "\1\u0117\3\uffff\1\u0118\1\uffff\1\u0118",
            "\1\u0119",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u011a\3\uffff\1\u011b\1\uffff\1\u011b",
            "\1\u011d\37\uffff\1\u011c",
            "\1\u011f\27\uffff\1\175\7\uffff\1\u011e",
            "\1\u011f\27\uffff\1\175\7\uffff\1\u011e",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u0120",
            "\1\u0121\3\uffff\1\u0122\1\uffff\1\u0122",
            "\1\u0124\37\uffff\1\u0123",
            "\1\u00b7",
            "\1\u00b7",
            "\1\u0125\4\uffff\1\u0126\1\uffff\1\u0126",
            "\1\u0127",
            "\1\u0084\17\uffff\1\u0085\17\uffff\1\u0086",
            "\1\u0128",
            "\1\u0129",
            "\1\u012a",
            "",
            "\1\u012b",
            "\1\u012c",
            "\1\u012f\12\uffff\1\u012e\6\uffff\1\u012d",
            "\1\u0130",
            "\1\144\1\65\1\144\1\65",
            "\1\u0131\4\uffff\1\u0132\1\uffff\1\u0132",
            "\1\u0133",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u0134\3\uffff\1\u0135\1\uffff\1\u0135",
            "\1\u0137\37\uffff\1\u0136",
            "\1\u0094\2\uffff\1\u0093\34\uffff\1\u0092",
            "\1\u0094\2\uffff\1\u0093\34\uffff\1\u0092",
            "\1\u0138\1\uffff\1\u0138",
            "\1\u013a\37\uffff\1\u0139",
            "\1\147\17\uffff\1\146\17\uffff\1\145",
            "\1\147\17\uffff\1\146\17\uffff\1\145",
            "\1\u013d\23\uffff\1\u013f\1\u013e\36\uffff\1\u013c\1\u013b",
            "\1\u0140",
            "\1\107\15\uffff\1\106\21\uffff\1\105",
            "\1\123\14\uffff\1\122\22\uffff\1\121",
            "\1\126\15\uffff\1\125\21\uffff\1\124",
            "\1\107\15\uffff\1\106\21\uffff\1\105",
            "\1\123\14\uffff\1\122\22\uffff\1\121",
            "\1\130\11\uffff\1\131\25\uffff\1\132",
            "\1\u0141\4\uffff\1\u0142\1\uffff\1\u0142",
            "\1\u0143",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u0144\1\uffff\1\u0144",
            "\1\u0146\37\uffff\1\u0145",
            "\1\167\7\uffff\1\166\27\uffff\1\165",
            "\1\167\7\uffff\1\166\27\uffff\1\165",
            "\1\u0147\3\uffff\1\u0148\1\uffff\1\u0148",
            "\1\u0149",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u014a\1\uffff\1\u014a",
            "\1\u014c\37\uffff\1\u014b",
            "\1\u014e\27\uffff\1\175\7\uffff\1\u014d",
            "\1\u014e\27\uffff\1\175\7\uffff\1\u014d",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u014f",
            "\1\u0150\3\uffff\1\u0151\1\uffff\1\u0151",
            "\1\u0153\37\uffff\1\u0152",
            "\1\u00b7",
            "\1\u00b7",
            "\1\u0154\1\uffff\1\u0154",
            "\1\u0155",
            "\1\u0084\17\uffff\1\u0085\17\uffff\1\u0086",
            "\1\u0156",
            "\1\u0157",
            "\1\u0158",
            "\1\u0159",
            "\1\u015a",
            "",
            "",
            "",
            "\1\u015d\12\uffff\1\u015c\6\uffff\1\u015b",
            "\1\u015e\4\uffff\1\u015f\1\uffff\1\u015f",
            "\1\u0160",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u0161\1\uffff\1\u0161",
            "\1\u0163\37\uffff\1\u0162",
            "\1\u0094\2\uffff\1\u0093\34\uffff\1\u0092",
            "\1\u0094\2\uffff\1\u0093\34\uffff\1\u0092",
            "\1\u0165\37\uffff\1\u0164",
            "\1\147\17\uffff\1\146\17\uffff\1\145",
            "\1\147\17\uffff\1\146\17\uffff\1\145",
            "\1\107\15\uffff\1\106\21\uffff\1\105",
            "\1\123\14\uffff\1\122\22\uffff\1\121",
            "\1\126\15\uffff\1\125\21\uffff\1\124",
            "\1\107\15\uffff\1\106\21\uffff\1\105",
            "\1\123\14\uffff\1\122\22\uffff\1\121",
            "\1\130\11\uffff\1\131\25\uffff\1\132",
            "\1\u0166\1\uffff\1\u0166",
            "\1\u0167",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u0169\37\uffff\1\u0168",
            "\1\167\7\uffff\1\166\27\uffff\1\165",
            "\1\167\7\uffff\1\166\27\uffff\1\165",
            "\1\u016a\1\uffff\1\u016a",
            "\1\u016b",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u016d\37\uffff\1\u016c",
            "\1\u016f\27\uffff\1\175\7\uffff\1\u016e",
            "\1\u016f\27\uffff\1\175\7\uffff\1\u016e",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u0170",
            "\1\u0171\1\uffff\1\u0171",
            "\1\u0173\37\uffff\1\u0172",
            "\1\u00b7",
            "\1\u00b7",
            "\1\u0174",
            "\1\u0084\17\uffff\1\u0085\17\uffff\1\u0086",
            "",
            "",
            "\1\u0175",
            "\1\u0176",
            "\1\u0178\10\uffff\1\u0177\5\uffff\1\u0179",
            "",
            "",
            "",
            "\1\u017a\1\uffff\1\u017a",
            "\1\u017b",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u017d\37\uffff\1\u017c",
            "\1\u0094\2\uffff\1\u0093\34\uffff\1\u0092",
            "\1\u0094\2\uffff\1\u0093\34\uffff\1\u0092",
            "\1\147\17\uffff\1\146\17\uffff\1\145",
            "\1\147\17\uffff\1\146\17\uffff\1\145",
            "\1\u017e",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\167\7\uffff\1\166\27\uffff\1\165",
            "\1\167\7\uffff\1\166\27\uffff\1\165",
            "\1\u017f",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\176\27\uffff\1\175\7\uffff\1\174",
            "\1\176\27\uffff\1\175\7\uffff\1\174",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u0180",
            "\1\u0182\37\uffff\1\u0181",
            "\1\u00b7",
            "\1\u00b7",
            "\1\u0084\17\uffff\1\u0085\17\uffff\1\u0086",
            "\1\u0183",
            "\1\u0185",
            "\1\u0186",
            "",
            "\1\u0187",
            "\1\u0188",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u0094\2\uffff\1\u0093\34\uffff\1\u0092",
            "\1\u0094\2\uffff\1\u0093\34\uffff\1\u0092",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u0189",
            "\1\u00b7",
            "\1\u00b7",
            "",
            "",
            "\1\u018a",
            "\1\u018c",
            "\1\u018d",
            "\2\35\1\uffff\2\35\22\uffff\1\35\14\uffff\1\35\2\uffff\12\35"+
            "\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
            "\uff80\35",
            "\1\u018e",
            "",
            "",
            "\1\u018f",
            "\1\u0190",
            "",
            "\1\u0191",
            "\1\u0193",
            "",
            "",
            "\1\u0194",
            "",
            ""
    };

    static final short[] DFA196_eot = DFA.unpackEncodedString(DFA196_eotS);
    static final short[] DFA196_eof = DFA.unpackEncodedString(DFA196_eofS);
    static final char[] DFA196_min = DFA.unpackEncodedStringToUnsignedChars(DFA196_minS);
    static final char[] DFA196_max = DFA.unpackEncodedStringToUnsignedChars(DFA196_maxS);
    static final short[] DFA196_accept = DFA.unpackEncodedString(DFA196_acceptS);
    static final short[] DFA196_special = DFA.unpackEncodedString(DFA196_specialS);
    static final short[][] DFA196_transition;

    static {
        int numStates = DFA196_transitionS.length;
        DFA196_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA196_transition[i] = DFA.unpackEncodedString(DFA196_transitionS[i]);
        }
    }

    class DFA196 extends DFA {

        public DFA196(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 196;
            this.eot = DFA196_eot;
            this.eof = DFA196_eof;
            this.min = DFA196_min;
            this.max = DFA196_max;
            this.accept = DFA196_accept;
            this.special = DFA196_special;
            this.transition = DFA196_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__114 | GEN | CDO | CDC | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS | GREATER | LBRACE | RBRACE | LBRACKET | RBRACKET | OPEQ | SEMI | COLON | DCOLON | SOLIDUS | MINUS | PLUS | STAR | LPAREN | RPAREN | COMMA | DOT | TILDE | PIPE | STRING | ONLY | NOT | AND | IDENT | HASH | IMPORT_SYM | PAGE_SYM | MEDIA_SYM | NAMESPACE_SYM | CHARSET_SYM | COUNTER_STYLE_SYM | FONT_FACE_SYM | IMPORTANT_SYM | TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM | MOZ_DOCUMENT_SYM | NUMBER | URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP | WS | NL | COMMENT );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA196_26 = input.LA(1);

                        s = -1;
                        if ( (LA196_26=='o') ) {s = 72;}

                        else if ( (LA196_26=='O') ) {s = 73;}

                        else if ( (LA196_26=='n') ) {s = 74;}

                        else if ( (LA196_26=='0') ) {s = 75;}

                        else if ( (LA196_26=='4'||LA196_26=='6') ) {s = 76;}

                        else if ( (LA196_26=='N') ) {s = 77;}

                        else if ( (LA196_26=='u') ) {s = 78;}

                        else if ( (LA196_26=='5'||LA196_26=='7') ) {s = 79;}

                        else if ( (LA196_26=='U') ) {s = 80;}

                        else if ( ((LA196_26>='\u0000' && LA196_26<='\t')||LA196_26=='\u000B'||(LA196_26>='\u000E' && LA196_26<='/')||(LA196_26>='1' && LA196_26<='3')||(LA196_26>='8' && LA196_26<='M')||(LA196_26>='P' && LA196_26<='T')||(LA196_26>='V' && LA196_26<='m')||(LA196_26>='p' && LA196_26<='t')||(LA196_26>='v' && LA196_26<='\uFFFF')) ) {s = 29;}

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA196_133 = input.LA(1);

                        s = -1;
                        if ( (LA196_133=='l') ) {s = 184;}

                        else if ( (LA196_133=='0') ) {s = 185;}

                        else if ( (LA196_133=='L') ) {s = 186;}

                        else if ( ((LA196_133>='\u0000' && LA196_133<='\t')||LA196_133=='\u000B'||(LA196_133>='\u000E' && LA196_133<='/')||(LA196_133>='1' && LA196_133<='3')||LA196_133=='5'||(LA196_133>='7' && LA196_133<='K')||(LA196_133>='M' && LA196_133<='k')||(LA196_133>='m' && LA196_133<='\uFFFF')) ) {s = 29;}

                        else if ( (LA196_133=='4'||LA196_133=='6') ) {s = 187;}

                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA196_82 = input.LA(1);

                        s = -1;
                        if ( (LA196_82=='o') ) {s = 120;}

                        else if ( (LA196_82=='O') ) {s = 121;}

                        else if ( ((LA196_82>='\u0000' && LA196_82<='\t')||LA196_82=='\u000B'||(LA196_82>='\u000E' && LA196_82<='/')||(LA196_82>='1' && LA196_82<='3')||LA196_82=='5'||(LA196_82>='7' && LA196_82<='N')||(LA196_82>='P' && LA196_82<='n')||(LA196_82>='p' && LA196_82<='\uFFFF')) ) {s = 29;}

                        else if ( (LA196_82=='0') ) {s = 122;}

                        else if ( (LA196_82=='4'||LA196_82=='6') ) {s = 123;}

                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA196_89 = input.LA(1);

                        s = -1;
                        if ( (LA196_89=='r') ) {s = 135;}

                        else if ( (LA196_89=='0') ) {s = 136;}

                        else if ( (LA196_89=='R') ) {s = 137;}

                        else if ( ((LA196_89>='\u0000' && LA196_89<='\t')||LA196_89=='\u000B'||(LA196_89>='\u000E' && LA196_89<='/')||(LA196_89>='1' && LA196_89<='4')||LA196_89=='6'||(LA196_89>='8' && LA196_89<='Q')||(LA196_89>='S' && LA196_89<='q')||(LA196_89>='s' && LA196_89<='\uFFFF')) ) {s = 29;}

                        else if ( (LA196_89=='5'||LA196_89=='7') ) {s = 138;}

                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA196_85 = input.LA(1);

                        s = -1;
                        if ( (LA196_85=='n') ) {s = 127;}

                        else if ( (LA196_85=='N') ) {s = 128;}

                        else if ( ((LA196_85>='\u0000' && LA196_85<='\t')||LA196_85=='\u000B'||(LA196_85>='\u000E' && LA196_85<='/')||(LA196_85>='1' && LA196_85<='3')||LA196_85=='5'||(LA196_85>='7' && LA196_85<='M')||(LA196_85>='O' && LA196_85<='m')||(LA196_85>='o' && LA196_85<='\uFFFF')) ) {s = 29;}

                        else if ( (LA196_85=='0') ) {s = 129;}

                        else if ( (LA196_85=='4'||LA196_85=='6') ) {s = 130;}

                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA196_147 = input.LA(1);

                        s = -1;
                        if ( (LA196_147=='y') ) {s = 199;}

                        else if ( (LA196_147=='Y') ) {s = 200;}

                        else if ( ((LA196_147>='\u0000' && LA196_147<='\t')||LA196_147=='\u000B'||(LA196_147>='\u000E' && LA196_147<='/')||(LA196_147>='1' && LA196_147<='4')||LA196_147=='6'||(LA196_147>='8' && LA196_147<='X')||(LA196_147>='Z' && LA196_147<='x')||(LA196_147>='z' && LA196_147<='\uFFFF')) ) {s = 29;}

                        else if ( (LA196_147=='0') ) {s = 201;}

                        else if ( (LA196_147=='5'||LA196_147=='7') ) {s = 202;}

                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA196_70 = input.LA(1);

                        s = -1;
                        if ( (LA196_70=='n') ) {s = 104;}

                        else if ( (LA196_70=='N') ) {s = 105;}

                        else if ( ((LA196_70>='\u0000' && LA196_70<='\t')||LA196_70=='\u000B'||(LA196_70>='\u000E' && LA196_70<='/')||(LA196_70>='1' && LA196_70<='3')||LA196_70=='5'||(LA196_70>='7' && LA196_70<='M')||(LA196_70>='O' && LA196_70<='m')||(LA196_70>='o' && LA196_70<='\uFFFF')) ) {s = 29;}

                        else if ( (LA196_70=='0') ) {s = 106;}

                        else if ( (LA196_70=='4'||LA196_70=='6') ) {s = 107;}

                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA196_118 = input.LA(1);

                        s = -1;
                        if ( (LA196_118=='t') ) {s = 167;}

                        else if ( (LA196_118=='T') ) {s = 168;}

                        else if ( ((LA196_118>='\u0000' && LA196_118<='\t')||LA196_118=='\u000B'||(LA196_118>='\u000E' && LA196_118<='/')||(LA196_118>='1' && LA196_118<='4')||LA196_118=='6'||(LA196_118>='8' && LA196_118<='S')||(LA196_118>='U' && LA196_118<='s')||(LA196_118>='u' && LA196_118<='\uFFFF')) ) {s = 29;}

                        else if ( (LA196_118=='0') ) {s = 169;}

                        else if ( (LA196_118=='5'||LA196_118=='7') ) {s = 170;}

                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA196_102 = input.LA(1);

                        s = -1;
                        if ( (LA196_102=='l') ) {s = 149;}

                        else if ( (LA196_102=='L') ) {s = 150;}

                        else if ( ((LA196_102>='\u0000' && LA196_102<='\t')||LA196_102=='\u000B'||(LA196_102>='\u000E' && LA196_102<='/')||(LA196_102>='1' && LA196_102<='3')||LA196_102=='5'||(LA196_102>='7' && LA196_102<='K')||(LA196_102>='M' && LA196_102<='k')||(LA196_102>='m' && LA196_102<='\uFFFF')) ) {s = 29;}

                        else if ( (LA196_102=='0') ) {s = 151;}

                        else if ( (LA196_102=='4'||LA196_102=='6') ) {s = 152;}

                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA196_125 = input.LA(1);

                        s = -1;
                        if ( ((LA196_125>='\u0000' && LA196_125<='\t')||LA196_125=='\u000B'||(LA196_125>='\u000E' && LA196_125<='/')||(LA196_125>='1' && LA196_125<='3')||LA196_125=='5'||(LA196_125>='7' && LA196_125<='\uFFFF')) ) {s = 29;}

                        else if ( (LA196_125=='0') ) {s = 176;}

                        else if ( (LA196_125=='4'||LA196_125=='6') ) {s = 177;}

                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 196, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA199_eotS =
        "\12\uffff";
    static final String DFA199_eofS =
        "\12\uffff";
    static final String DFA199_minS =
        "\1\103\1\uffff\1\60\2\uffff\1\60\1\64\2\60\1\64";
    static final String DFA199_maxS =
        "\1\170\1\uffff\1\170\2\uffff\1\67\1\70\3\67";
    static final String DFA199_acceptS =
        "\1\uffff\1\1\1\uffff\1\2\1\3\5\uffff";
    static final String DFA199_specialS =
        "\12\uffff}>";
    static final String[] DFA199_transitionS = {
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

    static final short[] DFA199_eot = DFA.unpackEncodedString(DFA199_eotS);
    static final short[] DFA199_eof = DFA.unpackEncodedString(DFA199_eofS);
    static final char[] DFA199_min = DFA.unpackEncodedStringToUnsignedChars(DFA199_minS);
    static final char[] DFA199_max = DFA.unpackEncodedStringToUnsignedChars(DFA199_maxS);
    static final short[] DFA199_accept = DFA.unpackEncodedString(DFA199_acceptS);
    static final short[] DFA199_special = DFA.unpackEncodedString(DFA199_specialS);
    static final short[][] DFA199_transition;

    static {
        int numStates = DFA199_transitionS.length;
        DFA199_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA199_transition[i] = DFA.unpackEncodedString(DFA199_transitionS[i]);
        }
    }

    class DFA199 extends DFA {

        public DFA199(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 199;
            this.eot = DFA199_eot;
            this.eof = DFA199_eof;
            this.min = DFA199_min;
            this.max = DFA199_max;
            this.accept = DFA199_accept;
            this.special = DFA199_special;
            this.transition = DFA199_transition;
        }
        public String getDescription() {
            return "1136:17: ( X | T | C )";
        }
    }
 

}