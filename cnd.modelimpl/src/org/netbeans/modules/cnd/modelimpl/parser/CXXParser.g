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

/**
 * C++ grammar.
 * @author Fedor Sergeev
 */

parser grammar CXXParser;

options {
    tokenVocab = APTTokenTypes;
    backtrack = false;
}

scope QualName {
    int qual;
    int type;
}

scope Declaration {
    declarator_type_t declarator;
    declaration_specifiers_t decl_specifiers;
    int type_specifiers_count;
}

@header {
package org.netbeans.modules.cnd.modelimpl.parser.generated;

import java.util.HashMap;
import org.netbeans.modules.cnd.modelimpl.parser.*;
}

@members {

    public /*final*/CppParserAction3 action;

    public CXXParser(TokenStream input, CppParserAction3 action) {
        super(input);
        assert action != null;
        this.action = action;
    }

    public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        // do nothing
    }

    class pCXX_grammar {
    }

    decl_kind blockscope_decl = null;
    decl_kind tparm_decl = null;
    decl_kind parm_decl = null;
    decl_kind field_decl = null;
    decl_kind object_decl = null;

    Object NULL = null;
    class decl_kind{
    }
    class type_specifier_t{
    }
    class name_specifier_t{
    }
    class declarator_type_t{
        public void init() {
        }
        public boolean is_function() {
            return true;
        }
        public void set_ident() {
        }
        public void set_ref() {
        }
        public void set_ptr(Object o1, Object o2) {
        }
        public void apply_array(Object o1) {
        }
        public void apply_parameters(Object o1) {
        }
        public void apply_ptr(Object o1) {
        }
        public void set_constructor(Object o1) {
        }

    }
    class declaration_specifiers_t{
        public void init(Object o) {
        }
        public void add_type(Object o1, Object o2) {
        }
        public void apply_specifier(Object o1, Object o2) {
        }
    }
    class qualifier_t{
    }
    class parameters_and_qualifiers_t{
    }
    class expression_t{
    }

    void println(Object o) {
    }
    void println(Object o1,Object o2) {
    }

    pCXX_grammar CTX;

    static int IDT_CLASS_NAME=1;
    static int IDT_ENUM_NAME=2;
    static int IDT_TYPEDEF_NAME=4;
    static int IDT_TEMPLATE_NAME=8;
    static int IDT_OBJECT_NAME = 16;

    void init_declaration(pCXX_grammar ctx, decl_kind kind)
    {
//        $Declaration::declarator.init();
//        $Declaration::decl_specifiers.init(kind);
//        $Declaration::type_specifiers_count = 0;
    }

    boolean type_specifier_already_present(pCXX_grammar ctx)
    {
        boolean result = false;
//        if ($Declaration->size($Declaration) > 0) {
//            result = $Declaration::type_specifiers_count != 0;
//        }
//        trace("type_specifier_already_present()=",result);
        return result;
    }
    boolean identifier_is(int x) {
//        trace("identifier_is()=",true);
        return true;
    }
    boolean top_level_of_template_arguments() {
//        trace("top_level_of_template_arguments()=",true);
        return true;
    }
    boolean operator_is_template() {
//        trace("operator_is_template()=",true);
        return true;
    }

    void qual_setup() {
    }
    void qual_add_colon2() {
    }

    void store_type_specifier(type_specifier_t ts, pCXX_grammar ctx) {
//        $Declaration::type_specifiers_count++;
//        trace("store_type_specifier->", $Declaration::type_specifiers_count);
    }
}

compilation_unit: translation_unit;


/*START*/

// [gram.basic] 
translation_unit:
        declaration[object_decl]* EOF
    ;

// [gram.stmt]
/*
 * As per 2003 standard:
 * "An expression-statement with a function-style explicit type conversion as its leftmost 
 * subexpression can be indistinguishable from a declaration where the first declarator starts with a '('. 
 * In those cases the statement is a declaration."
 *
 * Resolve declaration vs expression conflict in favor of declaration.
 * (actually declaration synpred is a HUGE hammer, we should try find something else)
 */
statement:
        labeled_statement
    |
        expression_or_declaration_statement
    |
        compound_statement
    |
        selection_statement
    |
        iteration_statement
    |
        jump_statement
    |
        try_block
    ;

labeled_statement:
        IDENT COLON statement
    |
        LITERAL_case constant_expression COLON statement
    |
        LITERAL_default COLON statement
    ;

expression_statement:
        expression? SEMICOLON
    ;

expression_or_declaration_statement
    :
        (declaration_statement)=> declaration_statement
    |
        expression SEMICOLON
    ;


compound_statement:
        LCURLY statement* RCURLY
    ;

selection_statement:
        LITERAL_if LPAREN condition RPAREN statement ( (LITERAL_else)=> LITERAL_else statement )?
    |
        LITERAL_switch LPAREN condition RPAREN statement
    ;

/*
 * The same expression-declaration ambiguity as in statement rule.
 */
condition
scope Declaration;
@init { init_declaration(CTX, blockscope_decl); }
    :
        (type_specifier+ declarator EQUAL)=>
            type_specifier+ declarator EQUAL assignment_expression
    |
        expression
    ;

iteration_statement:
        LITERAL_while LPAREN condition RPAREN statement
    |
        LITERAL_do statement LITERAL_while LPAREN expression RPAREN SEMICOLON
    |
        LITERAL_for LPAREN for_init_statement condition? SEMICOLON expression? RPAREN statement
    ;
/*
 * The same expression-declaration ambiguity as in statement rule.
 */
for_init_statement:
        (simple_declaration[blockscope_decl])=>
            simple_declaration[blockscope_decl]
    |
        expression_statement

    ;
jump_statement:
        LITERAL_break SEMICOLON
    |
        LITERAL_continue SEMICOLON
    |
        LITERAL_return expression? SEMICOLON
    |
        LITERAL_goto IDENT SEMICOLON
    ;

/*
 * simple_declaration has been split out of block_declaration so to have
 * an easier view of simple_declaration vs function_definition major conflict.
 */
declaration_statement:
        simple_declaration[blockscope_decl]
    |
        block_declaration
    ;

//[gram.dcl] 
/*
 * function_definition merged into one rule with simple_declaration (which in turn was taken out of block_declaration)
 */
declaration [decl_kind kind] :
        block_declaration
    |
        simple_declaration_or_function_definition[kind]
    |
        template_declaration[kind]
    |
        explicit_instantiation[kind]
    |
        explicit_specialization[kind]
    |
        linkage_specification[kind]
    |
        namespace_definition 
    ;
block_declaration:
        asm_definition 
    |
        namespace_alias_definition 
    |
        using_declaration
    |
        using_directive 
    ;

// IDs
id_expression:
        unqualified_id
    |
        qualified_id
    ;

unqualified_id:
        (LITERAL_OPERATOR operator_id)=>
            operator_function_id
    |
        conversion_function_id
    |
        TILDE class_name
    |
        simple_template_id_or_IDENT
    ;

qualified_id:
        nested_name_specifier LITERAL_template? unqualified_id
    |
        SCOPE (
            nested_name_specifier LITERAL_template? unqualified_id
        |
            operator_function_id
        |
            simple_template_id_or_IDENT
        )
    ;

