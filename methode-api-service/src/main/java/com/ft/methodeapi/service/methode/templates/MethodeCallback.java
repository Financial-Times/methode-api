package com.ft.methodeapi.service.methode.templates;

/**
 * Expresses the general form of the callbacks.
 *
 * @author Simon.Gibbs
 */
public interface MethodeCallback<T,W>  {

    public T doOperation(W withObject);

}
