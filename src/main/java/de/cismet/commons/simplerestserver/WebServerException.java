/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.simplerestserver;

/**
 * Main exception to be thrown if serious errors occur during application execution.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0, 20100511
 */
public final class WebServerException extends RuntimeException {

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructs an instance of <code>WebServerException</code> with the specified detail message.
     *
     * @param  msg  the detail message.
     */
    public WebServerException(final String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>WebServerException</code> with the specified detail message and the specified
     * cause.
     *
     * @param  msg    the detail message.
     * @param  cause  the exception cause
     */
    public WebServerException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
