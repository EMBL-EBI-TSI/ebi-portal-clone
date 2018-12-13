package uk.ac.ebi.tsc.portal.api.volumeinstance.service;

import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstance;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceRepository;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceStatus;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceStatusRepository;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Service
public class VolumeInstanceService {

    private final VolumeInstanceRepository volumeInstanceRepository;
    private final VolumeInstanceStatusRepository volumeInstanceStatusRepository;

    @Autowired
    public VolumeInstanceService(VolumeInstanceRepository volumeInstanceRepository,
                                 VolumeInstanceStatusRepository volumeInstanceStatusRepository) {
        this.volumeInstanceRepository = volumeInstanceRepository;
        this.volumeInstanceStatusRepository = volumeInstanceStatusRepository;
    }

    public Collection<VolumeInstance> findByAccountUsername(String username) {
        return this.volumeInstanceRepository.findByAccountUsername(username);
    }

    public VolumeInstance findByAccountUsernameAndId(String username, Long id) {
        return this.volumeInstanceRepository.findByAccountUsernameAndId(username, id).orElseThrow(
                () -> new VolumeInstanceNotFoundException(username, id));
    }

    public VolumeInstance save(VolumeInstance volumeInstance) {
        return this.volumeInstanceRepository.save(volumeInstance);
    }

    public void delete(Long deploymentId) {
        this.volumeInstanceRepository.delete(deploymentId);
    }


    public VolumeInstanceStatus findStatusByVolumeIsntanceId(Long volumeInstanceId) {
        return this.volumeInstanceStatusRepository.findByVolumeInstanceId(volumeInstanceId).orElseThrow(
                () -> new VolumeInstanceStatusNotFoundException(volumeInstanceId));
    }

    public VolumeInstance findByReference(String reference) {
        return this.volumeInstanceRepository.findByReference(reference).orElseThrow(
                () -> new VolumeInstanceNotFoundException(reference));
    }


}
