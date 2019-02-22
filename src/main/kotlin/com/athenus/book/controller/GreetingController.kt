package com.athenus.book.controller

import io.swagger.annotations.ApiOperation
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
class GreetingController {

    @ApiOperation("Greeting")
    @GetMapping(path = ["/"])
    fun greet(): String {

        return "Book REST service. Api documentation can be accessed from: /swagger-ui.html."
    }
}