package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.AddressDtos.DepartmentResponse;
import edu.unimagdalena.universitystore.service.ColombiaLocations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    // Departments and their cities, for the drop-down menus of the address form
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> findAll() {
        var result = ColombiaLocations.citiesByDepartment().entrySet().stream()
                .map(entry -> new DepartmentResponse(entry.getKey(), entry.getValue()))
                .toList();

        return ResponseEntity.ok(result);
    }
}
