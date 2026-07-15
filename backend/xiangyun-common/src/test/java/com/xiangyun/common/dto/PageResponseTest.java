package com.xiangyun.common.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void normalizesBoundsAndCalculatesTotalPages() {
        PageResponse<String> response = PageResponse.of(List.of("a", "b"), 0, 500, 201);

        assertThat(response.page()).isEqualTo(1);
        assertThat(response.pageSize()).isEqualTo(100);
        assertThat(response.total()).isEqualTo(201);
        assertThat(response.totalPages()).isEqualTo(3);
    }

    @Test
    void emptyResultHasNoPages() {
        assertThat(PageResponse.of(List.of(), 1, 20, 0).totalPages()).isZero();
    }
}
