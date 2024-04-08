package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.BoxTypeCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.BoxTypeResponse;
import kz.wonder.wonderuserrepository.entities.BoxType;
import kz.wonder.wonderuserrepository.entities.BoxTypeImages;
import kz.wonder.wonderuserrepository.entities.KaspiStoreAvailableBoxTypes;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.BoxTypeRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.services.BoxTypeService;
import kz.wonder.wonderuserrepository.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BoxTypeServiceImpl implements BoxTypeService {

	private final BoxTypeRepository boxTypeRepository;
	private final KaspiStoreRepository storeRepository;
	private final FileService fileService;

	@Override
	public void createBoxType(BoxTypeCreateRequest boxTypeCreateRequest) {
		var boxType = new BoxType();
		var images = new ArrayList<BoxTypeImages>();
		boxType.setName(boxTypeCreateRequest.getName());
		boxType.setDescription(boxTypeCreateRequest.getDescription());


		if (boxTypeCreateRequest.getImages() != null)
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
	public List<BoxTypeResponse> getAll(Long storeId) {
		if (storeId != null) {
			final var store = storeRepository.findById(storeId)
					.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), ""));

			return store
					.getAvailableBoxTypes()
					.stream()
					.map(KaspiStoreAvailableBoxTypes::getBoxType)
					.map(BoxTypeServiceImpl::getBoxTypeResponse)
					.toList();
		}


		return boxTypeRepository.findAll()
				.stream()
				.map(BoxTypeServiceImpl::getBoxTypeResponse).toList();
	}

	private static BoxTypeResponse getBoxTypeResponse(BoxType boxType) {
		return BoxTypeResponse.builder()
				.id(boxType.getId())
				.name(boxType.getName())
				.description(boxType.getDescription())
				.imageUrls(boxType.getImages().stream().map(j -> j.imageUrl).collect(Collectors.toList()))
				.build();
	}

	@Override
	public void deleteById(Long id) {
		var boxTypeToDelete = boxTypeRepository.findById(id)
				.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Box type doesn't exist"));

		boxTypeToDelete.getImages()
				.forEach(i -> fileService.deleteByName(i.imageUrl));

		boxTypeRepository.delete(boxTypeToDelete);
	}
}
