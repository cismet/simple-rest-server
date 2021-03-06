/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.simplerestserver;

import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface ServerParamProvider {

    //~ Instance fields --------------------------------------------------------

    String PARAM_JERSEY_PROPERTY_PACKAGES = "com.sun.jersey.config.property.packages";                 // NOI18N
    String PARAM_DEFAULT_IDLE_THREAD_TIMEOUT = "de.cismet.commons.simplerestserver.idleThreadTimeout"; // NOI18N

    String PARAM_SERVLET_CONTEXT = "de.cismet.commons.simplerestserver.servletContextPath";      // NOI18N
    String PARAM_STATIC_RESOURCE_PATH = "de.cismet.commons.simplerestserver.staticResourcePath"; // NOI18N

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Map<String, String> getServerParams();
}
