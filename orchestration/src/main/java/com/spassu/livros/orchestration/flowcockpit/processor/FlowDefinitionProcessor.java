package com.spassu.livros.orchestration.flowcockpit.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SupportedAnnotationTypes(FlowDefinitionProcessor.FLOW_DEFINITION_ANNOTATION)
public class FlowDefinitionProcessor extends AbstractProcessor {

    static final String FLOW_DEFINITION_ANNOTATION = "com.spassu.livros.orchestration.flowcockpit.FlowDefinition";

    private static final String FLOW_START_EVENT_ANNOTATION = "com.spassu.livros.orchestration.flowcockpit.FlowStartEvent";
    private static final String FLOW_STEP_ANNOTATION = "com.spassu.livros.orchestration.flowcockpit.FlowStep";
    private static final String FLOW_GATEWAY_ANNOTATION = "com.spassu.livros.orchestration.flowcockpit.FlowGateway";
    private static final String FLOW_GATEWAYS_ANNOTATION = "com.spassu.livros.orchestration.flowcockpit.FlowGateways";
    private static final String FLOW_END_EVENT_ANNOTATION = "com.spassu.livros.orchestration.flowcockpit.FlowEndEvent";
    private static final String FLOW_END_EVENTS_ANNOTATION = "com.spassu.livros.orchestration.flowcockpit.FlowEndEvents";

