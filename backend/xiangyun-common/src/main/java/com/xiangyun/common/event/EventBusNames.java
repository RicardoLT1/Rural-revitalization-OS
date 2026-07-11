package com.xiangyun.common.event;

public final class EventBusNames {
    public static final String EXCHANGE = "xiangyun.business.events";
    public static final String WORKFLOW_QUEUE = "xiangyun.analysis.workflow-changed";
    public static final String WORKFLOW_DEAD_QUEUE = "xiangyun.analysis.workflow-changed.dlq";
    public static final String WORKFLOW_ROUTING_KEY = "workflow.changed";
    public static final String DEAD_EXCHANGE = "xiangyun.business.events.dlx";

    private EventBusNames() {
    }
}
