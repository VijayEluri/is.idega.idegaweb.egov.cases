package is.idega.idegaweb.egov.cases.bpm;

import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.bpm.bundle.CasesJbpmFormsBundleManager;
import is.idega.idegaweb.egov.cases.util.CaseConstants;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.egov.cases.bpm.data.CasesJbpmDao;
import com.idega.jbpm.def.ProcessBundleManager;
import com.idega.util.CoreConstants;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/01/25 15:23:55 $ by $Author: civilis $
 *
 */
public class CasesJbpmProcess {
	
	private String formName;
	private String message;
	private String caseCategory;
	private String caseType;
	private String processDefinitionId;
	private String processInstanceId;
	private String chosenProcessDefinitionId;
	private String chosenProcessInstanceId;

	private String templateBundleLocation;
	private ProcessBundleManager processBundleManager;
	private CasesJbpmDao casesJbpmDao;
	
	private List<SelectItem> casesTypes = new ArrayList<SelectItem>();
	private List<SelectItem> casesCategories = new ArrayList<SelectItem>();
	private List<SelectItem> casesProcessesDefinitions = new ArrayList<SelectItem>();

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}
	
	public String createNewSimpleProcess() {
		
		if(getFormName() == null || getFormName().equals("")) {
		
			setMessage("Form name not set");
			return null;
		}
		
		if(getCaseCategory() == null || "".equals(getCaseCategory())) {
			setMessage("Case category not provided");
			return null;
		}
		
		if(getCaseType() == null || "".equals(getCaseType())) {
			setMessage("Case type not provided");
			return null;
		}
			
		try {
			FacesContext ctx = FacesContext.getCurrentInstance();
			IWMainApplication iwma = IWMainApplication.getIWMainApplication(ctx);
			Map<String, String> parameters = new HashMap<String, String>(2);
			parameters.put(CasesJbpmFormsBundleManager.caseCategoryIdParameter, getCaseCategory());
			parameters.put(CasesJbpmFormsBundleManager.caseTypeIdParameter, getCaseType());
			
			getProcessBundleManager().createBundle(ctx, iwma.getBundle(CaseConstants.IW_BUNDLE_IDENTIFIER), getTemplateBundleLocation(), getFormName(), parameters);
			
		} catch (IOException e) {
			setMessage("IO Exception occured");
			e.printStackTrace();
		} catch (Exception e) {
			setMessage("Exception occured");
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String getMessage() {
		return message == null ? CoreConstants.EMPTY : message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<SelectItem> getCasesTypes() {
		
		casesTypes.clear();
		
		try {
			
			@SuppressWarnings("unchecked")
			Collection<CaseType> types = getCasesBusiness(IWMainApplication.getIWMainApplication(FacesContext.getCurrentInstance()).getIWApplicationContext())
			.getCaseTypes();
			
			for (CaseType caseType : types) {
				
				SelectItem item = new SelectItem();
				
//				it is done in the same manner (toString for primary key), so anyway.. :\ 
				item.setValue(caseType.getPrimaryKey().toString());
				item.setLabel(caseType.getName());
				casesTypes.add(item);
			}
			
			return casesTypes;
			
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public void setCasesTypes(List<SelectItem> casesTypes) {
		this.casesTypes = casesTypes;
	}

	public List<SelectItem> getCasesCategories() {
		
		casesCategories.clear();
		
		try {
			
			@SuppressWarnings("unchecked")
			Collection<CaseCategory> categories = getCasesBusiness(IWMainApplication.getIWMainApplication(FacesContext.getCurrentInstance()).getIWApplicationContext())
			.getCaseCategories();
			
			for (CaseCategory caseCategory : categories) {
				
				SelectItem item = new SelectItem();
				
//				it is done in the same manner (toString for primary key), so anyway.. :\ 
				item.setValue(caseCategory.getPrimaryKey().toString());
				item.setLabel(caseCategory.getName());
				casesCategories.add(item);
			}
			
			return casesCategories;
			
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public void setCasesCategories(List<SelectItem> casesCategories) {
		this.casesCategories = casesCategories;
	}
	
	protected CasesBusiness getCasesBusiness(IWApplicationContext iwac) {
		try {
			return (CasesBusiness) IBOLookup.getServiceInstance(iwac, CasesBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	public String getCaseCategory() {
		return caseCategory;
	}

	public void setCaseCategory(String caseCategory) {
		this.caseCategory = caseCategory;
	}

	public String getCaseType() {
		return caseType;
	}

	public void setCaseType(String caseType) {
		this.caseType = caseType;
	}
	
	private void addDefaultSelectItem(List<SelectItem> selectItems) {
		
		SelectItem item = new SelectItem();
		
		item.setValue("");
		item.setLabel("No selection");
		
		selectItems.add(item);
	}
	
	public List<SelectItem> getSimpleCasesProcessesDefinitions() {

		casesProcessesDefinitions.clear();
		addDefaultSelectItem(casesProcessesDefinitions);
		
		try {
			List<Object[]> casesProcesses = getCasesJbpmDao().getSimpleProcessDefinitions();
			
			if(casesProcesses == null)
				return casesProcessesDefinitions;
			
			for (Object[] idAndName : casesProcesses) {
				
				SelectItem item = new SelectItem();
				
				item.setValue(String.valueOf(idAndName[0]));
				item.setLabel((String)idAndName[1]);
				casesProcessesDefinitions.add(item);
			}
			
			return casesProcessesDefinitions;
			
		} catch (Exception e) {
			setMessage("Exception occured");
			e.printStackTrace();
			casesProcessesDefinitions.clear();
			return casesProcessesDefinitions;
			
		}
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getChosenProcessDefinitionId() {
		return chosenProcessDefinitionId;
	}

	public void setChosenProcessDefinitionId(String chosenProcessDefinitionId) {
		this.chosenProcessDefinitionId = chosenProcessDefinitionId;
	}

	public String getChosenProcessInstanceId() {
		return chosenProcessInstanceId;
	}

	public void setChosenProcessInstanceId(String chosenProcessInstanceId) {
		this.chosenProcessInstanceId = chosenProcessInstanceId;
	}
	
	public void showProcessInitationForm() {
	
		setChosenProcessDefinitionId(getProcessDefinitionId());
	}
	
	public void showProcessProgressForm() {
		
		setChosenProcessInstanceId(getProcessInstanceId());
	}
	
	public String getTemplateBundleLocation() {
		return templateBundleLocation;
	}

	public void setTemplateBundleLocation(String templateBundleLocation) {
		this.templateBundleLocation = templateBundleLocation;
	}

	public ProcessBundleManager getProcessBundleManager() {
		return processBundleManager;
	}

	public void setProcessBundleManager(ProcessBundleManager processBundleManager) {
		this.processBundleManager = processBundleManager;
	}

	public CasesJbpmDao getCasesJbpmDao() {
		return casesJbpmDao;
	}

	public void setCasesJbpmDao(CasesJbpmDao casesJbpmDao) {
		this.casesJbpmDao = casesJbpmDao;
	}
}