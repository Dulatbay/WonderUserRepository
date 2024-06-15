package kz.wonder.wonderuserrepository.services;

import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.kaspi.client.model.response.Order.OrderEntry;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderParseService {
    @Transactional(rollbackFor = Exception.class)
    void processKaspiOrder(KaspiToken token, OrdersDataResponse.OrdersDataItem order, List<OrderEntry> products);
}
