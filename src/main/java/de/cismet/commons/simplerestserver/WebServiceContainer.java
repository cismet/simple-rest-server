/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.simplerestserver;

import java.net.URI;

/**
 * Interface for all Web Service Containers.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0, 20100511
 */
public interface WebServiceContainer {

    //~ Instance fields --------------------------------------------------------

    String DEFAULT_HOST = "localhost"; // NOI18N
    String DEFAULT_PROTOCOL = "http";  // NOI18N

    //~ Methods ----------------------------------------------------------------

    /**
     * Starts the Web Service Container.
     *
     * @throws  WebServerException  if any error occurs during container startup
     */
    void up() throws WebServerException;

    /**
     * Stops the Web Service Container.
     *
     * @throws  WebServerException  if any error occurs during container shutdown
     */
    void down() throws WebServerException;

    /**
     * Getter for the base {@link URI} of this container.
     *
     * @return  the base <code>URI</code>
     */
    URI getBaseUri();
}
