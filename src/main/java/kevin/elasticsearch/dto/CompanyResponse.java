package kevin.elasticsearch.dto;

import kevin.elasticsearch.domain.Company;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    private Long id;
    private String name;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<EmployeeSimpleResponse> employees;

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getAddress(),
                company.getCreatedAt(),
                company.getUpdatedAt(),
                company.getEmployees().stream()
                        .map(EmployeeSimpleResponse::from)
                        .collect(Collectors.toList())
        );
    }

    public static CompanyResponse fromWithoutEmployees(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getAddress(),
                company.getCreatedAt(),
                company.getUpdatedAt(),
                null
        );
    }
}
