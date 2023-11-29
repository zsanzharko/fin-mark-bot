package kz.zsanzharko.service.question;

import kz.zsanzharko.model.AnswerQuestionModel;
import kz.zsanzharko.model.QuestionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BasicQuestionServiceImpl
 */
public class BasicQuestionServiceImpl implements QuestionService {

    private List<QuestionModel> questionModels;
    private Map<Long, Integer> profilePerQuestionId;

    public BasicQuestionServiceImpl() {
        this.questionModels = initData();
    }

    private List<QuestionModel> initData() {
        List<QuestionModel> questionModels = new ArrayList<>();
        questionModels.add(new QuestionModel(1, "How old are you?"));
        questionModels.add(new QuestionModel(2, "What is your name?"));
        questionModels.add(new QuestionModel(3, "What is your last name?"));
        return questionModels;
    }

    @Override
    public QuestionModel getQuestion(Long chatId) {
        return null;
    }

    @Override
    public boolean isFinished(Long chatId) {
        return false;
    }

    @Override
    public void setAnswer(AnswerQuestionModel answer) {

    }
}