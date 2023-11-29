package kz.zsanzharko.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import kz.zsanzharko.enums.AnswerType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnswerQuestionModel {
    private Integer questionId;
    private Long profileId;
    private String answer;
    private AnswerType answerType;
}
