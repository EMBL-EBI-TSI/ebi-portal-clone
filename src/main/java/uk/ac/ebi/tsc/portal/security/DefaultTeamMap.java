package uk.ac.ebi.tsc.portal.security;

import org.springframework.stereotype.Component;

/**
 * Created by jdianes on 10/09/2018.
 */
@Component
public class DefaultTeamMap {
    private String teamName;
    private String emailDomain;

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getEmailDomain() {
        return emailDomain;
    }

    public void setEmailDomain(String emailDomain) {
        this.emailDomain = emailDomain;
    }
}
