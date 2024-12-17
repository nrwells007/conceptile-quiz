package com.conceptile.quiz.service;

import com.conceptile.quiz.entity.QuestionEntity;

import java.io.IOException;
import java.util.List;

public interface QuizService {

    void upsertQuestionsFromJson() throws IOException;

    List<QuestionEntity> getAllQuestions();

    List<QuestionEntity> getRandomQuestions();
}
