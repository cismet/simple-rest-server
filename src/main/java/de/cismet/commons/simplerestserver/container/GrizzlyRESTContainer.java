/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.simplerestserver.container;

import com.sun.grizzly.Controller;
import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import de.cismet.commons.simplerestserver.AbstractWSContainer;
import de.cismet.commons.simplerestserver.ServerParamProvider;
import de.cismet.commons.simplerestserver.WebServerConfig;
import de.cismet.commons.simplerestserver.WebServerException;

/**
 * Grizzly REST Servlet Container. Logging is piped to Log4J
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0, 20100518
 */
public final class GrizzlyRESTContainer extends AbstractWSContainer {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(GrizzlyRESTContainer.class);

    private static final transient String DEFAULT_CTX_PATH = "/";

    //~ Instance fields --------------------------------------------------------

// private transient SelectorThread selector;

    private transient GrizzlyWebServer webServer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GrizzlyRESTContainer object.
     *
     * @param  config  the config to use
     */
    public GrizzlyRESTContainer(final WebServerConfig config) {
        super(config);

        Controller.setLogger(new Java2Log4jLogger());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void up() throws WebServerException {
        if (this.webServer == null) {
            try {
                if (LOG.isInfoEnabled()) {
                    LOG.info("grizzly coming up @ " + baseuri + " :: server params: " + config.getServerParams()); // NOI18N
                }

                this.webServer = new GrizzlyWebServer(config.getPort());

                final ServletAdapter jerseyAdapter;

                // handle static resources, if specified
                final Map<String, String> serverParams = config.getServerParams();
                if (serverParams.containsKey(ServerParamProvider.PARAM_STATIC_RESOURCE_PATH)) {
                    final String staticResourcePath = serverParams.get(ServerParamProvider.PARAM_STATIC_RESOURCE_PATH);
                    jerseyAdapter = new ServletAdapter(staticResourcePath);
                    jerseyAdapter.setHandleStaticResources(true);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found static resources " + staticResourcePath);
                    }
                } else {
                    jerseyAdapter = new ServletAdapter();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("No static resources specified");
                    }
                }

                // pass server params to adapter
                for (final Map.Entry<String, String> param : serverParams.entrySet()) {
                    jerseyAdapter.addInitParameter(param.getKey(), param.getValue());
                }

                final String ctxPath;
                if (serverParams.containsKey(ServerParamProvider.PARAM_SERVLET_CONTEXT)) {
                    ctxPath = serverParams.get(ServerParamProvider.PARAM_SERVLET_CONTEXT);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found context path: " + ctxPath);
                    }
                } else {
                    ctxPath = DEFAULT_CTX_PATH;
                    LOG.warn("No context path found -> Usind default: " + ctxPath);
                }

                jerseyAdapter.setContextPath(ctxPath);
                jerseyAdapter.setServletInstance(new ServletContainer());

                // register all above defined adapters
                this.webServer.addGrizzlyAdapter(jerseyAdapter, new String[] { ctxPath });

                if (serverParams.containsKey(ServerParamProvider.PARAM_DEFAULT_IDLE_THREAD_TIMEOUT)) {
                    final String timeoutString = serverParams.get(
                            ServerParamProvider.PARAM_DEFAULT_IDLE_THREAD_TIMEOUT);
                    try {
                        final int timeout = Integer.parseInt(timeoutString);
                        this.webServer.getSelectorThread().setTransactionTimeout(timeout);
                    } catch (final NumberFormatException ex) {
                        LOG.warn("specified transaction timeout " + timeoutString + " is not a number -> ignored");
                    }
                }

                // let Grizzly run
                this.webServer.start();
            } catch (final Exception ex) {
                final String message = "could not start grizzly webcontainer"; // NOI18N
                LOG.error(message, ex);
                throw new WebServerException(message, ex);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @inheritDoc  }
     */
    @Override
    public synchronized void down() {
        if (this.webServer != null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("grizzly coming down @ " + baseuri + " :: server params: " + config.getServerParams()); // NOI18N
            }

            this.webServer.stop();
            this.webServer = null;
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Pipes java logs to logj4.
     *
     * @version  1.0, 20100518
     */
    private static final class Java2Log4jLogger extends java.util.logging.Logger {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Java2Log4jLogger object.
         */
        Java2Log4jLogger() {
            super("Java2Log4jLogger", null); // NOI18N
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   handler  DOCUMENT ME!
         *
         * @throws  SecurityException  DOCUMENT ME!
         */
        @Override
        public void addHandler(final Handler handler) throws SecurityException {
            // we don't add handler, we forward to Log4j
        }

        /**
         * DOCUMENT ME!
         *
         * @param  sourceClass   DOCUMENT ME!
         * @param  sourceMethod  DOCUMENT ME!
         */
        @Override
        public void entering(final String sourceClass, final String sourceMethod) {
            entering(sourceClass, sourceMethod, new Object[] {});
        }

        /**
         * DOCUMENT ME!
         *
         * @param  sourceClass   DOCUMENT ME!
         * @param  sourceMethod  DOCUMENT ME!
         * @param  param1        DOCUMENT ME!
         */
        @Override
        public void entering(final String sourceClass, final String sourceMethod, final Object param1) {
            entering(sourceClass, sourceMethod, new Object[] { param1 });
        }

        /**
         * DOCUMENT ME!
         *
         * @param  sourceClass   DOCUMENT ME!
         * @param  sourceMethod  DOCUMENT ME!
         * @param  params        DOCUMENT ME!
         */
        @Override
        public void entering(final String sourceClass, final String sourceMethod, final Object[] params) {
            final Logger log = Logger.getLogger(sourceClass);
            if (log.isDebugEnabled()) {
                final StringBuilder sb = new StringBuilder();
                for (final Object o : params) {
                    sb.append(o).append(", "); // NOI18N
                }

                if (sb.length() == 0) {
                    sb.append("none given"); // NOI18N
                } else {
                    sb.deleteCharAt(sb.length() - 1);
                    sb.deleteCharAt(sb.length() - 1);
                }

                log.debug("ENTER: " + sourceMethod + " :: params: " + sb.toString()); // NOI18N
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  sourceClass   DOCUMENT ME!
         * @param  sourceMethod  DOCUMENT ME!
         */
        @Override
        public void exiting(final String sourceClass, final String sourceMethod) {
            exiting(sourceClass, sourceMethod, null);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  sourceClass   DOCUMENT ME!
         * @param  sourceMethod  DOCUMENT ME!
         * @param  result        DOCUMENT ME!
         */
        @Override
        public void exiting(final String sourceClass, final String sourceMethod, final Object result) {
            final Logger log = Logger.getLogger(sourceClass);
            if (log.isDebugEnabled()) {
                log.debug("EXIT: " + sourceMethod + " :: result: " + result); // NOI18N
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public Filter getFilter() {
            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public synchronized Handler[] getHandlers() {
            return new Handler[] {};
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public Level getLevel() {
            return super.getLevel();
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public String getName() {
            return super.getName();
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public java.util.logging.Logger getParent() {
            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public ResourceBundle getResourceBundle() {
            return super.getResourceBundle();
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public String getResourceBundleName() {
            return super.getResourceBundleName();
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public synchronized boolean getUseParentHandlers() {
            return false;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   level  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public boolean isLoggable(final Level level) {
            return true;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  record  DOCUMENT ME!
         */
        @Override
        public void log(final LogRecord record) {
            final int level = record.getLevel().intValue();
            final Logger log = Logger.getLogger(
                    (record.getSourceClassName() == null) ? getLoggingClass() : record.getSourceClassName());
            if ((level < Level.INFO.intValue()) && log.isDebugEnabled()) {
                log.debug(record.getMessage(), record.getThrown());
            } else if ((level < Level.WARNING.intValue()) && log.isInfoEnabled()) {
                log.info(record.getMessage(), record.getThrown());
            } else if (level < Level.SEVERE.intValue()) {
                log.warn(record.getMessage(), record.getThrown());
            } else {
                log.error(record.getMessage(), record.getThrown());
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String getLoggingClass() {
            for (final StackTraceElement ste : new Throwable().getStackTrace()) {
                if (!ste.getClassName().contains(this.getClass().getName())) {
                    return ste.getClassName();
                }
            }
            return "unknown class"; // NOI18N
        }

        /**
         * DOCUMENT ME!
         *
         * @param  level  DOCUMENT ME!
         * @param  msg    DOCUMENT ME!
         */
        @Override
        public void log(final Level level, final String msg) {
            log(new LogRecord(level, msg));
        }

        /**
         * DOCUMENT ME!
         *
         * @param  level   DOCUMENT ME!
         * @param  msg     DOCUMENT ME!
         * @param  param1  DOCUMENT ME!
         */
        @Override
        public void log(final Level level, final String msg, final Object param1) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setParameters(new Object[] { param1 });
            log(lr);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  level   DOCUMENT ME!
         * @param  msg     DOCUMENT ME!
         * @param  params  DOCUMENT ME!
         */
        @Override
        public void log(final Level level, final String msg, final Object[] params) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setParameters(params);
            log(lr);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  level   DOCUMENT ME!
         * @param  msg     DOCUMENT ME!
         * @param  thrown  DOCUMENT ME!
         */
        @Override
        public void log(final Level level, final String msg, final Throwable thrown) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setThrown(thrown);
            log(lr);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  level         DOCUMENT ME!
         * @param  sourceClass   DOCUMENT ME!
         * @param  sourceMethod  DOCUMENT ME!
         * @param  msg           DOCUMENT ME!
         */
        @Override
        public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            log(lr);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  level         DOCUMENT ME!
         * @param  sourceClass   DOCUMENT ME!
         * @param  sourceMethod  DOCUMENT ME!
         * @param  msg           DOCUMENT ME!
         * @param  param1        DOCUMENT ME!
         */
        @Override
        public void logp(
                final Level level,
                final String sourceClass,
                final String sourceMethod,
                final String msg,
                final Object param1) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setParameters(new Object[] { param1 });
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            log(lr);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  level         DOCUMENT ME!
         * @param  sourceClass   DOCUMENT ME!
         * @param  sourceMethod  DOCUMENT ME!
         * @param  msg           DOCUMENT ME!
         * @param  params        DOCUMENT ME!
         */
        @Override
        public void logp(
                final Level level,
                final String sourceClass,
                final String sourceMethod,
                final String msg,
                final Object[] params) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setParameters(params);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            log(lr);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  level         DOCUMENT ME!
         * @param  sourceClass   DOCUMENT ME!
         * @param  sourceMethod  DOCUMENT ME!
         * @param  msg           DOCUMENT ME!
         * @param  thrown        DOCUMENT ME!
         */
        @Override
        public void logp(
                final Level level,
                final String sourceClass,
                final String sourceMethod,
                final String msg,
                final Throwable thrown) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setThrown(thrown);
            log(lr);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  level         DOCUMENT ME!
         * @param  sourceClass   DOCUMENT ME!
         * @param  sourceMethod  DOCUMENT ME!
         * @param  bundleName    DOCUMENT ME!
         * @param  msg           DOCUMENT ME!
         */
        @Override
        public void logrb(
                final Level level,
                final String sourceClass,
                final String sourceMethod,
                final String bundleName,
                final String msg) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setResourceBundleName(bundleName);
            log(lr);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  level         DOCUMENT ME!
         * @param  sourceClass   DOCUMENT ME!
         * @param  sourceMethod  DOCUMENT ME!
         * @param  bundleName    DOCUMENT ME!
         * @param  msg           DOCUMENT ME!
         * @param  param1        DOCUMENT ME!
         */
        @Override
        public void logrb(
                final Level level,
                final String sourceClass,
                final String sourceMethod,
                final String bundleName,
                final String msg,
                final Object param1) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setResourceBundleName(bundleName);
            lr.setParameters(new Object[] { param1 });
            log(lr);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  level         DOCUMENT ME!
         * @param  sourceClass   DOCUMENT ME!
         * @param  sourceMethod  DOCUMENT ME!
         * @param  bundleName    DOCUMENT ME!
         * @param  msg           DOCUMENT ME!
         * @param  params        DOCUMENT ME!
         */
        @Override
        public void logrb(
                final Level level,
                final String sourceClass,
                final String sourceMethod,
                final String bundleName,
                final String msg,
                final Object[] params) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setResourceBundleName(bundleName);
            lr.setParameters(params);
            log(lr);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  level         DOCUMENT ME!
         * @param  sourceClass   DOCUMENT ME!
         * @param  sourceMethod  DOCUMENT ME!
         * @param  bundleName    DOCUMENT ME!
         * @param  msg           DOCUMENT ME!
         * @param  thrown        DOCUMENT ME!
         */
        @Override
        public void logrb(
                final Level level,
                final String sourceClass,
                final String sourceMethod,
                final String bundleName,
                final String msg,
                final Throwable thrown) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setResourceBundleName(bundleName);
            lr.setThrown(thrown);
            log(lr);
        }

        /**
         * DOCUMENT ME!
         *
         * @param   handler  DOCUMENT ME!
         *
         * @throws  SecurityException  DOCUMENT ME!
         */
        @Override
        public synchronized void removeHandler(final Handler handler) throws SecurityException {
            // we don't care about handlers
        }

        /**
         * DOCUMENT ME!
         *
         * @param   newFilter  DOCUMENT ME!
         *
         * @throws  SecurityException  DOCUMENT ME!
         */
        @Override
        public void setFilter(final Filter newFilter) throws SecurityException {
            // we don't care about filters
        }

        /**
         * DOCUMENT ME!
         *
         * @param   newLevel  DOCUMENT ME!
         *
         * @throws  SecurityException  DOCUMENT ME!
         */
        @Override
        public void setLevel(final Level newLevel) throws SecurityException {
            // we don't care about levels
        }

        /**
         * DOCUMENT ME!
         *
         * @param  parent  DOCUMENT ME!
         */
        @Override
        public void setParent(final java.util.logging.Logger parent) {
            // we don't care about parents
        }

        /**
         * DOCUMENT ME!
         *
         * @param  useParentHandlers  DOCUMENT ME!
         */
        @Override
        public synchronized void setUseParentHandlers(final boolean useParentHandlers) {
            // we don't care about parent handlers
        }

        /**
         * DOCUMENT ME!
         *
         * @param  sourceClass   DOCUMENT ME!
         * @param  sourceMethod  DOCUMENT ME!
         * @param  thrown        DOCUMENT ME!
         */
        @Override
        public void throwing(final String sourceClass, final String sourceMethod, final Throwable thrown) {
            super.throwing(sourceClass, sourceMethod, thrown);
        }
    }
}
