/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.protocol;

import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Timestamp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PostgreSQLDateBinaryProtocolValueTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Test
    public void assertGetColumnLength() {
        assertThat(new PostgreSQLDateBinaryProtocolValue().getColumnLength(""), is(8));
    }
    
    @Test
    public void assertRead() {
        when(payload.readInt8()).thenReturn(1L);
        assertThat(new PostgreSQLDateBinaryProtocolValue().read(payload), is(1L));
    }
    
    @Test
    public void assertWrite() {
        Timestamp data = new Timestamp(System.currentTimeMillis());
        new PostgreSQLDateBinaryProtocolValue().write(payload, data);
        verify(payload).writeInt8(data.getTime());
    }
    
}

