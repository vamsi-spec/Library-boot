package com.acme.library.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public record PageQuery(int page, int size, List<Sort> sort) {
    public static final int MAX_SIZE = 100;

    public record Sort(String field, boolean ascending) {}

    public static PageQuery of(int page, int size, List<String> sortParams, Set<String> allowedFields, String defaultSort) {
        if(page < 0) {
            throw new InvalidRequestException("Page must be 0 or greater than 0");
        }
        if(size < 1 || size > MAX_SIZE) {
            throw new InvalidRequestException("size must be 1 and " + MAX_SIZE);
        }

        List<String> raw = (sortParams == null || sortParams.isEmpty()) ? List.of(defaultSort) : sortParams;

        List<Sort> sort = new ArrayList<>();

        for(String param : raw) {
            sort.add(parseSort(param,allowedFields));
        }
        return new PageQuery(page, size, List.copyOf(sort));
    }

    private static Sort parseSort(String param, Set<String> allowedFields) {
        String value = param.strip();
        boolean ascending = !value.startsWith("-");
        String field = ascending ? value : value.substring(1);
        if (!allowedFields.contains(field)) {
            throw new InvalidRequestException("Cannot sort by '" + field
                    + "'. Allowed fields: " + new TreeSet<>(allowedFields));   
        }
        return new Sort(field, ascending);
    }
}
