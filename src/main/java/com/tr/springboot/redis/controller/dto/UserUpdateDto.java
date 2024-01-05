package com.tr.springboot.redis.controller.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author: TR
 */
@Data
public class UserUpdateDto {

    @ApiModelProperty(required = true)
    @NotNull
    private Integer id;
    private String name;
    private Integer age;

}
