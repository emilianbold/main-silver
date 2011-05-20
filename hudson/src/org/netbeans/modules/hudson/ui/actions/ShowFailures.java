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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.ui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.CallstackFrameNode;
import org.netbeans.modules.gsf.testrunner.api.DiffViewAction;
import org.netbeans.modules.gsf.testrunner.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.TestMethodNode;
import org.netbeans.modules.gsf.testrunner.api.TestRunnerNodeFactory;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.TestsuiteNode;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.spi.HudsonLogger;
import org.netbeans.modules.hudson.ui.interfaces.OpenableInBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.hudson.ui.actions.Bundle.*;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Action to display test failures.
 */
public class ShowFailures extends AbstractAction implements Runnable {

    private static final Logger LOG = Logger.getLogger(ShowFailures.class.getName());
    
    private final HudsonJob job;
    private final String url;
    private final String displayName;

    public ShowFailures(HudsonJobBuild build) {
        this(build.getJob(), build.getUrl(), build.getDisplayName());
    }

    public ShowFailures(HudsonMavenModuleBuild module) {
        this(module.getBuild().getJob(), module.getUrl(), module.getBuildDisplayName());
    }

    @Messages("ShowFailures.label=Show Test Failures")
    private ShowFailures(HudsonJob job, String url, String displayName) {
        this.job = job;
        this.url = url;
        this.displayName = displayName;
        putValue(NAME, ShowFailures_label());
    }

    public void actionPerformed(ActionEvent e) {
        new RequestProcessor(url + "testReport").post(this); // NOI18N
    }

    private static final Pattern ASSERTION_FAILURE = Pattern.compile("(?m)junit[.]framework[.](AssertionFailedError|(Array)?ComparisonFailure)|java[.]lang[.]AssertionError($|: )");

