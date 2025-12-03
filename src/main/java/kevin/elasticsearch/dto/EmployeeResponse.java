package kevin.elasticsearch.dto;

import kevin.elasticsearch.domain.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String name;
    private String email;
    private String position;
    private Long companyId;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getPosition(),
                employee.getCompany() != null ? employee.getCompany().getId() : null,
                employee.getCompany() != null ? employee.getCompany().getName() : null,
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
}
