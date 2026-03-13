package de.medizininformatik_initiative.process.report.service;

import org.hl7.fhir.r4.model.Task;

import de.medizininformatik_initiative.process.report.ConstantsReport;
import de.medizininformatik_initiative.processes.common.util.ConstantsBase;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.client.dsf.DelayStrategy;
import dev.dsf.bpe.v2.service.MailService;
import dev.dsf.bpe.v2.variables.Variables;

public class HandleError implements ServiceTask
{
	public HandleError()
	{
	}

	@Override
	public void execute(ProcessPluginApi api, Variables variables)
	{
		Task task = variables.getStartTask();

		if (Task.TaskStatus.FAILED.equals(task.getStatus()))
		{
			sendMail(api.getMailService(), task, variables);
			api.getDsfClientProvider().getLocal().withRetry(ConstantsBase.DSF_CLIENT_RETRY_6_TIMES,
					DelayStrategy.constant(ConstantsBase.DSF_CLIENT_RETRY_INTERVAL_5MIN)).update(task);
		}
	}

	private void sendMail(MailService mailService, Task task, Variables variables)
	{
		String error = variables.getString(ConstantsReport.BPMN_EXECUTION_VARIABLE_REPORT_RECEIVE_ERROR_MESSAGE);
		String reportLocation = variables
				.getString(ConstantsReport.BPMN_EXECUTION_VARIABLE_REPORT_SEARCH_BUNDLE_RESPONSE_REFERENCE);

		String subject = "Error in process '" + ConstantsReport.PROCESS_NAME_FULL_REPORT_RECEIVE + "'";
		String message = "Could not download or insert new report with reference '" + reportLocation + "' in process '"
				+ ConstantsReport.PROCESS_NAME_FULL_REPORT_RECEIVE + "' from organization '"
				+ task.getRequester().getIdentifier().getValue() + "' in Task with id '" + task.getId() + "':\n"
				+ "- status code: " + ConstantsReport.CODESYSTEM_REPORT_STATUS_VALUE_RECEIVE_ERROR + "\n" + "- error: "
				+ (error == null ? "none" : error);

		mailService.send(subject, message);
	}
}
