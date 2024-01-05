package com.tr.springboot.redis.controller.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: TR
 */
@Data
public class UserAddDto {

    @ApiModelProperty(required = true)
    @NotBlank
    private String name;
    private Integer age;

}
