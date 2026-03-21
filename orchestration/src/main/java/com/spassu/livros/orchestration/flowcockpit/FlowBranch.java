package com.spassu.livros.orchestration.flowcockpit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines one outgoing branch of a gateway.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface FlowBranch {

    /** Human-readable label shown on the branch edge (example: Sim/Não). */
    String label() default "";

    /** Target nodeId this branch points to. */
    String nextStep();

    /** Optional branch intent used by the UI to style the path. */
    String edgeIntent() default "";
}
