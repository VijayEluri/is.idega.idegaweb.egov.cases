package is.idega.idegaweb.egov.cases.presentation.beans;

import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import is.idega.idegaweb.egov.cases.presentation.CasesProcessor;
import is.idega.idegaweb.egov.cases.util.CaseConstants;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.faces.component.UIComponent;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.process.business.CaseManager;
import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseStatus;
import com.idega.block.process.presentation.beans.GeneralCasesListBuilder;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.SpringBeanLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Heading3;
import com.idega.presentation.text.Heading5;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.PresentationUtil;
import com.idega.util.text.Name;

@Scope("session")
@Service(GeneralCasesListBuilder.SPRING_BEAN_IDENTIFIER)
public class CasesListBuilderImpl implements GeneralCasesListBuilder {
	
	private IWBundle bundle = null;
	private IWResourceBundle iwrb = null;
	
	private CasesBusiness casesBusiness = null;
	
	private String caseContainerStyle = "casesListCaseContainer";
	private String bodyItem = "casesListBodyContainerItem";
	private String oldBodyItem = "old_" + bodyItem;
	private String lastRowStyle = "lastRow";
	
	private Layer createHeader(IWContext iwc, Layer container, String prefix, int totalCases, boolean showCheckBoxes) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		if (totalCases < 1) {
			container.add(new Heading5(iwrb.getLocalizedString("no_case_exist", "There are no cases")));
			return container;
		}
		
		addWeb2Stuff(iwc, container);
		
		Layer casesContainer = new Layer();
		container.add(casesContainer);
		Layer headers = new Layer();
		casesContainer.add(headers);
		headers.setStyleClass("casesListHeadersContainer");
		String headerItem = "casesListHeadersContainerItem";

