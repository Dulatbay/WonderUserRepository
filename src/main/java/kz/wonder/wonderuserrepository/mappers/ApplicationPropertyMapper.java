package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.wonderuserrepository.entities.enums.ApplicationMode;
import kz.wonder.wonderuserrepository.entities.ApplicationProperty;
import org.springframework.stereotype.Component;

@Component
public class ApplicationPropertyMapper {
    public ApplicationProperty mapToGetApplicationPropertyByName(String name, ApplicationMode appMode) {
        ApplicationProperty applicationProperty = new ApplicationProperty();
        applicationProperty.setPropertyName(name);
        applicationProperty.setValue("false");
        applicationProperty.setApplicationMode(appMode);
        return applicationProperty;
    }
}
