package com.xiangyun.operation;

import java.util.List;

public record ResourceBatchRequest(List<String> ids, String action) {
}
