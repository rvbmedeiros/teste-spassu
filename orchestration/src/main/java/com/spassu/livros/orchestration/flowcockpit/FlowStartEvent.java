package com.spassu.livros.orchestration.flowcockpit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the BPM-like start event of a flow.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FlowStartEvent {

    String nodeId() default "start";

    String name() default "Início";

    String description() default "";

    /** First node id executed after start. */
    String nextStep();
}
