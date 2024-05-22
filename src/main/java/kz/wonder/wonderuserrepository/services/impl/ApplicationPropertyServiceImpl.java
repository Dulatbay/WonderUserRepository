package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.entities.ApplicationMode;
import kz.wonder.wonderuserrepository.entities.ApplicationProperty;
import kz.wonder.wonderuserrepository.repositories.ApplicationPropertyRepository;
import kz.wonder.wonderuserrepository.services.ApplicationPropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationPropertyServiceImpl implements ApplicationPropertyService {
    private final ApplicationPropertyRepository applicationPropertyRepository;

    @Value("${application.mode}")
    private ApplicationMode appMode;

    @Override
    public ApplicationProperty getApplicationPropertyByName(String name) {
        var applicationPropertyOptional = applicationPropertyRepository.findByPropertyNameAndApplicationMode(name, appMode);

        if (applicationPropertyOptional.isEmpty()) {
            ApplicationProperty applicationProperty = new ApplicationProperty();
            applicationProperty.setPropertyName(name);
            applicationProperty.setValue("false");
            applicationProperty.setApplicationMode(appMode);
            return applicationPropertyRepository.save(applicationProperty);
        }

        return applicationPropertyOptional.get();
    }
}
