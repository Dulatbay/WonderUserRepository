package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.params.AssemblySearchParameters;
import kz.wonder.wonderuserrepository.dto.request.AssembleProductRequest;
import kz.wonder.wonderuserrepository.dto.response.AssembleProcessResponse;
import kz.wonder.wonderuserrepository.dto.response.EmployeeAssemblyResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public interface AssemblyService {
    Page<EmployeeAssemblyResponse> findAssembliesByParams(String keycloakId, AssemblySearchParameters assemblySearchParameters);

    AssembleProcessResponse startAssemble(JwtAuthenticationToken starterToken, String orderId);

    AssembleProcessResponse assembleProduct(JwtAuthenticationToken starterToken, AssembleProductRequest assembleProductRequest);

    AssembleProcessResponse getAssemble(JwtAuthenticationToken token, String orderCode);

    void finishAssemble(String orderCode, String keycloakId);
}
