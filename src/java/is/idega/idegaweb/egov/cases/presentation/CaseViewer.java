/*
 * $Id$
 * Created on Oct 31, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.GeneralCase;

import java.rmi.RemoteException;

import javax.ejb.FinderException;

import com.idega.business.IBORuntimeException;
import com.idega.core.builder.data.ICPage;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Label;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.text.Name;


public class CaseViewer extends CaseCreator {
	
	private ICPage iHomePage;

	protected void present(IWContext iwc) {
		try {
			IWResourceBundle iwrb = getResourceBundle(iwc);
			
			Form form = new Form();
			form.setStyleClass("casesForm");
			
			GeneralCase theCase = null;
			try {
				theCase = getCasesBusiness(iwc).getGeneralCase(iwc.getParameter(getCasesBusiness(iwc).getSelectedCaseParameter()));
			}
			catch (FinderException fe) {
				fe.printStackTrace();
				throw new IBORuntimeException(fe);
			}
			CaseCategory category = theCase.getCaseCategory();
			User user = getCasesBusiness(iwc).getLastModifier(theCase);
			IWTimestamp created = new IWTimestamp(theCase.getCreated());
			
			form.add(getHeader(iwrb.getLocalizedString("case_viewer.view_case", "View case")));

			form.add(getPersonInfo(iwc, theCase.getOwner()));
			
			Layer clearLayer = new Layer(Layer.DIV);
			clearLayer.setStyleClass("Clear");
			
			Layer caseType = new Layer(Layer.SPAN);
			caseType.add(new Text(category.getName()));
			
			Layer message = new Layer(Layer.SPAN);
			message.add(new Text(theCase.getMessage()));
			
			Layer createdDate = new Layer(Layer.SPAN);
			createdDate.add(new Text(created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));
			
			Heading1 heading = new Heading1(iwrb.getLocalizedString("case_overview", "Case overview"));
			heading.setStyleClass("subHeader");
			heading.setStyleClass("topSubHeader");
			form.add(heading);
			
			Layer section = new Layer(Layer.DIV);
			section.setStyleClass("formSection");
			form.add(section);
			
			Layer formItem = new Layer(Layer.DIV);
			Label label = new Label();
			
			formItem.setStyleClass("formItem");
			label = new Label();
			label.setLabel(iwrb.getLocalizedString("case_type", "Case type"));
			formItem.add(label);
			formItem.add(caseType);
			section.add(formItem);
	
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label();
			label.setLabel(iwrb.getLocalizedString("created_date", "Created date"));
			formItem.add(label);
			formItem.add(createdDate);
			section.add(formItem);
	
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("informationItem");
			label = new Label();
			label.setLabel(iwrb.getLocalizedString("message", "Message"));
			formItem.add(label);
			formItem.add(message);
			section.add(formItem);
	
			Layer clear = new Layer(Layer.DIV);
			clear.setStyleClass("Clear");
			section.add(clear);
			
			if (!theCase.getCaseStatus().equals(getCasesBusiness(iwc).getCaseStatusOpen())) {
				Layer handler = new Layer(Layer.SPAN);
				handler.add(new Text(new Name(user.getFirstName(), user.getMiddleName(), user.getLastName()).getName(iwc.getCurrentLocale(), true)));
				
				Layer reply = new Layer(Layer.SPAN);
				reply.add(new Text(theCase.getReply()));
			
				heading = new Heading1(iwrb.getLocalizedString("handler_overview", "Handler overview"));
				heading.setStyleClass("subHeader");
				heading.setStyleClass("topSubHeader");
				form.add(heading);
				
				section = new Layer(Layer.DIV);
				section.setStyleClass("formSection");
				form.add(section);
				
				formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				label = new Label();
				label.setLabel(iwrb.getLocalizedString("handler", "Handler"));
				formItem.add(label);
				formItem.add(handler);
				section.add(formItem);
		
				formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				formItem.setStyleClass("informationItem");
				label = new Label();
				label.setLabel(iwrb.getLocalizedString("reply", "Reply"));
				formItem.add(label);
				formItem.add(reply);
				section.add(formItem);
		
				section.add(clear);
			}
		
			Layer bottom = new Layer(Layer.DIV);
			bottom.setStyleClass("bottom");
			form.add(bottom);

			Link home = getButtonLink(iwrb.getLocalizedString("my_page", "My page"));
			home.setStyleClass("buttonHome");
			if (getHomePage() != null) {
				home.setPage(getHomePage());
			}
			bottom.add(home);

			add(form);
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}
	
	protected ICPage getHomePage() {
		return iHomePage;
	}
	
	public void setHomePage(ICPage page) {
		iHomePage = page;
	}
}