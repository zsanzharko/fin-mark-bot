package kz.zsanzharko.service.category;

import kz.zsanzharko.model.CategoryModel;
import kz.zsanzharko.model.Profile;
import kz.zsanzharko.utils.DatabaseConnector;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final DatabaseConnector connector = DatabaseConnector.getInstance();

    @Override
    public List<CategoryModel> getCategory(Profile profile, Long currentId, Long parentId)
            throws SQLException {
        final String rootQuery = """
                SELECT ctg.id, ctg.title, description FROM mark_checker.i_ctg as ctg
                    left join mark_checker.i_lang il on ctg.lang = il.id
                where il.fm_l = ? and ctg.p_id is null;
                """;
        final String query = """
                SELECT ctg.id, ctg.title, description, p_id FROM mark_checker.i_ctg as ctg
                    left join mark_checker.i_lang il on ctg.lang = il.id
                where il.fm_l = ? and ctg.p_id = ?;
                """;
        PreparedStatement statement;
        if (currentId == null && parentId != null) {
            if (isRootCategory(profile, parentId)) {
                parentId = null;
                statement = connector.getConnection().prepareStatement(rootQuery);
                statement.setObject(1, profile.getLang().getLangText());
            } else {
                statement = connector.getConnection().prepareStatement(query);
                statement.setObject(1, profile.getLang().getLangText());
                statement.setObject(2, parentId);
            }
        } else if (currentId != null && parentId == null) {
            // send next subcategory
            statement = connector.getConnection().prepareStatement(query);
            statement.setObject(1, profile.getLang().getLangText());
            statement.setObject(2, currentId);
        } else if (currentId == null && parentId == null) {
            // send root category
            statement = connector.getConnection().prepareStatement(rootQuery);
            statement.setObject(1, profile.getLang().getLangText());
        } else return null;

        log.debug("Query for profile id - {}: {}", profile.getChatId(), statement);
        ResultSet resultSet = statement.executeQuery();
        log.debug("Query for profile id - {} executed", profile.getChatId());
        var categories = getDataFromDatabase(resultSet);
        statement.close();
        return categories;
    }

    private boolean isRootCategory(Profile profile, Long parentId) throws SQLException {
        String query = """
                select *
                from mark_checker.i_ctg ctg
                left join mark_checker.i_lang il on ctg.lang = il.id
                where il.fm_l = ? and ctg.id = ? and ctg.p_id is null
                """;
        PreparedStatement statement = connector.getConnection().prepareStatement(query);
        statement.setObject(1, profile.getLang().getLangText());
        statement.setObject(2, parentId);
        log.debug("Sql generated for {}: {}", profile.getChatId(), statement);
        ResultSet resultSet = statement.executeQuery();
        log.debug("Sql completed for {}: {}", profile.getChatId(), statement);
        return resultSet.next();
    }

    private List<CategoryModel> getDataFromDatabase(ResultSet resultSet) throws SQLException {
        List<CategoryModel> categoryModelList = new ArrayList<>();
        while (resultSet.next()) {
            CategoryModel model = new CategoryModel();
            model.setId(resultSet.getLong("id"));
            model.setTitle(resultSet.getString("title"));
            model.setDescription(resultSet.getString("description"));
            categoryModelList.add(model);
        }
        return categoryModelList;
    }


    private Long getParentId(Long id, Long profileId) throws SQLException {
        final String query = """
                select p_id as parent_id
                from mark_checker.ctg
                where id = ?
                """;
        PreparedStatement statement = connector.getConnection().prepareStatement(query);
        statement.setObject(1, id);
        log.debug("Query for profile id - {}: {}", profileId, statement);
        ResultSet resultSet = statement.executeQuery();
        log.debug("Query for profile id - {} executed", profileId);
        Long result = resultSet.next() ?
                resultSet.getLong("parent_id") :
                null;
        statement.close();
        return result;
    }
}
