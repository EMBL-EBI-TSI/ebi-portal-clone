package uk.ac.ebi.tsc.portal.api.deployment.repo;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
public enum DeploymentStatusEnum {
    STARTING, RUNNING, DESTROYING, STARTING_FAILED, RUNNING_FAILED, DESTROYED, DESTROYING_FAILED
}