/* original rule:
 *

nested_name_specifier:
        type-name SCOPE
    |
        namespace-name SCOPE
    |
        nested-name-specifier IDENT SCOPE
    |
        nested-name-specifier LITERAL_template? simple-template-id SCOPE
    ;

 * left-recursion removed here and LITERAL_template/IDENT ambiguity resolved
 */

nested_name_specifier returns [ name_specifier_t namequal ]
    :
        IDENT SCOPE
        (
            (LITERAL_template lookup_simple_template_id_nocheck SCOPE )=> LITERAL_template simple_template_id_nocheck SCOPE
        |
            (IDENT SCOPE) =>
                IDENT SCOPE
        |
            (lookup_simple_template_id SCOPE)=>
                simple_template_id SCOPE
        )*
    ;

lookup_nested_name_specifier:
        IDENT SCOPE
        (
            IDENT SCOPE
        |
            LITERAL_template lookup_simple_template_id SCOPE
        |
            lookup_simple_template_id SCOPE
        )*
    ;

//[gram.dcl]
/*
 * original rule:

simle_declaration
        decl_specifier* (init_declarator (COMMA init_declarator)*)* SEMICOLON
    ;

 * construtor_declarator introduced into init_declarator part to resolve ambiguity
 * between single decl_specifier and the constructor name in a declarator (declarator_id) of constructor.
 *
 */
simple_declaration [decl_kind kind]
scope Declaration;
@init { init_declaration(CTX, kind); }
    :
        decl_specifier*
        (
            SEMICOLON
        |
            (
                (constructor_declarator)=> constructor_declarator
            |
                init_declarator
            )
            // this is a continuation of init_declarator_list after constructor_declarator/init_declarator
            (COMMA init_declarator)* SEMICOLON
        )
    ;

/*
 * This is the above simple_declaration rule merged together with function definition
 * The idea is to avoid doing any lookaheads unless absolutely necessary (constructor declarator).
 * It requires certain duplication as common constructs in each branch of choice are a bit different
 * (see different init_declarator_list continuation sequences).
 */
simple_declaration_or_function_definition [decl_kind kind]
scope Declaration;
@init { init_declaration(CTX, kind); }
    :
        decl_specifier*
        (
            SEMICOLON
        |
            (constructor_declarator)=>
                constructor_declarator
                (
                    // this is a continuation of init_declarator_list after constructor_declarator
                    ( COMMA init_declarator )* SEMICOLON
                |
                    function_definition_after_declarator
                )
        |
            // greedy_declarator starts init_declarator
            greedy_declarator
            (
                { $greedy_declarator.type.is_function() }?
                    function_definition_after_declarator
            |
                // this is a continuation of init_declarator_list after greedy_declarator
                initializer? ( COMMA init_declarator )* SEMICOLON
            )
        )
    ;

decl_specifier
    :
        storage_class_specifier
//        { $Declaration::decl_specifiers.apply_specifier($decl_specifier.start, CTX); }
    |
        function_specifier 
//        { $Declaration::decl_specifiers.apply_specifier($decl_specifier.start, CTX); }
    |
        LITERAL_friend
//        { $Declaration::decl_specifiers.apply_specifier($LITERAL_friend, CTX); }
    |
        LITERAL_typedef
//        { $Declaration::decl_specifiers.apply_specifier($LITERAL_typedef, CTX); }
    |
        type_specifier
//        { $Declaration::decl_specifiers.add_type($type_specifier.ts, CTX); }
    ;

storage_class_specifier:
        LITERAL_auto 
    |
        LITERAL_register 
    |
        LITERAL_static 
    |
        LITERAL_extern 
    |
        LITERAL_mutable 
    |
        LITERAL___thread
    ;
function_specifier:
        LITERAL_inline 
    |
        LITERAL_virtual 
    |
        LITERAL_explicit 
    ;

/*
 * original rule

type_specifier:
        simple_type_specifier 
    |
        class_specifier 
    |
        enum_specifier 
    |
        elaborated_type_specifier
    |
        cv_qualifier 
    ;

 * Ambiguity in LITERAL_class because of class_specifier vs elaborated_type_specifier conflict
 * Ambiguity in LITERAL_enum because of enum_specifier vs elaborated_type_specifier conflict
 *
 * Note, that (LITERAL_class SCOPE) sequence is not valid for class_specifier
 * Similarly (LITERAL_enum SCOPE) sequence is not valid for enum_specifier
 */

type_specifier returns [type_specifier_t ts]
    :
        // LITERAL_class SCOPE does not cover all the elaborated_type_specifier cases even with LITERAL_class
        (LITERAL_class SCOPE)=>
            elaborated_type_specifier
    |
        // thus we need to make serious lookahead here to catch LCURLY
        (class_head LCURLY)=>
            class_specifier
    |
        // enum_specifier start sequence is simple
        (LITERAL_enum IDENT? LCURLY)=>
            enum_specifier
    |
        simple_type_specifier
        { store_type_specifier($simple_type_specifier.ts_val, CTX); }
    |
        // LITERAL_class SCOPE above does not cover all the elaborated_type_specifier cases
        elaborated_type_specifier
    |
        cv_qualifier
    ;

simple_type_specifier returns [type_specifier_t ts_val]
scope QualName;
@init { qual_setup(); }
    :
        LITERAL_char
    |
        LITERAL_wchar_t
    |
        LITERAL_bool
    |
        LITERAL_short
    |
        LITERAL_int
    |
        LITERAL_long
    |
        LITERAL_signed
    |
        LITERAL_unsigned
    |
        LITERAL_float
    |
        LITERAL_double
    |
        LITERAL_void
    |
        /*
         * "at most one type-specifier is allowed in the complete decl-specifier-seq of a declaration..."
         * In particular (qualified)type_name is allowed only once.
         */
        { !type_specifier_already_present(CTX) }? =>
            (SCOPE {{ qual_add_colon2(); }} )?
            /* note that original rule does not allow empty nested_name_specifier for the LITERAL_template alternative */
            (
                (lookup_nested_name_specifier)=>
                    nested_name_specifier (IDENT | LITERAL_template simple_template_id)
            |
                IDENT
            )
    ;

lookup_type_name:
        IDENT { identifier_is(IDT_CLASS_NAME|IDT_ENUM_NAME|IDT_TYPEDEF_NAME) }?
    ;

/*
 * original rule:
 *
elaborated_type_specifier:
        class_key SCOPE? nested_name_specifier? IDENT 
    |
        class_key SCOPE? nested_name_specifier? LITERAL_template? simple_template_id 
    |
        LITERAL_enum SCOPE? nested_name_specifier? IDENT 
    |
        LITERAL_typename SCOPE? nested_name_specifier IDENT 
    |
        LITERAL_typename SCOPE? nested_name_specifier LITERAL_template? simple_template_id 
    ;
* Ambiguity introduced by IDENT SCOPE IDENT sequence in a context of
* elaborated_type_specifier going right before declarators in simple declaration.
* Resolved by factoring out nested_name_specifier construct in 'class' situation.
* Resolved by specifically predicating IDENT SCOPE in 'enum' situation.
*/

elaborated_type_specifier:
        class_key SCOPE? (
            (IDENT SCOPE) =>
                nested_name_specifier (simple_template_id_or_IDENT | LITERAL_template simple_template_id_nocheck)
        |
             (simple_template_id_or_IDENT | LITERAL_template simple_template_id_nocheck)
        )
    |
        LITERAL_enum SCOPE? (
            (IDENT SCOPE)=>
                nested_name_specifier IDENT
        |
            nested_name_specifier IDENT
        |
            (IDENT)=>
                IDENT
        )
    |
        typename_specifier
    ;

