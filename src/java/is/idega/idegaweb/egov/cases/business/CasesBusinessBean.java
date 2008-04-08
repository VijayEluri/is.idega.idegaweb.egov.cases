/*
 * $Id$ Created on Oct 30, 2005
 * 
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseCategoryHome;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.CaseTypeHome;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import is.idega.idegaweb.egov.cases.data.GeneralCaseHome;
import is.idega.idegaweb.egov.cases.util.CaseConstants;
import is.idega.idegaweb.egov.message.business.CommuneMessageBusiness;

import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.business.CaseBusinessBean;
import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseLog;
import com.idega.block.process.data.CaseStatus;
import com.idega.block.text.data.LocalizedText;
import com.idega.block.text.data.LocalizedTextHome;
import com.idega.business.IBORuntimeException;
import com.idega.core.file.data.ICFile;
import com.idega.core.file.data.ICFileHome;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.text.Name;

public class CasesBusinessBean extends CaseBusinessBean implements CaseBusiness, CasesBusiness {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -1807320113180412800L;

	protected String getBundleIdentifier() {
		return CaseConstants.IW_BUNDLE_IDENTIFIER;
	}

	private CommuneMessageBusiness getMessageBusiness() {
		try {
			return (CommuneMessageBusiness) this.getServiceInstance(CommuneMessageBusiness.class);
		}
		catch (RemoteException e) {
			throw new IBORuntimeException(e.getMessage());
		}
	}

	private UserBusiness getUserBusiness() {
		try {
			return (UserBusiness) this.getServiceInstance(UserBusiness.class);
		}
		catch (RemoteException e) {
			throw new IBORuntimeException(e.getMessage());
		}
	}

	private GeneralCaseHome getGeneralCaseHome() {
		try {
			return (GeneralCaseHome) IDOLookup.getHome(GeneralCase.class);
		}
		catch (IDOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	private CaseCategoryHome getCaseCategoryHome() {
		try {
			return (CaseCategoryHome) IDOLookup.getHome(CaseCategory.class);
		}
		catch (IDOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	private CaseTypeHome getCaseTypeHome() {
		try {
			return (CaseTypeHome) IDOLookup.getHome(CaseType.class);
		}
		catch (IDOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	public ICFile getAttachment(Object attachmentPK) {
		try {
			return ((ICFileHome) IDOLookup.getHome(ICFile.class)).findByPrimaryKey(attachmentPK);
		}
		catch (IDOLookupException e) {
			e.printStackTrace();
		}
		catch (FinderException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String getLocalizedCaseDescription(Case theCase, Locale locale) {
		// Should not need to check the preferred locale for a user for now since it isn't used in sending messages
		try {
			GeneralCase genCase = getGeneralCase(theCase.getPrimaryKey());
			CaseCategory type = genCase.getCaseCategory();
			Object[] arguments = { type.getLocalizedCategoryName(locale) };

			IWResourceBundle iwrb = getBundle().getResourceBundle(locale);
			return MessageFormat.format(iwrb.getLocalizedString((genCase.getType() != null ? genCase.getType() + "." : "") + "case_code_key." + theCase.getCode(), theCase.getCode()), arguments);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return super.getLocalizedCaseDescription(theCase, locale);
		}
	}

	public String getLocalizedCaseStatusDescription(CaseStatus status, Locale locale) {
		return super.getLocalizedCaseStatusDescription(null, status, locale);
	}

	public String getLocalizedCaseStatusDescription(Case theCase, CaseStatus status, Locale locale) {
		// Should not need to check the preferred locale for a user for now since the only method that uses it,handleCase
		// passes the preferred locale in.
		try {
			GeneralCase genCase = getGeneralCase(theCase.getPrimaryKey());
			IWResourceBundle iwrb = getBundle().getResourceBundle(locale);
			return iwrb.getLocalizedString((genCase.getType() != null ? genCase.getType() + "." : "") + "case_status_key." + status.getStatus(), status.getStatus());
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return super.getLocalizedCaseStatusDescription(theCase, status, locale);
		}
	}

	public GeneralCase getGeneralCase(Object casePK) throws FinderException {
		return getGeneralCaseHome().findByPrimaryKey(new Integer(casePK.toString()));
	}

	public Collection getOpenCases(Collection groups) {
		try {
			String[] statuses = { getCaseStatusOpen().getStatus(), getCaseStatusReview().getStatus() };
			return getGeneralCaseHome().findAllByGroupAndStatuses(groups, statuses);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	public Collection getMyCases(User handler) {
		try {
			String[] statuses = { getCaseStatusPending().getStatus(), getCaseStatusWaiting().getStatus() };
			return getGeneralCaseHome().findAllByHandlerAndStatuses(handler, statuses);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	public Collection getClosedCases(Collection groups) {
		try {
			String[] statuses = { getCaseStatusInactive().getStatus(), getCaseStatusReady().getStatus() };
			return getGeneralCaseHome().findAllByGroupAndStatuses(groups, statuses);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	public Collection getCasesByUsers(Collection users) {
		try {
			return getGeneralCaseHome().findAllByUsers(users);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}
	
	public Collection getCasesByMessage(String message) {
		try {
			return getGeneralCaseHome().findAllByMessage(message);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}	

	public Collection getCasesByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Date fromDate, Date toDate, Boolean anonymous) {
		try {
			return getGeneralCaseHome().findByCriteria(parentCategory, category, type, status, fromDate, toDate, anonymous);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	public CaseCategory getCaseCategory(Object caseCategoryPK) throws FinderException {
		return getCaseCategoryHome().findByPrimaryKey(new Integer(caseCategoryPK.toString()));
	}

	public Collection getAllCaseCategories() {
		try {
			return getCaseCategoryHome().findAll();
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	public Collection getCaseCategories() {
		try {
			return getCaseCategoryHome().findAllTopLevelCategories();
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	public Collection getSubCategories(CaseCategory category) {
		try {
			return getCaseCategoryHome().findAllSubCategories(category);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	public Collection getCaseLogs(GeneralCase theCase) {
		try {
			Collection logs = getCaseLogsByCase(theCase);
			Collection returner = new ArrayList(logs);
			User owner = theCase.getOwner();

			Iterator iter = logs.iterator();
			while (iter.hasNext()) {
				CaseLog log = (CaseLog) iter.next();
				if (log.getPerformer().equals(owner)) {
					returner.remove(log);
				}
				else if (log.getComment() == null || log.getComment().length() == 0) {
					returner.remove(log);
				}
			}

			return returner;
		}
		catch (FinderException e) {
			return new ArrayList();
		}
	}

	public void removeCaseCategory(Object caseCategoryPK) throws FinderException, RemoveException {
		getCaseCategory(caseCategoryPK).remove();
	}

	public CaseType getCaseType(Object caseTypePK) throws FinderException {
		return getCaseTypeHome().findByPrimaryKey(new Integer(caseTypePK.toString()));
	}

	public Collection getCaseTypes() {
		try {
			return getCaseTypeHome().findAll();
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	public CaseType getFirstAvailableCaseType() {
		try {
			return getCaseTypeHome().findFirstType();
		}
		catch (FinderException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void removeCaseType(Object caseTypePK) throws FinderException, RemoveException {
		getCaseType(caseTypePK).remove();
	}

	/**
	 * The iwrb is the users preferred locale
	 */
	public GeneralCase storeGeneralCase(User sender, Object caseCategoryPK, Object caseTypePK, Object attachmentPK, String regarding, String message, String type, boolean isPrivate, IWResourceBundle iwrb) throws CreateException {
		Locale locale = iwrb.getLocale();
		// TODO use users preferred language!!

		GeneralCase theCase = getGeneralCaseHome().create();
		CaseCategory category = null;
		try {
			category = getCaseCategory(caseCategoryPK);
		}
		catch (FinderException fe) {
			throw new CreateException("Trying to store a case with case category that has no relation to a group");
		}
		CaseType caseType = null;
		try {
			caseType = getCaseType(caseTypePK);
		}
		catch (FinderException fe) {
			throw new CreateException("Trying to store a case with case type that does not exist");
		}
		ICFile attachment = null;
		if (attachmentPK != null) {
			attachment = getAttachment(attachmentPK);
		}

		Group handlerGroup = category.getHandlerGroup();

		theCase.setCaseCategory(category);
		theCase.setCaseType(caseType);
		theCase.setOwner(sender);
		theCase.setHandler(handlerGroup);
		theCase.setSubject(regarding);
		theCase.setMessage(message);
		theCase.setAttachment(attachment);
		theCase.setType(type);
		theCase.setAsPrivate(isPrivate);
		changeCaseStatus(theCase, getCaseStatusOpen().getStatus(), sender, (Group) null);

		try {
			String prefix = (type != null ? type + "." : "");

			String subject = iwrb.getLocalizedString(prefix + "case_sent_subject", "A new case sent in");
			String body = null;
			if (sender != null) {
				Name name = new Name(sender.getFirstName(), sender.getMiddleName(), sender.getLastName());

				Object[] arguments = { name.getName(locale), theCase.getCaseCategory().getLocalizedCategoryName(locale), message };
				body = MessageFormat.format(iwrb.getLocalizedString(prefix + "case_sent_body", "A new case has been sent in by {0} in case category {1}. \n\nThe case is as follows:\n{2}"), arguments);
			}
			else {
				Object[] arguments = { iwrb.getLocalizedString("anonymous", "Anonymous"), theCase.getCaseCategory().getLocalizedCategoryName(locale), message };
				body = MessageFormat.format(iwrb.getLocalizedString(prefix + "anonymous_case_sent_body", "An anonymous case has been sent in case category {1}. \n\nThe case is as follows:\n{2}"), arguments);
			}

			Collection handlers = getUserBusiness().getUsersInGroup(handlerGroup);
			Iterator iter = handlers.iterator();
			while (iter.hasNext()) {
				User handler = (User) iter.next();
				sendMessage(theCase, handler, sender, subject, body);
			}

			if (sender != null) {
				Object[] arguments2 = { theCase.getCaseCategory().getLocalizedCategoryName(locale) };
				subject = iwrb.getLocalizedString(prefix + "case_sent_confirmation_subject", "A new case sent in");
				body = MessageFormat.format(iwrb.getLocalizedString(prefix + "case_sent_confirmation_body", "Your case with case category {0} has been received and will be processed."), arguments2);

				sendMessage(theCase, sender, null, subject, body);
			}
		}
		catch (RemoteException e) {
			throw new IBORuntimeException(e);
		}
		
		return theCase;
	}

	public void allocateCase(GeneralCase theCase, Object caseCategoryPK, Object caseTypePK, User user, String message, User performer, IWContext iwc) {
		boolean hasChanges = false;
		try {
			CaseCategory category = caseCategoryPK != null ? getCaseCategory(caseCategoryPK) : null;
			Group handlerGroup = category != null ? category.getHandlerGroup() : null;
			if (category != null && !category.equals(theCase.getCaseCategory())) {
				theCase.setCaseCategory(category);
				theCase.setHandler(handlerGroup);
				hasChanges = true;
			}

			CaseType type = caseTypePK != null ? getCaseType(caseTypePK) : null;
			if (type != null && !theCase.getCaseType().equals(type)) {
				theCase.setCaseType(type);
				hasChanges = true;
			}

			if (hasChanges) {
				theCase.store();
			}
		}
		catch (FinderException fe) {
			fe.printStackTrace();
		}

		takeCase(theCase, user, iwc, performer, hasChanges);

		Name name = new Name(performer.getFirstName(), performer.getMiddleName(), performer.getLastName());
		Object[] arguments = { name.getName(iwc.getCurrentLocale()), theCase.getCaseCategory().getLocalizedCategoryName(iwc.getApplicationSettings().getDefaultLocale()), theCase.getPrimaryKey().toString(), message };

		String subject = getLocalizedString("case_allocation_subject", "A case has been allocated to you", iwc.getApplicationSettings().getDefaultLocale());
		String body = MessageFormat.format(getLocalizedString("case_allocation_body", "{0} has allocated case nr. {2} in the category {1} to you with the following message:\n{3}", iwc.getApplicationSettings().getDefaultLocale()), arguments);
		sendMessage(theCase, user, performer, subject, body);
	}

	public void handleCase(Object casePK, Object caseCategoryPK, Object caseTypePK, String status, User performer, String reply, IWContext iwc) throws FinderException {
		GeneralCase theCase = getGeneralCase(casePK);
		CaseCategory category = getCaseCategory(caseCategoryPK);
		CaseType type = getCaseType(caseTypePK);

		handleCase(theCase, category, type, status, performer, reply, iwc);
	}

	public void handleCase(GeneralCase theCase, CaseCategory category, CaseType type, String status, User performer, String reply, IWContext iwc) {
		theCase.setReply(reply);

		boolean isSameCategory = category.equals(theCase.getCaseCategory());
		theCase.setCaseCategory(category);

		Group handlerGroup = category.getHandlerGroup();
		boolean isInGroup = performer.hasRelationTo(handlerGroup);
		theCase.setHandler(handlerGroup);

		if (!isInGroup) {
			theCase.setHandledBy(null);
			status = getCaseStatusOpen().getStatus();
		}
		else {
			theCase.setHandledBy(performer);
		}

		if (!isSameCategory) {
			String prefix = (theCase.getType() != null ? theCase.getType() + "." : "");
			User sender = theCase.getOwner();

			String subject = getLocalizedString(prefix + "case_sent_subject", "A new case sent in", iwc.getApplicationSettings().getDefaultLocale());
			String body = null;
			if (sender != null) {
				Name name = new Name(sender.getFirstName(), sender.getMiddleName(), sender.getLastName());

				Object[] arguments = { name.getName(iwc.getCurrentLocale()), theCase.getCaseCategory().getLocalizedCategoryName(iwc.getApplicationSettings().getDefaultLocale()), theCase.getMessage() };
				body = MessageFormat.format(getLocalizedString(prefix + "case_sent_body", "A new case has been sent in by {0} in case category {1}. \n\nThe case is as follows:\n{2}", iwc.getApplicationSettings().getDefaultLocale()), arguments);
			}
			else {
				Object[] arguments = { getLocalizedString("anonymous", "Anonymous", iwc.getApplicationSettings().getDefaultLocale()), theCase.getCaseCategory().getLocalizedCategoryName(iwc.getApplicationSettings().getDefaultLocale()), theCase.getMessage() };
				body = MessageFormat.format(getLocalizedString(prefix + "anonymous_case_sent_body", "An anonymous case has been sent in case category {1}. \n\nThe case is as follows:\n{2}", iwc.getApplicationSettings().getDefaultLocale()), arguments);
			}

			try {
				Collection handlers = getUserBusiness().getUsersInGroup(handlerGroup);

				Iterator iter = handlers.iterator();
				while (iter.hasNext()) {
					User handler = (User) iter.next();
					sendMessage(theCase, handler, sender, subject, body);
				}
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		theCase.setCaseType(type);

		changeCaseStatus(theCase, status, reply, performer, (Group) null, true);

		User owner = theCase.getOwner();
		if (owner != null && isInGroup) {
			IWResourceBundle iwrb = getIWResourceBundleForUser(owner, iwc);
			Locale locale = iwrb.getLocale();
			String prefix = theCase.getType() != null ? theCase.getType() + "." : "";

			Object[] arguments = { theCase.getCaseCategory().getLocalizedCategoryName(locale), theCase.getCaseType().getName(), performer.getName(), reply, getLocalizedCaseStatusDescription(theCase, getCaseStatus(status), locale) };
			String subject = getLocalizedString(prefix + "case_handled_subject", "Your case has been handled", locale);
			String body = MessageFormat.format(getLocalizedString(prefix + "case_handled_body", "Your case with category {0} and type {1} has been handled by {2}.  The reply was as follows:\n\n{3}", locale), arguments);

			sendMessage(theCase, owner, performer, subject, body);
		}
	}

	public void takeCase(Object casePK, User performer, IWContext iwc) throws FinderException {
		GeneralCase theCase = getGeneralCase(casePK);
		takeCase(theCase, performer, iwc);
	}

	public void takeCase(GeneralCase theCase, User performer, IWContext iwc) {
		takeCase(theCase, performer, iwc, performer, false);
	}

	public void takeCase(GeneralCase theCase, User user, IWContext iwc, User performer, boolean hasChanges) {
		theCase.setHandledBy(user);

		changeCaseStatus(theCase, getCaseStatusPending().getStatus(), user, (Group) null);

		User owner = theCase.getOwner();

		IWResourceBundle iwrb = this.getIWResourceBundleForUser(owner, iwc);

		if (owner != null) {
			String prefix = theCase.getType() != null ? theCase.getType() + "." : "";

			if (hasChanges) {
				Name name = new Name(performer.getFirstName(), performer.getMiddleName(), performer.getLastName());
				Object[] arguments2 = { name.getName(iwc.getCurrentLocale()), theCase.getCaseCategory().getLocalizedCategoryName(iwc.getApplicationSettings().getDefaultLocale()), theCase.getPrimaryKey().toString() };

				String subject = getLocalizedString(prefix + "case_changed_subject", "Your case has been changed", iwc.getApplicationSettings().getDefaultLocale());
				String body = MessageFormat.format(getLocalizedString(prefix + "case_changed_body", "{0} has changed case nr. {2} to the category {1}", iwc.getApplicationSettings().getDefaultLocale()), arguments2);
				sendMessage(theCase, owner, performer, subject, body);
			}

			Object[] arguments = { theCase.getCaseCategory().getLocalizedCategoryName(iwrb.getLocale()), theCase.getCaseType().getName(), user.getName() };
			String subject = iwrb.getLocalizedString(prefix + "case_taken_subject", "Your case has been taken");
			String body = MessageFormat.format(iwrb.getLocalizedString(prefix + "case_taken_body", "Your case with category {0} and type {1} has been put into process by {2}"), arguments);

			sendMessage(theCase, owner, user, subject, body);
		}
	}

	public void reactivateCase(GeneralCase theCase, User performer, IWContext iwc) throws FinderException {
		theCase.setHandledBy(performer);

		changeCaseStatus(theCase, getCaseStatusPending().getStatus(), performer, (Group) null);

		User owner = theCase.getOwner();
		if (owner != null) {
			IWResourceBundle iwrb = this.getIWResourceBundleForUser(owner, iwc);

			String prefix = theCase.getType() != null ? theCase.getType() + "." : "";
			Object[] arguments = { theCase.getCaseCategory().getLocalizedCategoryName(iwrb.getLocale()), theCase.getCaseType().getName(), performer.getName() };
			String subject = iwrb.getLocalizedString(prefix + "case_reactivated_subject", "Your case has been reactivated");
			String body = MessageFormat.format(iwrb.getLocalizedString(prefix + "case_reactivated_body", "Your case with category {0} and type {1} has been reactivated by {2}"), arguments);

			sendMessage(theCase, owner, performer, subject, body);
		}
	}

	public void reviewCase(GeneralCase theCase, User performer, IWContext iwc) throws FinderException {
		CaseCategory category = theCase.getCaseCategory();
		Group handlerGroup = category.getHandlerGroup();

		changeCaseStatus(theCase, getCaseStatusReview().getStatus(), performer, (Group) null);

		User owner = theCase.getOwner();
		try {
			String prefix = theCase.getType() != null ? theCase.getType() + "." : "";
			IWResourceBundle iwrb = this.getIWResourceBundleForUser(owner, iwc);

			Name name = new Name(owner.getFirstName(), owner.getMiddleName(), owner.getLastName());
			Object[] arguments = { name.getName(iwrb.getLocale()), theCase.getCaseCategory().getLocalizedCategoryName(iwrb.getLocale()), theCase.getMessage() };
			String subject = iwrb.getLocalizedString(prefix + "case_review_handler_subject", "A case sent for review");
			String body = MessageFormat.format(iwrb.getLocalizedString(prefix + "case_review_handler_body", "A case has been sent in for review by {0} in case category {1}. \n\nThe case is as follows:\n{2}"), arguments);

			Collection handlers = getUserBusiness().getUsersInGroup(handlerGroup);
			Iterator iter = handlers.iterator();
			while (iter.hasNext()) {
				User handler = (User) iter.next();
				sendMessage(theCase, handler, owner, subject, body);
			}

			if (owner != null) {
				Object[] args = { theCase.getCaseCategory().getLocalizedCategoryName(iwrb.getLocale()), theCase.getCaseType().getName(), performer.getName() };
				subject = iwrb.getLocalizedString(prefix + "case_review_subject", "Your case has been sent for review");
				body = MessageFormat.format(iwrb.getLocalizedString(prefix + "case_review_body", "Your case with category {0} and type {1} has been sent for review"), args);

				sendMessage(theCase, owner, performer, subject, body);
			}
		}
		catch (RemoteException e) {
			throw new IBORuntimeException(e);
		}
	}

	public void sendReminder(GeneralCase theCase, User receiver, User sender, String message, IWContext iwc) {
		IWResourceBundle iwrb = this.getIWResourceBundleForUser(receiver, iwc);

		Object[] args = { theCase.getPrimaryKey().toString(), sender.getName(), message };
		String subject = iwrb.getLocalizedString("case_reminder_subject", "You have received a reminder for a case");
		String body = MessageFormat.format(iwrb.getLocalizedString("case_reminder_body", "{1} has sent you a reminder for case nr. {0} with the following message:\n{2}"), args);

		sendMessage(theCase, receiver, sender, subject, body);
	}

	public CaseCategory storeCaseCategory(Object caseCategoryPK, Object parentCaseCategoryPK, String name, String description, Object groupPK, int localeId, int order) throws FinderException, CreateException {
		CaseCategory category = null;
		boolean isDefaultLocale = ICLocaleBusiness.getLocaleId(this.getDefaultLocale()) == localeId;

		if (caseCategoryPK != null) {
			category = getCaseCategory(caseCategoryPK);
		}
		else {
			category = getCaseCategoryHome().create();
		}

		CaseCategory parentCategory = null;

		if (parentCaseCategoryPK != null) {
			parentCategory = getCaseCategory(parentCaseCategoryPK);
		}

		if (category.getName() == null || isDefaultLocale) {
			category.setName(name);
		}

		if (category.getDescription() == null || isDefaultLocale) {
			category.setDescription(description);
		}

		// watch out for endless nesting
		if (parentCaseCategoryPK != null && !parentCaseCategoryPK.equals(caseCategoryPK)) {
			category.setParent(parentCategory);
		}

		if (parentCaseCategoryPK == null) {
			category.setParent(null);
		}

		category.setHandlerGroup(groupPK);
		if (order != -1) {
			category.setOrder(order);
		}

		category.store();

		// localization

		LocalizedText locText = category.getLocalizedText(localeId);
		if (locText == null) {
			locText = ((LocalizedTextHome) com.idega.data.IDOLookup.getHomeLegacy(LocalizedText.class)).createLegacy();
		}

		locText.setHeadline(name);
		locText.setBody(description);
		locText.setLocaleId(localeId);
		locText.store();

		try {
			category.addLocalization(locText);
		}
		catch (SQLException e) {
			// error usually means the text is already added
			// e.printStackTrace();
			// throw new CreateException("Failed to add localization, the message was : "+e.getMessage());
		}

		return category;

	}

	public void storeCaseType(Object caseTypePK, String name, String description, int order) throws FinderException, CreateException {
		CaseType type = null;
		if (caseTypePK != null) {
			type = getCaseType(caseTypePK);
		}
		else {
			type = getCaseTypeHome().create();
		}

		type.setName(name);
		type.setDescription(description);
		if (order != -1) {
			type.setOrder(order);
		}
		type.store();
	}

	public Map getSubCategories(String categoryPK, String country) {
		Map map = new LinkedHashMap();
		Locale locale = new Locale(country, country.toUpperCase());

		if (categoryPK != null && categoryPK.length() > 0 && Integer.parseInt(categoryPK) > -1) {
			CaseCategory category = null;
			try {
				category = getCaseCategory(categoryPK);
			}
			catch (FinderException e) {
				e.printStackTrace();
			}

			if (category != null) {
				Collection coll = getSubCategories(category);

				if (!coll.isEmpty()) {
					map.put(categoryPK, getLocalizedString("case_creator.select_sub_category", "Select sub category", locale));

					Iterator iter = coll.iterator();
					while (iter.hasNext()) {
						CaseCategory subCategory = (CaseCategory) iter.next();
						map.put(subCategory.getPrimaryKey().toString(), subCategory.getName());
					}
				}
				else {
					map.put(categoryPK, getLocalizedString("case_creator.no_sub_category", "no sub category", locale));
				}
			}
		}
		return map;
	}

	public Map getUsers(String categoryPK) {
		try {
			Map map = new LinkedHashMap();

			if (categoryPK != null && categoryPK.length() > 0 && Integer.parseInt(categoryPK) > -1) {
				CaseCategory category = null;
				try {
					category = getCaseCategory(categoryPK);
				}
				catch (FinderException e) {
					e.printStackTrace();
				}

				if (category != null) {
					Group handlerGroup = category.getHandlerGroup();

					Collection handlers = getUserBusiness().getUsersInGroup(handlerGroup);
					if (!handlers.isEmpty()) {
						Iterator iter = handlers.iterator();
						while (iter.hasNext()) {
							User handler = (User) iter.next();
							map.put(handler.getPrimaryKey().toString(), handler.getName());
						}
					}
				}
			}
			return map;
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	private void sendMessage(GeneralCase theCase, User receiver, User sender, String subject, String body) {
		try {
			getMessageBusiness().createUserMessage(theCase, receiver, sender, subject, body, false);
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	public boolean canDeleteCase(Case theCase) {
		return false;
	}

	public boolean useSubCategories() {
		return getIWApplicationContext().getApplicationSettings().getBoolean(CaseConstants.PROPERTY_USE_SUB_CATEGORIES, false);
	}

	public boolean useTypes() {
		return getIWApplicationContext().getApplicationSettings().getBoolean(CaseConstants.PROPERTY_USE_TYPES, true);
	}

	public boolean allowPrivateCases() {
		return getIWApplicationContext().getApplicationSettings().getBoolean(CaseConstants.PROPERTY_ALLOW_PRIVATE_CASES, false);
	}

	public boolean allowAnonymousCases() {
		return getIWApplicationContext().getApplicationSettings().getBoolean(CaseConstants.PROPERTY_ALLOW_ANONYMOUS_CASES, false);
	}

	public boolean allowAttachments() {
		return getIWApplicationContext().getApplicationSettings().getBoolean(CaseConstants.PROPERTY_ALLOW_ATTACHMENTS, false);
	}
}