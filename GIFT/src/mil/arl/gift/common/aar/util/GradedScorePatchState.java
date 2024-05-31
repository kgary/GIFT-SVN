/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar.util;

import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.PublishLessonScore;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.score.GradedScoreNode;

/**
 * The patch for a graded score node in a playback log message.
 * 
 * @author nroberts
 */
public class GradedScorePatchState extends PatchedState {
    
    /** The new score that has been patched in  */
    private GradedScoreNode patchedScore;

    /**
     * Creates a new patch to change a graded score node
     * 
     * @param time the time that the patch should be applied.
     */
    public GradedScorePatchState(long time) {
        super(time);
    }
    
    /**
     * Updates this patch to set the new score to patch in
     * 
     * @param score the new score to patch in. Can be null.
     */
    public void updatePatchedScore(GradedScoreNode score) {
        patchedScore = score;
    }
    
    @Override
    public void applyPatch(MessageManager toApplyMsg) { 
        
        if (toApplyMsg == null 
                || toApplyMsg.getMessage() == null 
                || toApplyMsg.getMessage().getMessageType() != MessageTypeEnum.PUBLISH_LESSON_SCORE_REQUEST
                || !(toApplyMsg.getMessage().getPayload() instanceof PublishLessonScore)) {
            return;
        }
        
        LMSCourseRecord record = ((PublishLessonScore) toApplyMsg.getMessage().getPayload()).getCourseData();
        
        if (record == null) {
            return;
        }
        
        record.setRoot(patchedScore);
    }
    
    /**
     * Gets the new score that has been patched in
     * 
     * @return the patched score. Can be null.
     */
    public GradedScoreNode getPatchedScore() {
        return patchedScore;
    }
    
    @Override
    public void updatePatch(PatchedState newPatch) {
        if (newPatch == null || !(newPatch instanceof GradedScorePatchState)) {
            return;
        }
        
        patchedScore = ((GradedScorePatchState) newPatch).patchedScore;
    }

    @Override
    public String buildUniquePatchKey(long time) {
        return "" + time;
    }
}