// In C++0x this is factored out already
typename_specifier:
        LITERAL_typename SCOPE? nested_name_specifier ( simple_template_id_or_IDENT  | LITERAL_template simple_template_id_nocheck )
    ;

/*
 * original rule (not needed now):
enum_name:
        IDENT
    ;
 *
 */
enum_specifier:
        LITERAL_enum IDENT? LCURLY enumerator_list? RCURLY
    ;
enumerator_list:
        enumerator_definition (COMMA enumerator_definition)* 
    ;
enumerator_definition:
        enumerator 
    |
        enumerator ASSIGNEQUAL constant_expression 
    ;

enumerator:
        IDENT 
    ;

/*
 * original rules (not needed now):

namespace_name:
        original_namespace_name 
    |
        namespace_alias 
    ;

original_namespace_name:
        IDENT
    ;
 *
 */

/*
 * original rules:

namespace_definition:
        named_namespace_definition
    |
        unnamed_namespace_definition
    ;

named_namespace_definition:
        original_namespace_definition
    |
        extension_namespace_definition
    ;
original_namespace_definition:
        LITERAL_namespace IDENT LCURLY namespace_body RCURLY
    ;
extension_namespace_definition:
        LITERAL_namespace original_namespace_name LCURLY namespace_body RCURLY
    ;

unnamed_namespace_definition:
        LITERAL_namespace LCURLY namespace_body RCURLY
    ;

 * This is all unnecessarily complicated. We can easily handle it by one single rule:
 */
namespace_definition:
        LITERAL_namespace       {action.namespace_declaration($LITERAL_namespace);}
        (   
            IDENT               {action.namespace_name($IDENT);}
        )? 
        LCURLY                  {action.namespace_body($LCURLY);}
        namespace_body 
        RCURLY                  {action.end_namespace_body($RCURLY);} 
                                {action.end_namespace_declaration($RCURLY);}
    ;

namespace_body:
        declaration[object_decl] *
    ;

namespace_alias:
        IDENT
    ;

namespace_alias_definition:
        LITERAL_namespace IDENT ASSIGNEQUAL qualified_namespace_specifier SEMICOLON
    ;

qualified_namespace_specifier:
        SCOPE? nested_name_specifier? IDENT
    ;

/*
 * original rule:

using-declaration:
        LITERAL_using LITERAL_typename? SCOPE? nested_name_specifier unqualified_id SEMICOLON
     |
        LITERAL_using SCOPE unqualified_id SEMICOLON
     ;

 * Ambiguity in SCOPE between two alternatives resolved by collapsing them into one.
 * Note that new rule allows LITERAL_using unqualified_id w/o SCOPE, not allowed before.
 * It should be ruled out after the parsing.
 */
using_declaration
     : LITERAL_using LITERAL_typename? SCOPE? nested_name_specifier? unqualified_id SEMICOLON
    ;

using_directive:
        LITERAL_using LITERAL_namespace SCOPE? nested_name_specifier? IDENT SEMICOLON
    ;


asm_definition:
        LITERAL_asm LPAREN STRING_LITERAL RPAREN SEMICOLON
    ;

linkage_specification [decl_kind kind]:
        LITERAL_extern STRING_LITERAL LCURLY declaration[kind] * RCURLY
    |
        LITERAL_extern STRING_LITERAL declaration[kind]
    ;

init_declarator_list:
        init_declarator (COMMA init_declarator)*
    ;

/*
 * As per 2003 standard:
 * Ambiguity happens "between a function declaration with a redundant set of parentheses
 * around a parameter name and an object declaration with a function-style cast as the initializer."
 *
 * Thus declarator (which can end in parameters_and_qualifiers) conflicts with "()"-initializer.
 * "the resolution is to consider any construct that could possibly be a declaration a declaration".
 * Thus we take parameters_and_qualifiers as far as possible.
 *
 */
init_declarator:
        greedy_declarator initializer?
    ;

/*
 * original rule (naming as per C++0X)
declarator:
    ptr_declarator
    ;
ptr_declarator:
        noptr_declarator
    |
        ptr_operator ptr_declarator
    ;
noptr_declarator:
        declarator_id
    |
        noptr_declarator parameters-and-qualifiers
    |
        noptr_declarator LSQUARE constant_expression? RSQUARE
    |
        LPAREN ptr_declarator RPAREN
    ;
 * Ambiguity on nested_name qualifier is caused by ptr_operator vs declarator_id (of direct declarator).
 * It qualifies either STAR (for ptr_operator) or type_name (for declarator_id).
 * Resolve by syntactically probing ptr_operator first.
 */



declarator returns [declarator_type_t type]
    :
        noptr_declarator 
            {{ type = $noptr_declarator.type; }}
    |
        (ptr_operator)=>
            ptr_operator nested=declarator
                {{ type = $nested.type;
                   type.apply_ptr($ptr_operator.type);
                }}
    ;

// is quite unpretty because of left recursion removed here
noptr_declarator returns [declarator_type_t type]
    :
        (
            declarator_id
                {{ type = $declarator_id.type; }}
        |
            LPAREN declarator RPAREN
                {{ type = $declarator.type; }}
        ) // continued
        (
            parameters_and_qualifiers
                {{ type.apply_parameters($parameters_and_qualifiers.pq); }}
         |
             LSQUARE constant_expression? RSQUARE
                {{ type.apply_array($constant_expression.expr); }}
        )*
    ;

/*
 *   This rule was crafted in order to resolve ambiguity between decl_specifier (type_specifier)
 * and constructor declaration (which has declarator_id == class name).
 * For that we create a special "constructor-declarator", which is a function declarator *BUT* without a
 * leading class name.
 */
function_declarator returns [declarator_type_t type]
    :
        (constructor_declarator)=>
            constructor_declarator {{ type = $constructor_declarator.type; }}
    |
        declarator {{ type = $declarator.type; }}
    ;

constructor_declarator returns [declarator_type_t type]
    :
        parameters_and_qualifiers
            {{ type.set_constructor($parameters_and_qualifiers.pq); }}
    ;

/*

abstract_declarator:
        ptr_abstract_declarator
    ;
ptr_abstract_declarator:
        noptr_abstract_declarator
    |
        ptr_operator ptr_abstract_declarator?
    ;

noptr_abstract_declarator:
        noptr_abstract_declarator? parameters_and_qualifiers
    |
        noptr_abstract_declarator? LSQUARE constant_expression RSQUARE
    |
        ( ptr_abstract_declarator )
    ;
*/

abstract_declarator returns [declarator_type_t type]
    :
        noptr_abstract_declarator {{ type = $noptr_abstract_declarator.type; }}
    |
        ptr_operator decl=abstract_declarator?
            {{ type = $decl.type;
               type.apply_ptr($ptr_operator.type);
            }}
    ;

noptr_abstract_declarator returns [declarator_type_t type]
    :
        ( parameters_and_qualifiers | LSQUARE constant_expression? RSQUARE )+
    |
        (LPAREN abstract_declarator RPAREN)=>
            LPAREN abstract_declarator RPAREN ( parameters_and_qualifiers | LSQUARE constant_expression? RSQUARE )*
    ;