		//	Number
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString(prefix + "case_nr", "Case nr.")), headerItem, "CaseNumber");

		//	Sender
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("sender", "Sender")), headerItem, "Sender");
		
		//	Description
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("description", "Description")), headerItem, "Description");

		//	Creation date
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("created_date", "Created date")), headerItem, "CreatedDate");

		//	Status
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("status", "Status")), headerItem, "Status");
		
		//	Toggler - controller
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("view", "View")), headerItem, "Toggler");

		//	Handle case
		if (showCheckBoxes) {
			addLayerToCasesList(headers, Text.getNonBrakingSpace(), headerItem, "MultiHandle");
		}
		
		headers.add(new CSSSpacer());
		
		return casesContainer;
	}
	
	private Layer createBody(Layer casesContainer) {
		Layer casesBodyContainer = new Layer();
		casesContainer.add(casesBodyContainer);
		casesBodyContainer.setStyleClass("casesListBodyContainer");
		return casesBodyContainer;
	}
	
	private Layer addRowToCasesList(IWContext iwc, Layer casesBodyContainer, Case theCase, CaseStatus caseStatusReview, Locale l, String prefix, boolean showCheckBoxes,
			boolean isPrivate, int rowsCounter) {
		Layer caseContainer = new Layer();
		casesBodyContainer.add(caseContainer);
		caseContainer.setStyleClass(caseContainerStyle);
		
		CaseStatus status = theCase.getCaseStatus();
		User owner = theCase.getOwner();
		IWTimestamp created = new IWTimestamp(theCase.getCreated());
		
		CaseManager caseManager = null;
		if (theCase.getCaseManagerType() != null) {
			caseManager = getCasesBusiness(iwc).getCaseHandlersProvider().getCaseHandler(theCase.getCaseManagerType());
		}
//		if (rowsCounter % 2 == 0) {
//			caseManager = null;
//		}
		
		if (rowsCounter == 0) {
			caseContainer.setStyleClass("firstRow");
		}

		if (isPrivate) {
			caseContainer.setStyleClass("isPrivate");
		}
		if (status != null && caseStatusReview != null) {
			if (status.equals(caseStatusReview)) {
				caseContainer.setStyleClass("isReview");
			}
		}
		
		//	Number
		Layer numberContainer = addLayerToCasesList(caseContainer, null, bodyItem, "CaseNumber");
		String caseIdentifier = caseManager == null ? theCase.getPrimaryKey().toString() : caseManager.getProcessIdentifier(theCase);
		numberContainer.setStyleClass("firstColumn");
		numberContainer.add(new Text(caseIdentifier));

		//	Sender
		Layer senderContainer = addLayerToCasesList(caseContainer, null, bodyItem, "Sender");
		senderContainer.add(owner == null ? new Text(CoreConstants.MINUS) : new Text(new Name(owner.getFirstName(), owner.getMiddleName(),
				owner.getLastName()).getName(l)));
		
		//	Description
		Layer descriptionContainer = addLayerToCasesList(caseContainer, null, bodyItem, "Description");
		String subject = theCase.getSubject();
		descriptionContainer.add(new Text(subject == null ? CoreConstants.MINUS : subject));

		//	Creation date
		Layer creationDateContainer = addLayerToCasesList(caseContainer, null, bodyItem, "CreationDate");
		creationDateContainer.add(new Text(created.getLocaleDateAndTime(l, IWTimestamp.SHORT, IWTimestamp.SHORT)));

		//	Status
		String localizedStatus = null;
		try {
			localizedStatus = getCasesBusiness(iwc).getLocalizedCaseStatusDescription(theCase, status, l);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		addLayerToCasesList(caseContainer, new Text(localizedStatus == null ? CoreConstants.MINUS : localizedStatus), bodyItem, "Status");

		//	Controller
		Layer customerView = null;
		UIComponent childForContainer = null;
		if (caseManager == null) {
			childForContainer = getProcessLink(getBundle(iwc).getImage("edit.png", getResourceBundle(iwc).getLocalizedString(prefix + "view_case", "View case")),
					theCase);
		}
		else {
			childForContainer = Text.getNonBrakingSpace(10);
		}
		Layer togglerContainer = addLayerToCasesList(caseContainer, childForContainer, caseManager == null ? oldBodyItem : bodyItem, "Toggler");
		if (caseManager != null) {
			togglerContainer.setStyleClass("expand");
			togglerContainer.setMarkupAttribute("caseid", theCase.getPrimaryKey().toString());
			customerView = new Layer();
			togglerContainer.setMarkupAttribute("customerviewid", customerView.getId());
		}
		
		//	Handle case
		if (showCheckBoxes) {
			CheckBox box = new CheckBox(CasesProcessor.PARAMETER_CASE_PK, theCase.getPrimaryKey().toString());

			Layer multiHandleContainer = addLayerToCasesList(caseContainer, box, bodyItem, "MultiHandle");
			multiHandleContainer.setStyleClass("lastColumn");
		}

		if (rowsCounter % 2 == 0) {
			caseContainer.setStyleClass("evenRow");
		}
		else {
			caseContainer.setStyleClass("oddRow");
		}
		
		caseContainer.add(new CSSSpacer());
		
		if (customerView != null) {
			caseContainer.add(customerView);
			customerView.setStyleAttribute("display", "none");
		}
	
		return caseContainer;
	}
	
	public UIComponent getCasesList(IWContext iwc, String caseProcessorType, String prefix, boolean showCheckBoxes) {			
		Layer container = new Layer();

		IWResourceBundle iwrb = getResourceBundle(iwc);
		CasesBusiness casesBusiness = getCasesBusiness(iwc);
		if (casesBusiness == null) {
			container.add(new Heading3(iwrb.getLocalizedString("cases_list.can_not_get_cases_list", "Sorry, error occurred - can not generate cases list.")));
			return container;
		}
		
		Collection<GeneralCase> cases = null;
		try {
			cases = casesBusiness.getCases(iwc. getCurrentUser(), caseProcessorType);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		int totalCases = (cases == null || cases.isEmpty()) ? 0 : cases.size();
		
		Layer casesContainer = createHeader(iwc, container, prefix, totalCases, showCheckBoxes);
		
		if (totalCases < 1) {
			return container;
		}
		
		Layer casesBodyContainer = createBody(casesContainer);
		
		int rowsCounter = 0;
		Layer caseContainer = null;
		Locale l = iwc.getCurrentLocale();
		CaseStatus caseStatusReview = null;
		try {
			caseStatusReview = casesBusiness.getCaseStatusReview();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		for(GeneralCase theCase: cases) {			
			caseContainer = addRowToCasesList(iwc, casesBodyContainer, theCase, caseStatusReview, l, prefix, showCheckBoxes, theCase.isPrivate(), rowsCounter);
			rowsCounter++;
		}
		caseContainer.setStyleClass(lastRowStyle);
		
		return container;
	}
	
	//	TODO: test this
	public UIComponent getCasesList(IWContext iwc, Collection<Case> cases, String prefix) {
		Layer container = new Layer();

		IWResourceBundle iwrb = getResourceBundle(iwc);
		CasesBusiness casesBusiness = getCasesBusiness(iwc);
		if (casesBusiness == null) {
			container.add(new Heading3(iwrb.getLocalizedString("cases_list.can_not_get_cases_list", "Sorry, error occurred - can not generate cases list.")));
			return container;
		}
		
		int totalCases = (cases == null || cases.isEmpty()) ? 0 : cases.size();
		
		Layer casesContainer = createHeader(iwc, container, prefix, totalCases, false);
		
		if (totalCases < 1) {
			return container;
		}
		
		Layer casesBodyContainer = createBody(casesContainer);
		
		int rowsCounter = 0;
		Layer caseContainer = null;
		Locale l = iwc.getCurrentLocale();
		CaseStatus caseStatusReview = null;
		try {
			caseStatusReview = casesBusiness.getCaseStatusReview();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		for(Case theCase: cases) {			
			caseContainer = addRowToCasesList(iwc, casesBodyContainer, theCase, caseStatusReview, l, prefix, false, false, rowsCounter);
			rowsCounter++;
		}
		caseContainer.setStyleClass(lastRowStyle);

		return container;
	}
	
	public Link getProcessLink(PresentationObject object, Case theCase) {
		Link process = new Link(object);
		
		process.addParameter(CasesProcessor.PARAMETER_CASE_PK, theCase.getPrimaryKey().toString());
		process.addParameter(CasesProcessor.PARAMETER_ACTION, CasesProcessor.ACTION_PROCESS);

		return process;
	}
	
	private void addWeb2Stuff(IWContext iwc, Layer container) {
		Web2Business web2Business = SpringBeanLookup.getInstance().getSpringBean(iwc, Web2Business.class);
		
		List<String> scripts = new ArrayList<String>();
		try {
			scripts.add(web2Business.getBundleURIToJQueryLib());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		scripts.add(web2Business.getBundleURIToJQGrid());
		IWBundle bundle = getBundle(iwc);
		scripts.add(bundle.getVirtualPathWithFileNameString("javascript/CasesListHelper.js"));
		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);
		scripts.add("/dwr/interface/CasesEngine.js");
		scripts.add("/dwr/interface/BPMProcessAssets.js");
	
		List<String> css = new ArrayList<String>();
		css.add(web2Business.getBundleURIToJQGridStyles());
		
		String caseId = iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK);
		if (caseId == null || CoreConstants.EMPTY.equals(caseId)) {
			caseId = iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK + "_id");
		}
		StringBuilder action = new StringBuilder("initializeCasesList(");
		if (caseId == null || CoreConstants.EMPTY.equals(action)) {
			action.append("null");
		}
		else {
			action.append("'").append(caseId).append("'");
		}
		action.append(");");
		if (CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			container.add(PresentationUtil.getJavaScriptSourceLines(scripts));
			container.add(PresentationUtil.getStyleSheetsSourceLines(css));
			container.add(PresentationUtil.getJavaScriptAction(action.toString()));
		}
		else {
			PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
			PresentationUtil.addStyleSheetsToHeader(iwc, css);
			PresentationUtil.addJavaScriptActionToBody(iwc, "jQuery(document).ready(function() {"+action.toString()+"});");
		}
	}
	
	private CasesBusiness getCasesBusiness(IWApplicationContext iwac) {
		if (casesBusiness == null) {
			try {
				casesBusiness = (CasesBusiness) IBOLookup.getServiceInstance(iwac, CasesBusiness.class);
			} catch (IBOLookupException e) {
				e.printStackTrace();
			}
		}
		return casesBusiness;
	}
	
	
	private IWBundle getBundle(IWContext iwc) {
		if (bundle == null) {
			bundle = iwc.getIWMainApplication().getBundle(CaseConstants.IW_BUNDLE_IDENTIFIER);
		}
		return bundle;
	}
	
	private IWResourceBundle getResourceBundle(IWContext iwc) {
		if (iwrb == null) {
			iwrb = getBundle(iwc).getResourceBundle(iwc);
		}
		return iwrb;
	}
	
	private Layer addLayerToCasesList(Layer container, UIComponent child, String defaultStyleClass, String suffixForStyleClass) {
		Layer layer = new Layer();
		container.add(layer);
		
		if (defaultStyleClass != null) {
			layer.setStyleClass(defaultStyleClass);
			if (suffixForStyleClass != null) {
				layer.setStyleClass(defaultStyleClass + suffixForStyleClass);
			}
		}
		
		if (child != null) {
			layer.add(child);
		}
		
		return layer;
	}

}