package com.devsync.ai.api.dto.pm;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageEnvelope<T>(
        List<T> items, int pageNumber, int pageSize, long totalElements, int totalPages) {

    public static <T, U> PageEnvelope<U> map(Page<T> src, Function<T, U> mapElement) {
        return new PageEnvelope<>(
                src.getContent().stream().map(mapElement).toList(),
                src.getNumber(),
                src.getSize(),
                src.getTotalElements(),
                src.getTotalPages());
    }
}