universal_declarator returns [declarator_type_t type]
options { backtrack = true; }
    :
        declarator { type = $declarator.type; }
    |
        abstract_declarator { type = $abstract_declarator.type; }
    ;

greedy_declarator returns [declarator_type_t type]
    :
        greedy_nonptr_declarator {{ type = $greedy_nonptr_declarator.type; }}
    |
        (ptr_operator)=>
            ptr_operator decl=greedy_declarator
            {{ type = $decl.type;
               type.apply_ptr($ptr_operator.type);
            }}
    ;

/*
 * This is to resolve ambiguity between declarator and subsequent (expression) initializer in init_declarator.
 * Eat as much parameter sets as possible.
 */
greedy_nonptr_declarator returns [declarator_type_t type]
    :
        (
            declarator_id
                {{ type = $declarator_id.type; }}
        |
            LPAREN greedy_declarator RPAREN
                {{ type = $greedy_declarator.type; }}
        ) // continued
        (
            (parameters_and_qualifiers)=>
                parameters_and_qualifiers
                {{ type.apply_parameters($parameters_and_qualifiers.pq); }}
        |
            LSQUARE constant_expression? RSQUARE
                {{ type.apply_array($constant_expression.expr); }}
        )*
    ;

ptr_operator returns [ declarator_type_t type ]
    :
        STAR cv_qualifier*
            {{ type.set_ptr(NULL, $cv_qualifier.qual); }}
    |
        AMPERSAND 
            {{ type.set_ref(); }}
    |
        SCOPE? nested_name_specifier STAR cv_qualifier*
/*DLITERAL_ifF*/ //           {{ type.set_ptr(& $nested_name_specifier.namequal, $cv_qualifier.qual); }}
    ;

cv_qualifier returns [ qualifier_t qual ]:
/*DLITERAL_ifF*/        LITERAL_const //{{ qual = LITERAL_const; }}
    |
/*DLITERAL_ifF*/        LITERAL_volatile //{{ qual = LITERAL_volatile; }}
    ;

/*
 * original rule:

    |
        SCOPE? nested_name_specifier? type_name 

 * This alternative deleted, as it actually is contained in id_expression
 */

declarator_id returns [ declarator_type_t type ] :
        id_expression {{ type.set_ident(); }}
    ;

/*
 * from 8.2 Ambiguity resolution:
 * "any construct that could possibly be a type-id in its syntactic context
 * shall be considered a type-id"
 */
type_id:
        type_specifier+ abstract_declarator?
    ;

parameters_and_qualifiers returns [ parameters_and_qualifiers_t pq ]
    :

        LPAREN parameter_declaration_clause RPAREN cv_qualifier* exception_specification?
    ;

parameter_declaration_clause
scope Declaration; /* need it zero'ed to handle hoisted type_specifier predicate */
@init { init_declaration(CTX, parm_decl); }
    :
        ELLIPSIS?
    |
        parameter_declaration_list (COMMA? ELLIPSIS)?
    ;

parameter_declaration_list:
        parameter_declaration[parm_decl] (COMMA parameter_declaration[parm_decl])*
    ;
parameter_declaration [decl_kind kind]
scope Declaration;
@init { init_declaration(CTX, kind); }
    :
        decl_specifier+ universal_declarator? (ASSIGNEQUAL assignment_expression)?
    ;

/*
 * original rule:

function_definition:
        decl_specifier* declarator ctor_initializer? function_body 
    |
        decl_specifier* declarator function_try_block
    ;

 * Factoring out a sequence that follows declarator, as it helps disambiguating in context when
 * function_definition conflicts because of decl_specifier
 */
function_definition_after_declarator:
        ctor_initializer? function_body
    |
        function_try_block
    ;

/*
 * We have a baaad conflict caused by declaration w/o decl_specifier,
 * that is w/o return type specification.
 *
 * In old K&R C times this was an "implicit int" declaration.
 * Currently we allow only constructors/destructors to have no return type
 * (and surely it does not mean "implicit int").
 *
 * However constructor's name conflicts with type_specifier of an ordinary declaration.
 *
 * This conflict rises for any function declaration
 */

function_declaration [decl_kind kind]
scope Declaration;
@init { init_declaration(CTX, kind); }
    :
        decl_specifier* function_declarator
    ;

function_definition [decl_kind kind]:
        function_declaration[kind] function_definition_after_declarator
    ;

function_body:
        compound_statement 
    ;

initializer:
        ASSIGNEQUAL initializer_clause 
    |
        LPAREN expression_list RPAREN 
    ;
initializer_clause:
        assignment_expression 
    |
        LCURLY initializer_list COMMA? RCURLY
    |
        LCURLY RCURLY
    ;
initializer_list:
        initializer_clause (COMMA initializer_clause )*
    ;

//[gram.class] 
class_name:
        simple_template_id_or_IDENT 
    ;

class_specifier:
                                {action.class_declaration(input.LT(1));}
        class_head 
        LCURLY                  {action.class_body($LCURLY);}
        member_specification? 
        RCURLY                  {action.end_class_body($RCURLY);}
                                {action.end_class_declaration(input.LT(1));}
    ;

/*
 * Original rule:

class_head:
        class_key IDENT? base_clause? 
    |
        class_key nested_name_specifier IDENT base_clause? 
    |
        class_key nested_name_specifier? simple_template_id base_clause? 
    ;

*  Ambiguity due to nested_name_specifier usage
*/
optionally_qualified_name
    :
        nested_name_specifier? simple_template_id_or_IDENT
    ;

class_head:
        class_key optionally_qualified_name? base_clause?
    ;

class_key:
        LITERAL_class           {action.class_kind($LITERAL_class);}
    |
        LITERAL_struct          {action.class_kind($LITERAL_struct);}
    |
        LITERAL_union           {action.class_kind($LITERAL_union);}
    ;
member_specification :
        member_declaration[field_decl] member_specification?
    |
        access_specifier COLON member_specification?
    ;


/*
 * original rule (part that was rewritten)

 member_declaration:
        decl_specifier* member_declarator_list? SEMICOLON
    |
        function_definition SEMICOLON?
    |
        SCOPE? nested_name_specifier LITERAL_template? unqualified_id SEMICOLON
    |

member_declarator:
        declarator constant_initializer?
    |
        IDENT? COLON constant_expression
    ;

 *
 * (optional SEMICOLON? deleted after function_defition, as the first alternative takes care of it already)
 * Conflict on decl_specifier between first alternative and second one (function_definition) resolved
 * by factorizing on common parts of the first member_declarator (decl_specifier* declarator).
 * It was pretty involved, and besides member_declaration also affecting 3 other rules.
 *
 * Another conflict is between first set of alternatives and access declaration.
 * Access declaration is being subsumed by member declaration with absent decl_specifier.
 * There needs to be a special semantic check for "access declaration" when handling results of member declaration.
 */
member_declaration [decl_kind kind]
scope Declaration;
@init { init_declaration(CTX, kind); }
    :
        decl_specifier*
        (
            (IDENT? COLON)=>
                member_bitfield_declarator ( COMMA member_declarator )* SEMICOLON
        |
            (constructor_declarator)=>
                constructor_declarator
                (
                    // this was member_declarator_list
                    ( COMMA member_declarator )* SEMICOLON
                |
                    function_definition_after_declarator
                )
        |
            declarator
            (
                { $declarator.type.is_function() }?
                    function_definition_after_declarator
            |
                // this was member_declarator_list
                constant_initializer? ( COMMA member_declarator )* SEMICOLON
            )
        |
            SEMICOLON
        )
    |
        /* this is likely to be covered by decl_specifier/declarator part of member_declarator
            SCOPE? nested_name_specifier LITERAL_template? unqualified_id SEMICOLON
    |
        */

        using_declaration
    |
        template_declaration[kind]
    ;

