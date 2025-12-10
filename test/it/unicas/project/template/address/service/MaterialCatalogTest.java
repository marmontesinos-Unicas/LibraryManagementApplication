package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MaterialCatalogServiceTest {

    private MaterialCatalogService service;
    private List<Material> materials;
    private Map<Integer, String> typeMap;
    private Map<Integer, String> genreMap;
    private Map<Integer, Set<Integer>> materialGenreMap;

    @BeforeEach
    void setUp() {
        service = new MaterialCatalogService();

        Material m1 = new Material();
        m1.setIdMaterial(1);
        m1.setTitle("Java Programming");
        m1.setAuthor("Smith");
        m1.setYear(2020);
        m1.setMaterial_status("Available");
        m1.setIdMaterialType(1);

        Material m2 = new Material();
        m2.setIdMaterial(2);
        m2.setTitle("Database Systems");
        m2.setAuthor("Brown");
        m2.setYear(2015);
        m2.setMaterial_status("On Loan");
        m2.setIdMaterialType(2);

        materials = List.of(m1, m2);

        typeMap = Map.of(
                1, "Book",
                2, "DVD"
        );

        genreMap = Map.of(
                1, "Programming",
                2, "Databases"
        );

        materialGenreMap = Map.of(
                1, Set.of(1),
                2, Set.of(2)
        );
    }

    @Test
    void filter_by_type() {
        List<Material> result = service.filterMaterials(
                materials,
                materialGenreMap,
                typeMap,
                genreMap,
                Set.of("Book"),
                Set.of(),
                Set.of(),
                "",
                "",
                ""
        );

        assertEquals(1, result.size());
        assertEquals("Java Programming", result.get(0).getTitle());
    }

    @Test
    void filter_by_genre() {
        List<Material> result = service.filterMaterials(
                materials,
                materialGenreMap,
                typeMap,
                genreMap,
                Set.of(),
                Set.of(),
                Set.of("Databases"),
                "",
                "",
                ""
        );

        assertEquals(1, result.size());
        assertEquals("Database Systems", result.get(0).getTitle());
    }

    @Test
    void search_prioritizes_title() {
        List<Material> result = service.filterMaterials(
                materials,
                materialGenreMap,
                typeMap,
                genreMap,
                Set.of(),
                Set.of(),
                Set.of(),
                "",
                "",
                "java"
        );

        assertEquals(1, result.size());
        assertEquals("Java Programming", result.get(0).getTitle());
    }

    @Test
    void invalid_year_does_not_crash() {
        List<Material> result = service.filterMaterials(
                materials,
                materialGenreMap,
                typeMap,
                genreMap,
                Set.of(),
                Set.of(),
                Set.of(),
                "abc",
                "xyz",
                ""
        );

        assertEquals(2, result.size());
    }
}