package kz.zsanzharko.service.question;

import lombok.extern.slf4j.Slf4j;
import kz.zsanzharko.enums.AnswerType;
import kz.zsanzharko.exception.IncorrectAnswerForQuestionException;
import kz.zsanzharko.model.AnswerQuestionModel;
import kz.zsanzharko.model.QuestionModel;
import kz.zsanzharko.utils.DatabaseConnector;
import kz.zsanzharko.utils.RegexMatcher;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class DatabaseQuestionServiceImpl implements QuestionService {
    private final DatabaseConnector connector;

    public DatabaseQuestionServiceImpl() {
        this.connector = DatabaseConnector.getInstance();
    }

    @Override
    public QuestionModel getQuestion(Long chatId) {
        final String sql = """
                SELECT q.q_id, q.q_title, q.q_answer_type
                FROM mark_checker.questions q
                         LEFT JOIN mark_checker.daily_question_checks dqc
                                   ON q.q_id = dqc.q_id AND
                                      dqc.chat_id = ? AND
                                      dqc.send_date = CURRENT_DATE
                WHERE q.q_is_active = true
                  AND (dqc.chat_id IS NULL OR dqc.answer = '')
                ORDER BY q_id
                limit 1
                """;
        try (PreparedStatement statement = connector.getConnection().prepareStatement(sql)) {
            statement.setLong(1, chatId);
            log.debug("\n{}", statement);
            ResultSet result = statement.executeQuery();
            return result.next() ? QuestionModel.builder()
                    .id(result.getInt("q_id"))
                    .title(result.getString("q_title"))
                    .answerType(AnswerType.valueOf(result.getString("q_answer_type")))
                    .build() : null;
        } catch (SQLException e) {
            //fixme add exception
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isFinished(Long chatId) {
        final String sql = """
                SELECT
                    CASE
                        WHEN COUNT(dqc.dqc_id) = COUNT(q.q_id) THEN true
                        ELSE false
                        END AS questions_finished
                FROM mark_checker.questions q
                        LEFT JOIN
                    (SELECT q_id as dqc_id
                     FROM mark_checker.daily_question_checks
                     WHERE chat_id = ?
                       AND send_date = CURRENT_DATE) dqc
                    ON q.q_id = dqc.dqc_id
                WHERE q.q_is_active IS TRUE
                """;
        try (PreparedStatement statement = connector.getConnection().prepareStatement(sql)) {
            statement.setLong(1, chatId);
            log.debug("\n{}", statement);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getBoolean("questions_finished");
            }
        } catch (SQLException e) {
            //fixme add exception
            throw new RuntimeException(e);
        }
        //fixme throw exception
        return false;
    }


    @Override
    public void setAnswer(AnswerQuestionModel answer) throws IncorrectAnswerForQuestionException {
        validateAnswer(answer.getAnswer(), answer.getAnswerType());
        final String sql = """
                INSERT INTO mark_checker.daily_question_checks (chat_id, q_id, answer)
                SELECT ?, ?, ?
                WHERE NOT EXISTS (SELECT chat_id
                                  FROM mark_checker.daily_question_checks
                                  WHERE chat_id = ?
                                    AND send_date = CURRENT_DATE
                                    AND q_id = ?);
                """;
        try (PreparedStatement statement = connector.getConnection().prepareStatement(sql)) {
            statement.setLong(1, answer.getProfileId());
            statement.setInt(2, answer.getQuestionId());
            statement.setString(3, answer.getAnswer());
            statement.setLong(4, answer.getProfileId());
            statement.setInt(5, answer.getQuestionId());
            log.debug("\n{}", statement);
            statement.executeUpdate();
        } catch (SQLException e) {
            //fixme add exception
            throw new RuntimeException(e);
        }
    }

    private void validateAnswer(String answer, AnswerType answerType) throws IncorrectAnswerForQuestionException {
        String messageClient;
        if (answer == null || answer.isBlank()) {
            messageClient = "Извините, но мы обнаружали что ответ пуст. Прошу снова ответить на" +
                    " данный вопрос корректно, либо напишите @sanzharrko";
            throw new IncorrectAnswerForQuestionException("Answer is null or empty.",
                    messageClient);
        }

        switch (answerType) {
            case NUMBER -> {
                try {
                    Double.parseDouble(answer);
                } catch (NumberFormatException e) {
                    messageClient = String.format(
                            "Не допустимое значение \"%s\". Прошу отправить ответ в виде числа",
                            answer);
                    throw new IncorrectAnswerForQuestionException(e.getMessage(), messageClient);
                }
            }
            case NUM_REGEX -> {
                if (!RegexMatcher.match(answer)) {
                    messageClient = "Данный формат, который вы отправили, не верный. " +
                            "Просим отправить как показано на примере:\n017+100";
                    throw new IncorrectAnswerForQuestionException(
                            "Incorrect input in NUM_REGEX",
                            messageClient);
                }
            }
        }
    }
}
