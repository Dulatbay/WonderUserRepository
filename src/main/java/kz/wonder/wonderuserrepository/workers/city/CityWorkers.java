package kz.wonder.wonderuserrepository.workers.city;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import kz.wonder.wonderuserrepository.mappers.CityMapper;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CityWorkers {
    private final KaspiCityRepository kaspiCityRepository;
    private final CityMapper cityMapper;
    private final MessageSource messageSource;

    @JobWorker(type = "getCityById")
    public Map<String, Object> getCityById(@Variable("cityId") Long cityId) {
        log.info("Getting city by id: {}", cityId);
        Map<String, Object> result = new HashMap<>();

        var kaspiCity = kaspiCityRepository.findById(cityId)
                .orElseThrow(() -> new ZeebeBpmnError("400", "city-does-not-exist-error"));

        result.put("cityDto", cityMapper.toResponse(kaspiCity));

        return result;
    }
}
