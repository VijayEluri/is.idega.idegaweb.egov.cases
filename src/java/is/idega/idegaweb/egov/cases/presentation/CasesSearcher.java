package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.util.CasesConstants;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.business.CaseManager;
import com.idega.block.process.business.CaseManagersProvider;
import com.idega.block.process.data.CaseStatus;
import com.idega.block.process.presentation.beans.GeneralCasesListBuilder;
import com.idega.block.web2.business.Web2Business;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.AdvancedPropertyComparator;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.IWDatePicker;
import com.idega.presentation.ui.InterfaceObject;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SelectOption;
import com.idega.presentation.ui.TextInput;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.webface.WFUtil;

/**
 * Cases searcher. MUST be included in the same page as CasesList!
 * @author <a href="mailto:valdas@idega.com>Valdas Žemaitis</a>
 * Created: 2008.06.27
 * @version '$Revision 1.5'
 * Last modified: 2008.06.27 11:37:14 by: valdas
 */
public class CasesSearcher extends CasesBlock {
	
	private static final String PARAMETER_PROCESS_ID = "cf_prm_process_id";
	private static final String PARAMETER_CASE_STATUS = "cf_prm_case_status";
	private static final String PARAMETER_CASE_LIST_TYPE = "cf_prm_case_list_type";
	private static final String PARAMETER_CASE_CONTACT = "cf_prm_case_contact";

	private String textInputStyleClass = "textinput";
	private String buttonStyleClass = "button";
	
	private String listType;
	
	private boolean showAllStatuses;
	
	@Override
	protected void present(IWContext iwc) throws Exception {
		IWBundle bundle = iwc.getIWMainApplication().getBundle(CasesConstants.IW_BUNDLE_IDENTIFIER);
		Web2Business web2Business = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		
		List<String> scripts = new ArrayList<String>();
		scripts.add(web2Business.getBundleURIToJQueryLib());
		scripts.add(bundle.getVirtualPathWithFileNameString(CasesConstants.CASES_LIST_HELPER_JAVA_SCRIPT_FILE));
		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);
		scripts.add("/dwr/interface/CasesEngine.js");
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
		
		IWResourceBundle iwrb = getResourceBundle();
		
		Layer container = new Layer();
		add(container);
		container.setStyleClass("casesSearcherBoxStyleClass");
		
		container.add(new Heading1(iwrb.getLocalizedString("search_for_cases", "Search")));
		
		Layer inputsContainer = new Layer();
		container.add(inputsContainer);
		inputsContainer.setStyleClass("casesSearcherInputsBoxStyleClass");
		
		TextInput caseNumber = getTextInput(CaseFinder.PARAMETER_CASE_NUMBER, null);

		TextInput caseDescription = getTextInput(CaseFinder.PARAMETER_TEXT, null);
		
		TextInput name = getTextInput(CaseFinder.PARAMETER_NAME, null);

		TextInput personalID = getTextInput(CaseFinder.PARAMETER_PERSONAL_ID, null);
		
		TextInput contact = getTextInput(PARAMETER_CASE_CONTACT, iwrb.getLocalizedString("cases_search_enter_name_email_or_phone",
				"Contact's name, e-mail or phone number"));
		
		String listType = getListType();
		HiddenInput listTypeInput = new HiddenInput(PARAMETER_CASE_LIST_TYPE, StringUtil.isEmpty(listType) ? CoreConstants.EMPTY : listType);
		inputsContainer.add(listTypeInput);

		//	Case number
		addFormItem(inputsContainer, iwrb.getLocalizedString("case_nr", "Case nr."), caseNumber);
		
		// Case description
		addFormItem(inputsContainer, iwrb.getLocalizedString("description", "Description"), caseDescription);

		//	Case name
		addFormItem(inputsContainer, iwrb.getLocalizedString("name", "Name"), name);

		//	Case personal id
		addFormItem(inputsContainer, iwrb.getLocalizedString("personal_id", "Personal ID"), personalID);
		
