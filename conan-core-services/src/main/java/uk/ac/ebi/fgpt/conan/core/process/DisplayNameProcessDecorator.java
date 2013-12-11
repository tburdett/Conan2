package uk.ac.ebi.fgpt.conan.core.process;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;

/**
 * A implementation of a {@link ConanProcess} that decorates the underlying process with a display name that is distinct
 * from the unique name assigned to the process.  This allows the same underlying process to be given different names
 * depending, for example, on it's position in a {@link uk.ac.ebi.fgpt.conan.model.ConanPipeline}.
 *
 * @author Tony Burdett
 * @date 09/09/11
 */
public class DisplayNameProcessDecorator extends AbstractProcessDecorator {
    private String displayName;

    public DisplayNameProcessDecorator(ConanProcess process, String displayName) {
        super(process);
        this.displayName = displayName;
    }

    @Override
    public String getExecutable() {
        return "";
    }

    @Override
    public String getName() {
        return displayName;
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {
        return true;
    }
}
