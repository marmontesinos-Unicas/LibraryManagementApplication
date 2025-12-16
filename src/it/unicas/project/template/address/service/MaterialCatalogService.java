package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.view.UserCatalogController.GroupedMaterial;

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

    private final SearchService<Material> materialSearchService = new SearchService<>();
    private final SearchService<GroupedMaterial> groupedSearchService = new SearchService<>();

    // Search fields for Material
    private static final List<Function<Material, String>> MATERIAL_SEARCH_FIELDS =
            SearchService.<Material>fieldsBuilder()
                    .addField(Material::getTitle)
                    .addField(Material::getAuthor)
                    .addField(Material::getISBN)
                    .addField(Material::getMaterial_status)
                    .build();

    // Search fields for GroupedMaterial
    private static final List<Function<GroupedMaterial, String>> GROUPED_SEARCH_FIELDS =
            SearchService.<GroupedMaterial>fieldsBuilder()
                    .addField(GroupedMaterial::getTitle)
                    .addField(GroupedMaterial::getAuthor)
                    .addField(GroupedMaterial::getISBN)
                    .addField(GroupedMaterial::getType)
                    .addField(GroupedMaterial::getGenres)
                    .build();

    /**
     * Filter individual materials (for Admin view)
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
                    // Type filter: if empty, show only materials with no type
                    boolean matchesType;
                    if (selectedTypes.isEmpty()) {
                        matchesType = material.getIdMaterialType() == null ||
                                materialTypeMap.get(material.getIdMaterialType()) == null;
                    } else {
                        matchesType = selectedTypes.contains(materialTypeMap.get(material.getIdMaterialType()));
                    }

                    // Status filter: if empty, show only materials with no status
                    boolean matchesStatus;
                    if (selectedStatuses.isEmpty()) {
                        matchesStatus = material.getMaterial_status() == null ||
                                material.getMaterial_status().isEmpty();
                    } else {
                        matchesStatus = selectedStatuses.contains(material.getMaterial_status());
                    }

                    // Genre filter: if empty, show only materials with no genre
                    boolean matchesGenre;
                    if (selectedGenres.isEmpty()) {
                        Set<Integer> genreIds = materialGenreMap.get(material.getIdMaterial());
                        matchesGenre = genreIds == null || genreIds.isEmpty();
                    } else {
                        Set<Integer> genreIds = materialGenreMap.get(material.getIdMaterial());
                        if (genreIds != null && !genreIds.isEmpty()) {
                            matchesGenre = genreIds.stream()
                                    .map(genreMap::get)
                                    .anyMatch(selectedGenres::contains);
                        } else {
                            matchesGenre = false;
                        }
                    }

                    // Year filter
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

        // Apply search with prioritized fields
        if (!searchTerm.isEmpty()) {
            filtered = materialSearchService.searchAndSort(filtered, searchTerm, MATERIAL_SEARCH_FIELDS);
        }

        return filtered;
    }

    /**
     * Filter grouped materials (for User view)
     */
    public List<GroupedMaterial> filterGroupedMaterials(
            List<GroupedMaterial> groupedMaterials,
            Set<String> selectedTypes,
            Set<String> selectedGenres,
            String yearFrom,
            String yearTo,
            String searchTerm
    ) {

        List<GroupedMaterial> filtered = groupedMaterials.stream()
                .filter(gm -> {
                    // Type filter: if empty, show only materials with no type
                    boolean matchesType;
                    if (selectedTypes.isEmpty()) {
                        matchesType = gm.getType() == null ||
                                gm.getType().isEmpty() ||
                                gm.getType().equals("Unknown");
                    } else {
                        matchesType = selectedTypes.contains(gm.getType());
                    }

                    // Genre filter: if empty, show only materials with no genre
                    boolean matchesGenre;
                    if (selectedGenres.isEmpty()) {
                        matchesGenre = gm.getGenres() == null ||
                                gm.getGenres().equals("—") ||
                                gm.getGenres().isEmpty();
                    } else {
                        if (gm.getGenres() == null || gm.getGenres().equals("—")) {
                            matchesGenre = false;
                        } else {
                            // Check if any genre in the comma-separated list matches
                            matchesGenre = Arrays.stream(gm.getGenres().split(", "))
                                    .anyMatch(selectedGenres::contains);
                        }
                    }

                    // Year filter
                    boolean matchesYear = true;
                    try {
                        if (!yearFrom.isEmpty()) {
                            matchesYear = gm.getYear() >= Integer.parseInt(yearFrom);
                        }
                        if (!yearTo.isEmpty() && matchesYear) {
                            matchesYear = gm.getYear() <= Integer.parseInt(yearTo);
                        }
                    } catch (NumberFormatException ignored) {
                        matchesYear = true;
                    }

                    return matchesType && matchesGenre && matchesYear;
                })
                .collect(Collectors.toList());

        // Apply search with prioritized fields
        if (!searchTerm.isEmpty()) {
            filtered = groupedSearchService.searchAndSort(filtered, searchTerm, GROUPED_SEARCH_FIELDS);
        }

        return filtered;
    }
}