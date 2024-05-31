/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import java.io.IOException;
import java.util.List;

import mil.arl.gift.common.aar.LogIndexService;
import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ParsePlaybackLog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ParsePlaybackLogResult;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * The Class ParsePlaybackLogHandler.
 */
public class ParsePlaybackLogHandler implements ActionHandler<ParsePlaybackLog, ParsePlaybackLogResult> {

    @Override
    public ParsePlaybackLogResult execute(ParsePlaybackLog action, ExecutionContext ctx) throws DispatchException {
        long start = System.currentTimeMillis();

        ParsePlaybackLogResult result;
        try {
            FileProxy fileProxy = ServicesManager.getInstance().getFileServices().getFile(action.getFilename(),
                    action.getUsername());
            List<LogMetadata> metadatas = LogIndexService.extractMetadata(fileProxy);
            result = new ParsePlaybackLogResult(metadatas);
        } catch (IOException e) {
            result = new ParsePlaybackLogResult("The log file was unable to be parsed.");
            result.setErrorDetails(e.getMessage());
            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
        }

        MetricsSenderSingleton.getInstance().endTrackingRpc("util.ParsePlaybackLog", start);
        return result;
    }

    @Override
    public Class<ParsePlaybackLog> getActionType() {
        return ParsePlaybackLog.class;
    }

    @Override
    public void rollback(ParsePlaybackLog arg0, ParsePlaybackLogResult arg1, ExecutionContext arg2)
            throws DispatchException {
        /* Do nothing */
    }
}
