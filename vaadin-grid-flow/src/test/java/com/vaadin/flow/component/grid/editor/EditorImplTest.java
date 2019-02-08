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
package com.vaadin.flow.component.grid.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.StatusChangeEvent;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

public class EditorImplTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Grid<String> grid;
    private TestEditor editor;
	private MockUI ui;

    private static class TestEditor extends EditorImpl<String> {

        private List<String> refreshedItems = new ArrayList<>();

        public TestEditor(Grid<String> grid) {
            super(grid, null);
        }

        @Override
        protected void refresh(String item) {
            refreshedItems.add(item);
        }
    }

	private void fakeClientCommunication() {
		ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
		ui.getInternals().getStateTree().collectChanges(ignore -> {
		});
	}

    @Before
    public void setUp() {
        grid = new Grid<>();
		ui = new MockUI();
		ui.getElement().appendChild(grid.getElement());
        editor = new TestEditor(grid);

        editor.setBinder(new Binder<>());
        grid.getDataCommunicator().getKeyMapper().key("bar");
    }

    @Test(expected = IllegalStateException.class)
    public void editItem_itemIsNotKnown_throw() {
        editor.editItem("foo");
		fakeClientCommunication();
    }

    @Test(expected = IllegalStateException.class)
    public void editItem_noBinder_throw() {
        editor = new TestEditor(grid);
        editor.editItem("bar");
		fakeClientCommunication();
    }

    @Test(expected = IllegalStateException.class)
    public void editItem_editorIsBufferedAndOpen_throw() {
        grid.getDataCommunicator().getKeyMapper().key("foo");

        editor.setBuffered(true);
        editor.editItem("bar");
		fakeClientCommunication();
        editor.editItem("foo");
		fakeClientCommunication();
    }

    @Test
    public void editItem_itemIsKnown_binderStatusEventAndEditorOpenEvent() {
        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> openEventCapure = new AtomicReference<>();
        assertOpenEvents(statusEventCapture, openEventCapure);

        // In not buffered mode there is the bean in the binder
        Assert.assertEquals("bar", editor.getBinder().getBean());
    }

    @Test
    public void editItem_itemIsKnown_binderIsInBufferedMode_binderStatusEventAndEditorOpenEvent() {
        editor.setBuffered(true);

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> openEventCapure = new AtomicReference<>();
        assertOpenEvents(statusEventCapture, openEventCapure);

        // In not buffered mode there is no bean in the binder
        Assert.assertNull("bar", editor.getBinder().getBean());
    }

    @Test
    public void editItem_switchEditedItem_itemsAreRefreshed() {
        grid.getDataCommunicator().getKeyMapper().key("foo");

        editor.editItem("bar");

		fakeClientCommunication();

        editor.refreshedItems.clear();
        editor.editItem("foo");

		fakeClientCommunication();

        Assert.assertEquals(2, editor.refreshedItems.size());
        Assert.assertEquals("bar", editor.refreshedItems.get(0));
        Assert.assertEquals("foo", editor.refreshedItems.get(1));
    }

    @Test
    public void cancel_eventIsFiredAndItemIsRefreshed() {
        editor.editItem("bar");
		fakeClientCommunication();
        editor.refreshedItems.clear();

        AtomicReference<EditorEvent<String>> cancelEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();
        editor.addCancelListener(
                event -> cancelEventCapture.compareAndSet(null, event));
        editor.addCloseListener(
                event -> closeEventCapture.compareAndSet(null, event));
        editor.cancel();

        Assert.assertNotNull(cancelEventCapture.get());
        Assert.assertNotNull(closeEventCapture.get());

        Assert.assertEquals(1, editor.refreshedItems.size());
        Assert.assertEquals("bar", editor.refreshedItems.get(0));
    }

    @Test
    public void save_editorIsNotOpened_noEvents() {
        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> saveEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        assertNegativeSave(statusEventCapture, saveEventCapture,
                closeEventCapture);
        Assert.assertEquals(0, editor.refreshedItems.size());

        Assert.assertNull(statusEventCapture.get());
        Assert.assertNull(saveEventCapture.get());
        Assert.assertNull(closeEventCapture.get());
    }

    @Test
    public void save_editorIsOpened_editorIsInNotBufferedMode_noEvents() {
        editor.editItem("bar");
		fakeClientCommunication();
        editor.refreshedItems.clear();

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> saveEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        assertNegativeSave(statusEventCapture, saveEventCapture,
                closeEventCapture);
        Assert.assertEquals(0, editor.refreshedItems.size());

        Assert.assertNull(statusEventCapture.get());
        Assert.assertNull(saveEventCapture.get());
        Assert.assertNull(closeEventCapture.get());
    }

    @Test
    public void save_editorIsOpened_editorIsInBufferedMode_eventsAreFired() {
        editor.editItem("bar");
		fakeClientCommunication();
        editor.refreshedItems.clear();
        editor.setBuffered(true);

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> saveEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        Assert.assertTrue(doSave(statusEventCapture, saveEventCapture,
                closeEventCapture));
        Assert.assertEquals(1, editor.refreshedItems.size());
        Assert.assertEquals("bar", editor.refreshedItems.get(0));

        Assert.assertNotNull(statusEventCapture.get());
        Assert.assertNotNull(saveEventCapture.get());
        Assert.assertNotNull(closeEventCapture.get());
    }

    @Test
    public void save_editorIsOpened_editorIsInBufferedMode_beanIsInvalid_editorIsNotClosed() {
        editor.getBinder().forField(new TextField())
                .withValidator(value -> !value.equals("bar"), "")
                .bind(ValueProvider.identity(), (item, value) -> {
                });
        editor.editItem("bar");
		fakeClientCommunication();
        editor.refreshedItems.clear();
        editor.setBuffered(true);

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> saveEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        Assert.assertFalse(doSave(statusEventCapture, saveEventCapture,
                closeEventCapture));
        Assert.assertEquals(0, editor.refreshedItems.size());

        Assert.assertNotNull(statusEventCapture.get());
        Assert.assertNull(saveEventCapture.get());
        Assert.assertNull(closeEventCapture.get());

        Assert.assertTrue(statusEventCapture.get().hasValidationErrors());
    }

    @Test
    public void editorIsInBufferedMode_closeEditorThrows() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.reportMissingExceptionWithMessage("Buffered editor should be closed using save() or cancel()");

        editor.editItem("bar");
		fakeClientCommunication();
        editor.refreshedItems.clear();
        editor.setBuffered(true);

        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        editor.addCloseListener(
                event -> closeEventCapture.compareAndSet(null, event));

        editor.closeEditor();

        Assert.assertNull("Received close event even though method should have thrown.", closeEventCapture.get());
    }

    @Test
    public void editorInUnBufferedMode_closeEditorSendsCloseEvent() {
        editor.editItem("bar");
		fakeClientCommunication();
        editor.refreshedItems.clear();

        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        editor.addCloseListener(
                event -> closeEventCapture.compareAndSet(null, event));

        editor.closeEditor();
        Assert.assertNotNull("No close event was fired.", closeEventCapture.get());
    }


    private void assertNegativeSave(
            AtomicReference<StatusChangeEvent> statusEventCapture,
            AtomicReference<EditorEvent<String>> saveEventCapture,
            AtomicReference<EditorEvent<String>> closeEventCapture) {
        Assert.assertFalse(doSave(statusEventCapture, saveEventCapture,
                closeEventCapture));
    }

    private boolean doSave(
            AtomicReference<StatusChangeEvent> statusEventCapture,
            AtomicReference<EditorEvent<String>> saveEventCapture,
            AtomicReference<EditorEvent<String>> closeEventCapture) {
        editor.getBinder().addStatusChangeListener(
                event -> statusEventCapture.compareAndSet(null, event));
        editor.addSaveListener(
                event -> saveEventCapture.compareAndSet(null, event));
        editor.addCloseListener(
                event -> closeEventCapture.compareAndSet(null, event));

        return editor.save();
    }

    private void assertOpenEvents(
            AtomicReference<StatusChangeEvent> statusEventCapture,
            AtomicReference<EditorEvent<String>> openEventCapure) {
        editor.getBinder().addStatusChangeListener(
                event -> statusEventCapture.compareAndSet(null, event));

        editor.addOpenListener(
                event -> openEventCapure.compareAndSet(null, event));
        editor.editItem("bar");
		fakeClientCommunication();
        Assert.assertNotNull(statusEventCapture.get());
        Assert.assertNotNull(openEventCapure.get());

        Assert.assertEquals("bar", openEventCapure.get().getItem());
    }

	public static class MockUI extends UI {

		public MockUI() {
			this(findOrcreateSession());
		}

		public MockUI(VaadinSession session) {
			getInternals().setSession(session);
			setCurrent(this);
		}

		@Override
		protected void init(VaadinRequest request) {
			// Do nothing
		}

		private static VaadinSession findOrcreateSession() {
			VaadinSession session = VaadinSession.getCurrent();
			if (session == null) {
				session = new AlwaysLockedVaadinSession(null);
				VaadinSession.setCurrent(session);
			}
			return session;
		}
	}

	public static class AlwaysLockedVaadinSession extends MockVaadinSession {

		public AlwaysLockedVaadinSession(VaadinService service) {
			super(service);
			lock();
		}

	}

	public static class MockVaadinSession extends VaadinSession {
		/*
		 * Used to make sure there's at least one reference to the mock session while
		 * it's locked. This is used to prevent the session from being eaten by GC in
		 * tests where @Before creates a session and sets it as the current instance
		 * without keeping any direct reference to it. This pattern has a chance of
		 * leaking memory if the session is not unlocked in the right way, but it should
		 * be acceptable for testing use.
		 */
		private static final ThreadLocal<MockVaadinSession> referenceKeeper = new ThreadLocal<>();

		public MockVaadinSession(VaadinService service) {
			super(service);
		}

		@Override
		public void close() {
			super.close();
			closeCount++;
		}

		public int getCloseCount() {
			return closeCount;
		}

		@Override
		public Lock getLockInstance() {
			return lock;
		}

		@Override
		public void lock() {
			super.lock();
			referenceKeeper.set(this);
		}

		@Override
		public void unlock() {
			super.unlock();
			referenceKeeper.remove();
		}

		private int closeCount;

		private ReentrantLock lock = new ReentrantLock();
	}
}