member_bitfield_declarator:
        IDENT? COLON constant_expression
    ;

member_declarator:
        declarator constant_initializer?
    |
        member_bitfield_declarator
    ;

/*
 * original rule:

member_declarator_list:
        member_declarator ( COMMA member_declarator )*
    ;

 *
 * No longer needed as this list was inserted into member_declaration rule in order to
 * factorize first member_declaration entry.
 */

// = 0 (not used, as it conflicts with constant_initializer
pure_specifier:
        ASSIGNEQUAL literal
    ;

constant_initializer:
        ASSIGNEQUAL constant_expression 
    ;

// [gram.class.derived] 
base_clause:
        COLON base_specifier_list 
    ;
base_specifier_list:
        base_specifier ( COMMA base_specifier )*
    ;
base_specifier:
        SCOPE? nested_name_specifier? class_name 
    |
        LITERAL_virtual access_specifier? SCOPE? nested_name_specifier? class_name 
    |
        access_specifier LITERAL_virtual? SCOPE? nested_name_specifier? class_name 
    ;
access_specifier:
        LITERAL_private
    |
        LITERAL_protected
    |
        LITERAL_public
    ;

// [gram.special] 
conversion_function_id:
        LITERAL_OPERATOR conversion_type_id 
    ;
/*
 * original rule:

conversion_type_id:
        type_specifier+ conversion_declarator?
    ;
conversion_declarator:
        ptr_operator+
    ;

 * As per 2003 standard:
 * "The conversion-type-id in a conversion-function-id is the longest possible sequence
 *  of conversion-declarators... This prevents ambiguities between the declarator operator *
 *  and its expression counterparts."
 *
 * Resolve by folding and adding a synpred.
 */
conversion_type_id:
        type_specifier+
        ((ptr_operator)=> ptr_operator)*
    ;

ctor_initializer:
        COLON mem_initializer_list
    ;

mem_initializer_list:
        mem_initializer ( COMMA mem_initializer )*
    ;

mem_initializer:
        mem_initializer_id LPAREN expression_list? RPAREN 
    ;

/*
 * original rule:
mem_initializer_id:
        SCOPE? nested_name_specifier? class_name 
    |
        IDENT 
    ;
 * Ambiguity resolved by removing special class_name case
 */
mem_initializer_id:
        SCOPE? nested_name_specifier? IDENT
    ;

// [gram.over] 
operator_function_id:
        LITERAL_OPERATOR operator_id ( { operator_is_template() }?=> LESSTHAN template_argument_list? GREATERTHAN)?
    ;
/*
 * Ambiguity between operator new/delete and operator new/delete[] resolved towards the latter.
 */
operator_id returns [int id]:
        (LITERAL_new LSQUARE RSQUARE)=>
            LITERAL_new LSQUARE RSQUARE |
        (LITERAL_delete LSQUARE RSQUARE)=>
            LITERAL_delete LSQUARE RSQUARE |
        LITERAL_new | LITERAL_delete |
        PLUS | MINUS | STAR | DIVIDE | MOD | BITWISEXOR | AMPERSAND | BITWISEOR | TILDE |
        NOT | ASSIGNEQUAL | LESSTHAN | GREATERTHAN | PLUSEQUAL | MINUSEQUAL | TIMESEQUAL | DIVIDEEQUAL | MODEQUAL |
        BITWISEXOREQUAL | BITWISEANDEQUAL | BITWISEOREQUAL | SHIFTLEFT | SHIFTRIGHT | SHIFTRIGHTEQUAL | SHIFTLEFTEQUAL | EQUAL | NOTEQUAL |
        LESSTHANOREQUALTO | GREATERTHANOREQUALTO | AND | OR | PLUSPLUS | MINUSMINUS | COMMA | POINTERTOMBR | POINTERTO | 
        LPAREN RPAREN | LSQUARE RSQUARE
    ;

// [gram.temp] 
template_declaration [decl_kind kind]:
        LITERAL_export? LITERAL_template LESSTHAN template_parameter_list GREATERTHAN declaration[kind]
    ;

template_parameter_list:
        template_parameter ( COMMA template_parameter )*
    ;

/*
 * Ambiguity resolution for LITERAL_class {IDENT,GREATERTHAN,COMMA,ASSIGNEQUAL} conflict between type_parameter
 * and type_specifier, which starts parameter_declaration.
 * To resolve this ambiguity just make an additional type_parameter syntactically predicated
 * with this fixed lookahead.
 *
 * Note that COMMA comes from template_parameter_list rule and GREATERTHAN comes even further from 
 * template_declaration rule
*/
template_parameter:
    (LITERAL_class ( IDENT | GREATERTHAN | COMMA | ASSIGNEQUAL ) )=>
        type_parameter
    |
        // this should map the rest of type_parameter that starts differently from above
        type_parameter
    |
        parameter_declaration[tparm_decl]
    ;
type_parameter:
        LITERAL_class IDENT? 
    |
        LITERAL_class IDENT? ASSIGNEQUAL type_id 
    |
        LITERAL_typename IDENT? 
    |
        LITERAL_typename IDENT? ASSIGNEQUAL type_id 
    |
        LITERAL_template LESSTHAN template_parameter_list GREATERTHAN LITERAL_class IDENT? (ASSIGNEQUAL id_expression)?
    ;

simple_template_id
    :
        IDENT LESSTHAN { (identifier_is(IDT_TEMPLATE_NAME)) }?
            template_argument_list? GREATERTHAN
    ;
lookup_simple_template_id
    :
        IDENT LESSTHAN { (identifier_is(IDT_TEMPLATE_NAME)) }?
            look_after_tmpl_args
    ;

simple_template_id_nocheck
    :
        IDENT LESSTHAN template_argument_list? GREATERTHAN
    ;
lookup_simple_template_id_nocheck
    :
        IDENT LESSTHAN look_after_tmpl_args
    ;

simple_template_id_or_IDENT
    :
        IDENT                   {action.class_name($IDENT);}
        ( (LESSTHAN { (identifier_is(IDT_TEMPLATE_NAME)) }?) =>
            LESSTHAN template_argument_list? GREATERTHAN
        )?
    ;

lookup_simple_template_id_or_IDENT
    :
        IDENT
        ( { (identifier_is(IDT_TEMPLATE_NAME)) }?=>
            LESSTHAN look_after_tmpl_args
        )?
    ;

/*
 * original rule:
template_name:
        IDENT
    ;
 * not needed
 */

template_argument_list:
        template_argument ( COMMA template_argument )*
    ;
template_argument:
        // id_exression is included into assignment_expression, thus we need to explicitly rule it up
        (id_expression)=> id_expression
    |
        (type_id)=> type_id
    |
        assignment_expression
    ;

explicit_instantiation [decl_kind kind]:
        LITERAL_template declaration[kind]
    ;
explicit_specialization [decl_kind kind]:
        LITERAL_template LESSTHAN GREATERTHAN declaration[kind]
    ;
// [gram.except] 
try_block:
        LITERAL_try compound_statement handler+
    ;
