package com.spassu.livros.orchestration.flowcockpit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a BPM-like gateway node and its outgoing branches.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(FlowGateways.class)
public @interface FlowGateway {

    /** Stable node identifier used to connect edges. */
    String nodeId();

    /** Display name rendered in the FlowCockpit canvas. */
    String name();

    /** Optional business description of the decision/split. */
    String description() default "";

    /** Gateway type. Only EXCLUSIVE_GATEWAY or PARALLEL_GATEWAY are expected. */
    NodeType type();

    /** Outgoing branches from this gateway. */
    FlowBranch[] branches();
}
