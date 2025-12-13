package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Material;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MaterialCatalogService {

    private final SearchService<Material> searchService = new SearchService<>();

    // Define search fields once as a constant - Priority: Title > Author > ISBN > Status
    private static final List<Function<Material, String>> MATERIAL_SEARCH_FIELDS =
            SearchService.<Material>fieldsBuilder()
                    .addField(Material::getTitle)
                    .addField(Material::getAuthor)
                    .addField(Material::getISBN)
                    .addField(Material::getMaterial_status)
                    .build();

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
                        if (genreIds == null || genreIds.isEmpty()) {
                            matchesGenre = false;
                        } else {
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

        // Apply search with prioritized fields
        if (!searchTerm.isEmpty()) {
            filtered = searchService.searchAndSort(filtered, searchTerm, MATERIAL_SEARCH_FIELDS);
        }

        return filtered;
    }
}

