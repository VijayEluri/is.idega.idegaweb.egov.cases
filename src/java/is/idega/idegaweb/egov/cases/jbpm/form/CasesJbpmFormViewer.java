package is.idega.idegaweb.egov.cases.jbpm.form;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.w3c.dom.Document;

import com.idega.block.form.presentation.FormViewer;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2007/11/14 13:08:16 $ by $Author: civilis $
 */
public class CasesJbpmFormViewer extends IWBaseComponent {
	
	
	public static final String PROCESS_DEFINITION_PROPERTY = "processDefinitionId";
	public static final String PROCESS_INSTANCE_PROPERTY = "processInstanceId";
	public static final String CASES_JBPM_FORM_MANAGER_PROPERTY = "casesJbpmFormManager";
	
	private static final String FORMVIEWER_FACET = "formviewer";
	
	private String processDefinitionId;
	private String processInstanceId;
	private CasesJbpmFormManager casesJbpmFormManager;
	
    
	public CasesJbpmFormManager getCasesJbpmFormManager() {
		return casesJbpmFormManager;
	}
	
	public CasesJbpmFormManager getCasesJbpmFormManager(FacesContext context) {
		
		CasesJbpmFormManager casesJbpmProcess = getCasesJbpmFormManager();
		
		if(casesJbpmProcess == null) {
			
			casesJbpmProcess = getValueBinding(CASES_JBPM_FORM_MANAGER_PROPERTY) != null ? (CasesJbpmFormManager)getValueBinding(CASES_JBPM_FORM_MANAGER_PROPERTY).getValue(context) : null;
			setCasesJbpmFormManager(casesJbpmProcess);
		}
		
		return casesJbpmProcess;
	}

	public void setCasesJbpmFormManager(CasesJbpmFormManager casesJbpmProcess) {
		this.casesJbpmFormManager = casesJbpmProcess;
	}

	public String getProcessDefinitionId() {
		
		return processDefinitionId;
	}
	
	public String getProcessDefinitionId(FacesContext context) {

		String processDefinitionId = getProcessDefinitionId();
		
		if(processDefinitionId == null) {
			
			processDefinitionId = getValueBinding(PROCESS_DEFINITION_PROPERTY) != null ? (String)getValueBinding(PROCESS_DEFINITION_PROPERTY).getValue(context) : (String)context.getExternalContext().getRequestParameterMap().get(PROCESS_DEFINITION_PROPERTY);
			processDefinitionId = "".equals(processDefinitionId) ? null : processDefinitionId;
			setProcessDefinitionId(processDefinitionId);
		}
		
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		
		this.processDefinitionId = processDefinitionId;
	}

	public String getProcessInstanceId() {
		
		return processInstanceId;
	}
	
	public String getProcessInstanceId(FacesContext context) {

		String processInstanceId = getProcessInstanceId();
		
		if(processInstanceId == null) {
			
			processInstanceId = getValueBinding(PROCESS_INSTANCE_PROPERTY) != null ? (String)getValueBinding(PROCESS_INSTANCE_PROPERTY).getValue(context) : (String)context.getExternalContext().getRequestParameterMap().get(PROCESS_INSTANCE_PROPERTY);
			processInstanceId = "".equals(processInstanceId) ? null : processInstanceId;
			setProcessInstanceId(processInstanceId);
		}
		
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		
		this.processInstanceId = processInstanceId;
	}

	public CasesJbpmFormViewer() {
		
		super();
		setRendererType(null);
	}
	
	@Override
	protected void initializeComponent(FacesContext context) {
		
	}
	
	@Override
	public boolean getRendersChildren() {
		return true;
	}
	
	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		
		super.encodeBegin(context);
		
		String processDefinitionId = getProcessDefinitionId(context);
		String processInstanceId = getProcessInstanceId(context);
		
		FormViewer formviewer = null;
		
		if(processDefinitionId != null)
			formviewer = loadFormViewerFromDefinition(context, processDefinitionId);
			
		else if(processInstanceId != null) {
			
			formviewer = loadFormViewerFromInstance(context, processInstanceId);
		}
			
		
		@SuppressWarnings("unchecked")
		Map<String, UIComponent> facets = (Map<String, UIComponent>)getFacets();
		
		if(formviewer != null)
			facets.put(FORMVIEWER_FACET, formviewer);
		else
			facets.remove(FORMVIEWER_FACET);
	}
	
	private FormViewer loadFormViewerFromDefinition(FacesContext context, String processDefinitionId) {

		int initiatorId = IWContext.getIWContext(context).getCurrentUserId();
		
		Document xformsDoc = getCasesJbpmFormManager(context).loadDefinitionForm(context, Long.parseLong(processDefinitionId), initiatorId);
		FormViewer formviewer = new FormViewer();
		formviewer.setRendered(true);
		formviewer.setXFormsDocument(xformsDoc);
		
		return formviewer;
	}
	
	private FormViewer loadFormViewerFromInstance(FacesContext context, String processInstanceId) {

		Document xformsDoc = getCasesJbpmFormManager(context).loadInstanceForm(context, Long.parseLong(processInstanceId));
		
		FormViewer formviewer = new FormViewer();
		formviewer.setRendered(true);
		formviewer.setXFormsDocument(xformsDoc);
		return formviewer;
	}
	
	@Override
	public void encodeChildren(FacesContext context) throws IOException {
		
		super.encodeChildren(context);

		@SuppressWarnings("unchecked")
		Map<String, UIComponent> facets = (Map<String, UIComponent>)getFacets();
		FormViewer formviewer = (FormViewer)facets.get(FORMVIEWER_FACET);
		
		if(formviewer != null)
			renderChild(context, formviewer);
	}
	
	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		// TODO Auto-generated method stub
		super.encodeEnd(context);
	}
}