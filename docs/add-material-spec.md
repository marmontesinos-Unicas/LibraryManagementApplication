# Add New Material Feature – Specification

## 1. User Story
**As a Library Staff, I want to add new material so that it is available in the catalog.**

---

## 2. Acceptance Criteria
1. System allows entering all required fields:
    - title, author, year, ISBN, material type.
2. System prevents saving if any required field is missing.
3. When submitted, the new material is stored in the database.
4. The material appears immediately in catalog search.
5. System assigns the keyword `"available"` as the status.
6. System logs the event `"Material updated to catalog"`.
7. If a DB error occurs, system displays an error message and logs it.

---

## 3. Data Model (from database)
Tables involved:
- **MATERIALS**  
  (`title`, `author`, `year`, `ISBN`, `idMaterialType`, `material_status`)
- **materials_genres**  
  (links materials → genres)
- **GENRE**
- **MATERIAL_TYPE**

---

## 4. Required Fields and Validation Rules
| Field            | Required? | Validation                          |
|------------------|-----------|-------------------------------------|
| title            | Yes       | Not blank                           |
| author           | Yes       | Not blank                           |
| year             | No        | 4-digit integer (1000–9999) or null |
| ISBN             | No        | None                                |
| materialType     | Yes       | Must match existing ID              |
| genreIds         | Yes       | At least one genre required         |

System automatically sets:
- `material_status = "available"`

---

## 5. Business Rules
1. Input is validated before saving.
2. If validation fails → return validation error message.
3. After successful validation:
    - Save material → get generated id
    - Save genre links
    - Log `"Material updated to catalog"`
4. Material must be visible in catalog search immediately.

---

## 6. Error Handling
- Any `SQLException` or DB failure:
    - Log error with stack trace
    - Return user-friendly error:  
      `"Internal error. Please contact administrator."`

---

## 7. Tasks Breakdown
1. Write JUnit tests for `CatalogService.saveItem()`
2. Implement `ItemDAO.create()` (JDBC)
3. Implement `CatalogService.saveItem()`
4. Implement `CatalogController.addItem()`
5. Design UI form (`add-item.html`)
6. Write integration test (POST → DB → search)

---

## 8. Test Plan
### Unit Tests
- Missing title → validation exception
- Missing genre list → validation exception
- Valid DTO → DAO called, status set to “available”
- DAO throws SQLException → controller returns HTTP 500

### Integration Tests
- POST /api/catalog/items with valid JSON →  
  → INSERT executed in DB  
  → SELECT finds new row  
  → search endpoint returns the new material

---

**End of Document**
