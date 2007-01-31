package is.idega.idegaweb.egov.cases.business;


import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Locale;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseStatus;
import com.idega.business.IBOService;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;

public interface CasesBusiness extends IBOService, CaseBusiness {
	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getLocalizedCaseDescription
	 */
	public String getLocalizedCaseDescription(Case theCase, Locale locale) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getLocalizedCaseStatusDescription
	 */
	public String getLocalizedCaseStatusDescription(Case theCase, CaseStatus status, Locale locale) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getGeneralCase
	 */
	public GeneralCase getGeneralCase(Object casePK) throws FinderException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getOpenCases
	 */
	public Collection getOpenCases(Collection groups) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getMyCases
	 */
	public Collection getMyCases(User handler) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getClosedCases
	 */
	public Collection getClosedCases(Collection groups) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCasesByUsers
	 */
	public Collection getCasesByUsers(Collection users) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseCategory
	 */
	public CaseCategory getCaseCategory(Object caseCategoryPK) throws FinderException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getAllCaseCategories
	 */
	public Collection getAllCaseCategories() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseCategories
	 */
	public Collection getCaseCategories() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getSubCategories
	 */
	public Collection getSubCategories(CaseCategory category) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseLogs
	 */
	public Collection getCaseLogs(GeneralCase theCase) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#removeCaseCategory
	 */
	public void removeCaseCategory(Object caseCategoryPK) throws FinderException, RemoveException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseType
	 */
	public CaseType getCaseType(Object caseTypePK) throws FinderException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseTypes
	 */
	public Collection getCaseTypes() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getFirstAvailableCaseType
	 */
	public CaseType getFirstAvailableCaseType() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#removeCaseType
	 */
	public void removeCaseType(Object caseTypePK) throws FinderException, RemoveException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#storeGeneralCase
	 */
	public void storeGeneralCase(User sender, Object caseCategoryPK, Object caseTypePK, String message, String type, boolean isPrivate, IWResourceBundle iwrb) throws CreateException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#handleCase
	 */
	public void handleCase(Object casePK, Object caseCategoryPK, Object caseTypePK, String status, User performer, String reply, IWContext iwc) throws FinderException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#takeCase
	 */
	public void takeCase(Object casePK, User performer, IWContext iwc) throws FinderException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#reactivateCase
	 */
	public void reactivateCase(Object casePK, User performer, IWContext iwc) throws FinderException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#storeCaseCategory
	 */
	public CaseCategory storeCaseCategory(Object caseCategoryPK, Object parentCaseCategoryPK, String name, String description, Object groupPK, int localeId, int order) throws FinderException, CreateException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#storeCaseType
	 */
	public void storeCaseType(Object caseTypePK, String name, String description, int order) throws FinderException, CreateException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#canDeleteCase
	 */
	public boolean canDeleteCase(Case theCase) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#useSubCategories
	 */
	public boolean useSubCategories() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#useTypes
	 */
	public boolean useTypes() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#allowPrivateCases
	 */
	public boolean allowPrivateCases() throws RemoteException;
}