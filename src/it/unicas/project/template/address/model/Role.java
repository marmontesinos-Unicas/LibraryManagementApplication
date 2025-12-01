package it.unicas.project.template.address.model;

public class Role {

    private final Integer idRole;
    private final String admin_type;  // coincide con columna SQL

    public Role(Integer idRole, String admin_type){
        this.idRole = idRole;
        this.admin_type = admin_type;
    }

    public Integer getIdRole() { return idRole; }
    public String getAdmin_type() { return admin_type; }

    @Override
    public String toString() {
        return admin_type;
    }
}
