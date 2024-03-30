package kz.wonder.wonderuserrepository.config;

import kz.wonder.wonderuserrepository.services.CityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class CityUpdater {

    private final CityService cityService;

//    @Scheduled(fixedRate = 3600000 * 24) // 24 hours
//    public void updateCitiesFromKaspiApi() {
//       cityService.syncWithKaspi();
//    }
}