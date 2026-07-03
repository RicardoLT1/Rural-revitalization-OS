package com.xiangyun.analysis;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class ResponseTextSanitizer {

    private static final Charset GBK = Charset.forName("GBK");

    private ResponseTextSanitizer() {
    }

    public static Object clean(Object value) {
        if (value instanceof String text) {
            return repair(text);
        }
        if (value instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> clean(entry.getValue()),
                            (left, right) -> right,
                            java.util.LinkedHashMap::new
                    ));
        }
        if (value instanceof List<?> list) {
            return list.stream().map(ResponseTextSanitizer::clean).toList();
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> cleanMap(Map<String, Object> value) {
        return (Map<String, Object>) clean(value);
    }

    private static String repair(String text) {
        String mapped = directMapping(text);
        if (mapped != null) {
            return mapped;
        }
        if (!looksGbkMojibake(text)) {
            return text;
        }
        try {
            String repaired = new String(text.getBytes(GBK), StandardCharsets.UTF_8);
            return looksGbkMojibake(repaired) ? text : repaired;
        } catch (Exception ex) {
            return text;
        }
    }

    private static boolean looksGbkMojibake(String text) {
        return text.contains("缁") || text.contains("绱") || text.contains("钀")
                || text.contains("鏂") || text.contains("鍐") || text.contains("娲")
                || text.contains("闈") || text.contains("涔") || text.contains("璧")
                || text.contains("寰") || text.contains("瀹") || text.contains("椋")
                || text.contains("浼") || text.contains("鎷") || text.contains("鍟")
                || text.contains("銆") || text.contains("€");
    }

    private static String directMapping(String text) {
        return switch (text) {
            case "涓?", "涓�" -> "个";
            case "椤?", "椤�" -> "项";
            case "资源总数", "可合作资源", "待审批数", "风险流程" -> text;
            default -> null;
        };
    }
}
