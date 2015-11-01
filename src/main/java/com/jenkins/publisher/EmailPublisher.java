package com.jenkins.publisher;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class EmailPublisher extends Notifier {

	private final String emailId;
	private static Properties properties = new Properties();
	static {
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
	}

	@DataBoundConstructor
	public EmailPublisher(final String emailId) {
		this.emailId = emailId;
	}

	public String getEmailId() {
		return emailId;
	}

	@Override
	public boolean perform(
			@SuppressWarnings("rawtypes") final AbstractBuild build,
			final Launcher launcher, final BuildListener listener) {
		// logic to be executed by plugin
		try {
			// logger which prints on job 'Console Output'
			listener.getLogger().println("Starting Post Build Action");
			sendMail();
		} catch (Exception e) {
			listener.getLogger().printf("Error Occurred : %s ", e);
		}
		listener.getLogger().println("Finished Post Build Action");
		return true;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		/**
		 * Global configuration information variables. If you don't want fields
		 * to be persisted, use <tt>transient</tt>.
		 */
		private String username;
		private String password;

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}

		/**
		 * In order to load the persisted global configuration, you have to call
		 * load() in the constructor.
		 */
		public DescriptorImpl() {
			load();
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData)
				throws FormException {
			// To persist global configuration information, set that to
			// properties and call save().
			username = formData.getString("username");
			password = formData.getString("password");
			save();
			return super.configure(req, formData);
		}

		@Override
		public boolean isApplicable(
				@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
			// Indicates that this builder can be used with all kinds of project
			// types.
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Send Email";
		}
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	private void sendMail() throws Exception {
		final String username = getDescriptor().getUsername();
		final String password = getDescriptor().getPassword();
		Session session = Session.getInstance(properties,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress("aggarwalarpit.89@gmail.com"));
		message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(emailId));
		message.setSubject("Jenkins Post Build Mail");
		message.setText("Jenkins Post Build Mail");
		Transport.send(message);
	}
}
