package kz.zsanzharko.service.question;

import kz.zsanzharko.exception.IncorrectAnswerForQuestionException;
import kz.zsanzharko.model.AnswerQuestionModel;
import kz.zsanzharko.model.QuestionModel;

public interface QuestionService {

    QuestionModel getQuestion(Long chatId);

    boolean isFinished(Long chatId);

    void setAnswer(AnswerQuestionModel answer) throws IncorrectAnswerForQuestionException;
}
