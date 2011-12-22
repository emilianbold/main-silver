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
package org.netbeans.modules.web.common.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.logging.Level;


/**
 * @author ads
 *
 */
class WebSocketHandler76 extends WebSocketHandler75 implements WebSocketChanelHandler {

    public WebSocketHandler76( WebSocketServer webSocketServer , SelectionKey key) {
        super(webSocketServer, key );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketHandler75#sendHandshake()
     */
    @Override
    public void sendHandshake( ) {
        byte[] lastEightBytes = readRequestContent( getKey() );
        if ( lastEightBytes == null ){
            return;
        }
        StringBuilder builder = new StringBuilder(HTTP_RESPONSE);
        builder.append(CRLF);
        builder.append(WS_UPGRADE);
        builder.append(CRLF);
        builder.append(CONN_UPGRADE);
        builder.append(CRLF);
        builder.append("Sec-WebSocket-Origin: ");           // NOI18N
        String origin = getServer().getContext(getKey()).getHeaders().get("Origin");  // NOI18N
        if ( origin != null ){
            builder.append( origin);
        }
        builder.append(CRLF);
        builder.append("Sec-WebSocket-Location: ws://");    // NOI18N
        String host = getServer().getContext(getKey()).getHeaders().get(HOST);                
        if ( host != null) {
            builder.append( host );
        }
        else {
            builder.append("127.0.0.1:");                   // NOI18N
            builder.append( ((InetSocketAddress)getServer().getAddress()).getPort());
        }
        String request = getServer().getContext(getKey()).getRequestString();
        int index = request.indexOf(' ');
        String url = null;
        if ( index != -1 ){
            request = request.substring(index).trim();
            index = request.indexOf(' ');
            if ( index !=-1 ){
                url = request.substring( 0, index ).trim();
            }
        }
        else {
            url ="/";                                       // NOI18N
        }
        builder.append( url );
        builder.append( CRLF );
        builder.append( CRLF );
        byte[] headers = builder.toString().getBytes( 
                Charset.forName(WebSocketServer.UTF_8));
        byte[] responseContent = createResponseContent(getKey(), lastEightBytes);
        if ( responseContent == null ){
            close( getKey() );
        }
        byte[] response = new byte[ headers.length + responseContent.length ];
        System.arraycopy(headers, 0, response, 0, headers.length);
        System.arraycopy(responseContent, 0, response, headers.length, 
                responseContent.length);
        getServer().send(response , getKey() );
    }

    private byte[] createResponseContent(SelectionKey key,  byte[] lastEightBytes ) {
        Map<String, String> headers = getServer().getContext(key).getHeaders();
        String key1 = headers.get(WebSocketServer.KEY1);
        String key2 = headers.get(WebSocketServer.KEY2);
        ByteBuffer buffer = ByteBuffer.allocate(16).putInt(decodeNumber(key1)).
            putInt(decodeNumber(key2)).put(lastEightBytes);
        byte[] bytes = new byte[ buffer.capacity()];
        buffer.get( bytes );
        try {
            return  MessageDigest.getInstance("MD5").digest(bytes);     // NOI18N
        }
        catch( NoSuchAlgorithmException e ){
            WebSocketServer.LOG.log(Level.WARNING , null , e);
            return null;
        }
    }

    private byte[] readRequestContent( SelectionKey key ) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            while ( buffer.hasRemaining() ){
                if ( socketChannel.read( buffer ) == -1){
                    close( key );
                }
            }
            byte[] bytes = new byte[buffer.capacity()];
            buffer.flip();
            buffer.get( bytes );
            return bytes;
        }
        catch( IOException e ){
            close( key );
        }
        return null;
    }
    
    private int decodeNumber(String code) {
        long number = 0;
        int spaces = 0;
        for (int i=0; i<code.length(); i++) {
            char c = code.charAt(i);
            if (c >= '0' && c <= '9') {
                number *= 10;
                number += (c-'0');
            }
            if (c == ' ') {
                spaces++;
            }
        }
        return (int)(number/spaces);
    }

}
