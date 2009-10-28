/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 */

package org.netbeans.modules.cnd.dwarfdump;

import java.io.IOException;
import java.io.RandomAccessFile;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;

/**
 *
 * @author Alexander Simon
 */
public class FileMagic {
    private MyRandomAccessFile reader;
    private Magic magic;
    
    public FileMagic(String objFileName) throws WrongFileFormatException, IOException {
        reader = new MyRandomAccessFile(objFileName);
        try {
            readMagic();
        } catch (WrongFileFormatException ex){
            throw new WrongFileFormatException(ex.getMessage()+":"+objFileName);

        }
    }

    public RandomAccessFile getReader() {
        return reader;
    }

    public Magic getMagic() {
        return magic;
    }

    private void readMagic() throws WrongFileFormatException {
        byte[] bytes = new byte[8];
        try {
            reader.readFully(bytes);
        } catch (IOException ex) {
            dispose();
            throw new WrongFileFormatException("Not an ELF/PE/COFF/MACH-O file"); // NOI18N
        }
        if (isElfMagic(bytes)) {
            magic = Magic.Elf;
        } else if (isCoffMagic(bytes)) {
            magic = Magic.Coff;
        } else if (isExeMagic(bytes)) {
            magic = Magic.Exe;
        } else if (isPeMagic(bytes)) {
            magic = Magic.Pe;
        } else if (isMachoMagic(bytes)) {
            magic = Magic.Macho;
        } else if (isArchiveMagic(bytes)) {
            magic = Magic.Arch;
        } else {
            dispose();
            throw new WrongFileFormatException("Not an ELF/PE/COFF/MACH-O file"); // NOI18N
        }
    }

    public void dispose(){
        if (reader != null) {
            reader.dispose();
            reader = null;
        }
    }
    
    public static boolean isExeMagic(byte[] bytes){
        return bytes[0] == 'M' && bytes[1] == 'Z'; // NOI18N
    }

    public static boolean isPeMagic(byte[] bytes){
        return bytes[0] == 'P' && bytes[1] == 'E' && bytes[2] == 0 && bytes[3] == 0; // NOI18N
    }

    public static boolean isCoffMagic(byte[] bytes){
        return bytes[0] == 0x4c && bytes[1] == 0x01;
    }
    
    public static boolean isElfMagic(byte[] bytes){
        return bytes[0] == 0x7f && bytes[1] == 'E' && bytes[2] == 'L' && bytes[3] == 'F'; // NOI18N
    }
    
    public static boolean isMachoMagic(byte[] bytes){
        return (bytes[0] == (byte)0xce || bytes[0] == (byte)0xcf) && bytes[1] == (byte)0xfa && bytes[2] == (byte)0xed && bytes[3] == (byte)0xfe;
    }
    
    public static boolean isArchiveMagic(byte[] bytes){
        return bytes[0] == '!' && bytes[1] == '<' && bytes[2] == 'a' && bytes[3] == 'r' && // NOI18N
                bytes[4] == 'c' && bytes[5] == 'h' && bytes[6] == '>' && bytes[7] == '\n'; // NOI18N
    }

    private static final class MyRandomAccessFile extends RandomAccessFile {

        private static final int BUF_SIZE = 8 * 1024;
        private String fileName;
        private byte buffer[] = new byte[BUF_SIZE];
        private int buf_end = 0;
        private int buf_pos = 0;
        private long real_pos = 0;
        private static final boolean TRACE_STATISTIC = false;
        private long countOfReads = 0;
        private long countOfBufferReads = 0;

        private MyRandomAccessFile(String fileName) throws IOException {
            super(fileName, "r"); // NOI18N
            this.fileName = fileName;
            invalidate();
        }

        private void invalidate() throws IOException {
            buf_end = 0;
            buf_pos = 0;
            real_pos = super.getFilePointer();
        }

        private int fillBuffer() throws IOException {
            if (TRACE_STATISTIC) {
                countOfBufferReads++;
            }
            int n = super.read(buffer, 0, BUF_SIZE);
            if (n >= 0) {
                real_pos += n;
                buf_end = n;
                buf_pos = 0;
            }
            return n;
        }

        @Override
        public int read() throws IOException {
            if (TRACE_STATISTIC) {
                countOfReads++;
            }
            if (buf_pos >= buf_end) {
                if (fillBuffer() < 0) {
                    return -1;
                }
            }
            if (buf_end == 0) {
                return -1;
            } else {
                return (0xff & buffer[buf_pos++]);
            }
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {
            int leftover = buf_end - buf_pos;
            if (len <= leftover) {
                System.arraycopy(buffer, buf_pos, b, off, len);
                buf_pos += len;
                return len;
            }
            for (int i = 0; i < len; i++) {
                int c = read();
                if (c != -1) {
                    b[off + i] = (byte) c;
                } else {
                    if (i==0) {
                        return -1;
                    }
                    return i;
                }
            }
            return len;
        }

        @Override
        public long getFilePointer() throws IOException {
            long l = real_pos;
            return (l - buf_end + buf_pos);
        }

        @Override
        public void seek(long pos) throws IOException {
            int n = (int) (real_pos - pos);
            if (n >= 0 && n <= buf_end) {
                buf_pos = buf_end - n;
            } else {
                super.seek(pos);
                invalidate();
            }
        }

        public void dispose() {
            if (TRACE_STATISTIC) {
                if (buffer != null) {
                    System.err.println("File " + fileName); // NOI18N
                    try {
                        System.err.println("\tFile Length= " + length()); // NOI18N
                    } catch (IOException ex) {
                    }
                    System.err.println("\tByte Reads=  " + countOfReads); // NOI18N
                    System.err.println("\tBuffer Reads=" + countOfBufferReads); // NOI18N
                }
            }
            try {
                close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            buffer = null;
        }
    }
}
