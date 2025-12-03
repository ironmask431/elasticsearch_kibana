package kevin.elasticsearch.service;

import kevin.elasticsearch.domain.Company;
import kevin.elasticsearch.dto.CompanyRequest;
import kevin.elasticsearch.dto.CompanyResponse;
import kevin.elasticsearch.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        Company company = new Company(request.getName(), request.getAddress());
        Company savedCompany = companyRepository.save(company);
        return CompanyResponse.fromWithoutEmployees(savedCompany);
    }

    public CompanyResponse getCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));
        return CompanyResponse.from(company);
    }

    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(CompanyResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));
        
        company.updateInfo(request.getName(), request.getAddress());
        return CompanyResponse.fromWithoutEmployees(company);
    }

    @Transactional
    public void deleteCompany(Long id) {
        if (!companyRepository.existsById(id)) {
            throw new IllegalArgumentException("Company not found with id: " + id);
        }
        companyRepository.deleteById(id);
    }
}
