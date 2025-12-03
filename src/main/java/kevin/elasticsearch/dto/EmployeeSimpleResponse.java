package kevin.elasticsearch.dto;

import kevin.elasticsearch.domain.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSimpleResponse {
    private Long id;
    private String name;
    private String email;
    private String position;

    public static EmployeeSimpleResponse from(Employee employee) {
        return new EmployeeSimpleResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getPosition()
        );
    }
}
