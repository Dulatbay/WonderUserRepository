package kz.wonder.wonderuserrepository.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record BoxTypeResponse(Long id, String name, String description, List<String> imageUrls) {
}
