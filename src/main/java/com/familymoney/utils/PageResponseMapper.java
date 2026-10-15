package com.familymoney.utils;

import org.springframework.data.domain.Page;

public class PageResponseMapper {

  private PageResponseMapper() {
    /* This utility class should not be instantiated */
  }

  public static <T> PageResponse<T> from(final Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast());
  }
}
