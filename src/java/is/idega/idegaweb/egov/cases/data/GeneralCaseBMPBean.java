/*
 * $Id$ Created on Oct 30, 2005
 * 
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.data;

import is.idega.idegaweb.egov.cases.util.CaseConstants;

import java.sql.Date;
import java.util.Collection;
import java.util.logging.Level;

import javax.ejb.FinderException;

import com.idega.block.process.data.AbstractCaseBMPBean;
import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseStatus;
import com.idega.core.file.data.ICFile;
import com.idega.data.IDOException;
import com.idega.data.IDORelationshipException;
import com.idega.data.query.CountColumn;
import com.idega.data.query.InCriteria;
import com.idega.data.query.MatchCriteria;
import com.idega.data.query.Order;
import com.idega.data.query.SelectQuery;
import com.idega.data.query.Table;
import com.idega.user.data.User;

public class GeneralCaseBMPBean extends AbstractCaseBMPBean implements Case, GeneralCase {

	private static final long serialVersionUID = 1213681239602561355L;

	private static final String ENTITY_NAME = "comm_case";

	private static final String COLUMN_MESSAGE = "message";
	private static final String COLUMN_REPLY = "reply";
	private static final String COLUMN_CASE_CATEGORY = "case_category";
	private static final String COLUMN_CASE_TYPE = "case_type";
	private static final String COLUMN_FILE = "ic_file_id";
	private static final String COLUMN_TYPE = "type";
	private static final String COLUMN_HANDLER = "handler";
	private static final String COLUMN_IS_PRIVATE = "is_private";
	private static final String COLUMN_IS_ANONYMOUS = "is_anonymous";
	private static final String COLUMN_PRIORITY = "priority";
	private static final String COLUMN_TITLE = "title";
	private static final String COLUMN_WANT_REPLY = "want_reply";
	private static final String COLUMN_WANT_REPLY_EMAIL = "want_reply_email";
	private static final String COLUMN_WANT_REPLY_PHONE = "want_reply_phone";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.block.process.data.AbstractCaseBMPBean#getCaseCodeKey()
	 */
	public String getCaseCodeKey() {
		return CaseConstants.CASE_CODE_KEY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.block.process.data.AbstractCaseBMPBean#getCaseCodeDescription()
	 */
	public String getCaseCodeDescription() {
		return "General case";
	}

	public String getEntityName() {
		return ENTITY_NAME;
	}

	public void initializeAttributes() {
		addGeneralCaseRelation();

		addAttribute(COLUMN_MESSAGE, "Message", String.class, 4000);
		addAttribute(COLUMN_REPLY, "Reply", String.class, 4000);
		addAttribute(COLUMN_TYPE, "Type", String.class);
		addAttribute(COLUMN_IS_PRIVATE, "Is private", Boolean.class);
		addAttribute(COLUMN_IS_ANONYMOUS, "Is anonymous", Boolean.class);
		addAttribute(COLUMN_PRIORITY, "Priority", String.class);
		addAttribute(COLUMN_TITLE, "Title", String.class);
		addAttribute(COLUMN_WANT_REPLY, "Want reply", String.class);
		addAttribute(COLUMN_WANT_REPLY_EMAIL, "Email reply", String.class);
		addAttribute(COLUMN_WANT_REPLY_PHONE, "Phone reply", String.class);

		addManyToOneRelationship(COLUMN_CASE_CATEGORY, CaseCategory.class);
		addManyToOneRelationship(COLUMN_CASE_TYPE, CaseType.class);
		addManyToOneRelationship(COLUMN_FILE, ICFile.class);
		addManyToOneRelationship(COLUMN_HANDLER, User.class);
	}

	// Getters
	public String getMessage() {
		return getStringColumnValue(COLUMN_MESSAGE);
	}
	
	public String getReply() {
		String reply = getStringColumnValue(COLUMN_REPLY);

		if (reply == null || "".equals(reply)) {
			reply = getBody();
			if (reply == null) {
				reply = "";
			}
		}

		return reply;
	}

	public String getType() {
		return getStringColumnValue(COLUMN_TYPE);
	}

	public CaseCategory getCaseCategory() {
		return (CaseCategory) getColumnValue(COLUMN_CASE_CATEGORY);
	}

	public CaseType getCaseType() {
		return (CaseType) getColumnValue(COLUMN_CASE_TYPE);
	}

	public ICFile getAttachment() {
		return (ICFile) getColumnValue(COLUMN_FILE);
	}

	public User getHandledBy() {
		return (User) getColumnValue(COLUMN_HANDLER);
	}

	public boolean isPrivate() {
		return getBooleanColumnValue(COLUMN_IS_PRIVATE, false);
	}
	
	public boolean isAnonymous() {
		return getBooleanColumnValue(COLUMN_IS_ANONYMOUS, false);
	}

	public String getPriority() {
		return getStringColumnValue(COLUMN_PRIORITY);
	}

	public String getTitle() {
		return getStringColumnValue(COLUMN_TITLE);
	}

	public String getWantReply() {
		return getStringColumnValue(COLUMN_WANT_REPLY);
	}

	public String getWantReplyEmail() {
		return getStringColumnValue(COLUMN_WANT_REPLY_EMAIL);
	}

	public String getWantReplyPhone() {
		return getStringColumnValue(COLUMN_WANT_REPLY_PHONE);
	}

	// Setters
	public void setMessage(String message) {
		setColumn(COLUMN_MESSAGE, message);
	}
	
	public void setReply(String reply) {
		setColumn(COLUMN_REPLY, reply);
	}

	public void setType(String type) {
		setColumn(COLUMN_TYPE, type);
	}

	public void setCaseCategory(CaseCategory category) {
		setColumn(COLUMN_CASE_CATEGORY, category);
	}

	public void setCaseType(CaseType type) {
		setColumn(COLUMN_CASE_TYPE, type);
	}

	public void setAttachment(ICFile attachment) {
		setColumn(COLUMN_FILE, attachment);
	}

	public void setHandledBy(User handler) {
		setColumn(COLUMN_HANDLER, handler);
	}

	public void setAsPrivate(boolean isPrivate) {
		setColumn(COLUMN_IS_PRIVATE, isPrivate);
	}
	
	public void setAsAnonymous(boolean isAnonymous) {
		setColumn(COLUMN_IS_ANONYMOUS, isAnonymous);
	}

	public void setPriority(String priority) {
		setColumn(COLUMN_PRIORITY, priority);
	}

	public void setTitle(String title) {
		setColumn(COLUMN_TITLE, title);
	}

	public void setWantReply(String wantReply) {
		setColumn(COLUMN_WANT_REPLY, wantReply);
	}

	public void setWantReplyEmail(String wantReplyEmail) {
		setColumn(COLUMN_WANT_REPLY_EMAIL, wantReplyEmail);
	}

	public void setWantReplyPhone(String wantReplyPhone) {
		setColumn(COLUMN_WANT_REPLY_PHONE, wantReplyPhone);
	}
	
	// Finders
	public Collection ejbFindAllByGroup(Collection groups) throws FinderException {
		return ejbFindAllByGroupAndStatuses(groups, null, null);
	}

	/**
	 * 
	 * @param groups
	 * @param statuses
	 * @param caseManagerType - if caseHandlers is null, then it is not added to criteria list, but if it's empty, then the criteria is considered to be IS NULL
	 * @return
	 * @throws FinderException
	 */
	public Collection ejbFindAllByGroupAndStatuses(Collection groups, String[] statuses, String[] caseManagerType) throws FinderException {
		Table table = new Table(this);
		Table process = new Table(Case.class);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName(), true);
		try {
			query.addJoin(table, process);
		}
		catch (IDORelationshipException e) {
			e.printStackTrace();
			throw new FinderException(e.getMessage());
		}
		if (groups != null) {
			query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseHandlerColumnName()), groups));
		}
		if (statuses != null) {
			query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseCaseStatusColumnName()), statuses));
		}
		
		if (caseManagerType != null) {
			
			if(caseManagerType.length == 0) {
				
				query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseCaseManagerTypeColumnName())));
				
			} else {
			
				query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseCaseManagerTypeColumnName()), caseManagerType));
			}
		}

		log(Level.INFO, query.toString());
		return idoFindPKsByQuery(query);
	}

	public Collection ejbFindAllByHandler(User handler) throws FinderException {
		return ejbFindAllByHandlerAndStatuses(handler, null, null);
	}

	/**
	 * 
	 * @param handler
	 * @param statuses
	 * @param caseManagerType - if caseHandlers is null, then it is not added to criteria list, but if it's empty, then the criteria is considered to be IS NULL
	 * @return
	 * @throws FinderException
	 */
	public Collection ejbFindAllByHandlerAndStatuses(User handler, String[] statuses, String[] caseManagerType) throws FinderException {
		Table table = new Table(this);
		Table process = new Table(Case.class);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName(), true);
		try {
			query.addJoin(table, process);
		}
		catch (IDORelationshipException e) {
			e.printStackTrace();
			throw new FinderException(e.getMessage());
		}
		query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_HANDLER), MatchCriteria.EQUALS, handler));
		if (statuses != null) {
			query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseCaseStatusColumnName()), statuses));
		}
		
		if (caseManagerType != null) {
			
			if(caseManagerType.length == 0) {
				
				query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseCaseManagerTypeColumnName())));
				
			} else {
			
				query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseCaseManagerTypeColumnName()), caseManagerType));
			}
		}

		return idoFindPKsByQuery(query);
	}

	public Collection ejbFindAllByUsers(Collection users) throws FinderException {
		Table table = new Table(this);
		Table process = new Table(Case.class);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName(), true);
		try {
			query.addJoin(table, process);
		}
		catch (IDORelationshipException e) {
			e.printStackTrace();
			throw new FinderException(e.getMessage());
		}
		query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseUserColumnName()), users));

		return idoFindPKsByQuery(query);
	}
	
	public Collection ejbFindAllByMessage(String message) throws FinderException {
		Table table = new Table(this);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName(), true);
		query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_MESSAGE), MatchCriteria.LIKE, "%" + message + "%"));

		return idoFindPKsByQuery(query);
	}
	
	public Collection ejbFindByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous) throws FinderException {
		return ejbFindByCriteria(parentCategory, category, type, status, anonymous, null);
	}
	
	public Collection ejbFindByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Date fromDate, Date toDate, Boolean anonymous) throws FinderException {
		Table table = new Table(this);
		Table process = new Table(Case.class);
		Table categories = new Table(CaseCategory.class);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName());
		try {
			query.addJoin(table, process);
		}
		catch (IDORelationshipException e) {
			e.printStackTrace();
			throw new FinderException(e.getMessage());
		}

		if (parentCategory != null) {
			if (category == null) {
				try {
					query.addJoin(table, categories);
				}
				catch (IDORelationshipException e) {
					e.printStackTrace();
					throw new FinderException(e.getMessage());
				}
				query.addCriteria(new MatchCriteria(categories.getColumn("parent_category"), MatchCriteria.EQUALS, parentCategory));
			}
			else {
				query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_CASE_CATEGORY), MatchCriteria.EQUALS, category));
			}
		}
		if (type != null) {
			query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_CASE_TYPE), MatchCriteria.EQUALS, type));
		}
		if (status != null) {
			query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseCaseStatusColumnName()), MatchCriteria.EQUALS, status));
		}
		if (anonymous != null) {
			query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseUserColumnName()), !anonymous.booleanValue()));
		}
		if (fromDate != null) {
			query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseCreatedColumnName()), MatchCriteria.GREATEREQUAL, fromDate));
		}
		if (toDate != null) {
			query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseCreatedColumnName()), MatchCriteria.LESSEQUAL, toDate));
		}

		query.addOrder(new Order(process.getColumn(getSQLGeneralCaseCreatedColumnName()), true));

		System.out.println(query.toString());
		return idoFindPKsByQuery(query);
	}
	
	public Collection ejbFindByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous, String caseManagerType) throws FinderException {

		Table table = new Table(this);
		Table process = new Table(Case.class);
		Table categories = new Table(CaseCategory.class);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName());
		try {
			query.addJoin(table, process);
		}
		catch (IDORelationshipException e) {
			e.printStackTrace();
			throw new FinderException(e.getMessage());
		}

		if (parentCategory != null) {
			if (category == null) {
				try {
					query.addJoin(table, categories);
				}
				catch (IDORelationshipException e) {
					e.printStackTrace();
					throw new FinderException(e.getMessage());
				}
				query.addCriteria(new MatchCriteria(categories.getColumn("parent_category"), MatchCriteria.EQUALS, parentCategory));
			}
			else {
				query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_CASE_CATEGORY), MatchCriteria.EQUALS, category));
			}
		}
		if (type != null) {
			query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_CASE_TYPE), MatchCriteria.EQUALS, type));
		}
		if (status != null) {
			query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseCaseStatusColumnName()), MatchCriteria.EQUALS, status));
		}
		if (anonymous != null) {
			query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseUserColumnName()), !anonymous.booleanValue()));
		}
		if (caseManagerType != null) {
			query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseCaseManagerTypeColumnName()), MatchCriteria.EQUALS, caseManagerType));
		}

		query.addOrder(new Order(process.getColumn(getSQLGeneralCaseCreatedColumnName()), true));

		return idoFindPKsByQuery(query);
	}

	public int ejbHomeGetCountByGroup(Collection groups) throws IDOException {
		return ejbHomeGetCountByGroupAndStatuses(groups, null);
	}

	public int ejbHomeGetCountByGroupAndStatuses(Collection groups, String[] statuses) throws IDOException {
		Table table = new Table(this);
		Table process = new Table(Case.class);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(new CountColumn(table, getIDColumnName()));
		try {
			query.addJoin(table, process);
		}
		catch (IDORelationshipException e) {
			e.printStackTrace();
			throw new IDOException(e.getMessage());
		}
		query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseCaseStatusColumnName()), groups));
		if (statuses != null) {
			query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseCaseStatusColumnName()), statuses));
		}

		return idoGetNumberOfRecords(query);
	}
}