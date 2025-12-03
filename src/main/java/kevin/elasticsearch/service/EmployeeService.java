package kevin.elasticsearch.service;

import kevin.elasticsearch.domain.Company;
import kevin.elasticsearch.domain.Employee;
import kevin.elasticsearch.dto.EmployeeRequest;
import kevin.elasticsearch.dto.EmployeeResponse;
import kevin.elasticsearch.repository.CompanyRepository;
import kevin.elasticsearch.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Employee employee = new Employee(request.getName(), request.getEmail(), request.getPosition());
        
        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + request.getCompanyId()));
            company.addEmployee(employee);
        }
        
        Employee savedEmployee = employeeRepository.save(employee);
        return EmployeeResponse.from(savedEmployee);
    }

    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));
        return EmployeeResponse.from(employee);
    }

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(EmployeeResponse::from)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> getEmployeesByCompany(Long companyId) {
        return employeeRepository.findByCompanyId(companyId).stream()
                .map(EmployeeResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));
        
        employee.updateInfo(request.getName(), request.getEmail(), request.getPosition());
        
        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + request.getCompanyId()));
            
            if (employee.getCompany() != null) {
                employee.getCompany().removeEmployee(employee);
            }
            company.addEmployee(employee);
        }
        
        return EmployeeResponse.from(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));
        
        if (employee.getCompany() != null) {
            employee.getCompany().removeEmployee(employee);
        }
        
        employeeRepository.delete(employee);
    }
}
