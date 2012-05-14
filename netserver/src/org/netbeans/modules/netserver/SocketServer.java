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
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.netserver;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author ads
 *
 */
public class SocketServer implements Runnable {
    
    protected static final Logger LOG = Logger.getLogger( SocketServer.class.getCanonicalName());
    
    public SocketServer(SocketAddress address ) throws IOException {
        keys = new ConcurrentLinkedQueue<SelectionKey>();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        serverChannel.socket().bind(address);
        selector = Selector.open();

        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            while (!stop) {
                while (true) {
                    SelectionKey key = keys.poll();
                    if (key == null) {
                        break;
                    }
                    else {
                        if (key.isValid()) {
                            key.interestOps(SelectionKey.OP_WRITE);
                        }
                    }
                }
                getSelector().select();

                for (Iterator<SelectionKey> iterator = getSelector().selectedKeys()
                        .iterator(); iterator.hasNext();)
                {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    try {
                        if (key.isAcceptable()) {
                            acceptConnection(key);
                        }
                        else if (key.isReadable()) {
                            readData(key);
                        }
                        else if (key.isWritable()) {
                            writeData(key);
                        }
                    }
                    catch( ClosedChannelException e ){
                        chanelClosed( key );
                        close(key);
                    }
                }
            }
            serverChannel.close();
        }
        catch (IOException e) {
            LOG.log(Level.WARNING, null, e);
        }
    }
    
    protected void chanelClosed( SelectionKey key ) {
    }

    public void stop(){
        stop = true;
        getSelector().wakeup();
    }
    
    public void send( byte[] data , SelectionKey key ){
        getWriteQueue(key).add(ByteBuffer.wrap(data));
        keys.add(key);
        getSelector().wakeup();
    }
    
    public void close( SelectionKey key ) throws IOException {
        key.channel().close();
        key.cancel();
    }
    
    protected Queue<ByteBuffer> getWriteQueue( SelectionKey key ){
        Object attachment = key.attachment();
        return (Queue<ByteBuffer>) attachment;
    }
    
    protected void setReadHandler( ReadHandler handler ){
        this.handler = handler;
    }
    
    protected ReadHandler getReadHandler(){
        return handler;
    }
    
    protected SocketAddress getAddress(){
        return serverChannel.socket().getLocalSocketAddress();
    }
    
    protected Selector getSelector(){
        return selector;
    }
    
    protected void initWriteQueue( SelectionKey key ){
        if ( key.attachment() == null ){
            key.attach( new ConcurrentLinkedQueue<ByteBuffer>());
        } 
    }
    
    private void writeData( SelectionKey key ) throws IOException  {
        Queue<ByteBuffer> queue = getWriteQueue(key);
        while( queue!= null ){
            ByteBuffer buffer = queue.poll();
            if ( buffer == null ){
                break;
            }
            else {
                ((SocketChannel)key.channel()).write( buffer);
            }
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void readData( SelectionKey key ) throws IOException {
        initWriteQueue(key);
        handler.read(key);
    }

    private void acceptConnection( SelectionKey key ) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(getSelector(), SelectionKey.OP_READ);
    }

    private ServerSocketChannel serverChannel; 
    private Selector selector;
    private Queue<SelectionKey> keys;
    private ReadHandler handler;
    private volatile boolean stop;
}
