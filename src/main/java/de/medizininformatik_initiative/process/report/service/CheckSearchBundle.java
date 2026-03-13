package de.medizininformatik_initiative.process.report.service;

import java.util.Objects;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.medizininformatik_initiative.process.report.ConstantsReport;
import de.medizininformatik_initiative.process.report.util.SearchQueryCheckService;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Variables;

public class CheckSearchBundle implements ServiceTask, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(CheckSearchBundle.class);

	private final SearchQueryCheckService searchQueryCheckService;

	public CheckSearchBundle(SearchQueryCheckService searchQueryCheckService)
	{
		this.searchQueryCheckService = searchQueryCheckService;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(searchQueryCheckService, "searchQueryCheckService");
	}

	@Override
	public void execute(ProcessPluginApi api, Variables variables)
	{
		Task task = variables.getStartTask();
		Target target = variables.getTarget();
		Bundle bundle = variables.getFhirResource(ConstantsReport.BPMN_EXECUTION_VARIABLE_REPORT_SEARCH_BUNDLE);

		logger.info("Checking downloaded search Bundle from HRP '{}' as part of Task with id '{}'",
				target.getOrganizationIdentifierValue(), task.getId());

		try
		{
			searchQueryCheckService.checkBundle(bundle);

			logger.info(
					"Search Bundle downloaded from HRP '{}' as part of Task with id '{}' contains only valid requests of type GET and valid search params {}",
					target.getOrganizationIdentifierValue(), task.getId(),
					searchQueryCheckService.getValidSearchParams());
		}
		catch (Exception exception)
		{
			logger.warn("Error while checking search Bundle from HRP '{}' in Task with id '{}' - {}",
					target.getOrganizationIdentifierValue(), task.getId(), exception.getMessage());
			throw new RuntimeException(
					"Error while checking search Bundle from HRP '" + target.getOrganizationIdentifierValue()
							+ "' in Task with id '" + task.getId() + "' - " + exception.getMessage(),
					exception);
		}
	}
}
