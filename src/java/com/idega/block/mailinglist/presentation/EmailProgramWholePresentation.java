package com.idega.block.mailinglist.presentation;

import com.idega.block.mailinglist.data.*;
import com.idega.block.mailinglist.business.*;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.SelectionBox;
import com.idega.presentation.Table;
import com.idega.presentation.IWContext;
import com.idega.presentation.Block;
import com.idega.data.EntityFinder;
import java.util.List;
import java.util.Iterator;
import java.sql.SQLException;
import java.io.IOException;
import javax.mail.MessagingException;
import javax.mail.Folder;


/**
 * Title:        idegaWeb Classes
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      idega
 * @author <a href="bjarni@idega.is">Bjarni Viljhalmsson</a>
 * @version 1.0
 */

public class EmailProgramWholePresentation extends Block {

  Table wholeTable = new Table(2,2);

  private static int currentOpenWindow;
  private static int currentOpenLetterID;
  private boolean usesProperties = false;
  private int chosenProperty = 0;

  public EmailProgramWholePresentation() {
//    System.err.println("!!! EmailProgramWholePresentation is built");
  }

  public void eventController(IWContext modinfo) throws SQLException{

    //Maybe it�s better to do an for-loop here and use the numbers to do a switch instead of if-sentances
    //MailingListBusiness.addInboxData("rend mig i roven", "blablaablabal", "bjv@stones.com", "gimmi@mofo.com", "addsfaafCC");

    //Edit chosen mailinglist
    if ((modinfo.isParameterSet(EmailProgramSideTable.mailinglistSettingsButtonName)) /*|| (modinfo.isParameterSet(submitAndRemoveParameter))*/){
      System.err.println("Edit Mailinglist");
      currentOpenWindow = 1;
    }

   //Your Email Settings
    else if (modinfo.isParameterSet(EmailProgramTopTable.settingsButtonName)){
      System.err.println("settingsButtonName");
      currentOpenWindow = 2;
    }

    //New Mailinglist
    else if (modinfo.isParameterSet(EmailProgramTopTable.newMailinglistButtonName)){
      System.err.println("newMailinglistButtonName");
      currentOpenWindow = 3;
    }


    //Some setting applied.
    else if (modinfo.isParameterSet(AccountSettingsTable.OKButtonName)){
      //System.err.println("modinfo.getParameter(mailinglistDropDownMenuName) = "+modinfo.getParameter(mailinglistDropDownMenuName));
      //edit chosen mailinglist
      switch (currentOpenWindow) {
        //this mailinglist
        case 1:
          MailingListBusiness.updateMailinglistBusiness(modinfo, modinfo.getParameter(MailinglistSettingsTable.nameInputName),
                     modinfo.getParameter(MailinglistSettingsTable.emailInputName), modinfo.getParameter(MailinglistSettingsTable.pop3HostInputName));
          break;
        //this users settings
        case 2:
          break;
        //a new mailinglist
        case 3:  MailingListBusiness.addMailinglistBusiness(modinfo, modinfo.getParameter(MailinglistSettingsTable.nameInputName),
                     modinfo.getParameter(MailinglistSettingsTable.emailInputName), modinfo.getParameter(MailinglistSettingsTable.pop3HostInputName));
          break;
      }
    }

    //View Drafts
    else if (modinfo.isParameterSet(EmailProgramSideTable.draftsButtonName)){
      currentOpenWindow = 4;
    }
    //View Sent
    else if (modinfo.isParameterSet(EmailProgramSideTable.sentButtonName)){
      currentOpenWindow = 5;
    }
    //View Inbox
    else if (modinfo.isParameterSet(EmailProgramSideTable.inboxButtonName)){
      currentOpenWindow = 6;
    }
    //Delete Drafts/Inbox/Sent
    else if (modinfo.isParameterSet(EmailProgramListLetterTable.deleteButtonName)){
      String[] checkedBoxes;
      checkedBoxes = (String[]) modinfo.getParameterValues(EmailProgramListLetterTable.checkBoxName);
      if (currentOpenWindow == 6) {
        try {
          EmailServiceHandler.removeMessages(modinfo, checkedBoxes);
        }
        catch (MessagingException ex) {

        }

      }
      else {MailingListBusiness.removeEmailLetterDataBusiness(modinfo, checkedBoxes);}
    }
    //Start empty new letter
    else if (modinfo.isParameterSet(EmailProgramSideTable.newLetterButtonName)){
      currentOpenWindow = 7;
      currentOpenLetterID = -1;
    }

    //Start empty new letter on mailinglist
    else if (modinfo.isParameterSet(EmailProgramSideTable.newLetterOnMailinglistButtonName)){
      currentOpenWindow = 8;
      currentOpenLetterID = -1;
    }

    //View letters in Inbox
    else if (modinfo.isParameterSet(EmailProgramListLetterTable.viewInboxLetterLinkParameterName)){
      System.err.println("modinfo.getParameter(EmailProgramListLetterTable.viewInboxLetterLinkParameterName) = "+modinfo.getParameter(EmailProgramListLetterTable.viewInboxLetterLinkParameterName) );
      currentOpenLetterID = Integer.parseInt(modinfo.getParameter(EmailProgramListLetterTable.viewInboxLetterLinkParameterName));
      currentOpenWindow = 9;
    }
    //View letters in Sent/Drafts
    else if (modinfo.isParameterSet(EmailProgramListLetterTable.viewMyLetterLinkParameterName)){
      currentOpenLetterID = Integer.parseInt(modinfo.getParameter(EmailProgramListLetterTable.viewMyLetterLinkParameterName));
      currentOpenWindow = 10;
    }

    //Send letter
    else if (modinfo.isParameterSet(EmailProgramViewTable.sendLetterButtonName)){
     // System.err.println(" modinfo.getParameter(subjectInputName) = "+ modinfo.getParameter(subjectInputName) );
     // System.err.println(" modinfo.getParameter(letterTextAreaName) = "+ modinfo.getParameter(letterTextAreaName) );
     // System.err.println(" Boolean.TRUE = "+ Boolean.TRUE);
     // System.err.println(" modinfo.getParameter(adressInputName) = "+ modinfo.getParameter(adressInputName) );
      currentOpenLetterID = MailingListBusiness.addEmailLetterDataBusiness(modinfo);
      EmailLetterData letter = new EmailLetterData(currentOpenLetterID);

      try {
        EmailServices.sendServices( letter);
      }
      catch (Exception ex) {
      }

      currentOpenWindow = 10;
    }

    //Save letter
    else if (modinfo.isParameterSet(EmailProgramViewTable.saveLetterButtonName)){
      if (currentOpenLetterID != -1) {
        EmailLetterData letter = new EmailLetterData(currentOpenLetterID);
        if (letter.getHasSent()) currentOpenLetterID = -1;
        System.out.println("!!!! SAVE letter.getHasSent() = " +letter.getHasSent()+" 1 STIG");
      }
      currentOpenLetterID = MailingListBusiness.addEmailLetterDataBusiness( modinfo.getParameter(EmailProgramViewTable.subjectInputName),
                            modinfo.getParameter(EmailProgramViewTable.letterTextAreaName), Boolean.FALSE ,
                            modinfo.getParameter(EmailProgramViewTable.addressInputName), "a", //modinfo.getParameter(adressFromInputName),
                            modinfo.getParameter(EmailProgramViewTable.CCInputName), currentOpenLetterID);

      currentOpenWindow = 10;
    }

    //Reply letter
    else if (modinfo.isParameterSet(EmailProgramViewTable.replyLetterButtonName)){
      currentOpenWindow = 11;
    }

    //Forward letter
    else if (modinfo.isParameterSet(EmailProgramViewTable.forwardLetterButtonName)){
      currentOpenWindow = 12;
    }

    /*
    else if (first==0){
      currentOpenWindow=13;
      first=1;
    }
    */

    //System.out.println("OKButtonName ="+  modinfo.isParameterSet(OKButtonName));
    System.out.println("!!!!!!!CURRENT OPEN WINDOW = "+  String.valueOf(currentOpenWindow));

    /*System.err.println("modinfo.getParameter(EmailProgramListLetterTable.viewInboxLetterLinkParameterName) = "
    +modinfo.getParameter(EmailProgramListLetterTable.viewInboxLetterLinkParameterName));*/

    windowChooser(modinfo, currentOpenWindow);
  }

