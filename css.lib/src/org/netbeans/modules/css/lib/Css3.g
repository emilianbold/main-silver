//  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// 
//  Copyright 2011 Oracle and/or its affiliates. All rights reserved.
// 
//  Oracle and Java are registered trademarks of Oracle and/or its affiliates.
//  Other names may be trademarks of their respective owners.
// 
//  The contents of this file are subject to the terms of either the GNU
//  General Public License Version 2 only ("GPL") or the Common
//  Development and Distribution License("CDDL") (collectively, the
//  "License"). You may not use this file except in compliance with the
//  License. You can obtain a copy of the License at
//  http://www.netbeans.org/cddl-gplv2.html
//  or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
//  specific language governing permissions and limitations under the
//  License.  When distributing the software, include this License Header
//  Notice in each file and include the License file at
//  nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
//  particular file as subject to the "Classpath" exception as provided
//  by Oracle in the GPL Version 2 section of the License file that
//  accompanied this code. If applicable, add the following below the
//  License Header, with the fields enclosed by brackets [] replaced by
//  your own identifying information:
//  "Portions Copyrighted [year] [name of copyright owner]"
// 
//  If you wish your version of this file to be governed by only the CDDL
//  or only the GPL Version 2, indicate your decision by adding
//  "[Contributor] elects to include this software in this distribution
//  under the [CDDL or GPL Version 2] license." If you do not indicate a
//  single choice of license, a recipient has the option to distribute
//  your version of this file under either the CDDL, the GPL Version 2 or
//  to extend the choice of license to its licensees as provided above.
//  However, if you add GPL Version 2 code and therefore, elected the GPL
//  Version 2 license, then the option applies only if the new code is
//  made subject to such option by the copyright holder.
// 
//  Contributor(s):
// 
//  Portions Copyrighted 2011 Sun Microsystems, Inc.
//
// A complete lexer and grammar for CSS 2.1 as defined by the
// W3 specification.
//
// This grammar is free to use providing you retain everyhting in this header comment
// section.
//
// Author      : Jim Idle, Temporal Wave LLC.
// Contact     : jimi@temporal-wave.com
// Website     : http://www.temporal-wave.com
// License     : ANTLR Free BSD License
//
// Please visit our Web site at http://www.temporal-wave.com and try our commercial
// parsers for SQL, C#, VB.Net and more.
//
// This grammar is free to use providing you retain everything in this header comment
// section.
//

//Modifications to the original css21 source file by Jim Idle have been done to fulfill the 
//css3 parsing rules and making the parser more error prone.
//1) incorporated the grammar changes from selectors module: http://www.w3.org/TR/css3-selectors/#grammar
//      a. There's no 'universal' selector node, 'typeSelector' is used instead where instead of the identifier there's the star token.
//         This solves the too long (==3) lookahead problem in the simpleSelectorSequence rule
//2) implemented custom error recovery
//3) removed whitespaces from the alpha token fragments
//
//Author: Marek Fukala (mfukala@netbeans.org)
//Please be aware that the grammar doesn't properly and fully reflect the whole css3 specification!!!

grammar Css3;

//options {
//	output=AST;
//}

@header {
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
    
}

