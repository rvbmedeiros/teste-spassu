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

    /** Stable node id for graph links. If empty, FlowRegistry will generate one from order. */
    String nodeId() default "";

    /** Brief description of what this step does. */
    String description() default "";

    /** Outgoing node ids. Empty means no explicit next step. */
    String[] nextSteps() default {};

    /** Why this activity exists from a business perspective. */
    String purpose() default "";

    /** What input this activity expects. */
    String inputHint() default "";

    /** What output this activity produces. */
    String outputHint() default "";

    /** Main failure mode or business impact if this step fails. */
    String failureHint() default "";

    /**
     * Method name to call on failure for compensation.
     * Empty string means no rollback.
     */
    String rollbackStep() default "";
}
