package kz.zsanzharko.service.reporter;

import kz.zsanzharko.model.QA;
import kz.zsanzharko.model.QuestionModel;
import kz.zsanzharko.model.Report;
import kz.zsanzharko.utils.DatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DatabaseReportServiceImpl implements ReportService {
    private final DatabaseConnector connector;

    public DatabaseReportServiceImpl() {
        this.connector = DatabaseConnector.getInstance();
    }

    @Override
    public Report generateReport() {
        var report = new Report();
        setQuestions(report);
        addRegions(report);
        addQa(report);
        return report;
    }

    private void addRegions(Report report) {
        final String sql = """
                select p.full_name, pi.i_result as region
                from mark_checker.profiles p
                         left join mark_checker.profile_info pi on p.chat_id = pi.chat_id
                where
                 (pi.i_title = 'Region' or p.full_name is not null) and
                 p.profile_role = 'USER'
                """;
        try (var statement = connector.getConnection().createStatement()) {
            var result = statement.executeQuery(sql);
            while (result.next()) {
                String fullName = result.getString("full_name");
                String region = result.getString("region") == null ?
                        "Не заполнено" :
                        result.getString("region");
                report.addAdditionReport(fullName, "Регион", region);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setQuestions(Report report) {
        final String sql = """
                select q_id, q_title
                from mark_checker.questions
                where q_is_active is true
                """;
        try (var statement = connector.getConnection().createStatement()) {
            var result = statement.executeQuery(sql);
            List<QuestionModel> questionModelList = new ArrayList<>(15);
            while (result.next()) {
                var id = result.getInt("q_id");
                var title = result.getString("q_title");
                questionModelList.add(new QuestionModel(id, title));
            }
            report.setQuestionModelList(questionModelList);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addQa(Report report) {
        final String sql = """
                select p.full_name, q.q_id, dqc.send_date, dqc.answer
                from mark_checker.daily_question_checks dqc
                left join mark_checker.profiles p on p.chat_id = dqc.chat_id
                join mark_checker.questions q on q.q_id = dqc.q_id
                where p.profile_role = 'USER'
                ORDER BY send_date
                """;
        try (var statement = connector.getConnection().createStatement()) {
            var result = statement.executeQuery(sql);
            while (result.next()) {
                var fullName = result.getString("full_name");
                var qId = result.getInt("q_id");
                var date = result.getDate("send_date");
                var answer = result.getDouble("answer");
                report.addReportByDate(date, fullName, new QA(qId, answer));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
