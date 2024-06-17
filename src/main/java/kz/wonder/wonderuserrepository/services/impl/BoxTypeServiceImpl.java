package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.filemanager.client.api.FileManagerApi;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.FILE_MANAGER_IMAGE_DIR;


@Service
@RequiredArgsConstructor
@Slf4j
public class BoxTypeServiceImpl implements BoxTypeService {

    private final BoxTypeRepository boxTypeRepository;
    private final KaspiStoreRepository storeRepository;
    private final BoxTypeMapper boxTypeMapper;
    private final FileManagerApi fileManagerApi;
    private final MessageSource messageSource;

    @Override
    public void createBoxType(BoxTypeCreateRequest boxTypeCreateRequest) {
        var images = new ArrayList<BoxTypeImages>();

        var boxType = new BoxType();
        boxType.setName(boxTypeCreateRequest.getName());
        boxType.setDescription(boxTypeCreateRequest.getDescription());
        boxType.setDeleted(false);

        if (boxTypeCreateRequest.getImages() != null) {
            boxTypeCreateRequest.getImages()
                    .forEach(image -> {
                        var fileUrl = fileManagerApi.uploadFiles(FILE_MANAGER_IMAGE_DIR, List.of(image), true).getBody().get(0);
                        images.add(boxTypeMapper.toImage(boxType, fileUrl));
                    });
        }

        boxType.setImages(images);

        boxTypeRepository.save(boxType);
    }

    @Override
    public List<BoxTypeResponse> getAllByStore(Long storeId) {
        final var store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), messageSource.getMessage("services-impl.box-type-service-impl.warehouse-does-not-exist", null, LocaleContextHolder.getLocale())));

        return store
                .getAvailableBoxTypes()
                .stream()
                .map(KaspiStoreAvailableBoxTypes::getBoxType)
                .filter(boxType -> !boxType.isDeleted())
                .map(boxTypeMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        var boxTypeToDelete = boxTypeRepository.findById(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), messageSource.getMessage("services-impl.box-type-service-impl.box-type-does-not-exist", null, LocaleContextHolder.getLocale())));

        boxTypeToDelete.getImages()
                .forEach(image -> fileManagerApi.deleteFile(FILE_MANAGER_IMAGE_DIR, image.imageUrl));

        boxTypeToDelete.setDeleted(true);

        boxTypeRepository.save(boxTypeToDelete);
        log.info("Box Type with ID {} was deleted", id);
    }

    @Override
    public List<BoxTypeResponse> getAll() {
        return boxTypeRepository.findAllByDeletedIsFalse()
                .stream()
                .filter(boxType -> !boxType.isDeleted())
                .map(boxTypeMapper::toResponse)
                .toList();
    }
}
