/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License. 
 */
package com.vaadin.data.provider.hierarchical;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.data.TreeData;
import com.vaadin.data.provider.HierarchicalDataCommunicator;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.internal.StateNode;

import elemental.json.JsonValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class HierarchicalCommunicatorTest {

    private static final String ROOT = "ROOT";
    private static final String FOLDER = "FOLDER";
    private static final String LEAF = "LEAF";
    private TreeDataProvider<String> dataProvider;
    private HierarchicalDataCommunicator<String> communicator;
    private TreeData<String> treeData;
    private final int pageSize = 50;

    private class UpdateQueue implements Update {
        @Override
        public void clear(int start, int length) {
        }

        @Override
        public void set(int start, List<JsonValue> items) {
        }

        @Override
        public void commit(int updateId) {
        }
    }

    private final ArrayUpdater arrayUpdater = new ArrayUpdater() {
        @Override
        public UpdateQueue startUpdate(int sizeChange) {
            return new UpdateQueue();
        }

        @Override
        public void initialize() {
        }
    };
    
    @Before
    public void setUp() {
        treeData = new TreeData<>();
        treeData.addItems(null, ROOT);
        treeData.addItems(ROOT, FOLDER);
        treeData.addItems(FOLDER, LEAF);
        dataProvider = new TreeDataProvider<>(treeData);
        communicator = new HierarchicalDataCommunicator<>(
                Mockito.mock(CompositeDataGenerator.class),
                arrayUpdater, json -> {
                },
                Mockito.mock(StateNode.class), null);
        communicator.setDataProvider(dataProvider, null);
    }

    @Test
    public void testFolderRemoveRefreshAll() {
        testItemRemove(FOLDER, true);
    }

    @Test
    public void testLeafRemoveRefreshAll() {
        testItemRemove(LEAF, true);
    }

    @Test
    public void testFolderRemove() {
        testItemRemove(FOLDER, false);
    }

    @Test
    public void testLeafRemove() {
        testItemRemove(LEAF, false);
    }

    private void testItemRemove(String item, boolean refreshAll) {
        communicator.expand(ROOT, pageSize);
        communicator.expand(FOLDER, pageSize);
        // Put the item into client queue
        communicator.refresh(item);
        treeData.removeItem(item);
        if (refreshAll) {
            dataProvider.refreshAll();
        } else {
            dataProvider.refreshItem(item);
        }
    }

    @Test
    public void testReplaceAll() {
        // Some modifications
        communicator.expand(ROOT, pageSize);
        communicator.expand(FOLDER, pageSize);
        communicator.refresh(LEAF);
        // Replace dataprovider
        communicator.setDataProvider(new TreeDataProvider<>(new TreeData<>()),
                null);
        dataProvider.refreshAll();
        assertFalse("Stalled object in KeyMapper",
                communicator.getKeyMapper().has(ROOT));
        assertEquals(-1, communicator.getParentIndex(FOLDER).longValue());
    }

}
