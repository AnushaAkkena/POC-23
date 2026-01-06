
package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  @GetMapping("/")
  public String home() {
    return "Hello from CI/CD pipeline with GitHub, Jenkins, Maven, JUnit, Docker, and AWS!";
  }

  @GetMapping("/health")
  public String health() {
    return "UP";
  }
}
``
