package is.idega.idegaweb.egov.cases.presentation.beans;

import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import is.idega.idegaweb.egov.cases.presentation.CasesProcessor;
import is.idega.idegaweb.egov.cases.presentation.CasesStatistics;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.faces.component.UIComponent;

import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.business.CaseManagersProvider;
import com.idega.block.process.business.CasesRetrievalManager;
import com.idega.block.process.business.ProcessConstants;
import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseStatus;
import com.idega.block.process.presentation.UserCases;
import com.idega.block.process.presentation.beans.CaseListPropertiesBean;
import com.idega.block.process.presentation.beans.CasePresentation;
import com.idega.block.process.presentation.beans.GeneralCasesListBuilder;
import com.idega.block.web2.business.JQueryPlugin;
import com.idega.block.web2.business.Web2Business;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.core.accesscontrol.business.CredentialBusiness;
import com.idega.core.builder.data.ICPage;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.ListNavigator;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.paging.PagedDataCollection;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Heading3;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.HiddenInput;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.text.Name;
import com.idega.webface.WFUtil;

@Scope("singleton")
@Service(GeneralCasesListBuilder.SPRING_BEAN_IDENTIFIER)
public class CasesListBuilderImpl implements GeneralCasesListBuilder {
	
	private CaseManagersProvider caseManagersProvider;
	
	private String caseContainerStyle = "casesListCaseContainer";
	private String bodyItem = "casesListBodyContainerItem";
	private String oldBodyItem = "old_" + bodyItem;
	private String lastRowStyle = "lastRow";
	
	private String caseIdParName = "caseid";
	private String usePDFDownloadColumnParName = "usepdfdownloadcolumn";
	private String allowPDFSigningParName = "allowpdfsigning";
	
	private Layer createHeader(IWContext iwc, Layer container, int totalCases, boolean searchResults, CaseListPropertiesBean properties) {
		PresentationUtil.addStyleSheetToHeader(iwc, getBundle(iwc).getVirtualPathWithFileNameString("style/case.css"));
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer searchInfo = searchResults ? getSearchQueryInfo(iwc) : null;
		
		if (totalCases < 1) {
			if (searchResults) {
				container.add(new Heading3(iwrb.getLocalizedString("no_cases_found", "No cases were found by your query!")));
				container.add(searchInfo);
			}
			else {
				container.add(new Heading3(iwrb.getLocalizedString("no_case_exist", "There are no cases")));
			}
			return container;
		}
		
		String caseId = iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK);
		addResources(caseId, iwc, getBundle(iwc), properties.getType());
		
		if (searchResults) {
			StringBuilder message = new StringBuilder(iwrb.getLocalizedString("search_for_cases_results", "Your search results")).append(CoreConstants.SPACE);
			message.append("(").append(totalCases).append("):");
			container.add(new Heading3(message.toString()));
			
			container.add(searchInfo);
		}
		
		Layer casesContainer = new Layer();
		container.add(casesContainer);
		Layer headers = new Layer();
		casesContainer.add(headers);
		headers.setStyleClass("casesListHeadersContainer");
		String headerItem = "casesListHeadersContainerItem";