@members {
/**
     * Use the current stacked followset to work out the valid tokens that
     * can follow on from the current point in the parse, then recover by
     * eating tokens that are not a member of the follow set we compute.
     *
     * This method is used whenever we wish to force a sync, even though
     * the parser has not yet checked LA(1) for alt selection. This is useful
     * in situations where only a subset of tokens can begin a new construct
     * (such as the start of a new statement in a block) and we want to
     * proactively detect garbage so that the current rule does not exit on
     * on an exception.
     *
     * We could override recover() to make this the default behavior but that
     * is too much like using a sledge hammer to crack a nut. We want finer
     * grained control of the recovery and error mechanisms.
     */
    protected void syncToSet()
    {
        // Compute the followset that is in context wherever we are in the
        // rule chain/stack
        //
         BitSet follow = state.following[state._fsp]; //computeContextSensitiveRuleFOLLOW();

         syncToSet(follow);
    }

    protected void syncToSet(BitSet follow)
    {
        int mark = -1;

        //create error-recovery node
        dbg.enterRule(getGrammarFileName(), "recovery");

        try {

            mark = input.mark();

            // Consume all tokens in the stream until we find a member of the follow
            // set, which means the next production should be guaranteed to be happy.
            //
            while (! follow.member(input.LA(1)) ) {

                if  (input.LA(1) == Token.EOF) {

                    // Looks like we didn't find anything at all that can help us here
                    // so we need to rewind to where we were and let normal error handling
                    // bail out.
                    //
                    input.rewind();
                    mark = -1;
                    return;
                }
                input.consume();

                // Now here, because you are consuming some tokens, yu will probably want
                // to raise an error message such as "Spurious elements after the class member were discarded"
                // using whatever your override of displayRecognitionError() routine does to record
                // error messages. The exact error my depend on context etc.
                //
            }
        } catch (Exception e) {

          // Just ignore any errors here, we will just let the recognizer
          // try to resync as normal - something must be very screwed.
          //
        }
        finally {
            dbg.exitRule(getGrammarFileName(), "recovery");

            // Always release the mark we took
            //
            if  (mark != -1) {
                input.release(mark);
            }
        }
    }
    
    /**
         * synces to next RBRACE "}" taking nesting into account
         */
        protected void syncToRBRACE(int nest)
            {
                
                int mark = -1;
                //create error-recovery node
                dbg.enterRule(getGrammarFileName(), "recovery");

                try {
                    mark = input.mark();
                    for(;;) {
                        //read char
                        int c = input.LA(1);
                        
                        switch(c) {
                            case Token.EOF:
                                input.rewind();
                                mark = -1;
                                return ;
                            case Css3Lexer.LBRACE:
                                nest++;
                                break;
                            case Css3Lexer.RBRACE:
                                nest--;
                                if(nest == 0) {
                                    //do not eat the final RBRACE
                                    return ;
                                }
                        }
                        
                        input.consume();
                                            
                    }

                } catch (Exception e) {

                  // Just ignore any errors here, we will just let the recognizer
                  // try to resync as normal - something must be very screwed.
                  //
                }
                finally {
                    if  (mark != -1) {
                        input.release(mark);
                    }
                    dbg.exitRule(getGrammarFileName(), "recovery");
                }
            }
    
}

@lexer::header {
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
}

// -------------
// Main rule.   This is the main entry rule for the parser, the top level
//              grammar rule.
//
// A style sheet consists of an optional character set specification, an optional series
// of imports, and then the main body of style rules.
//
styleSheet  
    :   charSet?
    	WS*
        (imports WS*)*  
        (namespace WS*)*
        bodylist
     EOF
    ;

namespace
  : NAMESPACE_SYM WS* (namespace_prefix WS*)? (resourceIdentifier) WS* ';'
  ;

namespace_prefix
  : IDENT
  ;
    
resourceIdentifier
  : STRING|URI
  ;

// -----------------
// Character set.   Picks up the user specified character set, should it be present.
//
charSet
    :   CHARSET_SYM WS* STRING WS* SEMI
    ;

// ---------
// Import.  Location of an external style sheet to include in the ruleset.
//
imports
    :   IMPORT_SYM WS* (resourceIdentifier) WS* media_query_list SEMI
    ;

// ---------
// Media.   Introduce a set of rules that are to be used if the consumer indicates
//          it belongs to the signified medium.
//
media
    : MEDIA_SYM WS* media_query_list
        LBRACE WS*
            ( ( ruleSet | page ) WS*)*
         RBRACE
    ;

//mediaList
//        : medium (COMMA WS* medium)*
//	;

media_query_list
 : ( media_query ( COMMA WS* media_query )* )?
 ;
 
media_query
 : ((ONLY | NOT) WS* )?  media_type WS* ( AND WS* media_expression )*
 | media_expression ( AND WS* media_expression )*
 ;
 
media_type
 : IDENT | GEN
 ;
 
media_expression
 : '(' WS* media_feature WS* ( ':' WS* expr )? ')' WS*
 ;
media_feature
 : IDENT
 ;

// ---------    
// Medium.  The name of a medim that are particulare set of rules applies to.
//
medium
    : ( IDENT | GEN ) WS*
    ;
    

bodylist
    : bodyset*
    ;
    