function_try_block:
        LITERAL_try ctor_initializer? function_body handler+
    ;

handler:
        LITERAL_catch LPAREN exception_declaration RPAREN compound_statement 
    ;

/*
 * original rule:
exception_declaration:
        type_specifier+ declarator
    |
        type_specifier+ abstract_declarator?
    |
        ELLIPSIS
    ;

 * Ambiguity in declarator vs abstract_declarator resolved by moving it into universal_declarator
 */
exception_declaration
scope Declaration;
@init { init_declaration(CTX, blockscope_decl); }
    :
        type_specifier+ universal_declarator?
    |
        ELLIPSIS
    ;
throw_expression:
        LITERAL_throw assignment_expression? 
    ;
exception_specification:
        LITERAL_throw LPAREN type_id_list? RPAREN 
    ;
type_id_list:
        type_id ( COMMA type_id )*
    ;

// EXPRESSIONS
// [gram.expr]
primary_expression:
        literal
    |
        LITERAL_this
    |
        LPAREN expression RPAREN 
    |
        id_expression 
    ;

/*
 * original rule:
postfix_expression:
        primary_expression
    |
        postfix_expression LSQUARE expression RSQUARE
    |
        postfix_expression LPAREN expression_list? RPAREN
    |
        simple_type_specifier LPAREN expression_list? RPAREN
    |
        LITERAL_typename SCOPE? nested_name_specifier IDENT LPAREN expression_list? RPAREN
    |
        LITERAL_typename SCOPE? nested_name_specifier LITERAL_template? template_id LPAREN expression_list? RPAREN
    |
        postfix_expression DOT LITERAL_template? id_expression
    |
        postfix_expression POINTERTO LITERAL_template? id_expression
    |
        postfix_expression DOT pseudo_destructor_name
    |
        postfix_expression POINTERTO pseudo_destructor_name
    |
        postfix_expression PLUSPLUS
    |
        postfix_expression MINUSMINUS
    |
        dynamic_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        static_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        reinterpret_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        const_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        typeid LPAREN expression RPAREN
    |
        typeid LPAREN type_id RPAREN
    ;
/*
 * Left recursion removed by moving non-recursive into basic_postfix_expression and applying "recursive"
 * parts by a loop on top of it.
 *
 * "pseudo-destructor-name" thing is heavily conflicting with id_expression,
 * so it does not make any sense to introduce. This means that id_expression should
 * allow everything pseudo-destructor-name allows, and then be semantically checked later.
 */
postfix_expression:
        basic_postfix_expression
        (
            LSQUARE expression RSQUARE
        |
            LPAREN expression_list? RPAREN
        |
            DOT
            (
                LITERAL_template? id_expression
//            |
//                pseudo_destructor_name
            )
        |
            POINTERTO
            (
                LITERAL_template? id_expression
//            |
//                pseudo_destructor_name
            )
        |
            PLUSPLUS
        |
            MINUSMINUS
        )*
    ;

basic_postfix_expression:
        primary_expression
    |
        simple_type_specifier LPAREN expression_list? RPAREN
    |
        LITERAL_typename SCOPE? nested_name_specifier (
            IDENT LPAREN expression_list? RPAREN
        |
            LITERAL_template? simple_template_id LPAREN expression_list? RPAREN
        )
    |
        LITERAL_dynamic_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        LITERAL_static_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        LITERAL_reinterpret_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        LITERAL_const_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        // AMB
        // expression and type_id conflict in "simple_type_specifier"
        // rule up type_id, as it should be easier to check
        LITERAL_typeid LPAREN ( (type_id)=> type_id |  expression ) RPAREN
    ;

expression_list:
        assignment_expression ( COMMA assignment_expression )*
    ;
/*
 * original rule:
pseudo_destructor_name:
        SCOPE? nested_name_specifier? type_name SCOPE TILDE type_name
    |
        SCOPE? nested_name_specifier LITERAL_template simple_template_id SCOPE TILDE type_name
    |
        SCOPE? nested_name_specifier? TILDE type_name
    ;

 * A healthy dose of left-factoring solves the issue.
 *
 * This rule is not used anymore

pseudo_destructor_name:
        SCOPE?
        (
            nested_name_specifier? LITERAL_template? IDENT SCOPE TILDE IDENT
        |
            nested_name_specifier LITERAL_template simple_template_id SCOPE TILDE IDENT
        )
    ;
 *
 */

/*
 * ambiguity between postfix_expression and new/delete_expression caused by presence of
 * id_expression in a former alternative and is problematic to resolve.
 * For now just synpred on new/delete. Reconsider if it appears to be costly.
 *
 * As per 2003 standard:
 * "There is an ambiguity in the unary-expression ~X(), where X is a class-name.
 * The ambiguity is resolved in favor of treating ~ as a unary complement rather than
 * treating ~X as referring to a destructor."
 */
unary_expression:
       (TILDE cast_expression)=>
             TILDE cast_expression
    |
        (new_expression)=>
            new_expression
    |
        (delete_expression)=>
            delete_expression
    |
        postfix_expression
    |
        PLUSPLUS cast_expression
    |
        MINUSMINUS cast_expression
    |
        unary_operator_but_not_TILDE cast_expression
    |
        LITERAL_sizeof (
            unary_expression
        |
            (LPAREN type_id RPAREN)=>
                LPAREN type_id RPAREN
        )
    ;

unary_operator:
        unary_operator_but_not_TILDE | TILDE
    ;
unary_operator_but_not_TILDE:
        STAR | AMPERSAND | PLUS | MINUS | NOT
    ;

/*
 * original rule:

new_expression:
        SCOPE? LITERAL_new new_placement? new_type_id new_initializer? 
    |
        SCOPE? LITERAL_new new_placement? LPAREN type_id RPAREN new_initializer? 
    ;

 *
 * Complication appears due to the optional new_placement and (type_id).
 * Unhealthy dose of left-factoring solves this issue.
 */
new_expression:
        SCOPE? LITERAL_new
        (
            new_placement ( new_type_id | LPAREN type_id RPAREN )
        |
            (LPAREN type_id RPAREN)=>
                LPAREN type_id RPAREN
        |
            new_type_id
        ) new_initializer?
    ;

new_placement:
        LPAREN expression_list RPAREN 
    ;

/*
 * As per 2003 standard:
 * "The new-type-id in a new-expression is the longest possible sequence of new-declarators"
 *
 * As all the ambiguities in new_type_id seem to come from new_declarator's ptr_operator
 * force it by synpreds.
 *
 * Is this resolution correct??
 *  new (int(*p)) int; // new-placement expression
 */
new_type_id:
        type_specifier+
        ((ptr_operator)=>
            new_declarator)?
    ;

new_declarator:
        (ptr_operator)=>
            ptr_operator new_declarator
    |
        direct_new_declarator
    ;

direct_new_declarator:
        LSQUARE expression RSQUARE ( LSQUARE constant_expression RSQUARE )*
    ;

new_initializer:
        LPAREN expression_list? RPAREN
    ;
delete_expression:
        SCOPE? LITERAL_delete cast_expression
    |
        SCOPE? LITERAL_delete LSQUARE RSQUARE cast_expression
    ;

cast_expression :
        (LPAREN type_id RPAREN)=>
            LPAREN type_id RPAREN cast_expression
    |
        unary_expression
    ;