    private static final String NODE_TYPE_EXCLUSIVE = "EXCLUSIVE_GATEWAY";
    private static final String NODE_TYPE_PARALLEL = "PARALLEL_GATEWAY";

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        TypeElement flowDefinitionType = processingEnv.getElementUtils().getTypeElement(FLOW_DEFINITION_ANNOTATION);
        if (flowDefinitionType == null) {
            return false;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(flowDefinitionType)) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }
            validateFlow((TypeElement) element);
        }

        return false;
    }

    private void validateFlow(TypeElement flowType) {
        AnnotationMirror flowDefinition = findAnnotation(flowType, FLOW_DEFINITION_ANNOTATION);
        String flowId = stringValue(flowDefinition, "id", flowType.getSimpleName().toString());

        Map<String, Element> nodeOwners = new LinkedHashMap<>();
        List<StepData> steps = new ArrayList<>();
        List<GatewayData> gateways = new ArrayList<>();

        AnnotationMirror start = findAnnotation(flowType, FLOW_START_EVENT_ANNOTATION);
        String startNextStep = null;
        if (start != null) {
            String startNodeId = stringValue(start, "nodeId", "start").trim();
            registerNode(flowType, flowId, nodeOwners, startNodeId);
            startNextStep = stringValue(start, "nextStep", "").trim();
            if (startNextStep.isBlank()) {
                error(flowType, "flow=" + flowId + " start event must define non-blank nextStep");
            }
        }

        for (Element enclosed : flowType.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.METHOD) {
                continue;
            }

            AnnotationMirror stepAnnotation = findAnnotation(enclosed, FLOW_STEP_ANNOTATION);
            if (stepAnnotation == null) {
                continue;
            }

            int order = intValue(stepAnnotation, "order", 0);
            String explicitNodeId = stringValue(stepAnnotation, "nodeId", "").trim();
            String nodeId = explicitNodeId.isBlank() ? "step-" + order : explicitNodeId;
            registerNode(enclosed, flowId, nodeOwners, nodeId);

            List<String> nextSteps = stringListValue(stepAnnotation, "nextSteps");
            steps.add(new StepData((ExecutableElement) enclosed, nodeId, nextSteps));
        }

        for (AnnotationMirror gatewayAnnotation : findRepeatableAnnotations(flowType, FLOW_GATEWAY_ANNOTATION, FLOW_GATEWAYS_ANNOTATION)) {
            String gatewayNodeId = stringValue(gatewayAnnotation, "nodeId", "").trim();
            registerNode(flowType, flowId, nodeOwners, gatewayNodeId);

            String gatewayType = enumName(gatewayAnnotation, "type");
            List<BranchData> branches = new ArrayList<>();
            List<AnnotationMirror> branchMirrors = nestedAnnotationListValue(gatewayAnnotation, "branches");

            for (int i = 0; i < branchMirrors.size(); i++) {
                AnnotationMirror branch = branchMirrors.get(i);
                String label = stringValue(branch, "label", "").trim();
                String nextStep = stringValue(branch, "nextStep", "").trim();
                String edgeIntent = stringValue(branch, "edgeIntent", "").trim();
                branches.add(new BranchData(branch, i, label, nextStep, edgeIntent));
            }

            gateways.add(new GatewayData(flowType, gatewayNodeId, gatewayType, branches));
        }

        for (AnnotationMirror endEvent : findRepeatableAnnotations(flowType, FLOW_END_EVENT_ANNOTATION, FLOW_END_EVENTS_ANNOTATION)) {
            String endNodeId = stringValue(endEvent, "nodeId", "").trim();
            registerNode(flowType, flowId, nodeOwners, endNodeId);
        }

        Set<String> knownNodeIds = new LinkedHashSet<>(nodeOwners.keySet());

        if (startNextStep != null && !startNextStep.isBlank() && !knownNodeIds.contains(startNextStep)) {
            error(flowType, "flow=" + flowId + " start nextStep='" + startNextStep + "' points to unknown nodeId");
        }

        for (StepData step : steps) {
            for (String nextStep : step.nextSteps()) {
                if (nextStep == null || nextStep.isBlank()) {
                    error(step.owner(), "flow=" + flowId + " step='" + step.nodeId() + "' defines blank nextStep");
                    continue;
                }
                if (!knownNodeIds.contains(nextStep)) {
                    error(step.owner(), "flow=" + flowId + " step='" + step.nodeId() + "' points to unknown nextStep='" + nextStep + "'");
                }
            }
        }

        for (GatewayData gateway : gateways) {
            boolean checkGatewayRules = NODE_TYPE_EXCLUSIVE.equals(gateway.type())
                    || NODE_TYPE_PARALLEL.equals(gateway.type());

            if (!checkGatewayRules) {
                continue;
            }

            if (NODE_TYPE_EXCLUSIVE.equals(gateway.type()) && gateway.branches().size() != 2) {
                error(gateway.owner(), "flow=" + flowId + " gateway='" + gateway.nodeId()
                        + "' type=" + gateway.type() + " must declare exactly 2 branches");
                continue;
            }

            if (NODE_TYPE_PARALLEL.equals(gateway.type()) && gateway.branches().isEmpty()) {
                error(gateway.owner(), "flow=" + flowId + " gateway='" + gateway.nodeId()
                        + "' type=" + gateway.type() + " must declare at least 1 branch");
                continue;
            }

            Set<String> exclusiveIntents = new LinkedHashSet<>();
            for (BranchData branch : gateway.branches()) {
                if (branch.label().isBlank()) {
                    error(gateway.owner(), "flow=" + flowId + " gateway='" + gateway.nodeId()
                            + "' branchIndex=" + branch.index() + " must declare a non-blank label");
                }

                if (NODE_TYPE_EXCLUSIVE.equals(gateway.type())) {
                    if (branch.edgeIntent().isBlank()) {
                        error(gateway.owner(), "flow=" + flowId + " gateway='" + gateway.nodeId()
                                + "' branchIndex=" + branch.index() + " must declare non-blank edgeIntent");
                    } else if (!exclusiveIntents.add(branch.edgeIntent())) {
                        error(gateway.owner(), "flow=" + flowId + " gateway='" + gateway.nodeId()
                                + "' has duplicated edgeIntent='" + branch.edgeIntent() + "'");
                    }
                }

                if (branch.nextStep().isBlank()) {
                    error(gateway.owner(), "flow=" + flowId + " gateway='" + gateway.nodeId()
                            + "' branchIndex=" + branch.index() + " must declare non-blank nextStep");
                    continue;
                }

                if (!knownNodeIds.contains(branch.nextStep())) {
                    error(gateway.owner(), "flow=" + flowId + " gateway='" + gateway.nodeId() + "' branchIndex=" + branch.index()
                            + " points to unknown nextStep='" + branch.nextStep() + "'");
                }
            }
        }
    }

    private void registerNode(Element owner, String flowId, Map<String, Element> nodeOwners, String nodeId) {
        if (nodeId == null || nodeId.isBlank()) {
            error(owner, "flow=" + flowId + " contains blank nodeId");
            return;
        }

        Element existingOwner = nodeOwners.putIfAbsent(nodeId, owner);
        if (existingOwner != null && !Objects.equals(existingOwner, owner)) {
            error(owner, "flow=" + flowId + " duplicated nodeId='" + nodeId + "'");
        }
    }

    private AnnotationMirror findAnnotation(Element element, String annotationFqcn) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (annotationFqcn.equals(annotationFqcn(mirror))) {
                return mirror;
            }
        }
        return null;
    }

    private List<AnnotationMirror> findRepeatableAnnotations(Element element, String itemAnnotationFqcn, String containerAnnotationFqcn) {
        List<AnnotationMirror> annotations = new ArrayList<>();

        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            String fqcn = annotationFqcn(mirror);
            if (itemAnnotationFqcn.equals(fqcn)) {
                annotations.add(mirror);
            }
            if (containerAnnotationFqcn.equals(fqcn)) {
                annotations.addAll(nestedAnnotationListValue(mirror, "value"));
            }
        }

        return annotations;
    }

    private String annotationFqcn(AnnotationMirror mirror) {
        return ((TypeElement) mirror.getAnnotationType().asElement()).getQualifiedName().toString();
    }

    private String stringValue(AnnotationMirror annotation, String name, String defaultValue) {
        if (annotation == null) {
            return defaultValue;
        }

        Object value = annotationValue(annotation, name);
        if (value == null) {
            return defaultValue;
        }

        return String.valueOf(value);
    }

    private int intValue(AnnotationMirror annotation, String name, int defaultValue) {
        if (annotation == null) {
            return defaultValue;
        }

        Object value = annotationValue(annotation, name);
        if (value instanceof Integer i) {
            return i;
        }
        return defaultValue;
    }

    private String enumName(AnnotationMirror annotation, String name) {
        Object value = annotationValue(annotation, name);
        if (value instanceof VariableElement variableElement) {
            return variableElement.getSimpleName().toString();
        }
        return "";
    }

    private List<String> stringListValue(AnnotationMirror annotation, String name) {
        Object value = annotationValue(annotation, name);
        if (!(value instanceof List<?> values)) {
            return List.of();
        }

        List<String> result = new ArrayList<>(values.size());
        for (Object entry : values) {
            if (entry instanceof AnnotationValue annotationValue) {
                result.add(String.valueOf(annotationValue.getValue()));
            }
        }
        return result;
    }

    private List<AnnotationMirror> nestedAnnotationListValue(AnnotationMirror annotation, String name) {
        Object value = annotationValue(annotation, name);
        if (!(value instanceof List<?> values)) {
            return List.of();
        }

        List<AnnotationMirror> nested = new ArrayList<>(values.size());
        for (Object entry : values) {
            if (entry instanceof AnnotationValue annotationValue
                    && annotationValue.getValue() instanceof AnnotationMirror nestedMirror) {
                nested.add(nestedMirror);
            }
        }
        return nested;
    }

    private Object annotationValue(AnnotationMirror annotation, String name) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(name)) {
                return entry.getValue().getValue();
            }
        }

        for (Element enclosed : annotation.getAnnotationType().asElement().getEnclosedElements()) {
            if (!(enclosed instanceof ExecutableElement executableElement)) {
                continue;
            }
            if (executableElement.getSimpleName().contentEquals(name) && executableElement.getDefaultValue() != null) {
                return executableElement.getDefaultValue().getValue();
            }
        }

        return null;
    }

    private void error(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private record StepData(ExecutableElement owner, String nodeId, List<String> nextSteps) {
    }

    private record BranchData(AnnotationMirror owner, int index, String label, String nextStep, String edgeIntent) {
    }

    private record GatewayData(TypeElement owner, String nodeId, String type, List<BranchData> branches) {
    }
}