		//	Case contacts
		addFormItem(inputsContainer, iwrb.getLocalizedString("contact", "Contact"), contact);
		
		//	Process
		DropdownMenu processes = getDropdownForProcess(iwc);
		addFormItem(inputsContainer, iwrb.getLocalizedString("cases_search_select_process", "Process"), processes);

		//	Status
		DropdownMenu statuses = getDropdownForStatus(iwc);
		addFormItem(inputsContainer, iwrb.getLocalizedString("status", "Status"), statuses);
		
		//	Date range
		IWDatePicker dateRange = getDateRange(iwc);
		addFormItem(inputsContainer, iwrb.getLocalizedString("date_range", "Date range"), dateRange);
		
		inputsContainer.add(new CSSSpacer());

		Layer buttonsContainer = new Layer(Layer.DIV);
		buttonsContainer.setStyleClass("buttonLayer");
		inputsContainer.add(buttonsContainer);

		StringBuilder parameters  = new StringBuilder("['").append(GeneralCasesListBuilder.MAIN_CASES_LIST_CONTAINER_STYLE).append("', '");
		parameters.append(caseNumber.getId()).append("', '").append(name.getId()).append("', '").append(personalID.getId()).append("', '");
		parameters.append(processes.getId()).append("', '").append(statuses.getId()).append("', '").append(dateRange.getId()).append("', '");
		parameters.append(iwrb.getLocalizedString("searching", "Searching...")).append("', '").append(caseDescription.getId()).append("', '");
		parameters.append(listTypeInput.getId()).append("', '").append(contact.getId()).append("']");
		StringBuilder action = new StringBuilder("registerCasesSearcherBoxActions('").append(inputsContainer.getId()).append("', ")
												.append(parameters.toString()).append(");");
		if (!CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			action = new StringBuilder("jQuery(window).load(function() {").append(action.toString()).append("});");
		}
		PresentationUtil.addJavaScriptActionToBody(iwc, action.toString());
		
		GenericButton searchButton = new GenericButton(iwrb.getLocalizedString("search_for_cases", "Search"));
		searchButton.setStyleClass(buttonStyleClass);
		StringBuilder searchAction = new StringBuilder("searchForCases(").append(parameters.toString()).append(");");
		searchButton.setOnClick(searchAction.toString());
		buttonsContainer.add(searchButton);
		
