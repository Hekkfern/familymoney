package com.familymoney.utils;

import java.util.List;
import org.springframework.data.domain.Slice;

public record SliceResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last) {

  public static <T> SliceResponse<T> from(Slice<T> page) {
    return new SliceResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast());
  }
}
