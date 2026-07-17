package com.xiangyun.operation;

import java.util.List;

public record NotificationCenterView(List<NotificationItemView> items, int unreadCount, int total) {
}
