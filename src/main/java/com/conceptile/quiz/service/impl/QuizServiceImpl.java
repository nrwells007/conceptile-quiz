package com.conceptile.quiz.service.impl;

import com.conceptile.quiz.entity.QuestionEntity;
import com.conceptile.quiz.repository.QuizRepository;
import com.conceptile.quiz.service.QuizService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class QuizServiceImpl implements QuizService {

    @Value("${question.count}")
    private int count;

    private final QuizRepository quizRepository;

    private final ObjectMapper objectMapper;

    private final MongoTemplate mongoTemplate;

    public QuizServiceImpl (QuizRepository quizRepository, ObjectMapper objectMapper, MongoTemplate mongoTemplate) {
        this.quizRepository = quizRepository;
        this.objectMapper = objectMapper;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void upsertQuestionsFromJson() throws IOException {
        // Load JSON file from the classpath
        InputStream inputStream = new ClassPathResource("questionAnswer.json").getInputStream();
        List<QuestionEntity> questions = objectMapper.readValue(inputStream, new TypeReference<List<QuestionEntity>>() {});
        log.info("Loaded questions from JSON: {}", questions);

        // Upsert logic: Check if ID exists, then update or insert
        for (QuestionEntity question : questions) {
            log.info("Processing question with ID: {}", question.getId());
            Optional<QuestionEntity> existingQuestion = quizRepository.findById(question.getId());

            if (existingQuestion.isPresent()) {
                log.info("Updating existing question with ID: {}", question.getId());
                QuestionEntity existing = existingQuestion.get();
                existing.setQuestion(question.getQuestion());
                existing.setOptions(question.getOptions());
                existing.setCorrectAnswer(question.getCorrectAnswer());
                quizRepository.save(existing);
            } else {
                log.info("Inserting new question with ID: {}", question.getId());
                quizRepository.save(question);
            }
        }

        log.info("All questions processed successfully!");
    }

    @Override
    public List<QuestionEntity> getAllQuestions() {
        return quizRepository.findAll();
    }

    @Override
    public List<QuestionEntity> getRandomQuestions() {
        // MongoDB Aggregation to fetch random 'count' documents
        SampleOperation sampleStage = Aggregation.sample(count);
        Aggregation aggregation = Aggregation.newAggregation(sampleStage);

        AggregationResults<QuestionEntity> results = mongoTemplate.aggregate(
                aggregation, "questions", QuestionEntity.class);

        return results.getMappedResults();
    }
}
