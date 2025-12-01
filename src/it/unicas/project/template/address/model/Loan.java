package it.unicas.project.template.address.model;

public class Loan {

    private Integer idLoan;
    private Integer idUser;
    private Integer idMaterial;
    private String start_date;
    private String due_date;
    private String return_date;

    public Loan() {}

    public Loan(Integer idLoan, Integer idUser, Integer idMaterial,
                String start_date, String due_date, String return_date) {
        this.idLoan = idLoan;
        this.idUser = idUser;
        this.idMaterial = idMaterial;
        this.start_date = start_date;
        this.due_date = due_date;
        this.return_date = return_date;
    }

    public Loan(Integer idUser, Integer idMaterial,
                String start_date, String due_date, String return_date) {
        this(null, idUser, idMaterial, start_date, due_date, return_date);
    }

    // GETTERS & SETTERS
    public Integer getIdLoan() { return idLoan; }
    public void setIdLoan(Integer idLoan) { this.idLoan = idLoan; }

    public Integer getIdUser() { return idUser; }
    public void setIdUser(Integer idUser) { this.idUser = idUser; }

    public Integer getIdMaterial() { return idMaterial; }
    public void setIdMaterial(Integer idMaterial) { this.idMaterial = idMaterial; }

    public String getStart_date() { return start_date; }
    public void setStart_date(String start_date) { this.start_date = start_date; }

    public String getDue_date() { return due_date; }
    public void setDue_date(String due_date) { this.due_date = due_date; }

    public String getReturn_date() { return return_date; }
    public void setReturn_date(String return_date) { this.return_date = return_date; }

    @Override
    public String toString() {
        return "Loan " + idLoan;
    }
}
