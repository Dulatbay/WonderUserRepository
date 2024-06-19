package kz.wonder.wonderuserrepository.workers.barcode;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BarcodeWorkers {

    @JobWorker(type = "generateBarcodes")
    public Map<String, Object> generateBarcode(@VariablesAsType Map<String, Object> variables) {
        Map<String, Object> result = new HashMap<>();
        log.info("Generating barcode...");

        return result;
    }

    @JobWorker(type = "generateReport")
    public Map<String, Object> generateReport(@VariablesAsType Map<String, Object> variables) {
        Map<String, Object> result = new HashMap<>();
        log.info("Generating report...");


        return result;
    }
}
