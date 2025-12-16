package it.unicas.project.template.address.model;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) that represents information about a hold
 * that is close to expiring.
 * <p>
 * This class is used to transfer aggregated and calculated data from
 * the DAO layer to the service layer without exposing domain entities.
 * </p>
 */
public class ExpiringHoldInfo {

    /** Unique identifier of the hold */
    private final int idHold;

    /** Title of the material associated with the hold */
    private final String materialTitle;

    /** Author of the held material */
    private final String materialAuthor;

    /** Date on which the hold expires */
    private final LocalDate holdExpirationDate;

    /**
     * Constructs an {@code ExpiringHoldInfo} instance.
     *
     * @param idHold the unique identifier of the hold
     * @param materialTitle the title of the held material
     * @param materialAuthor the author of the held material
     * @param holdExpirationDate the date when the hold expires
     */
    public ExpiringHoldInfo(int idHold,
                            String materialTitle,
                            String materialAuthor,
                            LocalDate holdExpirationDate) {
        this.idHold = idHold;
        this.materialTitle = materialTitle;
        this.materialAuthor = materialAuthor;
        this.holdExpirationDate = holdExpirationDate;
    }

    /**
     * Returns the hold identifier.
     *
     * @return the hold ID
     */
    public int getIdHold() {
        return idHold;
    }

    /**
     * Returns the title of the held material.
     *
     * @return material title
     */
    public String getMaterialTitle() {
        return materialTitle;
    }

    /**
     * Returns the author of the held material.
     *
     * @return material author
     */
    public String getMaterialAuthor() {
        return materialAuthor;
    }

    /**
     * Returns the expiration date of the hold.
     *
     * @return hold expiration date
     */
    public LocalDate getHoldExpirationDate() {
        return holdExpirationDate;
    }
}