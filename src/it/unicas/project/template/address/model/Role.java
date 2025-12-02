package it.unicas.project.template.address.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Role (tabla estática) - solo lectura para UI
 */
public class Role {

    private final IntegerProperty idRole;
    private final StringProperty admin_type;

    public Role(Integer idRole, String admin_type){
        this.idRole = new SimpleIntegerProperty(idRole);
        this.admin_type = new SimpleStringProperty(admin_type);
    }

    // GETTERS
    public Integer getIdRole() { return idRole.get(); }
    public IntegerProperty idRoleProperty() { return idRole; }

    public String getAdmin_type() { return admin_type.get(); }
    public StringProperty admin_typeProperty() { return admin_type; }

    @Override
    public String toString() {
        return admin_type.get();
    }
}
