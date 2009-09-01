package org.netbeans.modules.cnd.execution.impl;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;

public final class SUNErrorParser extends ErrorParser {

    private static final Pattern SUN_ERROR_SCANNER_CPP_ERROR = Pattern.compile("^\"(.*)\", line ([0-9]+): Error:"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_CPP_WARNING = Pattern.compile("^\"(.*)\", line ([0-9]+): Warning:"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_C_ERROR = Pattern.compile("^\"(.*)\", line ([0-9]+):"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_C_WARNING = Pattern.compile("^\"(.*)\", line ([0-9]+): warning:"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_FORTRAN_ERROR = Pattern.compile("^\"(.*)\", Line = ([0-9]+),"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_FORTRAN_WARNING = Pattern.compile("^\"(.*)\", Line = ([0-9]+), Column = ([0-9]+): WARNING:"); // NOI18N
    private static final Pattern SUN_DIRECTORY_ENTER = Pattern.compile("\\(([^)]*)\\)[^:]*:"); // NOI18N

    private static final Pattern SS_OF_1 = Pattern.compile("::\\(.*\\)");// NOI18N
    private static final Pattern SS_OF_2 = Pattern.compile(":\\(.*\\).*");// NOI18N
    private static final Pattern SS_OF_3 = Pattern.compile("\\(.*\\).*:");// NOI18N
    private static final Pattern[] SunStudioOutputFilters = new Pattern[] {SS_OF_1, SS_OF_2, SS_OF_3};

    public SUNErrorParser(ExecutionEnvironment execEnv, FileObject relativeTo) {
        super(execEnv, relativeTo);
    }

    public Result handleLine(String line, Matcher m) throws IOException {
        Result res = _handleLine(line, m);
        if (res == NO_RESULT){
            // Remove lines extra lines from Sun Compiler output
            for (int i = 0; i < SunStudioOutputFilters.length; i++) {
                Matcher skipper = SunStudioOutputFilters[i].matcher(line);
                boolean found = skipper.find();
//                System.out.println("  " + found);
//                if (found)
//                    System.out.println("  " + m.start());
                if (found && skipper.start() == 0) {
                    return REMOVE_LINE;
                }
            }
        }
        return res;
    }

    public Result _handleLine(String line, Matcher m) throws IOException {
        if (m.pattern() == SUN_DIRECTORY_ENTER) {
            FileObject myObj = resolveFile(m.group(1));
            if (myObj != null) {
                relativeTo = myObj;
            }
            return NO_RESULT;
        }
        if (m.pattern() == SUN_ERROR_SCANNER_CPP_ERROR || m.pattern() == SUN_ERROR_SCANNER_CPP_WARNING || m.pattern() == SUN_ERROR_SCANNER_C_ERROR ||
            m.pattern() == SUN_ERROR_SCANNER_C_WARNING || m.pattern() == SUN_ERROR_SCANNER_FORTRAN_ERROR || m.pattern() == SUN_ERROR_SCANNER_FORTRAN_WARNING) {
            try {
                String file = m.group(1);
                Integer lineNumber = Integer.valueOf(m.group(2));
                //FileObject fo = relativeTo.getFileObject(file);
                FileObject fo = resolveRelativePath(relativeTo, file);
                boolean important = m.pattern() == SUN_ERROR_SCANNER_CPP_ERROR || m.pattern() == SUN_ERROR_SCANNER_C_ERROR ||
                                    m.pattern() == SUN_ERROR_SCANNER_FORTRAN_ERROR;
                if (fo != null) {
                    return new Results(line, new OutputListenerImpl(fo, lineNumber.intValue() - 1), important);
                }
            } catch (NumberFormatException e) {
            }
            return NO_RESULT;
        }
        throw new IllegalArgumentException("Unknown pattern: " + m.pattern().pattern());
    }

    public Pattern[] getPattern() {
        return new Pattern[]{SUN_ERROR_SCANNER_CPP_ERROR, SUN_ERROR_SCANNER_CPP_WARNING, SUN_ERROR_SCANNER_FORTRAN_WARNING,
                             SUN_ERROR_SCANNER_FORTRAN_ERROR, SUN_ERROR_SCANNER_C_WARNING, SUN_ERROR_SCANNER_C_ERROR, SUN_DIRECTORY_ENTER};
    }

}
