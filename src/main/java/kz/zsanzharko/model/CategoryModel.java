package kz.zsanzharko.model;

import lombok.Data;

@Data
public class CategoryModel {
    private Long id;
    private String title;
    private String description;
    private Long idParentCategory;
}