		if (properties.isShowCaseNumberColumn()) {
			//	Number
			addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("case_nr", "Case nr.")), headerItem, "CaseNumber");
		}
		
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
		if (properties.isShowCheckBoxes()) {
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
	
	private void prepareCellToBeGridExpander(Layer layer, String caseId, String gridViewerId, CaseListPropertiesBean properties) {
		if (layer == null) {
			return;
		}
		
		layer.setStyleClass(CasesConstants.CASES_LIST_GRID_EXPANDER_STYLE_CLASS);
		layer.setMarkupAttribute(caseIdParName, caseId);
		layer.setMarkupAttribute("customerviewid", gridViewerId);
		layer.setMarkupAttribute(usePDFDownloadColumnParName, String.valueOf(properties.isUsePDFDownloadColumn()));
		layer.setMarkupAttribute(allowPDFSigningParName, String.valueOf(properties.isAllowPDFSigning()));
		layer.setMarkupAttribute("hideemptysection", String.valueOf(properties.isHideEmptySection()));
	}
	
	@SuppressWarnings("unchecked")
	private Layer addRowToCasesList(IWContext iwc, Layer casesBodyContainer, CasePresentation theCase, CaseStatus caseStatusReview, Locale l,
			boolean isUserList, int rowsCounter, Map pages, String emailAddress, boolean descriptionIsEditable, CaseListPropertiesBean properties) {
		Layer caseContainer = new Layer();
		casesBodyContainer.add(caseContainer);
		caseContainer.setStyleClass(caseContainerStyle);
		if (!StringUtil.isEmpty(theCase.getProcessName())) {
			caseContainer.setStyleClass(theCase.getProcessName());
		}
					
		User owner = theCase.getOwner();
		IWTimestamp created = new IWTimestamp(theCase.getCreated());
				
		if (rowsCounter == 0) {
			caseContainer.setStyleClass("firstRow");
		}

		if (theCase.isPrivate()) {
			caseContainer.setStyleClass("isPrivate");
		}
		String caseStatusCode = null;
		CaseStatus status = theCase.getCaseStatus(); 
		if (status != null && caseStatusReview != null) {
			if (status.equals(caseStatusReview)) {
				caseContainer.setStyleClass("isReview");
			}
			
			caseStatusCode = status.getStatus();
			if (!StringUtil.isEmpty(caseStatusCode)) {
				caseContainer.setStyleClass(caseStatusCode);
			}
		}
		
		Layer numberContainer = null;
		if (properties.isShowCaseNumberColumn()) {
			//	Number
			numberContainer = addLayerToCasesList(caseContainer, null, bodyItem, "CaseNumber");
			
			String identifier = theCase.getCaseIdentifier();
			
			numberContainer.setStyleClass("firstColumn");
			if (identifier == null) {
				numberContainer.add(theCase.getPrimaryKey().toString());
			} else {
				if (theCase.isBpm()) {
					IWResourceBundle iwrb = getResourceBundle(iwc);
					Link sendEmail = new Link(getBundle(iwc).getImage("images/email.png", getTitleSendEmail(iwrb)),
							getEmailAddressMailtoFormattedWithSubject(emailAddress, identifier));
					numberContainer.add(sendEmail);
					numberContainer.add(Text.getNonBrakingSpace());
				}
				numberContainer.add(identifier);
			}
		}
		
		boolean showCheckBoxes = !theCase.isBpm() ? properties.isShowCheckBoxes() : false;
		
		Layer customerView = null;
		String caseId = theCase.getPrimaryKey().toString();
		String gridViewerId = null;
		if (theCase.isBpm()) {
			customerView = new Layer();
			gridViewerId = customerView.getId();
		}
		
		if (theCase.isBpm()) {
			prepareCellToBeGridExpander(numberContainer, caseId, gridViewerId, properties);
		}

		//	Sender
		Layer senderContainer = addLayerToCasesList(caseContainer, null, bodyItem, "Sender");
		senderContainer.add(owner == null ? new Text(CoreConstants.MINUS) : new Text(new Name(owner.getFirstName(), owner.getMiddleName(),
				owner.getLastName()).getName(l)));
		if (theCase.isBpm()) {
			prepareCellToBeGridExpander(senderContainer, caseId, gridViewerId, properties);
		}
		
		//	Description
		Layer descriptionContainer = addLayerToCasesList(caseContainer, null, bodyItem, "Description");
		if (descriptionIsEditable) {
			descriptionContainer.setStyleClass("casesListBodyItemIsEditable");
			descriptionContainer.setMarkupAttribute(caseIdParName, caseId);
		}
		String subject = theCase.getSubject();
		if (subject != null && subject.length() > 100) {
			subject = new StringBuilder(subject.substring(0, 100)).append(CoreConstants.DOT).append(CoreConstants.DOT).append(CoreConstants.DOT).toString();
		}
		descriptionContainer.add(new Text(subject == null ? CoreConstants.MINUS : subject));

		//	Creation date
		Layer creationDateContainer = addLayerToCasesList(caseContainer, null, bodyItem, "CreationDate");
		if (properties.isShowCreationTimeInDateColumn()) {
			creationDateContainer.setStyleClass("showOnlyDateValueForCaseInCasesListRow");
		}
		creationDateContainer.add(new Text(properties.isShowCreationTimeInDateColumn() ? created.getLocaleDateAndTime(l, IWTimestamp.SHORT, IWTimestamp.SHORT) :
			created.getLocaleDate(l, IWTimestamp.SHORT)));
		if (theCase.isBpm()) {
			prepareCellToBeGridExpander(creationDateContainer, caseId, gridViewerId, properties);
		}

		//	Status
		String localizedStatus = theCase.getLocalizedStatus();
		Layer statusContainer = addLayerToCasesList(caseContainer, new Text(StringUtil.isEmpty(localizedStatus) ? CoreConstants.MINUS : localizedStatus),
				bodyItem, "Status");
		if (theCase.isBpm()) {
			prepareCellToBeGridExpander(statusContainer, caseId, gridViewerId, properties);
		}
		if (!StringUtil.isEmpty(caseStatusCode)) {
			statusContainer.setStyleClass(caseStatusCode);
		}
		
		//	Controller
		UIComponent childForContainer = null;
		if (!theCase.isBpm()) {
			Image view = getBundle(iwc).getImage("edit.png", getResourceBundle(iwc).getLocalizedString("view_case", "View case"));
			if (isUserList) {
				childForContainer = getLinkToViewUserCase(iwc, theCase, view, pages, theCase.getCode(), status, properties.isAddCredentialsToExernalUrls());
			}
			else {
				childForContainer = getProcessLink(iwc, view, theCase);
			}
		}
		else {
			childForContainer = Text.getNonBrakingSpace(10);
		}
		Layer togglerContainer = addLayerToCasesList(caseContainer, childForContainer, !theCase.isBpm() ? oldBodyItem : bodyItem, "Toggler");
		if (theCase.isBpm()) {
			togglerContainer.setStyleClass("expand");
			togglerContainer.setMarkupAttribute("changeimage", "true");
			prepareCellToBeGridExpander(togglerContainer, caseId, gridViewerId, properties);
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
	
	private String getEmailAddressMailtoFormattedWithSubject(String emailAddress, String subject) {
		if (emailAddress == null || CoreConstants.EMPTY.equals(emailAddress)) {
			return subject;
		}
		
		return new StringBuilder("mailto:").append(emailAddress).append("?subject=(").append(subject).append(")").toString();
	} 
	
	public String getEmailAddressMailtoFormattedWithSubject(String subject) {
		return getEmailAddressMailtoFormattedWithSubject(getDefaultEmail(), subject);
	}
		
	private boolean isDescriptionEditable(String type, boolean isAdmin) {
		boolean descriptionIsEditable = CasesRetrievalManager.CASE_LIST_TYPE_OPEN.equals(type);
		if (!descriptionIsEditable) {
			descriptionIsEditable = CasesRetrievalManager.CASE_LIST_TYPE_MY.equals(type) && isAdmin;
		}
		return descriptionIsEditable;
	}
	
	private boolean isSearchResultsList(String caseProcessorType) {
		return ProcessConstants.CASE_LIST_TYPE_SEARCH_RESULTS.equals(caseProcessorType);
	}
	
	private Layer getCasesListContainer(boolean searchResults) {
		Layer container = new Layer();
		container.setStyleClass(GeneralCasesListBuilder.MAIN_CASES_LIST_CONTAINER_STYLE);
		if (searchResults) {
			container.setMarkupAttribute("searchresult", Boolean.TRUE.toString());
		}
		return container;
	}
	
	@SuppressWarnings("unchecked")
	private Layer getSearchQueryInfo(IWContext iwc) {
		Layer container = new Layer();
		container.setStyleClass("userCasesSearchQueryInfoContainer");
		
		Object o = iwc.getSessionAttribute(GeneralCasesListBuilder.USER_CASES_SEARCH_QUERY_BEAN_ATTRIBUTE);
		if (!(o instanceof List)) {
			container.setStyleAttribute("display", "none");
			return container;
		}
		List<AdvancedProperty> searchCriterias = (List<AdvancedProperty>) o;
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		if (ListUtil.isEmpty(searchCriterias)) {
			container.add(new Heading3(iwrb.getLocalizedString("cases_list_builder.there_are_no_searh_criterias", "There are no search criterias")));
			return container;
		}
		
		String localizedSearchInfo = iwrb.getLocalizedString("cases_list_builder.search_was_executed_by_following_criterias", "Searching by");
		if (!localizedSearchInfo.endsWith(":")) {
			localizedSearchInfo = new StringBuilder(localizedSearchInfo).append(":").toString();
		}
		container.add(new Heading3(localizedSearchInfo));
		container.add(new Break());
		
		Lists criterias = new Lists();
		container.add(criterias);
		for (AdvancedProperty searchCriteria: searchCriterias) {
			ListItem criteria = new ListItem();
			criterias.add(criteria);
			
			criteria.add(new StringBuilder(iwrb.getLocalizedString(searchCriteria.getId(), searchCriteria.getId())).append(CoreConstants.COLON)
						.append(CoreConstants.SPACE).append(searchCriteria.getValue()).toString());
		}
		
		return container;
	}
	
	public UIComponent getCasesList(IWContext iwc, PagedDataCollection<CasePresentation> cases, CaseListPropertiesBean properties) {		
		int pageSize = properties.getPageSize();
		int page = properties.getPage();
		
		String type = properties.getType();
		String instanceId = properties.getInstanceId();
		String componentId = properties.getComponentId();
		
		boolean showStatistics = properties.isShowStatistics();
		
		User currentUser = iwc.getCurrentUser();
		
		Collection<CasePresentation> filteredList = new ArrayList<CasePresentation>();
		Collection<CasePresentation> casesInList = cases == null ? null : cases.getCollection();
		for (CasePresentation casePresentation : casesInList) {
			if (casePresentation.getCode().equals(CasesConstants.CASE_CODE_KEY)) {
				try {
					GeneralCase genCase = getCasesBusiness(iwc).getGeneralCase(casePresentation.getPrimaryKey());
					CaseCategory category = genCase.getCaseCategory();
					Group handlerGroup = category.getHandlerGroup();
					if (currentUser.hasRelationTo(handlerGroup)) {
						filteredList.add(casePresentation);
					}
				}
				catch (FinderException e) {
					filteredList.add(casePresentation);
				}
				catch (RemoteException re) {
					throw new IBORuntimeException(re);
				}
			}
			else {
				filteredList.add(casePresentation);
			}
		}
		casesInList = filteredList;
		
		String emailAddress = getDefaultEmail();
		
		boolean descriptionIsEditable = isDescriptionEditable(type, iwc.isSuperAdmin());
		
		boolean searchResults = isSearchResultsList(type);
		Layer container = getCasesListContainer(searchResults);

		addProperties(container, properties);
		
		int totalCases = (casesInList == null || casesInList.isEmpty()) ? 0 : casesInList.size();

		if (pageSize > 0 && instanceId != null && componentId != null && totalCases > 0) {
			PresentationUtil.addStyleSheetToHeader(iwc, iwc.getIWMainApplication().getBundle(
					ProcessConstants.IW_BUNDLE_IDENTIFIER).getVirtualPathWithFileNameString("style/process.css"));
			Layer navigationLayer = new Layer(Layer.DIV);
			navigationLayer.setStyleClass("caseNavigation");
			container.add(navigationLayer);
			container.add(new CSSSpacer());

			IWResourceBundle resourceBundle = iwc.getIWMainApplication().getBundle(
					ProcessConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);

			ListNavigator navigator = new ListNavigator("userCases", totalCases);
			navigator.setFirstItemText(resourceBundle.getLocalizedString("page", "Page") + ":");
			navigator.setDropdownEntryName(resourceBundle.getLocalizedString("cases", "cases"));
			navigator.setPageSize(pageSize);
			navigator.setCurrentPage(page);
			StringBuilder navigationParams = new StringBuilder();
			navigationParams.append("'").append(instanceId).append("'");
			navigationParams.append(",'").append(componentId).append("'");
			navigator.setNavigationFunction("gotoCasesListPage('#PAGE#','" + pageSize + "'," + navigationParams + ");");
			navigator.setDropdownFunction("changeCasesListPageSize(this.value, " + navigationParams + ");");
			navigationLayer.add(navigator);
		}
		IWResourceBundle iwrb = getResourceBundle(iwc);
		CasesBusiness casesBusiness = getCasesBusiness(iwc);
		if (casesBusiness == null) {
			container.add(new Heading3(iwrb.getLocalizedString("cases_list.can_not_get_cases_list", "Sorry, error occurred - can not generate cases list.")));
			return container;
		}
		
		Layer casesContainer = createHeader(iwc, container, totalCases, searchResults, properties);
		
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

		for (CasePresentation theCase: casesInList) {
			caseContainer = addRowToCasesList(iwc, casesBodyContainer, theCase, caseStatusReview, l, false, rowsCounter, null, emailAddress, descriptionIsEditable,
					properties);
			rowsCounter++;
		}
		if (caseContainer != null) {
			caseContainer.setStyleClass(lastRowStyle);
		}
		
		if (showStatistics) {
			addStatistics(iwc, container, casesInList);
		}
		
		return container;
	}
	
	private void addStatistics(IWContext iwc, Layer container, Collection<CasePresentation> cases) {
		container.add(new CSSSpacer());
		
		Layer statisticsContainer = new Layer();
		container.add(statisticsContainer);
		statisticsContainer.setStyleClass("casesListCasesStatisticsContainer");
		
		statisticsContainer.add(getCasesStatistics(iwc, cases));
	}
	
	private void addProperties(Layer container, CaseListPropertiesBean properties) {
		container.getId();
		
		if (!StringUtil.isEmpty(properties.getInstanceId())) {
			HiddenInput input = new HiddenInput("casesListInstanceIdProperty", properties.getInstanceId());
			input.setStyleClass("casesListInstanceIdProperty");
			container.add(input);
		}
		
		addStatusesProperties(container, "casesListStatusesToShow", properties.getStatusesToShow());
		addStatusesProperties(container, "casesListStatusesToHide", properties.getStatusesToHide());
	}
	
	private void addStatusesProperties(Layer container, String className, List<String> statuses) {
		HiddenInput status = new HiddenInput(className);
		status.setStyleClass(className);
		status.setValue(ListUtil.isEmpty(statuses) ? CoreConstants.EMPTY : ListUtil.convertListOfStringsToCommaseparatedString(statuses));
		container.add(status);
	}
	
	@SuppressWarnings("unchecked")
	public UIComponent getUserCasesList(IWContext iwc, PagedDataCollection<CasePresentation> cases, Map pages, CaseListPropertiesBean properties) {
		int pageSize = properties.getPageSize();
		int page = properties.getPage();
		
		String type = properties.getType();
		String instanceId = properties.getInstanceId();
		String componentId = properties.getComponentId();

		boolean showStatistics = properties.isShowStatistics();
		
		Collection<CasePresentation> casesInList = cases.getCollection();
		
		String emailAddress = getDefaultEmail(); 
		
		boolean descriptionIsEditable = isDescriptionEditable(type, iwc.isSuperAdmin());
		
		boolean searchResults = isSearchResultsList(type);
		Layer container = getCasesListContainer(searchResults);
		
		addProperties(container, properties);
		
		if (pageSize > 0 && instanceId != null && componentId != null) {
			PresentationUtil.addStyleSheetToHeader(iwc, iwc.getIWMainApplication().getBundle(
					ProcessConstants.IW_BUNDLE_IDENTIFIER).getVirtualPathWithFileNameString("style/process.css"));
			Layer navigationLayer = new Layer(Layer.DIV);
			navigationLayer.setStyleClass("caseNavigation");
			container.add(navigationLayer);
			container.add(new CSSSpacer());

			IWResourceBundle resourceBundle = iwc.getIWMainApplication().getBundle(
					ProcessConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);

			ListNavigator navigator = new ListNavigator("userCases", cases.getTotalCount());
			navigator.setFirstItemText(resourceBundle.getLocalizedString("page", "Page") + ":");
			navigator.setDropdownEntryName(resourceBundle.getLocalizedString("cases", "cases"));
			navigator.setPageSize(pageSize);
			navigator.setCurrentPage(page);
			StringBuilder navigationParams = new StringBuilder();
			navigationParams.append("'").append(instanceId).append("'");
			navigationParams.append(",'").append(componentId).append("'");
			navigator.setNavigationFunction("gotoCasesListPage('#PAGE#','" + pageSize + "'," + navigationParams + ");");
			navigator.setDropdownFunction("changeCasesListPageSize(this.value, " + navigationParams + ");");
			navigationLayer.add(navigator);
		}

		IWResourceBundle iwrb = getResourceBundle(iwc);
		CasesBusiness casesBusiness = getCasesBusiness(iwc);
		if (casesBusiness == null) {
			container.add(new Heading3(iwrb.getLocalizedString("cases_list.can_not_get_cases_list", "Sorry, error occurred - can not generate cases list.")));
			return container;
		}
		
		int totalCases = (casesInList == null || casesInList.isEmpty()) ? 0 : casesInList.size();
		
		Layer casesContainer = createHeader(iwc, container, totalCases, searchResults, properties);
		
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
		for (CasePresentation theCase: casesInList) {			
			caseContainer = addRowToCasesList(iwc, casesBodyContainer, theCase, caseStatusReview, l, true, rowsCounter, pages, emailAddress, descriptionIsEditable,
					properties);
			rowsCounter++;
		}
		caseContainer.setStyleClass(lastRowStyle);

		if (showStatistics) {
			addStatistics(iwc, container, casesInList);
		}
		
		return container;
	}
	
	private String getDefaultEmail() {
		try {
			return IWMainApplication.getDefaultIWMainApplication().getSettings().getProperty(CoreConstants.PROP_SYSTEM_ACCOUNT);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private Link getLinkToViewUserCase(IWContext iwc, CasePresentation theCase, Image viewCaseImage, Map pages, String caseCode,
			CaseStatus caseStatus, boolean addCredentialsToExernalUrls) {
		CaseBusiness caseBusiness = null;
		try {
			caseBusiness = (CaseBusiness) IBOLookup.getServiceInstance(iwc, CaseBusiness.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		if (caseBusiness == null) {
			return null;
		}

		ICPage page = getPage(pages, caseCode, caseStatus == null ? null : caseStatus.getStatus());
		String caseUrl = theCase.getUrl();

		if (page != null) {
			Link link = new Link(viewCaseImage);
			link.setStyleClass("caseEdit");
			link.setToolTip(getToolTipForLink(iwc));

			try {
				link.addParameter(caseBusiness.getSelectedCaseParameter(), theCase.getPrimaryKey().toString());
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (EJBException e) {
				e.printStackTrace();
			}
			link.setPage(page);
			return link;
		} else if (caseUrl != null) {
			Link link = new Link(viewCaseImage, caseUrl);
			link.setStyleClass("caseEdit");
			link.setToolTip(getResourceBundle(iwc).getLocalizedString("view_case", "View case"));
			if (addCredentialsToExernalUrls) {
				try {
					getCredentialBusiness(iwc).addCredentialsToLink(link, iwc);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			return link;
		}

		return null;
	}
	
	@SuppressWarnings("unchecked")
	private ICPage getPage(Map pages, String caseCode, String caseStatus) {
		if (pages == null) {
			return null;
		}
		
		try {
			Object object = pages.get(caseCode);
			if (object instanceof ICPage) {
				return (ICPage) object;
			}
			else if (object instanceof Map) {
				Map statusMap = (Map) object;
				return (ICPage) statusMap.get(caseStatus);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String getToolTipForLink(IWContext iwc) {
		return getResourceBundle(iwc).getLocalizedString("view_case", "View case");
	}
	
	private Link getProcessLink(IWContext iwc, PresentationObject object, CasePresentation theCase) {
		Link process = new Link(object);
		
		WebContext webContext = WebContextFactory.get();
		if (webContext != null) {
			process.setURL(webContext.getCurrentPage());
		}
		
		process.setToolTip(getToolTipForLink(iwc));
		
		process.addParameter(CasesProcessor.PARAMETER_CASE_PK, theCase.getPrimaryKey().toString());
		process.addParameter(UserCases.PARAMETER_ACTION, CasesProcessor.ACTION_PROCESS);
		
		process.setStyleClass("old_casesListBodyContainerItemLink");

		return process;
	}
	
	private void addResources(String caseId, IWContext iwc, IWBundle bundle, String type) {
		Web2Business web2Business = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		
		List<String> scripts = new ArrayList<String>();
		scripts.add(web2Business.getBundleURIToJQueryLib());
		scripts.add(web2Business.getBundleURIToJQueryPlugin(JQueryPlugin.EDITABLE));
		scripts.add(bundle.getVirtualPathWithFileNameString(CasesConstants.CASES_LIST_HELPER_JAVA_SCRIPT_FILE));
		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);
		scripts.add("/dwr/interface/CasesEngine.js");
		
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
		action.append(", [");
		
		//	Localizations: array as parameter
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		action.append("'").append(iwrb.getLocalizedString("click_to_edit", "Click to edit...")).append("'");
		action.append(", '").append(iwrb.getLocalizedString("error_occurred_confirm_to_reload_page",
				"Oops! Error occurred. Reloading current page might help to avoid it. Do you want to reload current page?")).append("'");
		action.append(", '").append(iwrb.getLocalizedString("loading", "Loading...")).append("'");
		
		action.append("], true);");	//	TODO: set false after debug
		
		if (!CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			action = new StringBuilder("jQuery(window).load(function() {").append(action.toString()).append("});");
		}
		
		//	Adding resources
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
		PresentationUtil.addJavaScriptActionToBody(iwc, "if(CASE_GRID_CASE_PROCESSOR_TYPE == null) var CASE_GRID_CASE_PROCESSOR_TYPE = \"" + type.toString() +
				"\";");
		PresentationUtil.addJavaScriptActionToBody(iwc, action.toString());
	}
	
	private CasesBusiness getCasesBusiness(IWApplicationContext iwac) {
		try {
			return (CasesBusiness) IBOLookup.getServiceInstance(iwac, CasesBusiness.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private CredentialBusiness getCredentialBusiness(IWApplicationContext iwac) {
		
		try {
			return (CredentialBusiness) IBOLookup.getServiceInstance(iwac, CredentialBusiness.class);
		}
		catch (IBOLookupException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private IWBundle getBundle(IWContext iwc) {
		return iwc.getIWMainApplication().getBundle(CasesConstants.IW_BUNDLE_IDENTIFIER);
	}
	
	private IWResourceBundle getResourceBundle(IWContext iwc) {
		if (iwc == null) {
			iwc = CoreUtil.getIWContext();
		}
		return getBundle(iwc).getResourceBundle(iwc);
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

	public UIComponent getCaseManagerView(IWContext iwc, Integer caseId, String type) {
		
		try {
			
			Case theCase = getCasesBusiness(iwc).getCase(caseId);
			
			CasesRetrievalManager caseManager;
			
			if(theCase.getCaseManagerType() != null)
				caseManager = getCaseManagersProvider().getCaseManager();
			else 
				caseManager = null;
			
			if(caseManager != null) {
				
				UIComponent caseAssets = caseManager.getView(iwc, caseId, type, theCase.getCaseManagerType());
				
				if(caseAssets != null)
					return caseAssets;
				else
					Logger.getLogger(getClass().getName()).log(Level.WARNING, "No case assets component resolved from case manager: " + caseManager.getType() + 
							" by case pk: "+theCase.getPrimaryKey().toString());
			} else
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "No case manager resolved by type="+theCase.getCaseManagerType());
			
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception while resolving case manager view", e);
		}
		
		return null;
	}

	public CaseManagersProvider getCaseManagersProvider() {
		return caseManagersProvider;
	}

	@Autowired
	public void setCaseManagersProvider(CaseManagersProvider caseManagersProvider) {
		this.caseManagersProvider = caseManagersProvider;
	}

	public UIComponent getCasesStatistics(IWContext iwc, Collection<CasePresentation> cases) {
		CasesStatistics statistics = new CasesStatistics();
		statistics.setCases(cases);
		statistics.setUseStatisticsByCaseType(Boolean.FALSE);
		return statistics;
	}

	public String getSendEmailImage() {
		return IWMainApplication.getDefaultIWMainApplication().getBundle(CasesConstants.IW_BUNDLE_IDENTIFIER).getVirtualPathWithFileNameString("images/email.png");
	}

	private String getTitleSendEmail(IWResourceBundle iwrb) {
		String notFoundValue = "Send e-mail";
		return iwrb == null ? notFoundValue : iwrb.getLocalizedString("send_email", notFoundValue);
	}
	
	public String getTitleSendEmail() {
		return getTitleSendEmail(getResourceBundle(CoreUtil.getIWContext()));
	}
}