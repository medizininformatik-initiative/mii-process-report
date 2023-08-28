package de.medizininformatik_initiative.process.report.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import de.medizininformatik_initiative.process.report.ReportProcessPluginDefinition;
import de.medizininformatik_initiative.process.report.ReportProcessPluginDeploymentStateListener;
import de.medizininformatik_initiative.process.report.message.SendReceipt;
import de.medizininformatik_initiative.process.report.message.SendReport;
import de.medizininformatik_initiative.process.report.message.StartSendReport;
import de.medizininformatik_initiative.process.report.service.CheckSearchBundle;
import de.medizininformatik_initiative.process.report.service.CreateReport;
import de.medizininformatik_initiative.process.report.service.DownloadReport;
import de.medizininformatik_initiative.process.report.service.DownloadSearchBundle;
import de.medizininformatik_initiative.process.report.service.InsertReport;
import de.medizininformatik_initiative.process.report.service.SelectTargetDic;
import de.medizininformatik_initiative.process.report.service.SelectTargetHrp;
import de.medizininformatik_initiative.process.report.service.SetTimer;
import de.medizininformatik_initiative.process.report.service.StoreReceipt;
import de.medizininformatik_initiative.process.report.util.ReportStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.ProcessPluginDeploymentStateListener;
import dev.dsf.bpe.v1.service.MailService;

@Configuration
public class ReportConfig
{
	@Autowired
	private ProcessPluginApi api;

	@Autowired
	private MailService mailService;

	@Autowired
	private FhirClientConfig fhirClientConfig;

	// all Processes

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public ProcessPluginDeploymentStateListener reportProcessPluginDeploymentStateListener()
	{
		return new ReportProcessPluginDeploymentStateListener(fhirClientConfig.fhirClientFactory());
	}

	// reportAutostart Process

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SetTimer setTimer()
	{
		return new SetTimer(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StartSendReport startSendReport()
	{
		return new StartSendReport(api);
	}

	// reportSend Process

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SelectTargetHrp selectTargetHrp()
	{
		return new SelectTargetHrp(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DownloadSearchBundle downloadSearchBundle()
	{
		return new DownloadSearchBundle(api, reportStatusGenerator(), fhirClientConfig.dataLogger());
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public CheckSearchBundle checkSearchBundle()
	{
		return new CheckSearchBundle(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public CreateReport createReport()
	{
		String resourceVersion = new ReportProcessPluginDefinition().getResourceVersion();
		return new CreateReport(api, resourceVersion, fhirClientConfig.fhirClientFactory(),
				fhirClientConfig.dataLogger());
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public ReportStatusGenerator reportStatusGenerator()
	{
		return new ReportStatusGenerator();
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
		return new StoreReceipt(api, reportStatusGenerator(), mailService);
	}

	// reportReceive Process

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DownloadReport downloadReport()
	{
		return new DownloadReport(api, reportStatusGenerator());
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public InsertReport insertReport()
	{
		return new InsertReport(api, reportStatusGenerator(), mailService);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SelectTargetDic selectTargetDic()
	{
		return new SelectTargetDic(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SendReceipt sendReceipt()
	{
		return new SendReceipt(api, reportStatusGenerator());
	}
}