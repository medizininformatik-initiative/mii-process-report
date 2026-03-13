package de.medizininformatik_initiative.process.report.fhir.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.List;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;
import de.medizininformatik_initiative.process.report.ReportProcessPluginDefinition;
import dev.dsf.bpe.v2.service.process.ProcessAuthorizationHelper;
import dev.dsf.bpe.v2.service.process.ProcessAuthorizationHelperImpl;
import dev.dsf.fhir.validation.ResourceValidator;
import dev.dsf.fhir.validation.ResourceValidatorImpl;
import dev.dsf.fhir.validation.ValidationSupportRule;

public class ActivityDefinitionProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(ActivityDefinitionProfileTest.class);

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(
			ReportProcessPluginDefinition.VERSION, ReportProcessPluginDefinition.RELEASE_DATE,
			List.of("dsf-activity-definition-2.0.0.xml", "dsf-extension-process-authorization-2.0.0.xml",
					"dsf-extension-process-authorization-organization-2.0.0.xml",
					"dsf-extension-process-authorization-organization-practitioner-2.0.0.xml",
					"dsf-extension-process-authorization-parent-organization-role-2.0.0.xml",
					"dsf-extension-process-authorization-parent-organization-role-practitioner-2.0.0.xml",
					"dsf-extension-process-authorization-practitioner-2.0.0.xml",
					"dsf-coding-process-authorization-local-all-2.0.0.xml",
					"dsf-coding-process-authorization-local-all-practitioner-2.0.0.xml",
					"dsf-coding-process-authorization-local-organization-2.0.0.xml",
					"dsf-coding-process-authorization-local-organization-practitioner-2.0.0.xml",
					"dsf-coding-process-authorization-local-parent-organization-role-2.0.0.xml",
					"dsf-coding-process-authorization-local-parent-organization-role-practitioner-2.0.0.xml",
					"dsf-coding-process-authorization-remote-all-2.0.0.xml",
					"dsf-coding-process-authorization-remote-parent-organization-role-2.0.0.xml",
					"dsf-coding-process-authorization-remote-organization-2.0.0.xml", "dsf-meta-2.0.0.xml"),
			List.of("dsf-organization-role-2.0.0.xml", "dsf-practitioner-role-2.0.0.xml",
					"dsf-process-authorization-2.0.0.xml", "dsf-read-access-tag-2.0.0.xml"),
			List.of("dsf-organization-role-2.0.0.xml", "dsf-practitioner-role-2.0.0.xml",
					"dsf-process-authorization-recipient-2.0.0.xml", "dsf-process-authorization-requester-2.0.0.xml",
					"dsf-read-access-tag-2.0.0.xml"));

	private final ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	private final ProcessAuthorizationHelper processAuthorizationHelper = new ProcessAuthorizationHelperImpl();

	@Test
	public void testAutostartValid() throws Exception
	{
		ActivityDefinition ad = validationRule
				.readActivityDefinition(Paths.get("src/main/resources/fhir/ActivityDefinition/report-autostart.xml"));

		ValidationResult result = resourceValidator.validate(ad);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());

		assertTrue(processAuthorizationHelper.isValid(ad, taskProfile -> true, practitionerRole -> true,
				orgIdentifier -> true, organizationRole -> true));
	}

	@Test
	public void testSendValid() throws Exception
	{
		ActivityDefinition ad = validationRule
				.readActivityDefinition(Paths.get("src/main/resources/fhir/ActivityDefinition/report-send.xml"));

		ValidationResult result = resourceValidator.validate(ad);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());

		assertTrue(processAuthorizationHelper.isValid(ad, taskProfile -> true, practitionerRole -> true,
				orgIdentifier -> true, organizationRole -> true));
	}

	@Test
	public void testReceiveValid() throws Exception
	{
		ActivityDefinition ad = validationRule
				.readActivityDefinition(Paths.get("src/main/resources/fhir/ActivityDefinition/report-receive.xml"));

		ValidationResult result = resourceValidator.validate(ad);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());

		assertTrue(processAuthorizationHelper.isValid(ad, taskProfile -> true, practitionerRole -> true,
				orgIdentifier -> true, organizationRole -> true));
	}
}
