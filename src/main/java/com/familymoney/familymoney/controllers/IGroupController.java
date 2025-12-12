package com.familymoney.familymoney.controllers;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/groups")
public interface IGroupController {

  @PostMapping
  void createGroup(@NotBlank String name, @NotBlank String currency);

  void deleteGroup(@NotBlank String groupId);
}
