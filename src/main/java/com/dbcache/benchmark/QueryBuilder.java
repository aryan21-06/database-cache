package com.dbcache.benchmark;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;

public class QueryBuilder {

    private final Random random = new Random();
    private final List<String[]> temps = new ArrayList<>();

    public QueryBuilder() {
        this.temps.add(QueryTemplates.STUDENT_TEMPLATES);
        this.temps.add(QueryTemplates.COURSE_TEMPLATES);
        this.temps.add(QueryTemplates.ENROLLMENT_TEMPLATES);
        this.temps.add(QueryTemplates.DEPARTMENT_TEMPLATES);
        this.temps.add(QueryTemplates.JOIN_TEMPLATES);
    }

    public List<String> generateUniqueQueries(int count) {
        List<String> queries = new ArrayList<>();
        Set<String> uniqueSet = new HashSet<>();

        while (uniqueSet.size() < count) {
            String query = fillParameters(generateRandomQuery());
            uniqueSet.add(query);
        }

        queries.addAll(uniqueSet);
        return queries;
    }

    private String generateRandomQuery() {
        String[] queryTemplate = temps.get(random.nextInt(temps.size()));
        return queryTemplate[random.nextInt(queryTemplate.length)];
    }

    private String fillParameters(String template) {
        String result = template;
        while (result.contains("?")) {
            Object value = nextParameter(result);
            result = result.replaceFirst("\\?", Matcher.quoteReplacement(String.valueOf(value)));
        }
        return result;
    }

    private Object nextParameter(String template) {
        int q = template.indexOf('?');
        if (q < 0) return "0";

        String before = template.substring(0, q);
        String column = lastColumnBeforeOperator(before);
        return randomValueFor(column, template);
    }

    private String lastColumnBeforeOperator(String before) {
        int i = before.length() - 1;

        while (i >= 0 && Character.isWhitespace(before.charAt(i))) i--;

        while (i >= 0) {
            char ch = before.charAt(i);
            if (ch != '>' && ch != '<' && ch != '=' && ch != '!') break;
            i--;
        }

        while (i >= 0 && Character.isWhitespace(before.charAt(i))) i--;

        int start = i;
        while (start >= 0) {
            char ch = before.charAt(start);
            if (Character.isWhitespace(ch) || ch == ',' || ch == '(' || ch == ')' ||
                ch == '>' || ch == '<' || ch == '=' || ch == '!') break;
            start--;
        }

        if (i < 0 || start < 0) return "";
        String col = before.substring(start + 1, i + 1);

        int dot = col.lastIndexOf('.');
        if (dot >= 0) col = col.substring(dot + 1);

        return col;
    }

    private Object randomValueFor(String column, String template) {
        switch (column) {
            case "id":
                String lower = template.toLowerCase();
                if (lower.contains("from courses")) {
                    return randomInt(ParameterRanges.COURSE_ID_MIN, ParameterRanges.COURSE_ID_MAX);
                }
                if (lower.contains("from departments")) {
                    return randomInt(ParameterRanges.DEPARTMENT_ID_MIN, ParameterRanges.DEPARTMENT_ID_MAX);
                }
                return randomInt(ParameterRanges.STUDENT_ID_MIN, ParameterRanges.STUDENT_ID_MAX);
            case "year":
                return randomInt(ParameterRanges.YEAR_MIN, ParameterRanges.YEAR_MAX);
            case "gpa":
                return randomDouble(ParameterRanges.GPA_MIN, ParameterRanges.GPA_MAX);
            case "credits":
                return randomInt(ParameterRanges.CREDITS_MIN, ParameterRanges.CREDITS_MAX);
            case "student_id":
                return randomInt(ParameterRanges.STUDENT_ID_MIN, ParameterRanges.STUDENT_ID_MAX);
            case "course_id":
                return randomInt(ParameterRanges.COURSE_ID_MIN, ParameterRanges.COURSE_ID_MAX);
            case "department_id":
                return randomInt(ParameterRanges.DEPARTMENT_ID_MIN, ParameterRanges.DEPARTMENT_ID_MAX);
            case "semester":
                return "'" + randomFrom(ParameterRanges.SEMESTERS) + "'";
            case "grade":
                return "'" + randomFrom(ParameterRanges.GRADES) + "'";
            case "name":
                return "'" + randomFrom(ParameterRanges.DEPARTMENT_NAMES) + "'";
            case "head":
                return "'" + randomFrom(ParameterRanges.DEPARTMENT_HEADS) + "'";
            default:
                return "0";
        }
    }

    private int randomInt(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private double randomDouble(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    private <T> T randomFrom(T[] array) {
        return array[random.nextInt(array.length)];
    }
}
