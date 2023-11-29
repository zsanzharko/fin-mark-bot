package kz.zsanzharko.service.category;

import kz.zsanzharko.model.CategoryModel;
import kz.zsanzharko.model.Profile;

import java.sql.SQLException;
import java.util.List;

public interface CategoryService {

    List<CategoryModel> getCategory(Profile profile, Long currentId, Long parentId) throws SQLException;
}
