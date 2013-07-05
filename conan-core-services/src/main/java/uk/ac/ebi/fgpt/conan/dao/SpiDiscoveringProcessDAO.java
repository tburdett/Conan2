package uk.ac.ebi.fgpt.conan.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.util.*;

/**
 * A DAO that can load {@link uk.ac.ebi.fgpt.conan.model.ConanProcess}es dynamically from the classpath by looking for
 * all implementations of ConanProcesses declared in a services file entry.  Annotating your ConanProcess
 * implementations with the annotation {@link net.sourceforge.fluxion.spi.Spi} should be sufficient to enable their
 * discovery.
 *
 * @author Tony Burdett
 * @date 25-Nov-2010
 */
public class SpiDiscoveringProcessDAO implements ConanProcessDAO {
    /**
     * Maps a process name to a process, to ensure uniqueness.
     */
    private Map<String, ConanProcess> processMap;

    private ConanProperties conanProperties;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ConanProperties getConanProperties() {
        return conanProperties;
    }

    /**
     * Sets the {@link Properties} that represent the Conan global property set.  Properties must be injected here
     * before discovering and loading {@link uk.ac.ebi.fgpt.conan.model.ConanProcess}es purely to ensure we don't get
     * into a condition where processes are loaded before properties they rely on have been loaded by spring.  This
     * injects the raw underlying Properties object instead of the ConanProperties singleton object which can be used by
     * {@link ConanProcess}es, but in order to inject this the singleton must have been fully generated, by contract.
     * This is a bit of a lifecyle hack, really, but its otherwise quite an elegant solution to the problem of how to
     * inject global properties into dynamically loaded processes.
     *
     * @param conanProperties the properties that are globally set in Conan
     */
    public void setConanProperties(ConanProperties conanProperties) {
        // set field
        this.conanProperties = conanProperties;
        // check initialization

        StringBuilder sb = new StringBuilder();
        sb.append("Set the following global properties for the Conan framework...\n");
        for (Object key : ConanProperties.keySet()) {
            sb.append("\t")
                    .append(key.toString())
                    .append(" = ")
                    .append(ConanProperties.getProperty(key.toString()))
                    .append("\n");
        }
        getLog().debug(sb.toString());
    }

    /**
     * Get every known process that could be run by Conan.  Each process will normally belong to at least one pipeline,
     * but there may be some unassigned processes that may allow users to create new pipelines.
     * <p/>
     * Note that processes are lazily instantiated on the first request to <code>getAllAvailableProcesses()</code>.  At
     * this point, all known {@link uk.ac.ebi.fgpt.conan.model.ConanProcess} implementations are located and loaded.
     * Subsequent calls to <code>getAllAvailableProcesses()</code> will return the original set of processes, rather
     * than triggering new instance creation.
     * <p/>
     * On loading new processes, this method may throw an {@link Error} if there are multiple Processes assigned the
     * same name.  Process names should be unique - having processes with duplicated names on the classpath is
     * considered to be a JVM configuration error, and recovery should not be attempted.
     *
     * @return the list of processes available
     */
    public synchronized Collection<ConanProcess> getProcesses() {
        // lazily load available processes
        if (processMap == null) {
            ServiceLoader<ConanProcess> processLoader = ServiceLoader.load(ConanProcess.class);
            Iterator<ConanProcess> processIterator = processLoader.iterator();

            processMap = new HashMap<String, ConanProcess>();
            while (processIterator.hasNext()) {
                ConanProcess p = processIterator.next();
                if (!processMap.containsKey(p.getName())) {
                    processMap.put(p.getName(), p);
                } else {
                    if (processMap.get(p.getName()) != p) {
                        String msg = "Multiple different Conan processes with the same process name " +
                                "('" + p.getName() + "') are present on the classpath.  Process names must be unique.";
                        getLog().error(msg);
                        throw new ServiceConfigurationError(msg);
                    }
                }
            }
            getLog().debug("Loaded " + processMap.values().size() + " known processes");
        }

        return processMap.values();
    }

    public ConanProcess getProcess(String processName) {
        for (ConanProcess p : getProcesses()) {
            getLog().trace("Next process: " + p.getName());
            if (p.getName().equals(processName)) {
                return p;
            }
        }

        // if we got to here, no process matched this name, so return null
        getLog().warn("No process called '" + processName + "' was available on the classpath");
        return null;
    }
}
