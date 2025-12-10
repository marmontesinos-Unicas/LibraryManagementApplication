package it.unicas.project.template.address.service;

import java.util.*;
import java.util.function.Function;

/**
 * Generic search service that supports prioritized multi-field searching
 * @param <T> The type of object being searched
 */
public class SearchService<T> {

    /**
     * Searches and sorts items based on prioritized fields
     *
     * @param items List of items to search
     * @param searchTerm The search term
     * @param fieldExtractors Ordered list of functions to extract searchable text from each item.
     *                       Order determines priority (first = highest priority)
     * @return Sorted list with matches ordered by field priority
     */
    public List<T> searchAndSort(List<T> items, String searchTerm, List<Function<T, String>> fieldExtractors) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new ArrayList<>(items);
        }

        String term = searchTerm.toLowerCase().trim();

        // Create buckets for each priority level
        List<List<T>> buckets = new ArrayList<>();
        for (int i = 0; i < fieldExtractors.size(); i++) {
            buckets.add(new ArrayList<>());
        }

        // Categorize each item into the highest priority bucket that matches
        for (T item : items) {
            boolean matched = false;

            for (int i = 0; i < fieldExtractors.size(); i++) {
                String fieldValue = fieldExtractors.get(i).apply(item);

                if (containsWordStartingWith(fieldValue, term)) {
                    buckets.get(i).add(item);
                    matched = true;
                    break; // Item goes in highest priority match only
                }
            }
        }

        // Combine all buckets in priority order
        List<T> result = new ArrayList<>();
        for (List<T> bucket : buckets) {
            result.addAll(bucket);
        }

        return result;
    }

    /**
     * Checks if text contains a word starting with the search term
     */
    private boolean containsWordStartingWith(String text, String searchTerm) {
        if (text == null || searchTerm == null) {
            return false;
        }

        String[] words = text.toLowerCase().split("[\\s,.-]+");
        for (String word : words) {
            if (word.startsWith(searchTerm)) {
                return true;
            }
        }
        return false;
    }
}
