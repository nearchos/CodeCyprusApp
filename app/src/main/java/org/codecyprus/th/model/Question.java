package org.codecyprus.th.model;

import java.io.Serializable;

public class Question implements Serializable {

    private String uuid; // PK
    private String questionText; // can be HTML
    private QuestionType questionType;
    private String correctAnswer; // true/false, or A/B/C/D, or .../-1/0/1/2/..., or x.y num, or text, depending on type
    private String creatorEmail; // email of person who created question
    private Long creationTimestamp; // timestamp of creation event
    private Boolean shared; // whether it can be publicly shared

    public Question(String questionText, QuestionType questionType, String correctAnswer, final String creatorEmail, final long creationTimestamp, boolean shared) {
        this(null, questionText, questionType, correctAnswer, creatorEmail, creationTimestamp, shared);
    }

    public Question(String uuid, String questionText, QuestionType questionType, String correctAnswer, final String creatorEmail, final long creationTimestamp, boolean shared) {
        this.uuid = uuid;
        this.questionText = questionText;
        this.questionType = questionType;
        this.correctAnswer = correctAnswer;
        this.creatorEmail = creatorEmail;
        this.creationTimestamp = creationTimestamp;
        this.shared = shared;
    }

    public String getUuid() {
        return uuid;
    }

    public String getQuestionText() {
        return questionText;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public Long getCreationTimestamp() {
        return creationTimestamp;
    }

    public Boolean isShared() {
        return shared;
    }
}