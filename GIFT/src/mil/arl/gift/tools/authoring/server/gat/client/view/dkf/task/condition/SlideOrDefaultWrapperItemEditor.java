/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.math.BigInteger;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.PowerPointDwellCondition.Default;
import generated.dkf.PowerPointDwellCondition.Slides.Slide;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.NumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.PowerPointDwellConditionEditorImpl.SlideOrDefaultWrapper;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * An {@link ItemEditor} responsible for editing a given
 * {@link SlideOrDefaultWrapper}.
 *
 * @author tflowers
 *
 */
public class SlideOrDefaultWrapperItemEditor extends ItemEditor<SlideOrDefaultWrapper> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(SlideOrDefaultWrapperItemEditor.class.getName());

    /** The binder for combining the ui.xml file with this java file */
    private static SlideOrDefaultWrapperItemEditorUiBinder uiBinder = GWT.create(SlideOrDefaultWrapperItemEditorUiBinder.class);

    /** The interface that combines the ui.xml file with the java class */
    interface SlideOrDefaultWrapperItemEditorUiBinder extends UiBinder<Widget, SlideOrDefaultWrapperItemEditor> {
    }

    /** The panel that shows either 'All', 'Default', or the slide number. */
    @UiField
    protected DeckPanel slideNumberPanel;

    /** The label describing the default slide. Should be 'All' or 'Default'. */
    @UiField
    protected HTML defaultSlideLabel;

    /** The spinner for choosing a slide to affect. */
    @UiField
    protected NumberSpinner slideNumberSpinner;

    /** The box for setting the dwell time for a slide. */
    @UiField
    protected FormattedTimeBox slideTimeBox;

    /**
     * The wrapper that is currently being edited. Do not make edits to the
     * object using this reference.
     */
    private SlideOrDefaultWrapper editedWrapper = null;

    /** Validates the slide number */
    private final WidgetValidationStatus slideStatus;

    /** Validates the time for the slide */
    private final WidgetValidationStatus timeStatus;

    /**
     * Constructs a new {@link SlideOrDefaultWrapperItemEditor}
     */
    public SlideOrDefaultWrapperItemEditor() {
        initWidget(uiBinder.createAndBindUi(this));

        slideStatus = new WidgetValidationStatus(slideNumberSpinner, "There is already a rule for this slide. Please choose another slide.");
        timeStatus = new WidgetValidationStatus(slideTimeBox, "There must be at least one slide with a non-zero time.");
    }

    /**
     * Requests that the {@link #slideStatus} is reevaluated when the value
     * within {@link #slideNumberSpinner} changes.
     *
     * @param event The event containing the value of
     *        {@link #slideNumberSpinner}.
     */
    @UiHandler("slideNumberSpinner")
    protected void onSlideNumberChange(ValueChangeEvent<Integer> event) {
        requestValidation(slideStatus);
    }

    /**
     * Requests that the {@link #timeStatus} is reevaluated when the value
     * within {@link #slideNumberSpinner} changes.
     *
     * @param event The event containing the new value of {@link #slideTimeBox}.
     */
    @UiHandler("slideTimeBox")
    protected void onSlideTimeBox(ValueChangeEvent<Integer> event) {
        requestValidation(timeStatus);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(slideStatus);
        validationStatuses.add(timeStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (slideStatus.equals(validationStatus)) {
            if (editedWrapper.getDefault() != null) {
                // default can't be a duplicate
                slideStatus.setValid();
            } else {
                boolean foundDuplicate = false;
                BigInteger slideIndex = BigInteger.valueOf(slideNumberSpinner.getValue().longValue());
                for (SlideOrDefaultWrapper wrapper : getParentItemListEditor().getItems()) {
                    // skip the wrapper we are currently editing
                    if (editedWrapper == wrapper) {
                        continue;
                    }

                    if (wrapper.getSlide() != null && wrapper.getSlide().getIndex().equals(slideIndex)) {
                        foundDuplicate = true;
                        break;
                    }
                }
                slideStatus.setValidity(!foundDuplicate);
            }
        } else if (timeStatus.equals(validationStatus)) {
            if (slideTimeBox.getValue() > 0) {
                timeStatus.setValid();
            } else {
                boolean foundNonZero = false;
                for (SlideOrDefaultWrapper wrapper : getParentItemListEditor().getItems()) {
                    // skip the wrapper we are currently editing
                    if (editedWrapper == wrapper) {
                        continue;
                    }

                    if (wrapper.getSlide() != null) {
                        foundNonZero |= wrapper.getSlide().getTimeInSeconds() > 0;
                    } else if (wrapper.getDefault() != null) {
                        foundNonZero |= wrapper.getDefault().getTimeInSeconds() > 0;
                    }
                }
                timeStatus.setValidity(foundNonZero);
            }
        }
    }

    @Override
    protected boolean validate(SlideOrDefaultWrapper wrapper) {
        Default defaultSlide = wrapper.getDefault();
        Slide slide = wrapper.getSlide();

        /* Must have one or the other, but not none or both */
        if (defaultSlide == null && slide == null) {
            return false;
        } else if (defaultSlide != null && slide != null) {
            return false;
        }

        if (defaultSlide != null) {
            /* Can't be negative */
            if (defaultSlide.getTimeInSeconds() < 0) {
                return false;
            }

            for (SlideOrDefaultWrapper editorItem : getParentItemListEditor().getItems()) {
                /* Skip the provided item */
                if (wrapper == editorItem) {
                    continue;
                }

                /* Can only be 1 default slide */
                if (editorItem.getDefault() != null) {
                    return false;
                }
            }
        } else if (slide != null) {
            /* Can't be negative */
            if (slide.getTimeInSeconds() < 0) {
                return false;
            }

            /* Must be slide index of >= 1 */
            BigInteger slideIndex = slide.getIndex();
            if (slideIndex == null || slideIndex.intValue() < 1) {
                return false;
            }

            boolean hasNonZeroValue = slide.getTimeInSeconds() != 0;
            for (SlideOrDefaultWrapper editorItem : getParentItemListEditor().getItems()) {
                /* Skip the provided item */
                if (wrapper == editorItem) {
                    continue;
                }

                if (editorItem.getDefault() != null) {
                    hasNonZeroValue |= editorItem.getDefault().getTimeInSeconds() != 0;
                } else if (editorItem.getSlide() != null) {
                    hasNonZeroValue |= editorItem.getSlide().getTimeInSeconds() != 0;

                    /* Can't have duplicate indices */
                    BigInteger editorItemIndex = editorItem.getSlide().getIndex();
                    if (editorItemIndex != null && slideIndex.compareTo(editorItemIndex) == 0) {
                        return false;
                    }
                }
            }

            /* Must have a nonzero value */
            if (!hasNonZeroValue) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void populateEditor(SlideOrDefaultWrapper obj) {
        editedWrapper = obj;

        if (obj.getDefault() != null) {
            Default defaultSlide = obj.getDefault();

            showDefaultSlideLabel();
            defaultSlideLabel.setText(getParentItemListEditor().size() > 1 ? "Default" : "All");

            slideTimeBox.setValue((int) defaultSlide.getTimeInSeconds());
        } else if (obj.getSlide() != null) {
            Slide slide = obj.getSlide();

            showSlideSelector();
            slideNumberSpinner.setValue(slide.getIndex().intValue());

            slideTimeBox.setValue((int) slide.getTimeInSeconds());
        }
    }

    @Override
    protected void applyEdits(SlideOrDefaultWrapper obj) {
        if (slideNumberPanel.getVisibleWidget() == slideNumberPanel.getWidgetIndex(defaultSlideLabel)) {
            Default defaultSlide = new Default();
            defaultSlide.setTimeInSeconds(slideTimeBox.getValue());
            obj.setValue(defaultSlide);
        } else if (slideNumberPanel.getVisibleWidget() == slideNumberPanel.getWidgetIndex(slideNumberSpinner)) {
            Slide slide = new Slide();
            slide.setTimeInSeconds(slideTimeBox.getValue());
            slide.setIndex(BigInteger.valueOf(slideNumberSpinner.getValue()));
            obj.setValue(slide);
        }
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        slideNumberSpinner.setEnabled(!isReadonly);
        slideTimeBox.setEnabled(!isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {

    }

    /**
     * Shows the {@link #slideTimeBox} in the {@link #slideNumberPanel}.
     */
    private void showSlideSelector() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showSlideSelector()");
        }

        slideNumberPanel.showWidget(slideNumberPanel.getWidgetIndex(slideNumberSpinner));
    }

    /**
     * Shows the {@link #defaultSlideLabel} in the {@link #slideNumberPanel}.
     */
    private void showDefaultSlideLabel() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showDefaultSlideLabel()");
        }

        slideNumberPanel.showWidget(slideNumberPanel.getWidgetIndex(defaultSlideLabel));
    }
}