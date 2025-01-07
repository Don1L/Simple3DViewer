package com.cgvsu.math;

public class Vector3f {
    public float x, y, z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Сложение векторов
    public Vector3f add(Vector3f other) {
        return new Vector3f(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    // Вычитание векторов
    public Vector3f subtract(Vector3f other) {
        return new Vector3f(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    // Умножение на скаляр
    public Vector3f multiply(float scalar) {
        return new Vector3f(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    // Деление на скаляр
    public Vector3f divide(float scalar) {
        if (scalar == 0) throw new IllegalArgumentException("Division by zero");
        return new Vector3f(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    // Длина вектора
    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    // Нормализация вектора
    public Vector3f normalize() {
        float length = length();
        if (length == 0) throw new IllegalStateException("Cannot normalize a zero-length vector");
        return new Vector3f(x / length, y / length, z / length);
    }

    // Скалярное произведение
    public float dot(Vector3f other) {
        return x * other.x + y * other.y + z * other.z;
    }

    // Векторное произведение
    public Vector3f cross(Vector3f other) {
        return new Vector3f(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }

    // Расстояние между двумя векторами
    public float distance(Vector3f other) {
        float dx = x - other.x;
        float dy = y - other.y;
        float dz = z - other.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    // Сравнение векторов с учётом погрешности
    public boolean equals(Vector3f other) {
        final float eps = 1e-7f;
        return Math.abs(x - other.x) < eps && Math.abs(y - other.y) < eps && Math.abs(z - other.z) < eps;
    }
}