    @Messages({
        "# {0} - job #build", "ShowFailures.title={0} Test Failures",
        "# {0} - class & method name of failed test", "# {1} - suite name of failed test", "ShowFailures.from_suite={0} (from {1})",
        "LBL_GotoSource=Go to Source"
    })
    public void run() {
        try {
            XMLReader parser = XMLUtil.createXMLReader();
            parser.setContentHandler(new DefaultHandler() {
                InputOutput io;
                StringBuilder buf;
                Hyperlinker hyperlinker = new Hyperlinker(job);
                TestSession session = new TestSession(displayName, new Project() {
                    public @Override FileObject getProjectDirectory() {
                        return FileUtil.createMemoryFileSystem().getRoot();
                    }
                    public @Override Lookup getLookup() {
                        return Lookup.EMPTY;
                    }
                }, TestSession.SessionType.TEST, new TestRunnerNodeFactory() {
                    public @Override TestsuiteNode createTestSuiteNode(String suiteName, boolean filtered) {
                        // XXX could add OpenableInBrowser
                        return new TestsuiteNode(suiteName, filtered);
                    }
                    public @Override Node createTestMethodNode(final Testcase testcase, Project project) {
                        return new TestMethodNode(testcase, project, Lookups.singleton(new OpenableInBrowser() {
                            public @Override String getUrl() {
                                return url + "testReport/" + testcase.getClassName().replaceFirst("[.][^.]+$", "") + "/" + testcase.getClassName().replaceFirst(".+[.]", "") + "/" + testcase.getName() + "/";
                            }
                        })) {
                            public @Override Action[] getActions(boolean context) {
                                return new Action[] {
                                    // XXX singleton disabled since TR window has no activatedNodes (and no other parent of ResultTreeView implements Lookup.Provider)
                                    OpenUrlAction.get(OpenUrlAction.class).createContextAwareInstance(Lookups.singleton(this)),
                                    new DiffViewAction(testcase),
                                };
                            }
                        };
                    }
                    public @Override Node createCallstackFrameNode(String frameInfo, String displayName) {
                        return new CallstackFrameNode(frameInfo, displayName) {
                            public @Override Action getPreferredAction() {
                                return new AbstractAction(LBL_GotoSource()) {
                                    FileObject f;
                                    int line;
                                    {
                                        // XXX should have utility API to parse stack traces
                                        Matcher m = Pattern.compile("\tat (.+[.])[^.]+[.][^.]+[(]([^.]+[.]java):([0-9]+)[)]").matcher(frameInfo);
                                        if (m.matches()) {
                                            String resource = m.group(1).replace('.', '/') + m.group(2);
                                            f = GlobalPathRegistry.getDefault().findResource(resource);
                                            line = Integer.parseInt(m.group(3));
                                            LOG.log(Level.FINER, "matched {0} -> {1}", new Object[] {resource, f});
                                        } else {
                                            LOG.log(Level.FINER, "no match for {0}", frameInfo);
                                        }
                                        setEnabled(f != null);
                                    }
                                    public @Override void actionPerformed(ActionEvent e) {
                                        if (f != null) {
                                            HudsonLogger.Helper.openAt(f, line - 1, -1, true);
                                        }
                                    }
                                };
                            }
                            public @Override Action[] getActions(boolean context) {
                                return new Action[] {getPreferredAction()};
                            }
                        };
                    }
                });
                private void prepareOutput() {
                    if (io == null) {
                        String title = ShowFailures_title(displayName);
                        io = IOProvider.getDefault().getIO(title, new Action[0]);
                        io.select();
                        Manager.getInstance().testStarted(session);
                    }
                }
                class Suite {
                    String name;
                    String stdout;
                    String stderr;
                    Stack<Case> cases = new Stack<Case>();
                    List<Case> casesDone = new ArrayList<Case>();
                    long duration;
                }
                class Case {
                    String className;
                    String name;
                    String errorStackTrace;
                    long duration;
                }
                long parseDuration(String d) {
                    if (d == null) {
                        return 0;
                    }
                    try {
                        return (long) (1000 * Float.parseFloat(d));
                    } catch (NumberFormatException x) {
                        return 0;
                    }
                }
                Stack<Suite> suites = new Stack<Suite>();
                public @Override void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (qName.matches("errorStackTrace|stdout|stderr|name|className")) { // NOI18N
                        buf = new StringBuilder();
                    } else if (qName.equals("suite")) { // NOI18N
                        suites.push(new Suite());
                    } else if (qName.equals("case") && !suites.empty()) { // NOI18N
                        suites.peek().cases.push(new Case());
                    }
                }
                public @Override void characters(char[] ch, int start, int length) throws SAXException {
                    if (buf != null) {
                        buf.append(ch, start, length);
                    }
                }
                public @Override void endElement(String uri, String localName, String qName) throws SAXException {
                    if (suites.empty()) {
                        return;
                    }
                    Suite s = suites.peek();
                    String text = buf != null && buf.length() > 0 ? buf.toString() : null;
                    buf = null;
                    if (s.cases.empty()) { // suite level
                        if (qName.equals("stdout")) { // NOI18N
                            s.stdout = text;
                        } else if (qName.equals("stderr")) { // NOI18N
                            s.stderr = text;
                        } else if (qName.equals("name")) { // NOI18N
                            s.name = text;
                        } else if (qName.equals("duration")) { // NOI18N
                            s.duration = parseDuration(text);
                        }
                    } else { // case level
                        Case c = s.cases.peek();
                        if (qName.equals("errorStackTrace")) { // NOI18N
                            c.errorStackTrace = text;
                        } else if (qName.equals("name")) { // NOI18N
                            c.name = text;
                        } else if (qName.equals("className")) { // NOI18N
                            c.className = text;
                        } else if (qName.equals("duration")) { // NOI18N
                            c.duration = parseDuration(text);
                        }
                    }
                    if (qName.equals("suite")) { // NOI18N
                        try {
                            show(s);
                        } catch (IOException x) {
                            LOG.log(Level.FINE, null, x);
                        }
                        suites.pop();
                    } else if (qName.equals("case")) { // NOI18N
                        s.casesDone.add(s.cases.pop());
                    }
                }
                void show(Suite s) throws IOException {
                    prepareOutput();
                    OutputWriter out = io.getOut();
                    OutputWriter err = io.getErr();
                    TestSuite suite = new TestSuite(s.name);
                    session.addSuite(suite);
                    Manager.getInstance().displaySuiteRunning(session, suite.getName());
                    if (s.stderr != null) {
                        // XXX TR window does not seem to show only stdio from selected suite
                        Manager.getInstance().displayOutput(session, s.stderr, true);
                    }
                    if (s.stdout != null) {
                        Manager.getInstance().displayOutput(session, s.stdout, false);
                    }
                    for (final Case c : s.casesDone) {
                        if (c.errorStackTrace == null) {
                            continue;
                        }
                        String name = c.className + "." + c.name;
                        String shortName = c.name;
                        if (s.name != null && !s.name.equals(c.className)) {
                            shortName = name;
                            name = ShowFailures_from_suite(name, s.name);
                        }
                        println();
                        out.println("[" + name + "]"); // XXX use color printing to make it stand out?
                        show(c.errorStackTrace, /* err is too hard to read */ out);
                        Testcase test = new Testcase(shortName, null, session);
                        test.setClassName(c.className);
                        Trouble trouble = new Trouble(!ASSERTION_FAILURE.matcher(c.errorStackTrace).lookingAt());
                        trouble.setStackTrace(c.errorStackTrace.split("\r?\n"));
                        // XXX call setComparisonFailure if matches "expected:<...> but was:<...>"
                        test.setTrouble(trouble);
                        LOG.log(Level.FINE, "got {0} as {1}", new Object[] {name, test.getStatus()});
                        test.setTimeMillis(c.duration);
                        session.addTestCase(test);
                    }
                    if (s.stderr != null || s.stdout != null) {
                        println();
                        show(s.stderr, err);
                        show(s.stdout, out);
                    }
                    Manager.getInstance().displayReport(session, session.getReport(s.duration));
                }
                boolean firstLine = true;
                void println() {
                    if (firstLine) {
                        firstLine = false;
                    } else {
                        io.getOut().println();
                    }
                }
                void show(String lines, OutputWriter w) {
                    if (lines == null) {
                        return;
                    }
                    for (String line : lines.split("\r\n?|\n")) { // NOI18N
                        hyperlinker.handleLine(line, w);
                    }
                }
                public @Override void endDocument() throws SAXException {
                    if (io != null) {
                        io.getOut().close();
                        io.getErr().close();
                        Manager.getInstance().sessionFinished(session);
                    }
                }
            });
            // XXX could use ?tree (would be faster) if there were an alternate object for failed tests only
            String u = url + "testReport/api/xml?xpath=//suite[case/errorStackTrace]&wrapper=failures"; // NOI18N
            InputSource source = new InputSource(new ConnectionBuilder().job(job).url(u).connection().getInputStream());
            source.setSystemId(u);
            parser.parse(source);
        } catch (FileNotFoundException x) {
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception x) {
            Toolkit.getDefaultToolkit().beep();
            LOG.log(Level.INFO, null, x);
        }
    }

}
