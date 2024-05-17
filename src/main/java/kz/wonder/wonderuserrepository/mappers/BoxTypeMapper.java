package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.wonderuserrepository.dto.response.BoxTypeResponse;
import kz.wonder.wonderuserrepository.entities.BoxType;
import kz.wonder.wonderuserrepository.entities.BoxTypeImages;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BoxTypeMapper {
    public BoxTypeResponse toResponse(BoxType boxType) {
        return BoxTypeResponse.builder()
                .id(boxType.getId())
                .name(boxType.getName())
                .description(boxType.getDescription())
                .imageUrls(boxType.getImages().stream().map(j -> j.imageUrl).collect(Collectors.toList()))
                .build();
    }

    public BoxTypeImages toImage(BoxType boxType, String imageUrl) {
        var boxTypeImages = new BoxTypeImages();
        boxTypeImages.setBoxType(boxType);
        boxTypeImages.setImageUrl(imageUrl);
        return boxTypeImages;
    }
}
