package gr.hua.dit.petcare.security;

/**
 * Ρόλοι της εφαρμογής.
 * Συνήθως τους αποθηκεύουμε στον User ως strings: "OWNER", "VET", "ADMIN".
 */
public enum Role {
    OWNER,
    VET,
    ADMIN;

    /**
     * Πλήρες authority name για Spring Security (ROLE_OWNER κλπ).
     */
    public String asAuthority() {
        return "ROLE_" + name();
    }
}
