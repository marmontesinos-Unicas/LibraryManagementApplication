package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.User;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service class responsible for filtering and searching a catalog of User entities.
 * <p>
 * This class applies filtering based on user roles and then performs a prioritized
 * relevance search using fields like name, username, and email.
 * </p>
 *
 * Access Keyword Explanation: {@code public} - Allows other service or controller layers to utilize the filtering logic.
 */
public class UserCatalogService {

    // Instantiation of a generic search utility service for User objects
    private final SearchService<User> searchService = new SearchService<>();

    /**
     * Defines the prioritized fields used by the SearchService for ranking and searching users.
     * Priority: Name > Surname > Username > Email > National ID.
     */
    private static final List<Function<User, String>> USER_SEARCH_FIELDS =
            SearchService.<User>fieldsBuilder()
                    .addField(User::getName)
                    .addField(User::getSurname)
                    .addField(User::getUsername)
                    .addField(User::getEmail)
                    .addField(User::getNationalID)
                    .build();

    /**
     * Filters and searches users based on specified criteria.
     * <p>
     * Filtering is applied first (Role), followed by relevance-based searching and sorting.
     * </p>
     *
     * @param users List of all users.
     * @param roleMap Map of role ID to role name (e.g., 1 -> "Admin", 2 -> "User") for lookup.
     * @param selectedRoles Set of role names (e.g., "Admin", "User") to filter by.
     * @param searchTerm The term to search within the prioritized user fields.
     * @return Filtered and sorted list of users matching the criteria.
     */
    public List<User> filterUsers(
            List<User> users,
            Map<Integer, String> roleMap,
            Set<String> selectedRoles,
            String searchTerm
    ) {

        List<User> filtered = users.stream()
                .filter(user -> {
                    // Filter by role if specified
                    boolean matchesRole = selectedRoles.isEmpty();
                    if (!matchesRole) {
                        String userRole = roleMap.get(user.getIdRole()); // Look up the user's role name using their ID
                        matchesRole = userRole != null && selectedRoles.contains(userRole); // Check if the user's role exists and is in the selected set
                    }

                    return matchesRole;
                })
                .collect(Collectors.toList());

        // Apply search with user-specific priority
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            filtered = searchService.searchAndSort(filtered, searchTerm, USER_SEARCH_FIELDS);
        }

        return filtered;
    }
}