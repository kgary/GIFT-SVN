/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import mil.arl.gift.common.util.StringUtils;

/**
 * Search filter for finding specific course concepts
 * 
 * @author sharrison
 */
public class CourseConceptSearchFilter {

    /** The entire collection of course concepts */
    private Collection<String> courseConceptNames;

    /** Criteria for finding a course concept with a specific name */
    private String conceptName;

    /**
     * Criteria for finding a course concept that is or is not being assessed
     */
    private Boolean conceptAssessed;

    /**
     * Creates a filter for finding course concepts.
     * 
     * @param courseConceptNames the entire collection of course concepts.
     */
    public CourseConceptSearchFilter(Collection<String> courseConceptNames) {
        if (courseConceptNames == null) {
            throw new IllegalArgumentException("The parameter 'courseConceptNames' cannot be null.");
        }

        /* Make sure all concept name are trimmed and lower case */
        courseConceptNames = courseConceptNames.stream().map(str -> str.trim().toLowerCase())
                .collect(Collectors.toSet());
        this.courseConceptNames = Collections.unmodifiableCollection(courseConceptNames);
    }

    /**
     * Retrieve the entire collection of course concepts.
     * 
     * @return an unmodifiable collection of course concept names. Won't be
     *         null.
     */
    public Collection<String> getCourseConceptNames() {
        return courseConceptNames;
    }

    /**
     * Retrieve the concept name filter criteria.
     * 
     * @return the criteria for finding a course concept with a specific name.
     *         Can be null if no criteria is specified.
     */
    public String getConceptName() {
        return conceptName;
    }

    /**
     * Set the concept name filter criteria. Will be trimmed and converted to
     * lower case.
     * 
     * @param conceptName the criteria for finding a course concept with a
     *        specific name. A null value (default) means not to apply this
     *        filter criteria.
     */
    public void setConceptName(String conceptName) {
        this.conceptName = conceptName != null ? conceptName.trim().toLowerCase() : null;
    }

    /**
     * Retrieve the concept assessed filter criteria.
     * 
     * @return the criteria for finding a course concept that is or is not being
     *         assessed. Can be null if no criteria is specified.
     */
    public Boolean isConceptAssessed() {
        return conceptAssessed;
    }

    /**
     * Set the concept assessed filter criteria.
     * 
     * @param conceptAssessed the criteria for finding a course concept that is
     *        or is not being assessed. A null value (default) means not to
     *        apply this filter criteria.
     */
    public void setIsConceptAssessed(Boolean conceptAssessed) {
        this.conceptAssessed = conceptAssessed;
    }

    /**
     * Apply the filter criteria to the given {@link Concept}.
     * 
     * @param concept the concept to test against the filter criteria. Can't be
     *        null.
     * @return true if the concept is a course concept and passes the filter;
     *         false otherwise.
     */
    public boolean applyFilter(Concept concept) {
        if (concept == null) {
            throw new IllegalArgumentException("The parameter 'concept' cannot be null.");
        } else if (courseConceptNames.isEmpty()) {
            return false;
        }

        final String cNameToFind = concept.getName().toLowerCase();

        /* If using concept name, return false if the name is not equal */
        if (conceptName != null && !StringUtils.equalsIgnoreCase(conceptName, cNameToFind)) {
            return false;
        }

        /* If using concept assessment, return false if the assessment value is
         * not equal */
        if (conceptAssessed != null && !conceptAssessed.equals(concept.hasScoringRules())) {
            return false;
        }

        /* Last check, see if the concept is contained within the course
         * concepts */
        return courseConceptNames.contains(cNameToFind);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[CourseConceptSearchFilter: ");
        sb.append("course concepts = ").append(StringUtils.join(", ", courseConceptNames));
        sb.append(", concept name = ").append(conceptName);
        sb.append(", concept assessed = ").append(conceptAssessed);
        sb.append("]");

        return sb.toString();
    }
}
