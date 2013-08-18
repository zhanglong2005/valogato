<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://code.google.com/p/jmesa" prefix="jmesa" %>

<link rel="stylesheet" href="./css/throttling.css" type="text/css"/>
<link rel="stylesheet" href="./css/jmesa.css" type="text/css"/>

<script type="text/javascript" src="./js/jquery-1.3.min.js"></script>
<script type="text/javascript" src="./js/jquery.validate.js"></script>
<script type="text/javascript" src="./js/jmesa.js"></script>
<script type="text/javascript" src="./js/jquery.jmesa.js"></script>

<script type="text/javascript">
   function onInvokeExportAction(id) {
      var parameterString = createParameterStringForLimit(id);
      location.href = '<s:url action="list_backendservice_config.action" />?' + parameterString;
   }
   function onInvokeAction(id) {
       $.jmesa.setExportToLimit(id, '');
       $.jmesa.createHiddenInputFieldsForLimitAndSubmit(id);
   }
</script>

<div id="content_header">Backend Service Configuration / List</div>

<table>
   <tr>
      <td class="displaytag_background">
      
         <s:form action="list_backendservice_config.action" id="jMesaForm" acceptcharset="UTF-8" theme="simple"> 
            <jmesa:struts2TableModel id="jMesaTable" items="${backendServiceConfigList}" editable="false" maxRows="10" 
                                     maxRowsIncrements="10,15,25,50" stateAttr="restore" var="row"> 
               <jmesa:htmlTable width="90%" styleClass="jmesa"> 
                  <jmesa:htmlRow> 
                     <jmesa:htmlColumn property="backendService" title="Backend Service" width="300" 
                     	               cellEditor="org.vhorvath.valogato.web.jmesa.JMesaCell300Px" />
                     <jmesa:htmlColumn property="maxLoading" title="Max Loading" width="100" 
                                       cellEditor="org.vhorvath.valogato.web.jmesa.JMesaCell100Px"/>
                     <jmesa:htmlColumn property="averageResponseTime" title="Average Response Time" width="100" 
                                       cellEditor="org.vhorvath.valogato.web.jmesa.JMesaCell100Px"/>
                     <jmesa:htmlColumn property="features" title="Features" width="200" 
                                       cellEditor="org.vhorvath.valogato.web.jmesa.JMesaCell200Px"/>
                     <jmesa:htmlColumn property="simulatedServices" title="Simulated Services" width="200" 
                                       cellEditor="org.vhorvath.valogato.web.jmesa.JMesaCell200Px"/>
                     <jmesa:htmlColumn width="190" filterable="false">
                        <s:url id="updateUrl" action="update_backendservice_config.action">
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
  <tr>
     <td>
        <s:form action="list_backendservice_config.action" method="post">
           <s:submit name="buttonName" value="Export as XML" theme="simple" />
        </s:form>
     </td>
  </tr>
</table>
