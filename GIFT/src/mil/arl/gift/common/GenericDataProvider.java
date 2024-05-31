/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Creates a hierarchy of lists in order to easily duplicate the parent list's data to all it's
 * children recursively. Each child can contain it's own children lists.
 * 
 * @author sharrison
 */
public class GenericDataProvider<T> {

    /** The list of data; used to populate the children */
    private List<T> list;

    /** The list of children */
    private List<GenericDataProvider<T>> children;

    /**
     * Default Constructor.
     */
    public GenericDataProvider() {
    }

    /**
     * Private Constructor.
     * 
     * @param dataList the list of data objects.
     */
    private GenericDataProvider(List<T> dataList) {
        this.list = dataList;
    }

    /**
     * Creates a new child. This child will be populated with the parent's data list upon refresh.
     * 
     * @param childData the child data to populate upon refresh.
     * @return returns the created child.
     */
    public GenericDataProvider<T> createChild(List<T> childData) {
        GenericDataProvider<T> gdp = new GenericDataProvider<T>(childData);
        getChildren().add(gdp);
        return gdp;
    }

    /**
     * Removes the child if it exists.
     * 
     * @param child the child to remove
     * @return true if the list contained the child and was removed; false if the list is unchanged.
     */
    public boolean removeChild(GenericDataProvider<T> child) {
        return getChildren().remove(child);
    }

    /**
     * Removes the child that contains the specified list.
     * 
     * @param childData the list of data used to create a child.
     * @return true if the list exists in one of the children; false if the list is null or doesn't
     *         exist.
     */
    public boolean removeChild(List<T> childData) {
        boolean removed = false;

        if (childData != null) {
            Iterator<GenericDataProvider<T>> itr = getChildren().iterator();
            while (itr.hasNext()) {
                if (itr.next().getList().equals(childData)) {
                    itr.remove();
                    removed = true;
                    break;
                }
            }
        }

        return removed;
    }

    /**
     * Populates all children's data, recursively, with the data list.
     */
    public void refresh() {
        for (GenericDataProvider<T> dp : getChildren()) {
            dp.getList().clear();
            dp.getList().addAll(getList());
            dp.refresh();
        }
    }

    /**
     * Gets the data list.
     * 
     * @return the data list. Will never be null.
     */
    public List<T> getList() {
        if (list == null) {
            list = new ArrayList<T>();
        }

        return list;
    }

    /**
     * Gets the list of children.
     * 
     * @return the list of children. Will never be null.
     */
    public List<GenericDataProvider<T>> getChildren() {
        if (children == null) {
            children = new ArrayList<GenericDataProvider<T>>();
        }

        return children;
    }
}
