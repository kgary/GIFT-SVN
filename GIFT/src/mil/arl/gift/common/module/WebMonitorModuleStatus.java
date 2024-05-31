/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.module;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import mil.arl.gift.common.enums.ModuleTypeEnum;

/**
 * A {@link ModuleStatus} for a WebMonitorModule.
 *
 * @author tflowers
 *
 */
public class WebMonitorModuleStatus extends ModuleStatus {

    /** The set of domain session ids to which the web monitor is attached */
    private final Set<Integer> attachedDomainSession;

    /**
     * Creates a {@link WebMonitorModuleStatus} from a base {@link ModuleStatus}
     * and a {@link Set} of domain session ids.
     *
     * @param moduleStatus The {@link ModuleStatus} from which to construct the
     *        {@link WebMonitorModuleStatus}. Can't be null.
     * @param attachedDomainSessions The {@link Set} of domain session ids to
     *        which this web monitor module is attached. Can't be null. Can be
     *        empty.
     */
    public WebMonitorModuleStatus(ModuleStatus moduleStatus, Set<Integer> attachedDomainSessions) {
        super(moduleStatus.getModuleName(), moduleStatus.getQueueName(), ModuleTypeEnum.MONITOR_MODULE);
        this.attachedDomainSession = Collections.synchronizedSet(attachedDomainSessions);
    }

    /**
     * Creates a {@link WebMonitorModuleStatus} from a base
     * {@link ModuleStatus}.
     *
     * @param moduleStatus The {@link ModuleStatus} from which to construct the
     *        {@link WebMonitorModuleStatus}. Can't be null.
     */
    public WebMonitorModuleStatus(ModuleStatus moduleStatus) {
        this(moduleStatus, new HashSet<>());
    }

    /**
     * Getter for the {@link Set} of domain session ids to which the WebMonitor
     * is attached.
     *
     * @return The {@link Set} {@link #attachedDomainSession}.
     */
    public Set<Integer> getAttachedDomainSessions() {
        return attachedDomainSession;
    }
}
