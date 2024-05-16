package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.BoxTypeCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.BoxTypeResponse;
import kz.wonder.wonderuserrepository.entities.BoxType;
import kz.wonder.wonderuserrepository.entities.BoxTypeImages;
import kz.wonder.wonderuserrepository.entities.KaspiStoreAvailableBoxTypes;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.BoxTypeMapper;
import kz.wonder.wonderuserrepository.repositories.BoxTypeRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.services.BoxTypeService;
import kz.wonder.wonderuserrepository.services.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class BoxTypeServiceImpl implements BoxTypeService {

    private final BoxTypeRepository boxTypeRepository;
    private final KaspiStoreRepository storeRepository;
    private final FileService fileService;
    private final BoxTypeMapper boxTypeMapper;


    @Override
    public void createBoxType(BoxTypeCreateRequest boxTypeCreateRequest) {
        var boxType = new BoxType();
        var images = new ArrayList<BoxTypeImages>();
        boxType.setName(boxTypeCreateRequest.getName());
        boxType.setDescription(boxTypeCreateRequest.getDescription());


        if (boxTypeCreateRequest.getImages() != null)
            boxTypeCreateRequest.getImages()
                    .forEach(i -> {
                        images.add(boxTypeMapper.toImage(boxType, fileService.save(i)));
                    });

        boxType.setImages(images);

        boxTypeRepository.save(boxType);
    }

    @Override
    public List<BoxTypeResponse> getAll(Long storeId) {
        if (storeId != null) {
            final var store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), ""));

            return store
                    .getAvailableBoxTypes()
                    .stream()
                    .map(KaspiStoreAvailableBoxTypes::getBoxType)
                    .map(boxTypeMapper::toResponse)
                    .toList();
        }


        return boxTypeRepository.findAll()
                .stream()
                .map(boxTypeMapper::toResponse).toList();
    }

    @Override
    public void deleteById(Long id) {
        var boxTypeToDelete = boxTypeRepository.findById(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Box type doesn't exist"));

        boxTypeToDelete.getImages()
                .forEach(i -> fileService.deleteByName(i.imageUrl));

        boxTypeRepository.delete(boxTypeToDelete);
        log.info("Box Type with ID {} was deleted", id);
    }
}
