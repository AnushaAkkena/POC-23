
package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void healthEndpointShouldReturnUP() {
    ResponseEntity<String> response = restTemplate.getForEntity("/health", String.class);
    assertThat(response.getBody()).isEqualTo("UP");
  }
}
