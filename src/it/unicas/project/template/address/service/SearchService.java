package it.unicas.project.template.address.service;

import java.util.*;
import java.util.function.Function;

/**
 * Generic search service that supports prioritized multi-field searching
 * @param <T> The type of object being searched
 */
public class SearchService<T> {

    /**
     * Searches and sorts items based on prioritized fields.
     * Supports cross-field searching (e.g., "Harry Potter J.K. Rowling")
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
        String[] searchWords = term.split("\\s+");

        // Create buckets for each priority level
        List<List<T>> buckets = new ArrayList<>();
        for (int i = 0; i < fieldExtractors.size(); i++) {
            buckets.add(new ArrayList<>());
        }

        // Track items that didn't match any single field
        List<T> crossFieldCandidates = new ArrayList<>();

        // First pass: try to match all words within a single field
        for (T item : items) {
            boolean foundInSingleField = false;

            for (int i = 0; i < fieldExtractors.size(); i++) {
                String fieldValue = fieldExtractors.get(i).apply(item);

                if (allWordsMatchInField(fieldValue, searchWords)) {
                    buckets.get(i).add(item);
                    foundInSingleField = true;
                    break; // Item goes in highest priority match only
                }
            }

            if (!foundInSingleField) {
                crossFieldCandidates.add(item);
            }
        }

        // Second pass: check cross-field matches for remaining items
        List<T> crossFieldMatches = new ArrayList<>();
        for (T item : crossFieldCandidates) {
            if (matchesAcrossFields(item, searchWords, fieldExtractors)) {
                crossFieldMatches.add(item);
            }
        }

        // Combine: single-field matches first (by priority), then cross-field matches
        List<T> result = new ArrayList<>();
        for (List<T> bucket : buckets) {
            result.addAll(bucket);
        }
        result.addAll(crossFieldMatches);

        return result;
    }

    /**
     * Checks if all search words match within a single field
     */
    private boolean allWordsMatchInField(String fieldValue, String[] searchWords) {
        if (fieldValue == null) {
            return false;
        }

        String normalizedText = fieldValue.toLowerCase();
        Set<String> textWords = extractWords(normalizedText);

        // Each search word must match a DISTINCT word in the field
        Set<String> matchedWords = new HashSet<>();

        for (String searchWord : searchWords) {
            boolean found = false;
            for (String textWord : textWords) {
                // Skip if this word was already matched
                if (matchedWords.contains(textWord)) {
                    continue;
                }

                if (wordMatches(textWord, searchWord)) {
                    matchedWords.add(textWord);
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if search words match across multiple fields
     */
    private boolean matchesAcrossFields(T item, String[] searchWords, List<Function<T, String>> fieldExtractors) {
        // Collect all words from all fields
        Set<String> allWords = new HashSet<>();

        for (Function<T, String> extractor : fieldExtractors) {
            String fieldValue = extractor.apply(item);
            if (fieldValue != null) {
                allWords.addAll(extractWords(fieldValue.toLowerCase()));
            }
        }

        // Check if all search words can be matched to distinct words
        Set<String> matchedWords = new HashSet<>();

        for (String searchWord : searchWords) {
            boolean found = false;
            for (String textWord : allWords) {
                if (matchedWords.contains(textWord)) {
                    continue;
                }

                if (wordMatches(textWord, searchWord)) {
                    matchedWords.add(textWord);
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }

    /**
     * Extracts individual words from text
     */
    private Set<String> extractWords(String text) {
        Set<String> words = new HashSet<>();
        if (text == null) {
            return words;
        }

        // Split on whitespace and commas
        String[] tokens = text.split("[\\s,]+");
        for (String token : tokens) {
            // Remove leading/trailing punctuation but keep internal ones
            String cleanWord = token.replaceAll("^[.\\-]+|[.\\-]+$", "");
            if (!cleanWord.isEmpty()) {
                words.add(cleanWord);
            }
        }

        return words;
    }

    /**
     * Checks if a text word matches a search word
     * Handles special cases like "J.K." matching "jk" or "j.k"
     */
    private boolean wordMatches(String textWord, String searchWord) {
        if (textWord.startsWith(searchWord)) {
            return true;
        }

        // Also check with periods removed (so "jk" finds "j.k.")
        String textNoPeriods = textWord.replace(".", "");
        String searchNoPeriods = searchWord.replace(".", "");

        return textNoPeriods.startsWith(searchNoPeriods);
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