bodyset
    : (
    	ruleSet
        | media
        | page
        | counterStyle
        | fontFace
        | moz_document
        | generic_at_rule
      )
      WS*
    ;

//    	catch[ RecognitionException rce] {
//        reportError(rce);
//        syncToRBRACE(0); //nesting aware, initial nest == 0
//        input.consume(); //consume the RBRACE as well
//        }
    
    
generic_at_rule
    : GENERIC_AT_RULE WS* ( ( IDENT | STRING ) WS* )? 
        LBRACE 
        	syncTo_RBRACE
        RBRACE
	;    
moz_document
	: 
	MOZ_DOCUMENT_SYM WS* ( moz_document_function WS*) ( COMMA WS* moz_document_function WS* )*
	LBRACE WS*
	 ( ( ruleSet | page ) WS*)*
	RBRACE
	;

moz_document_function
	:
	URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP
	;
    
page
    : PAGE_SYM WS* ( IDENT WS* )? (pseudoPage WS*)?
        LBRACE WS*
            //the grammar in the http://www.w3.org/TR/css3-page/ says the declaration/margins should be delimited by the semicolon,
            //but there's no such char in the examples => making it arbitrary
            //the original rule:
            //( declaration | margin WS*)? ( SEMI WS* (declaration | margin WS*)? )* 
            (declaration|margin WS*)? (SEMI WS* (declaration|margin WS*)?)*
        RBRACE
    ;
    
counterStyle
    : COUNTER_STYLE_SYM WS* IDENT WS*
        LBRACE WS* syncTo_IDENT_RBRACE
		declarations
        RBRACE
    ;
    
fontFace
    : FONT_FACE_SYM WS*
        LBRACE WS* syncTo_IDENT_RBRACE
		declarations
        RBRACE
    ;
    

margin	
	: margin_sym WS* LBRACE WS* syncTo_IDENT_RBRACE declarations RBRACE
       ;
       
margin_sym 
	:
       TOPLEFTCORNER_SYM | 
       TOPLEFT_SYM | 
       TOPCENTER_SYM | 
       TOPRIGHT_SYM | 
       TOPRIGHTCORNER_SYM |
       BOTTOMLEFTCORNER_SYM | 
       BOTTOMLEFT_SYM | 
       BOTTOMCENTER_SYM | 
       BOTTOMRIGHT_SYM |
       BOTTOMRIGHTCORNER_SYM |
       LEFTTOP_SYM |
       LEFTMIDDLE_SYM |
       LEFTBOTTOM_SYM |
       RIGHTTOP_SYM |
       RIGHTMIDDLE_SYM |
       RIGHTBOTTOM_SYM 
       ;
    
pseudoPage
    : COLON IDENT
    ;
    
operator
    : SOLIDUS WS*
    | COMMA WS*
    |
    ;
    
combinator
    : PLUS WS*
    | GREATER WS*
    | TILDE WS*//use this rule preferably
    | 
    ;
    
unaryOperator
    : MINUS
    | PLUS
    ;  
    
property
    : (IDENT | GEN) WS*
    ;
    
ruleSet 
    :   selectorsGroup
        LBRACE WS* syncTo_IDENT_RBRACE
            declarations
        RBRACE
    ;
    	catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(RBRACE));
        input.consume(); //consume the RBRACE as well
        }
    
declarations
    :
        //Allow empty rule. Allows multiple semicolons
        declaration? (SEMI WS* declaration?)*
    ;
    
selectorsGroup
    :	selector (COMMA WS* selector)*
    ;
    
selector
    : simpleSelectorSequence (combinator simpleSelectorSequence)*
    ;
 

simpleSelectorSequence
	/* typeSelector and universal are ambiguous for lookahead==1 since namespace name and element name both starts with IDENT */
	:   
//	(  ( typeSelector | universal ) ((esPred)=>elementSubsequent)* )
	
        //using typeSelector even for the universal selector since the lookahead would have to be 3 (IDENT PIPE (IDENT|STAR) :-(
	(  typeSelector ((esPred)=>elementSubsequent)* )
	| 
	( ((esPred)=>elementSubsequent)+ )
	;
	catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(LBRACE)); 
    }
    
