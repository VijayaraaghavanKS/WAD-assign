package com.ssn.employeecrud.controller;

import com.ssn.employeecrud.model.Employee;
import com.ssn.employeecrud.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// REST flow: Postman request -> this Controller -> EmployeeService -> EmployeeRepository -> MongoDB.
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    // CREATE: POST /api/employees with JSON body
    @PostMapping
    public Employee createEmployee(@Valid @RequestBody Employee employee) {
        return service.create(employee);
    }

    // READ ALL: GET /api/employees
    @GetMapping
    public List<Employee> getAllEmployees() {
        return service.getAll();
    }

    // READ ONE: GET /api/employees/{id}
    @GetMapping("/{id}")
    public Employee getEmployee(@PathVariable String id) {
        return service.getById(id);
    }

    // UPDATE: PUT /api/employees/{id} with JSON body of updated fields
    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable String id, @Valid @RequestBody Employee employee) {
        return service.update(id, employee);
    }

    // DELETE: DELETE /api/employees/{id}
    @DeleteMapping("/{id}")
    public String deleteEmployee(@PathVariable String id) {
        service.delete(id);
        return "Employee deleted";
    }
}
