/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.PowerPointDwellCondition;
import generated.dkf.PowerPointDwellCondition.Default;
import generated.dkf.PowerPointDwellCondition.Slides;
import generated.dkf.PowerPointDwellCondition.Slides.Slide;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.DeletePredicate;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;

/**
 * The Class PowerPointDwellConditionEditor.
 */
public class PowerPointDwellConditionEditorImpl extends ConditionInputPanel<PowerPointDwellCondition> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(PowerPointDwellConditionEditorImpl.class.getName());

    /** The ui binder. */
    private static PowerPointDwellConditionEditorUiBinder uiBinder = GWT
            .create(PowerPointDwellConditionEditorUiBinder.class);

    /** The Interface PowerPointDwellConditionEditorUiBinder. */
    interface PowerPointDwellConditionEditorUiBinder extends UiBinder<Widget, PowerPointDwellConditionEditorImpl> {
    }

    /**
     * A wrapper that contains either a {@link Slide} or a {@link Default} object.
     *
     * @author tflowers
     *
     */
    public static class SlideOrDefaultWrapper {

        /** Field that contains the wrapped object. */
        private Serializable slideOrDefault;

        /**
         * Wraps a {@link Slide} object.
         *
         * @param slide The {@link Slide} to wrap. Can't be null.
         */
        public SlideOrDefaultWrapper(Slide slide) {
            setValue(slide);
        }

        /**
         * Wraps a {@link Default} object.
         *
         * @param defaultTiming The {@link Default} to wrap. Can't be null.
         */
        public SlideOrDefaultWrapper(Default defaultTiming) {
            setValue(defaultTiming);
        }

        /**
         * Sets the contained value to a provided {@link Slide}.
         *
         * @param slide The {@link Slide} to place within the wrapper, can't be
         *        null.
         */
        public void setValue(Slide slide) {
            if (slide == null) {
                throw new IllegalArgumentException("The parameter 'slide' cannot be null.");
            }

            slideOrDefault = slide;
        }

        /**
         * Sets the contained value to a provided {@link Default}.
         *
         * @param defaultTiming The {@link Default} to place within the wrapper,
         *        can't be null.
         */
        public void setValue(Default defaultTiming) {
            if (defaultTiming == null) {
                throw new IllegalArgumentException("The parameter 'defaultTiming' cannot be null.");
            }

            slideOrDefault = defaultTiming;
        }

        /**
         * Getter for the wrapped {@link Slide}.
         *
         * @return If a {@link Slide} is being wrapped it returns the slide, otherwise it returns
         *         null.
         */
        public Slide getSlide() {
            return slideOrDefault instanceof Slide ? (Slide) slideOrDefault : null;
        }

        /**
         * Getter for the wrapped {@link Default}.
         *
         * @return If a {@link Default} is being wrapped it returns the default, otherwise it returns
         *         null.
         */
        public Default getDefault() {
            return slideOrDefault instanceof Default ? (Default) slideOrDefault : null;
        }

        @Override
        public String toString() {
            return new StringBuilder("[SlideOrDefaultWrapper: ").append("slideOrDefault = ").append(slideOrDefault)
                    .append("]").toString();
        }
    }

    /** The data grid. */
    @UiField(provided = true)
    protected ItemListEditor<SlideOrDefaultWrapper> dataGrid = new ItemListEditor<>(new SlideOrDefaultWrapperItemEditor());

    /** The HTML that annotates the {@link #dataGrid} */
    @UiField
    protected HTML listEditorDescription;

    /** List that contains the wrapper objects */
    private List<SlideOrDefaultWrapper> wrapperList = new ArrayList<SlideOrDefaultWrapper>();

    /** The field that displays the slide index */
    private ItemField<SlideOrDefaultWrapper> indexField = new ItemField<SlideOrDefaultWrapper>("Slide Number", null) {

        @Override
        public Widget getViewWidget(SlideOrDefaultWrapper item) {
            if (item.getSlide() != null) {
                int index = item.getSlide().getIndex().intValue();
                return new HTML(Integer.toString(index));
            } else if (item.getDefault() != null) {
                return new HTML(wrapperList.size() > 1 ? "Other" : "All");
            }

            return null;
        }
    };

    /** The field that displays the timing information for a slide */
    private final ItemField<SlideOrDefaultWrapper> timeField = new ItemField<SlideOrDefaultWrapper>("Time", null) {

        @Override
        public Widget getViewWidget(SlideOrDefaultWrapper item) {
            int time = 0;
            if (item.getSlide() != null) {
                time = (int) item.getSlide().getTimeInSeconds();
            } else if (item.getDefault() != null) {
                time = (int) item.getDefault().getTimeInSeconds();
            }

            return new HTML(FormattedTimeBox.getDisplayText(time));
        }
    };

    /** The comparator for sorting the {@link #wrapperList} */
    private Comparator<SlideOrDefaultWrapper> wrapperComparator = new Comparator<SlideOrDefaultWrapper>() {

        @Override
        public int compare(SlideOrDefaultWrapper o1, SlideOrDefaultWrapper o2) {
            if (logger.isLoggable(Level.FINE)) {
                List<Object> params = Arrays.<Object>asList(o1, o2);
                logger.fine("wrapperComparator.compare(" + StringUtils.join(", ", params) + ")");
            }

            /* If the either object is a default, it always goes last */
            if (o1.getDefault() != null) {
                return 1;
            } else if (o2.getDefault() != null) {
                return -1;
            }

            return o1.getSlide().getIndex().compareTo(o2.getSlide().getIndex());
        }
    };

    /**
     * Default Constructor
     *
     * Required to be public for GWT UIBinder compatibility.
     */
    public PowerPointDwellConditionEditorImpl() {
        initWidget(uiBinder.createAndBindUi(this));

        dataGrid.setFields(Arrays.asList(indexField, timeField));
        dataGrid.addCreateListAction("Click here to add a new slide number",
                new CreateListAction<SlideOrDefaultWrapper>() {

                    @Override
                    public SlideOrDefaultWrapper createDefaultItem() {
                        Slide slide = new Slide();
                        slide.setIndex(BigInteger.ONE);
                        slide.setTimeInSeconds(0.0);
                        return new SlideOrDefaultWrapper(slide);
                    }
                });

        // only slide items can be deleted
        dataGrid.setDeletePredicate(new DeletePredicate<SlideOrDefaultWrapper>() {
            @Override
            public boolean canDelete(SlideOrDefaultWrapper item) {
                return item.getSlide() != null;
            }
        });

        // list changed
        dataGrid.addListChangedCallback(new ListChangedCallback<SlideOrDefaultWrapper>() {
            @Override
            public void listChanged(ListChangedEvent<SlideOrDefaultWrapper> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("dataGrid.listChangedCommand.execute()");
                }

                Collections.sort(wrapperList, wrapperComparator);
                dataGrid.setItems(wrapperList);

                List<Slide> slides = new ArrayList<>();
                for (SlideOrDefaultWrapper wrapper : wrapperList) {
                    if (wrapper.getSlide() != null) {
                        slides.add(wrapper.getSlide());
                    } else {
                        getInput().setDefault(wrapper.getDefault());
                    }
                }

                getInput().getSlides().getSlide().clear();
                getInput().getSlides().getSlide().addAll(slides);

                dataGrid.validateAllAndFireDirtyEvent(getCondition());
            }
        });
    }

    /**
     * Sets the text used to describe the {@link #dataGrid}.
     *
     * @param description The description to show next to the {@link #dataGrid}.
     */
    public void setItemListEditorDescription(String description) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setItemListEditorDescription(" + description + ")");
        }

        listEditorDescription.setHTML(description);
    }

    @Override
    protected void onEdit() {
        if (getInput().getSlides() == null) {
            getInput().setSlides(new Slides());
        }

        if (getInput().getDefault() == null) {
            getInput().setDefault(new Default());
        }

        wrapperList.clear();

        for (Slide slide : getInput().getSlides().getSlide()) {
            wrapperList.add(new SlideOrDefaultWrapper(slide));
        }

        wrapperList.add(new SlideOrDefaultWrapper(getInput().getDefault()));
        Collections.sort(wrapperList, wrapperComparator);
        dataGrid.setItems(wrapperList);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        // no validation statuses
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        // nothing to validate
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(dataGrid);
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        dataGrid.setReadonly(isReadonly);
    }
}