/*simpleSelector
    : elementName 
        ((esPred)=>elementSubsequent)*
        
    | ((esPred)=>elementSubsequent)+
    ;
 */
   
typeSelector 
	options { k = 2; }
 	:  ((nsPred)=>namespace_wqname_prefix)? ( elementName WS* )
 	;
 	 	 
 nsPred
 	:	
 	(IDENT | STAR)? PIPE
 	;
    
 /*
qname_prefix
  : ( namespace_prefix WS*)?  PIPE
  ;
*/
      
 namespace_wqname_prefix
  : ( namespace_prefix WS*)?  PIPE
   | namespace_wildcard_prefix WS* PIPE
  ;  
  
namespace_wildcard_prefix
  	:	
  	STAR
  	;
       
esPred
    : '#' | HASH | DOT | LBRACKET | COLON | DCOLON
    ;
    
elementSubsequent
    : 
    (
    	cssId
    	| cssClass
        | attrib
        | pseudo
    )
    WS*
    ;
    
//Error Recovery: Allow the parser to enter the cssId rule even if there's just hash char.
cssId
    : HASH | ( '#' NAME )
    ;
    catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(WS, IDENT, LBRACE)); 
    }

cssClass
    : DOT ( IDENT | GEN  )
    ;
    catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(WS, IDENT, LBRACE)); 
    }
    
//using typeSelector even for the universal selector since the lookahead would have to be 3 (IDENT PIPE (IDENT|STAR) :-(
elementName
    : ( IDENT | GEN ) | '*'
    ;
    
attrib
    : LBRACKET
    	namespace_wqname_prefix? WS*
        attrib_name WS*
        
            (
                (
                      OPEQ
                    | INCLUDES
                    | DASHMATCH
                    | BEGINS
                    | ENDS
                    | CONTAINS
                )
                WS*
                attrib_value
                WS*
            )?
    
      RBRACKET
;
catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
    }

syncTo_IDENT_RBRACKET_LBRACE
    @init {
        syncToSet(BitSet.of(IDENT, RBRACKET, LBRACE));
    }
    	:	
    	;

//bit similar naming to attrvalue, attrname - but these are different - for functions
attrib_name
	: IDENT
	;
	
attrib_value
	: 
	(
  	      IDENT
              | STRING
        )
        ;

pseudo
    : ( COLON | DCOLON )
             (
                ( 
                    ( IDENT | GEN )
                    ( // Function
                        WS* LPAREN WS* ( expr | '*' )? RPAREN
                    )?
                )
                |
                ( NOT WS* LPAREN WS* simpleSelectorSequence? RPAREN )
             )
    ;

declaration
    : 
    //syncToIdent //recovery: this will sync the parser the identifier (property) if there's a gargabe in front of it
    property COLON WS* expr prio?
    ;
    catch[ RecognitionException rce] {
        reportError(rce);
        //recovery: if an mismatched token occures inside a declaration is found,
        //then skip all tokens until an end of the rule is found represented by right curly brace
        consumeUntil(input, BitSet.of(SEMI, RBRACE)); 
    }

//recovery: syncs the parser to the first identifier in the token input stream or the closing curly bracket
//since the rule matches epsilon it will always be entered
syncTo_IDENT_RBRACE
    @init {
        syncToSet(BitSet.of(IDENT, RBRACE));
    }
    	:	
    	;
    	
syncTo_RBRACE
    @init {
        syncToRBRACE(1); //initial nest == 1
    }
    	:	
    	;    	

//synct to computed follow set in the rule
syncToFollow
    @init {
        syncToSet();
    }
    	:	
    	;
    
    
prio
    : IMPORTANT_SYM WS*
    ;
    
expr
    : term (operator term)*
    ;
    
term
    : unaryOperator?
        (
        (
              NUMBER
            | PERCENTAGE
            | LENGTH
            | EMS
            | EXS
            | ANGLE
            | TIME
            | FREQ
            | RESOLUTION
            | DIMENSION     //so we can match expression like a:nth-child(3n+1) -- the "3n" is lexed as dimension
        )
    | STRING
    | IDENT
    | GEN
    | URI
    | hexColor
    | function
    )
    WS*
    ;

