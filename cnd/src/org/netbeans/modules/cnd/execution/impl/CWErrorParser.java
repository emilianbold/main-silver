package org.netbeans.modules.cnd.execution.impl;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class CWErrorParser extends ErrorParser {

    private static final Pattern CW_ERROR_SCANNER = Pattern.compile("([^:\n]*):([0-9]+): .*"); // NOI18N
    private static final Pattern[] patterns = new Pattern[]{CW_ERROR_SCANNER};

    public CWErrorParser(ExecutionEnvironment execEnv, FileObject relativeTo) {
        super(execEnv, relativeTo);
    }

    public Result handleLine(String line) throws IOException {
        for (int pi = 0; pi < patterns.length; pi++) {
            Pattern p = patterns[pi];
            Matcher m = p.matcher(line);
            boolean found = m.find();
            if (found && m.start() == 0) {
                return handleLine(line, m);
            }
        }
        return null;
    }

    private Result handleLine(String line, Matcher m) throws IOException {
        if (m.pattern() == CW_ERROR_SCANNER) {
            try {
                String file = m.group(1);
                Integer lineNumber = Integer.valueOf(m.group(2));
                FileObject fo = FileUtil.toFileObject(CndFileUtils.normalizeFile(new File(FileUtil.toFile(relativeTo), file)));
                if (fo == null) {
                    return NO_RESULT;
                }
                return new Results(line, new OutputListenerImpl(fo, lineNumber.intValue() - 1), true);
            } catch (NumberFormatException e) {
            }
        }
        return NO_RESULT;
    }
}
