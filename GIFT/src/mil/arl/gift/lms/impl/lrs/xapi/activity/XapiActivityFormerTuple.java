package mil.arl.gift.lms.impl.lrs.xapi.activity;

import mil.arl.gift.lms.impl.common.LmsXapiActivityException;

/**
 * Interface for Activity creation from 2 arbitrary arguments. Implemented by ActivityTypeConcept classes.
 * Inspired by Abstract Factory design pattern.
 * 
 * @author Yet Analytics
 *
 * @param <T1> object used to create activity
 * @param <T2> object used to create activity
 */
public interface XapiActivityFormerTuple<T1, T2> extends XapiActivityFormer<T1> {

    /**
     * Creates AbstractGiftActivity child from the passed in types
     * 
     * @param t1 - type used to create activity.
     * @param t2 - another type necessary to create activity.
     *  
     * @return AbstractGiftActivity child class
     * 
     * @throws LmsXapiActivityException when unable to create activity
     */
    default AbstractGiftActivity asActivity(T1 t1, T2 t2) throws LmsXapiActivityException {
        throw new LmsXapiActivityException("Attempting to call default implementation of asActivity!");
    }
}
