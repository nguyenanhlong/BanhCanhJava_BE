package com.example.banhcanh;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class BanhcanhApplicationTests {

	@Test
	void contextLoads() {
		// Test Spring context loads successfully with H2 in-memory database
		assertNotNull(this, "Application context should load");
	}

}
