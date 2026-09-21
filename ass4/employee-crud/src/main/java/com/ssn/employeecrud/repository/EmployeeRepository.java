package com.ssn.employeecrud.repository;

import com.ssn.employeecrud.model.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;

// Extending MongoRepository gives save(), findAll(), findById(), deleteById() for free.
public interface EmployeeRepository extends MongoRepository<Employee, String> {
}
