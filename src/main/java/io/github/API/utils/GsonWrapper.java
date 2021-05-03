package io.github.API.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class GsonWrapper {
    private static final Gson gson = new Gson();

    private GsonWrapper() {
        // empty
    }

    public static <T> String toJson(T modelClassObject) {

        Type type = new TypeToken<T>() {}.getType();

        try {
            return gson.toJson(modelClassObject, type);
        } catch (Exception e) {
            System.out.println("toJson(T modelClassObject) Method -> " + e);
        }

        return null;
    }

    public static <T> String toJson(List<T> listOfModelClassObjects) {

        Type type = new TypeToken<List<T>>() {}.getType();

        try {
            return gson.toJson(listOfModelClassObjects, type);
        } catch (Exception e) {
            System.out.println("toJson(List<T> listOfModelClassObjects) Method -> " + e);
        }

        return null;
    }

    public static <T1, T2> String toJson(Map<T1, T2> keyValuePairsOfModelClassObjects) {

        Type type = new TypeToken<Map<T1, T2>>() {}.getType();

        try {
            return gson.toJson(keyValuePairsOfModelClassObjects, type);
        } catch (Exception e) {
            System.out.println("toJson(Map<T1, T2> keyValuePairsOfModelClassObjects) Method -> " + e);
        }

        return null;
    }

    public static <T> String toJson(T[] arrayOfModelClassObjects) {

        Type type = new TypeToken<T[]>() {}.getType();

        try {
            return gson.toJson(arrayOfModelClassObjects, type);
        } catch (Exception e) {
            System.out.println("toJson(T[] arrayOfModelClassObjects) Method -> " + e);
        }

        return null;
    }

    public static <T> T fromJson(String json, Class<T> modelClass) {

        try {
            return gson.fromJson(json, modelClass);
        } catch (Exception e) {
            System.out.println("fromJson(String json, Class<T> modelClass) Method -> " + e);
        }

        return null;
    }

    public static JsonObject fromJson(String json) {

        try {
            return gson.fromJson(json, JsonObject.class);
        } catch (Exception e) {
            System.out.println("fromJson(String json) Method -> " + e);
        }

        return null;
    }

    public static <T> List<T> fromJson(String json, Type desiredType) {

        try {
            return gson.fromJson(json, desiredType);
        } catch (Exception e) {
            System.out.println("fromJson(String json, Type desiredType) Method -> " + e);
        }

        return null;
    }
}

