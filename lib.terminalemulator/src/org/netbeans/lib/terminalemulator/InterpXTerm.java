/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 *			"Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 * Contributor(s): Ivan Soleimanipour.
 */

package org.netbeans.lib.terminalemulator;


class InterpXTerm extends InterpProtoANSIX {

    protected static class InterpTypeXTerm extends InterpTypeProtoANSIX {

	protected final Actor act_done_collect_escbs = new ACT_DONE_COLLECT_ESCBS();

	protected InterpTypeXTerm() {
	    st_esc_rb_N.setAction((char) 27, st_wait, act_nop);         // ESC
	    st_wait.setAction('\\', st_base, act_done_collect_escbs);
	}

	static final class ACT_DONE_COLLECT_ESCBS implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
		InterpProtoANSIX i = (InterpProtoANSIX) ai;
                int semix = i.text.indexOf(';');
                if (semix == -1)
                    return null;
                String p1 = i.text.substring(0, semix);
                String p2 = i.text.substring(semix+1);
                int code = Integer.parseInt(p1);
                switch (code) {
                    case 0:
                        ai.ops.op_icon_name(p2);
                        ai.ops.op_win_title(p2);
                        break;
                    case 1:
                        ai.ops.op_icon_name(p2);
                        break;
                    case 2:
                        ai.ops.op_win_title(p2);
                        break;
                    case 3:
                        /* LATER
                        cwd is a dttermism. For xterm we're supposed to set X properties
                        ai.ops.op_cwd(p2);
                        */
                        break;

                    case 10: {
                        // This is specific to nbterm!
                        int semix2 = p2.indexOf(';');
                        if (semix == -1)
                            return null;
                        String p3 = p2.substring(semix2+1);
                        p2 = p2.substring(0, semix2);
                        ai.ops.op_hyperlink(p2, p3);
                    }
                }
		return null;
	    }
	}

    }

    private InterpTypeXTerm type;

    private static final InterpTypeXTerm type_singleton = new InterpTypeXTerm();

    public InterpXTerm(Ops ops) {
	super(ops, type_singleton);
	this.type = type_singleton;
	setup();
    } 

    protected InterpXTerm(Ops ops, InterpTypeXTerm type) {
	super(ops, type);
	this.type = type;
	setup();
    } 

    @Override
    public String name() {
	return "xterm";	// NOI18N
    } 

    @Override
    public void reset() {
	super.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean dispatchAttr(AbstractInterp ai, int n) {
        switch (n) {
            case 0:
            case 1:
            case 4:
            case 5:
            case 7:
            case 8:

            case 22:
            case 24:
            case 25:
            case 27:
            case 28:

            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:

            case 39:

            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:

            case 49:
                ai.ops.op_attr(n);
                return true;
            default:
                return false;
        }
    }

    private void setup() {
    }
}