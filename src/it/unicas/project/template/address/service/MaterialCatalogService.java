package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.view.UserCatalogController.GroupedMaterial;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                    // Type filter
                    boolean matchesType;
                    if (selectedTypes.isEmpty()) {
                        // Empty selection: show only materials with no type
                        matchesType = material.getIdMaterialType() == null ||
                                materialTypeMap.get(material.getIdMaterialType()) == null;
                    } else {
                        // Check if material has a type and if it's in the selected types
                        String materialType = materialTypeMap.get(material.getIdMaterialType());
                        matchesType = selectedTypes.contains(materialType);

                        // If all types are selected, also include materials with no type
                        if (!matchesType && materialType == null) {
                            matchesType = true;
                        }
                    }

                    // Status filter
                    boolean matchesStatus;
                    if (selectedStatuses.isEmpty()) {
                        // Empty selection: show only materials with no status
                        matchesStatus = material.getMaterial_status() == null ||
                                material.getMaterial_status().isEmpty();
                    } else {
                        matchesStatus = selectedStatuses.contains(material.getMaterial_status());

                        // If material has no status, include it when all statuses are selected
                        if (!matchesStatus && (material.getMaterial_status() == null ||
                                material.getMaterial_status().isEmpty())) {
                            matchesStatus = true;
                        }
                    }

                    // Genre filter
                    boolean matchesGenre;
                    Set<Integer> genreIds = materialGenreMap.get(material.getIdMaterial());
                    boolean hasNoGenre = genreIds == null || genreIds.isEmpty();

                    if (selectedGenres.isEmpty()) {
                        // Empty selection: show only materials with no genre
                        matchesGenre = hasNoGenre;
                    } else {
                        if (hasNoGenre) {
                            // Material has no genre: include it (when any genre is selected, we include empty ones)
                            matchesGenre = true;
                        } else {
                            // Check if any of the material's genres are in the selected genres
                            matchesGenre = genreIds.stream()
                                    .map(genreMap::get)
                                    .anyMatch(selectedGenres::contains);
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
                    // Type filter
                    boolean matchesType;
                    boolean hasNoType = gm.getType() == null ||
                            gm.getType().isEmpty() ||
                            gm.getType().equals("Unknown");

                    if (selectedTypes.isEmpty()) {
                        // Empty selection: show only materials with no type
                        matchesType = hasNoType;
                    } else {
                        if (hasNoType) {
                            // Material has no type: include it (when any type is selected, we include empty ones)
                            matchesType = true;
                        } else {
                            matchesType = selectedTypes.contains(gm.getType());
                        }
                    }

                    // Genre filter
                    boolean matchesGenre;
                    boolean hasNoGenre = gm.getGenres() == null ||
                            gm.getGenres().equals("—") ||
                            gm.getGenres().isEmpty();

                    if (selectedGenres.isEmpty()) {
                        // Empty selection: show only materials with no genre
                        matchesGenre = hasNoGenre;
                    } else {
                        if (hasNoGenre) {
                            // Material has no genre: include it (when any genre is selected, we include empty ones)
                            matchesGenre = true;
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