package de.medizininformatik_initiative.process.report.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import de.medizininformatik_initiative.process.report.ReportProcessPluginDeploymentListener;
import de.medizininformatik_initiative.process.report.message.SendReceipt;
import de.medizininformatik_initiative.process.report.message.SendReport;
import de.medizininformatik_initiative.process.report.message.StartSendReport;
import de.medizininformatik_initiative.process.report.service.CheckSearchBundle;
import de.medizininformatik_initiative.process.report.service.CreateReport;
import de.medizininformatik_initiative.process.report.service.DownloadReport;
import de.medizininformatik_initiative.process.report.service.DownloadSearchBundle;
import de.medizininformatik_initiative.process.report.service.HandleError;
import de.medizininformatik_initiative.process.report.service.InsertReport;
import de.medizininformatik_initiative.process.report.service.LogDryRun;
import de.medizininformatik_initiative.process.report.service.SelectTargetDic;
import de.medizininformatik_initiative.process.report.service.SelectTargetHrp;
import de.medizininformatik_initiative.process.report.service.SetTimer;
import de.medizininformatik_initiative.process.report.service.StoreReceipt;
import de.medizininformatik_initiative.process.report.util.ReportStatusGenerator;
import de.medizininformatik_initiative.process.report.util.SearchQueryCheckService;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.documentation.ProcessDocumentation;

@Configuration
public class ReportConfig
{
	@Autowired
	private ProcessPluginApi api;

	@ProcessDocumentation(processNames = {
			"medizininformatik-initiativede_reportSend" }, description = "The identifier of the HRP which should receive the report", recommendation = "Only configure if more than one HRP exists in your network", example = "forschen-fuer-gesundheit.de")
	@Value("${de.medizininformatik.initiative.report.dic.hrp.identifier:#{null}}")
	private String hrpIdentifier;

	@ProcessDocumentation(required = true, processNames = {
			"medizininformatik-initiativede_reportSend" }, description = "The ID of a DIC FHIR server from the main DSF configuration as 'DSF FHIR Client'", example = "dic-fhir-store")
	@Value("${de.medizininformatik.initiative.report.dic.fhir.server.id:#{null}}")
	private String fhirStoreId;

	@ProcessDocumentation(processNames = {
			"medizininformatik-initiativede_reportSend" }, description = "To receive e-mails as dic, set to `true`")
	@Value("${de.medizininformatik.initiative.report.dic.email.enabled:false}")
	private boolean dicEmailEnabled;

	@ProcessDocumentation(processNames = {
			"medizininformatik-initiativede_reportSend" }, description = "The period the process waits to receive the status from the HRP, must be an ISO 8601 time duration pattern")
	@Value("${de.medizininformatik.initiative.report.dic.status.timer.interval:PT45M}")
	private String statusTimerInterval;

	@ProcessDocumentation(processNames = {
			"medizininformatik-initiativede_reportReceive" }, description = "To receive e-mails as hrp, set to `true`")
	@Value("${de.medizininformatik.initiative.report.hrp.email.enabled:false}")
	private boolean hrpEmailEnabled;

	// all Processes

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	public ReportProcessPluginDeploymentListener reportProcessPluginDeploymentListener()
	{
		return new ReportProcessPluginDeploymentListener(api, fhirStoreId);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public ReportStatusGenerator reportStatusGenerator()
	{
		return new ReportStatusGenerator();
	}

	// reportAutostart Process

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SetTimer setTimer()
	{
		return new SetTimer();
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StartSendReport startSendReport()
	{
		return new StartSendReport();
	}

	// reportSend Process

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SelectTargetHrp selectTargetHrp()
	{
		return new SelectTargetHrp(statusTimerInterval, hrpIdentifier);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DownloadSearchBundle downloadSearchBundle()
	{
		return new DownloadSearchBundle(reportStatusGenerator());
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public CheckSearchBundle checkSearchBundle()
	{
		return new CheckSearchBundle(searchQueryCheckService());
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	public SearchQueryCheckService searchQueryCheckService()
	{
		return new SearchQueryCheckService();
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public CreateReport createReport()
	{
		return new CreateReport(fhirStoreId);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public LogDryRun logDryRun()
	{
		return new LogDryRun(reportStatusGenerator(), dicEmailEnabled);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SendReport sendReport()
	{
		return new SendReport(api, reportStatusGenerator());
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StoreReceipt storeReceipt()
	{
		return new StoreReceipt(reportStatusGenerator(), dicEmailEnabled);
	}

	// reportReceive Process

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DownloadReport downloadReport()
	{
		return new DownloadReport(reportStatusGenerator());
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public InsertReport insertReport()
	{
		return new InsertReport(reportStatusGenerator(), hrpEmailEnabled);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public HandleError handleError()
	{
		return new HandleError(hrpEmailEnabled);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SelectTargetDic selectTargetDic()
	{
		return new SelectTargetDic();
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SendReceipt sendReceipt()
	{
		return new SendReceipt(api, reportStatusGenerator());
	}
}