pm_expression :
        cast_expression ( DOTMBR cast_expression | POINTERTOMBR cast_expression ) *
    ;

multiplicative_expression:
        pm_expression
        (
            STAR pm_expression
        |
            DIVIDE pm_expression
        |
            MOD pm_expression
        )*
    ;

additive_expression:
        multiplicative_expression ( PLUS multiplicative_expression | MINUS multiplicative_expression )*
    ;

shift_expression:
        additive_expression ( SHIFTLEFT additive_expression | SHIFTRIGHT additive_expression )*
    ;

/*
 * GREATERTHAN ambiguity (GREATERTHAN in relational expression vs GREATERTHAN closing template arguments list) is one of
 * C++ dumbest ambiguities. Resolve it by tracking whether expression is a top-level expression (e.g. not
 * parenthesized) and parsed in a context of template argument - then do not accept is as a continuation of
 * relational expression.
 */
relational_expression:
        shift_expression
        ( 
            { !top_level_of_template_arguments() }?=>
            GREATERTHAN shift_expression
          |
            LESSTHAN shift_expression
          |
            LESSTHANOREQUALTO shift_expression
          |
            GREATERTHANOREQUALTO shift_expression
        )*
    ;
equality_expression:
        relational_expression ( EQUAL relational_expression | NOTEQUAL relational_expression)*
    ;
and_expression:
        equality_expression ( AMPERSAND equality_expression )*
    ;
exclusive_or_expression:
        and_expression ( BITWISEXOR and_expression )*
    ;
inclusive_or_expression:
        exclusive_or_expression ( BITWISEOR exclusive_or_expression )*
    ;
logical_and_expression:
        inclusive_or_expression ( AND inclusive_or_expression )*
    ;
logical_or_expression:
        logical_and_expression ( OR logical_and_expression )*
    ;
conditional_expression:
        logical_or_expression (QUESTIONMARK expression COLON assignment_expression)?
    |
        QUESTIONMARK expression COLON assignment_expression
    ;
/*
 * These are the example of "precedence climbing" implementation
 *

binary_operator returns [ int prec]:
        PLUS| MINUS |
        STAR | DIVIDE | MOD | BITWISEXOR | AMPERSAND | BITWISEOR |
        NOT | LESSTHAN | GREATERTHAN |
        SHIFTLEFT | SHIFTRIGHT |
        EQUAL | NOTEQUAL | LESSTHANOREQUALTO | GREATERTHANOREQUALTO | AND | OR
    ;

fast_expression:
        climbing_expression[0]
    ;

climbing_expression [int prio]:
        primary_climbing
        ((binary_operator { $binary_operator.prec >= prio }? )=>
         binary_operator  climbing_expression[$binary_operator.prec+1])?
    ;

primary_climbing:
        unary_operator climbing_expression[$unary_operator.prec]
    ;
*/

/*
 * original rule:

assignment_expression:
        conditional_expression 
    |
        logical_or_expression assignment_operator assignment_expression 
    |

 * Ambiguity on logical_or_expression in assignment vs conditional_expression.
 * Resolved by unpretty rule-splitting and left-factoring.
 */
assignment_expression:
        // this is taken from conditional_expression
        QUESTIONMARK expression COLON assignment_expression
    |
        logical_or_expression (
            // this is taken from conditional_expression
            (QUESTIONMARK expression COLON assignment_expression)?
        |
            assignment_operator assignment_expression
        )
    |
        throw_expression
    ;

assignment_operator:
        ASSIGNEQUAL | TIMESEQUAL | DIVIDEEQUAL | MODEQUAL | PLUSEQUAL | MINUSEQUAL | SHIFTRIGHTEQUAL | SHIFTLEFTEQUAL |
        BITWISEANDEQUAL | BITWISEXOREQUAL | BITWISEOREQUAL
    ;

expression:
        assignment_expression ( COMMA assignment_expression )*
    ;

constant_expression returns [ expression_t expr ]
    :
        conditional_expression
    ;

// [gram.lex]

literal:
    DECIMALINT|FLOATONE|CHAR_LITERAL|STRING_LITERAL
    ;

// lookahead stuff
// token list arg_syms from parseutil.cc, to implement look_after_tmpl_args

// $<Look ahead

lookahead_tokenset_arg_syms
    :
        IDENT|DECIMALINT|FLOATONE|CHAR_LITERAL|STRING_LITERAL|
        PLUS|MINUS|STAR|AMPERSAND|LITERAL_sizeof|TILDE|
        NOT|PLUSPLUS|MINUSMINUS|LITERAL_OPERATOR|LITERAL_new|LITERAL_delete|
        LITERAL_this|
        LITERAL_void|LITERAL_char|LITERAL_short|LITERAL_long|LITERAL_float|LITERAL_double|LITERAL_signed|LITERAL_unsigned|LITERAL_int|
        DIVIDE|SHIFTLEFT|SHIFTRIGHT|BITWISEOR|AND|OR|BITWISEXOR|
        EQUAL|LESSTHANOREQUALTO|GREATERTHANOREQUALTO|NOTEQUAL|
        ASSIGNEQUAL|BITWISEANDEQUAL|DIVIDEEQUAL|SHIFTLEFTEQUAL|SHIFTRIGHTEQUAL|MINUSEQUAL|PLUSEQUAL|
        MODEQUAL|TIMESEQUAL|BITWISEOREQUAL|BITWISEXOREQUAL|DOT|MOD|
        POINTERTO|QUESTIONMARK|COLON|SCOPE|DOTMBR|POINTERTOMBR|COMMA|ELLIPSIS|
        LITERAL_typedef|LITERAL_extern|LITERAL_static|LITERAL_auto|LITERAL_register|LITERAL___thread|
        LITERAL_const|LITERAL_volatile|LITERAL_struct|LITERAL_union|LITERAL_class|LITERAL_enum|LITERAL_typename|
        LITERAL___offsetof|LITERAL___alignof|LITERAL_throw|LITERAL_wchar_t|LITERAL_typeid|
        LITERAL_const_cast|LITERAL_static_cast|LITERAL_dynamic_cast|LITERAL_reinterpret_cast|
        LITERAL_bool|LITERAL_true|LITERAL_false|
        LITERAL___global|LITERAL___symbolic|LITERAL___hidden|LITERAL___declspec|
        LITERAL___attribute__|LITERAL___typeof__|
        IS_ENUM|IS_UNION|IS_CLASS|IS_POD|IS_ABSTRACT|HAS_VIRT_DESTR|IS_EMPTY|IS_BASEOF|IS_POLYMORPH
    ;

look_after_tmpl_args
scope {
    int level;
}
@init{ 
    $look_after_tmpl_args::level = 0;
    int npar = 0;
    int nbrac = 0;
}
    :
        (
            // this gets us out if GREATERTHAN is met when level == 0
            (GREATERTHAN {
                    ($look_after_tmpl_args::level > 0)
                  }? )=>
            GREATERTHAN
                {{ if (npar == 0 && nbrac == 0) {
                            $look_after_tmpl_args::level--;
                            println("level-- (", $look_after_tmpl_args::level);
                        }
                }}
        |
            LESSTHAN {{ if (npar == 0 && nbrac == 0) {
                            $look_after_tmpl_args::level++;
                            println("level++ (", $look_after_tmpl_args::level);
                    }
                }}
        |
            LPAREN {{ npar++; }}
        |
            RPAREN {{ if (npar > 0) npar--; }}
        |
            LSQUARE {{ nbrac++; }}
        |
            RSQUARE {{ if (nbrac > 0) nbrac--; }}
        |
            lookahead_tokenset_arg_syms
        )* GREATERTHAN
    ;

