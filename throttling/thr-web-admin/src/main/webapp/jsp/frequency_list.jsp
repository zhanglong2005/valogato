<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://code.google.com/p/jmesa" prefix="jmesa" %> 

<!-- <link rel="stylesheet" href="./css/mydisplaytag.css" type="text/css"/> -->
<link rel="stylesheet" href="./css/throttling.css" type="text/css"/>
<link rel="stylesheet" href="./css/jmesa.css" type="text/css"/>

<script type="text/javascript" src="./js/jquery-1.3.min.js"></script>
<script type="text/javascript" src="./js/jquery.validate.js"></script>
<script type="text/javascript" src="./js/jmesa.js"></script>
<script type="text/javascript" src="./js/jquery.jmesa.js"></script>

<script type="text/javascript">
   function onInvokeExportAction(id) {
      var parameterString = createParameterStringForLimit(id);
      location.href = '<s:url action="list_frequency.action" />?' + parameterString;
   }
   function onInvokeAction(id) {
       $.jmesa.setExportToLimit(id, '');
       $.jmesa.createHiddenInputFieldsForLimitAndSubmit(id);
   }
</script>

<div id="content_header">The frequency of the usage of backend services / List</div>

<table>
   <tr>
      <td class="displaytag_background">

         <s:form action="list_frequency.action" id="jMesaForm" acceptcharset="UTF-8" theme="simple"> 
            <jmesa:struts2TableModel id="jMesaTable" items="${usageList}" editable="false" maxRows="10" maxRowsIncrements="10,15,25,50" 
                                     stateAttr="restore" var="row"> 
               <jmesa:htmlTable width="90%" styleClass="jmesa">
                  <jmesa:htmlRow> 
                     <jmesa:htmlColumn property="nameBackendService" title="Backend Service" width="500" 
                     	               cellEditor="org.vhorvath.valogato.web.jmesa.JMesaCell500Px" />
                     <jmesa:htmlColumn property="frequency" title="Frequency" width="100" 
                                       cellEditor="org.vhorvath.valogato.web.jmesa.JMesaCell100Px"/>
                     <jmesa:htmlColumn property="sleepingRequests" title="Sleeping requests" width="100" 
                                       cellEditor="org.vhorvath.valogato.web.jmesa.JMesaCell100Px"/>
                     <jmesa:htmlColumn width="365" filterable="false">
                        <s:url id="updateFrequencyUrl" action="update_frequency.action">
                           <s:param name="nameBackendService" value="#attr.row.nameBackendService" />
                        </s:url>
                        <s:a href="%{updateFrequencyUrl}" theme="simple" id="command_link">Update</s:a>&nbsp;&nbsp;&nbsp;
                        <s:url id="resetFrequencyUrl" action="reset_frequency.action">
                           <s:param name="nameBackendService" value="#attr.row.nameBackendService" />
                        </s:url>
                        <s:a href="%{resetFrequencyUrl}" theme="simple" id="command_link" onclick="return confirm('Are you sure you want to reset the frequency?')">Reset Fr.</s:a>&nbsp;&nbsp;&nbsp;
                        <s:url id="resetSleepingReqUrl" action="reset_sleepingreq.action">
                           <s:param name="nameBackendService" value="#attr.row.nameBackendService" />
                        </s:url>
                        <s:a href="%{resetSleepingReqUrl}" theme="simple" id="command_link" onclick="return confirm('Are you sure you want to reset the sleeping requests?')">Reset S. req.</s:a>
                     </jmesa:htmlColumn>
                  </jmesa:htmlRow> 
               </jmesa:htmlTable> 
            </jmesa:struts2TableModel> 
         </s:form>
                
       </td>
	  
  </tr>
  <tr>
     <td>
        <span style="display: table-cell;">
           <s:form action="reset_all_frequency.action" method="post">
              <s:submit value="Reset all Frequencies" onclick="return confirm('Are you sure you want to reset ALL the frequencies?')"/>
           </s:form>
        </span>
        <span style="display: table-cell;">
           <s:form action="reset_all_sleepingreq.action" method="post">
              <s:submit value="Reset all Waiting request values" onclick="return confirm('Are you sure you want to reset ALL the sleeping requests?')"/>
           </s:form>
        </span>
     </td>
  </tr>
</table>
