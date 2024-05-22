package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.entities.ApplicationProperty;

public interface ApplicationPropertyService {
    ApplicationProperty getApplicationPropertyByName(String name);
}
