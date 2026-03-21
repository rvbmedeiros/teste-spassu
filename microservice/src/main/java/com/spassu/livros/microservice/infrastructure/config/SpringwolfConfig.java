package com.spassu.livros.microservice.infrastructure.config;

import io.github.springwolf.asyncapi.v3.model.channel.ChannelObject;
import io.github.springwolf.asyncapi.v3.model.operation.Operation;
import io.github.springwolf.asyncapi.v3.model.operation.OperationAction;
import io.github.springwolf.core.asyncapi.AsyncApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Configuration
public class SpringwolfConfig {

    private static final String RPC_CHANNEL_PREFIX = "rpc.";
    private static final String CHANNEL_REF_PREFIX = "#/channels/";

    @Bean
    public AsyncApiCustomizer rpcListenersOnlyCustomizer() {
        return asyncApi -> {
            if (asyncApi == null) {
                return;
            }

            Map<String, Operation> operations = asyncApi.getOperations();
            if (operations == null || operations.isEmpty()) {
                return;
            }

            LinkedHashMap<String, Operation> filteredOperations = new LinkedHashMap<>();
            Set<String> allowedChannels = new LinkedHashSet<>();

            operations.forEach((operationId, operation) -> {
                if (!isRpcReceiveOperation(operation)) {
                    return;
                }

                String channelName = extractChannelName(operation);
                if (channelName == null || !channelName.startsWith(RPC_CHANNEL_PREFIX)) {
                    return;
                }

                filteredOperations.put(operationId, operation);
                allowedChannels.add(channelName);
            });

            asyncApi.setOperations(filteredOperations);
            asyncApi.setChannels(filterChannels(asyncApi.getChannels(), allowedChannels));
        };
    }

    private static boolean isRpcReceiveOperation(Operation operation) {
        return operation != null
                && operation.getAction() == OperationAction.RECEIVE
                && operation.getChannel() != null
                && operation.getChannel().getRef() != null;
    }

    private static String extractChannelName(Operation operation) {
        String channelRef = operation.getChannel().getRef();
        if (!channelRef.startsWith(CHANNEL_REF_PREFIX)) {
            return null;
        }
        return channelRef.substring(CHANNEL_REF_PREFIX.length());
    }

    private static Map<String, ChannelObject> filterChannels(Map<String, ChannelObject> channels, Set<String> allowedChannels) {
        if (channels == null || channels.isEmpty() || allowedChannels.isEmpty()) {
            return Collections.emptyMap();
        }

        LinkedHashMap<String, ChannelObject> filtered = new LinkedHashMap<>();
        channels.forEach((channelName, channelObject) -> {
            if (allowedChannels.contains(channelName)) {
                filtered.put(channelName, channelObject);
            }
        });
        return filtered;
    }
}