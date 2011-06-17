/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.simplerestserver;

import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class defines common members of containers.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0, 20100511
 */
public abstract class AbstractWSContainer implements WebServiceContainer {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(AbstractWSContainer.class);

    //~ Instance fields --------------------------------------------------------

    protected final transient WebServerConfig config;
    protected final transient URI baseuri;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractWSContainer object.
     *
     * @param   config  container configuration
     *
     * @throws  IllegalArgumentException  if the provided config is <code>null</code>
     */
    public AbstractWSContainer(final WebServerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null"); // NOI18N
        }
        this.config = config;
        baseuri = initUri();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Initialises the base {@link URI} of this container.
     *
     * @return  the created <code>URI</code>
     *
     * @throws  IllegalStateException  if the URI cannot be created
     */
    private URI initUri() {
        try {
            final URI uri = new URI(DEFAULT_PROTOCOL, null, DEFAULT_HOST, config.getPort(), "/", null, null);
            if (LOG.isInfoEnabled()) {
                LOG.info("created baseuri: " + uri); // NOI18N
            }

            return uri;
        } catch (final URISyntaxException ex) {
            final String message = "URI cannot be constructed"; // NOI18N
            LOG.error(message, ex);
            throw new IllegalStateException(message, ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public URI getBaseUri() {
        return baseuri;
    }
}
