package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

@Data
public  class StateInfo<T> {
    private T count;
    private Double percent;
}