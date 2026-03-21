package com.spassu.livros.orchestration.flowcockpit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method inside a {@link FlowDefinition} bean as a named step.
 * The annotation metadata is used by {@link FlowRegistry} to build
 * the workflow graph shown in FlowCockpit.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FlowStep {

    /** Execution order within the flow (1-based). */
    int order();

    /** Human-readable step name shown in FlowCockpit UI. */
    String name();

    /** Brief description of what this step does. */
    String description() default "";

    /**
     * Method name to call on failure for compensation.
     * Empty string means no rollback.
     */
    String rollbackStep() default "";
}
