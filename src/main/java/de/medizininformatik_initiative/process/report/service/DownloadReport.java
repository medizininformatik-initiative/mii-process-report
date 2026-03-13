package de.medizininformatik_initiative.process.report.service;

import java.util.List;
import java.util.Objects;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.medizininformatik_initiative.process.report.ConstantsReport;
import de.medizininformatik_initiative.process.report.util.ReportStatusGenerator;
import de.medizininformatik_initiative.processes.common.util.ConstantsBase;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.client.dsf.BasicDsfClient;
import dev.dsf.bpe.v2.client.dsf.DelayStrategy;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class DownloadReport implements ServiceTask, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadReport.class);

	private final ReportStatusGenerator statusGenerator;

	public DownloadReport(ReportStatusGenerator statusGenerator)
	{
		this.statusGenerator = statusGenerator;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(statusGenerator, "reportStatusGenerator");
	}

	@Override
	public void execute(ProcessPluginApi api, Variables variables)
	{
		Task task = variables.getStartTask();
		IdType reportReference = getReportReference(api, task);

		variables.setString(ConstantsReport.BPMN_EXECUTION_VARIABLE_REPORT_SEARCH_BUNDLE_RESPONSE_REFERENCE,
				reportReference.getValue());

		logger.info("Downloading report with id '{}' from organization '{}' referenced in Task with id '{}'",
				reportReference.getValue(), task.getRequester().getIdentifier().getValue(), task.getId());

		try
		{
			Bundle reportBundle = downloadReportBundle(api, reportReference);
			variables.setFhirResource(ConstantsReport.BPMN_EXECUTION_VARIABLE_REPORT_SEARCH_BUNDLE, reportBundle);
		}
		catch (Exception exception)
		{
			task.setStatus(Task.TaskStatus.FAILED);
			task.addOutput(statusGenerator.createReportStatusOutput(
					ConstantsReport.CODESYSTEM_REPORT_STATUS_VALUE_RECEIVE_ERROR, "Download report failed"));
			variables.updateTask(task);

			logger.warn(
					"Downloading report with id '{}' from organization '{}' referenced in Task with id '{}' failed - {}",
					reportReference.getValue(), task.getRequester().getIdentifier().getValue(), task.getId(),
					exception.getMessage());

			throw new ErrorBoundaryEvent(ConstantsReport.BPMN_EXECUTION_VARIABLE_REPORT_RECEIVE_ERROR,
					"Download report failed - " + exception.getMessage());
		}
	}

	private IdType getReportReference(ProcessPluginApi api, Task task)
	{
		List<String> reportReferences = api.getTaskHelper()
				.getInputParameterValues(task, ConstantsReport.CODESYSTEM_REPORT,
						ConstantsReport.CODESYSTEM_REPORT_VALUE_SEARCH_BUNDLE_RESPONSE_REFERENCE, Reference.class)
				.filter(Reference::hasReference).map(Reference::getReference).toList();

		if (reportReferences.isEmpty())
			throw new IllegalArgumentException("No report reference present in Task with id '" + task.getId() + "'");

		if (reportReferences.size() > 1)
			logger.warn("Found {} report references in task with id '{}', using only the first",
					reportReferences.size(), task.getId());

		return new IdType(reportReferences.get(0));
	}

	private Bundle downloadReportBundle(ProcessPluginApi api, IdType reportReference)
	{
		BasicDsfClient client = api.getDsfClientProvider().getByEndpointUrl(reportReference.getBaseUrl()).withRetry(
				ConstantsBase.DSF_CLIENT_RETRY_6_TIMES,
				DelayStrategy.constant(ConstantsBase.DSF_CLIENT_RETRY_INTERVAL_5MIN));

		if (reportReference.hasVersionIdPart())
			return client.read(Bundle.class, reportReference.getIdPart(), reportReference.getVersionIdPart());
		else
			return client.read(Bundle.class, reportReference.getIdPart());
	}
}