function
	: 	function_name WS*
		LPAREN WS*
		( 
			expr
		| 
		  	(
				attribute (COMMA WS* attribute )*				
			) 
		)
		RPAREN
	;
catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 
}
    
function_name
        //css spec allows here just IDENT, 
        //but due to some nonstandart MS extension like progid:DXImageTransform.Microsoft.gradien
        //the function name can be a bit more complicated
	: (IDENT COLON)? IDENT (DOT IDENT)*
    	;
    	
attribute
	: attrname WS* OPEQ WS* attrvalue
	;
    
attrname
	: IDENT (DOT IDENT)*
	;
	
attrvalue
	: expr
	;
    
hexColor
    : HASH
    ;
    
// ==============================================================
// LEXER
//
// The lexer follows the normative section of WWW standard as closely
// as it can. For instance, where the ANTLR lexer returns a token that
// is unambiguous for both ANTLR and lex (the standard defines tokens
// in lex notation), then the token names are equivalent.
//
// Note however that lex has a match order defined as top to bottom 
// with longest match first. This results in a fairly inefficent, match,
// REJECT, match REJECT set of operations. ANTLR lexer grammars are actaully
// LL grammars (and hence LL recognizers), which means that we must
// specifically disambiguate longest matches and so on, when the lex
// like normative grammar results in ambiguities as far as ANTLR is concerned.
//
// This means that some tokens will either be combined compared to the
// normative spec, and the paresr will recognize them for what they are.
// In this case, the token will named as XXX_YYY where XXX and YYY are the
// token names used in the specification.
//
// Lex style macro names used in the spec may sometimes be used (in upper case
// version) as fragment rules in this grammar. However ANTLR fragment rules
// are not quite the same as lex macros, in that they generate actual 
// methods in the recognizer class, and so may not be as effecient. In
// some cases then, the macro contents are embedded. Annotation indicate when
// this is the case.
//
// See comments in the rules for specific details.
// --------------------------------------------------------------
//
// N.B. CSS 2.1 is defined as case insensitive, but because each character
//      is allowed to be written as in escaped form we basically define each
//      character as a fragment and reuse it in all other rules.
// ==============================================================


// --------------------------------------------------------------
// Define all the fragments of the lexer. These rules neither recognize
// nor create tokens, but must be called from non-fragment rules, which
// do create tokens, using these fragments to either purely define the
// token number, or by calling them to match a certain portion of
// the token string.
//

GEN                     : '@@@';

fragment    HEXCHAR     : ('a'..'f'|'A'..'F'|'0'..'9')  ;

fragment    NONASCII    : '\u0080'..'\uFFFF'            ;   // NB: Upper bound should be \u4177777

fragment    UNICODE     : '\\' HEXCHAR 
                                (HEXCHAR 
                                    (HEXCHAR 
                                        (HEXCHAR 
                                            (HEXCHAR HEXCHAR?)?
                                        )?
                                    )?
                                )? 
                                ('\r'|'\n'|'\t'|'\f'|' ')*  ;
                                
fragment    ESCAPE      : UNICODE | '\\' ~('\r'|'\n'|'\f'|HEXCHAR)  ;

fragment    NMSTART     : '_'
                        | 'a'..'z'
                        | 'A'..'Z'
                        | NONASCII
                        | ESCAPE
                        ;

fragment    NMCHAR      : '_'
                        | 'a'..'z'
                        | 'A'..'Z'
                        | '0'..'9'
                        | '-'
                        | NONASCII
                        | ESCAPE
                        ;
                        
fragment    NAME        : NMCHAR+   ;

fragment    URL         : ( 
                              '['|'!'|'#'|'$'|'%'|'&'|'*'|'~'|'.'|':'|'/'
                            | NMCHAR
                          )*
                        ;

                        
// Basic Alpha characters in upper, lower and escaped form. 

fragment    A   :   ('a'|'A')     
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'1'
                ;
fragment    B   :   ('b'|'B')     
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'2'
                ;
fragment    C   :   ('c'|'C')     
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'3'
                ;
fragment    D   :   ('d'|'D')     
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'4'
                ;
fragment    E   :   ('e'|'E')     
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'5'
                ;
fragment    F   :   ('f'|'F')     
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'6'
                ;
