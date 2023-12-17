package kz.zsanzharko.service;

import kz.zsanzharko.enums.CategoryListType;
import kz.zsanzharko.model.CategoryModel;
import kz.zsanzharko.model.Profile;

import java.sql.SQLException;
import java.util.List;

public interface CategoryService {

    List<CategoryModel> getCategory(Profile profile, CategoryListType type, Long id) throws SQLException;
}
