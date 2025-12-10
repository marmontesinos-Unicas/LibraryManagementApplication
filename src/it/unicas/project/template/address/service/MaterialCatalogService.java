package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Material;

import java.util.*;
import java.util.stream.Collectors;

public class MaterialCatalogService {

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

        if (!searchTerm.isEmpty()) {
            filtered = searchAndSort(filtered, searchTerm);
        }

        return filtered;
    }
    List<Material> searchAndSort(List<Material> materials, String searchTerm) {
        String term = searchTerm.toLowerCase();

        List<Material> title = new ArrayList<>();
        List<Material> author = new ArrayList<>();
        List<Material> other = new ArrayList<>();

        for (Material m : materials) {
            if (containsWordStartingWith(m.getTitle(), term)) {
                title.add(m);
            } else if (containsWordStartingWith(m.getAuthor(), term)) {
                author.add(m);
            } else if (
                    containsWordStartingWith(m.getISBN(), term) ||
                            containsWordStartingWith(m.getMaterial_status(), term)
            ) {
                other.add(m);
            }
        }

        List<Material> result = new ArrayList<>();
        result.addAll(title);
        result.addAll(author);
        result.addAll(other);
        return result;
    }

    boolean containsWordStartingWith(String text, String searchTerm) {
        if (text == null || searchTerm == null) return false;

        for (String word : text.toLowerCase().split("[\\s,.-]+")) {
            if (word.startsWith(searchTerm.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}

