package is.idega.idegaweb.egov.cases.bpm;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;

import is.idega.idegaweb.egov.cases.business.CasesBusiness;

import com.idega.block.process.data.CaseStatus;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/01/25 15:23:55 $ by $Author: civilis $
 *
 */
public class CasesJbpmUtil {


	public Map<Locale, Map<String, String>> getCaseStatusesForHandling() {

		IWContext iwc = IWContext.getIWContext(FacesContext.getCurrentInstance());
		CasesBusiness casesBusiness = getCasesBusiness(iwc);
		
		try {

			Locale locale = iwc.getCurrentLocale();
			
			Map<String, String> caseStatuses = new HashMap<String, String>(3);
			CaseStatus status = casesBusiness.getCaseStatusPending();
			caseStatuses.put(status.getStatus(), casesBusiness.getLocalizedCaseStatusDescription(status, locale));
			status = casesBusiness.getCaseStatusWaiting();
			caseStatuses.put(status.getStatus(), casesBusiness.getLocalizedCaseStatusDescription(status, locale));
			status = casesBusiness.getCaseStatusReady();
			caseStatuses.put(status.getStatus(), casesBusiness.getLocalizedCaseStatusDescription(status, locale));
			
			Map<Locale, Map<String, String>> localizedCasesStatuses = new HashMap<Locale, Map<String,String>>(1);
			
//			FIXME: leave locale, when localization is fully implemented in forms.
			localizedCasesStatuses.put(true ? new Locale("en") : locale, caseStatuses);
			return localizedCasesStatuses;
			
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected CasesBusiness getCasesBusiness(IWApplicationContext iwac) {
		try {
			return (CasesBusiness) IBOLookup.getServiceInstance(iwac, CasesBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
}