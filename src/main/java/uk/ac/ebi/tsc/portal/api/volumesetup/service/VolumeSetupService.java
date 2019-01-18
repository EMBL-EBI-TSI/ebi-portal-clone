package uk.ac.ebi.tsc.portal.api.volumesetup.service;

import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetup;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetupRepository;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Service
public class VolumeSetupService {

    private final VolumeSetupRepository volumeSetupRepository;

    @Autowired
    public VolumeSetupService(VolumeSetupRepository volumeSetupRepository) {
        this.volumeSetupRepository = volumeSetupRepository;
    }

    public Collection<VolumeSetup> findByAccountUsername(String username) {
        return volumeSetupRepository.findByAccountUsername(username);
    }

    public VolumeSetup findByAccountUsernameAndName(String username, String name) {
        return volumeSetupRepository.findByAccountUsernameAndName(username, name).orElseThrow(
                () -> new VolumeSetupNotFoundException(name));
    }

    public VolumeSetup save(VolumeSetup volumeSetup) {
        return this.volumeSetupRepository.save(volumeSetup);
    }

    public Collection<VolumeSetup> findAll() {
        return this.volumeSetupRepository.findAll();
    }

    public VolumeSetup findById(Long setupId) {
        return this.volumeSetupRepository.findById(setupId).orElseThrow(
                () -> new VolumeSetupNotFoundException(setupId));
    }

    public void delete(Long setupId) {
        this.volumeSetupRepository.findById(setupId).orElseThrow(
                () -> new VolumeSetupNotFoundException(setupId));
        this.volumeSetupRepository.delete(setupId);
    }
}