fragment    G   :   ('g'|'G')  
                |   '\\'
                        (
                              'g'
                            | 'G'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'7'
                        )
                ;
fragment    H   :   ('h'|'H')  
                | '\\' 
                        (
                              'h'
                            | 'H'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'8'
                        )   
                ;
fragment    I   :   ('i'|'I')  
                | '\\' 
                        (
                              'i'
                            | 'I'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'9'
                        )
                ;
fragment    J   :   ('j'|'J')  
                | '\\' 
                        (
                              'j'
                            | 'J'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('A'|'a')
                        )   
                ;
fragment    K   :   ('k'|'K')  
                | '\\' 
                        (
                              'k'
                            | 'K'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('B'|'b')
                        )   
                ;
fragment    L   :   ('l'|'L')  
                | '\\' 
                        (
                              'l'
                            | 'L'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('C'|'c')
                        )   
                ;
fragment    M   :   ('m'|'M')  
                | '\\' 
                        (
                              'm'
                            | 'M'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('D'|'d')
                        )   
                ;
fragment    N   :   ('n'|'N')  
                | '\\' 
                        (
                              'n'
                            | 'N'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('E'|'e')
                        )   
                ;
fragment    O   :   ('o'|'O')  
                | '\\' 
                        (
                              'o'
                            | 'O'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('F'|'f')
                        )   
                ;
fragment    P   :   ('p'|'P')  
                | '\\'
                        (
                              'p'
                            | 'P'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('0')
                        )   
                ;
fragment    Q   :   ('q'|'Q')  
                | '\\' 
                        (
                              'q'
                            | 'Q'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('1')
                        )   
                ;
fragment    R   :   ('r'|'R')  
                | '\\' 
                        (
                              'r'
                            | 'R'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('2')
                        )   
                ;
fragment    S   :   ('s'|'S')  
                | '\\' 
                        (
                              's'
                            | 'S'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('3')
                        )   
                ;
fragment    T   :   ('t'|'T')  
                | '\\' 
                        (
                              't'
                            | 'T'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('4')
                        )   
                ;
fragment    U   :   ('u'|'U')  
                | '\\' 
                        (
                              'u'
                            | 'U'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('5')
                        )
                ;
fragment    V   :   ('v'|'V')  
                | '\\' 
                        (     'v'
                            | 'V'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('6')
                        )
                ;
fragment    W   :   ('w'|'W')  
                | '\\' 
                        (
                              'w'
                            | 'W'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('7')
                        )   
                ;
fragment    X   :   ('x'|'X')  
                | '\\' 
                        (
                              'x'
                            | 'X'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('8')
                        )
                ;
fragment    Y   :   ('y'|'Y')  
                | '\\' 
                        (
                              'y'
                            | 'Y'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('9')
                        )
                ;
fragment    Z   :   ('z'|'Z')  
                | '\\' 
                        (
                              'z'
                            | 'Z'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('A'|'a')
                        )
                ;


// -------------
// Comments.    Comments may not be nested, may be multilined and are delimited
//              like C comments: /* ..... */
//              COMMENTS are hidden from the parser which simplifies the parser 
//              grammar a lot.
//
COMMENT         : '/*' ( options { greedy=false; } : .*) '*/'
    
                    {
                        $channel = 2;   // Comments on channel 2 in case we want to find them
                    }
                ;

// ---------------------
// HTML comment open.   HTML/XML comments may be placed around style sheets so that they
//                      are hidden from higher scope parsing engines such as HTML parsers.
//                      They comment open is therfore ignored by the CSS parser and we hide
//                      it from the ANLTR parser.
//
CDO             : '<!--'

                    {
                        $channel = 3;   // CDO on channel 3 in case we want it later
                    }
                ;
    
// ---------------------            
// HTML comment close.  HTML/XML comments may be placed around style sheets so that they
//                      are hidden from higher scope parsing engines such as HTML parsers.
//                      They comment close is therfore ignored by the CSS parser and we hide
//                      it from the ANLTR parser.
//
CDC             : '-->'

                    {
                        $channel = 4;   // CDC on channel 4 in case we want it later
                    }
                ;
                
