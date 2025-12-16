package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Material;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service class responsible for filtering and searching a catalog of materials.
 * <p>
 * This class applies multiple filtering criteria (type, status, genre, year range)
 * and then performs a prioritized search on the remaining list of materials.
 * </p>
 *
 * Access Keyword Explanation: {@code public} - Allows other service or controller layers to utilize the filtering logic.
 */
public class MaterialCatalogService {

    private final SearchService<Material> searchService = new SearchService<>();

    // Define search fields once as a constant - Priority: Title > Author > ISBN > Status
    /**
     * Defines the prioritized fields used by the SearchService for ranking and searching materials.
     * <p>
     * The order (Title, Author, ISBN, Status) dictates the priority for relevance scoring.
     * </p>
     */
    private static final List<Function<Material, String>> MATERIAL_SEARCH_FIELDS =
            SearchService.<Material>fieldsBuilder()
                    .addField(Material::getTitle)
                    .addField(Material::getAuthor)
                    .addField(Material::getISBN)
                    .addField(Material::getMaterial_status)
                    .build();

    /**
     * Applies a chain of filters (Type, Status, Genre, Year Range) and a search term
     * to a base list of materials.
     * <p>
     * Filtering is done using the Java Stream API for efficiency and readability.
     * Search is applied last using the dedicated {@code SearchService}.
     * </p>
     *
     * @param materials The initial, unfiltered list of all Material objects.
     * @param materialGenreMap Map linking Material ID to a Set of Genre IDs (for genre lookup).
     * @param materialTypeMap Map linking Material Type ID to its name (for type name lookup).
     * @param genreMap Map linking Genre ID to its name (for genre name lookup).
     * @param selectedTypes Set of selected Material Type names to filter by.
     * @param selectedStatuses Set of selected Material Status names to filter by.
     * @param selectedGenres Set of selected Genre names to filter by.
     * @param yearFrom String representing the minimum year for the range filter (inclusive).
     * @param yearTo String representing the maximum year for the range filter (inclusive).
     * @param searchTerm The user-entered term for relevance-based searching.
     * @return The final list of materials after all filters and search criteria have been applied.
     */
    public List<Material> filterMaterials(
            List<Material> materials,
            Map<Integer, Set<Integer>> materialGenreMap,
            Map<Integer, String> materialTypeMap,
            Map<Integer, String> genreMap,
            Set<String> selectedTypes,
            Set<String> selectedStatuses,
            Set<String> selectedGenres,
            String yearFrom,
            String yearTo,
            String searchTerm
    ) {

        List<Material> filtered = materials.stream()
                .filter(material -> {
                    boolean matchesType =
                            selectedTypes.isEmpty() ||
                                    selectedTypes.contains(materialTypeMap.get(material.getIdMaterialType()));

                    boolean matchesStatus =
                            selectedStatuses.isEmpty() ||
                                    selectedStatuses.contains(material.getMaterial_status());

                    boolean matchesGenre = true;
                    if (!selectedGenres.isEmpty()) {
                        Set<Integer> genreIds = materialGenreMap.get(material.getIdMaterial());
                        if (genreIds != null && !genreIds.isEmpty()) {
                            matchesGenre = genreIds.stream()
                                    .map(genreMap::get)
                                    .anyMatch(selectedGenres::contains);
                        }
                    }

                    boolean matchesYear = true;
                    try {
                        if (!yearFrom.isEmpty()) {
                            matchesYear = material.getYear() >= Integer.parseInt(yearFrom);
                        }
                        if (!yearTo.isEmpty() && matchesYear) {
                            matchesYear = material.getYear() <= Integer.parseInt(yearTo);
                        }
                    } catch (NumberFormatException ignored) {
                        matchesYear = true;
                    }

                    return matchesType && matchesStatus && matchesGenre && matchesYear;
                })
                .collect(Collectors.toList());

        // Apply search with prioritized fields.
        // If a search term is provided, apply the search and sorting logic from the utility service
        if (!searchTerm.isEmpty()) {
            filtered = searchService.searchAndSort(filtered, searchTerm, MATERIAL_SEARCH_FIELDS);
        }

        return filtered;
    }
}

