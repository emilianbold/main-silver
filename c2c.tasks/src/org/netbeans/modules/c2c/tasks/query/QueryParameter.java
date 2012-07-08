/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.c2c.tasks.query;

import com.tasktop.c2c.internal.client.tasks.core.data.CfcTaskAttribute;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListModel;

/**
 *
 * @author Tomas Stupka
 */
public abstract class QueryParameter {
    

    protected boolean alwaysDisabled = false;
    private final CfcTaskAttribute attribute;
    
    public QueryParameter(CfcTaskAttribute attribute) {
        this.attribute = attribute;
    }
    public CfcTaskAttribute getAttribute() {
        return attribute;
    }
    
    abstract String getValues();
    abstract void setValues(String values);
    
    void setAlwaysDisabled(boolean bl) {
        this.alwaysDisabled = bl;
        setEnabled(false); // true or false, who cares. this is only to trigger the state change
    }
    
    abstract void setEnabled(boolean b);

//    public StringBuffer get(boolean encode) {
//        StringBuffer sb = new StringBuffer();
//        ParameterValue[] values = getValues();
//        for (ParameterValue pv : values) {
//            sb.append("&"); // NOI18N
//            sb.append(getParameter());
//            sb.append("="); // NOI18N
//            if(encode) {
//                try {
//                    String value = pv.getValue();
//                    if(value.equals("[Bug+creation]")) {                            // NOI18N
//                        // workaround: while encoding '+' in a products name works fine,
//                        // encoding it in in [Bug+creation] causes an error
//                        sb.append(URLEncoder.encode("[", encoding));                // NOI18N
//                        sb.append("Bug+creation");                                  // NOI18N
//                        sb.append(URLEncoder.encode("]", encoding));                // NOI18N
//                    } else {
//                        // use URLEncoder as it is used also by other clients of the bugzilla connector
//                        sb.append(URLEncoder.encode(value, encoding));
//                    }
//                } catch (UnsupportedEncodingException ex) {
//                    sb.append(URLEncoder.encode(pv.getValue()));
//                    C2C.LOG.log(Level.WARNING, null, ex);
//                }
//            } else {
//                sb.append(pv.getValue());
//            }
//        }
//        return sb;
//    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("["); // NOI18N
        sb.append(getAttribute().getKey());
        sb.append(":");
        sb.append(getAttribute().getLabel());
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    static class ComboParameter extends QueryParameter {
        private final JComboBox combo;
        public ComboParameter(JComboBox combo, CfcTaskAttribute attribute) {
            super(attribute);
            this.combo = combo;
            combo.setModel(new DefaultComboBoxModel());
        }
        
        @Override
        public String getValues() {
            return (String) combo.getSelectedItem();
        }
        
        public void setParameterValues(String values) {
            setParameterValues(values.split(","));
        }
        
        public void setParameterValues(String[] values) {
            combo.setModel(new DefaultComboBoxModel(values));
        }
        
        @Override
        public void setValues(String values) {
            if(values.isEmpty()) {
                return;
            }
            
            if(values == null) {
                combo.setSelectedIndex(-1);
                return;
            }

            // need the index as the given ParameterValue might have a different displayName
            int idx = ((DefaultComboBoxModel)combo.getModel()).getIndexOf(values);
            if(idx != -1) {
                combo.setSelectedIndex(idx);
            } 
        }
        @Override
        void setEnabled(boolean b) {
            combo.setEnabled(alwaysDisabled ? false : b);
        }
    }

    static class ListParameter extends QueryParameter {
        private final JList list;
        public ListParameter(JList list, CfcTaskAttribute attribute) {
            super(attribute);
            this.list = list;
            list.setModel(new DefaultListModel());
        }
        @Override
        public String getValues() {
            Object[] values = list.getSelectedValues();
            if(values == null || values.length == 0) {
                return null; //EMPTY_PARAMETER_VALUE;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                sb.append(values[i]);
                if(i < values.length - 1) {
                    sb.append(",");
                }
            }
            return sb.toString();
        }
        
        public void setParameterValues(String values) {
            setParameterValues(values.split(","));
        }
        
        public void setParameterValues(String[] values) {
            DefaultListModel m = new DefaultListModel();
            for (String pv : values) {
                m.addElement(pv);
            }
            list.setModel(m);
        }

        @Override
        public void setValues(String valuesString) {
            if(valuesString.isEmpty()) return;                                        // should not happen        XXX do we need this?
            list.clearSelection();
            String[] values = valuesString.split(",");
            if(values.length == 1 && "".equals(values[0].trim())) return;    // 1 empty ParameterValue stands for no selection XXX rewrite this
            List<Integer> selectionList = new LinkedList<Integer>();
            for (int i = 0; i < values.length; i++) {
                ListModel model = list.getModel();
                // need case sensitive compare
                for(int j = 0; j < model.getSize(); j++) {
                    String value = (String) model.getElementAt(j);
                    if(value.toLowerCase().equals(values[i].toLowerCase())) {
                        selectionList.add(j);
                        break;
                    }
                }
            }
            int[] selection = new int[selectionList.size()];
            int i = 0;
            for (int s : selectionList) {
                selection[i++] = s;
            }
            list.setSelectedIndices(selection);
            int idx = selection.length > 0 ? selection[0] : -1;
            if(idx > -1) list.scrollRectToVisible(list.getCellBounds(idx, idx));
        }
        @Override
        void setEnabled(boolean  b) {
            list.setEnabled(alwaysDisabled ? false : b);
        }
    }

    static class TextFieldParameter extends QueryParameter {
        private final JTextField txt;
        public TextFieldParameter(JTextField txt, CfcTaskAttribute attribute) {
            super(attribute);
            this.txt = txt;
        }
        @Override
        public String getValues() {
            String value = txt.getText();
            if(value == null || value.equals("")) { // NOI18N
                return null; //EMPTY_PARAMETER_VALUE;
            }
            return value;
        }
        @Override
        public void setValues(String value) {
            if(value == null) {
                value = "";
            }
            txt.setText(value); // NOI18N
        }
        @Override
        void setEnabled(boolean  b) {
            txt.setEnabled(alwaysDisabled ? false : b);
        }
    }

    static class CheckBoxParameter extends QueryParameter {
        private String selected = "true"; // NOI18N
        private final JCheckBox chk;
        public CheckBoxParameter(JCheckBox chk, CfcTaskAttribute attribute) {
            super(attribute);
            this.chk = chk;
        }
        @Override
        public String getValues() {
            return chk.isSelected() ? selected : null; //EMPTY_PARAMETER_VALUE;
        }
        @Override
        public void setValues(String value) {
            chk.setSelected(Boolean.parseBoolean(value)); // NOI18N
        }
        @Override
        void setEnabled(boolean  b) {
            chk.setEnabled(alwaysDisabled ? false : b);
        }
    }


//    public static class SimpleQueryParameter extends QueryParameter {
//        private final String[] values;
//
//        public SimpleQueryParameter(CfcTaskAttribute attribute, String[] values) {
//            super(attribute);
//            this.values = values;
//        }
//
//        @Override
//        String getValues() {
//            if(values == null || values.length == 0) {
//                return null; //EMPTY_PARAMETER_VALUE;;
//            }
//            ParameterValue[] ret = new ParameterValue[values.length];
//            for (int i = 0; i < values.length; i++) {
//                ret[i] = new ParameterValue(values[i]);
//            }
//            return ret;
//        }
//
//        @Override
//        void setValues(String values) {
//            // not interested
//        }
//
//        @Override
//        void setEnabled(boolean  b) {
//            // interested
//        }
//    }
}
