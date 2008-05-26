/*
 * $Id$ Created on Nov 7, 2005
 * 
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.business.CaseCategoryCollectionHandler;
import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.FinderException;

import com.idega.block.process.data.CaseLog;
import com.idega.business.IBORuntimeException;
import com.idega.core.file.data.ICFile;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.remotescripting.RemoteScriptHandler;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.util.SelectorUtility;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.text.TextSoap;

public class MyCases extends CasesProcessor {

	private static final String PARAMETER_CASE_CATEGORY_PK = "prm_case_category_pk";
	private static final String PARAMETER_SUB_CASE_CATEGORY_PK = "prm_sub_case_category_pk";
	private static final String PARAMETER_CASE_TYPE_PK = "prm_case_type_pk";
	
	public static final String TYPE = "MyCases";
	
	protected String getBlockID() {
		return "myCases";
	}

	@Override
	protected Collection<GeneralCase> getCases(User user) throws RemoteException {
		Collection<GeneralCase> cases = getCasesBusiness().getCases(user, getCasesProcessorType());
		Collection<GeneralCase> myCases = getCasesBusiness().getMyCases(user);
		
		if (cases != null) {
			myCases.addAll(cases);
		}
			
		return myCases;
	}

	protected void showProcessor(IWContext iwc, Object casePK) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("adminForm");
		form.setStyleClass("overview");
		form.maintainParameter(PARAMETER_CASE_PK);
		form.addParameter(PARAMETER_ACTION, "");

		boolean useSubCategories = getCasesBusiness(iwc).useSubCategories();

		GeneralCase theCase = null;
		try {
			theCase = getCasesBusiness().getGeneralCase(casePK);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			throw new IBORuntimeException(fe);
		}
		CaseCategory category = theCase.getCaseCategory();
		CaseCategory parentCategory = category.getParent();
		CaseType type = theCase.getCaseType();
		ICFile attachment = theCase.getAttachment();
		User owner = theCase.getOwner();
		IWTimestamp created = new IWTimestamp(theCase.getCreated());

		form.add(getPersonInfo(iwc, owner));

		Heading1 heading = new Heading1(getResourceBundle().getLocalizedString(getPrefix() + "case_overview", "Case overview"));
		heading.setStyleClass("subHeader");
		heading.setStyleClass("topSubHeader");
		form.add(heading);

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		if (theCase.isPrivate()) {
			section.add(getAttentionLayer(getResourceBundle().getLocalizedString(getPrefix() + "case.is_private", "The sender wishes that this case be handled as confidential.")));
		}

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");

		SelectorUtility util = new SelectorUtility();
		DropdownMenu categories = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_CATEGORY_PK), getCasesBusiness().getCaseCategories(), "getName");
		categories.keepStatusOnAction(true);
		categories.setSelectedElement(parentCategory != null ? parentCategory.getPrimaryKey().toString() : category.getPrimaryKey().toString());
		categories.setStyleClass("caseCategoryDropdown");

		DropdownMenu subCategories = new DropdownMenu(PARAMETER_SUB_CASE_CATEGORY_PK);
		subCategories.keepStatusOnAction(true);
		subCategories.setSelectedElement(category.getPrimaryKey().toString());
		subCategories.setStyleClass("subCaseCategoryDropdown");

		if (parentCategory != null) {
			@SuppressWarnings("unchecked")
			Collection<CaseCategory> collection = getCasesBusiness(iwc).getSubCategories(parentCategory);
			
			if (collection.isEmpty())
				subCategories.addMenuElement(category.getPrimaryKey().toString(), getResourceBundle().getLocalizedString("case_creator.no_sub_category", "no sub category"));
			else
				for (CaseCategory subCategory : collection)
					subCategories.addMenuElement(subCategory.getPrimaryKey().toString(), subCategory.getLocalizedCategoryName(iwc.getCurrentLocale()));
		}

		DropdownMenu types = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_TYPE_PK), getCasesBusiness().getCaseTypes(), "getName");
		types.keepStatusOnAction(true);
		types.setSelectedElement(type.getPrimaryKey().toString());
		types.setStyleClass("caseTypeDropdown");

		HiddenInput hiddenType = new HiddenInput(PARAMETER_CASE_TYPE_PK, type.getPrimaryKey().toString());

		DropdownMenu statuses = new DropdownMenu(PARAMETER_STATUS);
		statuses.addMenuElement(getCasesBusiness().getCaseStatusPending().getStatus(), getCasesBusiness().getLocalizedCaseStatusDescription(theCase, getCasesBusiness().getCaseStatusPending(), iwc.getCurrentLocale()));
		statuses.addMenuElement(getCasesBusiness().getCaseStatusWaiting().getStatus(), getCasesBusiness().getLocalizedCaseStatusDescription(theCase, getCasesBusiness().getCaseStatusWaiting(), iwc.getCurrentLocale()));
		statuses.addMenuElement(getCasesBusiness().getCaseStatusReady().getStatus(), getCasesBusiness().getLocalizedCaseStatusDescription(theCase, getCasesBusiness().getCaseStatusReady(), iwc.getCurrentLocale()));
		statuses.setSelectedElement(theCase.getStatus());
		statuses.setStyleClass("caseStatusDropdown");

		Layer message = new Layer(Layer.SPAN);
		message.add(new Text(TextSoap.formatText(theCase.getMessage())));

		Layer createdDate = new Layer(Layer.SPAN);
		createdDate.add(new Text(created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));

		TextArea reply = new TextArea(PARAMETER_REPLY);
		reply.setStyleClass("textarea");
		reply.keepStatusOnAction(true);

		if (getCasesBusiness().useTypes()) {
			Layer element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			Label label = new Label(getResourceBundle().getLocalizedString("case_type", "Case type"), types);
			element.add(label);
			element.add(types);
			section.add(element);
		}
		else {
			form.add(hiddenType);
		}

		Layer element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		Label label = new Label(getResourceBundle().getLocalizedString("case_category", "Case category"), categories);
		element.add(label);
		element.add(categories);
		section.add(element);

		if (useSubCategories) {
			try {
				RemoteScriptHandler rsh = new RemoteScriptHandler(categories, subCategories);
				rsh.setRemoteScriptCollectionClass(CaseCategoryCollectionHandler.class);
				element.add(rsh);
			}
			catch (IllegalAccessException iae) {
				iae.printStackTrace();
			}
			catch (InstantiationException ie) {
				ie.printStackTrace();
			}

			element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label(getResourceBundle().getLocalizedString("sub_case_category", "Sub case category"), subCategories);
			element.add(label);
			element.add(subCategories);
			section.add(element);
		}

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("created_date", "Created date"));
		element.add(label);
		element.add(createdDate);
		section.add(element);

		if (attachment != null) {
			Link link = new Link(new Text(attachment.getName()));
			link.setFile(attachment);
			link.setTarget(Link.TARGET_BLANK_WINDOW);

			Layer attachmentSpan = new Layer(Layer.SPAN);
			attachmentSpan.add(link);

			element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label();
			label.setLabel(getResourceBundle().getLocalizedString("attachment", "Attachment"));
			element.add(label);
			element.add(attachmentSpan);
			section.add(element);
		}

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		element.setStyleClass("informationItem");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString(getPrefix() + "message", "Message"));
		element.add(label);
		element.add(message);
		section.add(element);

		section.add(clear);

		heading = new Heading1(getResourceBundle().getLocalizedString("handler_overview", "Handler overview"));
		heading.setStyleClass("subHeader");
		form.add(heading);

		section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label(getResourceBundle().getLocalizedString("status", "status"), statuses);
		element.add(label);
		element.add(statuses);
		section.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label(getResourceBundle().getLocalizedString("reply", "Reply"), reply);
		element.add(label);
		element.add(reply);
		section.add(element);

		section.add(clear);

		@SuppressWarnings("unchecked")
		Collection<CaseLog> logs = getCasesBusiness(iwc).getCaseLogs(theCase);
		if (!logs.isEmpty())
			for (CaseLog log : logs)
				form.add(getHandlerLayer(iwc, this.getResourceBundle(), theCase, log));

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link back = getButtonLink(getResourceBundle().getLocalizedString("back", "Back"));
		back.setStyleClass("homeButton");
		back.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_VIEW));
		back.setToFormSubmit(form);
		bottom.add(back);

		Link next = getButtonLink(getResourceBundle().getLocalizedString("process", "Process"));
		next.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
		next.setToFormSubmit(form);
		bottom.add(next);

		add(form);
	}

	protected void save(IWContext iwc) throws RemoteException {
		Object casePK = iwc.getParameter(PARAMETER_CASE_PK);
		Object caseCategoryPK = iwc.getParameter(PARAMETER_CASE_CATEGORY_PK);
		Object subCaseCategoryPK = iwc.getParameter(PARAMETER_SUB_CASE_CATEGORY_PK);
		Object caseTypePK = iwc.getParameter(PARAMETER_CASE_TYPE_PK);
		String status = iwc.getParameter(PARAMETER_STATUS);
		String reply = iwc.getParameter(PARAMETER_REPLY);

		try {
			getCasesBusiness().handleCase(casePK, subCaseCategoryPK != null ? subCaseCategoryPK : caseCategoryPK, caseTypePK, status, iwc.getCurrentUser(), reply, iwc);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
		}
	}

	protected boolean showCheckBox() {
		return false;
	}
	
	@Override
	protected String getCasesProcessorType() {
		return TYPE;
	}
}