		GenericButton clearSearch = new GenericButton(iwrb.getLocalizedString("clear_search_results", "Clear"));
		clearSearch.setOnClick(new StringBuilder("clearSearchForCases(").append(parameters.toString()).append(");").toString());
		clearSearch.setStyleClass(buttonStyleClass);
		buttonsContainer.add(clearSearch);
	}
	
	private TextInput getTextInput(String name, String toolTip) {
		TextInput input = new TextInput(name);
		input.setStyleClass(textInputStyleClass);
		
		if (!StringUtil.isEmpty(toolTip)) {
			input.setToolTip(toolTip);
		}
		
		return input;
	}
	
	private void fillDropdown(Locale locale, DropdownMenu menu, List<AdvancedProperty> options, AdvancedProperty firstElement, String selectedElement) {
		if (locale == null) {
			locale = Locale.ENGLISH;
		}
		Collections.sort(options, new AdvancedPropertyComparator(locale));
		
		for (AdvancedProperty option: options) {
			menu.addOption(new SelectOption(option.getValue(), option.getId()));
		}
		menu.addFirstOption(new SelectOption(firstElement.getValue(), firstElement.getId()));
		
		if (selectedElement != null) {
			menu.setSelectedElement(selectedElement);
		}
	}
	
	private DropdownMenu getDropdownForProcess(IWContext iwc) {
		DropdownMenu menu = new DropdownMenu(PARAMETER_PROCESS_ID);
		String selectedProcess = iwc.isParameterSet(PARAMETER_PROCESS_ID) ? iwc.getParameter(PARAMETER_PROCESS_ID) : null;
		
		CaseManagersProvider caseManagersProvider = (CaseManagersProvider) WFUtil.getBeanInstance(CaseManagersProvider.beanIdentifier);
		if (caseManagersProvider == null) {
			return menu;
		}
		List<CaseManager> caseManagers = caseManagersProvider.getCaseManagers();
		if (caseManagers == null || caseManagers.isEmpty()) {
			return menu;
		}
		
		List<AdvancedProperty> allProcesses = new ArrayList<AdvancedProperty>();
		for (CaseManager caseManager: caseManagers) {
			List<AdvancedProperty> processes = caseManager.getAllCaseProcesses();
			if (processes != null && !processes.isEmpty()) {
				allProcesses.addAll(processes);
			}
		}
		if (allProcesses.isEmpty()) {
			return menu;
		}
		
		IWResourceBundle iwrb = getResourceBundle();
		
		allProcesses.add(0, new AdvancedProperty(CasesConstants.GENERAL_CASES_TYPE, iwrb.getLocalizedString("general_cases", "General cases")));
		fillDropdown(iwc.getCurrentLocale(), menu, allProcesses, new AdvancedProperty(String.valueOf(-1),
				iwrb.getLocalizedString("cases_search_select_process", "Select process")), selectedProcess);
		
		return menu;
	}
	
	@SuppressWarnings("unchecked")
	private DropdownMenu getDropdownForStatus(IWContext iwc) {
		DropdownMenu menu = new DropdownMenu(PARAMETER_CASE_STATUS);
		String selectedStatus = iwc.isParameterSet(PARAMETER_CASE_STATUS) ? iwc.getParameter(PARAMETER_CASE_STATUS) : null;
		
		CaseBusiness caseBusiness = getCasesBusiness();
		if (caseBusiness == null) {
			menu.setDisabled(true);
			return menu;
		}
		
		Collection<CaseStatus> allStatuses = null;
		try {
			allStatuses = caseBusiness.getCaseStatuses();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (allStatuses == null || allStatuses.isEmpty()) {
			menu.setDisabled(true);
			return menu;
		}
		
		Locale l = iwc.getCurrentLocale();
		if (l == null) {
			l = Locale.ENGLISH;
		}
		
		boolean addStatus = true;;
		String localizedStatus = null;
		List<AdvancedProperty> statuses = new ArrayList<AdvancedProperty>();
		for (CaseStatus status: allStatuses) {
			addStatus = true;
			
			try {
				localizedStatus = caseBusiness.getLocalizedCaseStatusDescription(null, status, l);
				if (!showAllStatuses && localizedStatus.equals(status.getStatus())) {
					addStatus = false;
				}
				
				if (addStatus) {
					statuses.add(new AdvancedProperty(status.getStatus(), localizedStatus));
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		fillDropdown(l, menu, statuses, new AdvancedProperty(String.valueOf(-1), getResourceBundle().getLocalizedString("select_status", "Select status")), selectedStatus);
		
		return menu;
	}
	
	private IWDatePicker getDateRange(IWContext iwc) {
		IWDatePicker datePicker = new IWDatePicker();
		
		datePicker.setDateRange(true);
		datePicker.setUseCurrentDateIfNotSet(false);
		
		return datePicker;
	}
	
	private void addFormItem(Layer layer, String localizedLabelText, InterfaceObject input) {
		Layer element = new Layer(Layer.DIV);
		layer.add(element);
		element.setStyleClass("formItem shortFormItem");
		
		Label label = null;
		label = new Label(localizedLabelText, input);
		element.add(label);
		element.add(input);		
	}

	public String getListType() {
		return listType;
	}

	public void setListType(String listType) {
		this.listType = listType;
	}

	public boolean isShowAllStatuses() {
		return showAllStatuses;
	}

	public void setShowAllStatuses(boolean showAllStatuses) {
		this.showAllStatuses = showAllStatuses;
	}

}
