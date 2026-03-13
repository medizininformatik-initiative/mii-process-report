package de.medizininformatik_initiative.process.report;

import java.util.List;
import java.util.Objects;

import org.hl7.fhir.r4.model.CapabilityStatement;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.ProcessPluginDeploymentListener;

public class ReportProcessPluginDeploymentListener implements ProcessPluginDeploymentListener, InitializingBean
{
	private final ProcessPluginApi api;

	private final String fhirStoreId;

	public ReportProcessPluginDeploymentListener(ProcessPluginApi api, String fhirStoreId)
	{
		this.api = api;
		this.fhirStoreId = fhirStoreId;
	}

	@Override
	public void afterPropertiesSet()
	{
		Objects.requireNonNull(api, "api");
		Objects.requireNonNull(fhirStoreId, fhirStoreId);
	}

	@Override
	public void onProcessesDeployed(List<String> activeProcesses)
	{
		if (activeProcesses.contains(ConstantsReport.PROCESS_NAME_FULL_REPORT_SEND))
		{
			CapabilityStatement conformance = api.getDsfClientProvider().getById(fhirStoreId)
					.orElseThrow(
							() -> new RuntimeException("DSF FHIR Client with ID '" + fhirStoreId + "' not configured"))
					.getConformance();

			Objects.requireNonNull(conformance,
					"Connection test for DSF FHIR Client with ID '" + fhirStoreId + "' failed - CapabilityStatement is null");
		}
	}
}
