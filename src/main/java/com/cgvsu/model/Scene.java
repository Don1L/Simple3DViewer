package com.cgvsu.model;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    private List<Model> models = new ArrayList<>();
    private Model activeModel = null;

    public void addModel(Model model) {
        models.add(model);
        if (activeModel == null) {
            activeModel = model;
        }
    }

    public List<Model> getModels() {
        return models;
    }

    public Model getActiveModel() {
        return activeModel;
    }

    public void setActiveModel(Model model) {
        if (models.contains(model)) {
            activeModel = model;
        }
    }
}