package kz.wonder.wonderuserrepository.dto.request;

import lombok.Data;

public record UserAuthRequest(String email, String password) { }