  //Why do I need modinfo in case 1!!!!???
  public void windowChooser(IWContext modinfo, int windowChooser) throws SQLException{
    switch (windowChooser) {
      case 1:{
        MailinglistSettingsTable settingsTable = new MailinglistSettingsTable();
        String chosenMailinglistId;
        chosenMailinglistId = modinfo.getParameter(EmailProgramSideTable.mailinglistDropDownMenuName);
        if (chosenMailinglistId != null) {
          Mailinglist chosenMailinglist = new Mailinglist(Integer.parseInt(chosenMailinglistId));
          settingsTable.setMailingListSettings(chosenMailinglist);
        }
        add(settingsTable);
      }
      break;
      case 2:{
        UserSettingsTable settingsTable = new UserSettingsTable();
        settingsTable.setUser(modinfo);
        add(settingsTable);
      }
      break;
      case 3:{
        MailinglistSettingsTable settingsTable = new MailinglistSettingsTable();
        add(settingsTable);
      }
      break;
      case 4:{
        EmailProgramListLetterTable emailProgramListLetterTable = new EmailProgramListLetterTable();
        emailProgramListLetterTable.setShowDrafts(modinfo);
        add(emailProgramListLetterTable);
      }
      break;
      case 5:{
        EmailProgramListLetterTable emailProgramListLetterTable = new EmailProgramListLetterTable();
        emailProgramListLetterTable.setShowSentLetters(modinfo);
        add(emailProgramListLetterTable);
      }
      break;
      case 6:{
        EmailProgramListLetterTable emailProgramListLetterTable = new EmailProgramListLetterTable();
        try {
          emailProgramListLetterTable.setShowInboxLetters(modinfo);
        }
        catch (MessagingException ex) {}

        add(emailProgramListLetterTable);
      }
      break;
      case 7:{
        EmailProgramViewTable emailProgramViewTable = new EmailProgramViewTable();
        emailProgramViewTable.setClearNewLetter();
        //even though the methood is empty this could simplify possible changes
        add(emailProgramViewTable);
      }
      break;
      case 8:{
        EmailProgramViewTable emailProgramViewTable = new EmailProgramViewTable();
        emailProgramViewTable.setNewLetterOnChosenEmails(modinfo);
        add(emailProgramViewTable);
      }
      break;
      case 9:{
        EmailProgramViewTable emailProgramViewTable = new EmailProgramViewTable();
        try {
          Folder inbox = EmailServiceHandler.getInbox(modinfo);
          try {emailProgramViewTable.setViewInboxLetter(inbox.getMessage(currentOpenLetterID));}
          catch (IOException ex) {}
        }
        catch (MessagingException ex) {}
        add(emailProgramViewTable);
      }
      break;
      case 10:{
        EmailProgramViewTable emailProgramViewTable = new EmailProgramViewTable();

        //Fails maybe because of deletion of the linkParameter???
        EmailLetterData letter = new EmailLetterData(currentOpenLetterID);
        emailProgramViewTable.setViewDraftLetter(letter);
        add(emailProgramViewTable);
      }
      break;
      case 11:{
        EmailProgramViewTable emailProgramViewTable = new EmailProgramViewTable();
        /** @todo FORWARD*/
        //InboxData letter = new InboxData(currentOpenLetterID);
        //emailProgramViewTable.setForwardLetterView(letter);
        currentOpenWindow = 10;
        add(emailProgramViewTable);
      }
      break;
      case 12:{
        EmailProgramViewTable emailProgramViewTable = new EmailProgramViewTable();
        /** @todo REPLY*/
        //InboxData letter = new InboxData(currentOpenLetterID);
        //emailProgramViewTable.setReplyLetterView(letter);
        currentOpenWindow = 10;
        add(emailProgramViewTable);
      }
      break;
      case 13:{
        EmailProgramStartScreen emailProgramStartScreen = new EmailProgramStartScreen();
        currentOpenWindow = 13;
        add(emailProgramStartScreen);
      }
      break;
    }
  }

