package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo;

import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import uk.ac.ebi.tsc.portal.api.account.repo.Account;

@Entity
@Table(name="cloud_provider_params_copy")
public class CloudProviderParamsCopy {
	
	static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String name;

    public String cloudProvider;

    @ManyToOne
    @JoinColumn (name = "account_id",referencedColumnName = "id")
    public Account account;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "cloudProviderParametersCopy", cascade = CascadeType.ALL, orphanRemoval=true, fetch = FetchType.EAGER)
    public Collection<CloudProviderParamsCopyField> fields;

    @NotNull
    @Column(name="cloud_provider_params_reference")
    public String cloudProviderParametersReference;

    public CloudProviderParamsCopy() { 
    }
    
    public CloudProviderParamsCopy(String name, String cloudProvider, Account account) {
        this.name = name;
        this.cloudProvider = cloudProvider;
        this.account = account;
        this.fields = new LinkedList<>();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCloudProvider() {
		return cloudProvider;
	}

	public void setCloudProvider(String cloudProvider) {
		this.cloudProvider = cloudProvider;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Collection<CloudProviderParamsCopyField> getFields() {
		return fields;
	}

	public void setFields(Collection<CloudProviderParamsCopyField> fields) {
		this.fields = fields;
	}

	public String getCloudProviderParametersReference() {
		return cloudProviderParametersReference;
	}

	public void setCloudProviderParametersReference(String cloudProviderParametersReference) {
		this.cloudProviderParametersReference = cloudProviderParametersReference;
	}
    
}
