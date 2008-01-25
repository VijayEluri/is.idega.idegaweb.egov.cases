package is.idega.idegaweb.egov.cases.bpm;

import com.idega.jbpm.exe.BPMManagersFactory;
import com.idega.jbpm.exe.ProcessManager;
import com.idega.jbpm.exe.ViewManager;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/01/25 15:23:55 $ by $Author: civilis $
 */
public class CasesBPMManagersCreator extends BPMManagersFactory {
	
	public static final String MANAGERS_TYPE = "cases";
	private static final String BEAN_IDENTIFIER = "casesBPMManagersCreator";
	private ProcessManager processManager;
	private ViewManager viewManager;

	public ViewManager getViewManager() {
		
		return viewManager;
	}
	
	public ProcessManager getProcessManager() {
		
		return processManager;
	}
	
	public String getManagersType() {
		
		return MANAGERS_TYPE; 
	}
	
	public String getBeanIdentifier() {

		return BEAN_IDENTIFIER;
	}

	public void setProcessManager(ProcessManager processManager) {
		this.processManager = processManager;
	}

	public void setViewManager(ViewManager viewManager) {
		this.viewManager = viewManager;
	}
}