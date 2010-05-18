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
package org.netbeans.modules.dlight.perfan.storage.impl;

import java.util.List;
import org.junit.Test;
import org.netbeans.modules.dlight.core.stack.api.ThreadDump;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshot.MemoryAccessType;
import org.netbeans.modules.dlight.perfan.stack.impl.FunctionCallImpl;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class DataraceImplTest {

    @Test
    public void testFromErprint() {
        List<DataraceImpl> dataraces = DataraceImpl.fromErprint(new String[]{
        "",
        "Total Races:  1 Experiment:  /tmp/dlight_av202691/experiment_2.er",
        "",
        "Race #1, Vaddr: 0x804a040",
        "      Access 1: Write, work + 0x00000072,",
        "                       line 54 in \"pi_pthreads.c\"",
        "      Access 2: Write, work + 0x00000072,",
        "                       line 54 in \"pi_pthreads.c\"",
        "  Total Traces: 2",
        "  Trace 1",
        "      Access 1: Write",
        "                work + 0x00000072, line 54 in \"pi_pthreads.c\"",
        "                thread_hj_start_routine + 0x00000067",
        "                collector_root + 0x0000005A",
        "                start_thread + 0x000000B8",
        "                clone + 0x0000005C",
        "      Access 2: Write",
        "                work + 0x00000072, line 54 in \"pi_pthreads.c\"",
        "                thread_hj_start_routine + 0x00000067",
        "                collector_root + 0x0000005A",
        "                start_thread + 0x000000B8",
        "                clone + 0x0000005C",
        "  Trace 2",
        "      Access 1: Write",
        "                work + 0x00000072, line 54 in \"pi_pthreads.c\"",
        "                thread_hj_start_routine + 0x00000067",
        "                collector_root + 0x0000005A",
        "                start_thread + 0x000000B8",
        "                clone + 0x0000005C",
        "      Access 2: Write",
        "                work + 0x00000072, line 54 in \"pi_pthreads.c\"",
        "                main + 0x00000092, line 74 in \"pi_pthreads.c\"",
        "                __libc_start_main + 0x000000E2",
        "                _start + 0x0000003C"});
        assertEquals(1, dataraces.size());

        DataraceImpl r1 = dataraces.get(0);
        assertEquals(0x804a040, r1.getAddress());
        assertEquals(2, r1.getThreadDumps().size());

        ThreadDump td1 = r1.getThreadDumps().get(0);
        assertEquals(2, td1.getThreadStates().size());
        assertEquals(MemoryAccessType.WRITE, td1.getThreadStates().get(0).getMemoryAccessType());
        assertEquals(5, td1.getThreadStates().get(0).getStack().size());
        assertEquals("clone", td1.getThreadStates().get(0).getStack().get(0).getFunction().getName());
        assertEquals("work", td1.getThreadStates().get(0).getStack().get(4).getFunction().getName());
        assertEquals(54, td1.getThreadStates().get(0).getStack().get(4).getOffset());
        assertEquals("pi_pthreads.c", ((FunctionCallImpl)td1.getThreadStates().get(0).getStack().get(4)).getSourceFile());
        assertEquals(5, td1.getThreadStates().get(1).getStack().size());

        ThreadDump td2 = r1.getThreadDumps().get(1);
        assertEquals(2, td2.getThreadStates().size());
        assertEquals(MemoryAccessType.WRITE, td2.getThreadStates().get(0).getMemoryAccessType());
        assertEquals(5, td2.getThreadStates().get(0).getStack().size());
        assertEquals(4, td2.getThreadStates().get(1).getStack().size());
    }

    @Test
    public void testFromErprintOpenmp() {
        List<DataraceImpl> dataraces = DataraceImpl.fromErprint(new String[]{
        "",
        "Total Races:  1 Experiment:  /var/tmp/dlight_mt154047/experiment_4.er",
        "",
        "Race #1, Vaddr: 0x8047818",
        "      Access 1: Write, main, ",
        "                       line 57 in \"pi_omp.c\"",
        "      Access 2: Write, main, ",
        "                       line 57 in \"pi_omp.c\"",
        "  Total Traces: 2",
        "  Trace 1",
        "      Access 1: Write",
        "                main, line 57 in \"pi_omp.c\"",
        "                main, line 52 in \"pi_omp.c\"",
        "                _start + 0x00000078",
        "      Access 2: Write",
        "                main, line 57 in \"pi_omp.c\"",
        "                main, line 52 in \"pi_omp.c\"",
        "                _start + 0x00000078",
        "  Trace 2",
        "      Access 1: Read ",
        "                main, line 53 in \"pi_omp.c\"",
        "                main, line 52 in \"pi_omp.c\"",
        "                _start + 0x00000078",
        "      Access 2: Write",
        "                main, line 57 in \"pi_omp.c\"",
        "                main, line 52 in \"pi_omp.c\"",
        "                _start + 0x00000078",
        ""});

        assertEquals(1, dataraces.size());

        DataraceImpl r1 = dataraces.get(0);
        assertEquals(0x8047818, r1.getAddress());
        assertEquals(2, r1.getThreadDumps().size());

        ThreadDump td1 = r1.getThreadDumps().get(0);
        assertEquals(2, td1.getThreadStates().size());
        assertEquals(MemoryAccessType.WRITE, td1.getThreadStates().get(0).getMemoryAccessType());
        assertEquals(3, td1.getThreadStates().get(0).getStack().size());
        assertEquals("_start", td1.getThreadStates().get(0).getStack().get(0).getFunction().getName());
        assertEquals("main", td1.getThreadStates().get(0).getStack().get(2).getFunction().getName());
        assertEquals(57, td1.getThreadStates().get(0).getStack().get(2).getOffset());
        assertEquals("pi_omp.c", ((FunctionCallImpl)td1.getThreadStates().get(0).getStack().get(2)).getSourceFile());
        assertEquals(3, td1.getThreadStates().get(1).getStack().size());

        ThreadDump td2 = r1.getThreadDumps().get(1);
        assertEquals(2, td2.getThreadStates().size());
        assertEquals(MemoryAccessType.READ, td2.getThreadStates().get(0).getMemoryAccessType());
        assertEquals(3, td2.getThreadStates().get(0).getStack().size());
        assertEquals(3, td2.getThreadStates().get(1).getStack().size());
    }

    @Test
    public void testFromErprintFortranOpenmp() {
        List<DataraceImpl> dataraces = DataraceImpl.fromErprint(new String[]{
        "",
        "Total Races:  1 Experiment:  /var/tmp/dlight_av202691/experiment_2.er",
        "",
        "Race #1, Vaddr: 0x80610fc",
        "      Access 1: Write, MAIN_ -- OMP parallel region from line 16 [_$p1A16.MAIN_] + 0x00000028, ",
        "                       line 17 in \"test.f\"",
        "      Access 2: Write, MAIN_ -- OMP parallel region from line 16 [_$p1A16.MAIN_] + 0x00000028, ",
        "                       line 17 in \"test.f\"",
        "  Total Traces: 2",
        "  Trace 1",
        "      Access 1: Write",
        "                MAIN_ -- OMP parallel region from line 16 [_$p1A16.MAIN_] + 0x00000028, line 17 in \"test.f\"",
        "                thread_hj_start_routine + 0x00000067",
        "                _thrp_setup + 0x0000007C",
        "                _lwp_start + 0x00000000",
        "      Access 2: Write",
        "                MAIN_ -- OMP parallel region from line 16 [_$p1A16.MAIN_] + 0x00000028, line 17 in \"test.f\"",
        "                thread_hj_start_routine + 0x00000067",
        "                _thrp_setup + 0x0000007C",
        "                _lwp_start + 0x00000000",
        "  Trace 2",
        "      Access 1: Write",
        "                MAIN_ -- OMP parallel region from line 16 [_$p1A16.MAIN_] + 0x00000028, line 17 in \"test.f\"",
        "                thread_hj_start_routine + 0x00000067",
        "                _thrp_setup + 0x0000007C",
        "                _lwp_start + 0x00000000",
        "      Access 2: Write",
        "                MAIN_ -- OMP parallel region from line 16 [_$p1A16.MAIN_] + 0x00000028, line 17 in \"test.f\"",
        "                MAIN + 0x0000005D, line 16 in \"test.f\"",
        "                main + 0x00000082",
        "                _start + 0x00000078",
        ""});

        assertEquals(1, dataraces.size());

        DataraceImpl r1 = dataraces.get(0);
        assertEquals(0x80610fc, r1.getAddress());
        assertEquals(2, r1.getThreadDumps().size());

        ThreadDump td1 = r1.getThreadDumps().get(0);
        assertEquals(2, td1.getThreadStates().size());
        assertEquals(MemoryAccessType.WRITE, td1.getThreadStates().get(0).getMemoryAccessType());
        assertEquals(4, td1.getThreadStates().get(0).getStack().size());
        assertEquals("_lwp_start", td1.getThreadStates().get(0).getStack().get(0).getFunction().getName());
        assertEquals("MAIN_ -- OMP parallel region from line 16 [_$p1A16.MAIN_]", td1.getThreadStates().get(0).getStack().get(3).getFunction().getName());
        assertEquals(17, td1.getThreadStates().get(0).getStack().get(3).getOffset());
        assertEquals("test.f", ((FunctionCallImpl)td1.getThreadStates().get(0).getStack().get(3)).getSourceFile());
        assertEquals(4, td1.getThreadStates().get(1).getStack().size());

        ThreadDump td2 = r1.getThreadDumps().get(1);
        assertEquals(2, td2.getThreadStates().size());
        assertEquals(MemoryAccessType.WRITE, td2.getThreadStates().get(0).getMemoryAccessType());
        assertEquals(4, td2.getThreadStates().get(0).getStack().size());
        assertEquals(4, td2.getThreadStates().get(1).getStack().size());
    }
}
