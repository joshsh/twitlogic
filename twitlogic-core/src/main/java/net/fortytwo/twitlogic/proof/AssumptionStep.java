package net.fortytwo.twitlogic.proof;

import net.fortytwo.twitlogic.vocabs.Assumption;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;

/**
 * Note: this class refers to an InferenceStep whose inference rule is
 * assumption, rather than to the Assumption class itself.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class AssumptionStep extends InferenceStep {
    private static final Resource ASSUMPTION
            = new URIImpl(Assumption.ASSUMPTION);

    public AssumptionStep(final RDFizerContext context) {
        super(ASSUMPTION, null, context);
    }
}
