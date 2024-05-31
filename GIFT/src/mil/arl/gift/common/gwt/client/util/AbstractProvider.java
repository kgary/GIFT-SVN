/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.HasAttachHandlers;

/**
 * A class that manages subscription based updates.
 * 
 * @author sharrison
 * @param <H> The type of handler the provider manages
 */
public abstract class AbstractProvider<H> {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(AbstractProvider.class.getName());

    /** The set of handlers listening to updates */
    private Set<H> registeredHandlers = new HashSet<>();

    /**
     * The handlers that attempted to be added or removed while the existing set
     * of {@link #registeredHandlers} were being executed.
     * 
     * <pre>
     * True: add handler.
     * False: remove handler.
     * </pre>
     */
    private Map<H, Boolean> handlersToBeProcessed = new HashMap<>();

    /** Flag indicating if the {@link #registeredHandlers} are being executed */
    private boolean isExecuting = false;

    /**
     * Constructor
     */
    protected AbstractProvider() {
    }

    /**
     * Add a handler to listen to updates. This method will manage adding and
     * removing the widget from listening to this provider based on its attached
     * state. onAttach will call {@link #addHandler(Object)} and onDetach will
     * call {@link #removeHandler(Object)}.
     * 
     * @param handler The handler to add. Will do nothing if null.
     */
    public void addManagedHandler(final H handler) {
        if (handler == null) {
            return;
        }

        if (!(handler instanceof HasAttachHandlers)) {

            if (logger.isLoggable(Level.WARNING)) {
                logger.warning(
                        "addHandlerManaged() was called with a handler that is not of type 'HasAttachHandlers'. The correct method to call is addHandler() and make sure that removeHandler() is called for cleanup.");
            }

            addHandler(handler);
            return;
        }

        HasAttachHandlers hasAttachHandlers = (HasAttachHandlers) handler;
        hasAttachHandlers.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    addHandler(handler);
                } else {
                    removeHandler(handler);
                }
            }
        });
    }

    /**
     * Add a handler to listen to updates. By calling this method you are taking
     * responsibility for calling {@link #removeHandler(Object)} when it is no
     * longer needed.
     * 
     * @param handler The handler to add. Will do nothing if null.
     */
    public void addHandler(final H handler) {
        if (handler == null) {
            return;
        }

        if (isExecuting) {
            handlersToBeProcessed.put(handler, true);
        } else {
            registeredHandlers.add(handler);
        }
    }

    /**
     * Removes a handler from listening to updates.
     * 
     * @param handler The handler to remove.
     */
    public void removeHandler(final H handler) {
        if (handler == null) {
            return;
        }

        if (isExecuting) {
            handlersToBeProcessed.put(handler, false);
        } else {
            registeredHandlers.remove(handler);
        }
    }

    /**
     * Execute all handlers safely (protects from
     * {@link ConcurrentModificationException}).
     * 
     * @param handlerExecutor the callback for executing a handler.
     */
    protected void executeHandlers(SafeHandlerExecution<H> handlerExecutor) {
        isExecuting = true;
        for (H handler : registeredHandlers) {
            try {
                handlerExecutor.execute(handler);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "A handler in " + getClass().getSimpleName() + " failed to process.", e);
            }
        }
        isExecuting = false;
        processWaitingHandlers();
    }

    /**
     * Process any handlers waiting to be processed.
     */
    private void processWaitingHandlers() {
        final Iterator<Entry<H, Boolean>> mapItr = handlersToBeProcessed.entrySet().iterator();
        while (mapItr.hasNext()) {
            Entry<H, Boolean> entry = mapItr.next();
            if (Boolean.TRUE.equals(entry.getValue())) {
                addHandler(entry.getKey());
            } else {
                removeHandler(entry.getKey());
            }
            mapItr.remove();
        }
    }

    /**
     * The callback that accepts a handler and safely performs some action on
     * it.
     * 
     * @author sharrison
     *
     * @param <H> the type of handler being executed.
     */
    public interface SafeHandlerExecution<H> {
        /**
         * Execute some action on the handler.
         * 
         * @param handler the handler to perform an action on.
         */
        void execute(H handler);
    }
}
