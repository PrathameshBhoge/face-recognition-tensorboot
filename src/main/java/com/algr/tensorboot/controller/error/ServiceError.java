package com.algr.tensorboot.controller.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ServiceError {
    private String errorMessage;
}
