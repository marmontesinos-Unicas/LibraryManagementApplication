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
            for (int i = 0; i < fieldExtractors.size(); i++) {
                String fieldValue = fieldExtractors.get(i).apply(item);

                if (matchesSearch(fieldValue, term)) {
                    buckets.get(i).add(item);
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
     * Checks if text matches the search term (supports multi-word searches)
     */
    private boolean matchesSearch(String text, String searchTerm) {
        if (text == null || searchTerm == null) {
            return false;
        }

        String normalizedText = text.toLowerCase();
        String[] searchWords = searchTerm.split("\\s+");

        // All search words must have a matching word in the text
        for (String searchWord : searchWords) {
            if (!containsWordStartingWith(normalizedText, searchWord)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if text contains a word starting with the search term
     */
    private boolean containsWordStartingWith(String text, String searchTerm) {
        if (text == null || searchTerm == null) {
            return false;
        }

        String[] words = text.split("[\\s,.-]+");
        for (String word : words) {
            if (word.startsWith(searchTerm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builder for creating prioritized search field configurations
     */
    public static class SearchFieldsBuilder<T> {
        private final List<Function<T, String>> fieldExtractors = new ArrayList<>();

        /**
         * Adds a field to search with the given priority (order matters)
         * @param extractor Function to extract the field value from the object
         * @return this builder for chaining
         */
        public SearchFieldsBuilder<T> addField(Function<T, String> extractor) {
            fieldExtractors.add(extractor);
            return this;
        }

        /**
         * Builds the list of field extractors
         * @return Immutable list of field extractors in priority order
         */
        public List<Function<T, String>> build() {
            return Collections.unmodifiableList(new ArrayList<>(fieldExtractors));
        }
    }

    /**
     * Creates a new builder for search fields
     */
    public static <T> SearchFieldsBuilder<T> fieldsBuilder() {
        return new SearchFieldsBuilder<>();
    }
}