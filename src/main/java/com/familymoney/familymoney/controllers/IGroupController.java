package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.dtos.group.CreateGroupRequestDto;
import com.familymoney.familymoney.controllers.dtos.group.UpdateGroupRequestDto;
import com.familymoney.familymoney.types.GroupId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/groups")
public interface IGroupController {

  @PostMapping
  void createGroup(@RequestBody @Valid CreateGroupRequestDto request);

  @DeleteMapping("/{groupId}")
  void deleteGroup(@PathVariable @NotBlank String groupId);

  @PatchMapping("/{groupId}")
  void updateGroupInfo(@PathVariable @NotNull GroupId groupId, @RequestBody @Valid UpdateGroupRequestDto request);
}
