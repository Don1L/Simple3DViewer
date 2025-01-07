package com.cgvsu.math;

public class Vector2f {
    public float x, y;

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // Сложение векторов
    public Vector2f add(Vector2f other) {
        return new Vector2f(this.x + other.x, this.y + other.y);
    }

    // Вычитание векторов
    public Vector2f subtract(Vector2f other) {
        return new Vector2f(this.x - other.x, this.y - other.y);
    }

    // Умножение на скаляр
    public Vector2f multiply(float scalar) {
        return new Vector2f(this.x * scalar, this.y * scalar);
    }

    // Деление на скаляр
    public Vector2f divide(float scalar) {
        if (scalar == 0) throw new IllegalArgumentException("Division by zero");
        return new Vector2f(this.x / scalar, this.y / scalar);
    }

    // Длина вектора
    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    // Нормализация вектора
    public Vector2f normalize() {
        float length = length();
        if (length == 0) throw new IllegalStateException("Cannot normalize a zero-length vector");
        return new Vector2f(x / length, y / length);
    }

    // Скалярное произведение
    public float dot(Vector2f other) {
        return x * other.x + y * other.y;
    }

    // Расстояние между двумя векторами
    public float distance(Vector2f other) {
        float dx = x - other.x;
        float dy = y - other.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}