INCLUDES        : '~='      ;
DASHMATCH       : '|='      ;
BEGINS          : '^='      ;
ENDS            : '$='      ;
CONTAINS        : '*='      ;

GREATER         : '>'       ;
LBRACE          : '{'       ;
RBRACE          : '}'       ;
LBRACKET        : '['       ;
RBRACKET        : ']'       ;
OPEQ            : '='       ;
SEMI            : ';'       ;
COLON           : ':'       ;
DCOLON          : '::'       ;
SOLIDUS         : '/'       ;
MINUS           : '-'       ;
PLUS            : '+'       ;
STAR            : '*'       ;
LPAREN          : '('       ;
RPAREN          : ')'       ;
COMMA           : ','       ;
DOT             : '.'       ;
TILDE		: '~'       ;
PIPE            : '|'       ;

// -----------------
// Literal strings. Delimited by either ' or "
//
fragment    INVALID :;
STRING          : '\'' ( ~('\n'|'\r'|'\f'|'\'') )* 
                    (
                          '\''
                        | { $type = INVALID; }
                    )
                    
                | '"' ( ~('\n'|'\r'|'\f'|'"') )*
                    (
                          '"'
                        | { $type = INVALID; }
                    )
                ;


ONLY 		: O N L Y;
NOT		: N O T; 
AND		: A N D;

// -------------
// Identifier.  Identifier tokens pick up properties names and values
//
IDENT           : '-'? NMSTART NMCHAR*  ;

// -------------
// Reference.   Reference to an element in the body we are styling, such as <XXXX id="reference">
//
HASH            : '#' NAME              ;

IMPORTANT_SYM   : '!' (WS|COMMENT)* I M P O R T A N T   ;

IMPORT_SYM          : '@' I M P O R T       ;
PAGE_SYM            : '@' P A G E           ;
MEDIA_SYM           : '@' M E D I A         ;
NAMESPACE_SYM       : '@' N A M E S P A C E ;
CHARSET_SYM         : '@charset'           ;
COUNTER_STYLE_SYM   : '@counter-style';
FONT_FACE_SYM       : '@font-face';

TOPLEFTCORNER_SYM     :'@top-left-corner';
TOPLEFT_SYM           :'@top-left';
TOPCENTER_SYM         :'@top-center';
TOPRIGHT_SYM          :'@top-right';
TOPRIGHTCORNER_SYM    :'@top-right-corner';
BOTTOMLEFTCORNER_SYM  :'@bottom-left-corner'; 
BOTTOMLEFT_SYM        :'@bottom-left';
BOTTOMCENTER_SYM      :'@bottom-center';
BOTTOMRIGHT_SYM       :'@bottom-right';
BOTTOMRIGHTCORNER_SYM :'@bottom-right-corner';
LEFTTOP_SYM           :'@left-top';
LEFTMIDDLE_SYM        :'@left-middle';
LEFTBOTTOM_SYM        :'@left-bottom';
RIGHTTOP_SYM          :'@right-top';
RIGHTMIDDLE_SYM       :'@right-middle';
RIGHTBOTTOM_SYM       :'@right-bottom';

MOZ_DOCUMENT_SYM      : '@-moz-document';

//this generic at rule must be after the last of the specific at rule tokens
GENERIC_AT_RULE	    : '@' NMCHAR+;	

//I cannot figure out how to use the fragment tokens to generate the following tokens.
//the parser generator cycles itself indefinitely.

//fragment TOP          : '@' T O P '-';
//fragment TOPLEFT      : TOP L E F T;
//fragment TOPRIGHT     : TOP R I G H T;

//TOPLEFTCORNER_SYM     : TOPLEFT '-' C O R N E R;
//TOPLEFT_SYM           : TOPLEFT;
//TOPCENTER_SYM         :	TOP '-' C E N T E R;
//TOPRIGHT_SYM          : TOPRIGHT;
//TOPRIGHTCORNER_SYM    : TOPRIGHT '-' C O R N E R;

//fragment BOTTOM          : '@' B O T T O M '-';
//fragment BOTTOMLEFT      : BOTTOM L E F T;
//fragment BOTTOMRIGHT     : BOTTOM R I G H T;

