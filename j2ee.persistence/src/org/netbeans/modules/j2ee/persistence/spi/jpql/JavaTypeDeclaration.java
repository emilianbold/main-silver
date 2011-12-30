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
package org.netbeans.modules.j2ee.persistence.spi.jpql;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.persistence.jpa.jpql.spi.IType;
import org.eclipse.persistence.jpa.jpql.spi.ITypeDeclaration;
import org.eclipse.persistence.jpa.jpql.spi.ITypeRepository;

/**
 * TODO: rewrite
 * @author sp153251
 */
public class JavaTypeDeclaration implements ITypeDeclaration {

    /**
     * Determines whether the type declaration represents an array.
     */
    private boolean array;
    /**
     * The actual type that contains the generics, if any is present.
     */
    private Object genericType;
    /**
     * The cached {@link ITypeDeclaration ITypeDeclarations} representing the generics of the {@link
     * Type}.
     */
    private ITypeDeclaration[] genericTypes;
    /**
     * The external form of the Java type.
     */
    private final IType type;
    /**
     * The repository of {@link IType ITypes}.
     */
    private ITypeRepository typeRepository;

    /**
     * Creates a new <code>JavaTypeDeclaration</code>.
     *
     * @param typeRepository The repository of {@link IType ITypes}
     * @param type The external form of the Java type
     * @param genericType The actual type that contains the generics, if any is present
     * @param array Determines whether the type declaration represents an array
     */
    JavaTypeDeclaration(ITypeRepository typeRepository,
            IType type,
            Object genericType,
            boolean array) {

        super();
        this.type = type;
        this.array = array;
        this.genericType = genericType;
        this.typeRepository = typeRepository;
    }

    private String buildArrayTypeName(String arrayTypeName) {

        StringBuilder sb = new StringBuilder();
        int index = arrayTypeName.indexOf('[');
        int dimensionality = (arrayTypeName.length() - index) / 2;
        String typeName = arrayTypeName.substring(0, index);

        while (--dimensionality >= 0) {
            sb.append("[");
        }

        String elementType = elementType(typeName);

        sb.append(elementType);
        sb.append(typeName);

        if (elementType.equals("L")) {
            sb.append(";");
        }

        return sb.toString();
    }

    private ITypeDeclaration[] buildParameterTypes() {

        List<ITypeDeclaration> parameterTypes = new ArrayList<ITypeDeclaration>();

        // Example: Class<T>
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            for (java.lang.reflect.Type tp : parameterizedType.getActualTypeArguments()) {
                ITypeDeclaration typeParameter = buildTypeDeclaration(tp);
                parameterTypes.add(typeParameter);
            }
        } // T[]
        else if (genericType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) genericType;
            parameterTypes.add(buildTypeDeclaration(genericArrayType.getGenericComponentType()));
        } // Example: Class
        else if (genericType.getClass() == Class.class) {
            ITypeDeclaration typeParameter = buildTypeDeclaration((Class<?>) genericType);
            parameterTypes.add(typeParameter);
        } // Example: <K, V>
        else if (genericType.getClass() == Class[].class) {
            for (Class<?> javaType : ((Class<?>[]) genericType)) {
                ITypeDeclaration typeParameter = buildTypeDeclaration(javaType);
                parameterTypes.add(typeParameter);
            }
        } // Example: <K, V>
        else if (genericType.getClass() == IType[].class) {
            for (IType tp : ((IType[]) genericType)) {
                ITypeDeclaration typeParameter = new JavaTypeDeclaration(typeRepository, tp, null, false);
                parameterTypes.add(typeParameter);
            }
        }

        return parameterTypes.toArray(new ITypeDeclaration[parameterTypes.size()]);
    }

    private JavaTypeDeclaration buildTypeDeclaration(Class<?> javaType) {
        return new JavaTypeDeclaration(
                typeRepository,
                typeRepository.getType(javaType),
                null,
                javaType.isArray());
    }

    private JavaTypeDeclaration buildTypeDeclaration(Object genericType) {

        // <T1, ..., Tn>
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            return buildTypeDeclaration(parameterizedType.getRawType());
        }

        // <T>
        if (genericType instanceof TypeVariable) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) genericType;
            for (java.lang.reflect.Type tp : typeVariable.getBounds()) {
                return buildTypeDeclaration(tp);
            }
            return buildTypeDeclaration(Object.class);
        }

        // ?
        if (genericType instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) genericType;
            for (java.lang.reflect.Type tp : wildcardType.getUpperBounds()) {
                return buildTypeDeclaration(tp);
            }
            return buildTypeDeclaration(Object.class);
        }

        // T[]
        if (genericType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) genericType;
            String arrayTypeName = buildArrayTypeName(genericArrayType.toString());
            IType arrayType = typeRepository.getType(arrayTypeName);

            return new JavaTypeDeclaration(
                    typeRepository,
                    arrayType,
                    genericArrayType.getGenericComponentType(),
                    true);
        }

        return buildTypeDeclaration((Class<?>) genericType);
    }

    private String elementType(String typeName) {

        if (typeName.equals("boolean")) {
            return "Z";
        }
        if (typeName.equals("byte")) {
            return "B";
        }
        if (typeName.equals("char")) {
            return "C";
        }
        if (typeName.equals("double")) {
            return "D";
        }
        if (typeName.equals("float")) {
            return "F";
        }
        if (typeName.equals("int")) {
            return "I";
        }
        if (typeName.equals("long")) {
            return "J";
        }
        if (typeName.equals("short")) {
            return "S";
        }

        return "L";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDimensionality() {
        if (array) {
            String name = type.getName();
            int index = 0;
            while (name.charAt(index) == '[') {
                index++;
            }
            return index;
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IType getType() {
        return type;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeDeclaration[] getTypeParameters() {
        if (genericTypes == null) {
            if (genericType == null) {
                genericTypes = new ITypeDeclaration[0];
            } else {
                genericTypes = buildParameterTypes();
            }
        }
        return genericTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isArray() {
        return array;
    }
}
