package com.acme.library.common;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class Paging {
    private Paging() {}

    public static <T> PageResponse<T> paginate(List<T> items, PageQuery query, Map<String, Comparator<T>> sorters) {
        Comparator<T> comparator = null;
        for(PageQuery.Sort sort : query.sort()) {
            Comparator<T> next = sorters.get(sort.field());
            if(!sort.ascending()) {
                next = next.reversed();
            }
            comparator = (comparator == null ) ? next : comparator.thenComparing(next);
        }
        Comparator<T> tieBreaker = sorters.get("id");
        comparator = (comparator == null) ? tieBreaker : comparator.thenComparing(tieBreaker);

        List<T> sorted = items.stream().sorted(comparator).toList();

        long total = sorted.size();
        int totalPages = (int) ((total + query.size() - 1) / query.size());
        long from = Math.min((long) query.page() * query.size(), total);
        long to = Math.min(from + query.size(), total);

        return new PageResponse<>(
                List.copyOf(sorted.subList((int) from, (int) to)),
                query.page(), query.size(), total, totalPages);


    }
}
