<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://code.google.com/p/jmesa" prefix="jmesa" %>

<link rel="stylesheet" href="./css/mydisplaytag.css" type="text/css"/>
<link rel="stylesheet" href="./css/jmesa.css" type="text/css"/>

<script type="text/javascript" src="./js/jquery-1.3.min.js"></script>
<script type="text/javascript" src="./js/jquery.validate.js"></script>
<script type="text/javascript" src="./js/jmesa.js"></script>
<script type="text/javascript" src="./js/jquery.jmesa.js"></script>

<script type="text/javascript">
   /* for changing the value in combobox */ 
   function submitForm() {
      var hidden = document.getElementById("update_backendservice_config_submitType");
      hidden.value = "changedFeature";
      document.backendservice_config_form.submit();
   }
   /* for jmesa table */
   function onInvokeExportAction(id) {
      var parameterString = createParameterStringForLimit(id);
      location.href = '<s:url action="update_backendservice_config.action" />?' + parameterString;
   }
   function onInvokeAction(id) {
      $.jmesa.setExportToLimit(id, '');
      $.jmesa.createHiddenInputFieldsForLimitAndSubmit(id);
   }
</script>

<div id="content_header">Backend Service Configuration / List / Update (<s:label value="%{nameBackendService}" theme="simple"/>)</div>

<!-- http://stackoverflow.com/questions/12948226/onchange-event-in-struts2 -->
<!-- http://mail-archives.apache.org/mod_mbox/struts-user/200712.mbox/%3C309414.98768.qm@web56704.mail.re3.yahoo.com%3E -->
<s:form action="update_backendservice_config.action" method="post" name="backendservice_config_form" theme="simple">
   <s:hidden value="%{nameBackendService}" name="nameBackendService"/>
   <s:hidden value="Save" name="submitType" id="update_backendservice_config_submitType"/>

   <table class="edit_background">
      <tr>
         <td>Backend Service:</td>
         <td><b><s:label value="%{nameBackendService}"/></b></td>
      </tr>
      <tr>
         <td>Max Loading:</td>
         <td><s:textfield name="maxLoading" size="8" maxlength="8"/></td>
      </tr>
      <tr>
         <td>Average Response Time (in millisec):</td>
         <td><s:textfield name="averageResponseTime" size="6" maxlength="6"/></td>
      </tr>
      <tr>
         <td colspan="2"><hr></td>
      </tr>
      <tr>
         <td colspan="2"><s:include value="/jsp/feature_update.jsp"/></td>
      </tr>
      <tr>
         <td colspan="2">
            <font color="red"><s:fielderror/></font>
         </td>
      </tr>
      <tr>
         <td colspan="2">
            <s:submit name="buttonName" value="Save" align="center" theme="simple"/>
            <s:submit name="buttonName" value="Cancel" align="center" theme="simple"/>
         </td>
      </tr>
   </table>
</s:form>

<s:if test="simulatedServiceList != null && simulatedServiceList.size > 0">
   <br>
   <table class="displaytag_background">
      <tr>
         <td><b>Simulated service being controlled individually</b></td>
      </tr>
      <tr>
         <td>

            <s:form action="update_backendservice_config.action" id="jMesaForm" acceptcharset="UTF-8" theme="simple">
               <s:hidden value="%{nameBackendService}" name="nameBackendService"/>
               <jmesa:struts2TableModel id="jMesaTable" items="${simulatedServiceList}" editable="false" maxRows="5" 
                                        maxRowsIncrements="5,10,20,40" stateAttr="restore" var="row"> 
                  <jmesa:htmlTable width="90%" styleClass="jmesa"> 
                     <jmesa:htmlRow> 
                        <jmesa:htmlColumn property="simulatedService" title="Simulated Service" width="300" 
                                          cellEditor="org.vhorvath.valogato.web.jmesa.JMesaCell300Px" />
                        <jmesa:htmlColumn property="feature" title="Feature" width="300" 
                                          cellEditor="org.vhorvath.valogato.web.jmesa.JMesaCell300Px"/>
                        <jmesa:htmlColumn width="100" filterable="false">
                           <s:url id="updateUrl" action="update_simulatedservice_config.action">
                              <s:param name="nameSimulatedService" value="#attr.row.simulatedService" />
                              <s:param name="nameBackendService" value="#attr.row.backendService" />
                           </s:url>
                           <s:a href="%{updateUrl}" theme="simple" id="command_link">Update</s:a>
                        </jmesa:htmlColumn>
                     </jmesa:htmlRow> 
                  </jmesa:htmlTable> 
               </jmesa:struts2TableModel> 
            </s:form>

         </td>
      </tr>
   </table>
</s:if>