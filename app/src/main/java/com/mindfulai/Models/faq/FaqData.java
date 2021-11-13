package com.mindfulai.Models.faq;

import java.util.ArrayList;

public class FaqData {

    private String name;
    private boolean selected;
    private ArrayList<FaqQuestionAnswers> questions;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<FaqQuestionAnswers> getQuestions() {
        return questions;
    }

    public void setQuestions(ArrayList<FaqQuestionAnswers> questions) {
        this.questions = questions;
    }
}
