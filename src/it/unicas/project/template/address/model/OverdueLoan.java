package it.unicas.project.template.address.model;

import java.time.LocalDate;

/**
 * Represents an overdue loan notification, combining data from 'loans' and 'materials'.
 * <p>
 * Used to display essential details of loans that are past their due date.
 * </p>
 */
public class OverdueLoan {

    /** Unique ID of the loan */
    private final int loanId;

    /** Title of the overdue material */
    private final String materialTitle;

    /** Author of the overdue material */
    private final String materialAuthor;

    /** Due date of the loan */
    private final LocalDate dueDate;

    /**
     * Constructs an OverdueLoan instance.
     *
     * @param loanId ID of the overdue loan
     * @param materialTitle title of the material
     * @param materialAuthor author of the material
     * @param dueDate the date the material was supposed to be returned
     */
    public OverdueLoan(int loanId, String materialTitle, String materialAuthor, LocalDate dueDate) {
        this.loanId = loanId;
        this.materialTitle = materialTitle;
        this.materialAuthor = materialAuthor;
        this.dueDate = dueDate;
    }

    /** Getters for UI binding and data access */
    public int getLoanId() { return loanId; }
    public String getMaterialTitle() { return materialTitle; }
    public String getMaterialAuthor() { return materialAuthor; }
    public LocalDate getDueDate() { return dueDate; }
}
