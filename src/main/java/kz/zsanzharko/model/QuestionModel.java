package kz.zsanzharko.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import kz.zsanzharko.enums.AnswerType;

/**
 * QuestionModel
 */
@Data
@Builder
@AllArgsConstructor
public class QuestionModel {
    private Integer id;
    private String title;
    private AnswerType answerType;

    public QuestionModel(Integer id, String title) {
        this.id = id;
        this.title = title;
    }
}