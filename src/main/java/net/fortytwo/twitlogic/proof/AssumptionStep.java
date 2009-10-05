package net.fortytwo.twitlogic.proof;

import net.fortytwo.twitlogic.vocabs.Assumption;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;

/**
 * Note: this class refers to an InferenceStep whose inference rule is
 * assumption, rather than to the Assumption class itself.
 * <p/>
 * User: josh
 * Date: Oct 4, 2009
 * Time: 3:57:24 AM
 */
public class AssumptionStep extends InferenceStep {
    private static final Resource ASSUMPTION
            = new URIImpl(Assumption.ASSUMPTION);

    public AssumptionStep(final RDFizerContext context) {
        super(ASSUMPTION, null, context);
    }
}