skip_balanced_Curl
            :
            LCURLY
            (options {greedy=false;}:
                skip_balanced_Curl | .
            )*
            RCURLY
        ;

// $>
// ==============
//

// LITERAL_template: 'template';
// COLON: ':'; SCOPE: '::';

// DOT: '.'; ELLIPSIS: '...';
// MINUS: '-'; PLUS: '+'; MINUSMINUS: '--'; PLUSPLUS: '++'; PLUSEQUAL: '+='; MINUSEQUAL: '-=';

// POINTERTO: '->'; 
// STAR: '*'; TIMESEQUAL: '*='; DOTMBR: '.*'; POINTERTOMBR: '->*';
// DIVIDE: '/'; DIVIDEEQUAL: '/=';
// MOD: '%'; MODEQUAL: '%=';
// NOT: '!';
// ASSIGNEQUAL: '=';
// EQUAL: '=='; LESSTHANOREQUALTO: '<='; GREATERTHANOREQUALTO: '>='; NOTEQUAL: '!=';
// AMPERSAND: '&'; AND: '&&'; BITWISEANDEQUAL: '&=';
// BITWISEOR: '|'; OR: '||'; BITWISEOREQUAL: '|=';
// BITWISEXOREQUAL: '^='; BITWISEXOR: '^'; 
// SHIFTLEFT: '<<'; SHIFTRIGHT: '>>'; SHIFTLEFTEQUAL: '<<='; SHIFTRIGHTEQUAL: '>>=';
// LITERAL_this: 'this';
// LITERAL_typename: 'typename';
// LITERAL_typeid: 'typeid';
// LPAREN: '('; RPAREN: ')';
// LSQUARE: '['; RSQUARE: ']';
// LCURLY: '{'; RCURLY: '}';
// LESSTHAN: '<'; GREATERTHAN: '>';

// LITERAL_char: 'char';
// LITERAL_wchar_t: 'wchar_t';
// LITERAL_bool: 'bool'; 
// LITERAL_true: 'true';
// LITERAL_false: 'false';
// LITERAL_short: 'short';
// LITERAL_int: 'int';
// LITERAL_long: 'long';
// LITERAL_signed: 'signed';
// LITERAL_unsigned: 'unsigned';
// LITERAL_float: 'float';
// LITERAL_double: 'double';
// LITERAL_void: 'void';

// LITERAL_enum: 'enum'; LITERAL_class: 'class'; LITERAL_struct: 'struct'; LITERAL_union: 'union';

// LITERAL_dynamic_cast: 'dynamic_cast';
// LITERAL_static_cast: 'static_cast';
// LITERAL_reinterpret_cast: 'reinterpret_cast';
// LITERAL_const_cast: 'const_cast';

// COMMA: ',';
// TILDE: '~';

// LITERAL_new: 'new'; LITERAL_delete: 'delete';
// LITERAL_namespace: 'namespace'; LITERAL_using: 'using';

// LITERAL_OPERATOR: 'operator';

// LITERAL_friend: 'friend';
// LITERAL_typedef: 'typedef';
// LITERAL_auto: 'auto';
// LITERAL_register: 'register';
// LITERAL_static: 'static';
// LITERAL_extern: 'extern';
// LITERAL_mutable: 'mutable';
// LITERAL_inline: 'inline';
// LITERAL_virtual: 'virtual';
// LITERAL_explicit: 'explicit';
// LITERAL_export: 'export';
// LITERAL_private: 'private';
// LITERAL_protected: 'protected';
// LITERAL_public: 'public';
// SEMICOLON: ';';

// LITERAL_try: 'try'; LITERAL_catch: 'catch'; LITERAL_throw: 'throw';

// LITERAL_const: 'const'; LITERAL_volatile: 'volatile';
// LITERAL_asm: 'asm';
// LITERAL_break: 'break'; LITERAL_continue: 'continue'; LITERAL_return: 'return';

// LITERAL_goto: 'goto';
// LITERAL_for: 'for'; LITERAL_while: 'while'; LITERAL_do: 'do';
// LITERAL_if: 'if'; LITERAL_else: 'else';
// LITERAL_switch: 'switch'; LITERAL_case: 'case'; LITERAL_default: 'default';

// QUESTIONMARK: '?'; LITERAL_sizeof: 'sizeof';
// LITERAL___offsetof: '__offsetof';
// LITERAL___thread: '__thread';

// LITERAL___global: '__global';
// LITERAL___symbolic: '__symbolic';
// LITERAL___hidden: '__hidden';

// LITERAL___declspec: '__declspec';
// LITERAL___attribute__: '__attribute__';
// LITERAL___typeof__: '__typeof__';
// LITERAL___alignof: '__alignof';

// LITERAL__Pragma: '_Pragma';

// HAS_TRIVIAL_DESTR: '__oracle_has_trivial_destructor';
// HAS_VIRTUAL_DESTR: '__oracle_has_virtual_destructor';
// IS_ENUM: '__oracle_is_enum';
// IS_UNION: '__oracle_is_union';
// IS_CLASS: '__oracle_is_class';
// IS_POD: '__oracle_is_pod';
// IS_ABSTRACT: '__oracle_is_abstract';
// IS_EMPTY: '__oracle_is_empty';
// IS_POLYMORPH: '__oracle_is_polymorphic';
// IS_BASEOF: '__oracle_is_base_of';

// CHAR_LITERAL
//     :   '\'' ( EscapeSequence | ~('\''|'\\') ) '\''
//     ;

// STRING
//     :  '"' STRING_GUTS '"'
//     ;

// fragment
// STRING_GUTS :	( EscapeSequence | ~('\\'|'"') )* ;

// fragment
// HEX_LITERAL : '0' ('x'|'X') HexDigit+ IntegerTypeSuffix? ;

// fragment
// DECIMAL_LITERAL : ('0' | '1'..'9' '0'..'9'*) IntegerTypeSuffix? ;

// fragment
// OCTAL_LITERAL : '0' ('0'..'7')+ IntegerTypeSuffix? ;

// fragment
// HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;

// fragment
// IntegerTypeSuffix
//     :	('l'|'L')
//     |	('u'|'U')  ('l'|'L')?
//     ;

// fragment
// Exponent : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

// fragment
// FloatTypeSuffix : ('f'|'F'|'d'|'D') ;

// fragment
// EscapeSequence
//     :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
//     |   OctalEscape
//     ;

// fragment
// OctalEscape
//     :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
//     |   '\\' ('0'..'7') ('0'..'7')
//     |   '\\' ('0'..'7')
//     ;

// fragment
// UnicodeEscape
//     :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
//     ;

// WS  :  (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;}
//     ;

// COMMENT
//     :   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
//     ;

// LINE_COMMENT
//     : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
//     ;

// LITERAL:
//         HEX_LITERAL
//     |
//         DECIMAL_LITERAL
//     |
//         OCTAL_LITERAL
//     ;

// IDENT
//     :	LETTER (LETTER|'0'..'9')*
//     ;
	
// fragment
// LETTER
//     :	'$'
//     |	'A'..'Z'
//     |	'a'..'z'
//     |	'_'
//     ;

/*END*/
