/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.mime4j.storage;

import junit.framework.TestCase;

public class DefaultStorageProviderTest extends TestCase {

    @Override
    protected void tearDown() throws Exception {
        System.getProperties().remove(
                DefaultStorageProvider.DEFAULT_STORAGE_PROVIDER_PROPERTY);
        DefaultStorageProvider.reset();
    }

    public void testDefaultInstance() throws Exception {
        System.getProperties().remove(
                DefaultStorageProvider.DEFAULT_STORAGE_PROVIDER_PROPERTY);
        DefaultStorageProvider.reset();

        StorageProvider instance = DefaultStorageProvider.getInstance();
        assertTrue(instance instanceof ThresholdStorageProvider);
    }

    public void testSetDefaultProperty() throws Exception {
        System.setProperty(
                DefaultStorageProvider.DEFAULT_STORAGE_PROVIDER_PROPERTY,
                MemoryStorageProvider.class.getName());
        DefaultStorageProvider.reset();

        StorageProvider instance = DefaultStorageProvider.getInstance();
        assertTrue(instance instanceof MemoryStorageProvider);
    }

    public void testSetter() throws Exception {
        StorageProvider instance = new MemoryStorageProvider();

        DefaultStorageProvider.setInstance(instance);
        assertSame(instance, DefaultStorageProvider.getInstance());
    }

}
