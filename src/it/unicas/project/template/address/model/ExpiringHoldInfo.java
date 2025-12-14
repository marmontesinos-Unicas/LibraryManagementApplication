package it.unicas.project.template.address.model;

import java.time.LocalDate;

/**
 * A simple class to hold data for a hold that is about to expire,
 * retrieved from the database by the DAO layer.
 */
public class ExpiringHoldInfo {

    private final int idHold;
    private final String materialTitle;
    private final String materialAuthor;
    private final LocalDate holdExpirationDate;

    /**
     * Constructs an ExpiringHoldInfo object.
     * @param idHold The ID of the hold record.
     * @param materialTitle The title of the held material.
     * @param materialAuthor The author of the held material.
     * @param holdExpirationDate The calculated date on which the hold expires.
     */
    public ExpiringHoldInfo(int idHold, String materialTitle, String materialAuthor, LocalDate holdExpirationDate) {
        this.idHold = idHold;
        this.materialTitle = materialTitle;
        this.materialAuthor = materialAuthor;
        this.holdExpirationDate = holdExpirationDate;
    }

    // --- Getters ---
    public int getIdHold() {
        return idHold;
    }

    public String getMaterialTitle() {
        return materialTitle;
    }

    public String getMaterialAuthor() {
        return materialAuthor;
    }

    public LocalDate getHoldExpirationDate() {
        return holdExpirationDate;
    }
}