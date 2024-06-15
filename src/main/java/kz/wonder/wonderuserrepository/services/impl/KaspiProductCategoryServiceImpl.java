package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.entities.KaspiProductCategory;
import kz.wonder.wonderuserrepository.repositories.KaspiProductCategoryRepository;
import kz.wonder.wonderuserrepository.services.KaspiProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KaspiProductCategoryServiceImpl implements KaspiProductCategoryService {
    private final KaspiProductCategoryRepository kaspiProductCategoryRepository;


    @Override
    public KaspiProductCategory findOrCreate(String categoryCode, String categoryTitle) {
        var categoryOptional = kaspiProductCategoryRepository.findByCode(categoryCode);

        if (categoryOptional.isPresent()) {
            return categoryOptional.get();
        }

        var kaspiProductCategory = new KaspiProductCategory();
        kaspiProductCategory.setCode(categoryCode);
        kaspiProductCategory.setTitle(categoryTitle);

        return kaspiProductCategoryRepository.save(kaspiProductCategory);
    }
}

