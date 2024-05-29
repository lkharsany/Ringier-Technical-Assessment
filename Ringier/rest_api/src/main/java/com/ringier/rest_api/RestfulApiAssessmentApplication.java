package com.ringier.rest_api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class RestfulApiAssessmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestfulApiAssessmentApplication.class, args);
	}

	@RestController
	@RequestMapping("/api")
	public static class ApiController {
		private static final String JSON_FILE = "tech_assess.json";
		private static final ObjectMapper objectMapper = new ObjectMapper();

		@GetMapping("/system_info")
		public ResponseEntity<Map<String, Object>> getSystemInfo() {
			SystemInfo systemInfo = new SystemInfo();
			HardwareAbstractionLayer hal = systemInfo.getHardware();
			OperatingSystem os = systemInfo.getOperatingSystem();

			double[] loadAverage = hal.getProcessor().getSystemLoadAverage(3);

			File root = new File("/");
			long freeDiskSpace = root.getUsableSpace();

			Map<String, Object> response = new HashMap<>();
			response.put("load_average", loadAverage);
			response.put("free_disk_space", freeDiskSpace);
			return ResponseEntity.ok(response);
		}

		@GetMapping("/return_value")
		public ResponseEntity<Map<String, String>> getReturnValue() {
			try {
				// Read the JSON file
				byte[] jsonData = Files.readAllBytes(Paths.get(JSON_FILE));
				JsonNode rootNode = objectMapper.readTree(jsonData);

				// Assuming `return_value` is nested inside another key. Adjust the path as necessary.
				JsonNode nestedNode = rootNode.path("tech").path("return_value");

				if (nestedNode.isMissingNode()) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(Map.of("error", "return_value not found"));
				}

				String returnValue = nestedNode.asText();
				return ResponseEntity.ok(Map.of("return_value", returnValue));
			} catch (IOException e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(Map.of("error", e.getMessage()));
			}
		}

		@PostMapping("/return_value")
		public ResponseEntity<Map<String, String>> setReturnValue(@RequestBody Map<String, String> payload) {
			String newValue = payload.get("return_value");
			if (newValue == null) {
				return ResponseEntity.badRequest().body(Map.of("error", "No return_value provided"));
			}

			try {
				// Read the JSON file
				Path json_path = Paths.get(JSON_FILE);
				byte[] jsonData = Files.readAllBytes(json_path);
				JsonNode rootNode = objectMapper.readTree(jsonData);

				// Assuming `return_value` is nested inside another key. Adjust the path as necessary.
				JsonNode nestedNode = rootNode.path("tech");
				if (nestedNode.isMissingNode()) {
					// Create the nested structure if it doesn't exist
					nestedNode = ((ObjectNode) rootNode).putObject("tech");
				}

				// Set the new value
				((ObjectNode) nestedNode).put("return_value", newValue);

				// Write the updated JSON back to the file
				Files.write(json_path, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(rootNode), StandardOpenOption.TRUNCATE_EXISTING);

				return ResponseEntity.ok(Map.of("message", "return_value updated successfully"));
			} catch (IOException e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(Map.of("error", e.getMessage()));
			}
		}
	}
}
