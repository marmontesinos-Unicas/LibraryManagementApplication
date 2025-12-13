package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LoanCatalogService {

    private final SearchService<Loan> searchService = new SearchService<>();

    /**
     * Define search fields with priority based on Maps for lookups
     * Priority: Material Title > User Name > User Surname > Author > Material ISBN
     *
     * Note: This requires passing materialMap and userMap to the filter method
     */
    private List<Function<Loan, String>> buildLoanSearchFields(
            Map<Integer, Material> materialMap,
            Map<Integer, User> userMap) {

        return SearchService.<Loan>fieldsBuilder()
                .addField(loan -> {
                    Material material = materialMap.get(loan.getIdMaterial());
                    return material != null ? material.getTitle() : "";
                })
                .addField(loan -> {
                    User user = userMap.get(loan.getIdUser());
                    return user != null ? user.getName() : "";
                })
                .addField(loan -> {
                    User user = userMap.get(loan.getIdUser());
                    return user != null ? user.getSurname() : "";
                })
                .addField(loan -> {
                    Material material = materialMap.get(loan.getIdMaterial());
                    return material != null ? material.getAuthor() : "";
                })
                .addField(loan -> {
                    Material material = materialMap.get(loan.getIdMaterial());
                    return material != null ? material.getISBN() : "";
                })
                // NEW: Add material type as searchable field
                .addField(loan -> {
                    Material material = materialMap.get(loan.getIdMaterial());
                    if (material == null || material.getIdMaterialType() == null) return "";

                    return switch (material.getIdMaterialType()) {
                        case 1 -> "Book";
                        case 2 -> "CD";
                        case 3 -> "Movie";
                        case 4 -> "Magazine";
                        default -> "";
                    };
                })
                // NEW: Add due date as searchable field (format: yyyy-MM-dd)
                .addField(loan -> {
                    return loan.getDue_date() != null ?
                            loan.getDue_date().toLocalDate().toString() : "";
                })
                .build();
    }

    /**
     * Filters and searches loans based on criteria
     *
     * @param loans List of all loans
     * @param materialMap Map of material ID to Material object (for search lookups)
     * @param userMap Map of user ID to User object (for search lookups)
     * @param selectedStatuses Filter by loan status (active, returned, overdue, etc.)
     * @param dateFrom Filter loans from this date
     * @param dateTo Filter loans to this date
     * @param searchTerm Search term for title, user names, author, ISBN
     * @return Filtered and sorted list of loans
     */
    public List<Loan> filterLoans(
            List<Loan> loans,
            Map<Integer, Material> materialMap,
            Map<Integer, User> userMap,
            Set<String> selectedStatuses,
            String dateFrom,
            String dateTo,
            String searchTerm
    ) {
        // First apply status and date filters
        List<Loan> filtered = loans.stream()
                .filter(loan -> {
                    // Status filter
                    if (!selectedStatuses.isEmpty()) {
                        if (selectedStatuses.contains("overdue")) {
                            boolean isOverdue = loan.getDue_date() != null &&
                                    loan.getDue_date().isBefore(java.time.LocalDateTime.now());
                            if (!isOverdue) return false;
                        }
                    }

                    // Date range filter (due_date)
                    if (dateFrom != null && !dateFrom.trim().isEmpty()) {
                        try {
                            java.time.LocalDate fromDate = java.time.LocalDate.parse(dateFrom);
                            if (loan.getDue_date() == null ||
                                    loan.getDue_date().toLocalDate().isBefore(fromDate)) {
                                return false;
                            }
                        } catch (Exception e) {
                            // Invalid date format, skip filter
                        }
                    }

                    if (dateTo != null && !dateTo.trim().isEmpty()) {
                        try {
                            java.time.LocalDate toDate = java.time.LocalDate.parse(dateTo);
                            if (loan.getDue_date() == null ||
                                    loan.getDue_date().toLocalDate().isAfter(toDate)) {
                                return false;
                            }
                        } catch (Exception e) {
                            // Invalid date format, skip filter
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        // Apply search with loan-specific priority
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            List<Function<Loan, String>> searchFields = buildLoanSearchFields(materialMap, userMap);
            filtered = searchService.searchAndSort(filtered, searchTerm, searchFields);
        }

        return filtered;
    }
}