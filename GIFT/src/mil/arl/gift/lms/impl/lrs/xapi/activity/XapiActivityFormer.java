package mil.arl.gift.lms.impl.lrs.xapi.activity;

import mil.arl.gift.lms.impl.common.LmsXapiActivityException;

/**
 * Interface for Activity creation from arbitrary argument. Implemented by ActivityTypeConcept classes.
 * Inspired by Abstract Factory design pattern.
 * 
 * @author Yet Analytics
 *
 * @param <T> object used to create activity
 */
public interface XapiActivityFormer<T> {
    /**
     * Creates AbstractGiftActivity child from the passed in type
     * 
     * @param t1 - type used to create corresponding activity.
     * 
     * @return AbstractGiftActivity child class
     * 
     * @throws LmsXapiActivityException when unable to create activity
     */
    default AbstractGiftActivity asActivity(T t) throws LmsXapiActivityException {
        throw new LmsXapiActivityException("Attempting to call default implementation of asActivity!");
    }
}
