package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.entities.KaspiProductCategory;

public interface KaspiProductCategoryService {

    KaspiProductCategory findOrCreate(String categoryCode, String categoryName);

}
