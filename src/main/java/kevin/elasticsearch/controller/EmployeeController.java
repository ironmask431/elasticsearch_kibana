package kevin.elasticsearch.controller;

import kevin.elasticsearch.dto.EmployeeRequest;
import kevin.elasticsearch.dto.EmployeeResponse;
import kevin.elasticsearch.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployee(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(
            @RequestParam(required = false) Long companyId) {
        List<EmployeeResponse> responses;
        if (companyId != null) {
            responses = employeeService.getEmployeesByCompany(companyId);
        } else {
            responses = employeeService.getAllEmployees();
        }
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
