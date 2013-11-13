package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.presentation.CasesBoardViewer;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardBean;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardTableBean;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;

public interface BoardCasesManager {
	
	public static final String BEAN_NAME = "boardCasesManager";

	/**
	 * 
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public List<CaseBoardBean> getAllSortedCases(
			IWContext iwc, 
			IWResourceBundle iwrb, 
			Collection<String> caseStatus, 
			String processName, 
			String uuid, 
			boolean isSubscribedOnly,
			boolean backPage,
			String taskName);

	/**
	 * 
	 * <p>Construcs data for table to be shown.</p>
	 * @param iwc - application context;
	 * @param caseStatus of cases to add to table, for example: 
	 * "BVJD, INPR, FINI, ...";
	 * @param processName - name ProcessDefinition object;
	 * @param customColumns - variable names, which should be shown as columns,
	 * for example: "string_caseIdentifier,string_caseDescription,..."
	 * @param uuid of {@link CasesBoardViewer} component;
	 * @return data for table to represent or <code>null</code> on failure.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public CaseBoardTableBean getTableData(IWContext iwc, 
			Collection<String> caseStatus, String processName, String uuid,
			boolean isSubscribedOnly, boolean useBasePage, String taskName);

	public AdvancedProperty getHandlerInfo(IWContext iwc, User handler);

	public String getLinkToTheTaskRedirector(IWContext iwc, String basePage, String caseId, Long processInstanceId, String backPage, String taskName);

	public List<AdvancedProperty> getAvailableVariables(String processName);

	public Map<Integer, List<AdvancedProperty>> getColumns(IWResourceBundle iwrb, String uuid);

	public List<String> getCustomColumns(String uuid);

	public boolean isColumnOfDomain(String currentColumn, String columnOfDomain);

	public int getIndexOfColumn(String column, String uuid);

	public Long getNumberValue(String value);

}