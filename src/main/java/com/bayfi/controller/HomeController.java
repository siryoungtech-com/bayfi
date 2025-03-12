package com.bayfi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "HomeController", description = "Home Api")
@RestController
public class HomeController {
    @Operation(
            description = "Endpoint for Home",
            summary = "This is the end point for home controller",
            responses = {
                    @ApiResponse(
                    description = "Success",
                    responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "unauthorized",
                            responseCode = "403"
                    )
            })
    @GetMapping
    public ResponseEntity<String> home(){
        return ResponseEntity.ok("Api is Up and Running ");
    }

}
