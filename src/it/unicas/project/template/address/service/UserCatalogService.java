package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.User;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class UserCatalogService {

    private final SearchService<User> searchService = new SearchService<>();

    // Priority: Name > Surname > Username > Email > National ID
    private static final List<Function<User, String>> USER_SEARCH_FIELDS =
            SearchService.<User>fieldsBuilder()
                    .addField(User::getName)
                    .addField(User::getSurname)
                    .addField(User::getUsername)
                    .addField(User::getEmail)
                    .addField(User::getNationalID)
                    .build();

    /**
     * Filters and searches users based on criteria
     *
     * @param users List of all users
     * @param roleMap Map of role ID to role name (e.g., 1 -> "Admin", 2 -> "User")
     * @param selectedRoles Filter by role names (e.g., "Admin", "User")
     * @param searchTerm Search term for name, surname, username, email, nationalID
     * @return Filtered and sorted list of users
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
                        String userRole = roleMap.get(user.getIdRole());
                        matchesRole = userRole != null && selectedRoles.contains(userRole);
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