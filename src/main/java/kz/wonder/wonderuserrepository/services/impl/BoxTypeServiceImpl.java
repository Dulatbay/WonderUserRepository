package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.BoxTypeCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.BoxTypeResponse;
import kz.wonder.wonderuserrepository.entities.BoxType;
import kz.wonder.wonderuserrepository.entities.BoxTypeImages;
import kz.wonder.wonderuserrepository.repositories.BoxTypeRepository;
import kz.wonder.wonderuserrepository.services.BoxTypeService;
import kz.wonder.wonderuserrepository.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BoxTypeServiceImpl implements BoxTypeService {

    private final BoxTypeRepository boxTypeRepository;
    private final FileService fileService;

    @Override
    public void createBoxType(BoxTypeCreateRequest boxTypeCreateRequest) {
        var boxType = new BoxType();
        var images = new ArrayList<BoxTypeImages>();
        boxType.setName(boxTypeCreateRequest.getName());
        boxType.setDescription(boxTypeCreateRequest.getDescription());

        boxTypeCreateRequest.getImages()
                .forEach(i -> {
                    var boxTypeImages = new BoxTypeImages();
                    boxTypeImages.setBoxType(boxType);
                    boxTypeImages.setImageUrl(fileService.save(i));
                    images.add(boxTypeImages);
                });

        boxType.setImages(images);

        boxTypeRepository.save(boxType);
    }

    @Override
    public List<BoxTypeResponse> getAll() {
        return boxTypeRepository.findAll().stream().map(
                i -> BoxTypeResponse.builder()
                        .name(i.getName())
                        .description(i.getDescription())
                        .imageUrls(i.getImages().stream().map(j -> j.imageUrl).collect(Collectors.toList()))
                        .build()
        ).toList();
    }
}