  public void setProperty(String propertyName, String[] propertyValues){
    usesProperties = true;
    if (propertyName.equalsIgnoreCase("windowPart")) {
      if ((propertyValues.length == 1) && (propertyValues[0].equalsIgnoreCase("sideMenu"))) {
        chosenProperty = 1;
      }
      else if ((propertyValues.length == 1) && (propertyValues[0].equalsIgnoreCase("topMenu"))) {
        chosenProperty = 2;
      }
      else if ((propertyValues.length == 1) && (propertyValues[0].equalsIgnoreCase("viewWindow"))) {
        chosenProperty = 3;
      }
    }
  }

  public void setProperty(String propertyName, String propertyValue){
    usesProperties = true;
  //  System.err.println("!!! propertyName = "+propertyName);
    if (propertyName.equalsIgnoreCase("windowPart")) {
      if (propertyValue.equalsIgnoreCase("sideMenu")) {
        chosenProperty = 1;
      }
      else if (propertyValue.equalsIgnoreCase("topMenu")) {
        chosenProperty = 2;
      }
      else if (propertyValue.equalsIgnoreCase("viewWindow")) {
        chosenProperty = 3;
      }
    }
  }

  public void setSideMenu(IWContext modinfo) throws SQLException{
    EmailProgramSideTable sideTablePart = new EmailProgramSideTable();
    sideTablePart.setSelectionBox(modinfo);
    sideTablePart.setShowNumberOfLetters(modinfo);
    add(sideTablePart);
  }

  public void setTopMenu(){
    EmailProgramTopTable topTablePart = new EmailProgramTopTable();
    add(topTablePart);
  }

  public void setViewWindow(IWContext modinfo) throws SQLException{
    eventController(modinfo);
  }

  public void main(IWContext modinfo) throws SQLException{
    if (usesProperties) {
      if (chosenProperty == 1) {
        setSideMenu(modinfo);
      }
      else if (chosenProperty == 2) {
        setTopMenu();
      }
      else if (chosenProperty == 3) {
        setViewWindow(modinfo);
      }
    }
    else{
      setSideMenu(modinfo);
      setViewWindow(modinfo);
      setTopMenu();
    }
  }
}