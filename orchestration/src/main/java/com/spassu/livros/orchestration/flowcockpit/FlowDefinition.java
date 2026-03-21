package com.spassu.livros.orchestration.flowcockpit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Spring bean as an orchestration flow.
 * FlowRegistry scans all beans annotated with this at startup
 * and builds the static flow graph exposed via GET /flows.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FlowDefinition {

    /** Unique machine-readable identifier for this flow. */
    String id();

    /** Human-readable display name. */
    String name();

    /** Brief description of what this flow orchestrates. */
    String description() default "";

    /** Team or role responsible for this flow. */
    String owner() default "";

    /** Human-readable flow version (example: 1.0.0). */
    String version() default "";

    /** Domain grouping used by FlowCockpit catalog filters. */
    String domainTag() default "";

    /** Business goal expressed in one concise sentence. */
    String businessGoal() default "";
}