//BOTTOMLEFTCORNER_SYM  : BOTTOMLEFT '-' C O R N E R;
//BOTTOMLEFT_SYM        : BOTTOMLEFT;
//BOTTOMCENTER_SYM      :	BOTTOM '-' C E N T E R;
//BOTTOMRIGHT_SYM       :	BOTTOMRIGHT; 
//BOTTOMRIGHTCORNER_SYM : BOTTOMRIGHT '-' C O R N E R;
  
//LEFTTOP_SYM           : '@' L E F T '-' T O P;
//LEFTMIDDLE_SYM        : '@' L E F T '-' M I D D L E;
//LEFTBOTTOM_SYM        : '@' L E F T '-' B O T T O M;

//RIGHTTOP_SYM          : '@' R I G H T '-' T O P;
//RIGHTMIDDLE_SYM       : '@' R I G H T '-' M I D D L E;
//RIGHTBOTTOM_SYM       : '@' R I G H T '-' B O T T O M;

// ---------
// Numbers. Numbers can be followed by pre-known units or unknown units
//          as well as '%' it is a precentage. Whitespace cannot be between
//          the numebr and teh unit or percent. Hence we scan any numeric, then
//          if we detect one of the lexical sequences for unit tokens, we change
//          the lexical type dynamically.
//
//          Here we first define the various tokens, then we implement the
//          number parsing rule.
//
fragment    EMS         :;  // 'em'
fragment    EXS         :;  // 'ex'
fragment    LENGTH      :;  // 'px'. 'cm', 'mm', 'in'. 'pt', 'pc'
fragment    ANGLE       :;  // 'deg', 'rad', 'grad'
fragment    TIME        :;  // 'ms', 's'
fragment    FREQ        :;  // 'khz', 'hz'
fragment    DIMENSION   :;  // nnn'Somethingnotyetinvented'
fragment    PERCENTAGE  :;  // '%'
fragment    RESOLUTION  :;  //dpi,dpcm	

NUMBER
    :   (
              '0'..'9'+ ('.' '0'..'9'+)?
            | '.' '0'..'9'+
        )
        (
              (D P (I|C))=>
                D P
                (
                     I | C M     
                )
                { $type = RESOLUTION; }
        	
            | (E (M|X))=>
                E
                (
                      M     { $type = EMS;          }
                    | X     { $type = EXS;          }
                )
            | (P(X|T|C))=>
                P
                (
                      X     
                    | T
                    | C
                )
                            { $type = LENGTH;       }   
            | (C M)=>
                C M         { $type = LENGTH;       }
            | (M (M|S))=> 
                M
                (
                      M     { $type = LENGTH;       }
            
                    | S     { $type = TIME;         }
                )
            | (I N)=>
                I N         { $type = LENGTH;       }
            
            | (D E G)=>
                D E G       { $type = ANGLE;        }
            | (R A D)=>
                R A D       { $type = ANGLE;        }
            
            | (S)=>S        { $type = TIME;         }
                
            | (K? H Z)=>
                K? H    Z   { $type = FREQ;         }
            
            | IDENT         { $type = DIMENSION;    }
            
            | '%'           { $type = PERCENTAGE;   }
            
            | // Just a number
        )
    ;

// ------------
// url and uri.
//
URI :   U R L
        '('
            ((WS)=>WS)? (URL|STRING) WS?
        ')'
    ;
    
MOZ_URL_PREFIX
	:
	'url-prefix('
            ((WS)=>WS)? (URL|STRING) WS?
        ')'
    
    	;

MOZ_DOMAIN
	:
	'domain('
            ((WS)=>WS)? (URL|STRING) WS?
        ')'
    
    	;

MOZ_REGEXP
	:
	'regexp('
            ((WS)=>WS)? STRING WS?
        ')'
    
    	;



// -------------
// Whitespace.  Though the W3 standard shows a Yacc/Lex style parser and lexer
//              that process the whitespace within the parser, ANTLR does not
//              need to deal with the whitespace directly in the parser.
//
//WS      : (' '|'\t')+           { $channel = HIDDEN;    }   ;
WS      : (' '|'\t')+;
NL      : ('\r' '\n'? | '\n')   { $channel = HIDDEN;    }   ;


// -------------
//  Illegal.    Any other character shoudl not be allowed.
//
