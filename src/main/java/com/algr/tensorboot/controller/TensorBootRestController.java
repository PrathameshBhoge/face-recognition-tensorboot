package com.algr.tensorboot.controller;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.algr.tensorboot.controller.error.ServiceError;
import com.algr.tensorboot.controller.error.ServiceException;
import com.algr.tensorboot.data.Recognition;
import com.algr.tensorboot.data.RecognitionResult;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Validated
@RestController
@RequestMapping(value = "${services.baseApiPath}")
public class TensorBootRestController {

    private final ImageProcessingService imageProcessingService;

    @Autowired
    public TensorBootRestController(ImageProcessingService imageProcessingService) {
        this.imageProcessingService = imageProcessingService;
    }

    @ApiOperation(value = "Make a POST request to upload the file", produces = "application/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PostMapping(value = "/recognizeFile", produces = "application/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseBody List<Recognition> handleFileUpload(@Valid @NotNull @RequestBody MultipartFile file) {
        log.debug("Image upload requested");
        RecognitionResult recognitionResult = imageProcessingService.processImageFile(file);
        log.debug("Found objects: {}", recognitionResult.getRecognitions());
        return recognitionResult.getRecognitions();
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ServiceError handleMaxUploadSizeExceededException(MaxUploadSizeExceededException exc) {
        log.info("Error during processing request", exc);
        return new ServiceError("File is too large");
    }

    @ExceptionHandler(ServiceException.class)
    public ServiceError handleServiceException(ServiceException exc) {
        log.info("Error during processing request", exc.getMessage());
        log.debug("Error during processing request", exc);
        return new ServiceError(exc.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ServiceError> handleConstraintViolationException(ConstraintViolationException exc) {
        log.info("Invalid request", exc.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ServiceError("Invalid request: " + exc.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ServiceError handleRuntimeException(RuntimeException exc) {
        log.info("Error during processing request", exc);
        return new ServiceError("Internal server error");
    }
}