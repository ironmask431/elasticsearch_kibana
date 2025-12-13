package kevin.elasticsearch.service;

import kevin.elasticsearch.domain.Company;
import kevin.elasticsearch.dto.CompanyRequest;
import kevin.elasticsearch.dto.CompanyResponse;
import kevin.elasticsearch.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        log.info("createCompany - name: {}, address: {}", request.getName(), request.getAddress());
        Company company = new Company(request.getName(), request.getAddress());
        Company savedCompany = companyRepository.save(company);
        return CompanyResponse.fromWithoutEmployees(savedCompany);
    }

    public CompanyResponse getCompany(Long id) {
        log.info("getCompany - id: {}", id);
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));
        return CompanyResponse.from(company);
    }

    public List<CompanyResponse> getAllCompanies() {
        log.info("getAllCompanies");
        return companyRepository.findAll().stream()
                .map(CompanyResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest request) {
        log.info("updateCompany - id: {}, name: {}, address: {}", id, request.getName(), request.getAddress());
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));
        
        company.updateInfo(request.getName(), request.getAddress());
        return CompanyResponse.fromWithoutEmployees(company);
    }

    @Transactional
    public void deleteCompany(Long id) {
        log.info("deleteCompany - id: {}", id);
        if (!companyRepository.existsById(id)) {
            throw new IllegalArgumentException("Company not found with id: " + id);
        }
        companyRepository.deleteById(id);
    }
}
