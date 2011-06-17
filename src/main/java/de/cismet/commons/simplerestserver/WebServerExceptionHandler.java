/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.simplerestserver;

import org.apache.log4j.Logger;

/**
 * Default uncaught exception handler implementation that logs the exception to the Log4j logger and then terminates the
 * application execution.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0, 20100511
 */
public final class WebServerExceptionHandler implements Thread.UncaughtExceptionHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(WebServerExceptionHandler.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  t  DOCUMENT ME!
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        LOG.fatal("caught uncomitted exception in thread: " + t, e); // NOI18N
        System.exit(2);
    }
}
