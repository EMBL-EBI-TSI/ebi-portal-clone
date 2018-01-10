package uk.ac.ebi.tsc.portal.api.healthcheck.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@RestController
@RequestMapping(value = "/ping")
public class HealthCheckRestController {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckRestController.class);

    @RequestMapping(method = {RequestMethod.GET})
    public String ping() {
        logger.trace("Ha, Ha, Ha, Ha, Staying...");
        logger.debug("Alive");
        return "pong";
    }

}
