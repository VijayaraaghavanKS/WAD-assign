package com.ssn.employeecrud.service;

import com.ssn.employeecrud.model.Employee;
import com.ssn.employeecrud.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Sits between the controller and the repository. Kept thin here since the
// exercise only needs plain CRUD, but this is where business logic would go.
@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public Employee create(Employee employee) {
        return repository.save(employee);
    }

    public List<Employee> getAll() {
        return repository.findAll();
    }

    public Employee getById(String id) {
        return repository.findById(id).orElse(null);
    }

    public Employee update(String id, Employee updated) {
        return repository.findById(id).map(employee -> {
            employee.setName(updated.getName());
            employee.setDepartment(updated.getDepartment());
            employee.setSalary(updated.getSalary());
            return repository.save(employee);
        }).orElse(null);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }
}
