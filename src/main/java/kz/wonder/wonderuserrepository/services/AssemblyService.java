package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.params.AssemblySearchParameters;
import kz.wonder.wonderuserrepository.dto.response.EmployeeAssemblyResponse;
import org.springframework.data.domain.Page;

public interface AssemblyService {
    Page<EmployeeAssemblyResponse> findAssembliesByParams(String keycloakId, AssemblySearchParameters assemblySearchParameters);
}
