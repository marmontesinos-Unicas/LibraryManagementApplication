package it.unicas.project.template.address.model;

import javafx.beans.property.*;
/**
 * Represents a user role in the system (static table) for UI display.
 * <p>
 * Provides JavaFX properties for TableView or other UI bindings.
 * </p>
 */
public class Role {

    /** Unique identifier of the role */
    private final IntegerProperty idRole;

    /** Type of administrator (role name) */
    private final StringProperty admin_type;

    /**
     * Constructs a Role with specified ID and name.
     *
     * @param idRole ID of the role
     * @param admin_type name of the role
     */
    public Role(Integer idRole, String admin_type){
        this.idRole = new SimpleIntegerProperty(idRole);
        this.admin_type = new SimpleStringProperty(admin_type);
    }

    /** Getters and JavaFX properties for UI binding */
    public Integer getIdRole() { return idRole.get(); }
    public IntegerProperty idRoleProperty() { return idRole; }

    public String getAdmin_type() { return admin_type.get(); }
    public StringProperty admin_typeProperty() { return admin_type; }

    /** Returns the role name as string */
    @Override
    public String toString() {
        return admin_type.get();
    }
}
